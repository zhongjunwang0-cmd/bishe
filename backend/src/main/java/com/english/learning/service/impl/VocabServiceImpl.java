package com.english.learning.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.english.learning.entity.Vocab;
import com.english.learning.mapper.VocabMapper;
import com.english.learning.service.VocabService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VocabServiceImpl extends ServiceImpl<VocabMapper, Vocab> implements VocabService {

    @Override
    @Cacheable(value = "vocabList")
    public List<Vocab> listAllCached() {
        return this.list();
    }
}
