package com.wood.yichat.model.vo;

import com.wood.yichat.model.domain.User;
import lombok.Data;

@Data
public class LoginUserVO extends User {
    /**
     * 令牌
     */
    private String token;
}
