package com.english.learning.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 离线英文写作批改与基础语法分析工具，供 AI 规则引擎使用。
 */
public final class EnglishWritingAnalyzer {

    private static final Pattern STANDALONE_I = Pattern.compile("\\bi\\b");
    private static final Map<String, String> COMMON_MISSPELLINGS = new LinkedHashMap<>();

    static {
        COMMON_MISSPELLINGS.put("doges", "dogs");
        COMMON_MISSPELLINGS.put("recieve", "receive");
        COMMON_MISSPELLINGS.put("occured", "occurred");
        COMMON_MISSPELLINGS.put("definately", "definitely");
        COMMON_MISSPELLINGS.put("seperate", "separate");
        COMMON_MISSPELLINGS.put("wierd", "weird");
        COMMON_MISSPELLINGS.put("alot", "a lot");
        COMMON_MISSPELLINGS.put("dont", "don't");
        COMMON_MISSPELLINGS.put("doesnt", "doesn't");
        COMMON_MISSPELLINGS.put("cant", "can't");
        COMMON_MISSPELLINGS.put("wont", "won't");
        COMMON_MISSPELLINGS.put("its a", "it's a");
        COMMON_MISSPELLINGS.put("your welcome", "you're welcome");
    }

    private EnglishWritingAnalyzer() {
    }

    public static boolean containsEnglishText(String text) {
        return text != null && text.matches(".*[a-zA-Z]{2,}.*");
    }

    public static boolean isCorrectionIntent(String queryLower) {
        if (queryLower == null || queryLower.isBlank()) {
            return false;
        }
        String[] keywords = {
                "批改", "修改", "纠正", "改正", "检查", "错误", "错在哪", "哪里错", "对不对", "对吗",
                "correct", "fix", "error", "mistake", "proofread", "review", "grammar check",
                "找出", "帮我", "帮忙", "改一下", "怎么改", "润色"
        };
        for (String kw : keywords) {
            if (queryLower.contains(kw)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isGrammarExplanationIntent(String queryLower) {
        if (queryLower == null || queryLower.isBlank()) {
            return false;
        }
        String[] keywords = {
                "语法", "grammar", "句型", "从句", "时态", "虚拟", "被动", "倒装", "冠词",
                "讲解", "解析", "什么是", "怎么用", "区别", "用法"
        };
        for (String kw : keywords) {
            if (queryLower.contains(kw)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> extractEnglishFragments(String text) {
        List<String> fragments = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return fragments;
        }

        String englishPart = text.replaceAll("[\\u4e00-\\u9fff\\u3000-\\u303f\\uff00-\\uffef]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (englishPart.isBlank()) {
            return fragments;
        }

        String[] sentences = englishPart.split("(?<=[.!?])\\s+");
        for (String sentence : sentences) {
            String trimmed = sentence.trim();
            if (trimmed.length() >= 3 && trimmed.matches(".*[a-zA-Z].*") && !fragments.contains(trimmed)) {
                fragments.add(trimmed);
            }
        }

        if (fragments.isEmpty()) {
            fragments.add(englishPart);
        }
        return fragments;
    }

    public static AnalysisResult analyze(String sentence) {
        AnalysisResult result = new AnalysisResult();
        result.original = sentence == null ? "" : sentence.trim();
        result.corrected = result.original;
        result.issues = new ArrayList<>();

        if (result.original.isBlank()) {
            return result;
        }

        checkSentenceCapitalization(result);
        checkStandaloneI(result);
        checkSubjectVerbAgreement(result);
        checkCommonMisspellings(result);
        checkArticleUsage(result);
        checkEndPunctuation(result);

        result.corrected = normalizeSpaces(result.corrected);
        return result;
    }

    public static String formatCorrectionResponse(String query, List<AnalysisResult> results) {
        StringBuilder sb = new StringBuilder();
        sb.append("【AI 写作批改】\n\n");

        if (results.size() > 1) {
            sb.append("共分析 ").append(results.size()).append(" 个句子\n\n");
            sb.append("修改后全文\n");
            for (int i = 0; i < results.size(); i++) {
                if (i > 0) {
                    sb.append(' ');
                }
                sb.append(results.get(i).corrected);
            }
            sb.append("\n\n");
            sb.append("────────────────────────\n\n");
        }

        for (int i = 0; i < results.size(); i++) {
            AnalysisResult r = results.get(i);
            if (results.size() > 1) {
                sb.append("第 ").append(i + 1).append(" 句\n\n");
            }

            sb.append("原句\n");
            sb.append("  ").append(r.original).append("\n\n");
            sb.append("修改后\n");
            sb.append("  ").append(r.corrected).append("\n\n");

            if (r.issues.isEmpty()) {
                sb.append("本句未发现明显语法或拼写错误。\n");
            } else {
                sb.append("语法讲解（").append(r.issues.size()).append(" 处）\n");
                for (int j = 0; j < r.issues.size(); j++) {
                    GrammarIssue issue = r.issues.get(j);
                    sb.append("\n");
                    sb.append(j + 1).append("）").append(issue.type).append("\n");
                    sb.append("   问题：").append(issue.problem).append("\n");
                    sb.append("   修改：").append(issue.correction).append("\n");
                    sb.append("   讲解：").append(issue.explanation).append("\n");
                }
            }

            if (i < results.size() - 1) {
                sb.append("\n────────────────────────\n\n");
            }
        }

        if (query != null && query.toLowerCase().contains("doge")) {
            sb.append("\n\n补充说明\n");
            sb.append("  若您指的是网络用语 Doge（柴犬梗），作为专有名词可大写为 Doge；")
              .append("若表示“狗”的复数，正确写法是 dogs。\n");
        }

        sb.append("\n\n继续批改请直接发送英文句子，或提问具体语法点。");
        return sb.toString();
    }

    public static String formatGrammarExplanation(String queryLower) {
        if (containsAny(queryLower, "现在完成", "present perfect", "have done")) {
            return grammarTopic("现在完成时 (Present Perfect)",
                    "结构：have/has + 过去分词",
                    "用法：① 过去动作对现在有影响（I have lost my keys.）；② 从过去持续到现在的状态（She has lived here for 5 years.）；③ 经历（Have you ever been to London?）",
                    "易错点：不能与明确的过去时间状语连用（× I have seen him yesterday. → ✓ I saw him yesterday.）");
        }
        if (containsAny(queryLower, "一般现在", "simple present", "第三人称单数")) {
            return grammarTopic("一般现在时 (Simple Present)",
                    "结构：主语 + 动词原形；第三人称单数加 -s/-es",
                    "用法：① 习惯/规律（I like dogs.）；② 客观事实（Water boils at 100°C.）；③ 时间表（The train leaves at 8.）",
                    "易错点：he/she/it 作主语时动词需加 -s（× He like dogs. → ✓ He likes dogs.）");
        }
        if (containsAny(queryLower, "定语从句", "relative clause", "which", "that 从句")) {
            return grammarTopic("定语从句 (Relative Clause)",
                    "结构：先行词 + 关系词（who/which/that/where/when）+ 从句",
                    "用法：修饰名词，补充说明“什么样的人/事物”",
                    "易错点：关系代词 that 不能用在介词后（× the book in that I read → ✓ the book in which I read）");
        }
        if (containsAny(queryLower, "被动", "passive")) {
            return grammarTopic("被动语态 (Passive Voice)",
                    "结构：be + 过去分词（+ by + 执行者）",
                    "用法：强调动作承受者或执行者不明确时（The report was written yesterday.）",
                    "易错点：不及物动词无被动（× The accident was happened. → ✓ The accident happened.）");
        }
        if (containsAny(queryLower, "虚拟", "subjunctive", "if i were")) {
            return grammarTopic("虚拟语气 (Subjunctive)",
                    "结构：If + 过去式/were, 主句 would/could/might + 动词原形",
                    "用法：与现在或过去事实相反的假设（If I were you, I would study harder.）",
                    "易错点：be 动词在虚拟语气中常用 were（If I were rich...）");
        }
        if (containsAny(queryLower, "冠词", "article", "a an the")) {
            return grammarTopic("冠词 (Articles)",
                    "a/an：泛指单数可数名词（an hour, a university）",
                    "the：特指双方已知或独一无二的事物（the sun, the book we discussed）",
                    "零冠词：复数泛指、不可数泛指、专有名词一般不加 the（× I like the dogs 表泛指时通常用 I like dogs.）");
        }
        return null;
    }

    private static String grammarTopic(String title, String... sections) {
        StringBuilder sb = new StringBuilder("【AI 语法讲解】\n");
        sb.append(title).append("\n\n");
        for (int i = 0; i < sections.length; i++) {
            sb.append(i + 1).append(". ").append(sections[i]).append("\n\n");
        }
        sb.append("您可以在「语法学习」模块查看更完整的专题内容，或发送具体句子让我帮您批改。");
        return sb.toString();
    }

    private static void checkSentenceCapitalization(AnalysisResult result) {
        String s = result.corrected;
        if (!s.isEmpty() && Character.isLowerCase(s.charAt(0))) {
            char first = Character.toUpperCase(s.charAt(0));
            result.corrected = first + s.substring(1);
            result.issues.add(new GrammarIssue(
                    "句首大小写",
                    "句首字母 \"" + s.charAt(0) + "\" 应大写",
                    "改为 \"" + first + "\"",
                    "英语句子的第一个字母必须大写，这是基本的书写规范。"
            ));
        }
    }

    private static void checkStandaloneI(AnalysisResult result) {
        Matcher matcher = STANDALONE_I.matcher(result.corrected);
        if (matcher.find()) {
            result.corrected = matcher.replaceAll("I");
            result.issues.add(new GrammarIssue(
                    "代词大小写",
                    "第一人称代词 \"i\" 应写作 \"I\"",
                    "统一改为 \"I\"",
                    "英语中第一人称代词 I 无论在句首还是句中，都必须大写，没有例外。"
            ));
        }
    }

    private static void checkSubjectVerbAgreement(AnalysisResult result) {
        String lower = result.corrected.toLowerCase();
        if (lower.matches("(?i)(he|she|it)\\s+like\\b.*")) {
            result.corrected = result.corrected.replaceFirst("(?i)(he|she|it)\\s+like\\b", "$1 likes");
            result.issues.add(new GrammarIssue(
                    "主谓一致",
                    "第三人称单数主语后动词 like 应变为 likes",
                    "将 like 改为 likes",
                    "一般现在时中，he/she/it 作主语时，动词需加 -s/-es（He likes dogs.）。"
            ));
        } else if (lower.matches("(?i)(i|you|we|they)\\s+likes\\b.*")) {
            result.corrected = result.corrected.replaceFirst("(?i)(i|you|we|they)\\s+likes\\b", "$1 like");
            result.issues.add(new GrammarIssue(
                    "主谓一致",
                    "I/you/we/they 作主语时动词应使用原形 like",
                    "将 likes 改为 like",
                    "一般现在时中，I/you/we/they 作主语时，动词用原形，不加 -s。"
            ));
        }
    }

    private static void checkCommonMisspellings(AnalysisResult result) {
        String lower = result.corrected.toLowerCase();
        for (Map.Entry<String, String> entry : COMMON_MISSPELLINGS.entrySet()) {
            String wrong = entry.getKey();
            if (containsWord(lower, wrong)) {
                result.corrected = replaceWordIgnoreCase(result.corrected, wrong, entry.getValue());
                result.issues.add(new GrammarIssue(
                        "拼写/用词",
                        "单词 \"" + wrong + "\" 使用不当",
                        "建议改为 \"" + entry.getValue() + "\"",
                        misspellingExplanation(wrong, entry.getValue())
                ));
            }
        }
    }

    private static void checkArticleUsage(AnalysisResult result) {
        String lower = result.corrected.toLowerCase();
        if (lower.matches("(?i).*\\ba\\s+[aeiou].*")) {
            Matcher m = Pattern.compile("(?i)\\ba\\s+([aeiou]\\w*)").matcher(result.corrected);
            if (m.find()) {
                String word = m.group(1);
                result.corrected = m.replaceFirst("an " + word);
                result.issues.add(new GrammarIssue(
                        "冠词",
                        "元音音素开头的词前应使用 an",
                        "将 a " + word + " 改为 an " + word,
                        "不定冠词 a/an 的选择取决于后面单词的发音：元音音素开头用 an（an apple），辅音音素开头用 a（a university）。"
                ));
            }
        }
    }

    private static void checkEndPunctuation(AnalysisResult result) {
        String trimmed = result.corrected.trim();
        if (!trimmed.isEmpty() && !trimmed.matches(".*[.!?]$")) {
            result.corrected = trimmed + ".";
            result.issues.add(new GrammarIssue(
                    "标点符号",
                    "陈述句末尾缺少句号",
                    "在句末添加 \".\"",
                    "英语陈述句通常以句号结尾，这有助于区分句子边界，使表达更规范。"
            ));
        }
    }

    private static String misspellingExplanation(String wrong, String correct) {
        if ("doges".equals(wrong)) {
            return "dog 的复数形式是 dogs（直接加 -s）。以 -s, -x, -ch, -sh 等结尾的名词才加 -es（如 boxes, watches）。";
        }
        if (wrong.contains("dont") || wrong.contains("doesnt") || wrong.contains("cant") || wrong.contains("wont")) {
            return "否定缩写需使用撇号（'）连接，如 don't = do not。";
        }
        if ("alot".equals(wrong)) {
            return "a lot 表示“很多”，是固定搭配，不能合并拼写为 alot。";
        }
        return "请使用标准拼写 \"" + correct + "\"，这更符合书面英语规范。";
    }

    private static boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsWord(String text, String word) {
        return Pattern.compile("(?i)\\b" + Pattern.quote(word) + "\\b").matcher(text).find();
    }

    private static String replaceWordIgnoreCase(String text, String wrong, String correct) {
        return text.replaceAll("(?i)\\b" + Pattern.quote(wrong) + "\\b", correct);
    }

    private static String normalizeSpaces(String text) {
        return text.replaceAll("\\s+", " ").trim();
    }

    public static class AnalysisResult {
        public String original;
        public String corrected;
        public List<GrammarIssue> issues;
    }

    public static class GrammarIssue {
        public final String type;
        public final String problem;
        public final String correction;
        public final String explanation;

        public GrammarIssue(String type, String problem, String correction, String explanation) {
            this.type = type;
            this.problem = problem;
            this.correction = correction;
            this.explanation = explanation;
        }
    }
}
