//package com.wood.yupao.service;
//
//import cn.hutool.http.HttpUtil;
//import com.google.gson.Gson;
//import com.wood.yupao.YuPaoApplication;
//import com.wood.yupao.model.domain.User;
//import org.junit.jupiter.api.Test;
//import org.junit.runner.RunWith;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.util.StopWatch;
//
//import javax.annotation.Resource;
//import java.util.*;
//import java.util.concurrent.*;
//
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = YuPaoApplication.class)
//public class UserServiceTest {
//
//    @Resource
//    UserService userService;
//
//    // CPU 密集型：分配的核心线程数 = CPU - 1
//    // IO 密集型：分配的核心线程数可以大于 CPU 核数
//    private final ExecutorService executorService = new ThreadPoolExecutor(60, 1000, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));
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
//        for (int i = 0; i < 1; i++) {
//            List<User> userList = Collections.synchronizedList(new ArrayList<>());
//            List<String> language = Arrays.asList("\"java\"", "\"c++\"", "\"python\"", "\"php\"");
//            List<String> gender = Arrays.asList("\"男\"", "\"女\"");
//            List<String> grade = Arrays.asList("\"大一\"", "\"大二\"", "\"大三\"", "\"大四\"");
//            List<String> hobby = Arrays.asList("\"swim\"", "\"sing\"", "\"rap\"");
//            List<String> emotion = Arrays.asList("\"happy\"", "\"emo\"", "\"angry\"", "\"sad\"");
//            List<List<String>> tagList = Arrays.asList(language, gender, grade, hobby, emotion);
//
//            for (int j = 0; j < INSERT_NUM; j++) {
//                User user = new User();
//                String name = HttpUtil.get("https://api.mir6.com/api/sjname");
//                user.setUsername(name);
//                Random random = new Random();
//                int number = random.nextInt(88888888) + 11111111;
//                user.setUserAccount(String.valueOf(number));
//                String avatarUrl = HttpUtil.get("https://api.vvhan.com/api/avatar/rand?type=json");
//                Gson gson = new Gson();
//                HashMap map = gson.fromJson(avatarUrl, HashMap.class);
//                user.setAvatarUrl((String) map.get("url"));
//                number = random.nextInt(2);
//                user.setGender(number);
//                user.setUserPassword("fed63ea6b39df48c361f4eecac3e5ddf");
//                Long phoneNumber = 10000000000L;
//                phoneNumber += random.nextInt(10000) * 10000 + random.nextInt(10000);
//                user.setPhone(String.valueOf(phoneNumber));
//                Long email = random.nextInt(100000) * 100000L + random.nextInt(100000);
//                user.setEmail(email + "@qq.com");
//                user.setUserStatus(0);
//                user.setUserRole(0);
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
//}