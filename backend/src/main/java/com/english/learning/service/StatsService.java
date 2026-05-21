package com.english.learning.service;

import java.util.Map;

public interface StatsService {

    Map<String, Object> getStats(Long userId, boolean global);
}
