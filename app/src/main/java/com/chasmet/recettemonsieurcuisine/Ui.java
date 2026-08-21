package com.chasmet.recettemonsieurcuisine;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class Ui {
    private Ui() {}

    public static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    public static TextView text(Context context, String value, float size, boolean bold) {
        TextView view = new TextView(context);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(Color.rgb(27, 27, 27));
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        view.setLineSpacing(0, 1.12f);
        return view;
    }

    public static void margin(View view, int left, int top, int right, int bottom) {
        ViewGroup.LayoutParams raw = view.getLayoutParams();
        LinearLayout.LayoutParams p;
        if (raw instanceof LinearLayout.LayoutParams) {
            p = (LinearLayout.LayoutParams) raw;
        } else {
            p = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
        p.setMargins(dp(view.getContext(), left), dp(view.getContext(), top),
                dp(view.getContext(), right), dp(view.getContext(), bottom));
        view.setLayoutParams(p);
    }

    public static Button filterButton(Context context, String text) {
        Button button = new Button(context);
        button.setText(text);
        button.setTextSize(14);
        button.setAllCaps(false);
        button.setBackgroundResource(R.drawable.bg_filter);
        button.setTextColor(Color.rgb(27, 27, 27));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        p.setMargins(0, 0, dp(context, 8), 0);
        button.setLayoutParams(p);
        return button;
    }

    public static void setFilterSelected(Button button, boolean selected) {
        button.setSelected(selected);
        button.setTextColor(selected ? Color.WHITE : Color.rgb(27, 27, 27));
    }
}
