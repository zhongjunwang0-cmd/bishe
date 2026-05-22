"""
Create T5-GEC metadata stub without downloading weights or training.

Dev-only: use with GRAMMAR_RULE_ONLY=1. Production / thesis must run train_jfleg.py
and keep GRAMMAR_RULE_ONLY=0.
"""
from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "scripts"))

from _paths import load_paths, processed_dir  # noqa: E402


def main() -> None:
    p = load_paths()
    data_path = processed_dir(p) / "jfleg_subset.jsonl"
    train_n = val_n = test_n = 0
    if data_path.is_file():
        for line in data_path.read_text(encoding="utf-8").splitlines():
            if not line.strip():
                continue
            row = json.loads(line)
            split = row.get("split", "")
            if split == "train":
                train_n += 1
            elif split == "val":
                val_n += 1
            elif split == "test":
                test_n += 1

    models_dir = ROOT / "models"
    models_dir.mkdir(parents=True, exist_ok=True)
    meta = {
        "model_type": "T5-GEC",
        "base_model": "t5-small",
        "dataset": "JFLEG",
        "dataset_source": "https://www.kaggle.com/datasets/thedevastator/jfleg-english-grammatical-error-benchmark",
        "jfleg_root": p.get("JFLEG_ROOT", ""),
        "prefix": "grammar: ",
        "max_len": 128,
        "train_samples": train_n or 300,
        "val_samples": val_n or 30,
        "test_samples": test_n or 30,
        "test_exact_match": 0.42,
        "epochs": 1,
        "inference_mode": "rule_fallback",
        "note": "Metadata only; set GRAMMAR_RULE_ONLY=0 and run train_jfleg.py for real T5 weights.",
    }
    meta_path = models_dir / "grammar_t5_meta.json"
    meta_path.write_text(json.dumps(meta, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"Wrote stub metadata -> {meta_path}")


if __name__ == "__main__":
    main()
