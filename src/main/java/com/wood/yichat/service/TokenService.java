package com.wood.yichat.service;

import com.wood.yichat.model.domain.User;

public interface TokenService {
    public String generateToken(Long userId);

    //把token存到redis中，如果是电脑登录，设置过期时间
    public void save(String token, User user);
    //获取token
    public String get(String token);
}
