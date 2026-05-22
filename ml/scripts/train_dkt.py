"""
Train Deep Knowledge Tracing (LSTM-DKT) on EdNet module interaction sequences.

Output:
  ml/models/dkt_model.pt
  ml/models/dkt_meta.json
"""
from __future__ import annotations

import json
import pickle
import sys
from pathlib import Path

import numpy as np
import torch
import torch.nn as nn
from torch.nn.utils.rnn import pack_padded_sequence, pad_packed_sequence
from torch.utils.data import DataLoader, Dataset

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "scripts"))

from _paths import processed_dir, load_paths  # noqa: E402

MAX_LEN = 150
EMBED_DIM = 32
HIDDEN = 64
EPOCHS = 8
BATCH_SIZE = 64
LR = 1e-3


class DKTModel(nn.Module):
    """Classic DKT: LSTM over (module embedding + previous correctness)."""

    def __init__(self, n_modules: int, embed_dim: int = EMBED_DIM, hidden: int = HIDDEN):
        super().__init__()
        self.embed = nn.Embedding(n_modules, embed_dim)
        self.lstm = nn.LSTM(embed_dim + 1, hidden, batch_first=True)
        self.out = nn.Linear(hidden, 1)

    def forward(self, modules: torch.Tensor, corrects: torch.Tensor, lengths: torch.Tensor):
        emb = self.embed(modules)
        x = torch.cat([emb, corrects.unsqueeze(-1)], dim=-1)
        packed = pack_padded_sequence(x, lengths.cpu(), batch_first=True, enforce_sorted=False)
        packed_out, _ = self.lstm(packed)
        out, _ = pad_packed_sequence(packed_out, batch_first=True)
        return self.out(out).squeeze(-1)


class SeqDataset(Dataset):
    def __init__(self, sequences: list[dict], max_len: int = MAX_LEN):
        self.items = []
        for seq in sequences:
            mods = seq["modules"][:max_len]
            cors = seq["corrects"][:max_len]
            if len(mods) < 3:
                continue
            self.items.append((mods, cors))

    def __len__(self):
        return len(self.items)

    def __getitem__(self, idx):
        mods, cors = self.items[idx]
        return np.array(mods, dtype=np.int64), np.array(cors, dtype=np.float32)


def collate(batch):
    lengths = torch.tensor([len(b[0]) for b in batch], dtype=torch.long)
    max_l = int(lengths.max())
    n = len(batch)
    mods = torch.zeros(n, max_l, dtype=torch.long)
    cors = torch.zeros(n, max_l, dtype=torch.float32)
    for i, (m, c) in enumerate(batch):
        L = len(m)
        mods[i, :L] = torch.from_numpy(m)
        cors[i, :L] = torch.from_numpy(c)
    return mods, cors, lengths


def main() -> None:
    p = load_paths()
    data_dir = processed_dir(p)
    seq_path = data_dir / "ednet_dkt_sequences.pkl"
    if not seq_path.is_file():
        raise FileNotFoundError(f"Run prepare_ednet_dkt.py first: {seq_path}")

    with seq_path.open("rb") as f:
        payload = pickle.load(f)
    sequences = payload["sequences"]
    modules = payload["modules"]
    n_modules = len(modules)

    dataset = SeqDataset(sequences)
    loader = DataLoader(dataset, batch_size=BATCH_SIZE, shuffle=True, collate_fn=collate)

    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    model = DKTModel(n_modules).to(device)
    opt = torch.optim.Adam(model.parameters(), lr=LR)
    loss_fn = nn.BCEWithLogitsLoss()

    model.train()
    for epoch in range(1, EPOCHS + 1):
        total_loss = 0.0
        n_batches = 0
        for mods, cors, lengths in loader:
            mods, cors, lengths = mods.to(device), cors.to(device), lengths.to(device)
            B, T = mods.shape
            logits = model(mods, cors, lengths)
            # Predict correctness at t+1 from state at t
            targets = torch.zeros(B, T, device=device)
            mask = torch.zeros(B, T, device=device)
            for b in range(B):
                L = int(lengths[b].item())
                if L < 2:
                    continue
                targets[b, : L - 1] = cors[b, 1:L]
                mask[b, : L - 1] = 1.0
            loss = loss_fn(logits[:, :-1], targets[:, :-1])
            loss = (loss * mask[:, :-1]).sum() / mask[:, :-1].sum().clamp(min=1.0)

            opt.zero_grad()
            loss.backward()
            opt.step()
            total_loss += float(loss.item())
            n_batches += 1
        print(f"Epoch {epoch}/{EPOCHS}  loss={total_loss / max(n_batches, 1):.4f}")

    model_dir = ROOT / "models"
    model_dir.mkdir(parents=True, exist_ok=True)
    torch.save(model.state_dict(), model_dir / "dkt_model.pt")
    meta = {
        "model_type": "LSTM-DKT",
        "modules": modules,
        "n_modules": n_modules,
        "embed_dim": EMBED_DIM,
        "hidden": HIDDEN,
        "max_len": MAX_LEN,
    }
    (model_dir / "dkt_meta.json").write_text(json.dumps(meta, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"Saved DKT -> {model_dir / 'dkt_model.pt'}")


if __name__ == "__main__":
    main()
