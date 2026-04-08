package com.scriptkill.controller;

import com.scriptkill.common.Result;
import com.scriptkill.entity.User;
import com.scriptkill.service.OssService;
import com.scriptkill.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private OssService ossService;

    @GetMapping("/info")
    public Result<User> getCurrentUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getCurrentUser(userId);
        return Result.success(user);
    }

    @PutMapping("/info")
    public Result<?> updateUserInfo(@RequestBody User user, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User currentUser = userService.getById(userId);
        if (currentUser == null) {
            return Result.error("用户不存在");
        }
        if (user.getRealName() != null) {
            currentUser.setRealName(user.getRealName());
        }
        if (user.getPhone() != null) {
            currentUser.setPhone(user.getPhone());
        }
        userService.updateById(currentUser);
        return Result.success("更新成功");
    }

    @PostMapping("/password")
    public Result<?> updatePassword(@RequestParam String oldPassword, @RequestParam String newPassword, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            userService.updatePassword(userId, oldPassword, newPassword);
            return Result.success("密码修改成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            // 删除旧头像
            User user = userService.getById(userId);
            if (user != null && user.getAvatar() != null && user.getAvatar().contains("minio")) {
                ossService.delete(user.getAvatar());
            }

            // 上传新头像到 MinIO
            String avatarUrl = ossService.upload(file, "avatar");
            userService.updateAvatar(userId, avatarUrl);

            return Result.success("上传成功", avatarUrl);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ========== 管理员功能 ==========

    @GetMapping("/list")
    public Result<List<User>> listUsers(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "无权限操作");
        }
        List<User> users = userService.list();
        return Result.success(users);
    }

    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "无权限操作");
        }
        User user = userService.getById(id);
        return Result.success(user);
    }

    @PostMapping
    public Result<?> createUser(@RequestBody User user, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "无权限操作");
        }
        try {
            // 密码必须MD5加密存储，否则新创建的用户（主持人等）无法登录
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                user.setPassword(org.springframework.util.DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
            }
            userService.save(user);
            return Result.success("创建成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<?> updateUser(@PathVariable Long id, @RequestBody User user, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "无权限操作");
        }
        user.setId(id);
        // 如果更新时带了密码，需要MD5加密
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(org.springframework.util.DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        }
        userService.updateById(user);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "无权限操作");
        }
        userService.removeById(id);
        return Result.success("删除成功");
    }

    @PostMapping("/{id}/reset-password")
    public Result<?> resetPassword(@PathVariable Long id, @RequestParam String newPassword, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "无权限操作");
        }
        try {
            User user = userService.getById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }
            // 重置密码时，新密码需要MD5加密
            String encryptedNewPassword = org.springframework.util.DigestUtils.md5DigestAsHex(newPassword.getBytes());
            userService.updatePassword(id, user.getPassword(), encryptedNewPassword);
            return Result.success("密码重置成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/role")
    public Result<?> assignRole(@PathVariable Long id, @RequestParam String role, HttpServletRequest request) {
        String adminRole = (String) request.getAttribute("role");
        if (!"ADMIN".equals(adminRole)) {
            return Result.error(403, "无权限操作");
        }
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setRole(role);
        userService.updateById(user);
        return Result.success("角色分配成功");
    }
}
