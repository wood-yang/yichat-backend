create table user
(
    id           bigint auto_increment comment 'id'
        primary key,
    username     varchar(256)                       null comment '用户昵称',
    userAccount  varchar(256)                       null comment '账号',
    avatarUrl    varchar(1024)                      null comment '用户头像',
    gender       tinyint                            null comment '性别',
    userPassword varchar(512)                       not null comment '密码',
    phone        varchar(128)                       null comment '电话',
    email        varchar(512)                       null comment '邮箱',
    userRole     int      default 0                 not null comment '用户角色 0 - 普通用户 1 - 管理员',
    tags         json null comment '标签 json 列表',
    userStatus   int      default 0                 not null comment '状态 0 - 正常',
    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete     tinyint  default 0                 not null comment '是否删除'
)
    comment '用户';

create table team
(
    id          bigint auto_increment comment 'id'
        primary key,
    name        varchar(256)                       null comment '队伍名称',
    description varchar(1024)                      null comment '描述',
    maxNum      int      default 1                 not null comment '最大人数',
    userId      bigint                             null comment '队长 id',
    status      int      default 0                 null comment '队伍状态 0 - 公开，1 - 私有，2 - 加密',
    password    varchar(128)                       null comment '密码',
    createTime  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete    tinyint  default 0                 not null comment '是否删除',
    expireTime  datetime                           null comment '过期时间'
)
    comment '队伍';

create table user_team
(
    id         bigint auto_increment comment 'id'
        primary key,
    userId     bigint                             null comment '用户 id',
    teamId     bigint                             null comment '队伍 id',
    joinTime   datetime                           null comment '加入时间',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete   tinyint  default 0                 not null comment '是否删除'
)
    comment '用户-队伍关系';