package com.english.learning.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.english.learning.common.Result;
import com.english.learning.entity.User;
import com.english.learning.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> listUsers() {
        List<User> users = userService.list();
        List<Map<String, Object>> result = users.stream().map(user -> {
            String roleStr = "User";
            if (user.getRoleId() == 1L) roleStr = "Admin";
            else if (user.getRoleId() == 2L) roleStr = "Teacher";

            return Map.<String, Object>of(
                "id", user.getId(),
                "username", user.getUsername(),
                "role", roleStr,
                "status", true // Mock status for now
            );
        }).collect(Collectors.toList());
        return Result.success(result);
    }

    @PostMapping("/add")
    public Result<String> addUser(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String role = body.get("role");
        if (userService.findByUsername(username) != null) {
            return Result.error("用户名已存在");
        }
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword("123456"); // default default password
        newUser.setNickname(username);
        newUser.setRoleId("Admin".equals(role) ? 1L : ("Teacher".equals(role) ? 2L : 3L));
        newUser.setCreateTime(LocalDateTime.now());
        userService.save(newUser);
        return Result.success("用户添加成功");
    }

    @PutMapping("/{id}/role")
    public Result<String> updateRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newRole = body.get("role");
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setRoleId("Admin".equals(newRole) ? 1L : ("Teacher".equals(newRole) ? 2L : 3L));
        userService.updateById(user);
        return Result.success("权限更新成功");
    }

    @PutMapping("/{id}/reset-password")
    public Result<String> resetPassword(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword("123456");
        userService.updateById(user);
        return Result.success("密码重置成功");
    }
}
