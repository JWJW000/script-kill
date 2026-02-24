package com.scriptkill.controller;

import com.scriptkill.common.Result;
import com.scriptkill.dto.LoginDTO;
import com.scriptkill.dto.RegisterDTO;
import com.scriptkill.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Validated @RequestBody LoginDTO loginDTO) {
        try {
            String token = userService.login(loginDTO);
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            return Result.success("登录成功", data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/register")
    public Result<?> register(@Validated @RequestBody RegisterDTO registerDTO) {
        try {
            userService.register(registerDTO);
            return Result.success("注册成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/info")
    public Result<?> getCurrentUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录或token已过期");
        }
        com.scriptkill.entity.User user = userService.getCurrentUser(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        return Result.success(user);
    }
}
