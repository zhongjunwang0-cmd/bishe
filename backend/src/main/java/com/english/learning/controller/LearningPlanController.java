package com.english.learning.controller;

import com.english.learning.common.Result;
import com.english.learning.dto.PlanItemDto;
import com.english.learning.dto.WeeklyPlanDto;
import com.english.learning.entity.User;
import com.english.learning.service.AiService;
import com.english.learning.service.LearningPlanService;
import com.english.learning.service.UserService;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/plan")
public class LearningPlanController {

    @Autowired
    private LearningPlanService learningPlanService;

    @Autowired
    private UserService userService;

    @Autowired
    private AiService aiService;

    @GetMapping("/current")
    public Result<WeeklyPlanDto> current() {
        User user = currentUser();
        if (user == null) {
            return Result.error("未登录");
        }
        Long userId = user.getId();
        WeeklyPlanDto plan = learningPlanService.getCurrentWeekPlan(userId);
        if (plan == null) {
            User fresh = userService.getById(userId);
            plan = learningPlanService.generateWeekPlan(fresh, false);
        }
        aiService.getLearningRecommend(userId);
        plan = learningPlanService.getCurrentWeekPlan(userId);
        return Result.success(plan);
    }

    @PostMapping("/generate")
    public Result<WeeklyPlanDto> generate() {
        User user = currentUser();
        if (user == null) {
            return Result.error("未登录");
        }
        User fresh = userService.getById(user.getId());
        learningPlanService.generateWeekPlan(fresh, true);
        aiService.getLearningRecommend(fresh.getId());
        return Result.success(learningPlanService.getCurrentWeekPlan(fresh.getId()));
    }

    @PatchMapping("/item/{id}/complete")
    public Result<PlanItemDto> complete(@PathVariable Long id) {
        User user = currentUser();
        if (user == null) {
            return Result.error("未登录");
        }
        try {
            return Result.success(learningPlanService.completeItem(user.getId(), id));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    private User currentUser() {
        return (User) SecurityUtils.getSubject().getPrincipal();
    }
}
