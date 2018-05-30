package com.homeaway.homeawayplaces.droid.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.widget.TextView;

/**
 * Created by pavan on 28/05/18.
 *
 */
public class ViewCompatUtil {

    public static Drawable setDrawableTint(@NonNull Drawable drawable, @ColorInt int color) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            drawable.setTint(color);
        }
        else {
            DrawableCompat.setTint(drawable = drawable.mutate(), color);
        }
        return drawable;
    }

    public static void setCompoundDrawablesWithIntrinsicBounds(@NonNull TextView textView,
             @ColorInt int start, @ColorInt int top, @ColorInt int end, @ColorInt int bottom) {

        Drawable[] drawables = textView.getCompoundDrawables();
        Drawable[] tintedDrawables = new Drawable[drawables.length];
        if (start != -1) {
            tintedDrawables[0] = setDrawableTint(drawables[0], start);
        }
        if (top != -1) {
            tintedDrawables[1] = setDrawableTint(drawables[1], top);
        }
        if (end != -1) {
            tintedDrawables[2] = setDrawableTint(drawables[2], end);
        }
        if (bottom != -1) {
            tintedDrawables[3] = setDrawableTint(drawables[3], bottom);
        }

        textView.setCompoundDrawablesWithIntrinsicBounds(tintedDrawables[0], tintedDrawables[1],
                tintedDrawables[2], tintedDrawables[3]);
    }

    public static void setCompoundDrawablesWithIntrinsicBounds(Context context,
            @NonNull TextView textView, @ColorRes int start, @ColorRes int top, @ColorRes int end,
            @ColorRes int bottom) {

        Drawable[] drawables = textView.getCompoundDrawables();
        Drawable[] tintedDrawables = new Drawable[drawables.length];
        if (start != -1) {
            tintedDrawables[0] = setDrawableTintByFix(drawables[0], context, start);
        }
        if (top != -1) {
            tintedDrawables[1] = setDrawableTintByFix(drawables[1], context, top);
        }
        if (end != -1) {
            tintedDrawables[2] = setDrawableTintByFix(drawables[2], context, end);
        }
        if (bottom != -1) {
            tintedDrawables[3] = setDrawableTintByFix(drawables[3], context, bottom);
        }

        textView.setCompoundDrawablesWithIntrinsicBounds(tintedDrawables[0], tintedDrawables[1],
                tintedDrawables[2], tintedDrawables[3]);
    }

    public static Drawable setDrawableTintByFix(@NonNull Drawable drawable,
            @NonNull Context context, @ColorRes int colorResId) {

        drawable = DrawableCompat.wrap(drawable);
        drawable = drawable.mutate();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            drawable.setTint(ContextCompat.getColor(context, colorResId));
        }
        else {
            DrawableCompat.setTint(drawable, ContextCompat.getColor(context, colorResId));
        }
        return drawable;
    }
}
