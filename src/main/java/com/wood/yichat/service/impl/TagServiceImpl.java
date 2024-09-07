package com.wood.yichat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wood.yichat.mapper.TagMapper;
import com.wood.yichat.model.domain.Tag;
import com.wood.yichat.service.TagService;
import org.springframework.stereotype.Service;

/**
* @author 24420
* @description 针对表【tag(标签)】的数据库操作Service实现
* @createDate 2024-08-09 17:47:24
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService {

}




