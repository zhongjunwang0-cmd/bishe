"""
Sample Common Voice metadata -> manifest.csv for pronunciation / ASR calibration.

Source: https://www.kaggle.com/mozillaorg/common-voice/data
Local:  F:/bishe/Common Voice/{split}.csv + audio under same folder tree

Note: If audio subfolders are empty, manifest still builds; set audio_exists=false.
      Re-extract clips into e.g. cv-valid-train/ or use Kaggle CLI with audio bundle.
"""
from __future__ import annotations

import sys
from pathlib import Path

import pandas as pd

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "scripts"))

from _paths import load_paths, processed_dir  # noqa: E402


def main() -> None:
    p = load_paths()
    cv_root = Path(p["COMMON_VOICE_ROOT"])
    split = p.get("CV_SPLIT", "cv-valid-train")
    csv_path = cv_root / f"{split}.csv"
    out_dir = processed_dir(p)
    out_dir.mkdir(parents=True, exist_ok=True)

    if not csv_path.is_file():
        raise FileNotFoundError(csv_path)

    df = pd.read_csv(csv_path)
    max_rows = int(p.get("CV_MAX_ROWS", 1500))
    min_dur = float(p.get("CV_MIN_DURATION_SEC", 1.0))
    max_dur = float(p.get("CV_MAX_DURATION_SEC", 10.0))

    if "duration" in df.columns and df["duration"].notna().any():
        df = df[(df["duration"] >= min_dur) & (df["duration"] <= max_dur)]
    else:
        print("Note: duration column empty; skipping duration filter.")

    if len(df) > max_rows:
        df = df.sample(n=max_rows, random_state=42)

    rows = []
    exists_count = 0
    for _, r in df.iterrows():
        rel = str(r["filename"]).replace("\\", "/")
        audio_path = cv_root / rel
        exists = audio_path.is_file()
        if exists:
            exists_count += 1
        rows.append(
            {
                "filename": rel,
                "text": str(r["text"]).strip(),
                "duration": r.get("duration"),
                "audio_path": str(audio_path),
                "audio_exists": exists,
                "split": split,
            }
        )

    out = pd.DataFrame(rows)
    out_path = out_dir / "common_voice_manifest.csv"
    out.to_csv(out_path, index=False)

    print(f"Wrote {len(out)} rows -> {out_path}")
    print(f"Audio files found on disk: {exists_count}/{len(out)}")
    if exists_count == 0:
        print(
            "\n[!] No .mp3 under Common Voice folders yet.\n"
            "    Pronunciation scoring in production uses *user recordings* (Whisper+WER).\n"
            "    To calibrate scores offline, place clips at paths in column audio_path,\n"
            "    or re-download the Kaggle bundle with audio included."
        )


if __name__ == "__main__":
    main()
