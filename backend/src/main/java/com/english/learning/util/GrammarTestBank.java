package com.english.learning.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 语法专项测试题库，按语法知识点 ID 匹配 seed_data 中的 biz_grammar 记录。
 */
public final class GrammarTestBank {

    public record GrammarQuestion(
            String question,
            List<Map<String, String>> options,
            String correct,
            String explanation
    ) {}

    private static final Map<Long, List<GrammarQuestion>> BANK = new LinkedHashMap<>();

    static {
        bank(1L,
                q("If I ___ wings, I could fly.",
                        opts("A", "have", "B", "had", "C", "will have", "D", "would have"),
                        "B", "与现在事实相反的虚拟语气，If 从句用过去式 had。"),
                q("If he ___ hard yesterday, he would have passed the exam.",
                        opts("A", "studied", "B", "had studied", "C", "studies", "D", "has studied"),
                        "B", "与过去事实相反，If 从句用 had + 过去分词。"),
                q("I wish I ___ taller.",
                        opts("A", "am", "B", "was/were", "C", "will be", "D", "have been"),
                        "B", "wish 后接与现在相反的虚拟语气，be 动词用 was/were。"));

        bank(2L,
                q("This is the book ___ I bought yesterday.",
                        opts("A", "who", "B", "which", "C", "where", "D", "when"),
                        "B", "先行词 book 是物，限定性定语从句用 which/that。"),
                q("My teacher, ___ studied in the UK, speaks English fluently.",
                        opts("A", "that", "B", "who", "C", "which", "D", "whom"),
                        "B", "非限定性定语从句修饰人，用 who，且不能用 that。"),
                q("The reason ___ he was late is that he missed the bus.",
                        opts("A", "why", "B", "which", "C", "where", "D", "when"),
                        "A", "reason 作先行词时，关系副词用 why。"));

        bank(3L,
                q("I ___ already finished my homework.",
                        opts("A", "have", "B", "had", "C", "will have", "D", "am having"),
                        "A", "already 常与现在完成时 have/has + 过去分词连用。"),
                q("When I arrived, the train ___ already left.",
                        opts("A", "has", "B", "had", "C", "was", "D", "would"),
                        "B", "过去某一时刻之前完成的动作，用过去完成时 had + 过去分词。"),
                q("By this time next year, I ___ English for three years.",
                        opts("A", "will study", "B", "will have studied", "C", "have studied", "D", "studied"),
                        "B", "by + 将来时间点，表示将来完成，用 will have + 过去分词。"));

        bank(4L,
                q("You ___ smoke in the hospital.",
                        opts("A", "mustn't", "B", "don't have to", "C", "needn't", "D", "may not"),
                        "A", "mustn't 表示禁止；don't have to 表示不必。"),
                q("He ___ speak five languages.",
                        opts("A", "can", "B", "must", "C", "should", "D", "may"),
                        "A", "can 表示能力；must 表示必须；should 表示建议。"),
                q("It ___ rain this afternoon, so take an umbrella.",
                        opts("A", "must", "B", "might", "C", "can", "D", "should"),
                        "B", "might 表示可能性较小；must 表示较大把握或义务。"));

        bank(5L,
                q("The data ___ analyzed by researchers now.",
                        opts("A", "is being", "B", "is", "C", "was", "D", "has been"),
                        "A", "现在进行时的被动语态：is/are being + 过去分词。"),
                q("English ___ in many countries.",
                        opts("A", "speaks", "B", "is spoken", "C", "spoke", "D", "is speaking"),
                        "B", "一般现在时被动：am/is/are + 过去分词。"),
                q("The report ___ by tomorrow.",
                        opts("A", "will finish", "B", "will be finished", "C", "finishes", "D", "is finishing"),
                        "B", "将来时被动：will be + 过去分词。"));

        bank(6L,
                q("Never ___ I seen such beautiful scenery.",
                        opts("A", "have", "B", "had", "C", "do", "D", "did"),
                        "A", "否定副词 never 置于句首，部分倒装：Never + 助动词 + 主语。"),
                q("Only by working hard ___ we succeed.",
                        opts("A", "we can", "B", "can we", "C", "we could", "D", "could we"),
                        "B", "Only + 状语置于句首，主句部分倒装。"),
                q("Here ___ the bus!",
                        opts("A", "the bus comes", "B", "comes the bus", "C", "is coming the bus", "D", "the bus is coming"),
                        "B", "Here/There 置于句首且主语为名词时，完全倒装。"));

        bank(7L,
                q("She decided ___ abroad next year.",
                        opts("A", "study", "B", "to study", "C", "studying", "D", "studied"),
                        "B", "decide 后接不定式 to do，表示将来计划。"),
                q("She enjoys ___ on rainy days.",
                        opts("A", "read", "B", "to read", "C", "reading", "D", "reads"),
                        "C", "enjoy 后接动名词 doing。"),
                q("I remember ___ the door, but I can't find my keys now.",
                        opts("A", "to lock", "B", "locking", "C", "lock", "D", "locked"),
                        "B", "remember doing 表示记得做过某事；remember to do 表示记得要去做。"));

        bank(8L,
                q("It was his hard work ___ made the project a success.",
                        opts("A", "which", "B", "that", "C", "who", "D", "what"),
                        "B", "It is/was...that 强调句型，强调主语 his hard work。"),
                q("I ___ study hard!",
                        opts("A", "do", "B", "did", "C", "does", "D", "doing"),
                        "B", "do/does/did + 动词原形，用于强调谓语。"),
                q("What he needs most ___ understanding and support.",
                        opts("A", "are", "B", "is", "C", "was", "D", "were"),
                        "B", "What 从句作主语时，谓语动词通常用单数 is。"));

        bank(9L,
                q("As soon as he ___, the meeting began.",
                        opts("A", "arrives", "B", "arrived", "C", "has arrived", "D", "will arrive"),
                        "B", "主句为过去时，时间状语从句也用一般过去时。"),
                q("___ the weather was terrible, they arrived on time.",
                        opts("A", "Because", "B", "Although", "C", "So", "D", "If"),
                        "B", "although/though 引导让步状语从句，表示尽管。"),
                q("The question was ___ difficult ___ nobody could answer it.",
                        opts("A", "so...that", "B", "such...that", "C", "too...to", "D", "enough...to"),
                        "A", "so + 形容词 + that 引导结果状语从句。"));

        bank(10L,
                q("It is ___ university near the city center.",
                        opts("A", "a", "B", "an", "C", "the", "D", "/"),
                        "A", "university 首音为 /j/ 辅音，用 a 而非 an。"),
                q("She plays ___ piano every weekend.",
                        opts("A", "a", "B", "an", "C", "the", "D", "/"),
                        "C", "西洋乐器前通常加定冠词 the。"),
                q("He is ___ tallest student in the class.",
                        opts("A", "a", "B", "an", "C", "the", "D", "/"),
                        "C", "形容词最高级前用定冠词 the。"));
    }

    private GrammarTestBank() {
    }

    public static List<GrammarQuestion> getQuestions(Long grammarId) {
        return BANK.getOrDefault(grammarId, defaultQuestions());
    }

    public static boolean hasQuestions(Long grammarId) {
        return BANK.containsKey(grammarId);
    }

    private static void bank(Long id, GrammarQuestion... questions) {
        BANK.put(id, List.of(questions));
    }

    private static GrammarQuestion q(String question, List<Map<String, String>> options,
                                     String correct, String explanation) {
        return new GrammarQuestion(question, options, correct, explanation);
    }

    private static List<Map<String, String>> opts(String l1, String v1, String l2, String v2,
                                                    String l3, String v3, String l4, String v4) {
        List<Map<String, String>> options = new ArrayList<>(4);
        options.add(option(l1, v1));
        options.add(option(l2, v2));
        options.add(option(l3, v3));
        options.add(option(l4, v4));
        return options;
    }

    private static Map<String, String> option(String label, String value) {
        Map<String, String> opt = new LinkedHashMap<>();
        opt.put("label", label + ". " + value);
        opt.put("value", label);
        return opt;
    }

    private static List<GrammarQuestion> defaultQuestions() {
        return List.of(
                q("Which sentence uses the correct verb form?",
                        opts("A", "He go to school every day.", "B", "He goes to school every day.",
                                "C", "He going to school every day.", "D", "He gone to school every day."),
                        "B", "第三人称单数一般现在时，动词加 -s。"),
                q("Choose the correct article.",
                        opts("A", "a", "B", "an", "C", "the", "D", "/"),
                        "B", "hour 以元音音素开头，用 an。"),
                q("Which is a complex sentence?",
                        opts("A", "I like English.", "B", "I like English, and she likes math.",
                                "C", "I like English because it is useful.", "D", "English, useful and fun."),
                        "C", "because 引导原因状语从句，构成复合句。"));
    }
}
