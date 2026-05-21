package com.english.learning.util;

import java.time.LocalDateTime;

/**
 * 艾宾浩斯遗忘曲线间隔：1天 → 2天 → 4天 → 7天 → 15天 → 30天
 */
public final class EbbinghausUtil {

    private static final int[] INTERVAL_DAYS = {1, 2, 4, 7, 15, 30};
    private static final int MASTERY_INCREMENT = 15;
    private static final int MASTERY_DECREMENT = 20;
    private static final int MASTERED_THRESHOLD = 85;
    private static final int MAX_STAGE = INTERVAL_DAYS.length - 1;

    private EbbinghausUtil() {
    }

    public static int intervalDaysForStage(int stage) {
        int idx = Math.max(0, Math.min(stage, MAX_STAGE));
        return INTERVAL_DAYS[idx];
    }

    public static LocalDateTime nextReviewTime(int stage) {
        return LocalDateTime.now().plusDays(intervalDaysForStage(stage));
    }

    public static int applyRemembered(int currentStage, int currentMastery) {
        int newStage = Math.min(currentStage + 1, MAX_STAGE);
        int newMastery = Math.min(100, currentMastery + MASTERY_INCREMENT);
        return newStage;
    }

    public static int newMasteryOnRemembered(int currentMastery) {
        return Math.min(100, currentMastery + MASTERY_INCREMENT);
    }

    public static int newMasteryOnForgot(int currentMastery) {
        return Math.max(0, currentMastery - MASTERY_DECREMENT);
    }

    public static int resetStageOnForgot(int currentStage) {
        return Math.max(0, currentStage - 1);
    }

    public static String resolveStatus(int reviewStage, int masteryLevel) {
        if (masteryLevel >= MASTERED_THRESHOLD && reviewStage >= 4) {
            return "MASTERED";
        }
        if (reviewStage >= 3) {
            return "REVIEWING";
        }
        if (reviewStage > 0 || masteryLevel > 0) {
            return "LEARNING";
        }
        return "NEW";
    }

    public static int[] getIntervalDays() {
        return INTERVAL_DAYS.clone();
    }
}
