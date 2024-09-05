package com.wood.yupao.model.vo;

import com.wood.yupao.model.domain.UserTeam;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 队伍用户信息封装类
 */
@Data
public class TeamUserVO implements Serializable {
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 队伍头像 url
     */
    private String avatarUrl;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 队伍状态 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

    /**
     * 密码
     */
    private String password;

    /**
     * 入队用户列表
     */
    List<UserVO> userList;

    /**
     * 创建人用户信息
     */
    UserVO createUser;

    /**
     * 对于当前用户来说是否加入
     */
    private Boolean hasJoin = false;

    /**
     * 已加入的用户数
     */
    private Integer hasJoinNum;

    /**
     * 队伍创建时间
     */
    private Date createTime;
}
