package com.english.learning.controller;

import com.english.learning.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @GetMapping("/learning-curve")
    public Result<Map<String, Object>> getLearningCurve() {
        Map<String, Object> data = new HashMap<>();
        List<String> categories = new ArrayList<>();
        categories.add("周一"); categories.add("周二"); categories.add("周三");
        categories.add("周四"); categories.add("周五"); categories.add("周六"); categories.add("周日");
        
        List<Integer> values = new ArrayList<>();
        values.add(30); values.add(45); values.add(60);
        values.add(20); values.add(80); values.add(100); values.add(50);
        
        data.put("categories", categories);
        data.put("values", values);
        return Result.success(data);
    }
}
