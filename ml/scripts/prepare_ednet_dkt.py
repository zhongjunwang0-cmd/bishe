"""
Build per-user interaction sequences for DKT training from EdNet subset parquet.
"""
from __future__ import annotations

import pickle
import sys
from pathlib import Path

import pandas as pd

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "scripts"))

from _paths import load_paths, processed_dir  # noqa: E402

MODULES = ["VOCAB", "GRAMMAR", "READING", "LISTENING", "ORAL"]
MODULE_TO_IDX = {m: i for i, m in enumerate(MODULES)}


def main() -> None:
    p = load_paths()
    data_dir = processed_dir(p)
    src = data_dir / "ednet_interactions_subset.parquet"
    if not src.is_file():
        raise FileNotFoundError(f"Run prepare_ednet.py first: {src}")

    df = pd.read_parquet(src)
    df = df.sort_values(["user_id", "timestamp"])

    sequences: list[dict] = []
    for user_id, grp in df.groupby("user_id"):
        mods, cors = [], []
        for _, row in grp.iterrows():
            mod = str(row.get("module", "GRAMMAR"))
            if mod not in MODULE_TO_IDX:
                continue
            mods.append(MODULE_TO_IDX[mod])
            cors.append(1.0 if bool(row.get("is_correct", False)) else 0.0)
        if len(mods) >= 3:
            sequences.append({"user_id": str(user_id), "modules": mods, "corrects": cors})

    out = data_dir / "ednet_dkt_sequences.pkl"
    with out.open("wb") as f:
        pickle.dump(
            {"sequences": sequences, "modules": MODULES, "module_to_idx": MODULE_TO_IDX},
            f,
        )
    print(f"Saved {len(sequences)} sequences -> {out}")


if __name__ == "__main__":
    main()
