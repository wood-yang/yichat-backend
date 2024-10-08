package com.wood.yichat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wood.yichat.mapper.UserTeamMapper;
import com.wood.yichat.model.domain.UserTeam;
import com.wood.yichat.service.UserTeamService;
import org.springframework.stereotype.Service;

/**
* @author 24420
* @description 针对表【user_team(用户-队伍关系)】的数据库操作Service实现
* @createDate 2024-08-16 16:03:55
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService {

}




