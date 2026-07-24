package com.smartparking.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartparking.dto.LoginDTO;
import com.smartparking.dto.RegisterDTO;
import com.smartparking.entity.User;

import java.util.Map;

public interface UserService extends IService<User> {

    /** 注册 */
    void register(RegisterDTO dto);

    /** 登录，返回 token 和用户信息 */
    Map<String, Object> login(LoginDTO dto);

    /** 获取当前用户信息 */
    User getCurrentUser(Long userId);
}
