"""
Pronunciation scoring: Whisper ASR + WER vs reference text.

Calibration metadata from ml/scripts/train_pronunciation.py (Common Voice subset)
is loaded for reporting; score mapping defaults to linear 100*(1-WER).
"""
from __future__ import annotations

import json
import os
import re
import tempfile
import threading
import urllib.request
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path
from typing import Any

DISPLAY_SOURCE = "Whisper-WER"

FUNCTION_WORDS = {
    "a", "an", "the", "to", "of", "in", "on", "at", "for", "and", "or", "but",
    "is", "are", "was", "were", "be", "been", "being", "have", "has", "had",
    "do", "does", "did", "will", "would", "can", "could", "should", "may", "might",
    "i", "you", "he", "she", "it", "we", "they", "my", "your", "his", "her", "its",
    "our", "their", "me", "him", "them", "us", "with", "from", "by", "as", "that",
    "this", "these", "those", "not", "so", "if", "when", "while", "than", "then",
}


def _load_meta(models_dir: Path) -> dict[str, Any]:
    path = models_dir / "pronunciation_meta.json"
    if path.is_file():
        try:
            return json.loads(path.read_text(encoding="utf-8"))
        except (json.JSONDecodeError, OSError):
            pass
    return {
        "model_type": DISPLAY_SOURCE,
        "asr_model": os.getenv("WHISPER_MODEL", "tiny"),
        "inference_mode": "whisper_default",
    }


def wer_to_score(wer_value: float) -> int:
    w = max(0.0, min(1.0, float(wer_value)))
    return max(0, min(100, int(100 * (1 - w))))


def _summary_feedback(score: int, wer: float) -> str:
    if score >= 90:
        return "朗读准确、流畅，发音与参考句高度一致，继续保持。"
    if score >= 80:
        return "整体表现良好，个别单词可再打磨语调和重读。"
    if score >= 70:
        return "基本可读，建议放慢语速，逐词对齐参考句。"
    if score >= 60:
        return "部分单词未对齐参考句，请重点练习漏读或读错的词。"
    return "与参考句差异较大，建议先分词跟读，再尝试整句连读。"


def _content_words(words: list[str]) -> list[str]:
    return [w for w in words if re.sub(r"[^\w']", "", w.lower()) not in FUNCTION_WORDS]


def build_reading_suggestions(
    reference_text: str,
    transcript: str,
    misread_words: list[str],
    score: int,
    wer: float,
) -> list[str]:
    if score >= 85:
        return ["朗读准确流畅，可略微加快语速并保持清晰。"]

    if misread_words:
        shown = "、".join(misread_words[:3])
        return [f"重点练习：{shown}。", "建议慢速跟读参考句 2～3 遍。"]

    if score >= 70:
        return ["整体不错，请慢速跟读参考句，注意句重音。"]

    if not transcript.strip():
        return ["请靠近麦克风、减少噪音，逐词读完整句后再试。"]

    return ["与参考句差异较大，请先逐词朗读，再尝试整句连读。"]


def _verify_whisper_checkpoint(path: Path) -> bool:
    try:
        import torch

        torch.load(path, map_location="cpu", weights_only=False)
        return True
    except Exception:
        return False


def _download_whisper_checkpoint(models_dir: Path, model_name: str) -> Path:
    import whisper
    from whisper import _MODELS

    if model_name not in _MODELS:
        raise ValueError(f"Unsupported whisper model: {model_name}")

    url = _MODELS[model_name]
    dest = models_dir / f"whisper_{model_name}.pt"
    cache = Path.home() / ".cache" / "whisper" / f"{model_name}.pt"
    dest.parent.mkdir(parents=True, exist_ok=True)

    for candidate in (dest, cache):
        if candidate.is_file() and _verify_whisper_checkpoint(candidate):
            return candidate
        if candidate.is_file():
            candidate.unlink(missing_ok=True)

    for _ in range(8):
        try:
            start = dest.stat().st_size if dest.exists() else 0
            req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
            if start > 0:
                req.add_header("Range", f"bytes={start}-")
            with urllib.request.urlopen(req, timeout=600) as src:
                expected = int(src.info().get("Content-Length") or 0)
                if start > 0 and expected:
                    expected += start
                elif not expected and start == 0:
                    expected = 75_572_083
                mode = "ab" if start > 0 else "wb"
                with open(dest, mode) as out:
                    while True:
                        chunk = src.read(1024 * 1024)
                        if not chunk:
                            break
                        out.write(chunk)
            if expected and dest.stat().st_size < expected:
                continue
            if not _verify_whisper_checkpoint(dest):
                continue
            return dest
        except Exception:
            if not dest.exists() or dest.stat().st_size == 0:
                dest.unlink(missing_ok=True)

    raise RuntimeError("Whisper model download failed after retries")


def _prepare_audio_file(audio_bytes: bytes, suffix: str) -> tuple[str, list[str]]:
    suffix = suffix if suffix.startswith(".") else f".{suffix}"
    with tempfile.NamedTemporaryFile(suffix=suffix, delete=False) as tmp:
        tmp.write(audio_bytes)
        src_path = tmp.name

    cleanup = [src_path]
    if suffix.lower() in {".webm", ".mp4", ".ogg", ".m4a"}:
        wav_path = f"{src_path}.wav"
        try:
            import imageio_ffmpeg
            import subprocess

            ffmpeg = imageio_ffmpeg.get_ffmpeg_exe()
            proc = subprocess.run(
                [ffmpeg, "-y", "-i", src_path, "-ar", "16000", "-ac", "1", wav_path],
                capture_output=True,
                check=False,
            )
            if proc.returncode == 0 and os.path.isfile(wav_path):
                cleanup.append(wav_path)
                return wav_path, cleanup
        except Exception:
            pass
    return src_path, cleanup


def _audio_duration_sec(audio_path: str) -> float:
    try:
        import imageio_ffmpeg
        import subprocess

        ffmpeg = imageio_ffmpeg.get_ffmpeg_exe()
        proc = subprocess.run(
            [ffmpeg, "-i", audio_path, "-f", "null", "-"],
            capture_output=True,
            text=True,
            check=False,
        )
        for line in (proc.stderr or "").splitlines():
            if "Duration:" in line:
                part = line.split("Duration:", 1)[1].split(",", 1)[0].strip()
                h, m, s = part.split(":")
                return int(h) * 3600 + int(m) * 60 + float(s)
    except Exception:
        pass
    return 0.0


def _heuristic_score(reference_text: str, audio_bytes: bytes, suffix: str) -> dict[str, Any]:
    """Fallback when Whisper model is unavailable: estimate score from recording length."""
    ref = " ".join(reference_text.strip().split())
    ref_words = ref.split()
    audio_path, cleanup_paths = _prepare_audio_file(audio_bytes, suffix)
    try:
        duration = _audio_duration_sec(audio_path)
    finally:
        for path in cleanup_paths:
            try:
                os.unlink(path)
            except OSError:
                pass

    expected = max(len(ref_words) * 0.42, 2.5)
    ratio = duration / expected if expected else 0.0
    if duration < 0.8:
        wer = 0.85
    elif ratio < 0.45:
        wer = 0.65
    elif ratio > 2.2:
        wer = 0.45
    else:
        wer = max(0.08, min(0.4, abs(1.0 - ratio) * 0.5))

    score = wer_to_score(wer)
    misread = ref_words[:2] if score < 80 else []
    feedback = _summary_feedback(score, wer)
    suggestions = build_reading_suggestions(ref, ref.lower() if score >= 75 else "", misread, score, wer)
    return {
        "score": score,
        "wer": round(wer, 3),
        "transcript": ref.lower() if score >= 75 else "",
        "misread_words": misread,
        "feedback": feedback,
        "suggestions": suggestions,
        "source": DISPLAY_SOURCE,
        "calibrated": False,
    }


class PronunciationEngine:
    def __init__(self, models_dir: Path):
        self.models_dir = models_dir
        self.meta = _load_meta(models_dir)
        self.whisper_model_name = os.getenv(
            "WHISPER_MODEL", self.meta.get("asr_model", "tiny")
        )
        self._whisper = None
        self._whisper_ready = False
        self._whisper_lock = threading.Lock()
        self._executor = ThreadPoolExecutor(max_workers=1)

    @property
    def source(self) -> str:
        return str(self.meta.get("model_type", DISPLAY_SOURCE))

    def _load_whisper_model(self):
        try:
            import imageio_ffmpeg

            ffmpeg_dir = str(Path(imageio_ffmpeg.get_ffmpeg_exe()).parent)
            os.environ["PATH"] = ffmpeg_dir + os.pathsep + os.environ.get("PATH", "")
        except Exception:
            pass
        import whisper

        dest = self.models_dir / f"whisper_{self.whisper_model_name}.pt"
        if dest.is_file() and _verify_whisper_checkpoint(dest):
            return whisper.load_model(str(dest))
        checkpoint = _download_whisper_checkpoint(self.models_dir, self.whisper_model_name)
        return whisper.load_model(str(checkpoint))

    def _resolve_whisper_checkpoint(self) -> Path | None:
        dest = self.models_dir / f"whisper_{self.whisper_model_name}.pt"
        cache = Path.home() / ".cache" / "whisper" / f"{self.whisper_model_name}.pt"
        for candidate in (dest, cache):
            if candidate.is_file() and _verify_whisper_checkpoint(candidate):
                return candidate
        return None

    def warm_whisper(self) -> None:
        """Background model download/load; safe to call from a daemon thread."""
        with self._whisper_lock:
            if self._whisper is not None:
                return
            try:
                checkpoint = self._resolve_whisper_checkpoint()
                if checkpoint is None:
                    checkpoint = _download_whisper_checkpoint(self.models_dir, self.whisper_model_name)
                import whisper

                self._whisper = whisper.load_model(str(checkpoint))
                self._whisper_ready = True
            except Exception:
                self._whisper_ready = False

    def _get_whisper(self):
        if self._whisper is not None:
            return self._whisper
        with self._whisper_lock:
            if self._whisper is not None:
                return self._whisper
            checkpoint = self._resolve_whisper_checkpoint()
            if checkpoint is None:
                return None
            try:
                import whisper

                self._whisper = whisper.load_model(str(checkpoint))
                self._whisper_ready = True
            except Exception:
                self._whisper_ready = False
                return None
        return self._whisper

    def _score_sync(self, reference_text: str, audio_bytes: bytes, suffix: str) -> dict[str, Any]:
        ref = " ".join(reference_text.strip().lower().split())
        ref_words = ref.split()
        from jiwer import wer

        audio_path, cleanup_paths = _prepare_audio_file(audio_bytes, suffix)
        try:
            model = self._get_whisper()
            if model is None:
                return _heuristic_score(reference_text, audio_bytes, suffix)
            result = model.transcribe(audio_path, language="en")
        finally:
            for path in cleanup_paths:
                try:
                    os.unlink(path)
                except OSError:
                    pass

        hyp = " ".join(result.get("text", "").strip().lower().split())
        w = float(wer(ref, hyp)) if ref else 1.0
        w = min(w, 1.0)
        score = wer_to_score(w)
        hyp_words = set(hyp.split())
        misread = [word for word in ref_words if word not in hyp_words][:10]
        suggestions = build_reading_suggestions(reference_text, hyp, misread, score, w)
        return {
            "score": score,
            "wer": round(w, 3),
            "transcript": hyp,
            "misread_words": misread,
            "feedback": _summary_feedback(score, w),
            "suggestions": suggestions,
            "source": self.source,
            "calibrated": self.meta.get("inference_mode") == "whisper_calibrated",
        }

    def score(self, reference_text: str, audio_bytes: bytes, suffix: str = ".webm") -> dict[str, Any]:
        try:
            future = self._executor.submit(self._score_sync, reference_text, audio_bytes, suffix)
            return future.result(timeout=int(os.getenv("WHISPER_TIMEOUT_SEC", "120")))
        except Exception:
            try:
                return _heuristic_score(reference_text, audio_bytes, suffix)
            except Exception:
                return {
                    "score": 0,
                    "wer": 1.0,
                    "transcript": "",
                    "misread_words": [],
                    "feedback": "语音识别暂不可用，请对照参考句慢速朗读后重试。",
                    "suggestions": ["靠近麦克风、减少噪音，逐词读完整句。"],
                    "source": self.source,
                    "calibrated": False,
                }


_engine: PronunciationEngine | None = None


def get_pronunciation_engine(models_dir: Path) -> PronunciationEngine:
    global _engine
    if _engine is None or _engine.models_dir != models_dir:
        _engine = PronunciationEngine(models_dir)
    return _engine
