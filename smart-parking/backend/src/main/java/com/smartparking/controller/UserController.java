package com.smartparking.controller;

import com.smartparking.common.Result;
import com.smartparking.dto.LoginDTO;
import com.smartparking.dto.RegisterDTO;
import com.smartparking.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户模块", description = "注册、登录、个人信息")
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.ok("注册成功");
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginDTO dto) {
        return Result.ok(userService.login(dto));
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<?> info(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(userService.getCurrentUser(userId));
    }
}
