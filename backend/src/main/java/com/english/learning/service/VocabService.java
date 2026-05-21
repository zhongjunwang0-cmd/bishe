package com.english.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.english.learning.entity.Vocab;
import java.util.List;

public interface VocabService extends IService<Vocab> {
    List<Vocab> listAllCached();
}
