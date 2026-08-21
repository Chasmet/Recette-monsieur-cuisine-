package com.chasmet.recettemonsieurcuisine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import java.io.InputStream;

public final class FeaturedMeatImages {
    private FeaturedMeatImages() {}

    public static boolean load(Context context, ImageView view, Recipe recipe) {
        int slot = slotFor(recipe.title);
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
        } catch (Exception ignored) {
            return false;
        }
    }

    private static int slotFor(String title) {
        String t = title == null ? "" : title.toLowerCase();
        if (t.contains("poulet") && (t.contains("légume") || t.contains("legume"))) return 0;
        if (t.contains("bourguignon") || (t.contains("boeuf") && t.contains("carotte"))) return 1;
        if (t.contains("dinde")) return 2;
        if (t.contains("porc") || t.contains("filet mignon")) return 3;
        if (t.contains("veau")) return 4;
        if (t.contains("agneau") || t.contains("mouton")) return 5;
        return -1;
    }
}
