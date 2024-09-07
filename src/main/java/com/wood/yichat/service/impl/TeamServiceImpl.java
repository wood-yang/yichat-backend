package com.wood.yichat.service.impl;

import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wood.yichat.common.ErrorCode;
import com.wood.yichat.exception.BusinessException;
import com.wood.yichat.mapper.TeamMapper;
import com.wood.yichat.model.domain.Team;
import com.wood.yichat.model.domain.User;
import com.wood.yichat.model.domain.UserTeam;
import com.wood.yichat.model.dto.TeamQuery;
import com.wood.yichat.model.enums.TeamStatusEnum;
import com.wood.yichat.model.request.TeamDeleteRequest;
import com.wood.yichat.model.request.TeamJoinRequest;
import com.wood.yichat.model.request.TeamQuitRequest;
import com.wood.yichat.model.request.TeamUpdateRequest;
import com.wood.yichat.model.vo.TeamUserVO;
import com.wood.yichat.model.vo.UserVO;
import com.wood.yichat.service.TeamService;
import com.wood.yichat.service.UserService;
import com.wood.yichat.service.UserTeamService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author 24420
 * @description 针对表【team(队伍)】的数据库操作Service实现
 * @createDate 2024-08-16 16:00:01
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {
        // 1. 是否登录，未登录不允许创建
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        final long userId = loginUser.getId();
        // 2. 请求参数是否为空
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 3. 校验信息
        //   1. 队伍人数 >= 1 且 <= 20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum < 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不符合要求");
        }
        //   2. 队伍标题 <= 20
        String name = team.getName();
        if (StringUtils.isBlank(name) || name.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍标题不符合要求");
        }
        //   3. 描述 <= 512
        String description = team.getDescription();
        if (StringUtils.isNotBlank(name) && description.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述不符合要求");
        }
        //   4. 是否公开（int） 不传默认为 0 （公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        if (status < 0 || status > 2) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不符合要求");
        }
        //   5. 如果状态为 2（加密），一定要有密码，且密码 <= 32
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        String password = Optional.ofNullable(team.getPassword()).orElse("");
        if (StringUtils.isBlank(password) && teamStatusEnum.equals(TeamStatusEnum.ENCRYPT) || password.length() > 32) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍密码不符合要求");
        }
        //   6. 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if (expireTime.before(new Date())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "超时时间 > 当前时间");
        }
        //   7. 校验用户最多创建 5 个队伍
        // todo 有 bug，可能同时创建100个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        long hasTeamNum = count(queryWrapper);
        if (hasTeamNum >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户最多创建 5 个队伍");
        }
        //4. 插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        Long teamId = team.getId();
        if (!result || teamId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        //5. 插入 用户 => 队伍关系 到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());

        result = userTeamService.save(userTeam);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        return teamId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin) {
//        // 1. 是否登录，未登录不允许查找
//        if (loginUser == null) {
//            throw new BusinessException(ErrorCode.NOT_LOGIN);
//        }
//        final long userId = loginUser.getId();
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        // 2. 创建查询条件
        if (teamQuery != null) {
            Long id = teamQuery.getId();
            if (id != null && id > 0) {
                teamQueryWrapper.eq("id", id);
            }
            List<Long> idList = teamQuery.getIdList();
            if (idList != null) {
                teamQueryWrapper.in("id", idList);
            }
            Long userId = teamQuery.getUserId();
            if (userId != null && userId > 0) {
                teamQueryWrapper.eq("userId", userId);
            }
            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)) {
                teamQueryWrapper.like("name", name);
            }
            Integer maxNum = teamQuery.getMaxNum();
            if (maxNum != null && maxNum > 0) {
                teamQueryWrapper.eq("maxNum", maxNum);
            }
            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)) {
                teamQueryWrapper.like("description", description);
            }
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                teamQueryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
            }
            Integer status = teamQuery.getStatus();
            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
            if (!isAdmin && TeamStatusEnum.PRIVATE.equals(statusEnum)) {
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            if (statusEnum == null) {
                teamQueryWrapper.ne("status", TeamStatusEnum.PRIVATE.getValue());
            } else {
                teamQueryWrapper.eq("status", statusEnum.getValue());
            }
            teamQueryWrapper.and(qw -> qw.isNull("expireTime").or().ge("expireTime", new Date()));
        }
        // 3. 查询队伍信息
        List<Team> teamList = this.list(teamQueryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }

        // 4. 关联查询创建人信息
        // 第一种：自己写 SQL
        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        for (Team team : teamList) {
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            Long createUserId = team.getUserId();
            User user = userService.getById(createUserId);
            if (user != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                teamUserVO.setCreateUser(userVO);
            }
            teamUserVOList.add(teamUserVO);
        }

        return teamUserVOList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = teamUpdateRequest.getId();
        if (id == null || id < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team oldTeam = this.getById(id);
        if (oldTeam == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        // 只有管理员或者队伍的创建者可以修改
        if (!loginUser.getId().equals(oldTeam.getUserId()) && userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        Integer status = teamUpdateRequest.getStatus();
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (statusEnum.equals(TeamStatusEnum.ENCRYPT)) {
            if (StringUtils.isBlank(teamUpdateRequest.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密房间密码不能设置为空");
            }
        }
        Team updateTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest, updateTeam);

        return this.updateById(updateTeam);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamJoinRequest.getTeamId();
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        Date expireTime = team.getExpireTime();
        if (expireTime != null && expireTime.before(new Date())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }
        Integer status = team.getStatus();
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (TeamStatusEnum.PRIVATE.equals(statusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍是私密的");
        }
        if (TeamStatusEnum.ENCRYPT.equals(statusEnum)) {
            String password = teamJoinRequest.getPassword();
            if (StringUtils.isBlank(password)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已加密，请输入密码");
            }
            if (!password.equals(team.getPassword())) {
                throw new BusinessException(ErrorCode.NULL_ERROR, "密码不匹配");
            }
        }
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        // todo 1. 让 1 个用户同时只能发起加入 1 个队伍的请求(用户加入的队伍个数有上限)
        //  2.让 1 个队伍，同时只能收到 1 个用户的请求(队伍的人数有上限)
        // 只有一个线程能获取到锁
        RLock lock1 = redissonClient.getLock("yupao:user" + ":" + userId);
        RLock lock2 = redissonClient.getLock("yupao:team" + ":" + teamId);

        RLock lock = redissonClient.getMultiLock(lock1, lock2);
        try {
            while (true) {
                // 抢到锁并执行
                if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                    // 最多加入 5 队伍
                    QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("userId", userId);
                    long hasTeamNum = userTeamService.count(queryWrapper);
                    if (hasTeamNum >= 5) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多加入 5 个队伍");
                    }
                    // 不能加入已经加入的队伍
                    queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("teamId", teamId);
                    queryWrapper.eq("userId", userId);
                    long hasUserJoinTeam = userTeamService.count(queryWrapper);
                    if (hasUserJoinTeam > 0) {
                        throw new BusinessException(ErrorCode.NULL_ERROR, "不能加入已经加入的队伍");
                    }
                    // 不能加入已满的队伍
                    queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("teamId", teamId);
                    long teamHasNum = userTeamService.count(queryWrapper);
                    if (teamHasNum >= team.getMaxNum()) {
                        throw new BusinessException(ErrorCode.NULL_ERROR, "队伍已满");
                    }
                    //插入关系
                    UserTeam userTeam = new UserTeam();
                    userTeam.setUserId(userId);
                    userTeam.setTeamId(teamId);
                    userTeam.setJoinTime(new Date());

                    return userTeamService.save(userTeam);
                }
            }
        } catch (InterruptedException e) {
            log.error("doPerCache error " + e);
            return false;
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(TeamDeleteRequest teamDeleteRequest, HttpServletRequest request) {
        if (teamDeleteRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamDeleteRequest.getId();
        if (teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        // 只有管理员或者队长可以删除队伍
        if (!userService.isAdmin(loginUser) && !userId.equals(team.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        return this.removeById(teamId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, HttpServletRequest request) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamQuitRequest.getTeamId();
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        // 退出队伍
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", teamId);
        queryWrapper.eq("userId", userId);
        boolean result = userTeamService.remove(queryWrapper);

        // 查询队伍中还有多少人
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", teamId);
        queryWrapper.orderByAsc("joinTime");
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        if (userTeamList == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userTeamList.isEmpty()) {
            this.removeById(teamId);
        } else {
            // 如果退出的为队长，移交队长给 队长退出后 第一个进入队伍的人
            if (userId.equals(team.getUserId())) {
                UserTeam userTeam = userTeamList.get(0);
                Long newUserId = userTeam.getUserId();
                team.setUserId(newUserId);
                this.updateById(team);
            }
        }
        return result;
    }
}




