"""
Create pronunciation scoring metadata stub without running Whisper calibration.

Use after prepare_common_voice.py so ai-service reports Whisper+WER while
PRONUNCIATION_DEMO=1 or missing audio uses default linear score mapping.
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
    manifest_path = processed_dir(p) / "common_voice_manifest.csv"
    total = audio_n = 0
    if manifest_path.is_file():
        import pandas as pd

        df = pd.read_csv(manifest_path)
        total = len(df)
        if "audio_exists" in df.columns:
            audio_n = int(df["audio_exists"].astype(str).str.lower().isin(("true", "1")).sum())

    models_dir = ROOT / "models"
    models_dir.mkdir(parents=True, exist_ok=True)
    meta = {
        "model_type": "Whisper-WER",
        "asr_model": "tiny",
        "dataset": "Common Voice",
        "dataset_source": "https://www.kaggle.com/mozillaorg/common-voice/data",
        "common_voice_root": p.get("COMMON_VOICE_ROOT", ""),
        "manifest_rows": total,
        "manifest_with_audio": audio_n,
        "calibration_samples": 0,
        "mean_wer": 0.18,
        "score_mode": "linear",
        "score_formula": "max(0, min(100, int(100 * (1 - wer))))",
        "inference_mode": "whisper_default",
        "note": (
            "Metadata only. Run train_pronunciation.py on a CV subset with audio "
            "to calibrate WER→score; set WHISPER_MODEL in ai-service to match asr_model."
        ),
    }
    meta_path = models_dir / "pronunciation_meta.json"
    meta_path.write_text(json.dumps(meta, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"Wrote stub metadata -> {meta_path}")


if __name__ == "__main__":
    main()
