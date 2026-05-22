"""
Fine-tune T5-small on JFLEG for grammar correction (GEC).

Input:  ml/data/processed/jfleg_subset.jsonl
Output: ml/models/grammar_t5/  (+ grammar_t5_meta.json)
"""
from __future__ import annotations

import json
import sys
from pathlib import Path

import torch
from torch.utils.data import DataLoader, Dataset
from tqdm import tqdm
from transformers import T5ForConditionalGeneration, T5Tokenizer

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "scripts"))

from _paths import load_paths, processed_dir  # noqa: E402

PREFIX = "grammar: "
MAX_LEN = 128
BATCH_SIZE = 8
EPOCHS = 1
LR = 3e-4
MODEL_NAME = "t5-small"
MAX_TRAIN_SAMPLES = 300
MAX_EVAL_SAMPLES = 30
EVAL_BEAMS = 1


class JflegDataset(Dataset):
    def __init__(self, rows: list[dict], tokenizer: T5Tokenizer):
        self.rows = rows
        self.tokenizer = tokenizer

    def __len__(self) -> int:
        return len(self.rows)

    def __getitem__(self, idx: int) -> dict[str, torch.Tensor]:
        row = self.rows[idx]
        src = PREFIX + row["source"]
        tgt = row["target"]
        enc = self.tokenizer(
            src,
            max_length=MAX_LEN,
            truncation=True,
            padding="max_length",
            return_tensors="pt",
        )
        dec = self.tokenizer(
            tgt,
            max_length=MAX_LEN,
            truncation=True,
            padding="max_length",
            return_tensors="pt",
        )
        labels = dec["input_ids"].squeeze(0)
        labels[labels == self.tokenizer.pad_token_id] = -100
        return {
            "input_ids": enc["input_ids"].squeeze(0),
            "attention_mask": enc["attention_mask"].squeeze(0),
            "labels": labels,
        }


def load_jsonl(path: Path) -> list[dict]:
    rows = []
    with path.open(encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if line:
                rows.append(json.loads(line))
    return rows


def evaluate(model, loader, tokenizer, device) -> float:
    model.eval()
    total, match = 0, 0
    with torch.no_grad():
        for batch in loader:
            input_ids = batch["input_ids"].to(device)
            attention_mask = batch["attention_mask"].to(device)
            labels = batch["labels"].to(device)
            out = model.generate(
                input_ids=input_ids,
                attention_mask=attention_mask,
                max_length=MAX_LEN,
                num_beams=EVAL_BEAMS,
                early_stopping=EVAL_BEAMS > 1,
            )
            for pred_ids, label_ids in zip(out, labels):
                pred = tokenizer.decode(pred_ids, skip_special_tokens=True).strip()
                label_ids = label_ids.clone()
                label_ids[label_ids == -100] = tokenizer.pad_token_id
                ref = tokenizer.decode(label_ids, skip_special_tokens=True).strip()
                total += 1
                if pred.lower() == ref.lower():
                    match += 1
    model.train()
    return match / max(total, 1)


def main() -> None:
    p = load_paths()
    data_path = processed_dir(p) / "jfleg_subset.jsonl"
    if not data_path.is_file():
        raise FileNotFoundError(f"Run prepare_jfleg.py first: {data_path}")

    all_rows = load_jsonl(data_path)
    train_rows = [r for r in all_rows if r.get("split") == "train"]
    val_rows = [r for r in all_rows if r.get("split") == "val"]
    test_rows = [r for r in all_rows if r.get("split") == "test"]
    if not train_rows:
        raise RuntimeError("No train rows in jfleg_subset.jsonl")

    if MAX_TRAIN_SAMPLES and len(train_rows) > MAX_TRAIN_SAMPLES:
        train_rows = train_rows[:MAX_TRAIN_SAMPLES]
    if MAX_EVAL_SAMPLES:
        val_rows = val_rows[:MAX_EVAL_SAMPLES]
        test_rows = test_rows[:MAX_EVAL_SAMPLES]

    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    print(
        f"Device: {device}  train={len(train_rows)} val={len(val_rows)} test={len(test_rows)}"
        f"  epochs={EPOCHS} beams={EVAL_BEAMS}",
        flush=True,
    )

    tokenizer = T5Tokenizer.from_pretrained(MODEL_NAME)
    try:
        model = T5ForConditionalGeneration.from_pretrained(MODEL_NAME, local_files_only=True).to(device)
    except Exception as exc:
        print(f"Local model unavailable ({type(exc).__name__}); downloading {MODEL_NAME}...", flush=True)
        model = T5ForConditionalGeneration.from_pretrained(MODEL_NAME).to(device)
    train_loader = DataLoader(
        JflegDataset(train_rows, tokenizer),
        batch_size=BATCH_SIZE,
        shuffle=True,
    )
    val_loader = DataLoader(JflegDataset(val_rows, tokenizer), batch_size=BATCH_SIZE) if val_rows else None
    test_loader = DataLoader(JflegDataset(test_rows, tokenizer), batch_size=BATCH_SIZE) if test_rows else None

    opt = torch.optim.AdamW(model.parameters(), lr=LR)
    model.train()

    for epoch in range(1, EPOCHS + 1):
        total_loss = 0.0
        n_batches = 0
        for batch in tqdm(train_loader, desc=f"Epoch {epoch}/{EPOCHS}"):
            input_ids = batch["input_ids"].to(device)
            attention_mask = batch["attention_mask"].to(device)
            labels = batch["labels"].to(device)
            out = model(input_ids=input_ids, attention_mask=attention_mask, labels=labels)
            loss = out.loss
            opt.zero_grad()
            loss.backward()
            opt.step()
            total_loss += float(loss.item())
            n_batches += 1
        avg_loss = total_loss / max(n_batches, 1)
        val_exact = evaluate(model, val_loader, tokenizer, device) if val_loader else 0.0
        print(f"Epoch {epoch}  loss={avg_loss:.4f}  val_exact_match={val_exact:.3f}", flush=True)

    test_exact = evaluate(model, test_loader, tokenizer, device) if test_loader else 0.0
    print(f"Test exact match: {test_exact:.3f}")

    model_dir = ROOT / "models" / "grammar_t5"
    model_dir.mkdir(parents=True, exist_ok=True)
    model.save_pretrained(model_dir)
    tokenizer.save_pretrained(model_dir)
    meta = {
        "model_type": "T5-GEC",
        "base_model": MODEL_NAME,
        "prefix": PREFIX,
        "max_len": MAX_LEN,
        "train_samples": len(train_rows),
        "val_samples": len(val_rows),
        "test_samples": len(test_rows),
        "test_exact_match": round(test_exact, 4),
        "epochs": EPOCHS,
    }
    (ROOT / "models" / "grammar_t5_meta.json").write_text(
        json.dumps(meta, ensure_ascii=False, indent=2), encoding="utf-8"
    )
    print(f"Saved grammar model -> {model_dir}")


if __name__ == "__main__":
    main()
