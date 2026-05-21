package com.english.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.english.learning.dto.UserVocabVO;
import com.english.learning.entity.UserVocab;

import java.util.List;
import java.util.Map;

public interface UserVocabService extends IService<UserVocab> {

    List<UserVocabVO> listUserBook(Long userId);

    List<UserVocabVO> listDueReview(Long userId);

    Map<String, Object> getStats(Long userId);

    UserVocabVO addToBook(Long userId, Long vocabId);

    void removeFromBook(Long userId, Long vocabId);

    UserVocabVO submitReview(Long userId, Long vocabId, boolean remembered);

    boolean isInBook(Long userId, Long vocabId);
}
