package com.english.learning.controller;

import com.english.learning.common.Result;
import com.english.learning.dto.GrammarCorrectDto;
import com.english.learning.dto.SubmitAnswersRequest;
import com.english.learning.entity.Grammar;
import com.english.learning.entity.LearningRecord;
import com.english.learning.entity.User;
import com.english.learning.service.AiModelClient;
import com.english.learning.service.GrammarService;
import com.english.learning.service.LearningRecordService;
import com.english.learning.util.EnglishWritingAnalyzer;
import com.english.learning.util.GrammarTestBank;
import com.english.learning.util.ScoreUtil;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/grammar")
public class GrammarController {

    @Autowired
    private GrammarService grammarService;

    @Autowired
    private LearningRecordService learningRecordService;

    @Autowired
    private AiModelClient aiModelClient;

    @PostMapping("/correct")
    public Result<GrammarCorrectDto> correct(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        if (text == null || text.isBlank()) {
            return Result.error("请输入需要纠错的英文句子");
        }

        GrammarCorrectDto dto = aiModelClient.correctGrammar(text.trim());
        if (dto == null) {
            EnglishWritingAnalyzer.AnalysisResult analysis = EnglishWritingAnalyzer.analyze(text.trim());
            dto = new GrammarCorrectDto();
            dto.setCorrected(analysis.corrected);
            dto.setSource("rule_fallback");
            List<Map<String, String>> issues = new ArrayList<>();
            for (EnglishWritingAnalyzer.GrammarIssue issue : analysis.issues) {
                Map<String, String> item = new LinkedHashMap<>();
                item.put("type", issue.type);
                item.put("message", issue.problem + " → " + issue.correction);
                issues.add(item);
            }
            dto.setIssues(issues);
        }
        return Result.success(dto);
    }

    @GetMapping("/list")
    public Result<List<Grammar>> list() {
        return Result.success(grammarService.listAllCached());
    }

    @GetMapping("/{id}/test")
    public Result<Map<String, Object>> test(@PathVariable Long id) {
        Grammar grammar = grammarService.getById(id);
        if (grammar == null) {
            return Result.error("语法知识点不存在");
        }
        List<GrammarTestBank.GrammarQuestion> bank = GrammarTestBank.getQuestions(id);
        List<Map<String, Object>> questions = new ArrayList<>();
        for (GrammarTestBank.GrammarQuestion item : bank) {
            Map<String, Object> question = new LinkedHashMap<>();
            question.put("question", item.question());
            question.put("options", item.options());
            questions.add(question);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("grammar", grammar);
        data.put("questions", questions);
        return Result.success(data);
    }

    @PostMapping("/{id}/test/submit")
    public Result<Map<String, Object>> submitTest(@PathVariable Long id,
                                                  @RequestBody SubmitAnswersRequest request) {
        Grammar grammar = grammarService.getById(id);
        if (grammar == null) {
            return Result.error("语法知识点不存在");
        }
        if (request.getAnswers() == null || request.getAnswers().isEmpty()) {
            return Result.error("请提交答案");
        }
        List<GrammarTestBank.GrammarQuestion> bank = GrammarTestBank.getQuestions(id);
        String answersJson = buildAnswersJson(bank);
        int score = ScoreUtil.calculateScore(answersJson, request.getAnswers());

        User currentUser = (User) SecurityUtils.getSubject().getPrincipal();
        if (currentUser != null) {
            LearningRecord record = new LearningRecord();
            record.setUserId(currentUser.getId());
            record.setType("GRAMMAR");
            record.setTargetId(id);
            record.setDuration(180);
            record.setScore(score);
            learningRecordService.save(record);
        }

        List<Map<String, String>> details = new ArrayList<>();
        for (int i = 0; i < bank.size(); i++) {
            GrammarTestBank.GrammarQuestion item = bank.get(i);
            Map<String, String> detail = new LinkedHashMap<>();
            detail.put("correct", item.correct());
            detail.put("explanation", item.explanation());
            if (i < request.getAnswers().size()) {
                detail.put("userAnswer", request.getAnswers().get(i));
            }
            details.add(detail);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("score", score);
        data.put("details", details);
        return Result.success(data);
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody Grammar grammar) {
        grammarService.saveWithEvict(grammar);
        return Result.success("添加成功");
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Long id) {
        grammarService.removeWithEvict(id);
        return Result.success("删除成功");
    }

    private String buildAnswersJson(List<GrammarTestBank.GrammarQuestion> bank) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < bank.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append("{\"correct\":\"").append(bank.get(i).correct()).append("\"}");
        }
        sb.append(']');
        return sb.toString();
    }
}
