"""
Sample JFLEG (validation + test) -> jsonl for grammar correction training.

Source: https://www.kaggle.com/datasets/thedevastator/jfleg-english-grammatical-error-benchmark
Local:  F:/bishe/JFLEG/validation.csv, test.csv
"""
from __future__ import annotations

import ast
import json
import os
import random
import re
import sys
from pathlib import Path

import pandas as pd

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "scripts"))

from _paths import load_paths, processed_dir  # noqa: E402


def parse_corrections(raw: str) -> str:
    """Return the first JFLEG reference correction."""
    if pd.isna(raw) or not str(raw).strip():
        return ""
    text = str(raw).strip()
    # JFLEG stores 4 refs as adjacent quoted strings; ast merges them — use regex instead
    quoted = re.findall(r"'((?:\\'|[^'])*)'", text)
    if quoted:
        return quoted[0].strip()
    try:
        items = ast.literal_eval(text)
        if isinstance(items, list) and items:
            return str(items[0]).strip()
    except (ValueError, SyntaxError):
        pass
    return text.strip()


def main() -> None:
    p = load_paths()
    jfleg_root = Path(p["JFLEG_ROOT"])
    out_dir = processed_dir(p)
    out_dir.mkdir(parents=True, exist_ok=True)

    frames = []
    for name in ("validation.csv", "test.csv"):
        fp = jfleg_root / name
        if not fp.exists():
            raise FileNotFoundError(fp)
        df = pd.read_csv(fp)
        df["split_source"] = name.replace(".csv", "")
        frames.append(df)

    df = pd.concat(frames, ignore_index=True)
    max_rows = int(p.get("JFLEG_MAX_ROWS", 1200))
    if len(df) > max_rows:
        df = df.sample(n=max_rows, random_state=42).reset_index(drop=True)

    rows = []
    for _, r in df.iterrows():
        src = str(r["sentence"]).strip()
        tgt = parse_corrections(r["corrections"])
        if not src or not tgt or src == tgt:
            continue
        rows.append(
            {
                "source": src,
                "target": tgt,
                "split_source": r["split_source"],
            }
        )

    random.seed(42)
    random.shuffle(rows)
    n = len(rows)
    n_train = int(n * 0.8)
    n_val = int(n * 0.1)

    for i, row in enumerate(rows):
        if i < n_train:
            row["split"] = "train"
        elif i < n_train + n_val:
            row["split"] = "val"
        else:
            row["split"] = "test"

    out_path = out_dir / "jfleg_subset.jsonl"
    with out_path.open("w", encoding="utf-8") as f:
        for row in rows:
            f.write(json.dumps(row, ensure_ascii=False) + "\n")

    stats = pd.DataFrame(rows)["split"].value_counts().to_dict()
    print(f"Wrote {len(rows)} rows -> {out_path}")
    print("Split counts:", stats)


if __name__ == "__main__":
    main()
