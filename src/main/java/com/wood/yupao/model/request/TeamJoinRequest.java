package com.wood.yupao.model.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 加入队伍请求体
 */
@Data
public class TeamJoinRequest implements Serializable {

    /**
     * 队伍 id
     */
    private Long teamId;


    /**
     * 密码
     */
    private String password;
}
