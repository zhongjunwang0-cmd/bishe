package com.english.learning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.english.learning.entity.Vocab;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VocabMapper extends BaseMapper<Vocab> {}
