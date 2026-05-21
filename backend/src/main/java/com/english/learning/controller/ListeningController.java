package com.english.learning.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.english.learning.common.Result;
import com.english.learning.entity.Listening;
import com.english.learning.service.ListeningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/listening")
public class ListeningController {
    
    @Autowired
    private ListeningService listeningService;

    @GetMapping("/list")
    public Result<List<Listening>> list() {
        QueryWrapper<Listening> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        return Result.success(listeningService.list(queryWrapper));
    }

    @PostMapping("/generate")
    public Result<String> generate() {
        Listening newTest = new Listening();
        newTest.setTitle("AI Listening Task " + UUID.randomUUID().toString().substring(0, 5));
        newTest.setCategory(Math.random() > 0.5 ? "Conversation" : "Lecture");
        newTest.setDuration((int)(Math.random() * 3 + 2) + ":" + (int)(Math.random() * 50 + 10));
        newTest.setAudioUrl("https://onlinetestcase.com/wp-content/uploads/2023/06/100-KB-MP3.mp3"); // placeholder audio
        newTest.setScore(0);
        newTest.setCreateTime(LocalDateTime.now());
        listeningService.save(newTest);
        return Result.success("生成听力训练成功");
    }

    @PutMapping("/{id}/score")
    public Result<String> updateScore(@PathVariable Long id, @RequestBody Listening listening) {
        Listening existing = listeningService.getById(id);
        if (existing == null) {
            return Result.error("Test not found");
        }
        existing.setScore(listening.getScore());
        listeningService.updateById(existing);
        return Result.success("得分保存成功");
    }
}
