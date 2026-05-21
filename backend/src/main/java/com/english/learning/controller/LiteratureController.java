package com.english.learning.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.english.learning.common.Result;
import com.english.learning.entity.Literature;
import com.english.learning.service.LiteratureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/literature")
public class LiteratureController {

    @Autowired
    private LiteratureService literatureService;

    // The directory where uploaded files will be stored.
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/data/uploads/";

    @GetMapping("/list")
    public Result<List<Literature>> list() {
        return Result.success(literatureService.listAllCached());
    }

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件为空");
        }
        
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        String newFilename = UUID.randomUUID().toString() + extension;
        
        try {
            File dest = new File(UPLOAD_DIR + newFilename);
            System.out.println("Saving file to: " + dest.getAbsolutePath());
            file.transferTo(dest.getAbsoluteFile());
            // Return the URL path
            return Result.success("/uploads/" + newFilename);
        } catch (IOException e) {
            System.err.println("Upload error: " + e.getMessage());
            e.printStackTrace();
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    @PostMapping
    public Result<String> addLiterature(@RequestBody Literature literature) {
        literature.setCreateTime(LocalDateTime.now());
        literatureService.saveWithEvict(literature);
        return Result.success("新增文献成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteLiterature(@PathVariable Long id) {
        literatureService.removeWithEvict(id);
        return Result.success("删除成功");
    }
}
