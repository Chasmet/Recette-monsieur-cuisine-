#!/usr/bin/env python3
import json, os, re
from pathlib import Path

ROOT=Path('app/src/main/assets/recipes')
COUNTRIES={
'france':('France','🇫🇷'),'senegal':('Sénégal','🇸🇳'),'italie':('Italie','🇮🇹'),
'espagne':('Espagne','🇪🇸'),'usa':('États-Unis','🇺🇸'),'allemagne':('Allemagne','🇩🇪')}
CATEGORIES={'plats':'Plats','entrees':'Entrées','desserts':'Desserts'}
NAMES={
'france':{
'plats':['Boeuf bourguignon','Blanquette de veau','Hachis parmentier','Gratin dauphinois','Ratatouille','Quiche lorraine','Poulet basquaise','Petit salé aux lentilles','Pot-au-feu','Navarin d’agneau','Boeuf carottes','Coq au vin','Cassoulet','Endives au jambon','Brandade de morue','Parmentier de canard','Saucisses lentilles','Poulet à la moutarde','Filet mignon sauce champignons','Risotto aux champignons','Purée maison','Aligot','Velouté de poireaux','Soupe à l’oignon','Chili de lentilles'],
'entrees':['Oeufs mimosa','Poireaux vinaigrette','Velouté de potimarron','Soupe de courgettes','Terrine de légumes','Rillettes de thon','Gougères','Cake salé','Flan de courgettes','Caviar d’aubergine','Tapenade','Pâté de campagne','Salade de lentilles','Mousse de saumon','Velouté d’asperges'],
'desserts':['Crème brûlée','Mousse au chocolat','Île flottante','Riz au lait','Flan pâtissier','Clafoutis aux cerises','Fondant au chocolat','Crème caramel','Compote de pommes','Tarte au citron','Pâte à crêpes','Gâteau au yaourt','Madeleines','Financiers','Crème pâtissière','Chouquettes','Brioche','Pain perdu','Semoule au lait','Ganache chocolat']},
'senegal':{
'plats':['Thiéboudiène rouge','Thiéboudiène blanc','Yassa poulet','Yassa poisson','Mafé boeuf','Mafé poulet','Soupou kandja','Thiou viande','Thiou poisson','Domoda boeuf','Domoda poisson','Ceebu yapp','Ceebu guinar','Caldou poisson','Mbaxalou saloum','Thiéré viande','Thiéré poisson','Lakhou bissap salé','Ndambe','Vermicelles au poulet','Riz au poisson fumé','Riz viande légumes','Poulet sauce oignons','Poisson sauce tomate','Lentilles sénégalaises'],
'entrees':['Pastels au thon','Pastels au poisson','Fataya viande','Accras de poisson','Salade sénégalaise','Soupe de poisson','Purée de niébé','Sauce rof','Salade avocat crevettes','Beignets de niébé','Croquettes de poisson','Salade de mangue verte','Vermicelles froids','Omelette sénégalaise','Tartinade de poisson'],
'desserts':['Thiéré lakh','Lakh','Ngalakh','Sombi','Sombi coco','Fondé','Dégué','Beignets dougoub','Beignets banane','Thiagry','Crème de bouye','Crème de bissap','Compote mangue','Confiture bissap','Confiture mangue','Yaourt maison','Gâteau coco','Gâteau banane','Bouillie de mil','Bouillie de maïs']},
'italie':{
'plats':['Risotto alla milanese','Risotto aux cèpes','Risotto aux fruits de mer','Polenta crémeuse','Lasagnes bolognaise','Lasagnes végétariennes','Sauce bolognaise','Pasta e fagioli','Minestrone','Gnocchi sauce tomate','Osso buco','Aubergines parmigiana','Poulet cacciatore','Ragù napolitain','Pâtes carbonara','Pâtes arrabbiata','Pâtes puttanesca','Pâtes pesto','Pâtes amatriciana','Soupe toscane','Frittata légumes','Risotto courgettes','Risotto citron','Polpette sauce tomate','Sauce tomate italienne'],
'entrees':['Bruschetta tomate','Caponata sicilienne','Focaccia','Arancini','Supplì','Crème de parmesan','Pesto génois','Tapenade italienne','Soupe tomate basilic','Polenta grillée','Poivrons marinés','Aubergines marinées','Crème de ricotta','Flan parmesan','Velouté de haricots'],
'desserts':['Tiramisu','Panna cotta','Zabaglione','Crème mascarpone','Torta caprese','Amaretti','Biscotti','Cannoli crème ricotta','Semifreddo chocolat','Gelato vanille','Gelato chocolat','Sorbet citron','Granita citron','Crème café','Gâteau ricotta citron','Budino chocolat','Panettone','Pandoro','Crème de noisette','Tiramisu fraise']},
'espagne':{
'plats':['Paella valencienne','Paella fruits de mer','Paella poulet','Riz noir','Fideuà','Fabada asturiana','Lentilles au chorizo','Pois chiches aux épinards','Poulet à l’ail','Boulettes sauce espagnole','Marmitako','Pisto manchego','Riz au poulet','Soupe de poisson espagnole','Pommes de terre riojana','Callos sauce tomate','Poulet sauce poivrons','Riz aux légumes','Merlu sauce verte','Morue tomate','Calamars sauce tomate','Riz aux champignons','Cocido simplifié','Crème de pois chiches','Tortilla espagnole'],
'entrees':['Gazpacho','Salmorejo','Ajo blanco','Croquetas jambon','Croquetas poulet','Tortilla pommes de terre','Patatas bravas sauce','Alioli','Romesco','Pimientos sauce','Champignons à l’ail','Soupe à l’ail','Crème de courgette','Crème de poivron','Empanadillas thon'],
'desserts':['Crème catalane','Arroz con leche','Flan espagnol','Churros','Natillas','Tarta de Santiago','Leche frita','Torrijas','Polvorones','Mantecados','Crème au citron','Flan coco','Gâteau orange','Bizcocho yaourt','Crème amande','Compote coing','Sorbet orange','Crème chocolat espagnole','Riz au lait cannelle','Gâteau amande']},
'usa':{
'plats':['Mac and cheese','Chili con carne','Pulled chicken','Meatloaf','Sloppy joe','Clam chowder','Corn chowder','Chicken pot pie filling','Jambalaya','Gumbo poulet','Baked beans','Buffalo chicken dip','Mashed potatoes','Gravy','Cheeseburger soup','Broccoli cheddar soup','Chicken noodle soup','Tomato soup','Beef stew','Turkey chili','White chicken chili','BBQ pulled pork','Creamed corn','Sweet potato mash','Cajun rice'],
'entrees':['Coleslaw','Guacamole','Salsa tomate','Cheese dip','Spinach artichoke dip','Deviled eggs','Corn dip','Bean dip','Potato salad','Macaroni salad','Buffalo dip','Crab dip','Pumpkin soup','Corn soup','Ranch dip'],
'desserts':['Cheesecake','Brownies','Cookies chocolat','Carrot cake','Pumpkin pie filling','Apple pie filling','Banana bread','Muffins myrtilles','Pancakes','Cinnamon rolls','Fudge chocolat','Rice pudding','Chocolate pudding','Vanilla pudding','Peanut butter cookies','Lemon curd','Cupcakes vanille','Red velvet cake','Donuts pâte','Milkshake vanille']},
'allemagne':{
'plats':['Goulasch allemand','Rouladen sauce','Sauerkraut saucisses','Kartoffelsuppe','Eintopf lentilles','Königsberger Klopse','Sauerbraten sauce','Spätzle','Käsespätzle','Soupe de pois cassés','Soupe de pommes de terre','Chou rouge braisé','Ragoût de boeuf','Ragoût de porc','Currywurst sauce','Frikadellen sauce','Poulet paprika','Lentilles saucisses','Purée pommes de terre','Ragoût chou','Soupe poireaux viande','Champignons crème','Goulasch saucisse','Riz paprika','Gratin chou-fleur'],
'entrees':['Salade pommes de terre','Salade concombre','Soupe bière fromage','Crème de champignons','Soupe chou-fleur','Soupe potiron','Obatzda','Tartinade fromage','Salade betterave','Crème de poireaux','Soupe lentilles','Salade chou','Sauce currywurst','Crème radis','Soupe carottes'],
'desserts':['Apfelmus','Rote Grütze','Vanillesauce','Milchreis','Grießpudding','Gâteau Forêt-Noire','Gâteau aux pommes','Streuselkuchen','Käsekuchen','Lebkuchen','Pudding chocolat','Pudding vanille','Compote prunes','Crème cannelle','Gâteau marbré','Pâte à strudel','Bienenstich crème','Gâteau noisette','Crème cerise','Semoule cerise']}}

def slug(s):
 s=s.lower().replace('’','-').replace("'",'-'); s=re.sub(r'[^a-z0-9àâäéèêëîïôöùûüç]+','-',s); return s.strip('-')
def template(country,cat,title):
 label,flag=COUNTRIES[country]; isdess=cat=='desserts'; ing=[{'amount':'250 g','name':'ingrédient principal'},{'amount':'1','name':'oignon ou base aromatique'},{'amount':'20 g','name':'huile ou matière grasse'},{'amount':'300 ml','name':'liquide adapté'},{'amount':'à ajuster','name':'sel et épices'}]
 if isdess: ing=[{'amount':'250 g','name':'ingrédient principal'},{'amount':'80 g','name':'sucre'},{'amount':'2','name':'oeufs'},{'amount':'250 ml','name':'lait ou crème'},{'amount':'1 pincée','name':'arôme adapté'}]
 return {'id':f'{country}-{cat}-{slug(title)}','title':title,'country':country,'countryLabel':label,'flag':flag,'category':cat,'categoryLabel':CATEGORIES[cat],'servings':4,'prepMinutes':15,'cookMinutes':30,'difficulty':'Facile','description':f'{title} adapté à la cuisine guidée Monsieur Cuisine.','image':'image.svg','ingredients':ing,'steps':[{'title':'Préparer','instruction':'Peser et préparer tous les ingrédients indiqués avant de commencer.','durationSeconds':0,'temperatureC':0,'speed':'','reverse':False,'turbo':False,'note':''},{'title':'Hacher la base','instruction':'Mettre les éléments aromatiques dans le bol et hacher.','durationSeconds':8,'temperatureC':0,'speed':'5','reverse':False,'turbo':False,'note':''},{'title':'Cuire','instruction':'Ajouter les ingrédients indiqués et cuire en surveillant la texture.','durationSeconds':1200,'temperatureC':100,'speed':'1','reverse':not isdess,'turbo':False,'note':'Adapter légèrement la durée selon la taille des morceaux.'},{'title':'Finaliser','instruction':'Rectifier l’assaisonnement ou la texture puis servir.','durationSeconds':20,'temperatureC':0,'speed':'2','reverse':False,'turbo':False,'note':''}]}
def svg(title,flag):
 return f'''<svg xmlns="http://www.w3.org/2000/svg" width="800" height="500"><rect width="800" height="500" rx="36" fill="#151a20"/><circle cx="400" cy="215" r="145" fill="#e9b949"/><circle cx="400" cy="215" r="105" fill="#f7f2e8"/><text x="400" y="220" text-anchor="middle" font-size="72">{flag}</text><text x="400" y="405" text-anchor="middle" font-family="sans-serif" font-size="34" fill="white">{title[:34]}</text></svg>'''
count=0
for country,cats in NAMES.items():
 for cat,names in cats.items():
  for title in names:
   d=ROOT/country/cat/slug(title); r=d/'recipe.json'
   if r.exists(): continue
   d.mkdir(parents=True,exist_ok=True); data=template(country,cat,title)
   r.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
   (d/'image.svg').write_text(svg(title,COUNTRIES[country][1]),encoding='utf-8'); count+=1
print(f'{count} nouvelles recettes générées')
