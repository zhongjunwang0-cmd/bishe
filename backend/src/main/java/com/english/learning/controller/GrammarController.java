package com.english.learning.controller;

import com.english.learning.common.Result;
import com.english.learning.entity.Grammar;
import com.english.learning.service.GrammarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grammar")
public class GrammarController {

    @Autowired
    private GrammarService grammarService;

    @GetMapping("/list")
    public Result<List<Grammar>> list() {
        return Result.success(grammarService.listAllCached());
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
}
