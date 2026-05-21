# 英语学习系统 (English Learning System)

本项目是一个基于 Spring Boot 3 + Vue 3 的全栈英语学习平台，旨在提供词汇、语法、文献阅读等全方位的学习支持。

## 🛠 技术栈
- **Frontend**: Vue 3 + Vite + Element Plus + ECharts
- **Backend**: Spring Boot 3 + Shiro + MyBatis-Plus + Redis
- **Database**: MySQL 8.0
- **Container**: Docker + Docker Compose

## How to Run
1. 确保 Docker Desktop 已启动。
2. 在根目录（包含 `docker-compose.yml` 的目录）执行：
   ```bash
   docker compose up -d --build
   ```
3. 等待容器启动完成后，访问服务。

## Services
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080

## Verification
1. 访问 `http://localhost:3000` 并使用管理员账号（`admin` / `123456`）登录。
2. 进入“词汇学习”或“阅读理解”模块，验证能否正常获取并展示题目数据。
3. 点击“做题”并提交一次成绩，验证能否看到成功提示。
4. 前往“学习记录追踪”，检查刚才的操作是否已经作为一条新学习记录出现。
5. 前往“讨论区评论”发表一条评论，刷新后验证评论是否依然存留。

## 测试账号
| 账号 | 密码 | 角色 | 说明 |
|------|------|------|------|
| admin | 123456 | 管理员 | 可访问全部功能，包含用户管理和系统设置 |
| teacher | 123456 | 教师 | 可发布/编辑内容，不可删除，无用户管理权限 |
| teacher2 | 123456 | 教师 | 同上，作为第二名教师账号演示多用户 |
| user | 123456 | 普通用户 | 仅可学习和发表评论，无内容管理权限 |
| student1~4 | 123456 | 普通用户 | 多学生账号，用于演示讨论区和学习记录 |

---

## 🌟 系统特色
- **采用RBAC权限模型**：实现多角色（管理员、教师、普通用户）精细化权限控制。
- **集成 AI 智能辅导（规则引擎，无外部 API 依赖）**：系统采用完全离线的 AI 辅导实现，无需 OpenAI 等外部 LLM API，
  保证一键 Docker 部署、数据安全和零网络依赖。AI 能力分两层：
  ① **个性化分析**：读取用户真实学习记录，动态统计学习时长、活跃模块分布、薄弱环节，生成定制化学习建议；
  ② **知识问答引擎**：覆盖词汇记忆、语法讲解、阅读策略、听力提升、写作技巧、口语训练、四六级/雅思/托福备考等 7 大领域的规则引擎。
- **支持 Redis 缓存优化，全模块覆盖**：所有高频只读列表接口（词汇、语法、文献、阅读理解、听力、选词填空、口语共 7 个模块）
  均已通过 Spring `@Cacheable` 注解接入 Redis，TTL=1 小时；新增和删除操作通过 `@CacheEvict` 自动失效，确保数据一致性，
  无需手动刷新缓存。

## 🐳 Docker 配置说明
- **后端 (Backend)**: 采用了基于 Alpine 的轻量级镜像，并配置了 Maven 阿里云镜像加速构建。
- **前端 (Frontend)**: 采用 Nginx 托管构建后的静态文件，并配置了生产环境的代理。
- **数据库 (Database)**: 自动执行 `mysql/init/init.sql` 脚本，开箱即拥有初始演示数据。
