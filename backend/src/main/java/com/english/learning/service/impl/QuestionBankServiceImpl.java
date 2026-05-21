package com.english.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.english.learning.entity.QuestionBank;
import com.english.learning.mapper.QuestionBankMapper;
import com.english.learning.service.QuestionBankService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class QuestionBankServiceImpl extends ServiceImpl<QuestionBankMapper, QuestionBank> implements QuestionBankService {

    @Override
    public List<QuestionBank> listByModule(String moduleType) {
        QueryWrapper<QuestionBank> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        if (moduleType != null && !moduleType.isBlank()) {
            wrapper.eq("module_type", moduleType);
        }
        return this.list(wrapper);
    }

    @Override
    public QuestionBank pickRandomActive(String moduleType) {
        return pickRandomActiveExcluding(moduleType, Collections.emptyList());
    }

    @Override
    public QuestionBank pickRandomActiveExcluding(String moduleType, Collection<Long> excludeBankIds) {
        QueryWrapper<QuestionBank> wrapper = new QueryWrapper<QuestionBank>()
                .eq("module_type", moduleType)
                .eq("status", "Active");
        if (excludeBankIds != null && !excludeBankIds.isEmpty()) {
            wrapper.notIn("id", excludeBankIds);
        }
        List<QuestionBank> active = this.list(wrapper);
        if (active.isEmpty()) {
            return null;
        }
        Collections.shuffle(active);
        return active.get(ThreadLocalRandom.current().nextInt(active.size()));
    }
}
