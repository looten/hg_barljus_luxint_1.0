package com.example.hg_bar;

/**
 * Created by Looten on 2015-07-29.
 */
import java.lang.reflect.Field;

import android.content.res.AssetManager;
import android.graphics.Typeface;

public final class FontsOverride {

    public static void setDefaultFont(AssetManager assetManager, String staticTypefaceFieldName, String fontAssetName) {
        final Typeface regular = Typeface.createFromAsset(assetManager, fontAssetName);
        replaceFont(staticTypefaceFieldName, regular);
    }

    protected static void replaceFont(String staticTypefaceFieldName, final Typeface newTypeface) {
        try {
            final Field staticField = Typeface.class.getDeclaredField(staticTypefaceFieldName);
            staticField.setAccessible(true);
            staticField.set(null, newTypeface);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}