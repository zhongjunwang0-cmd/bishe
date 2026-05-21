package com.english.learning.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.english.learning.common.Result;
import com.english.learning.entity.Cloze;
import com.english.learning.service.ClozeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cloze")
public class ClozeController {

    @Autowired
    private ClozeService clozeService;

    @GetMapping("/list")
    public Result<List<Cloze>> list() {
        QueryWrapper<Cloze> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        return Result.success(clozeService.list(queryWrapper));
    }

    @PostMapping("/generate")
    public Result<String> generate() {
        Cloze newTest = new Cloze();
        newTest.setTitle("AI Cloze Task " + UUID.randomUUID().toString().substring(0, 5));
        newTest.setContent("This is an automatically generated Cloze test. Read the passage and choose the correct word for each blank. \n\nLearning a foreign language ___ (1) a lot of time and practice. However, it is very rewarding. You can communicate with people from other ___ (2) and understand their culture better.");
        newTest.setBlanksCount(2);
        newTest.setCompletionStatus("Pending");
        newTest.setScore(0);
        newTest.setCreateTime(LocalDateTime.now());
        clozeService.save(newTest);
        return Result.success("生成选词填空成功");
    }

    @PutMapping("/{id}/score")
    public Result<String> updateScore(@PathVariable Long id, @RequestBody Cloze cloze) {
        Cloze existing = clozeService.getById(id);
        if (existing == null) {
            return Result.error("Test not found");
        }
        existing.setScore(cloze.getScore());
        existing.setCompletionStatus("Completed");
        clozeService.updateById(existing);
        return Result.success("得分保存成功");
    }
}
