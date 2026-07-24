package com.smartparking.service.impl;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartparking.common.BusinessException;
import com.smartparking.dto.LoginDTO;
import com.smartparking.dto.RegisterDTO;
import com.smartparking.entity.User;
import com.smartparking.mapper.UserMapper;
import com.smartparking.service.UserService;
import com.smartparking.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public void register(RegisterDTO dto) {
        // 检查手机号是否已注册
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, dto.getPhone());
        if (this.count(wrapper) > 0) {
            throw new BusinessException("该手机号已注册");
        }

        User user = new User();
        user.setPhone(dto.getPhone());
        user.setPassword(SecureUtil.md5(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : "用户" + dto.getPhone().substring(7));
        user.setCreditScore(100); // 初始信用分
        user.setRole(0);
        this.save(user);
    }

    @Override
    public Map<String, Object> login(LoginDTO dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, dto.getPhone());
        User user = this.getOne(wrapper);
        if (user == null) {
            throw new BusinessException("手机号未注册");
        }
        if (!user.getPassword().equals(SecureUtil.md5(dto.getPassword()))) {
            throw new BusinessException("密码错误");
        }

        String token = jwtUtils.generateToken(user.getId(), user.getPhone());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("nickname", user.getNickname());
        result.put("phone", user.getPhone());
        result.put("creditScore", user.getCreditScore());
        return result;
    }

    @Override
    public User getCurrentUser(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(null); // 不返回密码
        return user;
    }
}
