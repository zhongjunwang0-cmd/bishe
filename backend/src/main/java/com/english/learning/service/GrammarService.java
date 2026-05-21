package com.english.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.english.learning.entity.Grammar;
import java.util.List;

public interface GrammarService extends IService<Grammar> {
    List<Grammar> listAllCached();
    boolean saveWithEvict(Grammar grammar);
    boolean removeWithEvict(Long id);
}
