package com.english.learning.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses legacy cloze content that embeds options and answers in plain text:
 * passage with (1)_____ blanks, then "选项：A.word B.word ..." and "答案：1-A 2-B ..."
 */
public final class ClozeLegacyParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Pattern BLANK_PATTERN = Pattern.compile("\\(\\d+\\)_____");
    private static final Pattern OPTION_ITEM = Pattern.compile("([A-Z])\\.([^\\s]+)");
    private static final Pattern ANSWER_ITEM = Pattern.compile("(\\d+)-([A-Z])");

    private ClozeLegacyParser() {
    }

    public static class Parsed {
        private String content;
        private String questionsJson;
        private String answersJson;
        private int blankCount;

        public String getContent() {
            return content;
        }

        public String getQuestionsJson() {
            return questionsJson;
        }

        public String getAnswersJson() {
            return answersJson;
        }

        public int getBlankCount() {
            return blankCount;
        }
    }

    public static Parsed parse(String rawContent) {
        if (rawContent == null || rawContent.isBlank()) {
            return null;
        }

        int optionsIdx = rawContent.indexOf("选项");
        int answersIdx = rawContent.indexOf("答案");
        if (optionsIdx < 0 || answersIdx <= optionsIdx) {
            return null;
        }

        String passage = rawContent.substring(0, optionsIdx).trim();
        String optionsText = rawContent.substring(optionsIdx, answersIdx).replaceFirst("^选项[：:]\\s*", "").trim();
        String answersPart = rawContent.substring(answersIdx).replaceFirst("^答案[：:]\\s*", "").trim();

        Map<String, String> letterToWord = new LinkedHashMap<>();
        Matcher optionMatcher = OPTION_ITEM.matcher(optionsText);
        while (optionMatcher.find()) {
            letterToWord.put(optionMatcher.group(1), optionMatcher.group(2));
        }
        if (letterToWord.isEmpty()) {
            return null;
        }

        int blankCount = 0;
        Matcher blankMatcher = BLANK_PATTERN.matcher(passage);
        while (blankMatcher.find()) {
            blankCount++;
        }
        if (blankCount == 0) {
            return null;
        }

        Map<Integer, String> blankToLetter = new LinkedHashMap<>();
        Matcher answerMatcher = ANSWER_ITEM.matcher(answersPart);
        while (answerMatcher.find()) {
            blankToLetter.put(Integer.parseInt(answerMatcher.group(1)), answerMatcher.group(2));
        }
        if (blankToLetter.isEmpty()) {
            return null;
        }

        List<String> allOptions = new ArrayList<>(letterToWord.values());
        List<Map<String, Object>> questions = new ArrayList<>();
        List<Map<String, Object>> answers = new ArrayList<>();

        for (int i = 1; i <= blankCount; i++) {
            Map<String, Object> question = new LinkedHashMap<>();
            question.put("blankIndex", i);
            question.put("options", allOptions);
            questions.add(question);

            String letter = blankToLetter.get(i);
            String correct = letter == null ? "" : letterToWord.getOrDefault(letter, "");
            Map<String, Object> answer = new LinkedHashMap<>();
            answer.put("correct", correct);
            answer.put("explanation", correct.isEmpty() ? "" : "正确答案：" + correct);
            answers.add(answer);
        }

        try {
            Parsed parsed = new Parsed();
            parsed.content = passage;
            parsed.questionsJson = MAPPER.writeValueAsString(questions);
            parsed.answersJson = MAPPER.writeValueAsString(answers);
            parsed.blankCount = blankCount;
            return parsed;
        } catch (Exception e) {
            return null;
        }
    }
}
