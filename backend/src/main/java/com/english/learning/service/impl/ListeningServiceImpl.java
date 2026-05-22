package com.english.learning.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.english.learning.entity.Listening;
import com.english.learning.mapper.ListeningMapper;
import com.english.learning.service.ListeningService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListeningServiceImpl extends ServiceImpl<ListeningMapper, Listening> implements ListeningService {

    @Override
    @Cacheable(value = "listeningList")
    public List<Listening> listAllCached() {
        return this.list();
    }

    @Override
    @CacheEvict(value = "listeningList", allEntries = true)
    public boolean saveWithEvict(Listening listening) {
        return this.save(listening);
    }

    @Override
    @CacheEvict(value = "listeningList", allEntries = true)
    public boolean updateWithEvict(Listening listening) {
        return this.updateById(listening);
    }

    @Override
    @CacheEvict(value = "listeningList", allEntries = true)
    public boolean removeWithEvict(Long id) {
        return this.removeById(id);
    }
}
