package com.wood.yupao.model.request;

import lombok.Data;

/**
 * 用户退出队伍请求体
 */
@Data
public class TeamQuitRequest {

    /**
     * 队伍 id
     */
    private Long teamId;
}
