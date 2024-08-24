package com.wood.yupao.once;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 星球表格用户信息
 */
@Data
public class XingQiuTableUserInfo {
    /**
     * id
     */
    @ExcelProperty("成员编号")
    private int planetCode;

    /**
     * 用户昵称
     */
    @ExcelProperty("用户昵称")
    private String username;
}