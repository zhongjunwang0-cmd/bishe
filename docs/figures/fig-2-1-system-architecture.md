# 图 2-1 系统技术架构图

> **论文图题（建议）**：图 2-1 英语学习系统技术架构  
> **导出方式**：在 VS Code 安装 Mermaid 插件预览后截图；或使用 [Mermaid Live Editor](https://mermaid.live) 粘贴下方代码导出 PNG/SVG。

```mermaid
flowchart TB
    subgraph client["用户层"]
        Browser["浏览器<br/>(Chrome / Edge 等)"]
    end

    subgraph frontend["表现层"]
        Vue["Vue 3 + Vite<br/>Element Plus / Pinia / Axios"]
        Nginx["Nginx<br/>(生产静态资源 + 反向代理)"]
    end

    subgraph backend["业务层"]
        SB["Spring Boot 3<br/>Shiro RBAC · MyBatis-Plus"]
        Cache["Spring Cache"]
    end

    subgraph data["数据层"]
        MySQL[("MySQL 8.0<br/>用户 · 题库 · 学习记录")]
        Redis[("Redis 7<br/>列表缓存 TTL≈1h")]
    end

    subgraph ai["智能推理层"]
        AIS["ai-service<br/>FastAPI + Uvicorn :8000"]
        DKT["LSTM-DKT<br/>薄弱模块推荐"]
        T5["T5-GEC<br/>语法纠错"]
        WH["Whisper-WER<br/>发音评测"]
    end

    Browser --> Nginx
    Nginx --> Vue
    Vue -->|"REST / JSON"| SB
    SB --> Cache
    Cache --> Redis
    SB --> MySQL
    SB -->|"HTTP 代理<br/>ai.service.base-url"| AIS
    AIS --> DKT
    AIS --> T5
    AIS --> WH

    classDef store fill:#e8f4fc,stroke:#2563eb
    classDef aiModel fill:#fef3c7,stroke:#d97706
    class MySQL,Redis store
    class DKT,T5,WH aiModel
```

## 架构说明（图注可参考）

1. **用户层**：学习者与管理员通过浏览器访问系统。  
2. **表现层**：Vue 3 单页应用负责交互；Docker 部署时由 Nginx 托管构建产物并转发 API。  
3. **业务层**：Spring Boot 处理认证授权、题库与学习记录等业务；高频只读列表经 Redis 缓存。  
4. **数据层**：MySQL 持久化结构化数据；Redis 作为缓存，写操作触发 `@CacheEvict` 失效。  
5. **智能推理层**：独立 Python 服务加载三类模型，Java 后端通过 HTTP 调用，避免 JVM 内嵌深度学习框架。

## 简化版（版面较窄的论文用）

```mermaid
flowchart LR
    A[浏览器] --> B[Vue 3 前端]
    B --> C[Spring Boot 后端]
    C --> D[(MySQL)]
    C --> E[(Redis)]
    C --> F[ai-service]
    F --> G[LSTM-DKT]
    F --> H[T5-GEC]
    F --> I[Whisper-WER]
```
