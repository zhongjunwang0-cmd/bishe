"""Load paths from ml/config/paths.env or environment variables."""
from __future__ import annotations

import os
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CONFIG_DIR = ROOT / "config"


def _load_env_file() -> None:
    env_file = CONFIG_DIR / "paths.env"
    if not env_file.exists():
        env_file = CONFIG_DIR / "paths.env.example"
    if not env_file.exists():
        return
    for line in env_file.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, _, value = line.partition("=")
        key, value = key.strip(), value.strip()
        if key and key not in os.environ:
            os.environ[key] = value


def load_paths() -> dict[str, str]:
    _load_env_file()
    keys = [
        "JFLEG_ROOT",
        "COMMON_VOICE_ROOT",
        "EDNET_ROOT",
        "EDNET_KT1_DIR",
        "EDNET_QUESTIONS_CSV",
        "JFLEG_MAX_ROWS",
        "EDNET_MAX_USERS",
        "EDNET_MAX_ROWS_PER_USER",
        "CV_SPLIT",
        "CV_MAX_ROWS",
        "CV_MIN_DURATION_SEC",
        "CV_MAX_DURATION_SEC",
        "CV_CALIB_MAX_SAMPLES",
        "OUTPUT_DIR",
    ]
    out: dict[str, str] = {}
    for k in keys:
        if k in os.environ:
            out[k] = os.environ[k]
    defaults = {
        "JFLEG_ROOT": "F:/bishe/JFLEG",
        "COMMON_VOICE_ROOT": "F:/bishe/Common Voice",
        "EDNET_ROOT": "F:/bishe/EdNet-KT1",
        "EDNET_KT1_DIR": "F:/bishe/EdNet-KT1/EdNet-KT1/KT1",
        "EDNET_QUESTIONS_CSV": "F:/bishe/EdNet-KT1/EdNet-Contents/contents/questions.csv",
        "JFLEG_MAX_ROWS": "1200",
        "EDNET_MAX_USERS": "500",
        "EDNET_MAX_ROWS_PER_USER": "300",
        "CV_SPLIT": "cv-valid-train",
        "CV_MAX_ROWS": "1500",
        "CV_MIN_DURATION_SEC": "1.0",
        "CV_MAX_DURATION_SEC": "10.0",
        "CV_CALIB_MAX_SAMPLES": "200",
        "OUTPUT_DIR": str(ROOT / "data" / "processed"),
    }
    for k, v in defaults.items():
        out.setdefault(k, v)
    if not Path(out["OUTPUT_DIR"]).is_absolute():
        out["OUTPUT_DIR"] = str((ROOT / out["OUTPUT_DIR"]).resolve())
    return out


def processed_dir(p: dict[str, str] | None = None) -> Path:
    p = p or load_paths()
    return Path(p["OUTPUT_DIR"])
