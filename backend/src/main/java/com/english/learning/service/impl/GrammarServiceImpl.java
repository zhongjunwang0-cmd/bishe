package com.english.learning.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.english.learning.entity.Grammar;
import com.english.learning.mapper.GrammarMapper;
import com.english.learning.service.GrammarService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GrammarServiceImpl extends ServiceImpl<GrammarMapper, Grammar> implements GrammarService {

    @Override
    @Cacheable(value = "grammarList")
    public List<Grammar> listAllCached() {
        return this.list();
    }

    @Override
    @CacheEvict(value = "grammarList", allEntries = true)
    public boolean saveWithEvict(Grammar grammar) {
        return this.save(grammar);
    }

    @Override
    @CacheEvict(value = "grammarList", allEntries = true)
    public boolean removeWithEvict(Long id) {
        return this.removeById(id);
    }
}
