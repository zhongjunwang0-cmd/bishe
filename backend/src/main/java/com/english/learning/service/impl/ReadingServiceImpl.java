package com.english.learning.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.english.learning.entity.Reading;
import com.english.learning.mapper.ReadingMapper;
import com.english.learning.service.ReadingService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReadingServiceImpl extends ServiceImpl<ReadingMapper, Reading> implements ReadingService {

    @Override
    @Cacheable(value = "readingList")
    public List<Reading> listAllCached() {
        return this.list();
    }

    @Override
    @CacheEvict(value = "readingList", allEntries = true)
    public boolean saveWithEvict(Reading reading) {
        return this.save(reading);
    }

    @Override
    @CacheEvict(value = "readingList", allEntries = true)
    public boolean removeWithEvict(Long id) {
        return this.removeById(id);
    }
}
