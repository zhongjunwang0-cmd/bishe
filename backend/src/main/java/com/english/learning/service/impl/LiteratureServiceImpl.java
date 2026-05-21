package com.english.learning.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.english.learning.entity.Literature;
import com.english.learning.mapper.LiteratureMapper;
import com.english.learning.service.LiteratureService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LiteratureServiceImpl extends ServiceImpl<LiteratureMapper, Literature> implements LiteratureService {

    @Override
    @Cacheable(value = "literatureList")
    public List<Literature> listAllCached() {
        return this.list();
    }

    @Override
    @CacheEvict(value = "literatureList", allEntries = true)
    public boolean saveWithEvict(Literature literature) {
        return this.save(literature);
    }

    @Override
    @CacheEvict(value = "literatureList", allEntries = true)
    public boolean removeWithEvict(Long id) {
        return this.removeById(id);
    }
}
