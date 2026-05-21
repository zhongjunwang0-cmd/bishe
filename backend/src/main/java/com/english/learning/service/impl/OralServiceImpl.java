package com.english.learning.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.english.learning.entity.Oral;
import com.english.learning.mapper.OralMapper;
import com.english.learning.service.OralService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OralServiceImpl extends ServiceImpl<OralMapper, Oral> implements OralService {

    @Override
    @Cacheable(value = "oralList")
    public List<Oral> listAllCached() {
        return this.list();
    }

    @Override
    @CacheEvict(value = "oralList", allEntries = true)
    public boolean saveWithEvict(Oral oral) {
        return this.save(oral);
    }

    @Override
    @CacheEvict(value = "oralList", allEntries = true)
    public boolean removeWithEvict(Long id) {
        return this.removeById(id);
    }
}
