package com.example.hg_bar;

import android.app.Application;
import android.util.Log;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by Looten on 2015-08-23.
 */
public class ApplicationGlobal extends Application  {
    private static final String TAG = "ApplicationGlobal";
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "ApplicationGlobal");
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("gnuolanerg.ttf")
                        //.setFontAttrId(R.attr.fontPath)
                        .build()
        );
    }
}
