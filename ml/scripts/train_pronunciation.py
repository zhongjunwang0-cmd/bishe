"""
Calibrate pronunciation scoring (Whisper ASR + WER) on a Common Voice subset.

Input:  ml/data/processed/common_voice_manifest.csv  (audio_exists=true rows)
Output: ml/models/pronunciation_meta.json

Does NOT copy audio into the repo — reads clips from COMMON_VOICE_ROOT only.
Use a small CV_CALIB_MAX_SAMPLES (default 200) for laptop / thesis experiments;
scale up in Colab or after full Kaggle download if needed.
"""
from __future__ import annotations

import json
import sys
from pathlib import Path

import pandas as pd
from tqdm import tqdm

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "scripts"))

from _paths import load_paths, processed_dir  # noqa: E402

WHISPER_MODEL = "tiny"
MAX_CALIB_SAMPLES = 200


def _normalize(text: str) -> str:
    return " ".join(text.strip().lower().split())


def main() -> None:
    p = load_paths()
    manifest_path = processed_dir(p) / "common_voice_manifest.csv"
    if not manifest_path.is_file():
        raise FileNotFoundError(f"Run prepare_common_voice.py first: {manifest_path}")

    df = pd.read_csv(manifest_path)
    if "audio_exists" in df.columns:
        mask = df["audio_exists"].astype(str).str.lower().isin(("true", "1"))
        df = df[mask]
    if df.empty:
        raise RuntimeError(
            "No rows with audio_exists=true. Extract mp3 under "
            f"{p.get('COMMON_VOICE_ROOT')} (see ml/README.md Common Voice section)."
        )

    max_samples = int(p.get("CV_CALIB_MAX_SAMPLES", MAX_CALIB_SAMPLES))
    if len(df) > max_samples:
        df = df.sample(n=max_samples, random_state=42)

    try:
        import whisper
        from jiwer import wer
    except ImportError as exc:
        raise SystemExit(
            "Install calibration deps in ml venv: pip install openai-whisper jiwer"
        ) from exc

    print(f"Loading Whisper model={WHISPER_MODEL} ...", flush=True)
    model = whisper.load_model(WHISPER_MODEL)

    wers: list[float] = []
    for _, row in tqdm(df.iterrows(), total=len(df), desc="Calibrating"):
        audio_path = Path(str(row["audio_path"]))
        ref = _normalize(str(row["text"]))
        if not audio_path.is_file() or not ref:
            continue
        try:
            result = model.transcribe(str(audio_path), language="en")
            hyp = _normalize(result.get("text", ""))
            w = float(wer(ref, hyp)) if ref else 1.0
            wers.append(min(w, 1.0))
        except Exception as exc:
            print(f"Skip {audio_path.name}: {exc}", flush=True)

    if not wers:
        raise RuntimeError("Calibration produced no WER samples — check audio paths.")

    wers_sorted = sorted(wers)
    n = len(wers_sorted)
    mean_wer = sum(wers) / n
    p50 = wers_sorted[n // 2]
    p90 = wers_sorted[int(n * 0.9)]
    p95 = wers_sorted[int(n * 0.95)]

    models_dir = ROOT / "models"
    models_dir.mkdir(parents=True, exist_ok=True)
    meta = {
        "model_type": "Whisper-WER",
        "asr_model": WHISPER_MODEL,
        "dataset": "Common Voice",
        "dataset_source": "https://www.kaggle.com/mozillaorg/common-voice/data",
        "common_voice_root": p.get("COMMON_VOICE_ROOT", ""),
        "calibration_samples": n,
        "mean_wer": round(mean_wer, 4),
        "wer_p50": round(p50, 4),
        "wer_p90": round(p90, 4),
        "wer_p95": round(p95, 4),
        "score_mode": "linear",
        "score_formula": "max(0, min(100, int(100 * (1 - wer))))",
        "inference_mode": "whisper_calibrated",
        "note": (
            f"Calibrated on {n} CV clips. Production scores user uploads via same Whisper+WER pipeline."
        ),
    }
    meta_path = models_dir / "pronunciation_meta.json"
    meta_path.write_text(json.dumps(meta, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"mean_wer={mean_wer:.3f}  p50={p50:.3f}  p90={p90:.3f}  samples={n}")
    print(f"Saved -> {meta_path}")


if __name__ == "__main__":
    main()
