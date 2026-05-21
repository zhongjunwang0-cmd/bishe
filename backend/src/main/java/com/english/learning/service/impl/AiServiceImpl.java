package com.english.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.english.learning.entity.LearningRecord;
import com.english.learning.service.AiService;
import com.english.learning.service.ExternalLlmService;
import com.english.learning.service.LearningRecordService;
import com.english.learning.service.SystemConfigService;
import com.english.learning.dto.AiChatResult;
import com.english.learning.dto.AiIntegrationConfig;
import com.english.learning.util.EnglishWritingAnalyzer;
import com.english.learning.util.LearningRecordTypeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI 辅导服务实现。
 *
 * <p>问答流程：管理员在「工具与 API 集成」中启用并配置 Key 后，优先调用 OpenAI 兼容的外部 LLM
 * （DeepSeek / GPT 等）；若未配置或调用失败，自动降级到内置规则引擎，保证离线可用。</p>
 * <p>个性化学习建议始终基于用户真实学习记录生成。</p>
 */
@Service
public class AiServiceImpl implements AiService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AiServiceImpl.class);

    @Autowired
    private LearningRecordService learningRecordService;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private ExternalLlmService externalLlmService;

    // ─── 个性化建议（基于真实学习记录） ─────────────────────────────────────────
    @Override
    public String getPersonalizedAdvice(Long userId) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        QueryWrapper<LearningRecord> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).ge("create_time", sevenDaysAgo);
        List<LearningRecord> records = learningRecordService.list(qw);

        if (records.isEmpty()) {
            return "【AI 学习顾问】 您好！看起来您最近还没有开始系统学习。建议从「词汇学习」入手，" +
                   "每天坚持 15 分钟，配合「语法解析」打好基础，循序渐进地提升英语能力。加油！💪";
        }

        // 统计各模块学习次数
        Map<String, Long> typeCounts = records.stream()
                .collect(Collectors.groupingBy(
                        r -> LearningRecordTypeUtil.normalize(r.getType()),
                        Collectors.counting()));

        int totalDurationSecs = records.stream()
                .mapToInt(r -> r.getDuration() != null ? r.getDuration() : 0).sum();
        int totalMinutes = totalDurationSecs / 60;

        String topType = typeCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> LearningRecordTypeUtil.toDisplayName(e.getKey()))
                .orElse("未知");

        // 找出缺少练习的模块
        List<String> missingTypes = List.of("VOCAB","GRAMMAR","READING","LISTENING","ORAL").stream()
                .filter(t -> !typeCounts.containsKey(t))
                .map(LearningRecordTypeUtil::toDisplayName)
                .collect(Collectors.toList());

        StringBuilder advice = new StringBuilder("【AI 学习顾问】 ");
        advice.append("过去 7 天您累计学习了 ").append(totalMinutes).append(" 分钟，共完成 ")
              .append(records.size()).append(" 次练习，表现").append(totalMinutes >= 60 ? "优秀" : "良好").append("！📊\n\n");
        advice.append("● 您在「").append(topType).append("」模块投入最多，基础训练很扎实。\n");

        if (!missingTypes.isEmpty()) {
            advice.append("● 本周尚未涉及：").append(String.join("、", missingTypes))
                  .append("。建议适当补充，实现全面提升。\n");
        }

        if (typeCounts.size() >= 4 && totalMinutes >= 60) {
            advice.append("● 全面发展，状态极佳！可以尝试挑战「文献阅读」中的困难级别，进一步突破语言瓶颈。");
        } else if (totalMinutes < 30) {
            advice.append("● 学习时长略显不足，建议每天保持 30 分钟以上的有效练习，养成习惯更重要。");
        } else {
            advice.append("● 保持当前节奏，并适当增加听力和口语联系，全方位提升综合能力。");
        }

        return advice.toString();
    }

    // ─── 知识问答：外部 LLM 优先，规则引擎降级 ───────────────────────────────────
    @Override
    public AiChatResult getChatResponse(Long userId, String query) {
        AiIntegrationConfig aiConfig = systemConfigService.getAiIntegrationConfig();
        if (aiConfig.isConfigured() && query != null && !query.isBlank()) {
            var externalReply = externalLlmService.chat(aiConfig, query);
            if (externalReply.isPresent()) {
                AiChatResult result = new AiChatResult();
                result.setContent("【AI 智能辅导】 " + externalReply.get());
                result.setSource("external");
                return result;
            }
            log.debug("External AI unavailable, falling back to rule engine for query");
        }

        AiChatResult result = new AiChatResult();
        result.setContent(ruleEngineChatResponse(userId, query));
        result.setSource("rule_engine");
        return result;
    }

    private String ruleEngineChatResponse(Long userId, String query) {
        if (query == null || query.isBlank()) {
            return "【AI 学习顾问】 您好！请问有什么英语学习问题需要我来帮助您解答呢？😊";
        }

        String q = query.toLowerCase().strip();

        // ── 写作批改：用户提供英文句子并要求纠错/修改 ─────────────────────────────
        if (EnglishWritingAnalyzer.isCorrectionIntent(q) && EnglishWritingAnalyzer.containsEnglishText(query)) {
            return buildWritingCorrectionResponse(query);
        }

        // ── 语法讲解：具体语法点或附带英文句子的语法问题 ───────────────────────────
        if (EnglishWritingAnalyzer.isGrammarExplanationIntent(q)) {
            String grammarExplanation = EnglishWritingAnalyzer.formatGrammarExplanation(q);
            if (grammarExplanation != null) {
                return grammarExplanation;
            }
            if (EnglishWritingAnalyzer.containsEnglishText(query)) {
                return buildWritingCorrectionResponse(query);
            }
        }

        // ── 仅含英文短句时，默认按写作批改处理 ─────────────────────────────────────
        if (EnglishWritingAnalyzer.containsEnglishText(query) && isMostlyEnglish(query)) {
            return buildWritingCorrectionResponse(query);
        }

        // ── 词汇类 ──────────────────────────────────────────────────────────────
        if (contains(q, "单词","词汇","word","vocab","记忆","背单词","忘记")) {
            return "【AI 学习顾问】 关于词汇记忆，推荐以下几种科学方法：\n\n" +
                   "1️⃣ **艾宾浩斯记忆曲线**：在学完 1 小时、1 天、3 天、1 周后依次复习，可显著提高保留率。\n" +
                   "2️⃣ **语境记忆**：结合例句记忆单词，避免孤立死记，系统中每个词汇均配有真实例句。\n" +
                   "3️⃣ **词根词缀法**：掌握常见词根（如 -port- 运输、-graph- 写）可推断大量陌生词汇。\n" +
                   "4️⃣ **主动输出**：尝试用新词造句或纳入口语练习，从被动识别转为主动掌握。\n\n" +
                   "建议每天在「词汇学习」模块学习 10-15 个新词，配合「选词填空」巩固记忆效果。";
        }

        // ── 语法类（通用学习方法，非具体语法点） ───────────────────────────────────
        if (contains(q, "语法","grammar","句型","从句","时态","虚拟","被动","倒装","冠词")) {
            return "【AI 语法讲解】 语法是语言运用的框架，您可以这样系统掌握：\n\n" +
                   "1️⃣ **时态与语态**：理解 [时间轴 × 动作状态] 的逻辑就能举一反三。\n" +
                   "2️⃣ **从句结构**：定语从句、状语从句、名词性从句是三大主轴。\n" +
                   "3️⃣ **长难句拆解**：先找主谓主干，再逐层剥离修饰成分。\n" +
                   "4️⃣ **及时巩固**：学完语法规则后立刻造句练习。\n\n" +
                   "您可以问我具体语法点（如“讲解一般现在时”“定语从句怎么用”），或直接发送英文句子让我批改。";
        }

        // ── 阅读类 ──────────────────────────────────────────────────────────────
        if (contains(q, "阅读","read","文章","文献","理解","comprehension")) {
            return "【AI 学习顾问】 提升英语阅读能力需要量与质的双重积累：\n\n" +
                   "1️⃣ **泛读 + 精读结合**：泛读扩展词汇量和语感，精读培养深度理解和语言分析能力。\n" +
                   "2️⃣ **略读策略**：先快速浏览标题、段落首末句，把握文章框架，再带着问题精读细节。\n" +
                   "3️⃣ **学术文献阅读**：系统中收录了人工智能教育、气候变化、跨文化交际等 6 篇精选学术文献，" +
                   "均配有中文译文，是提升学术英语最高效的途径。\n" +
                   "4️⃣ **记录生词**：阅读中遇到不认识的词汇，记录下来后在词汇模块重点复习。";
        }

        // ── 听力类 ──────────────────────────────────────────────────────────────
        if (contains(q, "听力","listening","听","听懂","口音","bbc","ted","雅思","托福")) {
            return "【AI 学习顾问】 听力提升是一个循序渐进的过程，建议以下路径：\n\n" +
                   "1️⃣ **由慢到快**：先从日常英语对话开始，逐步过渡到 BBC、TED 演讲、学术讲座。\n" +
                   "2️⃣ **精听技巧**：遇到听不懂的片段，先猜测再反复听，最后对照文本，这比直接看文本收获更大。\n" +
                   "3️⃣ **多口音接触**：系统中覆盖英式（BBC/爱丁堡）、美式（CNN/TED）等多种口音，建议均衡练习。\n" +
                   "4️⃣ **同步跟读**：听的同时跟读（Shadow Reading）可同步提升语感和口语节奏感。\n\n" +
                   "系统「听力训练」模块现有 8 个精选练习，涵盖 BBC 新闻、TED 演讲、TOEFL/IELTS 模拟等多种类型。";
        }

        // ── 写作类 ──────────────────────────────────────────────────────────────
        if (contains(q, "写作","写","write","writing","作文","essay","论文","写好")) {
            return "\u3010AI \u5b66\u4e60\u987e\u95ee\u3011 \u82f1\u8bed\u5199\u4f5c\u80fd\u529b\u7684\u63d0\u5347\u9700\u8981\u300c\u8bfb-\u601d-\u5199\u300d\u4e09\u4f4d\u4e00\u4f53\u7684\u8bad\u7ec3\uff1a\n\n" +
                   "1️⃣ **结构先行**：动笔前先列提纲，明确论点（Thesis）、支撑论据（Body）和结论（Conclusion）。\n" +
                   "2️⃣ **积累句型**：学习地道的英文表达，如强调句（It is... that...）、让步句（Although...）、递进句。\n" +
                   "3️⃣ **多读范文**：系统「文献阅读」中的学术文章是学习规范英文写作的极好素材。\n" +
                   "4️⃣ **修改迭代**：写完后大声朗读，检查语法错误和逻辑连贯性，再逐步打磨措辞。\n" +
                   "5️⃣ **避免直译**：用英语思维写作，而非将中文翻译成英文。";
        }

        // ── 口语类 ──────────────────────────────────────────────────────────────
        if (contains(q, "口语","说","speak","speaking","发音","pronunciation","流利","fluent")) {
            return "【AI 学习顾问】 提升英语口语流利度的关键在于克服心理障碍和大量开口：\n\n" +
                   "1️⃣ **音标基础**：掌握国际音标，能让学习新单词的发音事半功倍，减少依赖中式发音习惯。\n" +
                   "2️⃣ **跟读模仿**：选取系统中的听力材料，跟读学习自然语速、连读和弱读规律。\n" +
                   "3️⃣ **口语练习模块**：系统提供 8 个口语话题，从介绍家乡到分析中西教育异同，覆盖日常到学术场景。\n" +
                   "4️⃣ **录音回听**：录下自己的口语练习，与原声对比，发现并纠正固定错误。\n" +
                   "5️⃣ **容错心态**：流利度优先，准确性其次。开口比沉默更重要。";
        }

        // ── 考试类 ──────────────────────────────────────────────────────────────
        if (contains(q, "四级","六级","cet","雅思","ielts","托福","toefl","考试","备考","分数")) {
            return "【AI 学习顾问】 关于英语考试备考策略：\n\n" +
                   "🎯 **CET-4 / CET-6**：词汇是基础（CET-4 约 4000 词，CET-6 约 6000 词），配合系统中分级的 CET-4/6 词汇专项练习，" +
                   "每天攻克 20 个高频词；阅读理解重点提升信息查找和主旨理解；翻译题注重积累中英文对应的固定搭配。\n\n" +
                   "🎯 **IELTS / TOEFL**：系统中 IELTS/TOEFL 标记的词汇是核心目标词；听力模块的考试模拟题型需强化练习；" +
                   "写作需学习议论文（Task 2）的结构和高分词汇搭配。\n\n" +
                   "建议制定 8-12 周备考计划，每周重点攻破 1-2 个技能板块，临考前 2 周做整套模拟题熟悉题型节奏。";
        }

        // ── 学习方法 / 效率类 ─────────────────────────────────────────────────────
        if (contains(q, "方法","计划","怎么学","如何","时间","效率","坚持","motivation","学不进")) {
            return "【AI 学习顾问】 高效英语学习的核心方法论：\n\n" +
                   "📅 **制定计划**：每天固定 30-60 分钟，比碎片化的间歇学习有效 3 倍。可参考：\n" +
                   "   • 周一三五 → 词汇 + 语法\n   • 周二四六 → 阅读理解 + 文献\n   • 每天 → 听力 10 分钟（碎片时间）\n\n" +
                   "🧠 **间隔重复**：利用艾宾浩斯遗忘曲线，系统地在 1天/3天/1周/1月 的节点复习已学内容。\n\n" +
                   "📈 **追踪进度**：系统的「学习记录追踪」功能可以可视化您的学习轨迹，保持动力。\n\n" +
                   "🎯 **有效输出**：每学完一个语法点，立即用它造 3 个句子；每学 5 个单词，尝试用其中 2 个写段话。";
        }

        // ── 个性化建议兜底 ───────────────────────────────────────────────────────
        if (contains(q, "建议","咨询","推荐","我该","适合我","适合我")) {
            return getPersonalizedAdvice(userId);
        }

        // ── 通用兜底回复 ─────────────────────────────────────────────────────────
        return "【AI 学习顾问】 您的问题是：「" + query.trim() + "」\n\n" +
               "我理解您的咨询，但能否提供更多背景以便我给出更精准的建议？例如：\n" +
               "• 您目前的英语水平（CET-4/6 或 IELTS 分数）\n" +
               "• 主要学习痛点（词汇、语法、听力、写作……）\n" +
               "• 近期学习目标（备考、提升日常交流、学术英语……）\n\n" +
               "或者，直接告诉我「给我学习建议」，我将基于您的真实学习数据为您定制专属学习方案！😊";
    }

    private String buildWritingCorrectionResponse(String query) {
        List<EnglishWritingAnalyzer.AnalysisResult> results = EnglishWritingAnalyzer
                .extractEnglishFragments(query)
                .stream()
                .map(EnglishWritingAnalyzer::analyze)
                .toList();

        if (results.isEmpty()) {
            return "【AI 写作批改】 请直接发送需要批改的英文句子，例如：\n" +
                   "「I like doges. 请帮我找出错误并修改」\n\n" +
                   "我会逐条指出错误并给出语法讲解。";
        }
        return EnglishWritingAnalyzer.formatCorrectionResponse(query, results);
    }

    /** 判断文本是否以英文为主（用于识别用户直接发送的英文句子） */
    private boolean isMostlyEnglish(String text) {
        long latin = text.chars().filter(c -> (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')).count();
        long cjk = text.chars().filter(c -> Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN).count();
        return latin >= 3 && latin > cjk;
    }

    // ─── 工具方法 ───────────────────────────────────────────────────────────────
    /** 检查 query 是否包含关键词列表中的任意一个 */
    private boolean contains(String query, String... keywords) {
        for (String kw : keywords) {
            if (query.contains(kw)) return true;
        }
        return false;
    }
}
