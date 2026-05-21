package com.english.learning.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.english.learning.common.Result;
import com.english.learning.entity.Oral;
import com.english.learning.service.OralService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/oral")
public class OralController {

    private static final List<String> ORAL_TOPIC_POOL = List.of(
            "介绍你的家乡及其文化特色",
            "描述一次难忘的旅行经历",
            "谈谈你对人工智能未来发展的看法",
            "如何平衡学业压力与个人生活",
            "描述一位对你影响最大的人物",
            "就环境保护发表你的观点",
            "分析中西方教育体系的异同",
            "描述你理想中的职业与未来规划",
            "描述你最喜欢的一部电影或书籍",
            "谈谈你的爱好与特长",
            "介绍你的学习或工作计划",
            "分享一次印象深刻的团队合作经历",
            "描述你理想中的大学生活",
            "谈谈网络购物的利与弊",
            "介绍一道你家乡的特色美食",
            "描述一次与朋友的愉快聚会",
            "谈谈如何保持健康的生活方式",
            "介绍你最喜欢的一座城市"
    );

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
        Set<String> existingTopics = oralService.list(new QueryWrapper<Oral>().select("topic"))
                .stream()
                .map(Oral::getTopic)
                .collect(Collectors.toSet());

        List<String> availableTopics = ORAL_TOPIC_POOL.stream()
                .filter(topic -> !existingTopics.contains(topic))
                .collect(Collectors.toList());

        if (availableTopics.isEmpty()) {
            return Result.error("所有口语练习主题均已添加，请从列表中选择练习");
        }

        Collections.shuffle(availableTopics);
        String topic = availableTopics.get(0);

        Oral newTopic = new Oral();
        newTopic.setTopic(topic);
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
