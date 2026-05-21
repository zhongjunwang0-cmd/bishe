package com.english.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.english.learning.entity.LearningRecord;
import com.english.learning.service.AiService;
import com.english.learning.service.LearningRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI 辅导服务实现。
 *
 * <p><b>设计说明</b>：本系统为完全离线部署的自含式英语学习平台，不依赖任何外部 LLM API，
 * 以保证环境无依赖、数据安全合规。AI 辅导能力通过以下两层机制实现：</p>
 * <ol>
 *   <li><b>个性化学习分析</b>：读取当前用户过去 7 天的真实学习记录，动态计算累计时长、
 *   活跃模块分布、全面性等指标，生成定制化学习建议（真实数据驱动，非硬编码）。</li>
 *   <li><b>知识问答引擎</b>：基于领域规则匹配 + 语境扩展的规则引擎，覆盖词汇、语法、
 *   阅读、听力、写作、考试策略等常见学习咨询场景，响应包含具体方法论和行动建议。</li>
 * </ol>
 */
@Service
public class AiServiceImpl implements AiService {

    @Autowired
    private LearningRecordService learningRecordService;

    // ─── 模块中文映射 ───────────────────────────────────────────────────────────
    private static final Map<String, String> TYPE_ZH = Map.of(
            "VOCAB",    "词汇学习",
            "GRAMMAR",  "语法解析",
            "LIT",      "文献阅读",
            "READING",  "阅读理解",
            "LISTENING","听力训练",
            "CLOZE",    "选词填空",
            "ORAL",     "口语练习"
    );

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
                .collect(Collectors.groupingBy(LearningRecord::getType, Collectors.counting()));

        int totalDurationSecs = records.stream()
                .mapToInt(r -> r.getDuration() != null ? r.getDuration() : 0).sum();
        int totalMinutes = totalDurationSecs / 60;

        String topType = typeCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> TYPE_ZH.getOrDefault(e.getKey(), e.getKey()))
                .orElse("未知");

        // 找出缺少练习的模块
        List<String> missingTypes = List.of("VOCAB","GRAMMAR","READING","LISTENING","ORAL").stream()
                .filter(t -> !typeCounts.containsKey(t))
                .map(t -> TYPE_ZH.getOrDefault(t, t))
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

    // ─── 知识问答引擎（领域规则 + 语境扩展） ─────────────────────────────────────
    @Override
    public String getChatResponse(Long userId, String query) {
        if (query == null || query.isBlank()) {
            return "【AI 学习顾问】 您好！请问有什么英语学习问题需要我来帮助您解答呢？😊";
        }

        String q = query.toLowerCase().strip();

        // ── 词汇类 ──────────────────────────────────────────────────────────────
        if (contains(q, "单词","词汇","word","vocab","记忆","背单词","忘记")) {
            return "【AI 学习顾问】 关于词汇记忆，推荐以下几种科学方法：\n\n" +
                   "1️⃣ **艾宾浩斯记忆曲线**：在学完 1 小时、1 天、3 天、1 周后依次复习，可显著提高保留率。\n" +
                   "2️⃣ **语境记忆**：结合例句记忆单词，避免孤立死记，系统中每个词汇均配有真实例句。\n" +
                   "3️⃣ **词根词缀法**：掌握常见词根（如 -port- 运输、-graph- 写）可推断大量陌生词汇。\n" +
                   "4️⃣ **主动输出**：尝试用新词造句或纳入口语练习，从被动识别转为主动掌握。\n\n" +
                   "建议每天在「词汇学习」模块学习 10-15 个新词，配合「选词填空」巩固记忆效果。";
        }

        // ── 语法类 ──────────────────────────────────────────────────────────────
        if (contains(q, "语法","grammar","句型","从句","时态","虚拟","被动","倒装","冠词")) {
            return "【AI 学习顾问】 语法是语言运用的框架，以下几点可以帮您系统掌握：\n\n" +
                   "1\u5e8f\ufe0f\u20e3 **\u65f6\u6001\u4e0e\u8bed\u6001**\uff1a\u82f1\u8bed 16 \u79cd\u65f6\u6001\u770b\u4f3c\u590d\u6742\uff0c\u7406\u89e3 [\u65f6\u95f4\u8f74 x \u52a8\u4f5c\u72b6\u6001] \u7684\u903b\u8f91\u5c31\u80fd\u4e3e\u4e00\u53cd\u4e09\u3002\n" +
                   "2\u5e8f\ufe0f\u20e3 **\u4ece\u53e5\u7ed3\u6784**\uff1a\u5b9a\u8bed\u4ece\u53e5\uff08\u4fee\u9970\u540d\u8bcd\uff09\u3001\u72b6\u8bed\u4ece\u53e5\uff08\u4fee\u9970\u52a8\u8bcd/\u5f62\u5bb9\u8bcd\uff09\u3001\u540d\u8bcd\u6027\u4ece\u53e5\uff08\u5145\u5f53\u6210\u5206\uff09\u662f\u4e09\u5927\u4e3b\u8f74\u3002\n" +
                   "3\u5e8f\ufe0f\u20e3 **\u957f\u96be\u53e5\u62c6\u89e3**\uff1a\u9047\u5230\u590d\u6742\u53e5\u5b50\uff0c\u5148\u627e\u4e3b\u8c13\u5bbe\u4e3b\u5e72\uff0c\u518d\u9010\u5c42\u5265\u79bb\u4fee\u9970\u6210\u5206\u3002\n" +
                   "4\u5e8f\ufe0f\u20e3 **\u53ca\u65f6\u5df3\u56fa**\uff1a\u5b66\u5b8c\u8bed\u6cd5\u89c4\u5219\u540e\u7acb\u523b\u505a\u7ec3\u4e60\uff0c\u9632\u6b62\u9057\u5fd8\u3002\n\n" +
                   "您可以在「语法解析库」中查看虚拟语气、定语从句、情态动词等 10 个核心专题，每篇均附例句与辨析技巧。";
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

    // ─── 工具方法 ───────────────────────────────────────────────────────────────
    /** 检查 query 是否包含关键词列表中的任意一个 */
    private boolean contains(String query, String... keywords) {
        for (String kw : keywords) {
            if (query.contains(kw)) return true;
        }
        return false;
    }
}
