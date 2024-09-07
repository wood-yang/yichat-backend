package com.wood.yichat.service.impl;
import com.google.gson.Gson;
import com.wood.yichat.model.domain.User;
import com.wood.yichat.service.TokenService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * token 服务类
 */
@Service
public class TokenServiceImpl implements TokenService {
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    //生成token(格式为:设备-加密的用户名-时间-六位随机数)
    public String generateToken(Long userId) {
        StringBuilder token = new StringBuilder("token:");
        //加密的用户id
        token.append(DigestUtils.md5Hex(String.valueOf(userId)) + "-");
        //时间
        token.append(new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + "-");
        //六位随机字符串
        token.append(new Random().nextInt(999999 - 111111 + 1) + 111111 );
        System.out.println("token-->" + token.toString());
        return token.toString();
    }
 
    //把token存到redis中，如果是电脑登录，设置过期时间
    public void save(String token, User user) {
        Gson gson = new Gson();
        redisTemplate.opsForValue().set(token, gson.toJson(user));
    }
    //获取token
    public String get(String token) {
        return redisTemplate.opsForValue().get(token);
    }
}