package com.english.learning.controller;

import com.english.learning.common.Result;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/system")
public class AdminSystemController {

    // Mocking an in-memory configuration storage
    private final Map<String, Object> systemConfig = new HashMap<>();

    @GetMapping("/dashboard")
    public Result<?> dashboard() {
        return Result.success(new HashMap<>());
    }

    @GetMapping("/config")
    public Result<Map<String, Object>> getConfig() {
        return Result.success(systemConfig);
    }

    @PutMapping("/config")
    public Result<String> saveConfig(@RequestBody Map<String, Object> config) {
        systemConfig.putAll(config);
        return Result.success("系统配置保存成功，服务已重新加载！");
    }
}
