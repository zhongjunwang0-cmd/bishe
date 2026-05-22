"""
One-shot deep-learning training for thesis / production deployment.

Required before答辩演示（避免被质疑“只有规则引擎”）:
  1. python scripts/run_prepare_all.py   # 若 processed 数据尚未生成
  2. python scripts/run_train_dl.py    # 本脚本：train_dkt + train_jfleg
  3. 启动 ai-service 时 GRAMMAR_RULE_ONLY=0（默认已是 0）
"""
from __future__ import annotations

import subprocess
import sys
from pathlib import Path

SCRIPTS = Path(__file__).parent
PY = sys.executable

STEPS = (
    ("prepare_ednet_dkt.py", "EdNet → DKT 序列"),
    ("train_dkt.py", "LSTM-DKT 深度知识追踪"),
    ("train_jfleg.py", "T5-GEC 语法纠错微调"),
)


def main() -> None:
    for name, label in STEPS:
        path = SCRIPTS / name
        print(f"\n=== {label} ({name}) ===")
        subprocess.check_call([PY, str(path)])

    print("\nDeep-learning models ready under ml/models/")
    print("  - dkt_model.pt + dkt_meta.json")
    print("  - grammar_t5/ + grammar_t5_meta.json")
    print("\nStart ai-service with GRAMMAR_RULE_ONLY=0 (default):")
    print("  cd ../ai-service && uvicorn app.main:app --host 0.0.0.0 --port 8000")


if __name__ == "__main__":
    main()
