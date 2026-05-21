package com.english.learning.controller;

import com.english.learning.common.Result;
import com.english.learning.dto.UserVocabVO;
import com.english.learning.dto.VocabReviewSubmitRequest;
import com.english.learning.entity.User;
import com.english.learning.service.UserVocabService;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vocab")
public class VocabReviewController {

    @Autowired
    private UserVocabService userVocabService;

    @GetMapping("/book/list")
    public Result<List<UserVocabVO>> listBook() {
        User user = currentUser();
        if (user == null) {
            return Result.error("未登录");
        }
        return Result.success(userVocabService.listUserBook(user.getId()));
    }

    @GetMapping("/book/stats")
    public Result<Map<String, Object>> stats() {
        User user = currentUser();
        if (user == null) {
            return Result.error("未登录");
        }
        return Result.success(userVocabService.getStats(user.getId()));
    }

    @PostMapping("/book/add/{vocabId}")
    public Result<UserVocabVO> addToBook(@PathVariable Long vocabId) {
        User user = currentUser();
        if (user == null) {
            return Result.error("未登录");
        }
        try {
            return Result.success(userVocabService.addToBook(user.getId(), vocabId));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/book/remove/{vocabId}")
    public Result<?> removeFromBook(@PathVariable Long vocabId) {
        User user = currentUser();
        if (user == null) {
            return Result.error("未登录");
        }
        userVocabService.removeFromBook(user.getId(), vocabId);
        return Result.success("已移出生词本");
    }

    @GetMapping("/review/today")
    public Result<List<UserVocabVO>> todayReview() {
        User user = currentUser();
        if (user == null) {
            return Result.error("未登录");
        }
        return Result.success(userVocabService.listDueReview(user.getId()));
    }

    @PostMapping("/review/submit")
    public Result<UserVocabVO> submitReview(@RequestBody VocabReviewSubmitRequest request) {
        User user = currentUser();
        if (user == null) {
            return Result.error("未登录");
        }
        if (request.getVocabId() == null || request.getRemembered() == null) {
            return Result.error("参数不完整");
        }
        try {
            return Result.success(userVocabService.submitReview(
                    user.getId(), request.getVocabId(), request.getRemembered()));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/book/check/{vocabId}")
    public Result<Boolean> checkInBook(@PathVariable Long vocabId) {
        User user = currentUser();
        if (user == null) {
            return Result.error("未登录");
        }
        return Result.success(userVocabService.isInBook(user.getId(), vocabId));
    }

    private User currentUser() {
        return (User) SecurityUtils.getSubject().getPrincipal();
    }
}
