package com.english.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.english.learning.entity.Oral;
import java.util.List;

public interface OralService extends IService<Oral> {
    List<Oral> listAllCached();
    boolean saveWithEvict(Oral oral);
    boolean removeWithEvict(Long id);
}
