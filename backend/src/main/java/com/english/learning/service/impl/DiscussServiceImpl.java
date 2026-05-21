package com.english.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.english.learning.entity.Discuss;
import com.english.learning.mapper.DiscussMapper;
import com.english.learning.service.DiscussService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiscussServiceImpl extends ServiceImpl<DiscussMapper, Discuss> implements DiscussService {

    @Override
    @Cacheable(value = "discussionList")
    public List<Discuss> listAllCached() {
        QueryWrapper<Discuss> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        return this.list(queryWrapper);
    }
}
