package com.wood.yichat;

import com.wood.yichat.utils.AlgorithmUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

/**
 * 算法工具类测试
 */
@SpringBootTest(classes = AlgorithmUtilsTest.class)
public class AlgorithmUtilsTest {

    @Test
    void test() {
        List<String> tagList1 = Arrays.asList("Java", "大二", "男");
        List<String> tagList2 = Arrays.asList("Java", "大一", "男");

        int minDistance = AlgorithmUtils.minDistance(tagList1, tagList2);
        System.out.println(minDistance);
    }
}
