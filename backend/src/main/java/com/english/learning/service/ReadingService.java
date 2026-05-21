package com.english.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.english.learning.entity.Reading;
import java.util.List;

public interface ReadingService extends IService<Reading> {
    List<Reading> listAllCached();
    boolean saveWithEvict(Reading reading);
    boolean removeWithEvict(Long id);
}
