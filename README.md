# Recettes Monsieur Cuisine

Application Android Java de recettes guidées, pensée pour être utilisée directement devant le robot de cuisine.

## Contenu v1
- **54 recettes** incluses hors ligne.
- 6 cuisines : France, Sénégal, Italie, Espagne, États-Unis, Allemagne.
- 3 rubriques par pays : plats, entrées, desserts.
- Chaque recette possède son propre dossier avec `recipe.json` et `image.svg`.
- Recherche, filtres par pays/rubrique et favoris.
- Vue recette illustrée + liste de courses copiable.
- Mode cuisine pas-à-pas : chaque ingrédient est validé avant de passer au suivant.
- Réglages Monsieur Cuisine affichés à chaque étape : durée, température, vitesse, sens inverse et turbo quand applicable.
- Minuteur intégré avec son + vibration.
- Progression mémorisée pour reprendre une recette plus tard.

## Structure des recettes
`app/src/main/assets/recipes/<pays>/<categorie>/<recette>/recipe.json`

Chaque dossier de recette contient aussi `image.svg`.

## Compilation locale
```bash
./gradlew assembleDebug
```

APK :
`app/build/outputs/apk/debug/app-debug.apk`

## GitHub Actions
Le workflow `.github/workflows/build-apk.yml` compile l'APK à chaque push sur `main` et à la demande, puis le publie comme Artifact.
