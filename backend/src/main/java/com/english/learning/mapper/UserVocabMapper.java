package com.english.learning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.english.learning.entity.UserVocab;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserVocabMapper extends BaseMapper<UserVocab> {
}
