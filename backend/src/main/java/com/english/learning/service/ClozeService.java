package com.english.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.english.learning.entity.Cloze;
import java.util.List;

public interface ClozeService extends IService<Cloze> {
    List<Cloze> listAllCached();
    boolean saveWithEvict(Cloze cloze);
    boolean removeWithEvict(Long id);
}
