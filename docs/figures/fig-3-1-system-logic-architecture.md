# 图 3-1 系统逻辑总体架构图

> **论文图题**：图 3-1 英语学习系统逻辑总体架构  
> **导出 PNG**：在项目根目录执行  
> `npx -y @mermaid-js/mermaid-cli -i docs/figures/fig-3-1-system-logic-architecture.mmd -o docs/figures/fig-3-1-system-logic-architecture.png -b white -w 1200`  
> 或粘贴下方代码到 [Mermaid Live Editor](https://mermaid.live) 导出。

## 完整版（论文推荐）

```mermaid
flowchart TB
    subgraph L1["用户层"]
        U1["学习者"]
        U2["教师"]
        U3["管理员"]
    end

    subgraph L2["表现层"]
        direction TB
        FE1["学习前台<br/>词汇 · 语法 · 阅读 · 听力 · 填空 · 口语"]
        FE2["个人中心<br/>生词本 · 周计划 · 学习记录 · AI 辅导"]
        FE3["管理后台<br/>题库 · 内容维护 · 用户 · 系统配置"]
    end

    subgraph L3["业务服务层"]
        direction TB
        S1["认证与权限<br/>登录 · RBAC · 会话"]
        S2["学习内容<br/>词汇 · 语法 · 文献"]
        S3["测评练习<br/>抽题 · 判分 · 计分"]
        S4["学习跟踪<br/>记录 · 统计 · 周计划"]
        S5["社区互动<br/>讨论评论"]
        S6["AI 代理<br/>请求转发 · 结果封装"]
        S7["系统管理<br/>用户 · 配置 · 工具检测"]
    end

    subgraph L4["数据层"]
        DB1[("MySQL<br/>用户 · 题库 · 记录 · 计划")]
        DB2[("Redis<br/>列表缓存")]
    end

    subgraph L5["智能推理层"]
        AI0["ai-service"]
        AI1["LSTM-DKT<br/>薄弱模块推荐"]
        AI2["T5-GEC<br/>语法纠错"]
        AI3["Whisper-WER<br/>发音评测"]
    end

    U1 --> FE1
    U1 --> FE2
    U2 --> FE3
    U3 --> FE3

    FE1 --> S1
    FE1 --> S2
    FE1 --> S3
    FE2 --> S4
    FE2 --> S6
    FE3 --> S7
    FE1 --> S5

    S1 --> DB1
    S2 --> DB1
    S2 --> DB2
    S3 --> DB1
    S4 --> DB1
    S5 --> DB1
    S7 --> DB1

    S6 -->|"HTTP / JSON"| AI0
    AI0 --> AI1
    AI0 --> AI2
    AI0 --> AI3

    classDef user fill:#f3f4f6,stroke:#374151
    classDef ui fill:#dbeafe,stroke:#2563eb
    classDef svc fill:#dcfce7,stroke:#16a34a
    classDef data fill:#e0e7ff,stroke:#4f46e5
    classDef ai fill:#fef3c7,stroke:#d97706

    class U1,U2,U3 user
    class FE1,FE2,FE3 ui
    class S1,S2,S3,S4,S5,S6,S7 svc
    class DB1,DB2 data
    class AI0,AI1,AI2,AI3 ai
```

## 图注（可写入论文）

1. **用户层**：三类角色共用同一 Web 入口，按 RBAC 展示不同菜单。  
2. **表现层**：学习前台负责练习与展示；个人中心聚合生词本、计划与 AI 辅导；管理后台供教师与管理员维护内容与账号。  
3. **业务服务层**：Spring Boot 按领域拆分服务，完成鉴权、题库、记录、讨论等业务逻辑；AI 代理模块统一转发推理请求。  
4. **数据层**：MySQL 持久化结构化数据；Redis 缓存高频只读列表，写操作触发失效。  
5. **智能推理层**：独立 Python 服务加载深度学习模型，与 Web 业务解耦，便于模型单独升级与部署。

## 竖版简图（版面较窄时用）

```mermaid
flowchart TB
    A[用户层<br/>学习者 / 教师 / 管理员] --> B[表现层<br/>学习前台 · 个人中心 · 管理后台]
    B --> C[业务服务层<br/>认证 · 内容 · 测评 · 跟踪 · 讨论 · AI代理 · 管理]
    C --> D[(MySQL)]
    C --> E[(Redis)]
    C --> F[智能推理层<br/>DKT · T5-GEC · Whisper]
```
