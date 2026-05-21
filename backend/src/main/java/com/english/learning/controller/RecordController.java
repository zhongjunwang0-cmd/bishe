package com.english.learning.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.english.learning.common.Result;
import com.english.learning.entity.LearningRecord;
import com.english.learning.entity.User;
import com.english.learning.service.LearningRecordService;
import com.english.learning.util.LearningRecordTypeUtil;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/list")
    public Result<?> list() {
        User currentUser = (User) SecurityUtils.getSubject().getPrincipal();
        if (currentUser == null) {
            return Result.error("未登录");
        }

        QueryWrapper<LearningRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", currentUser.getId()).orderByDesc("create_time");
        List<LearningRecord> records = learningRecordService.list(queryWrapper);

        List<Map<String, Object>> activities = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (LearningRecord record : records) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("timestamp", record.getCreateTime() != null ? record.getCreateTime().format(formatter) : "");
            activity.put("type", "primary");
            activity.put("content", "完成了 " + LearningRecordTypeUtil.toDisplayName(record.getType())
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
