//package com.wood.yupao.service;
//
//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//import com.wood.yupao.mapper.UserMapper;
//import com.wood.yupao.model.domain.User;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.util.StopWatch;
//
//import javax.annotation.Resource;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//import java.util.concurrent.*;
//
//@SpringBootTest
//public class InsertUsersTest {
//
//    @Resource
//    UserService userService;
//    @Resource
//    UserMapper userMapper;
//
//
//    // CPU 密集型：分配的核心线程数 = CPU - 1
//    // IO 密集型：分配的核心线程数可以大于 CPU 核数
//    private ExecutorService executorService = new ThreadPoolExecutor(60, 1000, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));
//
//    /**
//     * 模拟插入用户
//     */
//    @Test
//    public void doInsertUsers() {
//        StopWatch stopWatch = new StopWatch();
//        stopWatch.start();
//        List<User> userList = new ArrayList<User>();
//        final int INSERT_NUM = 100000;
//        for (int i = 0; i < INSERT_NUM; i++) {
//            User user = new User();
//            user.setUsername("假用户");
//            user.setUserAccount("fakeGod");
//            user.setAvatarUrl("https://ts2.cn.mm.bing.net/th?id=OIP-C.PwHLbZIbsGxF2dbe0zOc_QHaHa&w=250&h=250&c=8&rs=1&qlt=90&o=6&dpr=1.5&pid=3.1&rm=2");
//            user.setGender(0);
//            user.setUserPassword("12345678");
//            user.setPhone("32132");
//            user.setEmail("544891");
//            user.setUserStatus(0);
//            user.setUserRole(0);
//            user.setPlanetCode("2130");
//            user.setTags("[]");
//            userList.add(user);
//        }
//        userService.saveBatch(userList, 500);
//        stopWatch.stop();
//        //1000: 10595
//        //10000: 10672
//        //50000: 10852
//        System.out.println(stopWatch.getTotalTimeMillis());
//    }
//
//    /**
//     * 并发插入用户
//     */
//    @Test
//    public void doConcurrencyInsertUsers() {
//        StopWatch stopWatch = new StopWatch();
//        stopWatch.start();
//
//        final int INSERT_NUM = 1000;
//        // 分十组
//        List<CompletableFuture<Void>> futureList = new ArrayList<>();
//        for (int i = 0; i < 100; i++) {
//            List<User> userList = Collections.synchronizedList(new ArrayList<>());
//            List<String> language = Arrays.asList("\"java\"", "\"c++\"", "\"python\"");
//            List<String> gender = Arrays.asList("\"男\"", "\"女\"");
//            List<String> grade = Arrays.asList("\"大一\"", "\"大二\"", "\"大三\"", "\"大四\"");
//            List<String> hobby = Arrays.asList("\"swim\"", "\"sing\"", "\"rap\"");
//            List<String> emotion = Arrays.asList("\"happy\"", "\"emo\"", "\"angry\"");
//            List<List<String>> tagList = Arrays.asList(language, gender, grade, hobby, emotion);
//
//            for (int j = 0; j < INSERT_NUM; j++) {
//                User user = new User();
//                user.setUsername("假用户");
//                user.setUserAccount("fakeGod");
//                user.setAvatarUrl("https://ts2.cn.mm.bing.net/th?id=OIP-C.PwHLbZIbsGxF2dbe0zOc_QHaHa&w=250&h=250&c=8&rs=1&qlt=90&o=6&dpr=1.5&pid=3.1&rm=2");
//                user.setGender(0);
//                user.setUserPassword("12345678");
//                user.setPhone("32132");
//                user.setEmail("544891");
//                user.setUserStatus(0);
//                user.setUserRole(0);
//                user.setPlanetCode("2130");
//                StringBuffer tags = new StringBuffer();
//                tags.append('[');
//                for (int k = 0; k < 5; k++) {
//                    int size = tagList.get(k).size();
//                    int index = (int) (Math.random() * size);
//                    tags.append(tagList.get(k).get(index));
//                    if (k != 4) {
//                        tags.append(", ");
//                    }
//                }
//                tags.append(']');
//                user.setTags(tags.toString());
//                userList.add(user);
//            }
//            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//                System.out.println("Test thread name: " + Thread.currentThread().getName());
//                userService.saveBatch(userList, 10000);
//            }, executorService);
//            futureList.add(future);
//        }
//        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
//        stopWatch.stop();
//        //10: 2723
//        //20: 2202
//        //100: 2205
//        System.out.println(stopWatch.getTotalTimeMillis());
//    }
//
//
//    /**
//     * 删除用户
//     */
//    @Test
//    public void doDeleteUsers() {
//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        queryWrapper.ge("id", 7);
//        userMapper.delete(queryWrapper);
//    }
//}
