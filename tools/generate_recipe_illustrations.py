#!/usr/bin/env python3
import json, hashlib, html, re, unicodedata
from pathlib import Path

ROOT = Path('app/src/main/assets/recipes')
VRAC = Path('VRAC')
MANUAL_EXTS = ['.jpg','.jpeg','.png','.webp']

PALETTES = {
 'france':('#f6ead7','#d94b3d','#f2bd4a','#355c7d'),
 'senegal':('#fff1bf','#d66b2c','#488c43','#f2c84b'),
 'italie':('#f4ead7','#c94a44','#4e8a57','#efc85b'),
 'espagne':('#fff0cf','#c94a35','#e3a62f','#6d3f34'),
 'usa':('#f1e7da','#b84a45','#3f6693','#f0c45a'),
 'allemagne':('#f1e9da','#7b5135','#d3a93b','#545d48')
}

def norm(s):
 s=unicodedata.normalize('NFKD',s).encode('ascii','ignore').decode().lower()
 return re.sub(r'[^a-z0-9]+',' ',s)

def kind(title, category):
 t=norm(title)
 if category=='desserts':
  if any(x in t for x in ['soupe','creme','pudding','riz au lait','semoule','compote','sorbet','gelato','granita']): return 'bowl'
  if any(x in t for x in ['tarte','pie','gateau','cake','cheesecake','tiramisu','brownie','brioche','panettone']): return 'cake'
  return 'dessert'
 if any(x in t for x in ['soupe','veloute','chowder','gumbo','goulasch','ragout','mafe','domoda','yassa','thiou','sauce','curry','lentilles']): return 'bowl'
 if any(x in t for x in ['riz','paella','risotto','ceebu','thieb','jambalaya','pilaf']): return 'rice'
 if any(x in t for x in ['pates','pasta','spatzle','gnocchi','lasagne','vermicelle','mac and cheese']): return 'pasta'
 if any(x in t for x in ['salade','coleslaw','taboule']): return 'salad'
 if any(x in t for x in ['poisson','morue','merlu','calamar','saumon']): return 'fish'
 if any(x in t for x in ['poulet','chicken','boeuf','bœuf','porc','veau','viande','meat','saucisse']): return 'meat'
 return 'plate'

def shapes(k, c1,c2,c3):
 if k=='bowl':
  return f'<ellipse cx="400" cy="265" rx="180" ry="72" fill="{c3}"/><path d="M220 260 Q400 430 580 260" fill="{c1}"/><ellipse cx="400" cy="260" rx="145" ry="48" fill="{c2}"/><circle cx="350" cy="250" r="18" fill="#f7e2a1"/><circle cx="430" cy="270" r="16" fill="#7b9d54"/><circle cx="475" cy="245" r="13" fill="#c95b44"/>'
 if k=='rice':
  return f'<ellipse cx="400" cy="315" rx="205" ry="78" fill="{c1}"/><ellipse cx="400" cy="285" rx="165" ry="92" fill="#fff3c6"/><circle cx="335" cy="255" r="20" fill="{c2}"/><circle cx="430" cy="245" r="18" fill="#72934d"/><circle cx="475" cy="285" r="16" fill="{c3}"/><circle cx="365" cy="310" r="15" fill="#d88243"/>'
 if k=='pasta':
  return f'<ellipse cx="400" cy="315" rx="205" ry="78" fill="{c1}"/><path d="M285 285 C330 220 370 350 415 265 S500 330 525 260" fill="none" stroke="#efcc69" stroke-width="28" stroke-linecap="round"/><circle cx="355" cy="270" r="18" fill="{c2}"/><circle cx="455" cy="300" r="16" fill="#6d9654"/>'
 if k=='salad':
  return f'<ellipse cx="400" cy="310" rx="195" ry="74" fill="{c1}"/><path d="M245 275 Q400 420 555 275" fill="#e8efe2"/><circle cx="335" cy="265" r="38" fill="#6d9e55"/><circle cx="400" cy="245" r="32" fill="#8db765"/><circle cx="465" cy="275" r="34" fill="#5f8f4d"/><circle cx="380" cy="295" r="20" fill="{c2}"/>'
 if k=='fish':
  return f'<ellipse cx="400" cy="315" rx="210" ry="80" fill="{c1}"/><path d="M275 275 Q390 190 500 275 Q390 360 275 275Z" fill="#e9c59d"/><path d="M500 275 L565 225 L565 325Z" fill="#d9ae7d"/><circle cx="315" cy="260" r="8" fill="#2b2b2b"/><circle cx="410" cy="320" r="18" fill="#6f9d54"/>'
 if k=='meat':
  return f'<ellipse cx="400" cy="315" rx="210" ry="80" fill="{c1}"/><path d="M285 280 Q335 205 420 245 Q500 220 525 300 Q455 350 360 330 Q300 340 285 280Z" fill="#a95f47"/><path d="M330 270 Q395 240 470 280" fill="none" stroke="#f1c58e" stroke-width="13"/><circle cx="500" cy="315" r="22" fill="#70964e"/>'
 if k=='cake':
  return f'<ellipse cx="400" cy="335" rx="190" ry="62" fill="{c1}"/><path d="M300 315 L330 205 L500 230 L520 320Z" fill="#e3b071"/><path d="M330 205 L500 230 L480 260 L320 240Z" fill="{c2}"/><circle cx="420" cy="215" r="16" fill="#f4eee1"/>'
 if k=='dessert':
  return f'<ellipse cx="400" cy="330" rx="165" ry="55" fill="{c1}"/><rect x="325" y="215" width="150" height="115" rx="38" fill="#f0d7ab"/><path d="M335 245 Q400 205 465 245" fill="none" stroke="{c2}" stroke-width="16"/>'
 return f'<ellipse cx="400" cy="315" rx="210" ry="80" fill="{c1}"/><circle cx="400" cy="275" r="110" fill="#f3dfad"/><circle cx="355" cy="260" r="30" fill="{c2}"/><circle cx="430" cy="300" r="28" fill="#70974d"/><circle cx="465" cy="245" r="22" fill="{c3}"/>'

def make_svg(data):
 title=data.get('title','Recette'); country=data.get('country','france'); cat=data.get('category','plats'); flag=data.get('flag','🍽️')
 bg,c1,c2,c3=PALETTES.get(country,('#f3eadc','#be6244','#dda943','#55755b'))
 seed=int(hashlib.sha1(title.encode('utf-8')).hexdigest()[:6],16)
 # subtle unique placement pattern
 dots=''.join(f'<circle cx="{80+(seed>>(i*3))%650}" cy="{85+(seed>>(i*4))%300}" r="{8+(i%3)*4}" fill="{c3}" opacity="0.10"/>' for i in range(7))
 body=shapes(kind(title,cat),c1,c2,c3)
 safe=html.escape(title)
 small=safe if len(safe)<=34 else safe[:31]+'…'
 return f'''<svg xmlns="http://www.w3.org/2000/svg" width="800" height="500" viewBox="0 0 800 500">
<rect width="800" height="500" rx="34" fill="{bg}"/>{dots}
<rect x="52" y="42" width="92" height="54" rx="27" fill="#ffffff" opacity="0.84"/><text x="98" y="78" text-anchor="middle" font-size="28">{flag}</text>
{body}
<rect x="70" y="395" width="660" height="70" rx="24" fill="#ffffff" opacity="0.90"/>
<text x="400" y="438" text-anchor="middle" font-family="sans-serif" font-size="30" font-weight="700" fill="#252525">{small}</text>
</svg>'''

manual=generated=0
for recipe_file in sorted(ROOT.rglob('recipe.json')):
 data=json.loads(recipe_file.read_text(encoding='utf-8'))
 rid=data.get('id',recipe_file.parent.name)
 found=any((VRAC/(rid+ext)).exists() for ext in MANUAL_EXTS)
 if found:
  manual+=1
  continue
 svg=recipe_file.parent/'image.svg'
 svg.write_text(make_svg(data),encoding='utf-8')
 data['image']='image.svg'
 recipe_file.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 generated+=1
print(f'{manual} illustrations VRAC conservées, {generated} illustrations générées automatiquement')
