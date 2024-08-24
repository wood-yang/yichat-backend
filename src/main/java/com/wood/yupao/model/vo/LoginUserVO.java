package com.wood.yupao.model.vo;

import com.wood.yupao.model.domain.User;
import lombok.Data;

@Data
public class LoginUserVO extends User {
    /**
     * 令牌
     */
    private String token;
}
