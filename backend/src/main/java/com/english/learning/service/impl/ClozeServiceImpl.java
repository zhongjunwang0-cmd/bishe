package com.english.learning.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.english.learning.entity.Cloze;
import com.english.learning.mapper.ClozeMapper;
import com.english.learning.service.ClozeService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClozeServiceImpl extends ServiceImpl<ClozeMapper, Cloze> implements ClozeService {

    @Override
    @Cacheable(value = "clozeList")
    public List<Cloze> listAllCached() {
        return this.list();
    }

    @Override
    @CacheEvict(value = "clozeList", allEntries = true)
    public boolean saveWithEvict(Cloze cloze) {
        return this.save(cloze);
    }

    @Override
    @CacheEvict(value = "clozeList", allEntries = true)
    public boolean removeWithEvict(Long id) {
        return this.removeById(id);
    }
}
