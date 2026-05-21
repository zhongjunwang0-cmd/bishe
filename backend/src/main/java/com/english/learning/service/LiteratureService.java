package com.english.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.english.learning.entity.Literature;
import java.util.List;

public interface LiteratureService extends IService<Literature> {
    List<Literature> listAllCached();
    boolean saveWithEvict(Literature literature);
    boolean removeWithEvict(Long id);
}
