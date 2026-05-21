package com.english.learning.controller;

import com.english.learning.common.Result;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Collections;

@RestController
@RequestMapping("/api/admin/content")
@RequiresRoles("ADMIN")
public class AdminContentController {
    @GetMapping("/vocab/list")
    public Result<?> listVocab() {
        return Result.success(Collections.emptyList());
    }

    @GetMapping("/grammar/list")
    public Result<?> listGrammar() {
        return Result.success(Collections.emptyList());
    }



    @GetMapping("/oral/list")
    public Result<?> listOral() {
        return Result.success(Collections.emptyList());
    }
}
