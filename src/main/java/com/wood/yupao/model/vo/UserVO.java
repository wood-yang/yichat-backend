package com.wood.yupao.model.vo;


import lombok.Data;

/**
 * 用户包装类（脱敏）
 */
@Data
public class UserVO {
    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态 0 - 正常
     */
    private Integer userStatus;

    /**
     * 星球编号
     */
    private String planetCode;

    /**
     * 标签列表json
     */
    private String tags;
}
