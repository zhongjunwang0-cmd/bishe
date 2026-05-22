# 深度学习部署与答辩检查清单

> **答辩前必读**：系统 AI 能力包含 **LSTM-DKT 深度知识追踪** 与 **T5-GEC 语法纠错**，不是纯规则引擎。  
> 部署时必须完成下方训练步骤，并确认 `GRAMMAR_RULE_ONLY=0`。

## 1. 必须执行的训练（不可跳过）

在 `ml/` 目录下，按顺序执行：

```powershell
cd Brix_English-Learning-System\ml
pip install -r requirements.txt

# 若 processed 数据尚未生成（首次部署）
python scripts/run_prepare_all.py
python scripts/prepare_ednet_dkt.py

# 一键训练两大深度学习模型（答辩主模型）
python scripts/run_train_dl.py
```

等价于分别运行：

| 步骤 | 脚本 | 产出 | 论文对应 |
|------|------|------|----------|
| 1 | `train_dkt.py` | `ml/models/dkt_model.pt` + `dkt_meta.json` | EdNet-KT1 → LSTM 深度知识追踪 |
| 2 | `train_jfleg.py` | `ml/models/grammar_t5/` + `grammar_t5_meta.json` | JFLEG → T5 语法纠错微调 |

**禁止** 在答辩/生产环境使用 `stub_grammar_model.py` + `GRAMMAR_RULE_ONLY=1` 作为最终方案（仅可用于本地快速联调）。

## 2. 环境变量（ai-service）

```powershell
# 默认已是 0；显式设置以免答辩机环境被覆盖
$env:GRAMMAR_RULE_ONLY="0"
$env:MODELS_DIR="..\ml\models"   # 指向含权重的目录

cd ..\ai-service
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

可参考 `ai-service/.env.example`。

## 3. 部署验收

### 3.1 自动检查脚本

```powershell
cd ml
$env:GRAMMAR_RULE_ONLY="0"
python scripts/verify_dl_deploy.py
```

通过时应看到 `dkt_model.pt`、`grammar_t5/` 权重存在，且 `GRAMMAR_RULE_ONLY=0`。

### 3.2 Health 接口

```powershell
curl http://localhost:8000/health
```

期望 JSON（关键字段）：

```json
{
  "dkt_model": true,
  "grammar_model": true,
  "grammar_mode": "fine_tuned"
}
```

若 `dkt_model: false` → 未训练或未挂载 `dkt_model.pt`。  
若 `grammar_mode: "rule"` 且 `grammar_model: false` → 未跑 `train_jfleg.py` 或 `GRAMMAR_RULE_ONLY=1`。

### 3.3 功能抽检

```powershell
# 语法纠错（应走 T5-GEC，source 为 T5-GEC）
curl -X POST http://localhost:8000/api/grammar/correct ^
  -H "Content-Type: application/json" ^
  -d "{\"text\":\"he like doges\"}"

# 学习推荐（应走 LSTM-DKT，model_type 为 LSTM-DKT）
curl -X POST http://localhost:8000/api/kt/recommend ^
  -H "Content-Type: application/json" ^
  -d "{\"module_stats\":[{\"module\":\"VOCAB\",\"accuracy\":0.4,\"attempts\":10}]}"
```

Spring Boot 需能访问 ai-service（`AI_SERVICE_URL`，默认 `http://localhost:8000`）。  
Docker 部署后端时见根目录 `docker-compose.yml` 中 `AI_SERVICE_URL=http://host.docker.internal:8000`。

## 4. CI（GitHub Actions）

仓库 `.github/workflows/ml-models.yml` 在 push/PR 时：

1. 安装 `ml/` 与 `ai-service/` 依赖  
2. 执行 `run_train_dl.py`（需 `ml/data/processed/` 中已有采样数据）  
3. 执行 `verify_dl_deploy.py`（`GRAMMAR_RULE_ONLY=0`）

本地与 CI 使用同一套脚本，避免“本地规则、答辩深度学习”不一致。

## 5. 仅开发联调（非答辩环境）

无 GPU、暂不下载 T5 权重时：

```powershell
python scripts/stub_grammar_model.py
$env:GRAMMAR_RULE_ONLY="1"
uvicorn app.main:app --port 8000
```

此模式 **不得** 作为毕设最终演示配置。

## 6. 数据集路径

修改 `ml/config/paths.env` 指向本机 JFLEG / EdNet 目录。详见 [ml/README.md](../ml/README.md)。
