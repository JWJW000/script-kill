package com.scriptkill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scriptkill.entity.User;
import com.scriptkill.dto.LoginDTO;
import com.scriptkill.dto.RegisterDTO;

public interface UserService extends IService<User> {
    String login(LoginDTO loginDTO);
    void register(RegisterDTO registerDTO);
    User getCurrentUser(Long userId);
    void updatePassword(Long userId, String oldPassword, String newPassword);
    void updateAvatar(Long userId, String avatar);
}
