#!/usr/bin/env python3
import base64
import json
import zlib
from pathlib import Path


def main():
    payload_dir = Path("tools/payload")
    chunks = [p.read_text(encoding="utf-8").strip() for p in sorted(payload_dir.glob("part*.txt"))]
    if not chunks:
        raise SystemExit("Aucun payload trouvé")

    decoded = base64.b64decode("".join(chunks))
    files = json.loads(zlib.decompress(decoded).decode("utf-8"))

    for path, content in files.items():
        target = Path(path)
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(content, encoding="utf-8")

    print(f"Projet généré : {len(files)} fichiers.")


if __name__ == "__main__":
    main()
