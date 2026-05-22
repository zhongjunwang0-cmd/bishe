package com.english.learning.service;

public interface UploadFileService {
    boolean isLocalUpload(String url);

    void deleteLocalUploadIfUnreferenced(String audioUrl);
}
