package com.english.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.english.learning.dto.KtRecommendDto;
import com.english.learning.dto.PlanItemDto;
import com.english.learning.dto.WeeklyPlanDto;
import com.english.learning.entity.LearningPlanItem;
import com.english.learning.entity.User;

public interface LearningPlanService extends IService<LearningPlanItem> {

    WeeklyPlanDto getCurrentWeekPlan(Long userId);

    WeeklyPlanDto generateWeekPlan(User user, boolean forceRegenerate);

    PlanItemDto completeItem(Long userId, Long itemId);

    void syncCompletionFromRecords(Long userId);

    void syncAiRecommendToPlan(Long userId, KtRecommendDto recommend);
}
