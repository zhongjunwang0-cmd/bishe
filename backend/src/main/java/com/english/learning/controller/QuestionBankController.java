package com.english.learning.controller;

import com.english.learning.common.Result;
import com.english.learning.entity.QuestionBank;
import com.english.learning.service.QuestionBankService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/question-bank")
@RequiresRoles(value = {"ADMIN", "TEACHER"}, logical = Logical.OR)
public class QuestionBankController {

    @Autowired
    private QuestionBankService questionBankService;

    @GetMapping("/list")
    public Result<List<QuestionBank>> list(@RequestParam(required = false) String moduleType) {
        return Result.success(questionBankService.listByModule(moduleType));
    }

    @GetMapping("/{id}")
    public Result<QuestionBank> detail(@PathVariable Long id) {
        QuestionBank bank = questionBankService.getById(id);
        if (bank == null) {
            return Result.error("题库不存在");
        }
        return Result.success(bank);
    }

    @PostMapping
    public Result<Long> add(@RequestBody QuestionBank bank) {
        if (bank.getModuleType() == null || bank.getTitle() == null) {
            return Result.error("题型与标题不能为空");
        }
        if (bank.getQuestionsJson() == null || bank.getAnswersJson() == null) {
            return Result.error("题目与答案 JSON 不能为空");
        }
        bank.setStatus(bank.getStatus() == null ? "Active" : bank.getStatus());
        bank.setCreateTime(LocalDateTime.now());
        bank.setUpdateTime(LocalDateTime.now());
        questionBankService.save(bank);
        return Result.success(bank.getId());
    }

    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @RequestBody QuestionBank bank) {
        QuestionBank existing = questionBankService.getById(id);
        if (existing == null) {
            return Result.error("题库不存在");
        }
        bank.setId(id);
        bank.setUpdateTime(LocalDateTime.now());
        questionBankService.updateById(bank);
        return Result.success("题库更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        questionBankService.removeById(id);
        return Result.success("题库删除成功");
    }

    @PutMapping("/{id}/status")
    public Result<String> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        QuestionBank existing = questionBankService.getById(id);
        if (existing == null) {
            return Result.error("题库不存在");
        }
        existing.setStatus(body.get("status"));
        existing.setUpdateTime(LocalDateTime.now());
        questionBankService.updateById(existing);
        return Result.success("状态更新成功");
    }
}
