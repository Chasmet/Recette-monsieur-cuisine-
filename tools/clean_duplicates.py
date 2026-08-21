#!/usr/bin/env python3
import json, re, shutil, unicodedata
from pathlib import Path

ROOT = Path('app/src/main/assets/recipes')

def norm(s):
    s = unicodedata.normalize('NFKD', s).encode('ascii','ignore').decode().lower()
    s = s.replace('oe','oe').replace('boeuf','boeuf')
    s = re.sub(r'[^a-z0-9]+',' ',s).strip()
    return s

# Doublons sémantiques hérités de la première version. On garde la meilleure fiche,
# généralement la fiche historique détaillée quand elle existe.
GROUPS = [
 ('france','desserts',['Crème brûlée']),
 ('france','entrees',['Oeufs mimosa','Œufs mimosa']),
 ('france','plats',['Boeuf bourguignon','Bœuf bourguignon']),
 ('allemagne','desserts',['Käsekuchen']),
 ('allemagne','desserts',['Rote Grütze']),
 ('senegal','plats',['Mafé boeuf','Mafé de bœuf']),
 ('senegal','desserts',['Thiagry','Thiakry']),
 ('espagne','desserts',['Crema catalana','Crème catalane']),
 ('espagne','entrees',['Croquetas au jambon','Croquetas jambon']),
 ('espagne','entrees',['Gazpacho','Gazpacho andalou']),
 ('espagne','plats',['Tortilla espagnole','Tortilla de pommes de terre']),
 ('italie','plats',['Polenta crémeuse','Polenta crémeuse au parmesan']),
 ('usa','desserts',['Brownies','Brownies chocolat']),
 ('usa','desserts',['Cheesecake','New York cheesecake']),
 ('usa','entrees',['Coleslaw','Coleslaw crémeux']),
 ('usa','plats',['Pulled chicken','Pulled chicken barbecue']),
 ('usa','entrees',['Buffalo chicken dip','Buffalo dip']),
]

def score(path):
    try:
        d=json.loads((path/'recipe.json').read_text(encoding='utf-8'))
        return len(d.get('ingredients',[]))*10 + len(d.get('steps',[]))*20 + len(json.dumps(d,ensure_ascii=False))
    except Exception: return 0

removed=[]
for country,cat,titles in GROUPS:
    base=ROOT/country/cat
    if not base.exists(): continue
    wanted={norm(x) for x in titles}
    matches=[]
    for p in base.iterdir():
        if not p.is_dir() or not (p/'recipe.json').exists(): continue
        try: title=json.loads((p/'recipe.json').read_text(encoding='utf-8')).get('title','')
        except Exception: continue
        if norm(title) in wanted: matches.append(p)
    if len(matches)>1:
        keep=max(matches,key=score)
        for p in matches:
            if p!=keep:
                removed.append(str(p)); shutil.rmtree(p)

# Sécurité générale: deux fiches ayant exactement le même titre normalisé dans le même pays/catégorie.
for country in ROOT.iterdir():
    if not country.is_dir(): continue
    for cat in country.iterdir():
        if not cat.is_dir(): continue
        seen={}
        for p in sorted(cat.iterdir()):
            if not p.is_dir() or not (p/'recipe.json').exists(): continue
            try: title=json.loads((p/'recipe.json').read_text(encoding='utf-8')).get('title','')
            except Exception: continue
            k=norm(title)
            if k in seen:
                keep=max([seen[k],p],key=score); drop=p if keep==seen[k] else seen[k]
                if drop.exists(): removed.append(str(drop)); shutil.rmtree(drop)
                seen[k]=keep
            else: seen[k]=p
print(f'{len(removed)} doublons supprimés')
for x in removed: print(' -',x)
