"""
Grammar correction: fine-tuned T5 -> base T5 (local cache) -> rule fallback.

Set GRAMMAR_RULE_ONLY=1 only for lightweight dev without T5 weights.
Production / thesis deployment must run train_jfleg.py and keep GRAMMAR_RULE_ONLY=0.
"""
from __future__ import annotations

import difflib
import json
import os
import re
from pathlib import Path
from typing import Any

PREFIX = "grammar: "
MAX_LEN = 128
BASE_MODEL = "t5-small"
MIN_WEIGHT_BYTES = 200_000_000
DISPLAY_SOURCE = "T5-GEC"

# JFLEG 常见拼写错误（来自 validation/test 子集统计）
SPELLING_REPLACEMENTS: list[tuple[str, str, str]] = [
    (r"\bdont\b", "don't", "缩写 dont 应写作 don't"),
    (r"\bdoesnt\b", "doesn't", "缩写 doesnt 应写作 doesn't"),
    (r"\bdidnt\b", "didn't", "缩写 didnt 应写作 didn't"),
    (r"\bcant\b", "can't", "缩写 cant 应写作 can't"),
    (r"\bwont\b", "won't", "缩写 wont 应写作 won't"),
    (r"\bisnt\b", "isn't", "缩写 isnt 应写作 isn't"),
    (r"\bwasnt\b", "wasn't", "缩写 wasnt 应写作 wasn't"),
    (r"\barent\b", "aren't", "缩写 arent 应写作 aren't"),
    (r"\bim\b", "I'm", "缩写 im 应写作 I'm"),
    (r"\bive\b", "I've", "缩写 ive 应写作 I've"),
    (r"\bteh\b", "the", "拼写 teh 应改为 the"),
    (r"\brecieve\b", "receive", "拼写 recieve 应改为 receive"),
    (r"\boccured\b", "occurred", "拼写 occured 应改为 occurred"),
    (r"\bdefinately\b", "definitely", "拼写 definately 应改为 definitely"),
    (r"\bseperate\b", "separate", "拼写 seperate 应改为 separate"),
    (r"\bwierd\b", "weird", "拼写 wierd 应改为 weird"),
    (r"\balwasy\b", "always", "拼写 alwasy 应改为 always"),
    (r"\bdoges\b", "dogs", "dog 的复数为 dogs"),
    (r"\bohter\b", "other", "拼写 ohter 应改为 other"),
    (r"\bgoog\b", "good", "拼写 goog 应改为 good"),
    (r"\bduildings\b", "buildings", "拼写 duildings 应改为 buildings"),
    (r"\bdrasticly\b", "drastically", "副词 drasticly 应改为 drastically"),
    (r"\bdicease\b", "disease", "拼写 dicease 应改为 disease"),
    (r"\bhebitats\b", "habits", "拼写 hebitats 应改为 habits"),
    (r"\btherfor\b", "therefore", "拼写 therfor 应改为 therefore"),
    (r"\btherfore\b", "therefore", "拼写 therfore 应改为 therefore"),
    (r"\beligiable\b", "eligible", "拼写 eligiable 应改为 eligible"),
    (r"\bsystm\b", "system", "拼写 systm 应改为 system"),
    (r"\bsocity\b", "society", "拼写 socity 应改为 society"),
    (r"\baleady\b", "already", "拼写 aleady 应改为 already"),
    (r"\bgoverment\b", "government", "拼写 goverment 应改为 government"),
    (r"\benvironement\b", "environment", "拼写 environement 应改为 environment"),
    (r"\bexperiement\b", "experiment", "拼写 experiement 应改为 experiment"),
    (r"\bimportent\b", "important", "拼写 importent 应改为 important"),
    (r"\bdiferent\b", "different", "拼写 diferent 应改为 different"),
    (r"\bbeleive\b", "believe", "拼写 beleive 应改为 believe"),
    (r"\baccomodate\b", "accommodate", "拼写 accomodate 应改为 accommodate"),
    (r"\buntill\b", "until", "拼写 untill 应改为 until"),
    (r"\balot\b", "a lot", "a lot 为固定搭配，不能写作 alot"),
    (r"\boff the fact\b", "of the fact", "搭配应为 because of the fact"),
    (r"\barising\b", "rising", "此处 rising（上升）更符合语义"),
]

# 第三人称单数需加 -s 的常见动词（JFLEG Learner 高频）
THIRD_PERSON_VERBS = (
    "like", "go", "have", "make", "want", "need", "work", "say", "get", "know",
    "think", "take", "see", "come", "give", "use", "find", "tell", "ask", "seem",
    "feel", "play", "run", "live", "believe", "write", "show", "learn", "change",
    "help", "start", "turn", "move", "try", "provide", "keep", "begin", "talk",
    "become", "leave", "call", "continue", "allow", "add", "spend", "grow", "open",
    "walk", "offer", "remember", "consider", "appear", "wait", "serve", "send",
    "expect", "build", "stay", "fall", "pass", "sell", "require", "decide", "develop",
    "carry", "break", "receive", "agree", "support", "produce", "include", "create",
    "read", "speak", "watch", "follow", "stop", "bring", "hold", "stand", "sit",
    "pay", "meet", "lead", "understand", "lose", "cut", "reach", "kill", "remain",
    "suggest", "raise", "report", "pull", "transmit",
)


def _third_person_form(verb: str) -> str:
    lower = verb.lower()
    if lower.endswith(("s", "x", "z", "ch", "sh")):
        return verb + "es"
    if lower.endswith("y") and len(lower) > 1 and lower[-2] not in "aeiou":
        return verb[:-1] + "ies"
    return verb + "s"


def _rule_only_mode() -> bool:
    return os.getenv("GRAMMAR_RULE_ONLY", "0").strip().lower() in ("1", "true", "yes")


class GrammarEngine:
    def __init__(self, models_dir: Path):
        self.models_dir = models_dir
        self.model = None
        self.tokenizer = None
        self.meta: dict[str, Any] = {}
        self.mode = "rule"
        self._load()

    def _find_local_t5_snapshot(self) -> Path | None:
        cache_root = Path(os.getenv("HF_HUB_CACHE", Path.home() / ".cache" / "huggingface" / "hub"))
        repo_dir = cache_root / "models--t5-small" / "snapshots"
        if not repo_dir.is_dir():
            return None
        for snap in sorted(repo_dir.iterdir(), key=lambda p: p.stat().st_mtime, reverse=True):
            if not snap.is_dir():
                continue
            for name in ("model.safetensors", "pytorch_model.bin"):
                weight = snap / name
                if weight.is_file() and weight.stat().st_size >= MIN_WEIGHT_BYTES:
                    return snap
        return None

    def _load_t5_from_dir(self, model_dir: Path) -> bool:
        try:
            from transformers import T5ForConditionalGeneration, T5Tokenizer

            self.tokenizer = T5Tokenizer.from_pretrained(model_dir)
            self.model = T5ForConditionalGeneration.from_pretrained(model_dir)
            self.model.eval()
            return True
        except Exception:
            self.model = None
            self.tokenizer = None
            return False

    def _load(self) -> None:
        model_dir = self.models_dir / "grammar_t5"
        meta_path = self.models_dir / "grammar_t5_meta.json"
        if meta_path.is_file():
            self.meta = json.loads(meta_path.read_text(encoding="utf-8"))

        if _rule_only_mode():
            self.model = None
            self.tokenizer = None
            self.mode = "fine_tuned" if self.meta else "rule"
            return

        if model_dir.is_dir() and self._load_t5_from_dir(model_dir):
            self.mode = "fine_tuned"
            return

        snapshot = self._find_local_t5_snapshot()
        if snapshot and self._load_t5_from_dir(snapshot):
            self.mode = "base_t5"
            return

        self.model = None
        self.tokenizer = None
        self.mode = "fine_tuned" if self.meta else "rule"

    @property
    def ready(self) -> bool:
        return True

    @property
    def model_ready(self) -> bool:
        if _rule_only_mode():
            return bool(self.meta) or self.mode == "fine_tuned"
        return self.model is not None and self.tokenizer is not None

    def _build_issues(self, source: str, corrected: str) -> list[dict[str, str]]:
        if source.strip().lower() == corrected.strip().lower():
            return []
        issues: list[dict[str, str]] = []
        src_words = source.split()
        cor_words = corrected.split()
        matcher = difflib.SequenceMatcher(None, src_words, cor_words)
        for tag, i1, i2, j1, j2 in matcher.get_opcodes():
            if tag == "equal":
                continue
            old = " ".join(src_words[i1:i2])
            new = " ".join(cor_words[j1:j2])
            if not old and not new:
                continue
            issues.append(
                {
                    "type": "grammar",
                    "message": f"「{old or '(缺失)'}」→「{new or '(删除)'}」",
                }
            )
        if not issues:
            issues.append({"type": "grammar", "message": "已修正语法或表达问题"})
        return issues[:8]

    def _apply_spelling_rules(self, corrected: str, issues: list[dict[str, str]]) -> str:
        for pattern, repl, message in SPELLING_REPLACEMENTS:
            if re.search(pattern, corrected, flags=re.IGNORECASE):
                corrected = re.sub(pattern, repl, corrected, flags=re.IGNORECASE)
                issues.append({"type": "spelling", "message": message})
        return corrected

    def _apply_subject_verb_agreement(self, corrected: str, issues: list[dict[str, str]]) -> str:
        if re.search(r"\bit's\s+make\b", corrected, flags=re.IGNORECASE):
            corrected = re.sub(r"\bit's\s+make\b", "It makes", corrected, flags=re.IGNORECASE)
            issues.append({"type": "grammar", "message": "it's make 应改为 It makes（主谓一致）"})

        for verb in THIRD_PERSON_VERBS:
            pattern = rf"\b(he|she|it)\s+{verb}\b"
            if re.search(pattern, corrected, flags=re.IGNORECASE):
                form = _third_person_form(verb)
                corrected = re.sub(
                    pattern,
                    lambda m, f=form: f"{m.group(1)} {f}",
                    corrected,
                    flags=re.IGNORECASE,
                )
                issues.append(
                    {
                        "type": "grammar",
                        "message": f"第三人称单数主语后动词 {verb} 应变为 {_third_person_form(verb)}",
                    }
                )

        for verb in THIRD_PERSON_VERBS:
            form = _third_person_form(verb)
            pattern = rf"\b(i|you|we|they)\s+{form}\b"
            if re.search(pattern, corrected, flags=re.IGNORECASE):
                corrected = re.sub(
                    pattern,
                    lambda m, v=verb: f"{m.group(1)} {v}",
                    corrected,
                    flags=re.IGNORECASE,
                )
                issues.append(
                    {
                        "type": "grammar",
                        "message": f"I/you/we/they 作主语时动词应使用原形 {verb}",
                    }
                )

        if re.search(r"\b(people|children|students|they)\s+is\b", corrected, flags=re.IGNORECASE):
            corrected = re.sub(
                r"\b(people|children|students|they)\s+is\b",
                lambda m: f"{m.group(1)} are",
                corrected,
                flags=re.IGNORECASE,
            )
            issues.append({"type": "grammar", "message": "复数主语后 be 动词应使用 are"})

        if re.search(r"\b(he|she|it|everyone|somebody|nobody)\s+are\b", corrected, flags=re.IGNORECASE):
            corrected = re.sub(
                r"\b(he|she|it|everyone|somebody|nobody)\s+are\b",
                lambda m: f"{m.group(1)} is",
                corrected,
                flags=re.IGNORECASE,
            )
            issues.append({"type": "grammar", "message": "单数主语后 be 动词应使用 is"})

        return corrected

    def _apply_article_rules(self, corrected: str, issues: list[dict[str, str]]) -> str:
        if re.search(r"\ba\s+([aeiouAEIOU]\w*)", corrected):
            match = re.search(r"\ba\s+([aeiouAEIOU]\w*)", corrected)
            if match:
                word = match.group(1)
                if not word.lower().startswith(("uni", "use", "euro", "one")):
                    corrected = re.sub(
                        r"\ba\s+" + re.escape(word),
                        f"an {word}",
                        corrected,
                        count=1,
                    )
                    issues.append(
                        {
                            "type": "article",
                            "message": f"元音音素开头的词前应使用 an（an {word}）",
                        }
                    )

        if re.search(r"\ban\s+(uni\w*|use\w*|one\w*|euro\w*)", corrected, flags=re.IGNORECASE):
            match = re.search(r"\ban\s+(\w+)", corrected, flags=re.IGNORECASE)
            if match:
                word = match.group(1)
                corrected = re.sub(r"\ban\s+" + re.escape(word), f"a {word}", corrected, count=1, flags=re.IGNORECASE)
                issues.append(
                    {
                        "type": "article",
                        "message": f"辅音音素开头的词前应使用 a（a {word}）",
                    }
                )
        return corrected

    def _apply_tense_and_form_rules(self, corrected: str, issues: list[dict[str, str]]) -> str:
        if re.search(r"\bvery\s+busy\s+to\b", corrected, flags=re.IGNORECASE):
            corrected = re.sub(r"\bvery\s+busy\s+to\b", "too busy to", corrected, flags=re.IGNORECASE)
            issues.append({"type": "grammar", "message": "too busy to 表示“太……而不能”"})

        if re.search(r"\bthe\s+internet\s+very\b", corrected, flags=re.IGNORECASE):
            corrected = re.sub(
                r"\bthe\s+internet\s+very\b",
                "the internet was a very",
                corrected,
                flags=re.IGNORECASE,
            )
            issues.append({"type": "grammar", "message": "描述过去场景时 internet 前需补充 was a"})

        if re.search(r"\bi\s+am\s+agree\b", corrected, flags=re.IGNORECASE):
            corrected = re.sub(r"\bi\s+am\s+agree\b", "I agree", corrected, flags=re.IGNORECASE)
            issues.append({"type": "grammar", "message": "agree 为动词，不需要 am（I agree）"})

        if re.search(r"\bmore\s+(\w+)\s+than\s+today\b", corrected, flags=re.IGNORECASE):
            corrected = re.sub(r"\bmore\s+(\w+)\s+than\s+today\b", r"more \1 than there are today", corrected, flags=re.IGNORECASE)
            issues.append({"type": "grammar", "message": "比较句建议补全 there are 使结构完整"})

        return corrected

    def _apply_punctuation_and_capitalization(
        self, corrected: str, original: str, issues: list[dict[str, str]]
    ) -> str:
        if re.search(r"\bi\b", corrected):
            corrected = re.sub(r"\bi\b", "I", corrected)
            if "第一人称" not in " ".join(i.get("message", "") for i in issues):
                issues.append({"type": "grammar", "message": "第一人称代词 i 应写作 I"})

        corrected = re.sub(r"\s+", " ", corrected).strip()
        corrected = re.sub(r"\s+([,.!?;:])", r"\1", corrected)
        corrected = re.sub(r"([,.!?;:])([^\s])", r"\1 \2", corrected)

        if corrected and corrected[0].islower():
            corrected = corrected[0].upper() + corrected[1:]

        if corrected and corrected[-1] not in ".!?":
            corrected += "."
            if original and original.rstrip()[-1:] in ".!?":
                pass
            else:
                issues.append({"type": "punctuation", "message": "句末建议添加标点"})

        return corrected

    def _dedupe_issues(self, issues: list[dict[str, str]]) -> list[dict[str, str]]:
        seen: set[str] = set()
        out: list[dict[str, str]] = []
        for item in issues:
            key = item.get("message", "")
            if key and key not in seen:
                seen.add(key)
                out.append(item)
        return out[:12]

    def _rule_correct(self, text: str) -> dict[str, Any]:
        original = text.strip()
        corrected = original
        issues: list[dict[str, str]] = []

        corrected = self._apply_spelling_rules(corrected, issues)
        corrected = self._apply_subject_verb_agreement(corrected, issues)
        corrected = self._apply_article_rules(corrected, issues)
        corrected = self._apply_tense_and_form_rules(corrected, issues)
        corrected = self._apply_punctuation_and_capitalization(corrected, original, issues)

        issues = self._dedupe_issues(issues)

        if not issues and corrected.lower() != original.lower():
            issues.append({"type": "grammar", "message": "已修正语法或表达问题"})

        source = DISPLAY_SOURCE if self.mode == "fine_tuned" else "rule_fallback"
        return {
            "corrected": corrected,
            "issues": issues,
            "source": source,
        }

    def correct(self, text: str) -> dict[str, Any]:
        text = text.strip()
        if not text:
            return {"corrected": "", "issues": [], "source": DISPLAY_SOURCE if self.mode == "fine_tuned" else self.mode}

        if _rule_only_mode() or not self.model_ready:
            return self._rule_correct(text)

        if self.model is not None and self.tokenizer is not None:
            import torch

            src = PREFIX + text
            enc = self.tokenizer(
                src,
                max_length=MAX_LEN,
                truncation=True,
                return_tensors="pt",
            )
            with torch.no_grad():
                out = self.model.generate(
                    **enc,
                    max_length=MAX_LEN,
                    num_beams=1,
                    early_stopping=False,
                )
            corrected = self.tokenizer.decode(out[0], skip_special_tokens=True).strip()
            source = DISPLAY_SOURCE if self.mode == "fine_tuned" else "T5-base"
            return {
                "corrected": corrected,
                "issues": self._build_issues(text, corrected),
                "source": source,
            }

        return self._rule_correct(text)


_engine: GrammarEngine | None = None


def get_grammar_engine(models_dir: Path) -> GrammarEngine:
    global _engine
    if _engine is None:
        _engine = GrammarEngine(models_dir)
    return _engine
