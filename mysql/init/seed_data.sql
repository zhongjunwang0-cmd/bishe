SET NAMES utf8mb4;
USE `english_learning`;

INSERT INTO `biz_vocab` (`word`, `phonetic`, `translation`, `example`, `level`) VALUES
('ambiguous','/æmˈbɪɡjuəs/','模糊的；多义的','这份合同的条款措辞含糊，容易引起误解。','CET-6'),
('perseverance','/ˌpɜːsɪˈvɪərəns/','坚持不懈；毅力','成功需要天赋与坚持不懈的努力，缺一不可。','IELTS'),
('eloquent','/ˈeləkwənt/','口才好的；雄辩的','她发表了一篇有力的演讲，打动了所有在场的人。','IELTS'),
('cognition','/kɒɡˈnɪʃn/','认知；认识','早期语言学习对幼儿认知发展至关重要。','CET-6'),
('diligent','/ˈdɪlɪdʒənt/','勤奋的；努力的','勤奋的学生往往在考试中取得优异成绩。','CET-4'),
('abandon','/əˈbændən/','放弃；遗弃','他们不得不放弃原来的计划，重新开始。','CET-4'),
('curriculum','/kəˈrɪkjʊləm/','课程；课程体系','学校正在对英语课程进行全面改革。','CET-6'),
('hypothesis','/haɪˈpɒθəsɪs/','假设；假说','科学家提出了一个新的气候变化假说。','IELTS'),
('collaborate','/kəˈlæbəreɪt/','合作；协作','两所大学将合作开展这项研究项目。','CET-6'),
('perspective','/pəˈspektɪv/','观点；视角；透视法','从不同角度看问题有助于做出更好的决策。','IELTS'),
('integrity','/ɪnˈteɡrɪti/','诚信；正直；完整性','在职场中，诚信是最重要的品质之一。','CET-6'),
('fluctuate','/ˈflʌktʃueɪt/','波动；起伏','汇率每天都在持续波动，变化不定。','CET-6'),
('metaphor','/ˈmetəfə/','比喻；隐喻','时间就是金钱，这是一个常见的比喻表达。','CET-4'),
('controversial','/ˌkɒntrəˈvɜːʃl/','有争议的；引发争论的','这一政策在社会上引发了广泛争议。','IELTS'),
('phenomenon','/fɪˈnɒmɪnɒn/','现象','极端天气已经成为全球性现象，引发高度关注。','CET-6'),
('comprehend','/ˌkɒmprɪˈhend/','理解；领悟','他难以理解这篇学术论文的核心思想。','CET-6'),
('inevitable','/ɪnˈevɪtəbl/','不可避免的；必然的','人口老龄化是一个不可避免的社会问题。','IELTS'),
('authentic','/ɔːˈθentɪk/','真实的；真正的；可信的','博物馆展出了几件珍贵的真品文物。','CET-6'),
('proficiency','/prəˈfɪʃənsi/','熟练；精通','英语四六级考试是衡量大学生英语水平的重要标准。','CET-6'),
('significant','/sɪɡˈnɪfɪkənt/','重要的；显著的；有意义的','这一发现对医学领域具有重大意义。','CET-4');

INSERT INTO `biz_grammar` (`title`, `category`, `content`) VALUES
('虚拟语气 (Subjunctive Mood)', '句型结构',
'虚拟语气用于表达与现实相反的愿望、假设或条件。\n\n【与现在相反】If + 主语 + 动词过去式，主语 + would/could/might + 动词原形\n例句：如果我有翅膀，我就能飞翔。If I had wings, I could fly.\n\n【与过去相反】If + 主语 + had + 过去分词，主语 + would have + 过去分词\n例句：如果昨天认真学习，他就通过考试了。If he had studied hard, he would have passed the exam.\n\n【常见搭配】wish / as if / as though / would rather + 虚拟语气'),

('定语从句 (Relative Clauses)', '从句',
'定语从句用于修饰名词或代词，分为限定性和非限定性两种。\n\n【限定性定语从句】不加逗号，对先行词进行必要的限定说明。\n例：这就是我昨天买的那本书。This is the book that I bought yesterday.\n\n【非限定性定语从句】加逗号，对先行词进行补充说明，不能用that。\n例：我的老师，曾在英国留学，英语非常流利。My teacher, who studied in the UK, speaks English fluently.\n\n【关系词选用】人→who/whom/whose；物→which/that；时间→when；地点→where；原因→why'),

('时态体系 — 完成时 (Perfect Tense)', '时态',
'完成时强调动作对另一时间点的影响或联系。\n\n【现在完成时】have/has + 过去分词，表示过去发生的动作对现在的影响。\n例：我已经完成了作业。I have already finished my homework.\n常与 already / yet / ever / never / just / recently 搭配\n\n【过去完成时】had + 过去分词，表示过去某一时刻之前已完成的动作（过去的过去）。\n例：当我到达车站时，火车已经开走了。When I arrived at the station, the train had already left.\n\n【将来完成时】will have + 过去分词，表示将来某时刻前将完成的动作。\n例：到明年这时候，我将已经学习英语三年了。By this time next year, I will have studied English for three years.'),

('情态动词 (Modal Verbs) 用法辨析', '词法',
'情态动词本身有含义，后接动词原形，不能单独作谓语。\n\n【can/could】能力或可能性\n  can 表示现在的能力；could 表示过去的能力或礼貌请求\n  例：他能说五种语言。He can speak five languages.\n\n【may/might】许可或推测\n  may 表示许可（正式）或较大可能；might 表示较小可能\n  例：他可能已经知道这件事了。He might already know about it.\n\n【must/have to】必须\n  must 表示主观义务；have to 表示客观需要\n  例：我不得不每天六点起床。I have to get up at six every day.\n\n【should/ought to】建议或义务\n  例：你应该多锻炼身体。You should exercise more.'),

('被动语态 (Passive Voice)', '句型结构',
'被动语态用于强调动作的承受者，或不知道/不关注动作执行者的情况。\n\n【基本结构】be + 过去分词（+ by 引出动作执行者）\n各时态变化体现在be动词的形式上：\n● 一般现在时：am/is/are + 过去分词\n● 一般过去时：was/were + 过去分词\n● 现在完成时：have/has been + 过去分词\n● 含情态动词：情态动词 + be + 过去分词\n\n例句对比：\n主动：研究人员正在分析数据。Researchers are analyzing the data.\n被动：数据正在被分析中。The data is being analyzed.'),

('倒装句 (Inversion)', '句型结构',
'倒装句通过改变正常语序来强调某一成分或满足语法要求。\n\n【完全倒装】整个谓语置于主语之前\n● 表方向/地点状语置于句首：Here comes the bus! 公共汽车来了！\n● There be结构：屋子里有很多人。There were many people in the room.\n\n【部分倒装】助动词/情态动词/be动词置于主语前\n● 否定副词置于句首：Never have I seen such beautiful scenery. 我从来没见过如此美丽的风景。\n● Only引导的状语置于句首：Only by working hard can we succeed. 只有通过努力，我们才能成功。\n● So/Neither + 助动词：A: I like English. B: So do I. 我也是。'),

('非谓语动词 — 不定式 vs 动名词', '词法',
'不定式（to + 动词原形）和动名词（动词 + ing）都属于非谓语动词，用法有明显区别。\n\n【接不定式的动词】表示将来、意愿、计划：\nwant/wish/hope/plan/decide/expect/refuse/manage/agree\n例：他决定明年出国留学。He decided to study abroad next year.\n\n【接动名词的动词】表示已发生的、习惯性的动作：\nenjoy/finish/mind/avoid/suggest/consider/keep/practice\n例：她喜欢在雨天读书。She enjoys reading on rainy days.\n\n【含义不同的词】\nremember to do（记得要去做）vs remember doing（记得做过）\nstop to do（停下来去做另一件事）vs stop doing（停止正在做的事）'),

('强调句型 (Emphatic Sentences)', '句型结构',
'英语中有多种强调句型，用于突出句子某一成分。\n\n【It is/was...that/who 强调句】最常用，几乎可强调任意成分。\n结构：It is/was + 被强调部分 + that/who + 其余部分\n例：正是他的努力使这个项目取得了成功。It was his hard work that made the project a success.\n\n【do/does/did 强调谓语】\n例：我确实学习了。I did study hard!\n\n【What从句强调】\n例：他最需要的是理解和支持。What he needs most is understanding and support.\n\n辨析：去掉 It is/was 和 that 后句子仍通顺→强调句；不通顺→主语从句。'),

('状语从句 (Adverbial Clauses)', '从句',
'状语从句修饰主句的动词、形容词或其他成分，按意义分为以下几类：\n\n【时间状语从句】when/while/as/before/after/until/as soon as\n例：他一来，会议就开始了。As soon as he arrived, the meeting began.\n\n【条件状语从句】if/unless/as long as/provided that\n例：只要坚持练习，英语一定会进步。As long as you keep practicing, your English will improve.\n\n【原因状语从句】because/since/as\n例：因为天气恶劣，比赛被推迟了。The game was postponed because the weather was terrible.\n\n【让步状语从句】although/though/even if/even though\n例：尽管路途遥远，他们仍按时到达。Although the journey was long, they arrived on time.\n\n【结果状语从句】so...that/such...that\n例：这道题太难了，没有人能回答。The question was so difficult that nobody could answer it.'),

('冠词 (Articles) 用法精讲', '词法',
'英语冠词分为不定冠词(a/an)和定冠词(the)，用法精细。\n\n【不定冠词 a/an】第一次提到，泛指某一个\n● a + 辅音开头的词；an + 元音音素开头的词\n● 例：It is a university. 这是一所大学。（university 首音为/j/辅音）\n● 例：It is an hour. 这是一小时。（hour 首音为元音）\n\n【定冠词 the】特指，双方都知道的事物\n● 表示独一无二：the sun / the moon / the earth\n● 形容词最高级：He is the tallest student in the class.\n● 乐器前：She plays the piano. 她弹钢琴。\n● 序数词前：This is the first time he has been to Beijing.\n\n【不用冠词(零冠词)】\n● 专有名词：Peking University\n● 球类、棋类运动：play football\n● 三餐、节日：have breakfast');

INSERT INTO `biz_literature` (`title`, `content`, `translation`, `author`) VALUES
('人工智能对高等教育的影响',
'Artificial intelligence (AI) is fundamentally transforming higher education in unprecedented ways. Universities worldwide are integrating AI-driven tools into their curricula, assessment methods, and administrative processes. Intelligent tutoring systems provide personalized learning experiences, adapting content difficulty based on individual student performance data. Natural language processing enables automated essay grading, while predictive analytics help identify at-risk students before they fall behind. However, concerns regarding academic integrity, data privacy, and the digital divide require careful policy consideration. Institutions must balance the efficiency gains of AI adoption with preserving the critical thinking and interpersonal skills that remain uniquely human.',
'人工智能正以前所未有的方式从根本上改变高等教育格局。世界各地的高校正将AI驱动工具融入课程、评估方法和行政流程之中。智能辅导系统根据学生个人表现数据调整内容难度，提供个性化学习体验。自然语言处理技术实现了论文自动批改，而预测性分析则有助于在学生落后之前识别其潜在风险。然而，学术诚信、数据隐私和数字鸿沟等问题需要慎重的政策考量。各院校必须在提升AI应用效率与保留批判性思维及人际交往能力之间寻求平衡。',
'张明远'),

('气候变化与英语语言的演变',
'Climate change has not only altered the physical environment but has also profoundly influenced the English language. New lexicons have emerged, including terms such as carbon footprint, net zero, greenwashing, and climate anxiety. These linguistic innovations reflect increasing societal awareness of and engagement with environmental issues. Linguists observe that environmental metaphors increasingly permeate political discourse, scientific literature, and everyday conversation. The Intergovernmental Panel on Climate Change (IPCC) reports have introduced standardized scientific terminology that has gradually entered mainstream usage.',
'气候变化不仅改变了自然环境，也对英语语言产生了深远影响。大量新词汇应运而生，如碳足迹、净零排放、绿色洗白和气候焦虑等。这些语言创新反映了社会对环境问题日益增长的认识和参与度。语言学家观察到，环境隐喻越来越多地渗透到政治话语、科学文献和日常对话之中。政府间气候变化专门委员会（IPCC）的报告引入了标准化科学术语，这些术语逐渐进入主流应用。',
'李环境'),

('跨文化交际中的语言障碍',
'Effective cross-cultural communication requires far more than linguistic competence. Cultural norms, nonverbal cues, high-context versus low-context communication styles, and power dynamics all influence how messages are encoded and decoded across cultural boundaries. Studies in intercultural pragmatics reveal that miscommunication often arises not from vocabulary deficiencies but from differing assumptions about conversational norms, politeness strategies, and discourse organization. Language learners and professional communicators must develop cultural intelligence alongside linguistic proficiency to navigate these complex interpersonal landscapes successfully.',
'有效的跨文化沟通远不止于语言能力。文化规范、非语言线索、高语境与低语境交流风格以及权力动态，都影响着跨文化边界中信息的编码与解码方式。跨文化语用学研究揭示，沟通误解往往并非源于词汇匮乏，而是源于对会话规范、礼貌策略和话语组织方式存在不同假设。语言学习者和职业沟通者必须在提升语言能力的同时发展文化智识。',
'王文化'),

('全球化背景下的英语教学新趋势',
'The globalization of English as a lingua franca has fundamentally repositioned English language teaching (ELT) pedagogy. Contemporary approaches increasingly emphasize communication effectiveness over strict adherence to native-speaker norms, recognizing that English belongs to all its users rather than any particular national community. Task-based language teaching (TBLT), content and language integrated learning (CLIL), and translanguaging practices have gained prominence in international classrooms. Digital technologies have expanded learning opportunities through massive open online courses (MOOCs) and AI-powered language applications.',
'英语作为通用语的全球化已从根本上重新定位了英语语言教学（ELT）的教学法。当代方法越来越强调沟通有效性，而非严格遵循母语者规范，认为英语属于所有使用者而非任何特定国家社区。任务型语言教学（TBLT）、内容与语言融合教学（CLIL）以及跨语言实践在国际课堂中日益受到重视。数字技术通过大规模开放在线课程（MOOCs）和AI驱动的语言应用程序扩展了学习机会。',
'陈教授'),

('学术写作中的论证逻辑',
'Effective academic writing demands rigorous logical argumentation, evidence-based reasoning, and precise language use. A well-constructed academic argument typically follows an organizational pattern: a clear thesis statement articulating the author position, body paragraphs presenting supporting evidence with logical transitions, and a conclusion that synthesizes key points without merely restating the introduction. Common logical fallacies — including ad hominem attacks, straw man arguments, and hasty generalizations — undermine argumentative credibility and must be carefully avoided. Academic integrity requires proper attribution of sources through standardized citation systems.',
'有效的学术写作要求严格的逻辑论证、循证推理和精确的语言运用。结构良好的学术论证通常遵循以下组织模式：明确表达作者立场的论点陈述、提供支撑证据并配有逻辑过渡的正文段落，以及综合要点而非单纯重复引言的结论。常见的逻辑谬误——包括人身攻击、稻草人论证和草率概括——会削弱论证的可信度，必须谨慎避免。学术诚信要求通过标准化引用系统对来源进行适当归因。',
'刘学术'),

('莎士比亚作品中的语言艺术',
'William Shakespeare contribution to the English language extends far beyond his thirty-seven plays and 154 sonnets. Linguists estimate that Shakespeare coined approximately 1,700 words still in active use today, including bedroom, lonely, generous, and eyeball. His dramatic works introduced complex psychological characterization, poetic dialogue, and structural innovations that transformed theatrical conventions of the Elizabethan era. The soliloquy technique provided an unprecedented window into characters inner moral conflicts. Shakespeare influence on English idiom is pervasive: expressions such as break the ice, heart of gold, and wild goose chase all trace their origins to his works.',
'莎士比亚对英语语言的无与伦比的贡献远远超越了他的37部戏剧和154首十四行诗。语言学家估计，莎士比亚创造了约1700个至今仍在使用的词语，包括"卧室"、"孤独"、"慷慨"和"眼球"等。他的戏剧作品引入了复杂的心理刻画、诗意对话和结构创新，从而改变了伊丽莎白时代的戏剧惯例。独白技巧为观众了解人物内心道德冲突提供了前所未有的窗口。莎士比亚对英语习语的影响无处不在，诸多日常表达均可追溯至他的作品。',
'王文学');

INSERT INTO `biz_reading` (`title`, `content`, `difficulty`, `score`) VALUES
('城市化与可持续发展',
'Rapid urbanization presents both tremendous opportunities and formidable challenges for sustainable development. By 2050, an estimated 68% of the world population will reside in urban areas, placing enormous pressure on infrastructure, resources, and ecosystem services. Smart city initiatives leverage digital technologies including IoT sensors, big data analytics, and AI optimization to improve energy efficiency, reduce traffic congestion, and enhance public services.\n\n请回答以下问题：\n1. 到2050年，预计全球城市人口比例是多少？\n2. 智慧城市举措使用了哪些数字技术？\n3. 城市化带来了哪些主要挑战？',
'中等', 85),

('数字时代的阅读习惯变迁',
'The advent of digital media has fundamentally altered reading behaviors across all demographic groups. Research indicates that while overall reading rates have declined, digital reading has significantly increased, particularly among younger generations who prefer news aggregators, social media feeds, and e-books over traditional print media. Cognitive scientists distinguish between deep reading — the slow, immersive engagement with complex texts that builds critical thinking — and skimming, the rapid scanning behavior typical of online reading.\n\n请回答以下问题：\n1. 数字阅读呈现怎样的变化趋势？\n2. 认知科学家如何区分深度阅读与浏览？\n3. 教育工作者应如何应对这一趋势？',
'困难', 78),

('音乐与语言习得的关联',
'Emerging research in cognitive neuroscience reveals striking parallels between musical and linguistic processing in the human brain. Both music and language engage overlapping neural networks responsible for rhythm perception, syntactic processing, and working memory. Studies indicate that early musical training significantly enhances phonological awareness, a critical prerequisite for reading acquisition. Children who receive instrumental music instruction demonstrate superior performance on verbal memory tasks and show accelerated vocabulary development compared to non-musician peers.\n\n请回答以下问题：\n1. 音乐和语言在大脑中共享哪些神经网络功能？\n2. 早期音乐训练对语音意识有何影响？\n3. 这些发现对语言课程设计有何实际意义？',
'中等', 92),

('全球英语教育政策比较研究',
'National English language education policies reflect complex negotiations between global economic demands, local cultural preservation, and pedagogical philosophy. East Asian nations, including China, Japan, and South Korea, have invested substantially in expanding English education through compulsory school programs, standardized examinations, and teacher training initiatives. European countries emphasize multilingualism and communicative competence over grammatical accuracy under the Common European Framework of Reference for Languages.\n\n请回答以下问题：\n1. 文章提到了哪些东亚国家在英语教育方面的投入？\n2. 欧洲国家英语教育政策的侧重点是什么？\n3. 全球南方国家面临什么样的语言政策挑战？',
'困难', 70),

('英语商务谈判技巧',
'Successful business negotiation in English requires mastery of specialized discourse strategies, cultural sensitivity, and strategic communication techniques. Skilled negotiators employ a range of linguistic tools: open-ended questions to gather information, conditional structures to propose trade-offs, hedging language to maintain flexibility, and rapport-building small talk to establish interpersonal trust. Research identifies four predominant negotiation styles — competitive, collaborative, compromising, and avoiding — each associated with distinct linguistic patterns.\n\n请回答以下问题：\n1. 商务谈判中有效的语言工具有哪些？\n2. 文章提到了哪四种主要谈判风格？\n3. 成功跨文化谈判的关键是什么？',
'简单', 95),

('环境保护与英语写作体裁',
'Environmental advocacy has generated a rich body of English writing spanning scientific reports, policy documents, journalistic articles, and literary nonfiction. Each genre employs distinct rhetorical strategies calibrated to its intended audience and purpose. Scientific environmental writing prioritizes precision and evidence citation. Journalistic environmental writing blends scientific accuracy with compelling narrative to engage general readers. Environmental policy documents combine legal precision with persuasive argumentation. Literary environmental writing uses vivid descriptive language and metaphorical imagery to foster emotional connection to the natural world.\n\n请回答以下问题：\n1. 文章涉及哪几种环境写作体裁？\n2. 每种体裁使用的主要修辞策略有何不同？\n3. 掌握多种环境写作体裁对英语学习者有何益处？',
'简单', 88);

INSERT INTO `biz_listening` (`title`, `category`, `duration`, `audio_url`, `score`) VALUES
('BBC新闻：全球气候峰会最新进展',       'BBC新闻',   '03:45', 'https://onlinetestcase.com/wp-content/uploads/2023/06/100-KB-MP3.mp3', 88),
('托福听力模拟 — 大学讲座：心理学导论', 'TOEFL模拟', '06:20', 'https://onlinetestcase.com/wp-content/uploads/2023/06/100-KB-MP3.mp3', 75),
('雅思听力练习 — 租房对话场景',         'IELTS模拟', '04:15', 'https://onlinetestcase.com/wp-content/uploads/2023/06/100-KB-MP3.mp3', 82),
('TED演讲：语言如何塑造我们的思维',     'TED演讲',   '08:30', 'https://onlinetestcase.com/wp-content/uploads/2023/06/100-KB-MP3.mp3', 91),
('日常英语对话 — 在机场办理登机手续',   '日常口语',  '02:50', 'https://onlinetestcase.com/wp-content/uploads/2023/06/100-KB-MP3.mp3', 95),
('学术英语 — 研究方法论讲座节选',       '学术讲座',  '10:00', 'https://onlinetestcase.com/wp-content/uploads/2023/06/100-KB-MP3.mp3', NULL),
('CNN商业新闻 — 科技公司季报解读',      '商业英语',  '05:10', 'https://onlinetestcase.com/wp-content/uploads/2023/06/100-KB-MP3.mp3', 79),
('英式英语精听 — 爱丁堡旅游指南',       '英式英语',  '04:40', 'https://onlinetestcase.com/wp-content/uploads/2023/06/100-KB-MP3.mp3', 86);

INSERT INTO `biz_cloze` (`title`, `content`, `blanks_count`, `completion_status`, `score`) VALUES
('科技与生活 — 智能手机依赖',
'Modern smartphones have become (1)_____ tools in daily life, transforming how people (2)_____, work, and entertain themselves. Research suggests that the (3)_____ use of smartphones is associated with decreased attention spans and disrupted sleep (4)_____. However, when used (5)_____, these devices offer unprecedented access to information and productivity tools. The key lies in developing healthy digital (6)_____ that balance technological benefits with personal well-being.\n\n选项：A.indispensable B.communicate C.excessive D.patterns E.mindfully F.habits\n答案：1-A 2-B 3-C 4-D 5-E 6-F',
6, '已完成', 83),

('环境保护 — 碳中和目标',
'Global efforts to achieve carbon (1)_____ have accelerated in response to increasingly (2)_____ climate change impacts. Governments, corporations, and (3)_____ are pledging to reduce greenhouse gas (4)_____ through renewable energy (5)_____. Scientists warn that without (6)_____ action within this decade, limiting global warming to 1.5 degrees Celsius becomes increasingly (7)_____.\n\n选项：A.neutrality B.severe C.individuals D.emissions E.transition F.decisive G.unlikely\n答案：1-A 2-B 3-C 4-D 5-E 6-F 7-G',
7, '已完成', 91),

('文化交流 — 语言学习动机',
'Researchers have identified two primary types of motivation in second language (1)_____: integrative motivation, which involves a desire to connect with the (2)_____ of the target language, and (3)_____ motivation, which focuses on practical (4)_____ such as career advancement. Studies suggest that learners with strong (5)_____ motivation tend to achieve higher levels of (6)_____ and maintain learning over longer periods. Creating meaningful cultural (7)_____ opportunities can significantly enhance integrative motivation.\n\n选项：A.acquisition B.culture C.instrumental D.benefits E.integrative F.proficiency G.exchange\n答案：1-A 2-B 3-C 4-D 5-E 6-F 7-G',
7, '已完成', 78),

('商务英语 — 职场沟通',
'Effective workplace (1)_____ requires clarity, (2)_____, and cultural (3)_____. In professional settings, emails should maintain a formal yet approachable (4)_____, use clear (5)_____ headings, and include a specific call to (6)_____. Meetings benefit from structured (7)_____, clear time management, and inclusive participation that ensures all voices are (8)_____.\n\n选项：A.communication B.conciseness C.sensitivity D.tone E.subject F.action G.agendas H.heard\n答案：1-A 2-B 3-C 4-D 5-E 6-F 7-G 8-H',
8, '已完成', 96),

('学术英语 — 研究论文写作',
'Writing an effective academic research paper requires (1)_____ planning and structured (2)_____. The introduction should clearly state the research (3)_____ and provide relevant (4)_____ information. The literature (5)_____ synthesizes existing research to identify gaps that the study addresses. The (6)_____ section details the research design and (7)_____ procedures. Results are reported (8)_____, followed by a discussion that interprets findings in relation to the research questions.\n\n选项：A.careful B.execution C.question D.background E.review F.methodology G.analysis H.objectively\n答案：1-A 2-B 3-C 4-D 5-E 6-F 7-G 8-H',
8, '进行中', NULL),

('新闻英语 — 媒体素养',
'Media (1)_____ has become an essential skill in the digital age, where information spreads (2)_____ across social platforms. Critical readers must evaluate sources for (3)_____, check for (4)_____ bias, and cross-reference claims with multiple reliable sources. The rise of (5)_____ news and AI-generated content makes these skills increasingly (6)_____. Educational programs increasingly integrate media literacy into (7)_____ to prepare students for informed citizenship in the modern world.\n\n选项：A.literacy B.instantly C.credibility D.ideological E.fabricated F.critical G.curricula\n答案：1-A 2-B 3-C 4-D 5-E 6-F 7-G',
7, '未开始', NULL);

INSERT INTO `biz_oral` (`topic`, `score`, `attempts`) VALUES
('介绍你的家乡及其文化特色', 87, 3),
('描述一次难忘的旅行经历', 82, 2),
('谈谈你对人工智能未来发展的看法', 79, 4),
('如何平衡学业压力与个人生活', 91, 2),
('描述一位对你影响最大的人物', 85, 3),
('就环境保护发表你的观点', 76, 5),
('分析中西方教育体系的异同', 88, 2),
('描述你理想中的职业与未来规划', NULL, 1);

INSERT INTO `biz_discuss` (`user_id`, `content`, `target_type`, `target_id`) VALUES
(3, '这个词 ambiguous 确实很难记，我用联想记忆法效果不错，推荐给大家！', 'VOCAB', 3),
(4, '建议老师在词汇表里多加一些例句，光看定义很难理解词语在语境中的使用。', 'VOCAB', 3),
(3, '虚拟语气那节讲得超清楚！我之前一直混淆If从句的时态，看完这篇解析豁然开朗了。', 'GRAMMAR', 1),
(5, '关于完成时那篇解析，能不能补充将来完成进行时的用法？实际写作中也很常见。', 'GRAMMAR', 3),
(6, '人工智能对高等教育那篇文献读完很有收获，建议老师布置配套的讨论式作业，深化理解。', 'LIT', 1),
(3, '莎士比亚那篇文献对词源分析太有意思了！没想到这么多日常用语都来自他的作品。', 'LIT', 6),
(4, '情态动词辨析这个盘点做得很系统，以前总是搞不清must和have to的区别，现在清楚多了。', 'GRAMMAR', 4),
(5, '跨文化交际那篇文献对我准备留学面试很有帮助，建议加入更多实际案例分析。', 'LIT', 3),
(6, '定语从句的限定性和非限定性区分，老师如果能多出几道辨析练习题就更好了。', 'GRAMMAR', 2),
(3, '我用学习记录功能追踪了自己一个月的词汇学习进度，进步很明显，这个功能设计得很好！', 'VOCAB', 5);

INSERT INTO `biz_learning_record` (`user_id`, `type`, `target_id`, `duration`, `score`) VALUES
(3, 'VOCAB',    3,  720, NULL),
(3, 'VOCAB',    4,  540, NULL),
(3, 'GRAMMAR',  1, 1800, NULL),
(3, 'GRAMMAR',  3, 1200, NULL),
(3, 'LIT',      1, 3600, NULL),
(3, 'READING',  1, 2400, 85),
(3, 'LISTENING',1, 1350, 78),
(3, 'CLOZE',    1,  600, 92),
(3, 'ORAL',     1, 1800, 88),
(4, 'VOCAB',    5,  480, NULL),
(4, 'VOCAB',    8,  360, NULL),
(4, 'GRAMMAR',  2, 1500, NULL),
(4, 'LIT',      2, 2700, NULL),
(4, 'READING',  3, 1800, 76),
(5, 'VOCAB',    6,  420, NULL),
(5, 'GRAMMAR',  5,  900, NULL),
(5, 'LIT',      4, 3000, NULL),
(5, 'LISTENING',3, 2550, 81),
(6, 'VOCAB',    7,  600, NULL),
(6, 'GRAMMAR',  7, 1200, NULL),
(6, 'READING',  5, 2100, 90),
(6, 'ORAL',     3,  900, 72);

-- 题库模板（/generate 随机抽题来源）
INSERT INTO `biz_question_bank` (`module_type`, `title`, `content`, `difficulty`, `questions_json`, `answers_json`, `status`) VALUES
('READING', '城市化与可持续发展',
'Rapid urbanization presents both tremendous opportunities and formidable challenges for sustainable development. By 2050, an estimated 68% of the world population will reside in urban areas, placing enormous pressure on infrastructure, resources, and ecosystem services. Smart city initiatives leverage digital technologies including IoT sensors, big data analytics, and AI optimization to improve energy efficiency, reduce traffic congestion, and enhance public services.',
'Medium',
'[{"question":"By 2050, what percentage of the world population is expected to live in urban areas?","options":[{"label":"A. 50%","value":"A"},{"label":"B. 68%","value":"B"},{"label":"C. 75%","value":"C"},{"label":"D. 80%","value":"D"}]},{"question":"Which technologies are mentioned as part of smart city initiatives?","options":[{"label":"A. IoT sensors and big data analytics","value":"A"},{"label":"B. Nuclear power and fossil fuels","value":"B"},{"label":"C. Traditional agriculture only","value":"C"},{"label":"D. Manual record keeping","value":"D"}]}]',
'[{"correct":"B","explanation":"The passage explicitly states that 68% of the world population will reside in urban areas by 2050."},{"correct":"A","explanation":"Smart city initiatives use IoT sensors, big data analytics, and AI optimization according to the passage."}]',
'Active'),

('READING', '数字时代的阅读习惯变迁',
'The advent of digital media has fundamentally altered reading behaviors across all demographic groups. Research indicates that while overall reading rates have declined, digital reading has significantly increased, particularly among younger generations who prefer news aggregators, social media feeds, and e-books over traditional print media. Cognitive scientists distinguish between deep reading — the slow, immersive engagement with complex texts that builds critical thinking — and skimming, the rapid scanning behavior typical of online reading.',
'Hard',
'[{"question":"What trend does the passage describe regarding digital reading?","options":[{"label":"A. It has significantly increased","value":"A"},{"label":"B. It has completely disappeared","value":"B"},{"label":"C. It is only popular among older adults","value":"C"},{"label":"D. It has replaced all print media","value":"D"}]},{"question":"How do cognitive scientists define deep reading?","options":[{"label":"A. Rapid scanning of online content","value":"A"},{"label":"B. Slow, immersive engagement with complex texts","value":"B"},{"label":"C. Reading only social media feeds","value":"C"},{"label":"D. Skimming headlines quickly","value":"D"}]}]',
'[{"correct":"A","explanation":"The passage states digital reading has significantly increased despite overall reading rate decline."},{"correct":"B","explanation":"Deep reading is defined as slow, immersive engagement that builds critical thinking."}]',
'Active'),

('READING', '音乐与语言习得的关联',
'Emerging research in cognitive neuroscience reveals striking parallels between musical and linguistic processing in the human brain. Both music and language engage overlapping neural networks responsible for rhythm perception, syntactic processing, and working memory. Studies indicate that early musical training significantly enhances phonological awareness, a critical prerequisite for reading acquisition.',
'Medium',
'[{"question":"What neural functions do music and language share according to the passage?","options":[{"label":"A. Rhythm perception and syntactic processing","value":"A"},{"label":"B. Visual processing only","value":"B"},{"label":"C. Motor coordination exclusively","value":"C"},{"label":"D. Long-term memory storage only","value":"D"}]},{"question":"What effect does early musical training have?","options":[{"label":"A. It reduces vocabulary size","value":"A"},{"label":"B. It enhances phonological awareness","value":"B"},{"label":"C. It has no effect on reading","value":"C"},{"label":"D. It replaces language learning","value":"D"}]}]',
'[{"correct":"A","explanation":"Both engage networks for rhythm perception, syntactic processing, and working memory."},{"correct":"B","explanation":"Early musical training significantly enhances phonological awareness, a prerequisite for reading."}]',
'Active');

INSERT INTO `biz_question_bank` (`module_type`, `title`, `content`, `category`, `duration`, `audio_url`, `questions_json`, `answers_json`, `status`) VALUES
('LISTENING', '日常英语 — 软件更新对话', 'M: Have you tried out the new software update they pushed yesterday?\nW: Yes, I have! The interface is so much cleaner and easier to use.',
'日常口语', '02:30', 'https://onlinetestcase.com/wp-content/uploads/2023/06/100-KB-MP3.mp3',
'[{"question":"What are the speakers discussing?","options":[{"label":"A. A weekend trip","value":"A"},{"label":"B. A new software program","value":"B"},{"label":"C. A job interview","value":"C"},{"label":"D. Dinner plans","value":"D"}]},{"question":"How does the woman feel about the update?","options":[{"label":"A. She thinks it is confusing","value":"A"},{"label":"B. She loves the new interface","value":"B"},{"label":"C. She has not downloaded it yet","value":"C"},{"label":"D. She wants to revert to the old version","value":"D"}]}]',
'[{"correct":"B","explanation":"The male speaker mentions trying out a new software update.","transcript":"M: Have you tried out the new software update they pushed yesterday?"},{"correct":"B","explanation":"The woman praises the cleaner and easier interface.","transcript":"W: Yes, I have! The interface is so much cleaner and easier to use."}]',
'Active'),

('LISTENING', 'TED演讲 — 语言与思维', 'Language is not merely a tool for communication — it fundamentally shapes how we perceive and categorize the world around us. Different languages encode concepts differently, influencing speakers cognitive frameworks.',
'TED演讲', '08:30', 'https://onlinetestcase.com/wp-content/uploads/2023/06/100-KB-MP3.mp3',
'[{"question":"What is the main idea of the talk?","options":[{"label":"A. Language shapes how we think","value":"A"},{"label":"B. All languages are identical","value":"B"},{"label":"C. Translation is impossible","value":"C"},{"label":"D. Grammar rules are universal","value":"D"}]},{"question":"How do different languages differ according to the speaker?","options":[{"label":"A. They encode concepts differently","value":"A"},{"label":"B. They use the same vocabulary","value":"B"},{"label":"C. They have no cultural influence","value":"C"},{"label":"D. They only differ in pronunciation","value":"D"}]}]',
'[{"correct":"A","explanation":"The talk argues language shapes perception and categorization of the world.","transcript":"Language is not merely a tool for communication — it fundamentally shapes how we perceive..."},{"correct":"A","explanation":"Different languages encode concepts differently, influencing cognitive frameworks.","transcript":"Different languages encode concepts differently..."}]',
'Active'),

('LISTENING', 'IELTS模拟 — 租房对话', 'Agent: Good morning, I have a two-bedroom flat available near the university.\nStudent: That sounds perfect. What is the monthly rent and does it include utilities?',
'IELTS模拟', '04:15', 'https://onlinetestcase.com/wp-content/uploads/2023/06/100-KB-MP3.mp3',
'[{"question":"What type of accommodation is being discussed?","options":[{"label":"A. A two-bedroom flat","value":"A"},{"label":"B. A studio apartment","value":"B"},{"label":"C. A shared dormitory","value":"C"},{"label":"D. A hotel room","value":"D"}]},{"question":"What does the student want to know?","options":[{"label":"A. The monthly rent and utilities","value":"A"},{"label":"B. The landlord name only","value":"B"},{"label":"C. The building age","value":"C"},{"label":"D. The number of floors","value":"D"}]}]',
'[{"correct":"A","explanation":"The agent offers a two-bedroom flat near the university.","transcript":"I have a two-bedroom flat available near the university."},{"correct":"A","explanation":"The student asks about monthly rent and whether utilities are included.","transcript":"What is the monthly rent and does it include utilities?"}]',
'Active');

INSERT INTO `biz_question_bank` (`module_type`, `title`, `content`, `questions_json`, `answers_json`, `status`) VALUES
('CLOZE', '语言学习基础',
'Learning a foreign language (1)_____ a lot of time and practice. However, it is very rewarding. You can communicate with people from other (2)_____ and understand their culture better.',
'[{"blankIndex":1,"options":["takes","spends","costs","pays"]},{"blankIndex":2,"options":["cities","countries","villages","towns"]}]',
'[{"correct":"takes","explanation":"take time 意为花费时间，主语为动名词 Learning。"},{"correct":"countries","explanation":"与 foreign language 和 culture 搭配，countries 最恰当。"}]',
'Active'),

('CLOZE', '科技与生活 — 智能手机依赖',
'Modern smartphones have become (1)_____ tools in daily life, transforming how people (2)_____, work, and entertain themselves. Research suggests that the (3)_____ use of smartphones is associated with decreased attention spans.',
'[{"blankIndex":1,"options":["indispensable","optional","temporary","minor"]},{"blankIndex":2,"options":["communicate","calculate","negotiate","compete"]},{"blankIndex":3,"options":["excessive","moderate","minimal","occasional"]}]',
'[{"correct":"indispensable","explanation":"智能手机已成为日常生活中不可或缺的工具。"},{"correct":"communicate","explanation":"人们用手机交流、工作和娱乐。"},{"correct":"excessive","explanation":"过度使用智能手机与注意力下降相关。"}]',
'Active'),

('CLOZE', '环境保护 — 碳中和目标',
'Global efforts to achieve carbon (1)_____ have accelerated in response to increasingly (2)_____ climate change impacts. Governments and corporations are pledging to reduce greenhouse gas (3)_____.',
'[{"blankIndex":1,"options":["neutrality","stability","efficiency","diversity"]},{"blankIndex":2,"options":["severe","minor","temporary","predictable"]},{"blankIndex":3,"options":["emissions","production","consumption","investment"]}]',
'[{"correct":"neutrality","explanation":"carbon neutrality 碳中和是固定搭配。"},{"correct":"severe","explanation":"气候变化影响日益严重。"},{"correct":"emissions","explanation":"greenhouse gas emissions 温室气体排放。"}]',
'Active');
