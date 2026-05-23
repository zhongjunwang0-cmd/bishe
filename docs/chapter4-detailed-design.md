# 第四章 详细设计与实现

> **使用说明**：将本文粘贴至 Word 后，按学校格式设置标题（黑体/宋体、行距、页眉页脚）。文中 `【图4-x】` 为界面截图占位，请本地运行系统后截取对应页面替换。Mermaid 图源文件见 `docs/figures/fig-4-*.md`。

---

## 4.1 系统总体设计

### 4.1.1 设计目标与原则

在第三章需求分析的基础上，本章从**可实现的工程视角**阐述 Brix 英语学习系统的详细设计。设计遵循以下原则：

1. **前后端分离**：表现层（Vue 3）与业务层（Spring Boot）通过 REST/JSON 通信，便于独立开发与部署。
2. **领域分层**：后端按 Controller → Service → Mapper 组织，实体与 DTO 分离，统一使用 `Result<T>` 封装响应。
3. **智能服务解耦**：深度学习推理部署于独立 `ai-service`（FastAPI），Java 端仅做 HTTP 代理与结果聚合，避免 JVM 加载 PyTorch 带来的资源与版本冲突。
4. **数据驱动个性化**：学习记录、生词本状态、周计划任务均持久化至 MySQL；高频只读列表辅以 Redis 缓存。

系统逻辑架构已在第三章给出（图 3-1）。本章重点说明**各模块的内部结构、关键算法、接口契约与界面实现**。

### 4.1.2 工程目录与分层结构

| 层次 | 技术栈 | 主要目录 | 职责 |
|------|--------|----------|------|
| 表现层 | Vue 3 + Vite + Element Plus | `frontend/src/views`、`router` | 页面交互、路由、Axios 调用 API |
| 业务层 | Spring Boot 3 + Shiro + MyBatis-Plus | `backend/.../controller`、`service`、`mapper` | 鉴权、业务逻辑、持久化 |
| 数据层 | MySQL 8、Redis 7 | `mysql/init` | 表结构、种子数据、迁移脚本 |
| 智能层 | FastAPI + PyTorch 模型 | `ai-service/app`、`ml/models` | DKT 推荐、语法纠错、发音评测 |

后端包结构按功能划分为：`controller`（REST 入口）、`service`/`service.impl`（业务）、`mapper`（数据访问）、`entity`（表映射）、`dto`（传输对象）、`config`（Shiro、缓存、RestTemplate）、`util`（艾宾浩斯、计分、JSON 等工具类）。

### 4.1.3 统一接口规范

所有业务 API 前缀为 `/api`，响应体采用统一结构：

```java
// backend/.../common/Result.java
public class Result<T> {
    private Integer code;    // 200 成功，500 等业务错误
    private String message;
    private T data;
}
```

前端 Axios 拦截器根据 `code` 判断成功与否；Shiro 在过滤器链中对 `/api/**` 做会话校验，未登录请求返回 401。管理端接口额外要求 `ADMIN` 或 `TEACHER` 角色。

---

## 4.2 数据库详细设计

### 4.2.1 概念模型

系统数据围绕**用户—学习内容—学习行为**三类实体展开：

- **用户域**：`sys_user`、`sys_role`、`sys_permission` 及关联表，支撑 RBAC。
- **内容域**：`biz_vocab`、`biz_grammar`、`biz_literature`、`biz_question_bank` 及由题库生成的 `biz_reading`、`biz_listening`、`biz_cloze`、`biz_oral`。
- **行为域**：`biz_user_vocab`（生词本与复习状态）、`biz_learning_record`（各模块学习/测验记录）、`biz_learning_plan_item`（周计划任务）、`biz_discuss`（讨论）。

### 4.2.2 核心表说明

**（1）用户表 `sys_user`**

存储账号、BCrypt 密码摘要、昵称、英语水平 `level`、目标考试 `target_exam`、每日目标分钟数 `daily_goal` 等，为周计划模板与 AI 建议提供画像输入。

**（2）生词本表 `biz_user_vocab`**

| 字段 | 类型 | 说明 |
|------|------|------|
| user_id, vocab_id | BIGINT | 联合唯一，标识用户收藏的单词 |
| mastery_level | INT | 掌握度 0–100 |
| review_stage | INT | 艾宾浩斯阶段 0–6 |
| status | VARCHAR | NEW / LEARNING / REVIEWING / MASTERED |
| next_review_time | DATETIME | 下次应复习时刻 |

**（3）题库模板表 `biz_question_bank`**

教师维护的统一题库，`module_type` 取 READING / LISTENING / CLOZE；`questions_json`、`answers_json` 以 JSON 存储题目与标准答案。学习者调用 `/generate` 时由服务随机抽题并写入对应业务表，实现**一次生成、多次作答、成绩回写**。

**（4）学习记录表 `biz_learning_record`**

字段 `type` 标识模块（词汇、阅读、听力等），`target_id` 关联具体资源，`duration` 记录时长（秒），`score` 记录得分。该表是统计看板、DKT 薄弱模块推荐的数据源。

**（5）周计划表 `biz_learning_plan_item`**

按用户、`week_start`（周一日期）、`plan_date` 存储每日任务；`source` 区分 `template`（档案模板）与 `ai_recommend`（AI 同步写入），`completed` 标记完成情况。

### 4.2.3 索引与完整性

- 生词本：`uk_user_vocab(user_id, vocab_id)` 防止重复收藏。
- 周计划：`idx_user_week`、`idx_user_date` 加速按周、按日查询。
- 外键逻辑在应用层通过 Service 校验（如删除词汇前检查生词本引用），保持迁移脚本简洁。

---

## 4.3 后端模块设计与实现

### 4.3.1 认证与权限模块

**设计**：采用 Apache Shiro，`UserRealm` 完成认证与角色加载；密码使用 BCrypt 比对。

```java
// UserRealm.java — 按 role_id 映射角色
if (user.getRoleId() == 1L) info.addRole("ADMIN");
else if (user.getRoleId() == 2L) info.addRole("TEACHER");
else info.addRole("USER");
```

**流程**：用户 POST `/api/user/login` → `UserController` 调用 `Subject.login` → 会话写入 Cookie → 后续请求携带会话 ID → `SecurityUtils.getSubject().getPrincipal()` 获取当前 `User` 实体。

**【图4-1 建议】** 登录界面截图（`Login.vue`）。

### 4.3.2 词汇与生词本模块

**模块职责**

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/vocab/book/list` | GET | 生词本列表 |
| `/api/vocab/book/add/{vocabId}` | POST | 加入生词本 |
| `/api/vocab/review/today` | GET | 今日待复习 |
| `/api/vocab/review/submit` | POST | 提交复习结果 |

**艾宾浩斯算法**（`EbbinghausUtil`）：复习间隔序列为 **1 → 2 → 4 → 7 → 15 → 30** 天。用户点击「记得」则阶段 +1、掌握度 +15；「忘记」则阶段回退、掌握度 −20。当掌握度 ≥ 85 且阶段 ≥ 4 时状态置为 `MASTERED`。

```java
// EbbinghausUtil.java 核心常量
private static final int[] INTERVAL_DAYS = {1, 2, 4, 7, 15, 30};

public static LocalDateTime nextReviewTime(int stage) {
    return LocalDateTime.now().plusDays(intervalDaysForStage(stage));
}
```

**待复习查询**（`UserVocabServiceImpl.listDueReview`）：筛选 `next_review_time <= now` 且 `status != MASTERED` 的记录，按下次复习时间升序返回。

**复习提交**（`submitReview`）：根据 `remembered` 更新 stage、mastery、status、`next_review_time`，并递增 `review_count`。

控制器层保持薄封装，例如 `VocabReviewController` 仅从 Shiro 取当前用户 ID 后委派 Service。

### 4.3.3 测评类模块（阅读 / 听力 / 填空）

三类模块共享**题库抽题—作答—自动计分—写入学习记录**模式，以阅读为例：

1. `POST /api/reading/generate`：`QuestionGenerateService` 从 `biz_question_bank` 随机选取 Active 模板，解析 JSON 生成 `biz_reading` 实例。
2. `GET /api/reading/{id}`：返回文章与 `questions`（由 `questions_json` 解析）。
3. `POST /api/reading/{id}/submit`：`ScoreUtil.calculateScore` 对比用户答案与 `answers_json`，回写 `score` 并记录 `LearningRecord`。

听力模块增加 `audio_url` 字段供前端 `<audio>` 播放；填空模块在 `content` 中预留空位，题目 JSON 描述各空选项。

### 4.3.4 学习记录与统计模块

`RecordController` 提供分页查询与汇总；`StatsController` 聚合各模块练习次数、平均分、近 7 日趋势，供首页 ECharts 图表使用。记录类型通过 `LearningRecordTypeUtil` 映射到 DKT 所需的 VOCAB、GRAMMAR、READING、LISTENING、ORAL 五类。

### 4.3.5 AI 代理模块

`AiServiceImpl` 承担三类能力：

1. **薄弱模块推荐**：读取近 30 天 `biz_learning_record` → 构造 `ModuleStatDto`、`LearningEventDto` → HTTP 调用 `ai-service` `/api/kt/recommend`（LSTM-DKT）；失败时降级为规则推荐，并调用 `learningPlanService.syncAiRecommendToPlan` 写入周计划。
2. **语法纠错**：优先 T5-GEC，不可用时回退规则分析（`EnglishWritingAnalyzer`）。
3. **AI 问答**：读取 `sys_config` 中的 LLM Key，调用 OpenAI 兼容接口；未配置则使用内置规则回复。

```java
// AiServiceImpl.getLearningRecommend 片段逻辑
List<LearningRecord> records = learningRecordService.list(
    qw.eq("user_id", userId).ge("create_time", since));
KtRecommendDto result = aiModelClient.recommend(moduleStats, events, String.valueOf(userId));
learningPlanService.syncAiRecommendToPlan(userId, result);
```

### 4.3.6 管理后台模块

`AdminContentController`、`AdminUserController`、`AdminSystemController` 等提供题库 CRUD、用户管理、系统配置（AI Key、语音 API）。写操作触发 `@CacheEvict` 清理 Redis 中的列表缓存，保证学习者侧读到最新内容。

---

## 4.4 前端界面设计与实现

### 4.4.1 路由与布局

`frontend/src/router/index.ts` 定义学习前台与管理后台路由。主布局 `Layout.vue` 含侧边栏菜单，按 Shiro 返回的角色动态显示「管理」入口。

| 路由 | 组件 | 功能 |
|------|------|------|
| `/` | Home.vue | 首页看板、周计划摘要、快捷入口 |
| `/vocab` | VocabLearning.vue | 词库、生词本、闪卡复习 |
| `/reading` | ReadingPractice.vue | 阅读测验 |
| `/listening` | ListeningTraining.vue | 听力播放与答题 |
| `/cloze` | ClozePractice.vue | 选词填空 |
| `/oral` | OralPractice.vue | 口语录音与评测 |
| `/grammar` | GrammarList.vue / GrammarTest.vue | 语法学习与测验 |
| `/ai-tutoring` | AiTutoring.vue | AI 辅导与写作纠错 |
| `/profile` | PersonalCenter.vue | 档案、周计划、生词统计 |
| `/admin/*` | AdminDashboard 等 | 教师/管理员维护 |

### 4.4.2 词汇学习界面

`VocabLearning.vue` 采用 **Element Plus 标签页** 组织三个子功能：

1. **词库浏览**：表格展示单词、音标、释义、难度；支持 Web Speech API 点击发音；「加入生词本」调用 `POST /api/vocab/book/add/{id}`。
2. **我的生词本**：`el-progress` 展示掌握度；显示复习阶段、状态标签、下次复习时间。
3. **今日复习**：闪卡式 UI，正面单词、背面释义，用户选择「记得/忘记」后调用 `review/submit`，进度条显示本轮完成比例。

界面顶部展示统计卡片（总数、今日待复习、已掌握、学习中），并标注艾宾浩斯间隔说明，与后端算法一致。

**【图4-2 建议】** 词汇学习—词库浏览 Tab 截图。  
**【图4-3 建议】** 词汇学习—今日复习闪卡界面截图。

### 4.4.3 首页与学习看板

`Home.vue` 根据角色展示不同欢迎语与统计项：学习者可见学习时长、本周计划进度条、今日任务列表（含完成勾选）；教师/管理员侧重内容与用户管理快捷入口。中部 ECharts 折线图展示近阶段各模块学习量。

**【图4-4 建议】** 学习者首页（含周计划与图表）截图。

### 4.4.4 测评类界面

阅读/听力/填空页面统一模式：**生成试卷 → 展示题干与选项 → 提交 → 显示得分与解析**。听力页在题干上方嵌入音频播放器；口语页使用 `MediaRecorder` 录制 webm，通过 `FormData` 上传至后端再转发 `ai-service`。

**【图4-5 建议】** 阅读理解答题界面截图。  
**【图4-6 建议】** 口语练习与发音评分结果截图。

### 4.4.5 个人中心与 AI 辅导

`PersonalCenter.vue` 编辑 level、target_exam、daily_goal，管理周计划任务勾选状态。`AiTutoring.vue` 提供对话区与写作纠错文本框，展示 T5 纠错结果与 DKT 推荐薄弱模块。

**【图4-7 建议】** AI 辅导页（推荐 + 对话）截图。

### 4.4.6 管理后台界面

`AdminDashboard.vue` 为管理入口；`TestManagement.vue` 维护 `biz_question_bank` JSON 题目；`UserManagement.vue` 重置密码与角色。界面采用 Element Plus 表格 + 对话框表单，与后端 Admin 系列 API 一一对应。

**【图4-8 建议】** 题库管理界面截图。

---

## 4.5 智能推理服务设计与实现

### 4.5.1 服务架构

`ai-service` 基于 FastAPI 启动，默认端口 8000。启动时可选预加载 Whisper 模型（后台线程），避免首次口语评测长时间阻塞。

暴露三类 REST 接口：

| 端点 | 模型 | 输入 | 输出 |
|------|------|------|------|
| `POST /api/kt/recommend` | LSTM-DKT（EdNet 训练） | 各模块准确率、尝试次数、事件序列 | weak_modules、today_tasks、mastery |
| `POST /api/grammar/correct` | T5-GEC | 英文文本 | corrected、issues |
| `POST /api/pronunciation/score` | Whisper + WER | 参考文本 + 音频文件 | score、wer、transcript、feedback |

推荐链路优先级：**DKT → GBDT 备用模型 → 规则排序**，保证无模型文件时仍可返回合理建议。

### 4.5.2 与 Java 后端的协作

Spring Boot 通过 `RestTemplate`/`AiModelClient` 配置项 `ai.service.base-url` 调用上述接口。Docker Compose 中将 `ai-service` 与 `backend` 置于同一网络，后端环境变量指向服务名 `ai-service:8000`。

---

## 4.6 关键业务流程

### 4.6.1 词汇复习流程

见图 4-9（序列图源文件：`docs/figures/fig-4-1-vocab-review-sequence.md`）。

1. 用户打开「今日复习」→ 前端 `GET /api/vocab/review/today`。
2. 后端查询 `biz_user_vocab` 中到期记录并返回 VO 列表。
3. 用户翻转闪卡并选择记得/忘记 → `POST /api/vocab/review/submit`。
4. `UserVocabServiceImpl` 调用 `EbbinghausUtil` 更新阶段与 `next_review_time`。
5. 前端更新队列，全部完成后展示「今日复习完成」结果页。

### 4.6.2 阅读测验流程

见图 4-10（`docs/figures/fig-4-2-reading-exam-sequence.md`）。

1. 用户点击生成 → `POST /api/reading/generate` 写入新 `biz_reading`。
2. 加载详情答题 → `POST /api/reading/{id}/submit` 计分并写 `biz_learning_record`。
3. 统计数据供首页图表与 DKT 使用。

### 4.6.3 AI 个性化推荐流程

1. 用户进入 AI 辅导或首页触发推荐 → `AiController` → `AiServiceImpl.getLearningRecommend`。
2. 聚合学习记录 → 调用 `ai-service` DKT。
3. 将 `today_tasks` 同步至 `biz_learning_plan_item`（`source=ai_recommend`）。
4. 前端在个人中心/首页展示推荐任务。

---

## 4.7 部署与运行实现

系统支持 Docker Compose 一键启动：MySQL、Redis、backend、frontend（Nginx 静态资源 + 反向代理）、ai-service。详细步骤见 `docs/DEPLOYMENT.md`。生产环境下前端构建产物由 Nginx 托管，API 请求代理至 Spring Boot，保证浏览器同源策略下会话 Cookie 正常传递。

---

## 4.8 本章小结

本章在需求分析基础上，完成了数据库表结构、后端分层模块、前端页面组织及 AI 推理服务的详细设计说明，并给出了词汇复习、阅读测验、智能推荐等核心流程的实现要点与关键代码。界面部分需在系统运行环境中补充实际截图，以满足论文对「设计与实现」的篇幅与可视化要求（建议全章不少于 5 页，含 6–8 张界面图与 2–3 张流程/结构图）。

---

## 附录：论文章节图表清单

| 编号 | 建议图题 | 来源 |
|------|----------|------|
| 图 4-1 | 用户登录界面 | 运行系统截图 Login |
| 图 4-2 | 词汇词库浏览界面 | VocabLearning 浏览 Tab |
| 图 4-3 | 词汇闪卡复习界面 | VocabLearning 复习 Tab |
| 图 4-4 | 学习者首页看板 | Home.vue |
| 图 4-5 | 阅读理解测验界面 | ReadingPractice |
| 图 4-6 | 口语发音评测结果 | OralPractice |
| 图 4-7 | AI 辅导界面 | AiTutoring |
| 图 4-8 | 题库管理界面 | TestManagement |
| 图 4-9 | 词汇复习流程序列图 | fig-4-1-vocab-review-sequence.md |
| 图 4-10 | 阅读测验流程序列图 | fig-4-2-reading-exam-sequence.md |
| 图 4-11 | 后端分层结构图（可选） | fig-4-3-backend-layers.md |
