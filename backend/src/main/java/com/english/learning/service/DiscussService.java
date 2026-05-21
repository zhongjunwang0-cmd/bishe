package com.english.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.english.learning.entity.Discuss;

import java.util.List;

public interface DiscussService extends IService<Discuss> {
    List<Discuss> listAllCached();
}
