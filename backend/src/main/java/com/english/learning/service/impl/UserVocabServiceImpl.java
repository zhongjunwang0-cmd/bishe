package com.english.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.english.learning.dto.UserVocabVO;
import com.english.learning.entity.LearningRecord;
import com.english.learning.entity.UserVocab;
import com.english.learning.entity.Vocab;
import com.english.learning.mapper.UserVocabMapper;
import com.english.learning.service.LearningRecordService;
import com.english.learning.service.UserVocabService;
import com.english.learning.service.VocabService;
import com.english.learning.util.EbbinghausUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UserVocabServiceImpl extends ServiceImpl<UserVocabMapper, UserVocab> implements UserVocabService {

    @Autowired
    private VocabService vocabService;

    @Autowired
    private LearningRecordService learningRecordService;

    @Override
    public List<UserVocabVO> listUserBook(Long userId) {
        List<UserVocab> records = list(new QueryWrapper<UserVocab>()
                .eq("user_id", userId)
                .orderByDesc("update_time"));
        return records.stream()
                .map(this::toVO)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserVocabVO> listDueReview(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        List<UserVocab> records = list(new QueryWrapper<UserVocab>()
                .eq("user_id", userId)
                .and(w -> w.isNull("next_review_time")
                        .or()
                        .le("next_review_time", now))
                .ne("status", "MASTERED")
                .orderByAsc("next_review_time"));
        return records.stream()
                .map(this::toVO)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getStats(Long userId) {
        List<UserVocab> all = list(new QueryWrapper<UserVocab>().eq("user_id", userId));
        LocalDateTime now = LocalDateTime.now();
        long dueToday = all.stream()
                .filter(uv -> !"MASTERED".equals(uv.getStatus()))
                .filter(uv -> uv.getNextReviewTime() == null || !uv.getNextReviewTime().isAfter(now))
                .count();
        long mastered = all.stream().filter(uv -> "MASTERED".equals(uv.getStatus())).count();
        long learning = all.stream()
                .filter(uv -> "LEARNING".equals(uv.getStatus()) || "NEW".equals(uv.getStatus()))
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", all.size());
        stats.put("dueToday", dueToday);
        stats.put("mastered", mastered);
        stats.put("learning", learning);
        stats.put("intervals", EbbinghausUtil.getIntervalDays());
        return stats;
    }

    @Override
    public UserVocabVO addToBook(Long userId, Long vocabId) {
        Vocab vocab = vocabService.getById(vocabId);
        if (vocab == null) {
            throw new IllegalArgumentException("词汇不存在");
        }

        UserVocab existing = getOne(new QueryWrapper<UserVocab>()
                .eq("user_id", userId)
                .eq("vocab_id", vocabId));
        if (existing != null) {
            return toVO(existing);
        }

        UserVocab userVocab = new UserVocab();
        userVocab.setUserId(userId);
        userVocab.setVocabId(vocabId);
        userVocab.setMasteryLevel(0);
        userVocab.setReviewStage(0);
        userVocab.setStatus("NEW");
        userVocab.setNextReviewTime(LocalDateTime.now());
        userVocab.setReviewCount(0);
        save(userVocab);
        return toVO(userVocab);
    }

    @Override
    public void removeFromBook(Long userId, Long vocabId) {
        remove(new QueryWrapper<UserVocab>()
                .eq("user_id", userId)
                .eq("vocab_id", vocabId));
    }

    @Override
    public UserVocabVO submitReview(Long userId, Long vocabId, boolean remembered) {
        UserVocab userVocab = getOne(new QueryWrapper<UserVocab>()
                .eq("user_id", userId)
                .eq("vocab_id", vocabId));
        if (userVocab == null) {
            throw new IllegalArgumentException("该词不在生词本中");
        }

        int stage = userVocab.getReviewStage() == null ? 0 : userVocab.getReviewStage();
        int mastery = userVocab.getMasteryLevel() == null ? 0 : userVocab.getMasteryLevel();

        if (remembered) {
            stage = EbbinghausUtil.applyRemembered(stage, mastery);
            mastery = EbbinghausUtil.newMasteryOnRemembered(mastery);
        } else {
            stage = EbbinghausUtil.resetStageOnForgot(stage);
            mastery = EbbinghausUtil.newMasteryOnForgot(mastery);
        }

        userVocab.setReviewStage(stage);
        userVocab.setMasteryLevel(mastery);
        userVocab.setStatus(EbbinghausUtil.resolveStatus(stage, mastery));
        userVocab.setLastReviewTime(LocalDateTime.now());
        userVocab.setNextReviewTime(EbbinghausUtil.nextReviewTime(stage));
        userVocab.setReviewCount((userVocab.getReviewCount() == null ? 0 : userVocab.getReviewCount()) + 1);
        updateById(userVocab);

        LearningRecord record = new LearningRecord();
        record.setUserId(userId);
        record.setType("VOCAB");
        record.setTargetId(vocabId);
        record.setDuration(remembered ? 30 : 15);
        learningRecordService.save(record);

        return toVO(userVocab);
    }

    @Override
    public boolean isInBook(Long userId, Long vocabId) {
        return count(new QueryWrapper<UserVocab>()
                .eq("user_id", userId)
                .eq("vocab_id", vocabId)) > 0;
    }

    private UserVocabVO toVO(UserVocab userVocab) {
        Vocab vocab = vocabService.getById(userVocab.getVocabId());
        if (vocab == null) {
            return null;
        }
        UserVocabVO vo = new UserVocabVO();
        vo.setId(userVocab.getId());
        vo.setVocabId(vocab.getId());
        vo.setWord(vocab.getWord());
        vo.setPhonetic(vocab.getPhonetic());
        vo.setTranslation(vocab.getTranslation());
        vo.setExample(vocab.getExample());
        vo.setLevel(vocab.getLevel());
        vo.setMasteryLevel(userVocab.getMasteryLevel());
        vo.setReviewStage(userVocab.getReviewStage());
        vo.setStatus(userVocab.getStatus());
        vo.setNextReviewTime(userVocab.getNextReviewTime());
        vo.setLastReviewTime(userVocab.getLastReviewTime());
        vo.setReviewCount(userVocab.getReviewCount());
        return vo;
    }
}
