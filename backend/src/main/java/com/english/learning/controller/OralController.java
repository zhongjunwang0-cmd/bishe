package com.english.learning.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.english.learning.common.Result;
import com.english.learning.entity.Oral;
import com.english.learning.service.OralService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/oral")
public class OralController {
    
    @Autowired
    private OralService oralService;

    @GetMapping("/list")
    public Result<List<Oral>> list() {
        QueryWrapper<Oral> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        return Result.success(oralService.list(queryWrapper));
    }

    @PostMapping("/generate")
    public Result<String> generate() {
        Oral newTopic = new Oral();
        newTopic.setTopic("AI Daily Conversation topic " + UUID.randomUUID().toString().substring(0, 5));
        newTopic.setAttempts(0);
        newTopic.setCreateTime(LocalDateTime.now());
        oralService.save(newTopic);
        return Result.success("生成口语练习主题成功");
    }

    @PutMapping("/{id}/evaluate")
    public Result<String> evaluate(@PathVariable Long id, @RequestBody Oral oral) {
        Oral existing = oralService.getById(id);
        if (existing == null) {
            return Result.error("Topic not found");
        }
        Integer currentHighest = existing.getScore();
        if (currentHighest == null || oral.getScore() > currentHighest) {
            existing.setScore(oral.getScore());
        }
        existing.setAttempts(existing.getAttempts() + 1);
        oralService.updateById(existing);
        return Result.success("评测得分保存成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteOral(@PathVariable Long id) {
        boolean removed = oralService.removeById(id);
        if(removed) {
            return Result.success("删除口语练习主题成功");
        } else {
            return Result.error("删除口语练习主题失败");
        }
    }
}
