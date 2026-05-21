package com.english.learning.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.english.learning.common.Result;
import com.english.learning.dto.SubmitAnswersRequest;
import com.english.learning.entity.Listening;
import com.english.learning.service.ListeningService;
import com.english.learning.service.QuestionGenerateService;
import com.english.learning.util.JsonUtil;
import com.english.learning.util.ScoreUtil;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/listening")
public class ListeningController {

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/data/uploads/";
    private static final Set<String> ALLOWED_AUDIO_EXTENSIONS = Set.of(".mp3", ".wav", ".m4a", ".ogg");

    @Autowired
    private ListeningService listeningService;

    @Autowired
    private QuestionGenerateService questionGenerateService;

    @GetMapping("/list")
    public Result<List<Listening>> list() {
        QueryWrapper<Listening> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        return Result.success(listeningService.list(queryWrapper));
    }

    @GetMapping("/{id}")
    public Result<Listening> detail(@PathVariable Long id) {
        Listening listening = listeningService.getById(id);
        if (listening == null) {
            return Result.error("测试不存在");
        }
        listening.setQuestions(JsonUtil.parse(listening.getQuestionsJson()));
        return Result.success(listening);
    }

    @GetMapping("/{id}/answers")
    public Result<Object> answers(@PathVariable Long id) {
        Listening listening = listeningService.getById(id);
        if (listening == null) {
            return Result.error("测试不存在");
        }
        return Result.success(JsonUtil.parse(listening.getAnswersJson()));
    }

    @PostMapping("/upload")
    @RequiresRoles(value = {"ADMIN", "TEACHER"}, logical = Logical.OR)
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件为空");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase(Locale.ROOT)
                : "";
        if (!ALLOWED_AUDIO_EXTENSIONS.contains(extension)) {
            return Result.error("仅支持 MP3、WAV、M4A、OGG 格式");
        }

        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String newFilename = UUID.randomUUID().toString() + extension;
        try {
            File dest = new File(UPLOAD_DIR + newFilename);
            file.transferTo(dest.getAbsoluteFile());
            return Result.success("/uploads/" + newFilename);
        } catch (IOException e) {
            return Result.error("音频上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/generate")
    public Result<String> generate() {
        Listening listening = questionGenerateService.generateListening();
        if (listening == null) {
            return Result.error("暂无可用听力题库，请联系教师添加");
        }
        return Result.success("已从题库随机生成听力训练");
    }

    @PostMapping("/publish/{bankId}")
    @RequiresRoles(value = {"ADMIN", "TEACHER"}, logical = Logical.OR)
    public Result<Listening> publish(@PathVariable Long bankId) {
        Listening listening = questionGenerateService.publishListeningFromBank(bankId);
        if (listening == null) {
            return Result.error("题库不存在或不是听力类型");
        }
        return Result.success(listening);
    }

    @DeleteMapping("/{id}")
    @RequiresRoles(value = {"ADMIN", "TEACHER"}, logical = Logical.OR)
    public Result<String> delete(@PathVariable Long id) {
        Listening existing = listeningService.getById(id);
        if (existing == null) {
            return Result.error("听力材料不存在");
        }
        listeningService.removeById(id);
        return Result.success("删除成功");
    }

    @PostMapping("/{id}/submit")
    public Result<Map<String, Object>> submit(@PathVariable Long id, @RequestBody SubmitAnswersRequest request) {
        Listening existing = listeningService.getById(id);
        if (existing == null) {
            return Result.error("测试不存在");
        }
        if (request.getAnswers() == null || request.getAnswers().isEmpty()) {
            return Result.error("请提交答案");
        }
        int score = ScoreUtil.calculateScore(existing.getAnswersJson(), request.getAnswers());
        existing.setScore(score);
        listeningService.updateById(existing);
        Map<String, Object> result = new HashMap<>();
        result.put("score", score);
        return Result.success(result);
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
