#!/usr/bin/env python3
import json, shutil
from pathlib import Path

RECIPES = Path('app/src/main/assets/recipes')
VRAC = Path('VRAC')
VRAC.mkdir(exist_ok=True)
entries = []
for recipe_file in sorted(RECIPES.rglob('recipe.json')):
    data = json.loads(recipe_file.read_text(encoding='utf-8'))
    rid = data.get('id') or recipe_file.parent.name
    country = data.get('countryLabel','')
    category = data.get('categoryLabel','')
    title = data.get('title', recipe_file.parent.name)
    entries.append((country, category, title, rid, recipe_file.parent))

lines = ['LISTE DES ILLUSTRATIONS À DÉPOSER DANS VRAC',
         'Formats acceptés : .jpg .jpeg .png .webp',
         'Nom exact attendu : <id-recette>.jpg (ou .png/.webp)',
         '']
for country, category, title, rid, _ in entries:
    lines.append(f'{country} | {category} | {title} | {rid}.jpg')
(VRAC/'LISTE_IMAGES.txt').write_text('\n'.join(lines)+'\n', encoding='utf-8')

# Si des images ont déjà été déposées, les copier au bon dossier et mettre à jour le JSON.
exts = ['.jpg','.jpeg','.png','.webp']
for country, category, title, rid, target_dir in entries:
    found = None
    for ext in exts:
        p = VRAC / f'{rid}{ext}'
        if p.exists():
            found = p; break
    if not found: continue
    target = target_dir / ('image' + found.suffix.lower())
    shutil.copy2(found, target)
    recipe_file = target_dir/'recipe.json'
    data = json.loads(recipe_file.read_text(encoding='utf-8'))
    data['image'] = target.name
    recipe_file.write_text(json.dumps(data, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')
print(f'{len(entries)} recettes indexées pour VRAC')
