package com.scriptkill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scriptkill.common.Constants;
import com.scriptkill.common.JwtUtil;
import com.scriptkill.dto.LoginDTO;
import com.scriptkill.dto.RegisterDTO;
import com.scriptkill.entity.User;
import com.scriptkill.mapper.UserMapper;
import com.scriptkill.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public String login(LoginDTO loginDTO) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", loginDTO.getUsername());
        User user = this.getOne(wrapper);
        
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        
        String encryptedPassword = DigestUtils.md5DigestAsHex(loginDTO.getPassword().getBytes());
        if (!user.getPassword().equals(encryptedPassword)) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        return jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
    }

    @Override
    public void register(RegisterDTO registerDTO) {
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new RuntimeException("两次密码输入不一致");
        }
        
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", registerDTO.getUsername());
        if (this.count(wrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }
        
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(DigestUtils.md5DigestAsHex(registerDTO.getPassword().getBytes()));
        user.setRealName(registerDTO.getRealName());
        user.setPhone(registerDTO.getPhone());
        user.setRole(Constants.ROLE_PLAYER);
        user.setStatus(1);
        
        this.save(user);
    }

    @Override
    public User getCurrentUser(Long userId) {
        return this.getById(userId);
    }

    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = this.getById(userId);
        String encryptedOldPassword = DigestUtils.md5DigestAsHex(oldPassword.getBytes());
        if (!user.getPassword().equals(encryptedOldPassword)) {
            throw new RuntimeException("原密码错误");
        }
        user.setPassword(DigestUtils.md5DigestAsHex(newPassword.getBytes()));
        this.updateById(user);
    }

    @Override
    public void updateAvatar(Long userId, String avatar) {
        User user = this.getById(userId);
        user.setAvatar(avatar);
        this.updateById(user);
    }
}
