package com.wood.yupao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wood.yupao.common.ErrorCode;
import com.wood.yupao.exception.BusinessException;
import com.wood.yupao.model.domain.User;
import com.wood.yupao.service.UserService;
import com.wood.yupao.mapper.UserMapper;
import com.wood.yupao.utils.AlgorithmUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.wood.yupao.contant.UserConstant.ADMIN_ROLE;

/**
 * 用户服务实现类
 *
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "yupi";

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 4 || checkPassword.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return -1;
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            return -1;
        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        // 1. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 3. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            return -1;
        }
        return user.getId();
    }

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        if (userAccount.length() < 4) {
            return null;
        }
        if (userPassword.length() < 4) {
            return null;
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return null;
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            return null;
        }
        // 3. 用户脱敏
        User safetyUser = getSafetyUser(user);
        // 4. 记录用户的登录态
//        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
//        System.out.println("Save SessionId=" + request.getSession().getId());
        return safetyUser;
    }

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setTags(originUser.getTags());
        return safetyUser;
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public Boolean userLogout(HttpServletRequest request, HttpServletResponse response) {
        Boolean delete = false;
        // 移除登录态
        String token = request.getHeader("token");
        if(!"undefined".equals(token)){
            delete = redisTemplate.delete(token);
        }
        return delete;
    }

    /**
     * 根据标签搜索用户（SQL版）
     *
     * @param tagNameList 用户拥有的标签
     * @return
     */
    @Deprecated
    private List<User> searchUsersByTagsBySQL(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //拼接 and 查询
        //like ’%java%‘ and like ’%c++%‘
        for (String tagName : tagNameList) {
            queryWrapper = queryWrapper.like("tags", tagName);
        }
        //1.先查询所有用户
        List<User> userList = userMapper.selectList(queryWrapper);
        //2.在内存中判断是否包含要求的标签
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }

    /**
     * 根据标签搜索用户(内存过滤)
     *
     * @param tagNameList 用户拥有的标签
     * @return
     */
    @Override
    public List<User> searchUsersByTags(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //1.先查询所有用户
        List<User> userList = userMapper.selectList(queryWrapper);
        //2.在内存中判断是否包含要求的标签
        Gson gson = new Gson();
        //想要并发执行就将 steam() 改为 parallelStream()
        return userList.stream().filter(user -> {
            String tagsStr = user.getTags();
            if (StringUtils.isBlank(tagsStr)) {
                return false;
            }
            Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {}.getType());
            //ofNullable()判断是否为空 orElse(如果不为null就返回原对象 为null就返回参数里指定的对象)
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            for (String tagName : tagNameList) {
                if (!tempTagNameSet.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }

    /**
     * 修改用户信息
     *
     * @param user
     * @param loginUser
     * @return
     */
    @Override
    public int updateUser(User user, User loginUser) {
        Long userId = user.getId();
        if (userId <= 0 || user.isNull()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 仅管理员和自己可修改
        // 如果是管理员，允许更新任意用户
        // 如果不是管理员，只允许更新自己的信息
        if (!isAdmin(loginUser) && !userId.equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        int result = userMapper.updateById(user);
        User newUser = userMapper.selectById(userId);


        return result;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String token = request.getHeader("Token");
        String userJson = (String) redisTemplate.opsForValue().get(token);
        if (userJson != null && !userJson.equals("undefined")){
            Gson gson = new Gson();
            User user = gson.fromJson(userJson, User.class);
            return user;
        }
//        Cookie[] cookies = request.getCookies();
//        if(cookies != null){
//            for (Cookie cookie : cookies) {
//                if(cookie.getName().equals("token")){
//                    String value = cookie.getValue();
//                    String userJson = tokenService.get(value);
//                    if (userJson != null){
//                        Gson gson = new Gson();
//                        User user = gson.fromJson(userJson, User.class);
//                        return user;
//                    }
//                }
//            }
//        }
        throw new BusinessException(ErrorCode.NOT_LOGIN);
//        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
//        if (userObj == null) {
//            throw new BusinessException(ErrorCode.NOT_LOGIN);
//        }
//        return (User) userObj;
    }

    /**
     * 判断是否为管理员
     *
     * @param request
     * @return
     */
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        User loginUser = this.getLoginUser(request);
        return loginUser != null && loginUser.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 判断是否为管理员
     *
     * @param loginUser
     * @return
     */
    public boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getUserRole() == ADMIN_ROLE;
    }

    @Override
    public List<User> matchUsers(int num, User loginUser) {
        if (num <= 0 || num > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagsList = gson.fromJson(tags, new TypeToken<List<String>>() {}.getType());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "tags");
        queryWrapper.isNotNull("tags");
        List<User> userList = this.list(queryWrapper);

        PriorityQueue<User> userPriorityQueue = new PriorityQueue<>(num + 2, (o1, o2) -> {
            List<String> userTagList1 = gson.fromJson(o1.getTags(), new TypeToken<List<String>>() {
            }.getType());
            List<String> userTagList2 = gson.fromJson(o2.getTags(), new TypeToken<List<String>>() {
            }.getType());
            return AlgorithmUtils.minDistance(tagsList, userTagList2) - AlgorithmUtils.minDistance(tagsList, userTagList1);
        });
        for (User user : userList) {
            userPriorityQueue.add(user);
            if (userPriorityQueue.size() > num + 1) {
                userPriorityQueue.poll();
            }
        }
        List<Long> matchUserIdList = userPriorityQueue.stream().map(User::getId).collect(Collectors.toList());
        matchUserIdList.sort((o1, o2) -> (int) (o1 - o2));
        List<User> matchUserList = new ArrayList<>();
        for (Long userId : matchUserIdList) {
            if (loginUser.getId().equals(userId)) {
                continue;
            }
            User matchUser = this.getById(userId);
            matchUserList.add(matchUser);
        }
        // 如果匹配列表中不包含自己，就多一个推荐人，需要移除
        if (matchUserList.size() > num) {
            matchUserList.remove(matchUserList.size() - 1);
        }
        return matchUserList;

        // 用户列表的下标 => 相似度
//        SortedMap<Integer, Integer> indexDistanceMap = new TreeMap<>();
//        for (int i = 0; i < userList.size(); i++) {
//            User user = userList.get(i);
//            String userTags = user.getTags();
//            if (StringUtils.isBlank(userTags) || loginUser.getId().equals(user.getId())) {
//                continue;
//            }
//            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {}.getType());
//            // 计算分数
//            int minDistance = AlgorithmUtils.minDistance(tagsList, userTagList);
//
//            indexDistanceMap.put(i, minDistance);
//        }
//        List<Integer> maxDistanceIndexList = indexDistanceMap.keySet().stream().limit(num).collect(Collectors.toList());
//        List<User> matchUserList = maxDistanceIndexList.stream().map(index -> getSafetyUser(userList.get(index))).collect(Collectors.toList());

//        return matchUserList;
    }
}