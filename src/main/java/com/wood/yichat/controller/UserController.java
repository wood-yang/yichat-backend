package com.wood.yichat.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.wood.yichat.common.BaseResponse;
import com.wood.yichat.common.ErrorCode;
import com.wood.yichat.common.ResultUtils;
import com.wood.yichat.exception.BusinessException;
import com.wood.yichat.model.domain.User;
import com.wood.yichat.model.request.UserLoginRequest;
import com.wood.yichat.model.request.UserRegisterRequest;
import com.wood.yichat.model.vo.LoginUserVO;
import com.wood.yichat.service.TokenService;
import com.wood.yichat.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户接口
 *
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private TokenService tokenService;

    /**
     * 用户注册
     *
     * @param request
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest request) {
        // 校验
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = request.getUserAccount();
        String userPassword = request.getUserPassword();
        String checkPassword = request.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            return null;
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request, HttpServletResponse response) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        if (user == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "登陆失败");
        }
        String token = this.tokenService.generateToken(user.getId());
        //将token码保存到redis中
        this.tokenService.save(token, user);
//        Cookie cookie = new Cookie("token", token);
//        cookie.setMaxAge(60*60*24*30);//设置cookie有效期30天(这只会对手机有效，当然你也可以区别设置)
//        cookie.setPath("/");
//        cookie.setDomain("https://yichat-backend-119385-6-1328506132.sh.run.tcloudbase.com");
////        cookie.setDomain("localhost");
//        response.addCookie(cookie); //保存cookie.setAttribute("token",token);
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        loginUserVO.setToken(token);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request, HttpServletResponse response) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.userLogout(request, response);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注销失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 获取当前用户
     *
     * @param request
     * @return
     */
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId = loginUser.getId();
        // TODO 校验用户是否合法
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);
        if (safetyUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "获取用户失败");
        }
        return ResultUtils.success(safetyUser);
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "缺少管理员权限");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);
        if (userList == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "搜索失败");
        }
        List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "缺少管理员权限");
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        if (!b) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "删除失败");
        }
        return ResultUtils.success(true);
    }

    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.searchUsersByTags(tagNameList);
        return ResultUtils.success(userList);
    }

    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        // 校验参数是否为空
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取当前用户信息
        User loginUser = userService.getLoginUser(request);
        // 触发更新
        Integer result = userService.updateUser(user, loginUser);
        if (result == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String token = this.tokenService.generateToken(user.getId());
        //将token码保存到redis中
        this.tokenService.save(token, user);
        return ResultUtils.success(result);
    }

    /**
     * 推荐用户
     * @param request
     * @return
     */
    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageSize, long pageNum, HttpServletRequest request) {
        // 如果有缓存，直接读缓存
        User loginUser = userService.getLoginUser(request);
        String redisKey = String.format("yichat:user:recommend:%s", loginUser.getId());
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        String json = (String) valueOperations.get(redisKey);
        Gson gson = new Gson();
        Page userPage = gson.fromJson(json, Page.class);
        if (userPage != null) {
            return ResultUtils.success(userPage);
        }
        // 无缓存，查数据库
        Random random = new Random();
        HashSet<Long> idSet = new HashSet<>();
        while (idSet.size() != 100) {
            long number = random.nextInt(1000) + 1;
            idSet.add(number);
        }
        idSet.remove(loginUser.getId());
        List<Long> idList = new ArrayList<>(idSet);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", idList);
        userPage = userService.page(new Page<>(pageNum, pageSize), queryWrapper);
        if (userPage == null) {
            return ResultUtils.success(userPage);
        }
        // 把缓存写进去
        try {
            json = gson.toJson(userPage);
            valueOperations.set(redisKey, json, 30, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("redis set key error", e);
        }
        return ResultUtils.success(userPage);
    }

    /**
     * 获得最匹配的用户
     *
     * @param request
     * @return
     */
    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(int num, HttpServletRequest request) {
        if (num <= 0 || num > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);

        return ResultUtils.success(userService.matchUsers(num, loginUser));
    }
}
