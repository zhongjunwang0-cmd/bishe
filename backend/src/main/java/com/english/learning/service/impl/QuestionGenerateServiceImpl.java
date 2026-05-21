package com.english.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.english.learning.entity.Cloze;
import com.english.learning.entity.Listening;
import com.english.learning.entity.QuestionBank;
import com.english.learning.entity.Reading;
import com.english.learning.service.ClozeService;
import com.english.learning.service.ListeningService;
import com.english.learning.service.QuestionBankService;
import com.english.learning.service.QuestionGenerateService;
import com.english.learning.service.ReadingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class QuestionGenerateServiceImpl implements QuestionGenerateService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private QuestionBankService questionBankService;

    @Autowired
    private ReadingService readingService;

    @Autowired
    private ListeningService listeningService;

    @Autowired
    private ClozeService clozeService;

    @Override
    public Reading generateReading() {
        List<Long> usedBankIds = readingService.list(new QueryWrapper<Reading>()
                        .isNotNull("bank_id")
                        .select("bank_id"))
                .stream()
                .map(Reading::getBankId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        QuestionBank bank = questionBankService.pickRandomActiveExcluding("READING", usedBankIds);
        if (bank == null) {
            return null;
        }
        Reading reading = new Reading();
        reading.setBankId(bank.getId());
        reading.setTitle(bank.getTitle());
        reading.setContent(bank.getContent());
        reading.setDifficulty(bank.getDifficulty());
        reading.setQuestionsJson(bank.getQuestionsJson());
        reading.setAnswersJson(bank.getAnswersJson());
        reading.setScore(0);
        reading.setCreateTime(LocalDateTime.now());
        readingService.save(reading);
        return reading;
    }

    @Override
    public Listening generateListening() {
        QuestionBank bank = questionBankService.pickRandomActive("LISTENING");
        if (bank == null) {
            return null;
        }
        return createListeningFromBank(bank);
    }

    @Override
    public Listening publishListeningFromBank(Long bankId) {
        QuestionBank bank = questionBankService.getById(bankId);
        if (bank == null || !"LISTENING".equals(bank.getModuleType())) {
            return null;
        }
        return createListeningFromBank(bank);
    }

    private Listening createListeningFromBank(QuestionBank bank) {
        Listening listening = new Listening();
        listening.setBankId(bank.getId());
        listening.setTitle(bank.getTitle());
        listening.setCategory(bank.getCategory());
        listening.setDuration(bank.getDuration());
        listening.setAudioUrl(bank.getAudioUrl());
        listening.setContent(bank.getContent());
        listening.setQuestionsJson(bank.getQuestionsJson());
        listening.setAnswersJson(bank.getAnswersJson());
        listening.setScore(0);
        listening.setCreateTime(LocalDateTime.now());
        listeningService.save(listening);
        return listening;
    }

    @Override
    public Cloze generateCloze() {
        QuestionBank bank = questionBankService.pickRandomActive("CLOZE");
        if (bank == null) {
            return null;
        }
        Cloze cloze = new Cloze();
        cloze.setBankId(bank.getId());
        cloze.setTitle(bank.getTitle() + " #" + UUID.randomUUID().toString().substring(0, 5));
        cloze.setContent(bank.getContent());
        cloze.setQuestionsJson(bank.getQuestionsJson());
        cloze.setAnswersJson(bank.getAnswersJson());
        cloze.setBlanksCount(countBlanks(bank.getQuestionsJson()));
        cloze.setCompletionStatus("Pending");
        cloze.setScore(0);
        cloze.setCreateTime(LocalDateTime.now());
        clozeService.save(cloze);
        return cloze;
    }

    private int countBlanks(String questionsJson) {
        try {
            JsonNode node = MAPPER.readTree(questionsJson);
            return node.isArray() ? node.size() : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
