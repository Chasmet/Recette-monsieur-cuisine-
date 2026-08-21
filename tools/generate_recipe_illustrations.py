#!/usr/bin/env python3
import json, re, unicodedata, hashlib
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

ROOT=Path('app/src/main/assets/recipes')
VRAC=Path('VRAC')
MANUAL_EXTS=['.jpg','.jpeg','.png','.webp']
PALETTES={
 'france':('#F7E9D7','#B9472F','#F1C766','#375B78'),
 'senegal':('#FFF0C2','#D56A2E','#5E8D4A','#E7C34E'),
 'italie':('#F7E8D9','#C74D42','#4F8A57','#E6C35A'),
 'espagne':('#FFF0CE','#C74635','#E4A62F','#714437'),
 'usa':('#F1E8DD','#B84D45','#466995','#EFC45A'),
 'allemagne':('#F0E8DC','#7A5238','#D0A83B','#59604D')}

def norm(s):
 s=unicodedata.normalize('NFKD',s).encode('ascii','ignore').decode().lower()
 return re.sub(r'[^a-z0-9]+',' ',s)

def kind(title,cat):
 t=norm(title)
 if cat=='desserts':
  if any(x in t for x in ['gateau','cake','tarte','pie','brown','cheesecake','tiramisu','brioche','panettone']): return 'cake'
  return 'dessert'
 if any(x in t for x in ['riz','paella','risotto','ceebu','thieb','jambalaya']): return 'rice'
 if any(x in t for x in ['pate','pasta','spatzle','gnocchi','lasagne','vermicelle','mac and cheese']): return 'pasta'
 if any(x in t for x in ['salade','coleslaw']): return 'salad'
 if any(x in t for x in ['poisson','morue','merlu','calamar','saumon']): return 'fish'
 if any(x in t for x in ['poulet','chicken','boeuf','porc','veau','viande','meat','saucisse']): return 'meat'
 return 'bowl'

def font(size,bold=False):
 paths=['/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf' if bold else '/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf']
 for p in paths:
  if Path(p).exists(): return ImageFont.truetype(p,size)
 return ImageFont.load_default()

def draw_food(d,k,c1,c2,c3):
 # plate shadow + plate
 d.ellipse((170,170,630,405),fill='#D7C8B7')
 d.ellipse((190,155,610,385),fill='#FFF8EA')
 if k=='rice':
  d.ellipse((275,205,525,340),fill='#F5E3A2'); d.ellipse((315,225,365,270),fill=c2); d.ellipse((420,245,465,288),fill='#6F9B55'); d.ellipse((380,290,425,330),fill=c3)
 elif k=='pasta':
  for i in range(8):
   x=255+i*35; d.arc((x,210,x+120,340),10,260,fill='#E9C35F',width=18)
  d.ellipse((330,245,380,295),fill=c2); d.ellipse((455,275,500,320),fill='#6D9654')
 elif k=='salad':
  for box,col in [((270,220,355,305),'#6E9D55'),((350,195,445,290),'#8BB56A'),((430,225,520,315),'#5E8E4C'),((365,270,415,320),c2)]: d.ellipse(box,fill=col)
 elif k=='fish':
  d.polygon([(260,275),(380,205),(520,275),(380,345)],fill='#E8C39A'); d.polygon([(520,275),(585,225),(585,325)],fill='#D4A777'); d.ellipse((290,255,306,271),fill='#222')
 elif k=='meat':
  d.rounded_rectangle((260,215,535,335),radius=45,fill='#A85E46'); d.line((305,265,485,285),fill='#F0C28D',width=14); d.ellipse((500,295,545,340),fill='#6F9650')
 elif k=='cake':
  d.polygon([(300,325),(330,205),(500,225),(525,330)],fill='#E1AE70'); d.polygon([(330,205),(500,225),(480,265),(320,245)],fill=c2); d.ellipse((410,205,445,240),fill='#FFF3E7')
 elif k=='dessert':
  d.rounded_rectangle((315,205,485,335),radius=35,fill='#EED7AA'); d.arc((325,225,475,285),180,360,fill=c2,width=16)
 else:
  d.ellipse((250,210,550,345),fill=c2); d.ellipse((310,245,350,285),fill='#F4D38C'); d.ellipse((405,260,445,300),fill='#6F9851'); d.ellipse((455,225,490,260),fill=c3)

def make_png(data,path):
 title=data.get('title','Recette'); country=data.get('country','france'); cat=data.get('category','plats')
 bg,c1,c2,c3=PALETTES.get(country,('#F5EBDD','#BE6244','#DDA943','#55755B'))
 im=Image.new('RGB',(800,500),bg); d=ImageDraw.Draw(im)
 seed=int(hashlib.sha1(title.encode()).hexdigest()[:8],16)
 for i in range(8):
  x=50+((seed>>(i*3))%700); y=45+((seed>>(i*4))%300); r=8+(i%3)*4
  d.ellipse((x-r,y-r,x+r,y+r),fill=c3)
 draw_food(d,kind(title,cat),c1,c2,c3)
 d.rounded_rectangle((55,395,745,470),radius=22,fill='#FFFDF9')
 text=title if len(title)<=34 else title[:31]+'…'
 f=font(30,True); box=d.textbbox((0,0),text,font=f); d.text(((800-(box[2]-box[0]))/2,416),text,font=f,fill='#252525')
 d.rounded_rectangle((50,35,210,88),radius=24,fill='#2B1A10'); d.text((72,50),data.get('countryLabel',''),font=font(18,True),fill='#FFFFFF')
 im.save(path,'PNG',optimize=True)

manual=generated=0
for recipe_file in sorted(ROOT.rglob('recipe.json')):
 data=json.loads(recipe_file.read_text(encoding='utf-8'))
 rid=data.get('id',recipe_file.parent.name)
 manual_file=None
 for ext in MANUAL_EXTS:
  p=VRAC/(rid+ext)
  if p.exists(): manual_file=p; break
 if manual_file:
  manual+=1; continue
 out=recipe_file.parent/'image.png'
 make_png(data,out)
 data['image']='image.png'
 recipe_file.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 old=recipe_file.parent/'image.svg'
 if old.exists(): old.unlink()
 generated+=1
print(f'{manual} illustrations VRAC conservées, {generated} PNG générés')
