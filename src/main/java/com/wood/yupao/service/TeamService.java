package com.wood.yupao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wood.yupao.model.domain.Team;
import com.wood.yupao.model.domain.User;
import com.wood.yupao.model.dto.TeamQuery;
import com.wood.yupao.model.request.TeamDeleteRequest;
import com.wood.yupao.model.request.TeamJoinRequest;
import com.wood.yupao.model.request.TeamQuitRequest;
import com.wood.yupao.model.request.TeamUpdateRequest;
import com.wood.yupao.model.vo.TeamUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 24420
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2024-08-16 16:00:01
*/
public interface TeamService extends IService<Team> {
    /**
     * 创建队伍
     *
     * @param team
     * @param loginUser
     * @return
     */
    long addTeam(Team team, User loginUser);

    /**
     * 搜索队伍
     *
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin);

    /**
     * 更新队伍
     *
     * @param teamUpdateRequest
     * @param request
     * @return
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, HttpServletRequest request);

    /**
     * 加入队伍
     *
     * @param teamJoinRequest
     * @param request
     * @return
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, HttpServletRequest request);

    /**
     * 删除队伍
     *
     * @param teamDeleteRequest
     * @param request
     * @return
     */
    boolean deleteTeam(TeamDeleteRequest teamDeleteRequest, HttpServletRequest request);

    /**
     * 退出队伍
     *
     * @param teamQuitRequest
     * @param request
     * @return
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, HttpServletRequest request);
}
