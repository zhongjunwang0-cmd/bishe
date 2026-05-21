package com.english.learning.controller;

import com.english.learning.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    @GetMapping("/info")
    public Result<?> info() {
        return Result.success(new HashMap<>());
    }
}
