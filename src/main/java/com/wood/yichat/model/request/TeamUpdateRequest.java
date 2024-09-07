package com.wood.yichat.model.request;

import lombok.Data;

import java.util.Date;

/**
 * 队伍更新请求体
 */
@Data
public class TeamUpdateRequest {

    /**
     * id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 队伍状态 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

    /**
     * 密码
     */
    private String password;
}
