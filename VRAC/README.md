# VRAC — illustrations des recettes

Dépose ici toutes les illustrations sans les ranger manuellement.

## Règle de nommage

Chaque fichier doit porter exactement l'identifiant de la recette :

`<id-recette>.jpg`

Formats acceptés : `.jpg`, `.jpeg`, `.png`, `.webp`.

Exemples :

- `senegal-plats-yassa-poulet.jpg`
- `france-plats-boeuf-bourguignon.jpg`
- `italie-desserts-tiramisu.jpg`
- `espagne-plats-paella-valencienne.jpg`

Le script `tools/prepare_vrac.py` reconnaît automatiquement les images, les copie dans le bon dossier de recette et modifie le champ `image` du `recipe.json` correspondant.

La liste complète est générée dans `VRAC/LISTE_IMAGES.txt`.
