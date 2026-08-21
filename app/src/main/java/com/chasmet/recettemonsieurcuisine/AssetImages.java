package com.chasmet.recettemonsieurcuisine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import java.io.InputStream;

public final class AssetImages {
    private AssetImages() {}

    public static void load(Context context, ImageView view, String assetPath) {
        try (InputStream in = context.getAssets().open(assetPath)) {
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            if (bitmap != null) {
                view.setImageBitmap(bitmap);
                view.setScaleType(ImageView.ScaleType.CENTER_CROP);
                view.setBackgroundColor(0xFFF5EBDD);
                return;
            }
        } catch (Exception ignored) {}
        view.setImageResource(R.drawable.app_logo);
        view.setScaleType(ImageView.ScaleType.CENTER_CROP);
    }
}
