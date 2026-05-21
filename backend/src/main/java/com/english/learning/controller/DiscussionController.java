package com.english.learning.controller;

import com.english.learning.common.Result;
import com.english.learning.entity.Discuss;
import com.english.learning.entity.User;
import com.english.learning.service.DiscussService;
import com.english.learning.service.UserService;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/discussion")
public class DiscussionController {

    @Autowired
    private DiscussService discussService;

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public Result<?> list() {
        List<Discuss> discussions = discussService.listAllCached();

        List<Map<String, Object>> comments = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (Discuss discuss : discussions) {
            Map<String, Object> comment = new HashMap<>();
            comment.put("id", discuss.getId());
            
            User user = userService.getById(discuss.getUserId());
            String username = user != null ? (user.getNickname() != null ? user.getNickname() : user.getUsername()) : "未知用户";
            
            comment.put("user", username);
            comment.put("time", discuss.getCreateTime() != null ? discuss.getCreateTime().format(formatter) : "");
            comment.put("text", discuss.getContent());
            comment.put("likes", 0);
            
            comments.add(comment);
        }

        return Result.success(comments);
    }

    @PostMapping("/add")
    @CacheEvict(value = "discussionList", allEntries = true)
    public Result<?> add(@RequestBody Map<String, String> payload) {
        User currentUser = (User) SecurityUtils.getSubject().getPrincipal();
        if (currentUser == null) {
            return Result.error("未登录");
        }

        String content = payload.get("text");
        if (content == null || content.trim().isEmpty()) {
            return Result.error("评论内容不能为空");
        }

        Discuss discuss = new Discuss();
        discuss.setUserId(currentUser.getId());
        discuss.setContent(content);
        discuss.setTargetType("GENERAL");
        discuss.setTargetId(0L);
        discussService.save(discuss);

        return Result.success("发布成功");
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = "discussionList", allEntries = true)
    public Result<?> delete(@PathVariable Long id) {
        discussService.removeById(id);
        return Result.success("删除成功");
    }
}
