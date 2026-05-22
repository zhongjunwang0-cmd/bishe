"""
Sample EdNet-KT1 per-user logs + join questions.csv -> parquet for KT / recommendation.

Source: https://www.kaggle.com/datasets/xuedengyue0702/ednetkt1processed
Local:  F:/bishe/EdNet-KT1/EdNet-KT1/KT1/u*.csv
        F:/bishe/EdNet-KT1/EdNet-Contents/contents/questions.csv
"""
from __future__ import annotations

import random
import sys
from pathlib import Path

import pandas as pd
from tqdm import tqdm

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "scripts"))

from _paths import load_paths, processed_dir  # noqa: E402

# Map EdNet tag buckets -> English learning modules in your system
MODULES = ("VOCAB", "GRAMMAR", "READING", "LISTENING", "ORAL")


def tag_to_module(tag_str: str) -> str:
    if not tag_str or pd.isna(tag_str):
        return "GRAMMAR"
    first = str(tag_str).split(";")[0].strip()
    try:
        idx = int(first) % len(MODULES)
    except ValueError:
        idx = hash(first) % len(MODULES)
    return MODULES[idx]


def load_questions_lookup(path: Path) -> pd.DataFrame:
    usecols = ["question_id", "correct_answer", "tags", "part"]
    df = pd.read_csv(path, usecols=usecols)
    df["module"] = df["tags"].map(tag_to_module)
    return df.set_index("question_id")


def main() -> None:
    p = load_paths()
    kt_dir = Path(p["EDNET_KT1_DIR"])
    questions_path = Path(p["EDNET_QUESTIONS_CSV"])
    out_dir = processed_dir(p)
    out_dir.mkdir(parents=True, exist_ok=True)

    if not kt_dir.is_dir():
        raise FileNotFoundError(kt_dir)
    if not questions_path.is_file():
        raise FileNotFoundError(questions_path)

    print("Loading questions metadata (this may take ~30s)...")
    q_lookup = load_questions_lookup(questions_path)

    user_files = sorted(kt_dir.glob("u*.csv"))
    max_users = int(p.get("EDNET_MAX_USERS", 500))
    max_rows = int(p.get("EDNET_MAX_ROWS_PER_USER", 300))

    random.seed(42)
    if len(user_files) > max_users:
        user_files = random.sample(user_files, max_users)

    chunks: list[pd.DataFrame] = []
    for uf in tqdm(user_files, desc="EdNet users"):
        user_id = uf.stem[1:]  # u123 -> 123
        try:
            udf = pd.read_csv(uf, nrows=max_rows)
        except Exception as e:
            print(f"Skip {uf.name}: {e}")
            continue
        udf["user_id"] = user_id
        udf = udf.merge(
            q_lookup,
            left_on="question_id",
            right_index=True,
            how="left",
        )
        udf["is_correct"] = udf["user_answer"] == udf["correct_answer"]
        udf["elapsed_sec"] = udf["elapsed_time"] / 1000.0
        chunks.append(udf)

    if not chunks:
        raise RuntimeError("No EdNet user rows loaded")

    full = pd.concat(chunks, ignore_index=True)
    full = full.dropna(subset=["question_id"])

    out_interactions = out_dir / "ednet_interactions_subset.parquet"
    full.to_parquet(out_interactions, index=False)

    # Per-user module mastery features for recommendation model
    agg = (
        full.groupby(["user_id", "module"])
        .agg(
            attempts=("is_correct", "count"),
            accuracy=("is_correct", "mean"),
            avg_elapsed=("elapsed_sec", "mean"),
        )
        .reset_index()
    )
    out_features = out_dir / "ednet_user_module_features.parquet"
    agg.to_parquet(out_features, index=False)

    print(f"Interactions: {len(full)} rows -> {out_interactions}")
    print(f"User-module features: {len(agg)} rows -> {out_features}")
    print("Modules:", full["module"].value_counts().head().to_dict())


if __name__ == "__main__":
    main()
