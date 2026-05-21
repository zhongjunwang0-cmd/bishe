package com.english.learning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.english.learning.entity.Literature;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LiteratureMapper extends BaseMapper<Literature> {}
