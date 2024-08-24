//package com.wood.yupao.once;
//
//import com.wood.yupao.mapper.UserMapper;
//import com.wood.yupao.model.domain.User;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StopWatch;
//
//import javax.annotation.Resource;
//
//@Component
//public class InsertUsers {
//
//    @Resource
//    UserMapper userMapper;
//
//
//    /**
//     * 模拟插入用户
//     */
////    @Scheduled(initialDelay = 5000, fixedRate = Long.MAX_VALUE)
//    public void doInsertUsers() {
//        StopWatch stopWatch = new StopWatch();
//        stopWatch.start();
//        final int INSERT_NUM = 1000;
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
//
//            userMapper.insert(user);
//        }
//        stopWatch.stop();
//        System.out.println(stopWatch.getTotalTimeMillis());
//    }
//}
