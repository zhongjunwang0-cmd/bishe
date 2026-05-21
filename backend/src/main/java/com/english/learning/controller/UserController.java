package com.english.learning.controller;

import com.english.learning.common.Result;
import com.english.learning.entity.User;
import com.english.learning.service.UserService;
import com.english.learning.util.PasswordService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordService passwordService;

    @PostMapping("/login")
    public Result<?> login(@RequestBody User loginUser) {
        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(loginUser.getUsername(), loginUser.getPassword());
        try {
            subject.login(token);
            User user = (User) subject.getPrincipal();
            return Result.success(user);
        } catch (Exception e) {
            return Result.error("登录失败：" + e.getMessage());
        }
    }

    @PostMapping("/register")
    public Result<?> register(@RequestBody User user) {
        if (userService.findByUsername(user.getUsername()) != null) {
            return Result.error("用户名已存在");
        }
        user.setRoleId(3L);
        user.setPassword(passwordService.encode(user.getPassword()));
        userService.save(user);
        return Result.success("注册成功");
    }

    @GetMapping("/logout")
    public Result<?> logout() {
        SecurityUtils.getSubject().logout();
        return Result.success("退出成功");
    }

    @PostMapping("/forgot-password")
    public Result<?> forgotPassword(@RequestBody User user) {
        User existingUser = userService.findByUsername(user.getUsername());
        if (existingUser == null) {
            return Result.error("用户名不存在");
        }
        existingUser.setPassword(passwordService.encode(user.getPassword()));
        userService.save(existingUser);
        return Result.success("密码重置成功");
    }
}
