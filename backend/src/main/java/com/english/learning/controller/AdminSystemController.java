package com.english.learning.controller;

import com.english.learning.common.Result;
import com.english.learning.service.SystemConfigService;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/system")
@RequiresRoles("ADMIN")
public class AdminSystemController {

    private final SystemConfigService systemConfigService;

    public AdminSystemController(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    @GetMapping("/dashboard")
    public Result<?> dashboard() {
        return Result.success(new HashMap<>());
    }

    @GetMapping("/config")
    public Result<Map<String, Object>> getConfig() {
        return Result.success(systemConfigService.getConfigForAdmin());
    }

    @PutMapping("/config")
    public Result<Map<String, Object>> saveConfig(@RequestBody Map<String, Object> config) {
        systemConfigService.saveConfig(config);

        Map<String, Object> result = new HashMap<>();
        boolean aiEnabled = Boolean.TRUE.equals(config.get("aiEnabled"))
                || "true".equalsIgnoreCase(String.valueOf(config.get("aiEnabled")));
        if (aiEnabled) {
            String aiTest = systemConfigService.testAiConnection();
            result.put("aiTest", aiTest);
            if (!"成功".equals(aiTest)) {
                result.put("message", "配置已保存，但 AI 连接测试失败：" + aiTest);
                return Result.error(400, "配置已保存，但 AI 连接测试失败：" + aiTest);
            }
            result.put("message", "系统配置保存成功，AI 连接测试通过！");
        } else {
            result.put("message", "系统配置保存成功！");
        }
        return Result.success(result);
    }
}
