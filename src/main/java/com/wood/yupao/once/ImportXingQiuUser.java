package com.wood.yupao.once;

import com.alibaba.excel.EasyExcel;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 导入星球用户信息到数据库
 */
public class ImportXingQiuUser {
    public static void main(String[] args) {
//        String fileName = "D:\\develop\\idea_java\\user-center-backend\\src\\main\\resources\\testExcel.xlsx";
        String fileName = "D:\\develop\\idea_java\\user-center-backend\\src\\main\\resources\\prodExcel.xlsx";
        List<XingQiuTableUserInfo> userInfoList =
                EasyExcel.read(fileName).head(XingQiuTableUserInfo.class).sheet().doReadSync();
        System.out.println("总数 = " + userInfoList.size());
        Map<String, List<XingQiuTableUserInfo>> listMap =
                userInfoList.stream()
                        .filter(userInfo -> StringUtils.isNotEmpty(userInfo.getUsername()))
                        .collect(Collectors.groupingBy(XingQiuTableUserInfo::getUsername));
        for (Map.Entry<String, List<XingQiuTableUserInfo>> stringListEntry : listMap.entrySet()) {
            if (stringListEntry.getValue().size() > 1) {
                System.out.println("username = " + stringListEntry.getKey());
                System.out.println(1);
            }
        }

        System.out.println("不重复昵称数 = " + listMap.keySet().size());
    }
}
