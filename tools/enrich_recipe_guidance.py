#!/usr/bin/env python3
import json, re, unicodedata
from pathlib import Path

ROOT=Path('app/src/main/assets/recipes')
BAD={'ingrédient principal','ingredient principal','oignon ou base aromatique','huile ou matière grasse','liquide adapté','sel et épices','lait ou crème','arôme adapté'}

def norm(s):
    s=unicodedata.normalize('NFKD',s).encode('ascii','ignore').decode().lower()
    return re.sub(r'[^a-z0-9 ]+',' ',s)

def I(amount,name): return {'amount':amount,'name':name}
def S(title,instruction,sec=0,temp=0,speed='',reverse=False,turbo=False,note=''):
    return {'title':title,'instruction':instruction,'durationSeconds':sec,'temperatureC':temp,'speed':speed,'reverse':reverse,'turbo':turbo,'note':note}

def pantry(country):
    return {
      'france':[I('1','oignon jaune'),I('2 gousses','ail'),I('20 g','beurre ou huile d’olive')],
      'senegal':[I('2','oignons jaunes'),I('2 gousses','ail'),I('20 g','huile d’arachide')],
      'italie':[I('1','oignon'),I('2 gousses','ail'),I('20 g','huile d’olive')],
      'espagne':[I('1','oignon'),I('2 gousses','ail'),I('20 g','huile d’olive')],
      'usa':[I('1','oignon'),I('2 gousses','ail'),I('20 g','huile neutre ou beurre')],
      'allemagne':[I('1','oignon'),I('20 g','beurre ou huile'),I('1 c. à café','paprika doux')]
    }.get(country,[I('1','oignon'),I('2 gousses','ail'),I('20 g','huile')])

def special(title):
    t=norm(title)
    if t=='aligot':
        return [I('800 g','pommes de terre à purée, épluchées et coupées en cubes'),I('300 g','tomme fraîche d’Auvergne coupée en fines lamelles'),I('100 ml','crème entière'),I('40 g','beurre'),I('1 gousse','ail'),I('1 c. à café rase','sel'),I('2 pincées','poivre')],[
          S('Cuire les pommes de terre','Mettre 500 ml d’eau dans le bol. Placer les 800 g de pommes de terre dans le panier cuisson.',1500,120,'1',False,False,'Les pommes de terre doivent être très tendres.'),
          S('Vider l’eau','Retirer le panier avec précaution, vider complètement l’eau puis remettre les pommes de terre cuites dans le bol.'),
          S('Écraser','Ajouter 40 g de beurre, 100 ml de crème, l’ail, le sel et le poivre. Mixer 25 secondes.',25,0,'4'),
          S('Faire fondre la tomme','Programmer 4 minutes à 80 °C, vitesse 2. Ajouter progressivement les 300 g de tomme par l’ouverture du couvercle.',240,80,'2'),
          S('Filer l’aligot','Mélanger encore 45 secondes, vitesse 3, jusqu’à obtenir une texture lisse, élastique et filante.',45,0,'3',False,False,'Servir immédiatement.')]
    if 'boeuf bourguignon' in t:
        return [I('800 g','bœuf à bourguignon en cubes de 4 cm'),I('150 g','lardons fumés'),I('2','carottes en rondelles'),I('1','oignon coupé en deux'),I('2 gousses','ail'),I('500 ml','vin rouge de Bourgogne'),I('200 ml','bouillon de bœuf'),I('20 g','farine'),I('20 g','huile'),I('1','bouquet garni'),I('1 c. à café','sel'),I('1/2 c. à café','poivre')],[S('Hacher oignon et ail','Mettre l’oignon et l’ail dans le bol.',5,0,'5'),S('Faire revenir','Ajouter l’huile et les lardons.',300,120,'1',True),S('Ajouter le bœuf','Ajouter les cubes de bœuf et les carottes.',480,120,'1',True),S('Singer la viande','Ajouter la farine et mélanger.',30,0,'2',True),S('Mijoter','Verser le vin rouge et le bouillon, ajouter le bouquet garni, le sel et le poivre.',5400,95,'1',True,False,'Bouchon doseur retiré, panier cuisson posé sur le couvercle pour laisser évaporer.'),S('Vérifier','La viande doit être fondante. Prolonger 15 minutes à 95 °C si nécessaire.',0,0,'')]
    if 'yassa poulet' in t:
        return [I('800 g','haut de cuisses de poulet désossés, en morceaux'),I('500 g','oignons émincés'),I('80 ml','jus de citron'),I('30 g','moutarde de Dijon'),I('2 gousses','ail'),I('20 g','huile d’arachide'),I('1','cube de bouillon émietté'),I('100 ml','eau'),I('1/2 c. à café','poivre'),I('1 pincée','sel')],[S('Mariner','Mélanger le poulet avec le jus de citron, la moutarde, la moitié des oignons, l’ail et le poivre. Laisser 30 minutes au frais.'),S('Hacher l’ail','Mettre l’ail dans le bol.',5,0,'6'),S('Cuire les oignons','Ajouter l’huile et tous les oignons.',720,120,'1',True),S('Ajouter le poulet','Ajouter le poulet avec la marinade, le cube de bouillon et 100 ml d’eau.',1200,100,'1',True),S('Réduire la sauce','Retirer le bouchon doseur et cuire encore 8 minutes.',480,110,'1',True,False,'La sauce doit être épaisse et les oignons fondants.')]
    if 'mafe' in t:
        protein='poulet' if 'poulet' in t else 'bœuf'
        return [I('700 g',protein+' en morceaux'),I('180 g','pâte d’arachide non sucrée'),I('400 g','tomates concassées'),I('1','oignon'),I('2 gousses','ail'),I('2','carottes en gros morceaux'),I('1','patate douce en cubes'),I('500 ml','eau'),I('20 g','huile d’arachide'),I('1','cube de bouillon'),I('1/2 c. à café','poivre')],[S('Hacher','Mettre l’oignon et l’ail dans le bol.',6,0,'5'),S('Faire revenir','Ajouter l’huile.',300,120,'1'),S('Ajouter la viande','Ajouter '+protein+' et faire revenir.',420,120,'1',True),S('Préparer la sauce','Ajouter tomates, pâte d’arachide, eau et cube de bouillon.',30,0,'3'),S('Mijoter','Ajouter carottes et patate douce.',2100,100,'1',True,False,'La sauce doit napper la cuillère et les légumes être tendres.')]
    if 'lasagnes bolognaise' in t:
        return [I('500 g','bœuf haché'),I('1','oignon'),I('2 gousses','ail'),I('700 g','coulis de tomate'),I('30 g','huile d’olive'),I('12','plaques de lasagnes'),I('500 ml','lait'),I('40 g','beurre'),I('40 g','farine'),I('100 g','parmesan râpé'),I('150 g','mozzarella râpée'),I('1 c. à café','sel'),I('1/2 c. à café','poivre')],[S('Hacher oignon et ail','Mettre oignon et ail dans le bol.',5,0,'5'),S('Faire revenir','Ajouter l’huile.',240,120,'1'),S('Cuire le bœuf','Ajouter le bœuf haché.',420,120,'1',True),S('Cuire la sauce','Ajouter le coulis de tomate, la moitié du sel et le poivre.',1200,100,'1',True),S('Réserver la bolognaise','Verser la sauce dans un saladier et rincer rapidement le bol.'),S('Faire la béchamel','Mettre lait, beurre, farine et reste de sel.',720,90,'4'),S('Monter les lasagnes','Dans un plat : sauce, plaques, béchamel. Répéter 3 à 4 fois. Terminer par parmesan et mozzarella.'),S('Cuire au four','Enfourner 35 minutes dans un four préchauffé à 180 °C.',2100,0,'',False,False,'Le dessus doit être gratiné et les pâtes tendres.')]
    if 'tiramisu' in t and 'fraise' not in t:
        return [I('250 g','mascarpone'),I('3','œufs, blancs et jaunes séparés'),I('80 g','sucre'),I('200 ml','café fort refroidi'),I('200 g','biscuits à la cuillère'),I('20 g','cacao non sucré')],[S('Monter les blancs','Bol propre et sec : insérer le fouet, ajouter les 3 blancs.',210,0,'4'),S('Réserver les blancs','Transvaser délicatement dans un saladier et retirer le fouet.'),S('Fouetter jaunes et sucre','Mettre les jaunes et le sucre.',120,0,'4'),S('Ajouter le mascarpone','Ajouter le mascarpone.',40,0,'3'),S('Incorporer les blancs','Ajouter les blancs montés et mélanger très doucement à la spatule, hors robot.'),S('Monter le dessert','Tremper rapidement les biscuits dans le café. Alterner biscuits et crème dans un plat.'),S('Repos','Saupoudrer de cacao et placer au réfrigérateur au moins 6 heures.',21600,0,'')]
    if 'paella valencienne' in t:
        return [I('320 g','riz rond espagnol'),I('500 g','poulet en morceaux'),I('150 g','haricots verts plats'),I('1','poivron rouge en lanières'),I('2','tomates mûres concassées'),I('1','oignon'),I('2 gousses','ail'),I('700 ml','bouillon de volaille chaud'),I('30 g','huile d’olive'),I('1 dose','safran'),I('1 c. à café','paprika'),I('1 c. à café','sel')],[S('Hacher','Mettre oignon et ail dans le bol.',5,0,'5'),S('Faire revenir','Ajouter huile, poulet et poivron.',600,120,'1',True),S('Ajouter tomate et épices','Ajouter tomates, paprika et safran.',300,110,'1',True),S('Cuire le riz','Ajouter riz, haricots et bouillon.',1080,100,'1',True,False,'Ne pas utiliser le bouchon doseur ; poser le panier sur le couvercle.'),S('Repos','Laisser reposer 5 minutes avant de servir.',300,0,'')]
    if 'mac and cheese' in t:
        return [I('350 g','macaronis'),I('700 ml','eau'),I('250 ml','lait entier'),I('250 g','cheddar râpé'),I('30 g','beurre'),I('25 g','farine'),I('1 c. à café','moutarde'),I('1/2 c. à café','sel'),I('2 pincées','poivre')],[S('Cuire les pâtes','Mettre eau et sel dans le bol, chauffer 8 minutes.',480,100,'1'),S('Ajouter les macaronis','Ajouter les pâtes.',600,100,'1',True,False,'Respecter le temps indiqué sur le paquet si différent.'),S('Égoutter','Égoutter les pâtes et réserver.'),S('Faire la sauce','Mettre beurre, farine, lait, moutarde et poivre.',480,90,'4'),S('Ajouter le cheddar','Ajouter le cheddar râpé.',120,80,'3'),S('Mélanger','Remettre les macaronis et mélanger.',45,0,'1',True)]
    if 'currywurst' in t:
        return [I('4','saucisses type bratwurst'),I('400 g','tomates concassées'),I('60 g','ketchup'),I('1','oignon'),I('15 g','huile'),I('2 c. à café','curry en poudre'),I('1 c. à café','paprika doux'),I('1 c. à café','sucre'),I('1 c. à soupe','vinaigre de cidre'),I('1/2 c. à café','sel')],[S('Hacher l’oignon','Mettre l’oignon dans le bol.',5,0,'5'),S('Faire revenir','Ajouter l’huile.',240,120,'1'),S('Cuire la sauce','Ajouter tomates, ketchup, curry, paprika, sucre, vinaigre et sel.',900,100,'2'),S('Mixer la sauce','Mixer pour obtenir une sauce lisse.',25,0,'7'),S('Cuire les saucisses','Faire dorer les saucisses à la poêle ou au gril 8 à 10 minutes, puis les couper en rondelles.'),S('Servir','Napper les saucisses de sauce chaude et saupoudrer d’un peu de curry.')]
    return None

def infer_focus(title):
    t=norm(title)
    pairs=[('poulet','poulet'),('chicken','poulet'),('boeuf','bœuf'),('veau','veau'),('porc','porc'),('poisson','poisson blanc'),('morue','morue dessalée'),('merlu','filets de merlu'),('calamar','calamars'),('saumon','saumon'),('crevette','crevettes'),('lentille','lentilles'),('haricot','haricots'),('pois chiche','pois chiches'),('pomme de terre','pommes de terre'),('patate','patates douces'),('courgette','courgettes'),('aubergine','aubergines'),('poireau','poireaux'),('potimarron','potimarron'),('potiron','potiron'),('carotte','carottes'),('chou fleur','chou-fleur'),('chou','chou'),('champignon','champignons'),('riz','riz'),('risotto','riz Arborio'),('pasta','pâtes'),('pates','pâtes'),('gnocchi','gnocchi'),('spatzle','spätzle'),('vermicelle','vermicelles'),('tomate','tomates'),('maïs','maïs'),('mais','maïs'),('mangue','mangue'),('banane','bananes'),('citron','citron'),('orange','orange'),('chocolat','chocolat noir'),('noisette','noisettes'),('amande','amandes'),('ricotta','ricotta')]
    for k,v in pairs:
        if k in t:return v
    return ''

def enrich(data):
    title=data.get('title','Recette'); t=norm(title); country=data.get('country','france'); cat=data.get('category','plats')
    sp=special(title)
    if sp:
        data['ingredients'],data['steps']=sp
        data['prepMinutes']=max(15,data.get('prepMinutes',15)); data['cookMinutes']=max(20,round(sum(x['durationSeconds'] for x in data['steps'])/60))
        data['description']=f"{title} : recette guidée détaillée pour Monsieur Cuisine, avec quantités, ordre d’ajout et réglages précis."
        return data
    focus=infer_focus(title)
    if cat=='desserts':
        # Crèmes, compotes et desserts cuits au bol
        if any(x in t for x in ['creme','pudding','riz au lait','milchreis','semoule','sombi','bouillie','compote','confiture','curd','natillas','zabaglione','rote grutze']):
            main=focus or ('riz rond' if 'riz' in t or 'milchreis' in t else 'lait entier')
            ing=[I('500 ml','lait entier'),I('80 g','sucre'),I('2','œufs')]
            if main!='lait entier': ing.insert(0,I('250 g',main))
            if 'chocolat' in t: ing.append(I('120 g','chocolat noir pâtissier'))
            elif 'citron' in t: ing.append(I('1','citron non traité, zeste et jus'))
            elif 'cannelle' in t or 'riz' in t: ing.append(I('1/2 c. à café','cannelle moulue'))
            else: ing.append(I('1 c. à café','extrait de vanille'))
            steps=[S('Mettre les ingrédients','Verser tous les ingrédients dans le bol dans l’ordre indiqué.'),S('Cuire doucement','Cuire en mélangeant pour éviter que le fond accroche.',720,90,'3'),S('Vérifier la texture','La préparation doit napper la spatule. Prolonger 2 à 3 minutes à 90 °C si elle est trop liquide.'),S('Refroidir','Verser dans des ramequins et laisser refroidir avant de placer au réfrigérateur.')]
            data['ingredients']=ing; data['steps']=steps; data['cookMinutes']=12
        elif any(x in t for x in ['sorbet','gelato','granita','milkshake']):
            fruit=focus or ('fruits surgelés' if 'sorbet' in t else 'glace vanille')
            ing=[I('500 g',fruit),I('60 g','sucre'),I('30 ml','jus de citron')]
            if 'gelato' in t or 'milkshake' in t: ing += [I('250 ml','lait entier'),I('100 ml','crème entière')]
            data['ingredients']=ing; data['steps']=[S('Préparer','Utiliser des fruits bien froids ou surgelés selon la recette.'),S('Mixer','Mettre tous les ingrédients dans le bol.',60,0,'8'),S('Racler','Racler les parois puis mixer encore.',30,0,'10',False,True),S('Servir','Servir immédiatement ou placer 1 heure au congélateur pour raffermir.')]; data['cookMinutes']=0
        else:
            flavor=focus
            ing=[I('250 g','farine de blé'),I('120 g','sucre'),I('2','œufs'),I('100 g','beurre doux fondu'),I('120 ml','lait entier'),I('8 g','levure chimique')]
            if flavor: ing.append(I('150 g',flavor))
            if 'tarte' in t or 'pie' in t: ing=[I('250 g','farine de blé'),I('125 g','beurre froid en cubes'),I('80 g','sucre'),I('1','œuf')]+([I('400 g',flavor)] if flavor else [I('400 g','garniture indiquée par le nom de la recette')])
            data['ingredients']=ing
            data['steps']=[S('Préchauffer le four','Préchauffer le four à 180 °C, chaleur traditionnelle.'),S('Mélanger','Mettre les ingrédients liquides puis les ingrédients secs dans le bol.',30,0,'4'),S('Racler et homogénéiser','Racler les parois avec la spatule puis mélanger encore.',15,0,'4'),S('Mettre en moule','Verser dans un moule beurré ou chemisé de papier cuisson.'),S('Cuire au four','Cuire 25 à 35 minutes à 180 °C selon l’épaisseur.',1800,0,'',False,False,'Planter la lame d’un couteau : elle doit ressortir sèche.')]
            data['cookMinutes']=30
    elif cat=='entrees' and any(x in t for x in ['salade','coleslaw','tapenade','pesto','guacamole','salsa','tartinade','obatzda','rillettes']):
        main=focus or ('légumes frais' if 'salade' in t else 'ingrédients frais de la recette')
        ing=[I('400 g',main)]+pantry(country)[:2]+[I('20 ml','jus de citron ou vinaigre'),I('30 ml','huile'),I('1/2 c. à café','sel'),I('2 pincées','poivre')]
        data['ingredients']=ing; data['steps']=[S('Préparer','Laver, éplucher et couper les ingrédients frais comme indiqué.'),S('Hacher','Mettre les éléments à hacher dans le bol.',5,0,'5'),S('Assaisonner','Ajouter jus de citron ou vinaigre, huile, sel et poivre.',15,0,'3'),S('Assembler','Ajouter le reste des ingrédients et mélanger délicatement.',20,0,'1',True),S('Repos','Laisser reposer 10 minutes au frais avant de servir.')]; data['cookMinutes']=0
    else:
        # familles salées
        main=focus
        if any(x in t for x in ['risotto']):
            extra=focus if focus!='riz Arborio' else ('champignons' if 'champignon' in t else 'courgettes' if 'courgette' in t else 'citron' if 'citron' in t else 'parmesan')
            ing=[I('320 g','riz Arborio'),I('700 ml','bouillon chaud'),I('80 g','parmesan râpé'),I('100 ml','vin blanc sec')]+pantry(country)+([I('250 g',extra)] if extra else [])
            steps=[S('Hacher','Mettre l’oignon et l’ail dans le bol.',5,0,'5'),S('Faire revenir','Ajouter l’huile.',240,120,'1'),S('Nacrer le riz','Ajouter le riz et le vin blanc.',180,120,'1',True),S('Cuire le risotto','Ajouter le bouillon et la garniture.',1080,100,'1',True,False,'Ne pas mettre le bouchon doseur.'),S('Finir','Ajouter le parmesan et mélanger.',30,0,'2',True)]
            data['ingredients']=ing;data['steps']=steps;data['cookMinutes']=25
        elif any(x in t for x in ['soupe','veloute','chowder','minestrone']):
            veg=main or ('pommes de terre et légumes' if 'soupe' in t else 'légumes de saison')
            ing=[I('600 g',veg),I('700 ml','bouillon de légumes')]+pantry(country)+[I('1/2 c. à café','sel'),I('2 pincées','poivre')]
            if 'veloute' in t or 'creme' in t: ing.append(I('100 ml','crème entière'))
            data['ingredients']=ing;data['steps']=[S('Hacher les aromates','Mettre oignon et ail dans le bol.',5,0,'5'),S('Faire revenir','Ajouter la matière grasse.',240,120,'1'),S('Ajouter les légumes','Ajouter les légumes coupés en morceaux et le bouillon.',1500,100,'1'),S('Mixer','Attendre 1 minute puis mixer progressivement.',60,0,'8'),S('Finir','Ajouter la crème si prévue, ajuster sel et poivre.',30,0,'3')];data['cookMinutes']=30
        elif any(x in t for x in ['pates','pasta','gnocchi','spatzle','vermicelle','fideua']):
            pasta='pâtes sèches' if 'gnocchi' not in t and 'spatzle' not in t and 'vermicelle' not in t else (focus or 'pâtes')
            ing=[I('350 g',pasta),I('700 ml','eau'),I('1 c. à café','sel')]+pantry(country)+[I('400 g','tomates concassées'),I('60 g','fromage râpé')]
            data['ingredients']=ing;data['steps']=[S('Préparer la sauce','Hacher oignon et ail.',5,0,'5'),S('Faire revenir','Ajouter l’huile.',240,120,'1'),S('Cuire la sauce','Ajouter tomates et assaisonnement.',720,100,'2'),S('Réserver la sauce','Verser la sauce dans un saladier.'),S('Cuire les pâtes','Mettre 700 ml d’eau et le sel. Chauffer puis ajouter les pâtes.',600,100,'1',True,False,'Adapter le temps au paquet.'),S('Assembler','Égoutter si nécessaire, remettre la sauce et mélanger.',30,0,'1',True)];data['cookMinutes']=30
        elif any(x in t for x in ['riz','paella','ceebu','thieb','jambalaya','cajun']):
            ing=[I('320 g','riz long ou riz rond selon la recette'),I('650 ml','bouillon chaud')]+pantry(country)+([I('500 g',main)] if main and main!='riz' else [I('400 g','légumes coupés en morceaux')])+[I('1 c. à café','sel'),I('1/2 c. à café','poivre')]
            data['ingredients']=ing;data['steps']=[S('Hacher les aromates','Mettre oignon et ail dans le bol.',5,0,'5'),S('Faire revenir','Ajouter l’huile et la garniture principale.',420,120,'1',True),S('Ajouter le riz','Ajouter le riz et mélanger.',60,120,'1',True),S('Cuire','Ajouter le bouillon et les légumes.',1080,100,'1',True,False,'Ne pas mettre le bouchon doseur ; poser le panier cuisson sur le couvercle.'),S('Repos','Laisser reposer 5 minutes avant de servir.',300,0,'')];data['cookMinutes']=30
        elif any(x in t for x in ['purée','puree','aligot']):
            ing=[I('800 g',main or 'pommes de terre'),I('350 ml','lait entier'),I('40 g','beurre'),I('1 c. à café','sel'),I('2 pincées','muscade')]
            data['ingredients']=ing;data['steps']=[S('Cuire','Mettre les morceaux et le lait dans le bol.',1500,95,'1',True),S('Ajouter beurre et assaisonnement','Ajouter beurre, sel et muscade.'),S('Mixer','Mixer jusqu’à la texture souhaitée.',30,0,'4')];data['cookMinutes']=27
        else:
            protein=main or ('légumes variés' if cat=='entrees' else 'viande ou légumes indiqués dans le nom de la recette')
            amount='700 g' if protein in ['poulet','bœuf','veau','porc','poisson blanc','morue dessalée','filets de merlu','calamars','saumon'] else '600 g'
            ing=[I(amount,protein)]+pantry(country)+[I('400 g','tomates concassées'),I('250 ml','bouillon adapté'),I('1 c. à café','sel'),I('1/2 c. à café','poivre')]
            data['ingredients']=ing;data['steps']=[S('Préparer','Couper '+protein+' en morceaux réguliers pour une cuisson homogène.'),S('Hacher les aromates','Mettre oignon et ail dans le bol.',5,0,'5'),S('Faire revenir','Ajouter la matière grasse.',300,120,'1'),S('Saisir la garniture','Ajouter '+protein+'.',420,120,'1',True),S('Mijoter','Ajouter tomates, bouillon, sel et poivre.',1500,100,'1',True,False,'Vérifier la cuisson au centre avant de servir.'),S('Rectifier','Goûter la sauce et ajuster l’assaisonnement.')];data['cookMinutes']=35
    data['description']=f"{title} : recette détaillée et guidée pour Monsieur Cuisine. Chaque ingrédient est nommé et chaque étape indique le temps, la température et la vitesse quand le robot est utilisé."
    data['difficulty']='Facile'
    return data

changed=0
for f in ROOT.rglob('recipe.json'):
    data=json.loads(f.read_text(encoding='utf-8'))
    names={norm(x.get('name','')) for x in data.get('ingredients',[])}
    generic=any(norm(x) in names for x in BAD) or any('adapté à la cuisine guidée' in data.get('description','') for _ in [0])
    if generic:
        data=enrich(data)
        f.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
        changed+=1
print(f'{changed} recettes génériques enrichies avec ingrédients et réglages détaillés')
