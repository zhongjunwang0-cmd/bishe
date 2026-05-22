package com.english.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.english.learning.dto.KtRecommendDto;
import com.english.learning.dto.PlanDayDto;
import com.english.learning.dto.PlanItemDto;
import com.english.learning.dto.WeeklyPlanDto;
import com.english.learning.entity.LearningPlanItem;
import com.english.learning.entity.LearningRecord;
import com.english.learning.entity.User;
import com.english.learning.mapper.LearningPlanItemMapper;
import com.english.learning.service.LearningPlanService;
import com.english.learning.service.LearningRecordService;
import com.english.learning.service.UserService;
import com.english.learning.util.LearningRecordTypeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LearningPlanServiceImpl extends ServiceImpl<LearningPlanItemMapper, LearningPlanItem>
        implements LearningPlanService {

    private static final List<String> DAY_LABELS = List.of(
            "周一", "周二", "周三", "周四", "周五", "周六", "周日");

    private static final String SOURCE_TEMPLATE = "template";
    private static final String SOURCE_AI = "ai_recommend";

    @Autowired
    private LearningRecordService learningRecordService;

    @Autowired
    private UserService userService;

    @Override
    public WeeklyPlanDto getCurrentWeekPlan(Long userId) {
        LocalDate weekStart = currentWeekStart();
        List<LearningPlanItem> items = listByUserAndWeek(userId, weekStart);
        if (items.isEmpty()) {
            return null;
        }
        syncCompletionFromRecords(userId);
        items = listByUserAndWeek(userId, weekStart);
        User user = userService.getById(userId);
        return buildWeeklyPlanDto(items, weekStart, user);
    }

    @Override
    @Transactional
    public WeeklyPlanDto generateWeekPlan(User user, boolean forceRegenerate) {
        LocalDate weekStart = currentWeekStart();
        List<LearningPlanItem> existing = listByUserAndWeek(user.getId(), weekStart);
        if (!existing.isEmpty() && !forceRegenerate) {
            return buildWeeklyPlanDto(existing, weekStart, user);
        }

        if (!existing.isEmpty()) {
            remove(new LambdaQueryWrapper<LearningPlanItem>()
                    .eq(LearningPlanItem::getUserId, user.getId())
                    .eq(LearningPlanItem::getWeekStart, weekStart));
        }

        String level = normalizeLevel(user.getLevel());
        String targetExam = normalizeTargetExam(user.getTargetExam());
        int dailyGoal = normalizeDailyGoal(user.getDailyGoal());
        List<String> weakModules = detectWeakModules(user.getId());

        List<LearningPlanItem> generated = buildPlanItems(
                user.getId(), weekStart, level, targetExam, dailyGoal, weakModules);
        saveBatch(generated);

        syncCompletionFromRecords(user.getId());
        return buildWeeklyPlanDto(listByUserAndWeek(user.getId(), weekStart), weekStart, user);
    }

    @Override
    @Transactional
    public PlanItemDto completeItem(Long userId, Long itemId) {
        LearningPlanItem item = getById(itemId);
        if (item == null || !userId.equals(item.getUserId())) {
            throw new IllegalArgumentException("计划任务不存在");
        }
        item.setCompleted(1);
        item.setCompletedAt(LocalDateTime.now());
        updateById(item);
        return toPlanItemDto(item);
    }

    @Override
    public void syncCompletionFromRecords(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime dayEnd = today.plusDays(1).atStartOfDay();

        LambdaQueryWrapper<LearningRecord> qw = new LambdaQueryWrapper<>();
        qw.eq(LearningRecord::getUserId, userId)
                .ge(LearningRecord::getCreateTime, dayStart)
                .lt(LearningRecord::getCreateTime, dayEnd);
        List<LearningRecord> records = learningRecordService.list(qw);
        if (records.isEmpty()) {
            return;
        }

        Set<String> practicedModules = records.stream()
                .map(r -> LearningRecordTypeUtil.toKtModule(r.getType()))
                .filter(m -> m != null)
                .collect(Collectors.toSet());

        List<LearningPlanItem> todayItems = list(new LambdaQueryWrapper<LearningPlanItem>()
                .eq(LearningPlanItem::getUserId, userId)
                .eq(LearningPlanItem::getPlanDate, today)
                .eq(LearningPlanItem::getCompleted, 0));

        for (LearningPlanItem item : todayItems) {
            if (practicedModules.contains(item.getModule())) {
                item.setCompleted(1);
                item.setCompletedAt(LocalDateTime.now());
                updateById(item);
            }
        }
    }

    @Override
    @Transactional
    public void syncAiRecommendToPlan(Long userId, KtRecommendDto recommend) {
        if (recommend == null || recommend.getTodayTasks() == null || recommend.getTodayTasks().isEmpty()) {
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate weekStart = currentWeekStart();

        if (listByUserAndWeek(userId, weekStart).isEmpty()) {
            User user = userService.getById(userId);
            if (user != null) {
                generateWeekPlan(user, false);
            }
        }

        remove(new LambdaQueryWrapper<LearningPlanItem>()
                .eq(LearningPlanItem::getUserId, userId)
                .eq(LearningPlanItem::getPlanDate, today)
                .eq(LearningPlanItem::getSource, SOURCE_AI)
                .eq(LearningPlanItem::getCompleted, 0));

        User user = userService.getById(userId);
        int targetMinutes = user != null ? Math.max(10, normalizeDailyGoal(user.getDailyGoal()) / 2) : 15;
        List<String> weakModules = recommend.getWeakModules() != null ? recommend.getWeakModules() : List.of();
        List<String> tasks = recommend.getTodayTasks();

        for (int i = 0; i < tasks.size(); i++) {
            String rawTask = tasks.get(i).trim();
            if (rawTask.isEmpty()) {
                continue;
            }
            String taskText = rawTask.startsWith("【AI推荐】") ? rawTask : "【AI推荐】" + rawTask;
            if (existsTodayTaskText(userId, today, taskText)) {
                continue;
            }

            String module = inferModuleFromTask(rawTask, i, weakModules);
            LearningPlanItem item = new LearningPlanItem();
            item.setUserId(userId);
            item.setWeekStart(weekStart);
            item.setPlanDate(today);
            item.setDayOfWeek(today.getDayOfWeek().getValue());
            item.setModule(module);
            item.setSource(SOURCE_AI);
            item.setTaskText(taskText);
            item.setTargetMinutes(targetMinutes);
            item.setCompleted(0);
            save(item);
        }

        syncCompletionFromRecords(userId);
    }

    private boolean existsTodayTaskText(Long userId, LocalDate today, String taskText) {
        return count(new LambdaQueryWrapper<LearningPlanItem>()
                .eq(LearningPlanItem::getUserId, userId)
                .eq(LearningPlanItem::getPlanDate, today)
                .eq(LearningPlanItem::getTaskText, taskText)) > 0;
    }

    private String inferModuleFromTask(String taskText, int index, List<String> weakModules) {
        String lower = taskText.toLowerCase();
        if (lower.contains("词汇") || lower.contains("单词")) {
            return "VOCAB";
        }
        if (lower.contains("语法")) {
            return "GRAMMAR";
        }
        if (lower.contains("听力")) {
            return "LISTENING";
        }
        if (lower.contains("口语") || lower.contains("录音")) {
            return "ORAL";
        }
        if (lower.contains("阅读") || lower.contains("理解") || lower.contains("填空")) {
            return "READING";
        }
        if (index < weakModules.size()) {
            String mod = LearningRecordTypeUtil.toKtModule(weakModules.get(index));
            if (mod != null) {
                return mod;
            }
        }
        return "READING";
    }

    private List<LearningPlanItem> listByUserAndWeek(Long userId, LocalDate weekStart) {
        return list(new LambdaQueryWrapper<LearningPlanItem>()
                .eq(LearningPlanItem::getUserId, userId)
                .eq(LearningPlanItem::getWeekStart, weekStart)
                .orderByAsc(LearningPlanItem::getDayOfWeek)
                .orderByAsc(LearningPlanItem::getId));
    }

    private WeeklyPlanDto buildWeeklyPlanDto(List<LearningPlanItem> items, LocalDate weekStart, User user) {
        WeeklyPlanDto dto = new WeeklyPlanDto();
        dto.setWeekStart(weekStart.toString());
        dto.setWeekEnd(weekStart.plusDays(6).toString());
        if (user != null) {
            dto.setLevel(normalizeLevel(user.getLevel()));
            dto.setTargetExam(normalizeTargetExam(user.getTargetExam()));
            dto.setDailyGoal(normalizeDailyGoal(user.getDailyGoal()));
        }
        LocalDate today = LocalDate.now();

        Map<LocalDate, List<LearningPlanItem>> byDate = items.stream()
                .collect(Collectors.groupingBy(LearningPlanItem::getPlanDate, LinkedHashMap::new,
                        Collectors.toList()));

        List<PlanDayDto> days = new ArrayList<>();
        List<PlanItemDto> todayItems = new ArrayList<>();
        int completed = 0;
        int total = items.size();

        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);
            List<LearningPlanItem> dayItems = byDate.getOrDefault(date, List.of());
            PlanDayDto dayDto = new PlanDayDto();
            dayDto.setDate(date.toString());
            dayDto.setDayOfWeek(i + 1);
            dayDto.setDayLabel(DAY_LABELS.get(i));
            dayDto.setToday(date.equals(today));

            List<PlanItemDto> itemDtos = dayItems.stream().map(this::toPlanItemDto).toList();
            dayDto.setItems(itemDtos);
            days.add(dayDto);

            if (date.equals(today)) {
                todayItems.addAll(itemDtos);
            }
            completed += (int) dayItems.stream().filter(it -> it.getCompleted() != null && it.getCompleted() == 1).count();
        }

        dto.setDays(days);
        dto.setTodayItems(todayItems);
        dto.setCompletedCount(completed);
        dto.setTotalCount(total);
        return dto;
    }

    private PlanItemDto toPlanItemDto(LearningPlanItem item) {
        PlanItemDto dto = new PlanItemDto();
        dto.setId(item.getId());
        dto.setPlanDate(item.getPlanDate().toString());
        dto.setDayOfWeek(item.getDayOfWeek());
        dto.setDayLabel(DAY_LABELS.get(Math.max(0, Math.min(6, item.getDayOfWeek() - 1))));
        dto.setModule(item.getModule());
        dto.setModuleLabel(LearningRecordTypeUtil.toDisplayName(item.getModule()));
        dto.setSource(item.getSource() != null ? item.getSource() : SOURCE_TEMPLATE);
        dto.setTaskText(item.getTaskText());
        dto.setTargetMinutes(item.getTargetMinutes());
        dto.setCompleted(item.getCompleted() != null && item.getCompleted() == 1);
        return dto;
    }

    private List<LearningPlanItem> buildPlanItems(Long userId, LocalDate weekStart,
                                                   String level, String targetExam,
                                                   int dailyGoal, List<String> weakModules) {
        double scale = dailyGoal / 30.0;
        int vocabCount = clamp((int) Math.round(15 * scale), 10, 30);
        int readingCount = scale >= 1.2 ? 2 : 1;
        int listeningCount = scale >= 1.0 ? 1 : 1;
        int grammarSets = scale >= 0.8 ? 1 : 1;
        int oralCount = scale >= 1.0 ? 1 : 1;

        List<List<String>> weeklyModules = weeklyTemplate(targetExam);
        if (!weakModules.isEmpty()) {
            weeklyModules = overlayWeakModules(weeklyModules, weakModules);
        }

        List<LearningPlanItem> items = new ArrayList<>();
        for (int day = 0; day < 7; day++) {
            LocalDate planDate = weekStart.plusDays(day);
            List<String> modules = weeklyModules.get(day);
            int dayMinutesBudget = dailyGoal;
            int perTaskMinutes = Math.max(10, dayMinutesBudget / Math.max(1, modules.size()));

            for (String module : modules) {
                LearningPlanItem item = new LearningPlanItem();
                item.setUserId(userId);
                item.setWeekStart(weekStart);
                item.setPlanDate(planDate);
                item.setDayOfWeek(day + 1);
                item.setModule(module);
                item.setSource(SOURCE_TEMPLATE);
                item.setTaskText(buildTaskText(module, level, targetExam, vocabCount,
                        grammarSets, readingCount, listeningCount, oralCount));
                item.setTargetMinutes(perTaskMinutes);
                item.setCompleted(0);
                items.add(item);
            }
        }
        return items;
    }

    private List<List<String>> weeklyTemplate(String targetExam) {
        return switch (targetExam) {
            case "CET-4" -> List.of(
                    List.of("VOCAB"),
                    List.of("GRAMMAR"),
                    List.of("READING"),
                    List.of("VOCAB", "LISTENING"),
                    List.of("READING"),
                    List.of("GRAMMAR", "VOCAB"),
                    List.of("READING", "VOCAB"));
            case "CET-6" -> List.of(
                    List.of("VOCAB"),
                    List.of("GRAMMAR", "READING"),
                    List.of("READING"),
                    List.of("LISTENING", "VOCAB"),
                    List.of("GRAMMAR"),
                    List.of("READING", "VOCAB"),
                    List.of("GRAMMAR", "LISTENING"));
            case "IELTS" -> List.of(
                    List.of("LISTENING"),
                    List.of("READING"),
                    List.of("VOCAB", "ORAL"),
                    List.of("LISTENING", "GRAMMAR"),
                    List.of("READING", "ORAL"),
                    List.of("VOCAB", "LISTENING"),
                    List.of("ORAL", "READING"));
            case "TOEFL" -> List.of(
                    List.of("READING"),
                    List.of("LISTENING"),
                    List.of("VOCAB", "READING"),
                    List.of("LISTENING", "GRAMMAR"),
                    List.of("READING", "ORAL"),
                    List.of("LISTENING", "VOCAB"),
                    List.of("READING", "GRAMMAR"));
            default -> List.of(
                    List.of("VOCAB"),
                    List.of("GRAMMAR"),
                    List.of("READING"),
                    List.of("LISTENING"),
                    List.of("VOCAB", "READING"),
                    List.of("ORAL"),
                    List.of("GRAMMAR", "VOCAB"));
        };
    }

    /** 将薄弱模块优先插入周三、周五 */
    private List<List<String>> overlayWeakModules(List<List<String>> template, List<String> weakModules) {
        List<List<String>> result = new ArrayList<>();
        for (List<String> day : template) {
            result.add(new ArrayList<>(day));
        }
        int[] injectDays = {2, 4};
        for (int i = 0; i < weakModules.size() && i < injectDays.length; i++) {
            int dayIdx = injectDays[i];
            List<String> dayModules = result.get(dayIdx);
            String weak = weakModules.get(i);
            if (!dayModules.contains(weak)) {
                if (dayModules.size() >= 2) {
                    dayModules.set(1, weak);
                } else {
                    dayModules.add(weak);
                }
            }
        }
        return result;
    }

    private String buildTaskText(String module, String level, String targetExam,
                                 int vocabCount, int grammarSets, int readingCount,
                                 int listeningCount, int oralCount) {
        String examTag = "GENERAL".equals(targetExam) ? "" : "（" + targetExam + "）";
        return switch (module) {
            case "VOCAB" -> String.format("词汇学习 %d 个单词%s · %s", vocabCount, examTag, level);
            case "GRAMMAR" -> String.format("语法测试 %d 套%s", grammarSets, examTag);
            case "READING" -> String.format("阅读理解 %d 篇%s", readingCount, examTag);
            case "LISTENING" -> String.format("听力训练 %d 套%s", listeningCount, examTag);
            case "ORAL" -> String.format("口语录音练习 %d 次%s", oralCount, examTag);
            default -> "综合练习";
        };
    }

    private List<String> detectWeakModules(Long userId) {
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        LambdaQueryWrapper<LearningRecord> qw = new LambdaQueryWrapper<>();
        qw.eq(LearningRecord::getUserId, userId).ge(LearningRecord::getCreateTime, since);
        List<LearningRecord> records = learningRecordService.list(qw);

        Map<String, List<LearningRecord>> byModule = new LinkedHashMap<>();
        for (LearningRecord r : records) {
            String mod = LearningRecordTypeUtil.toKtModule(r.getType());
            if (mod != null) {
                byModule.computeIfAbsent(mod, k -> new ArrayList<>()).add(r);
            }
        }

        List<ModuleAcc> stats = new ArrayList<>();
        for (String mod : List.of("VOCAB", "GRAMMAR", "READING", "LISTENING", "ORAL")) {
            List<LearningRecord> list = byModule.getOrDefault(mod, List.of());
            double acc = 0.5;
            if (!list.isEmpty()) {
                double sum = 0;
                int count = 0;
                for (LearningRecord r : list) {
                    if (r.getScore() != null) {
                        sum += Math.min(100, Math.max(0, r.getScore())) / 100.0;
                        count++;
                    }
                }
                acc = count > 0 ? sum / count : 0.5;
            }
            stats.add(new ModuleAcc(mod, acc, list.size()));
        }

        return stats.stream()
                .sorted(Comparator.comparingDouble(ModuleAcc::accuracy).thenComparingInt(ModuleAcc::attempts))
                .limit(2)
                .map(m -> m.module)
                .toList();
    }

    private LocalDate currentWeekStart() {
        return LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private String normalizeLevel(String level) {
        if (level == null || level.isBlank()) {
            return "B1";
        }
        return level.trim().toUpperCase();
    }

    private String normalizeTargetExam(String targetExam) {
        if (targetExam == null || targetExam.isBlank()) {
            return "GENERAL";
        }
        return targetExam.trim().toUpperCase();
    }

    private int normalizeDailyGoal(Integer dailyGoal) {
        if (dailyGoal == null || dailyGoal < 10) {
            return 30;
        }
        return Math.min(dailyGoal, 180);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private record ModuleAcc(String module, double accuracy, int attempts) {}
}
