package com.wood.yichat.model.request;

import lombok.Data;

import java.io.Serializable;

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
