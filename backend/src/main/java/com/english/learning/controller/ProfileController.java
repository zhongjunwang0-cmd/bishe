package com.english.learning.controller;

import com.english.learning.common.Result;
import com.english.learning.dto.UpdateProfileRequest;
import com.english.learning.dto.UserProfileDto;
import com.english.learning.entity.User;
import com.english.learning.service.AiService;
import com.english.learning.service.LearningPlanService;
import com.english.learning.service.UserService;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private static final List<String> VALID_LEVELS = List.of(
            "A1", "A2", "B1", "B2", "CET-4", "CET-6", "IELTS", "TOEFL");
    private static final List<String> VALID_EXAMS = List.of(
            "CET-4", "CET-6", "IELTS", "TOEFL", "GENERAL");

    @Autowired
    private UserService userService;

    @Autowired
    private LearningPlanService learningPlanService;

    @Autowired
    private AiService aiService;

    @GetMapping("/info")
    public Result<UserProfileDto> info() {
        User user = currentUser();
        if (user == null) {
            return Result.error("未登录");
        }
        User fresh = userService.getById(user.getId());
        return Result.success(toProfileDto(fresh));
    }

    @PutMapping
    public Result<UserProfileDto> update(@RequestBody UpdateProfileRequest request) {
        User user = currentUser();
        if (user == null) {
            return Result.error("未登录");
        }
        User entity = userService.getById(user.getId());
        if (entity == null) {
            return Result.error("用户不存在");
        }

        boolean planPrefsChanged = false;
        if (request.getNickname() != null) {
            entity.setNickname(request.getNickname().trim());
        }
        if (request.getEmail() != null) {
            entity.setEmail(request.getEmail().trim());
        }
        if (request.getLevel() != null) {
            String level = request.getLevel().trim().toUpperCase();
            if (!VALID_LEVELS.contains(level)) {
                return Result.error("无效的水平等级");
            }
            planPrefsChanged |= !Objects.equals(entity.getLevel(), level);
            entity.setLevel(level);
        }
        if (request.getTargetExam() != null) {
            String exam = request.getTargetExam().trim().toUpperCase();
            if (!VALID_EXAMS.contains(exam)) {
                return Result.error("无效的目标考试");
            }
            planPrefsChanged |= !Objects.equals(entity.getTargetExam(), exam);
            entity.setTargetExam(exam);
        }
        if (request.getDailyGoal() != null) {
            int goal = Math.max(10, Math.min(180, request.getDailyGoal()));
            planPrefsChanged |= !Objects.equals(entity.getDailyGoal(), goal);
            entity.setDailyGoal(goal);
        }

        userService.updateById(entity);

        if (planPrefsChanged) {
            User updated = userService.getById(entity.getId());
            learningPlanService.generateWeekPlan(updated, true);
            aiService.getLearningRecommend(updated.getId());
        }

        return Result.success(toProfileDto(userService.getById(entity.getId())));
    }

    private UserProfileDto toProfileDto(User user) {
        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setLevel(user.getLevel() != null ? user.getLevel() : "B1");
        dto.setTargetExam(user.getTargetExam() != null ? user.getTargetExam() : "GENERAL");
        dto.setDailyGoal(user.getDailyGoal() != null ? user.getDailyGoal() : 30);
        return dto;
    }

    private User currentUser() {
        return (User) SecurityUtils.getSubject().getPrincipal();
    }
}
