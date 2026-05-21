package com.english.learning.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.english.learning.common.Result;
import com.english.learning.entity.Reading;
import com.english.learning.service.ReadingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reading")
public class ReadingController {

    @Autowired
    private ReadingService readingService;

    @GetMapping("/list")
    public Result<List<Reading>> list() {
        List<Reading> list = readingService.list(new QueryWrapper<Reading>().orderByDesc("create_time"));
        return Result.success(list);
    }

    @PostMapping("/generate")
    public Result<String> generate() {
        Reading newTest = new Reading();
        newTest.setTitle("AI Generated Reading Test " + UUID.randomUUID().toString().substring(0, 5));
        newTest.setContent("This is an automatically generated reading comprehension test. Please read the passage carefully and answer the questions below. \n\nLorem ipsum dolor sit amet, consectetur adipiscing elit...");
        newTest.setDifficulty(Math.random() > 0.5 ? "Medium" : "Hard");
        newTest.setScore(0);
        newTest.setCreateTime(LocalDateTime.now());
        readingService.save(newTest);
        return Result.success("生成阅读测试成功");
    }

    @org.springframework.web.bind.annotation.PutMapping("/{id}/score")
    public Result<String> updateScore(@org.springframework.web.bind.annotation.PathVariable Long id, @org.springframework.web.bind.annotation.RequestBody Reading reading) {
        Reading existing = readingService.getById(id);
        if (existing == null) {
            return Result.error("Test not found");
        }
        existing.setScore(reading.getScore());
        readingService.updateById(existing);
        return Result.success("得分保存成功");
    }
}
