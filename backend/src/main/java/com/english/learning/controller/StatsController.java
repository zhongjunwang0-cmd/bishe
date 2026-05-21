package com.english.learning.controller;

import com.english.learning.common.Result;
import com.english.learning.entity.User;
import com.english.learning.service.StatsService;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private StatsService statsService;

    @GetMapping
    public Result<Map<String, Object>> getStats(
            @RequestParam(defaultValue = "personal") String scope) {
        User currentUser = (User) SecurityUtils.getSubject().getPrincipal();
        if (currentUser == null) {
            return Result.error("未登录");
        }

        boolean global = "global".equalsIgnoreCase(scope)
                && (currentUser.getRoleId() == 1L || currentUser.getRoleId() == 2L);
        Map<String, Object> data = statsService.getStats(currentUser.getId(), global);
        return Result.success(data);
    }

    @GetMapping("/learning-curve")
    public Result<Map<String, Object>> getLearningCurve(
            @RequestParam(defaultValue = "personal") String scope) {
        User currentUser = (User) SecurityUtils.getSubject().getPrincipal();
        if (currentUser == null) {
            return Result.error("未登录");
        }

        boolean global = "global".equalsIgnoreCase(scope)
                && (currentUser.getRoleId() == 1L || currentUser.getRoleId() == 2L);
        Map<String, Object> stats = statsService.getStats(currentUser.getId(), global);
        @SuppressWarnings("unchecked")
        Map<String, Object> durationTrend = (Map<String, Object>) stats.get("durationTrend");
        return Result.success(durationTrend);
    }
}
