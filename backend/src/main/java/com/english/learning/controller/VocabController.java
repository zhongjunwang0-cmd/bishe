package com.english.learning.controller;

import com.english.learning.common.Result;
import com.english.learning.entity.Vocab;
import com.english.learning.service.VocabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vocab")
public class VocabController {

    @Autowired
    private VocabService vocabService;

    @GetMapping("/list")
    public Result<List<Vocab>> list() {
        return Result.success(vocabService.listAllCached());
    }

    @PostMapping("/add")
    @CacheEvict(value = "vocabList", allEntries = true)
    public Result<?> add(@RequestBody Vocab vocab) {
        vocabService.save(vocab);
        return Result.success("添加成功");
    }

    @DeleteMapping("/delete/{id}")
    @CacheEvict(value = "vocabList", allEntries = true)
    public Result<?> delete(@PathVariable Long id) {
        vocabService.removeById(id);
        return Result.success("删除成功");
    }
}
