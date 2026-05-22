package com.english.learning.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.english.learning.common.Result;
import com.english.learning.entity.LearningRecord;
import com.english.learning.entity.User;
import com.english.learning.service.LearningRecordService;
import com.english.learning.service.UserService;
import com.english.learning.util.LearningRecordTypeUtil;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/record")
public class RecordController {

    @Autowired
    private LearningRecordService learningRecordService;

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public Result<?> list(
            @RequestParam(defaultValue = "personal") String scope,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        User currentUser = (User) SecurityUtils.getSubject().getPrincipal();
        if (currentUser == null) {
            return Result.error("未登录");
        }

        boolean global = "global".equalsIgnoreCase(scope)
                && (currentUser.getRoleId() == 1L || currentUser.getRoleId() == 2L);

        QueryWrapper<LearningRecord> queryWrapper = new QueryWrapper<>();
        if (!global) {
            queryWrapper.eq("user_id", currentUser.getId());
        }
        if (startDate != null) {
            queryWrapper.ge("create_time", startDate.atStartOfDay());
        }
        if (endDate != null) {
            queryWrapper.le("create_time", endDate.atTime(LocalTime.MAX));
        }
        queryWrapper.orderByDesc("create_time");
        List<LearningRecord> records = learningRecordService.list(queryWrapper);

        List<Map<String, Object>> activities = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (LearningRecord record : records) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("timestamp", record.getCreateTime() != null ? record.getCreateTime().format(formatter) : "");
            activity.put("type", "primary");

            String prefix = "";
            if (global) {
                User user = userService.getById(record.getUserId());
                String displayName = user != null
                        ? (user.getNickname() != null ? user.getNickname() : user.getUsername())
                        : "未知用户";
                prefix = displayName + " ";
            }

            activity.put("content", prefix + "完成了 " + LearningRecordTypeUtil.toDisplayName(record.getType())
                    + " 学习，时长: " + (record.getDuration() == null ? 0 : record.getDuration()) + " 秒"
                    + (record.getScore() != null ? "，得分: " + record.getScore() : ""));
            activities.add(activity);
        }

        return Result.success(activities);
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody LearningRecord record) {
        User currentUser = (User) SecurityUtils.getSubject().getPrincipal();
        if (currentUser == null) {
            return Result.error("未登录");
        }
        record.setUserId(currentUser.getId());
        record.setType(LearningRecordTypeUtil.normalize(record.getType()));
        learningRecordService.save(record);
        return Result.success("记录保存成功");
    }
}
