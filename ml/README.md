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

## 推荐子集规模（论文够用）

| 数据集 | 默认采样 | 用途 |
|--------|----------|------|
| JFLEG | 1200 句 | 语法纠错微调 |
| EdNet | 500 学生 × 300 条/人 | 学习行为 / 薄弱模块推荐 |
| Common Voice | 1500 条 metadata（时长 1–10s） | 发音评测 WER 标定 |

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

# JFLEG → 在 Colab / 本机 GPU 微调 T5（自行添加 train_jfleg.py 或 notebooks）
# Common Voice → 用 manifest 中有 audio_exists=true 的样本校准 WER→分数曲线
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
