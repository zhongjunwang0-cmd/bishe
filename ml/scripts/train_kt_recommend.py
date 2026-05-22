"""
Train lightweight module recommender from EdNet user-module features.

Input:  ml/data/processed/ednet_user_module_features.parquet
Output: ml/models/kt_recommend.joblib
"""
from __future__ import annotations

import sys
from pathlib import Path

import joblib
import numpy as np
import pandas as pd
from sklearn.ensemble import GradientBoostingClassifier
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "scripts"))

from _paths import load_paths, processed_dir  # noqa: E402

MODULES = ["VOCAB", "GRAMMAR", "READING", "LISTENING", "ORAL"]


def build_training_frame(features: pd.DataFrame) -> tuple[np.ndarray, np.ndarray]:
    """Label = module with lowest accuracy (needs practice)."""
    rows = []
    labels = []
    for user_id, grp in features.groupby("user_id"):
        pivot = grp.set_index("module")
        acc = {m: pivot.loc[m, "accuracy"] if m in pivot.index else 0.5 for m in MODULES}
        attempts = {m: pivot.loc[m, "attempts"] if m in pivot.index else 0 for m in MODULES}
        weak = min(MODULES, key=lambda m: (acc[m], -attempts[m]))
        vec = []
        for m in MODULES:
            vec.extend(
                [
                    acc[m],
                    attempts[m],
                    pivot.loc[m, "avg_elapsed"] if m in pivot.index else 30.0,
                ]
            )
        rows.append(vec)
        labels.append(weak)
    return np.array(rows, dtype=np.float32), np.array(labels)


def main() -> None:
    p = load_paths()
    data_dir = processed_dir(p)
    feat_path = data_dir / "ednet_user_module_features.parquet"
    if not feat_path.exists():
        raise FileNotFoundError(f"Run prepare_ednet.py first: {feat_path}")

    features = pd.read_parquet(feat_path)
    X, y = build_training_frame(features)

    le = LabelEncoder()
    le.fit(MODULES)
    y_enc = le.transform(y)

    X_train, X_test, y_train, y_test = train_test_split(
        X, y_enc, test_size=0.2, random_state=42, stratify=y_enc
    )

    clf = GradientBoostingClassifier(n_estimators=80, max_depth=4, random_state=42)
    clf.fit(X_train, y_train)
    acc = clf.score(X_test, y_test)
    print(f"Test accuracy (weak-module prediction): {acc:.3f}")

    model_dir = ROOT / "models"
    model_dir.mkdir(parents=True, exist_ok=True)
    bundle = {"model": clf, "label_encoder": le, "modules": MODULES}
    out = model_dir / "kt_recommend.joblib"
    joblib.dump(bundle, out)
    print(f"Saved -> {out}")


if __name__ == "__main__":
    main()
