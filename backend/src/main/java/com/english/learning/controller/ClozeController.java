package com.english.learning.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.english.learning.common.Result;
import com.english.learning.dto.SubmitAnswersRequest;
import com.english.learning.entity.Cloze;
import com.english.learning.service.ClozeService;
import com.english.learning.service.QuestionGenerateService;
import com.english.learning.util.ClozeLegacyParser;
import com.english.learning.util.JsonUtil;
import com.english.learning.util.ScoreUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cloze")
public class ClozeController {

    @Autowired
    private ClozeService clozeService;

    @Autowired
    private QuestionGenerateService questionGenerateService;

    @GetMapping("/list")
    public Result<List<Cloze>> list() {
        QueryWrapper<Cloze> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        return Result.success(clozeService.list(queryWrapper));
    }

    @GetMapping("/{id}")
    public Result<Cloze> detail(@PathVariable Long id) {
        Cloze cloze = clozeService.getById(id);
        if (cloze == null) {
            return Result.error("测试不存在");
        }
        ensureStructured(cloze);
        cloze.setQuestions(JsonUtil.parse(cloze.getQuestionsJson()));
        return Result.success(cloze);
    }

    @GetMapping("/{id}/answers")
    public Result<Object> answers(@PathVariable Long id) {
        Cloze cloze = clozeService.getById(id);
        if (cloze == null) {
            return Result.error("测试不存在");
        }
        ensureStructured(cloze);
        return Result.success(JsonUtil.parse(cloze.getAnswersJson()));
    }

    private void ensureStructured(Cloze cloze) {
        if (cloze.getQuestionsJson() != null && !cloze.getQuestionsJson().isBlank()) {
            return;
        }
        ClozeLegacyParser.Parsed parsed = ClozeLegacyParser.parse(cloze.getContent());
        if (parsed == null) {
            return;
        }
        cloze.setContent(parsed.getContent());
        cloze.setQuestionsJson(parsed.getQuestionsJson());
        cloze.setAnswersJson(parsed.getAnswersJson());
        cloze.setBlanksCount(parsed.getBlankCount());
        clozeService.updateById(cloze);
    }

    @PostMapping("/generate")
    public Result<String> generate() {
        Cloze cloze = questionGenerateService.generateCloze();
        if (cloze == null) {
            return Result.error("暂无可用选词填空题库，请联系教师添加");
        }
        return Result.success("已从题库随机生成选词填空");
    }

    @PostMapping("/{id}/submit")
    public Result<Map<String, Object>> submit(@PathVariable Long id, @RequestBody SubmitAnswersRequest request) {
        Cloze existing = clozeService.getById(id);
        if (existing == null) {
            return Result.error("测试不存在");
        }
        ensureStructured(existing);
        if (request.getAnswers() == null || request.getAnswers().isEmpty()) {
            return Result.error("请提交答案");
        }
        int score = ScoreUtil.calculateScore(existing.getAnswersJson(), request.getAnswers());
        existing.setScore(score);
        existing.setCompletionStatus("Completed");
        clozeService.updateById(existing);
        Map<String, Object> result = new HashMap<>();
        result.put("score", score);
        return Result.success(result);
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
