package com.wood.yichat.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用分页请求参数
 */
@Data
public class PageRequest implements Serializable {

    /**
     * 页面大小
     */
    protected int pageSize = 10;

    /**
     * 第几页
     */
    protected int pageNum = 1;
}
