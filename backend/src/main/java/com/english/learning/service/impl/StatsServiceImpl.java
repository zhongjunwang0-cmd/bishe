package com.english.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.english.learning.entity.LearningRecord;
import com.english.learning.entity.UserVocab;
import com.english.learning.service.LearningRecordService;
import com.english.learning.service.StatsService;
import com.english.learning.service.UserService;
import com.english.learning.service.UserVocabService;
import com.english.learning.util.LearningRecordTypeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatsServiceImpl implements StatsService {

    private static final String[] WEEKDAY_LABELS = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
    private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("M月", Locale.CHINA);

    @Autowired
    private LearningRecordService learningRecordService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserVocabService userVocabService;

    @Override
    public Map<String, Object> getStats(Long userId, boolean global) {
        QueryWrapper<LearningRecord> qw = new QueryWrapper<>();
        if (!global) {
            qw.eq("user_id", userId);
        }
        List<LearningRecord> records = learningRecordService.list(qw);

        Map<String, Object> result = new HashMap<>();
        result.put("summary", buildSummary(records, userId, global));
        result.put("durationTrend", global ? buildMonthlyDurationTrend(records) : buildWeeklyDurationTrend(records));
        result.put("moduleDistribution", buildModuleDistribution(records));
        result.put("scoreTrend", global ? buildMonthlyScoreTrend(records) : buildWeeklyScoreTrend(records));
        return result;
    }

    private Map<String, Object> buildSummary(List<LearningRecord> records, Long userId, boolean global) {
        Map<String, Object> summary = new HashMap<>();

        int totalDurationSecs = records.stream()
                .mapToInt(r -> r.getDuration() != null ? r.getDuration() : 0)
                .sum();
        summary.put("totalDurationHours", Math.round(totalDurationSecs / 36.0) / 100.0);
        summary.put("totalRecords", records.size());

        long litCount = records.stream()
                .filter(r -> "LIT".equals(LearningRecordTypeUtil.normalize(r.getType())))
                .count();
        summary.put("litCompleted", litCount);

        if (global) {
            summary.put("totalUsers", userService.count());
            summary.put("todayActiveUsers", countTodayActiveUsers(records));
            summary.put("moduleTypes", records.stream()
                    .map(r -> LearningRecordTypeUtil.normalize(r.getType()))
                    .distinct()
                    .count());
        } else {
            QueryWrapper<UserVocab> vocabQw = new QueryWrapper<>();
            vocabQw.eq("user_id", userId).eq("status", "MASTERED");
            summary.put("masteredVocab", userVocabService.count(vocabQw));

            LocalDate today = LocalDate.now();
            List<Integer> todayScores = records.stream()
                    .filter(r -> r.getScore() != null && r.getCreateTime() != null
                            && r.getCreateTime().toLocalDate().equals(today))
                    .map(LearningRecord::getScore)
                    .collect(Collectors.toList());

            if (todayScores.isEmpty()) {
                summary.put("todayScore", null);
            } else {
                int avg = (int) Math.round(todayScores.stream().mapToInt(Integer::intValue).average().orElse(0));
                summary.put("todayScore", avg);
            }
            summary.put("todayScoreMax", 100);
        }

        return summary;
    }

    private long countTodayActiveUsers(List<LearningRecord> records) {
        LocalDate today = LocalDate.now();
        return records.stream()
                .filter(r -> r.getCreateTime() != null && r.getCreateTime().toLocalDate().equals(today))
                .map(LearningRecord::getUserId)
                .distinct()
                .count();
    }

    private Map<String, Object> buildWeeklyDurationTrend(List<LearningRecord> records) {
        LocalDate weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        List<String> categories = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            categories.add(WEEKDAY_LABELS[i]);
            int minutes = records.stream()
                    .filter(r -> r.getCreateTime() != null && r.getCreateTime().toLocalDate().equals(day))
                    .mapToInt(r -> r.getDuration() != null ? r.getDuration() : 0)
                    .sum() / 60;
            values.add(minutes);
        }

        Map<String, Object> trend = new HashMap<>();
        trend.put("categories", categories);
        trend.put("values", values);
        return trend;
    }

    private Map<String, Object> buildMonthlyDurationTrend(List<LearningRecord> records) {
        LocalDate now = LocalDate.now();
        List<String> categories = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            categories.add(month.format(MONTH_LABEL));
            int year = month.getYear();
            int hours = records.stream()
                    .filter(r -> {
                        if (r.getCreateTime() == null) return false;
                        LocalDate d = r.getCreateTime().toLocalDate();
                        return d.getYear() == year && d.getMonth() == month.getMonth();
                    })
                    .mapToInt(r -> r.getDuration() != null ? r.getDuration() : 0)
                    .sum() / 3600;
            values.add(hours);
        }

        Map<String, Object> trend = new HashMap<>();
        trend.put("categories", categories);
        trend.put("values", values);
        return trend;
    }

    private List<Map<String, Object>> buildModuleDistribution(List<LearningRecord> records) {
        Map<String, Long> grouped = records.stream()
                .collect(Collectors.groupingBy(
                        r -> LearningRecordTypeUtil.toDisplayName(r.getType()),
                        Collectors.counting()));

        return grouped.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", e.getKey());
                    item.put("value", e.getValue());
                    return item;
                })
                .collect(Collectors.toList());
    }

    private Map<String, Object> buildWeeklyScoreTrend(List<LearningRecord> records) {
        LocalDate weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        List<String> categories = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            categories.add(WEEKDAY_LABELS[i]);
            List<Integer> dayScores = records.stream()
                    .filter(r -> r.getScore() != null && r.getCreateTime() != null
                            && r.getCreateTime().toLocalDate().equals(day))
                    .map(LearningRecord::getScore)
                    .collect(Collectors.toList());
            values.add(dayScores.isEmpty() ? 0
                    : (int) Math.round(dayScores.stream().mapToInt(Integer::intValue).average().orElse(0)));
        }

        Map<String, Object> trend = new HashMap<>();
        trend.put("categories", categories);
        trend.put("values", values);
        return trend;
    }

    private Map<String, Object> buildMonthlyScoreTrend(List<LearningRecord> records) {
        LocalDate now = LocalDate.now();
        List<String> categories = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            categories.add(month.format(MONTH_LABEL));
            List<Integer> monthScores = records.stream()
                    .filter(r -> {
                        if (r.getScore() == null || r.getCreateTime() == null) return false;
                        LocalDate d = r.getCreateTime().toLocalDate();
                        return d.getYear() == month.getYear() && d.getMonth() == month.getMonth();
                    })
                    .map(LearningRecord::getScore)
                    .collect(Collectors.toList());
            values.add(monthScores.isEmpty() ? 0
                    : (int) Math.round(monthScores.stream().mapToInt(Integer::intValue).average().orElse(0)));
        }

        Map<String, Object> trend = new HashMap<>();
        trend.put("categories", categories);
        trend.put("values", values);
        return trend;
    }
}
