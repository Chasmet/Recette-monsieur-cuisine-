package com.chasmet.recettemonsieurcuisine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import java.io.InputStream;

public final class FeaturedMeatImages {
    private FeaturedMeatImages() {}

    public static boolean load(Context context, ImageView view, Recipe recipe) {
        int slot = slotFor(recipe);
        if (slot < 0) return false;
        try (InputStream in = context.getAssets().open("featured/meat_sprite.jpg")) {
            Bitmap sheet = BitmapFactory.decodeStream(in);
            if (sheet == null) return false;
            int col = slot % 2;
            int row = slot / 2;
            int w = sheet.getWidth() / 2;
            int h = sheet.getHeight() / 3;
            Bitmap crop = Bitmap.createBitmap(sheet, col * w, row * h, w, h);
            view.setImageBitmap(crop);
            view.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return true;
        } catch (Exception ignored) { return false; }
    }

    private static int slotFor(Recipe recipe) {
        StringBuilder source = new StringBuilder(recipe.title == null ? "" : recipe.title.toLowerCase());
        for (Recipe.Ingredient ingredient : recipe.ingredients) source.append(' ').append(ingredient.name == null ? "" : ingredient.name.toLowerCase());
        String t = source.toString();
        if (t.contains("veau")) return 4;
        if (t.contains("agneau") || t.contains("mouton")) return 5;
        if (t.contains("dinde") || t.contains("pintade")) return 2;
        if (t.contains("porc") || t.contains("filet mignon") || t.contains("jambon") || t.contains("saucisse") || t.contains("merguez")) return 3;
        if (t.contains("boeuf") || t.contains("bœuf") || t.contains("steak") || t.contains("viande hachée") || t.contains("viande hachee")) return 1;
        if (t.contains("poulet") || t.contains("canard") || t.contains("lapin")) return 0;
        return -1;
    }
}
