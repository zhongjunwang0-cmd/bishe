package com.english.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.english.learning.entity.QuestionBank;

import java.util.Collection;
import java.util.List;

public interface QuestionBankService extends IService<QuestionBank> {
    List<QuestionBank> listByModule(String moduleType);

    QuestionBank pickRandomActive(String moduleType);

    QuestionBank pickRandomActiveExcluding(String moduleType, Collection<Long> excludeBankIds);
}
