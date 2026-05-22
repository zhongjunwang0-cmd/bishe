"""
AI inference service: LSTM-DKT (EdNet), grammar, pronunciation.
"""
from __future__ import annotations

import os
from pathlib import Path
from typing import Any

import joblib
import numpy as np
from fastapi import FastAPI, File, Form, UploadFile
from pydantic import BaseModel, Field

from app.dkt_engine import TASK_MAP, get_dkt_engine
from app.grammar_engine import get_grammar_engine
from app.pronunciation_engine import get_pronunciation_engine

APP_ROOT = Path(__file__).resolve().parents[1]
PROJECT_ROOT = APP_ROOT.parent
MODELS_DIR = Path(os.getenv("MODELS_DIR", PROJECT_ROOT / "ml" / "models"))

app = FastAPI(title="English Learning AI Service", version="0.2.0")


@app.on_event("startup")
def preload_models() -> None:
    """Warm up Whisper in background; do not block HTTP startup."""
    if os.getenv("PRELOAD_WHISPER", "1") != "1":
        return
    import threading

    def _warm() -> None:
        try:
            engine = get_pronunciation_engine(MODELS_DIR)
            engine.warm_whisper()
        except Exception:
            pass

    threading.Thread(target=_warm, daemon=True).start()


_kt_bundle: dict[str, Any] | None = None
MODULES = ["VOCAB", "GRAMMAR", "READING", "LISTENING", "ORAL"]


def load_gbdt_fallback() -> dict[str, Any] | None:
    global _kt_bundle
    if _kt_bundle is not None:
        return _kt_bundle
    path = MODELS_DIR / "kt_recommend.joblib"
    if path.is_file():
        _kt_bundle = joblib.load(path)
    return _kt_bundle


class ModuleStat(BaseModel):
    module: str
    accuracy: float = 0.5
    attempts: int = 0
    avg_elapsed: float = 30.0


class LearningEvent(BaseModel):
    module: str
    correct: bool = False


class RecommendRequest(BaseModel):
    user_id: str | None = None
    module_stats: list[ModuleStat] = Field(default_factory=list)
    events: list[LearningEvent] = Field(default_factory=list)


class RecommendResponse(BaseModel):
    weak_modules: list[str]
    today_tasks: list[str]
    mastery: float
    model_type: str = "LSTM-DKT"


class GrammarRequest(BaseModel):
    text: str


class GrammarResponse(BaseModel):
    corrected: str
    issues: list[dict[str, str]]
    source: str


class PronunciationResponse(BaseModel):
    score: int
    wer: float
    transcript: str
    misread_words: list[str]
    feedback: str
    suggestions: list[str] = Field(default_factory=list)
    source: str = "Whisper-WER"
    calibrated: bool = False


def _gbdt_recommend(stats: dict[str, ModuleStat]) -> RecommendResponse | None:
    bundle = load_gbdt_fallback()
    if not bundle:
        return None
    vec = []
    for m in MODULES:
        s = stats[m]
        vec.extend([s.accuracy, float(s.attempts), s.avg_elapsed])
    pred = bundle["model"].predict([vec])[0]
    weak = bundle["label_encoder"].inverse_transform([pred])[0]
    weak_list = sorted(MODULES, key=lambda m: (stats[m].accuracy, -stats[m].attempts))[:2]
    tasks = [TASK_MAP[m] for m in weak_list]
    if weak not in weak_list:
        tasks.insert(0, TASK_MAP.get(weak, "综合练习"))
    mastery = float(np.mean([stats[m].accuracy for m in MODULES]))
    return RecommendResponse(
        weak_modules=weak_list,
        today_tasks=tasks,
        mastery=round(mastery, 3),
        model_type="GBDT-fallback",
    )


def _rule_recommend(stats: dict[str, ModuleStat]) -> RecommendResponse:
    weak_list = sorted(MODULES, key=lambda m: (stats[m].accuracy, -stats[m].attempts))[:2]
    tasks = [TASK_MAP[m] for m in weak_list]
    mastery = float(np.mean([stats[m].accuracy for m in MODULES]))
    return RecommendResponse(
        weak_modules=weak_list,
        today_tasks=tasks,
        mastery=round(mastery, 3),
        model_type="rule",
    )


@app.get("/health")
def health():
    dkt = get_dkt_engine(MODELS_DIR)
    grammar = get_grammar_engine(MODELS_DIR)
    pronunciation = get_pronunciation_engine(MODELS_DIR)
    return {
        "status": "ok",
        "dkt_model": dkt.ready,
        "grammar_model": grammar.model_ready,
        "grammar_mode": grammar.mode,
        "pronunciation_model": (MODELS_DIR / "pronunciation_meta.json").is_file(),
        "pronunciation_source": pronunciation.source,
        "gbdt_fallback": (MODELS_DIR / "kt_recommend.joblib").is_file(),
        "models_dir": str(MODELS_DIR),
    }


@app.post("/api/kt/recommend", response_model=RecommendResponse)
def recommend(req: RecommendRequest):
    stats = {s.module: s for s in req.module_stats}
    for m in MODULES:
        if m not in stats:
            stats[m] = ModuleStat(module=m)

    dkt = get_dkt_engine(MODELS_DIR)
    if dkt.ready:
        result = dkt.recommend(
            [s.model_dump() for s in stats.values()],
            [e.model_dump() for e in req.events],
        )
        if result:
            return RecommendResponse(**result)

    gbdt = _gbdt_recommend(stats)
    if gbdt:
        return gbdt
    return _rule_recommend(stats)


@app.post("/api/grammar/correct", response_model=GrammarResponse)
def grammar_correct(req: GrammarRequest):
    grammar = get_grammar_engine(MODELS_DIR)
    result = grammar.correct(req.text.strip())
    return GrammarResponse(**result)


@app.post("/api/pronunciation/score", response_model=PronunciationResponse)
async def pronunciation_score(
    reference_text: str = Form(...),
    audio: UploadFile = File(...),
):
    data = await audio.read()
    suffix = Path(audio.filename or "audio.webm").suffix or ".webm"
    engine = get_pronunciation_engine(MODELS_DIR)
    result = engine.score(reference_text, data, suffix=suffix)
    return PronunciationResponse(**result)
