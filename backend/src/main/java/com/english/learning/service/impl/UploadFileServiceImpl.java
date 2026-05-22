package com.english.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.english.learning.entity.Listening;
import com.english.learning.entity.QuestionBank;
import com.english.learning.mapper.ListeningMapper;
import com.english.learning.mapper.QuestionBankMapper;
import com.english.learning.service.UploadFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class UploadFileServiceImpl implements UploadFileService {

    private static final Logger log = LoggerFactory.getLogger(UploadFileServiceImpl.class);

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/data/uploads/";
    public static final String UPLOAD_PREFIX = "/uploads/";

    @Autowired
    private ListeningMapper listeningMapper;

    @Autowired
    private QuestionBankMapper questionBankMapper;

    @Override
    public boolean isLocalUpload(String url) {
        return url != null && url.startsWith(UPLOAD_PREFIX);
    }

    @Override
    public void deleteLocalUploadIfUnreferenced(String audioUrl) {
        if (!isLocalUpload(audioUrl)) {
            return;
        }
        if (countReferences(audioUrl) > 0) {
            return;
        }

        File file = resolveUploadFile(audioUrl);
        if (file == null || !file.exists() || !file.isFile()) {
            return;
        }

        if (!file.delete()) {
            log.warn("Failed to delete unused upload file: {}", audioUrl);
        }
    }

    private long countReferences(String audioUrl) {
        long listeningCount = listeningMapper.selectCount(
                new QueryWrapper<Listening>().eq("audio_url", audioUrl));
        long bankCount = questionBankMapper.selectCount(
                new QueryWrapper<QuestionBank>().eq("audio_url", audioUrl));
        return listeningCount + bankCount;
    }

    private File resolveUploadFile(String audioUrl) {
        String filename = audioUrl.substring(UPLOAD_PREFIX.length());
        if (filename.isEmpty() || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return null;
        }

        File file = new File(UPLOAD_DIR, filename);
        try {
            String uploadDirPath = new File(UPLOAD_DIR).getCanonicalPath();
            String filePath = file.getCanonicalPath();
            if (!filePath.startsWith(uploadDirPath + File.separator) && !filePath.equals(uploadDirPath)) {
                return null;
            }
        } catch (IOException e) {
            return null;
        }
        return file;
    }
}
