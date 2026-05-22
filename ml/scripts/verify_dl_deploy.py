"""
Verify deep-learning artifacts before deployment / thesis defense.

Exit 0 when DKT + T5-GEC weights are present and GRAMMAR_RULE_ONLY is not forced on.
"""
from __future__ import annotations

import json
import os
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MODELS = ROOT / "models"
MIN_T5_WEIGHT_BYTES = 200_000_000


def _fail(msg: str) -> None:
    print(f"FAIL: {msg}", file=sys.stderr)
    sys.exit(1)


def _ok(msg: str) -> None:
    print(f"OK: {msg}")


def main() -> None:
    rule_only = os.getenv("GRAMMAR_RULE_ONLY", "0").strip().lower() in ("1", "true", "yes")
    if rule_only:
        _fail(
            "GRAMMAR_RULE_ONLY=1 — grammar inference uses rules only. "
            "Set GRAMMAR_RULE_ONLY=0 for thesis / production."
        )
    _ok("GRAMMAR_RULE_ONLY=0 (deep-learning grammar enabled)")

    dkt_pt = MODELS / "dkt_model.pt"
    dkt_meta = MODELS / "dkt_meta.json"
    if not dkt_pt.is_file() or not dkt_meta.is_file():
        _fail(f"DKT model missing — run: python scripts/train_dkt.py (after prepare_ednet_dkt.py)")
    meta = json.loads(dkt_meta.read_text(encoding="utf-8"))
    if meta.get("model_type") != "LSTM-DKT":
        _fail(f"Unexpected DKT meta model_type: {meta.get('model_type')}")
    _ok(f"LSTM-DKT weights ({dkt_pt.name}, {dkt_pt.stat().st_size // 1024} KB)")

    t5_dir = MODELS / "grammar_t5"
    if not t5_dir.is_dir():
        _fail(f"T5-GEC directory missing — run: python scripts/train_jfleg.py")
    weight_names = ("model.safetensors", "pytorch_model.bin")
    weight_file = next((t5_dir / n for n in weight_names if (t5_dir / n).is_file()), None)
    if weight_file is None or weight_file.stat().st_size < MIN_T5_WEIGHT_BYTES:
        _fail(
            f"T5-GEC weights missing or too small under {t5_dir} — run: python scripts/train_jfleg.py"
        )
    _ok(f"T5-GEC fine-tuned weights ({weight_file.name})")

    g_meta_path = MODELS / "grammar_t5_meta.json"
    if g_meta_path.is_file():
        g_meta = json.loads(g_meta_path.read_text(encoding="utf-8"))
        if g_meta.get("inference_mode") == "rule_fallback":
            print(
                "WARN: grammar_t5_meta.json still says inference_mode=rule_fallback; "
                "re-run train_jfleg.py after training.",
                file=sys.stderr,
            )

    print("\nAll deep-learning deployment checks passed.")


if __name__ == "__main__":
    main()
