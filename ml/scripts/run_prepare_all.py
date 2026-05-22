"""Run all three dataset prepare scripts."""
import subprocess
import sys
from pathlib import Path

SCRIPTS = Path(__file__).parent
PY = sys.executable

for name in ("prepare_jfleg.py", "prepare_ednet.py", "prepare_common_voice.py"):
    path = SCRIPTS / name
    print(f"\n=== {name} ===")
    subprocess.check_call([PY, str(path)])

print("\nAll done. Outputs under ml/data/processed/")
