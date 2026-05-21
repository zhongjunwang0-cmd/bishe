package com.english.learning.controller;

import com.english.learning.common.Result;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Collections;

@RestController
@RequestMapping("/api/admin/interaction")
@RequiresRoles("ADMIN")
public class AdminInteractionController {
    @GetMapping("/test/list")
    public Result<?> listTests() {
        return Result.success(Collections.emptyList());
    }

    @GetMapping("/discussion/list")
    public Result<?> listDiscussions() {
        return Result.success(Collections.emptyList());
    }
}
