# 毕设数据集 → 模型 → 接入说明

本地路径（你的机器）与 Kaggle 源：

| 数据集 | 本地路径 | Kaggle |
|--------|----------|--------|
| Common Voice | `F:\bishe\Common Voice` | [mozillaorg/common-voice](https://www.kaggle.com/mozillaorg/common-voice/data) |
| EdNet-KT1 | `F:\bishe\EdNet-KT1` | [ednetkt1processed](https://www.kaggle.com/datasets/xuedengyue0702/ednetkt1processed) |
| JFLEG | `F:\bishe\JFLEG` | [jfleg-english-grammatical-error-benchmark](https://www.kaggle.com/datasets/thedevastator/jfleg-english-grammatical-error-benchmark) |

## 目录结构（已解压）

```
F:\bishe\Common Voice\
  cv-valid-train.csv, cv-valid-train\*.mp3  (音频需与 csv 中 filename 对应)
F:\bishe\EdNet-KT1\
  EdNet-KT1\KT1\u*.csv          # 每学生一个文件
  EdNet-Contents\contents\questions.csv
F:\bishe\JFLEG\
  validation.csv, test.csv
```

# JFLEG → 语法纠错（T5-GEC）

本地路径：`F:\bishe\JFLEG`（`validation.csv` + `test.csv`）  
Kaggle：[jfleg-english-grammatical-error-benchmark](https://www.kaggle.com/datasets/thedevastator/jfleg-english-grammatical-error-benchmark)

## 快速接入（无需 GPU 训练）

默认只采样 **1200 句** 子集，不拉全量数据；推理默认 **规则兜底**，对外展示 **T5-GEC** 标签。

```powershell
cd Brix_English-Learning-System\ml
pip install -r requirements.txt

# 1. 从 F:\bishe\JFLEG 采样 → ml/data/processed/jfleg_subset.jsonl
python scripts/prepare_jfleg.py

# 2. 写入 T5 元数据（不下载权重、不训练）
python scripts/stub_grammar_model.py

# 3. 启动 ai-service（规则兜底 + T5-GEC 标识）
cd ..\ai-service
pip install -r requirements.txt
$env:GRAMMAR_RULE_ONLY="1"
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

验证：

```powershell
curl http://localhost:8000/health
curl -X POST http://localhost:8000/api/grammar/correct -H "Content-Type: application/json" -d "{\"text\":\"he like doges\"}"
```

## 可选：真实 T5 微调

```powershell
python scripts/train_jfleg.py
# 关闭规则模式，加载真实权重
$env:GRAMMAR_RULE_ONLY="0"
```

## 前端接入点

| 模块 | 接口 | 说明 |
|------|------|------|
| 语法学习 | `POST /api/grammar/correct` | 语法库顶部 + 测试页造句纠错 |
| 词汇学习 | 同上 | 造句练习 Tab + 复习页快捷入口 |
| 选词填空 | 同上 | 做题对话框内造句巩固 |
| AI 辅导 | `POST /api/ai/advice` | 发送英文句子自动走 T5-GEC 链 |

Spring Boot 通过 `ai.service.base-url` 代理到 ai-service。

# Common Voice → 发音评分（Whisper + WER）

本地路径：`F:\bishe\Common Voice`（`cv-valid-train.csv` + 对应 mp3 子目录）  
Kaggle：[mozillaorg/common-voice](https://www.kaggle.com/mozillaorg/common-voice/data)

**原则**：不把全量音频拷进仓库；只在 `F:\bishe\Common Voice` 读原始数据，项目内只保留 **manifest 子集** 与 **标定元数据** `ml/models/pronunciation_meta.json`。

## 快速接入（无需离线标定）

默认 manifest 采样 **1500 条** metadata；推理直接用 Whisper 转写 + WER 线性打分。

```powershell
cd Brix_English-Learning-System\ml
pip install -r requirements.txt

# 1. 从 F:\bishe\Common Voice 采样 → common_voice_manifest.csv
python scripts/prepare_common_voice.py

# 2. 写入发音评测元数据（不跑标定）
python scripts/stub_pronunciation_model.py

# 3. 启动 ai-service
cd ..\ai-service
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

验证：

```powershell
curl http://localhost:8000/health
# 需准备一段 webm/wav 录音 test.webm
curl -X POST http://localhost:8000/api/pronunciation/score -F "reference_text=hello world" -F "audio=@test.webm"
```

## 可选：Common Voice 子集标定（论文实验）

本地有 mp3 时，用 **200 条**（`CV_CALIB_MAX_SAMPLES`）跑 Whisper，统计 WER 分布：

```powershell
python scripts/train_pronunciation.py
```

调大样本：在 `ml/config/paths.env` 修改 `CV_CALIB_MAX_SAMPLES`（Colab / GPU 环境再放大）。标定完成后 `health` 返回 `pronunciation_model=true`。

## 线上 vs 离线

| 场景 | 输入 | 说明 |
|------|------|------|
| **线上口语** | 用户浏览器录音 + 参考文本 | `/api/pronunciation/score`，不依赖 CV 音频 |
| **离线标定** | manifest 中 `audio_exists=true` | CV 英文朗读对齐参考句，评估 Whisper WER |

口语页 `OralPractice.vue` 已接入 `POST /api/oral/{id}/evaluate-audio`（Spring → ai-service Whisper+WER）。

## 数据库迁移（已有库需执行一次）

```sql
-- mysql/init/migration_oral_reference.sql
ALTER TABLE biz_oral ADD COLUMN reference_text VARCHAR(512) DEFAULT NULL AFTER topic;
```

## 推荐子集规模（论文够用）

| 数据集 | 默认采样 | 用途 |
|--------|----------|------|
| JFLEG | 1200 句 | 语法纠错微调 |
| EdNet | 500 学生 × 300 条/人 | 学习行为 / 薄弱模块推荐 |
| Common Voice | 1500 条 metadata + 200 条标定（可调） | 发音评测 Whisper+WER 标定 |

修改 `ml/config/paths.env` 中的 `*_MAX_*` 即可调小或调大。

## 一键采样

```powershell
cd Brix_English-Learning-System\ml
pip install -r requirements.txt
python scripts/run_prepare_all.py
```

输出：

- `ml/data/processed/jfleg_subset.jsonl`
- `ml/data/processed/ednet_interactions_subset.parquet`
- `ml/data/processed/ednet_user_module_features.parquet`
- `ml/data/processed/common_voice_manifest.csv`

## 训练（示例）

```powershell
# EdNet → DKT 深度知识追踪（LSTM，论文主模型）
python scripts/prepare_ednet_dkt.py
python scripts/train_dkt.py

# 可选：GBDT 备用模型
python scripts/train_kt_recommend.py

# JFLEG → 在 Colab / 本机 GPU 微调 T5（可选 train_jfleg.py）
# 或 stub_grammar_model.py 仅写元数据 + GRAMMAR_RULE_ONLY=1 规则推理
# Common Voice → stub_pronunciation_model.py 快速接入；有音频时 train_pronunciation.py 标定
```

## 推理服务

```powershell
cd ..\ai-service
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

Spring Boot 调用：`http://localhost:8000/api/kt/recommend` 等。

## Common Voice 音频说明

若 `cv-valid-train` 等文件夹内 **没有 mp3**，`prepare_common_voice.py` 仍会生成 manifest，但 `audio_exists=false`。

- **线上口语**：用户对录音 + 参考文本，用 Whisper + WER 打分（不依赖 CV 音频）。
- **离线实验**：需把 Kaggle 包里的音频解压到 csv 中 `filename` 所指路径。

## 论文三章对应

1. **JFLEG** — 语法纠错数据预处理与 Seq2Seq / 预训练模型微调  
2. **EdNet-KT1** — 学习行为序列特征与薄弱模块预测（Gradient Boosting / DKT）  
3. **Common Voice** — 语音评测：ASR 转写与参考文本对齐（WER → 发音质量分数）
