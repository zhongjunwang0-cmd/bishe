package com.english.learning.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.english.learning.common.Result;
import com.english.learning.dto.SubmitAnswersRequest;
import com.english.learning.entity.Reading;
import com.english.learning.service.QuestionBankService;
import com.english.learning.service.QuestionGenerateService;
import com.english.learning.service.ReadingService;
import com.english.learning.util.JsonUtil;
import com.english.learning.util.ScoreUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reading")
public class ReadingController {

    @Autowired
    private ReadingService readingService;

    @Autowired
    private QuestionGenerateService questionGenerateService;

    @Autowired
    private QuestionBankService questionBankService;

    @GetMapping("/list")
    public Result<List<Reading>> list() {
        List<Reading> list = readingService.list(new QueryWrapper<Reading>().orderByDesc("create_time"));
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<Reading> detail(@PathVariable Long id) {
        Reading reading = readingService.getById(id);
        if (reading == null) {
            return Result.error("测试不存在");
        }
        reading.setQuestions(JsonUtil.parse(reading.getQuestionsJson()));
        return Result.success(reading);
    }

    @GetMapping("/{id}/answers")
    public Result<Object> answers(@PathVariable Long id) {
        Reading reading = readingService.getById(id);
        if (reading == null) {
            return Result.error("测试不存在");
        }
        return Result.success(JsonUtil.parse(reading.getAnswersJson()));
    }

    @PostMapping("/generate")
    public Result<String> generate() {
        Reading reading = questionGenerateService.generateReading();
        if (reading == null) {
            if (questionBankService.listByModule("READING").stream()
                    .noneMatch(b -> "Active".equals(b.getStatus()))) {
                return Result.error("暂无可用阅读题库，请联系教师添加");
            }
            return Result.error("题库中的阅读文章已全部生成，暂无新测试");
        }
        return Result.success("已从题库随机生成阅读测试");
    }

    @PostMapping("/{id}/submit")
    public Result<Map<String, Object>> submit(@PathVariable Long id, @RequestBody SubmitAnswersRequest request) {
        Reading existing = readingService.getById(id);
        if (existing == null) {
            return Result.error("测试不存在");
        }
        if (request.getAnswers() == null || request.getAnswers().isEmpty()) {
            return Result.error("请提交答案");
        }
        int score = ScoreUtil.calculateScore(existing.getAnswersJson(), request.getAnswers());
        existing.setScore(score);
        readingService.updateById(existing);
        Map<String, Object> result = new HashMap<>();
        result.put("score", score);
        return Result.success(result);
    }

    @PutMapping("/{id}/score")
    public Result<String> updateScore(@PathVariable Long id, @RequestBody Reading reading) {
        Reading existing = readingService.getById(id);
        if (existing == null) {
            return Result.error("Test not found");
        }
        existing.setScore(reading.getScore());
        readingService.updateById(existing);
        return Result.success("得分保存成功");
    }
}
