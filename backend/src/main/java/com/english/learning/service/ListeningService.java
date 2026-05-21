package com.english.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.english.learning.entity.Listening;
import java.util.List;

public interface ListeningService extends IService<Listening> {
    List<Listening> listAllCached();
    boolean saveWithEvict(Listening listening);
    boolean removeWithEvict(Long id);
}
