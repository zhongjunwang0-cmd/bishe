package com.english.learning.controller;

import com.english.learning.common.Result;
import com.english.learning.entity.User;
import com.english.learning.service.AiService;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    @PostMapping("/advice")
    public Result<Map<String, String>> getAdvice(@RequestBody Map<String, String> request) {
        User currentUser = (User) SecurityUtils.getSubject().getPrincipal();
        Long userId = currentUser != null ? currentUser.getId() : -1L;
        
        String input = request.get("content");
        String advice;
        
        if (input != null && !input.isEmpty() && !input.contains("建议")) {
            advice = aiService.getChatResponse(userId, input);
        } else {
            advice = aiService.getPersonalizedAdvice(userId);
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("advice", advice);
        return Result.success(response);
    }
}
