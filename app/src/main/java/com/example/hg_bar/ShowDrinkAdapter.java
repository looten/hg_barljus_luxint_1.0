package com.example.hg_bar;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Looten on 2016-07-30.
 */
public class ShowDrinkAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<ShowDrink> drinks;
    private LayoutInflater mInflater;
    FontCache font;

    public class BaseActivity extends Activity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
                    Log.e("Error"+Thread.currentThread().getStackTrace()[2],paramThrowable.getLocalizedMessage());
                }
            });
        }
    }

    public ShowDrinkAdapter(Context context, ArrayList<ShowDrink> drinks) {
        this.context = context;
        this.drinks = drinks;
        mInflater = LayoutInflater.from(context);
    }


    public int getCount() {
        return drinks.size();
    }

    public Object getItem(int position) {
        return drinks.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        font = new FontCache();

        if (convertView == null) {
            Typeface typeFace = font.get("gnuolanerg.ttf", context);

            convertView = mInflater.inflate(R.layout.rowlayout_show_drink, null);

            holder = new ViewHolder();

            holder.bottle = (TextView) convertView.findViewById(R.id.bottle);
            holder.amount = (TextView) convertView.findViewById(R.id.amount);

            holder.bottle.setTypeface(typeFace);
            holder.amount.setTypeface(typeFace);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.bottle.setText(drinks.get(position).getBottle());
        holder.amount.setText(drinks.get(position).getAmount());

        return convertView;
    }

    static class ViewHolder {
        TextView bottle;
        TextView amount;
    }
}
