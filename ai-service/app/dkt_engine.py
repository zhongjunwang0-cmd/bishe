"""
Load and run LSTM-DKT for module mastery / weak-module recommendation.
"""
from __future__ import annotations

import json
from pathlib import Path
from typing import Any

import numpy as np
import torch
import torch.nn as nn

MODULES = ["VOCAB", "GRAMMAR", "READING", "LISTENING", "ORAL"]

TASK_MAP = {
    "VOCAB": "词汇复习 15 个单词",
    "GRAMMAR": "语法测试 1 套",
    "READING": "阅读理解 1 篇",
    "LISTENING": "听力训练 1 套",
    "ORAL": "口语录音练习 1 次",
}


class DKTModel(nn.Module):
    def __init__(self, n_modules: int, embed_dim: int, hidden: int):
        super().__init__()
        self.embed = nn.Embedding(n_modules, embed_dim)
        self.lstm = nn.LSTM(embed_dim + 1, hidden, batch_first=True)
        self.out = nn.Linear(hidden, 1)

    def forward(self, modules: torch.Tensor, corrects: torch.Tensor):
        emb = self.embed(modules)
        x = torch.cat([emb, corrects.unsqueeze(-1)], dim=-1)
        out, _ = self.lstm(x)
        return self.out(out).squeeze(-1)


class DKTEngine:
    def __init__(self, models_dir: Path):
        self.models_dir = models_dir
        self.model: DKTModel | None = None
        self.meta: dict[str, Any] = {}
        self.module_to_idx: dict[str, int] = {}
        self._load()

    def _load(self) -> None:
        meta_path = self.models_dir / "dkt_meta.json"
        model_path = self.models_dir / "dkt_model.pt"
        if not meta_path.is_file() or not model_path.is_file():
            return
        self.meta = json.loads(meta_path.read_text(encoding="utf-8"))
        modules = self.meta.get("modules", MODULES)
        self.module_to_idx = {m: i for i, m in enumerate(modules)}
        n_modules = len(modules)
        embed_dim = int(self.meta.get("embed_dim", 32))
        hidden = int(self.meta.get("hidden", 64))
        self.model = DKTModel(n_modules, embed_dim, hidden)
        self.model.load_state_dict(torch.load(model_path, map_location="cpu"))
        self.model.eval()

    @property
    def ready(self) -> bool:
        return self.model is not None

    def _stats_to_sequence(
        self, module_stats: list[dict], events: list[dict], max_len: int
    ) -> tuple[list[int], list[float]]:
        mods: list[int] = []
        cors: list[float] = []

        for ev in events:
            m = ev.get("module")
            if m not in self.module_to_idx:
                continue
            mods.append(self.module_to_idx[m])
            cors.append(1.0 if ev.get("correct") else 0.0)

        if len(mods) < 3:
            for st in module_stats:
                m = st.get("module")
                if m not in self.module_to_idx:
                    continue
                acc = float(st.get("accuracy", 0.5))
                n = min(int(st.get("attempts", 0)), 20)
                if n <= 0:
                    n = 1
                for _ in range(n):
                    mods.append(self.module_to_idx[m])
                    cors.append(1.0 if acc >= 0.6 else 0.0)

        max_len = int(self.meta.get("max_len", max_len))
        return mods[-max_len:], cors[-max_len:]

    def recommend(
        self, module_stats: list[dict], events: list[dict] | None = None
    ) -> dict[str, Any] | None:
        if not self.ready:
            return None
        events = events or []
        max_len = int(self.meta.get("max_len", 150))
        mods, cors = self._stats_to_sequence(module_stats, events, max_len)
        if len(mods) < 2:
            return None

        modules_t = torch.tensor([mods], dtype=torch.long)
        corrects_t = torch.tensor([cors], dtype=torch.float32)

        with torch.no_grad():
            logits = self.model(modules_t, corrects_t)[0]
            probs = torch.sigmoid(logits).numpy()

        # Mastery per module: mean predicted P(correct) at steps where module appears
        mastery_by_mod: dict[str, list[float]] = {m: [] for m in MODULES}
        for t in range(len(mods) - 1):
            key = MODULES[mods[t]]
            mastery_by_mod[key].append(float(probs[t]))

        module_mastery: dict[str, float] = {}
        for m in MODULES:
            vals = mastery_by_mod[m]
            if vals:
                module_mastery[m] = float(np.mean(vals))
            else:
                st = next((s for s in module_stats if s.get("module") == m), None)
                module_mastery[m] = float(st.get("accuracy", 0.5)) if st else 0.5

        weak_list = sorted(MODULES, key=lambda m: module_mastery[m])[:2]
        pred_weak = weak_list[0]
        tasks = [TASK_MAP[m] for m in weak_list]
        if pred_weak not in weak_list:
            tasks.insert(0, TASK_MAP.get(pred_weak, "综合练习"))

        overall = float(np.mean(list(module_mastery.values())))
        return {
            "weak_modules": weak_list,
            "today_tasks": tasks,
            "mastery": round(overall, 3),
            "model_type": "LSTM-DKT",
        }


_engine: DKTEngine | None = None


def get_dkt_engine(models_dir: Path) -> DKTEngine:
    global _engine
    if _engine is None:
        _engine = DKTEngine(models_dir)
    return _engine
