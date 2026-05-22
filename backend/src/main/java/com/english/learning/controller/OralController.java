package com.english.learning.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.english.learning.common.Result;
import com.english.learning.dto.PronunciationScoreDto;
import com.english.learning.entity.Oral;
import com.english.learning.service.AiModelClient;
import com.english.learning.service.OralService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/oral")
public class OralController {

    private record OralPrompt(String topic, String referenceText) {}

    private static final List<OralPrompt> ORAL_TOPIC_POOL = List.of(
            new OralPrompt("介绍你的家乡及其文化特色",
                    "My hometown is famous for its rich history, local food, and friendly people."),
            new OralPrompt("描述一次难忘的旅行经历",
                    "Last summer I visited a beautiful city and learned a lot about its culture."),
            new OralPrompt("谈谈你对人工智能未来发展的看法",
                    "I believe artificial intelligence will help people learn and work more efficiently."),
            new OralPrompt("如何平衡学业压力与个人生活",
                    "Students should manage their time well to balance study pressure and personal life."),
            new OralPrompt("描述一位对你影响最大的人物",
                    "The person who influenced me most taught me to work hard and never give up."),
            new OralPrompt("就环境保护发表你的观点",
                    "We should protect the environment by saving energy and reducing waste every day."),
            new OralPrompt("分析中西方教育体系的异同",
                    "Chinese and Western education systems both value knowledge but emphasize different skills."),
            new OralPrompt("描述你理想中的职业与未来规划",
                    "In the future I hope to find a meaningful job and keep improving my English skills."),
            new OralPrompt("描述你最喜欢的一部电影或书籍",
                    "My favorite book inspires me to stay curious and keep learning new things."),
            new OralPrompt("谈谈你的爱好与特长",
                    "My favorite hobby helps me relax and express myself in a creative way."),
            new OralPrompt("介绍你的学习或工作计划",
                    "This semester I plan to study English regularly and practice speaking every week."),
            new OralPrompt("分享一次印象深刻的团队合作经历",
                    "During a team project we shared ideas and finished the task successfully together."),
            new OralPrompt("描述你理想中的大学生活",
                    "My ideal university life includes active learning, new friends, and healthy habits."),
            new OralPrompt("谈谈网络购物的利与弊",
                    "Online shopping is convenient, but we should spend money wisely and carefully."),
            new OralPrompt("介绍一道你家乡的特色美食",
                    "My hometown is well known for a delicious local dish that many visitors enjoy."),
            new OralPrompt("描述一次与朋友的愉快聚会",
                    "I had a wonderful time with my friends when we talked, laughed, and shared stories."),
            new OralPrompt("谈谈如何保持健康的生活方式",
                    "A healthy lifestyle includes regular exercise, enough sleep, and a balanced diet."),
            new OralPrompt("介绍你最喜欢的一座城市",
                    "My favorite city has beautiful scenery, convenient transport, and a lively culture.")
    );

    private static final Map<String, String> REFERENCE_BY_TOPIC = ORAL_TOPIC_POOL.stream()
            .collect(Collectors.toMap(OralPrompt::topic, OralPrompt::referenceText, (a, b) -> a, LinkedHashMap::new));

    @Autowired
    private OralService oralService;

    @Autowired
    private AiModelClient aiModelClient;

    @GetMapping("/list")
    public Result<List<Oral>> list() {
        QueryWrapper<Oral> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        List<Oral> items = oralService.list(queryWrapper);
        for (Oral item : items) {
            if (item.getReferenceText() == null || item.getReferenceText().isBlank()) {
                item.setReferenceText(REFERENCE_BY_TOPIC.get(item.getTopic()));
            }
        }
        return Result.success(items);
    }

    @PostMapping("/generate")
    public Result<String> generate() {
        Set<String> existingTopics = oralService.list(new QueryWrapper<Oral>().select("topic"))
                .stream()
                .map(Oral::getTopic)
                .collect(Collectors.toSet());

        List<OralPrompt> availableTopics = ORAL_TOPIC_POOL.stream()
                .filter(prompt -> !existingTopics.contains(prompt.topic()))
                .collect(Collectors.toList());

        if (availableTopics.isEmpty()) {
            return Result.error("所有口语练习主题均已添加，请从列表中选择练习");
        }

        Collections.shuffle(availableTopics);
        OralPrompt prompt = availableTopics.get(0);

        Oral newTopic = new Oral();
        newTopic.setTopic(prompt.topic());
        newTopic.setReferenceText(prompt.referenceText());
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

    @PostMapping("/{id}/evaluate-audio")
    public Result<PronunciationScoreDto> evaluateAudio(
            @PathVariable Long id,
            @RequestParam(value = "referenceText", required = false) String referenceText,
            @RequestParam("audio") MultipartFile audio) {
        Oral existing = oralService.getById(id);
        if (existing == null) {
            return Result.error("Topic not found");
        }
        if (audio == null || audio.isEmpty()) {
            return Result.error("请上传录音文件");
        }

        String ref = referenceText;
        if (ref == null || ref.isBlank()) {
            ref = existing.getReferenceText();
        }
        if (ref == null || ref.isBlank()) {
            ref = REFERENCE_BY_TOPIC.get(existing.getTopic());
        }
        if (ref == null || ref.isBlank()) {
            return Result.error("缺少英文参考句，无法评测发音");
        }

        try {
            PronunciationScoreDto dto = aiModelClient.scorePronunciation(
                    ref,
                    audio.getBytes(),
                    audio.getOriginalFilename()
            );
            if (dto == null) {
                dto = buildOfflineFallback(ref);
            }
            normalizePronunciationResult(dto, ref);

            Integer currentHighest = existing.getScore();
            if (currentHighest == null || dto.getScore() > currentHighest) {
                existing.setScore(dto.getScore());
            }
            existing.setAttempts(existing.getAttempts() + 1);
            if (existing.getReferenceText() == null || existing.getReferenceText().isBlank()) {
                existing.setReferenceText(ref);
            }
            oralService.updateById(existing);
            return Result.success(dto);
        } catch (Exception e) {
            return Result.error("录音评测失败: " + e.getMessage());
        }
    }

    private void normalizePronunciationResult(PronunciationScoreDto dto, String referenceText) {
        String transcript = dto.getTranscript();
        if (transcript != null && (transcript.contains("install openai-whisper") || transcript.contains("Demo mode"))) {
            dto.setTranscript("");
        }
        if (dto.getFeedback() != null && dto.getFeedback().contains("Demo mode")) {
            dto.setFeedback("本次未能完成自动评测，请参考下方朗读建议练习后重试。");
        }
        if (dto.getSuggestions() == null || dto.getSuggestions().isEmpty()) {
            dto.setSuggestions(buildReadingSuggestions(referenceText, dto));
        } else if (dto.getSuggestions().size() > 2) {
            dto.setSuggestions(new ArrayList<>(dto.getSuggestions().subList(0, 2)));
        }
    }

    private List<String> buildReadingSuggestions(String referenceText, PronunciationScoreDto dto) {
        List<String> tips = new ArrayList<>();
        int score = dto.getScore();

        if (score >= 85) {
            tips.add("朗读准确流畅，可略微加快语速并保持清晰。");
            return tips;
        }

        if (dto.getMisreadWords() != null && !dto.getMisreadWords().isEmpty()) {
            tips.add("重点练习：" + String.join("、", dto.getMisreadWords().subList(
                    0, Math.min(3, dto.getMisreadWords().size()))) + "。");
            tips.add("建议慢速跟读参考句 2～3 遍。");
            return tips;
        }

        if (score >= 70) {
            tips.add("整体不错，请慢速跟读参考句，注意句重音。");
        } else if (dto.getTranscript() != null && !dto.getTranscript().isBlank()) {
            tips.add("与参考句有差异，请对照识别结果逐词练习。");
        } else {
            tips.add("请靠近麦克风、减少噪音，逐词读完整句后再试。");
        }
        return tips;
    }

    private PronunciationScoreDto buildOfflineFallback(String referenceText) {
        PronunciationScoreDto dto = new PronunciationScoreDto();
        dto.setScore(0);
        dto.setWer(1.0);
        dto.setTranscript("");
        dto.setFeedback("Whisper 评测服务暂不可用，已生成朗读建议，请对照参考句练习后重试。");
        dto.setSource("Whisper-WER");
        dto.setSuggestions(buildReadingSuggestions(referenceText, dto));
        return dto;
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
