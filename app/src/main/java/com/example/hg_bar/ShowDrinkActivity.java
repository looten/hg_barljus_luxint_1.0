package com.example.hg_bar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class ShowDrinkActivity extends ActionBarActivity {
    //  Switch switch_quick_choice;
    private static final String TAG = "ShowDrinkActivity";
    InterfaceFragment fragment;
    String drink_name;
    ListView list_view;
    JSONObject json = new JSONObject();
    Map<String, String> map = new HashMap<String, String>();
    ImageView drinkIcon;
    TextView drinkName;
    TextView noteView;
    ShowDrinkAdapter Adapter;
    ArrayList<ShowDrink> drinks, temp;

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


    //private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_show_drink);

        Intent intent = getIntent();
        // switch_quick_choice = (Switch) findViewById(R.id.switch_quick_choice);
        drinkIcon = (ImageView) findViewById(R.id.drinkIcon);
        drinkName = (TextView) findViewById(R.id.drinkName);
        list_view = (ListView) findViewById(R.id.listView);
        noteView = (TextView) findViewById(R.id.noteView);


        drinks = new ArrayList<>();
        String name = intent.getExtras().getString("name");
        drink_name = name;
        String type = intent.getExtras().getString("type");
        String bottles = intent.getExtras().getString("bottles");
        Log.d(TAG, bottles);
        //int count = intent.getExtras().getInt("count");
        String info = intent.getExtras().getString("info");
        //String count_s = count + "";


        switch (type) {
            case "S":
                drinkIcon.setImageResource(R.drawable.soft_icon);
                break;
            case "M":
                drinkIcon.setImageResource(R.drawable.milk_icon);
                break;
            case "J":
                drinkIcon.setImageResource(R.drawable.fruit_icon);
                break;
            default:
                drinkIcon.setImageResource(R.drawable.random_icon);
                break;
        }

        drinkName.setText(name);


        Log.d(TAG, "" + bottles);

        try {
            json = new JSONObject(bottles);
            //Log.d(TAG, "jArr" +json.getJSONArray("bottles"));

            JSONObject json_bottles = json.getJSONObject("bottles");
            Iterator x = json_bottles.keys();
            //JSONArray jsonArray = new JSONArray();

            while (x.hasNext()) {
                String key = (String) x.next();
                String value = (String) json_bottles.get(key);

                ShowDrink D = new ShowDrink();
                D.setBottle(key);
                D.setAmount(value);
                drinks.add(D);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Adapter = new ShowDrinkAdapter(getApplicationContext(), drinks);
        list_view.setAdapter(Adapter);
        noteView.setText(info);
        //countView.setText(count_s);

        list_view.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        ShowDrink drink = (ShowDrink) parent.getItemAtPosition(position);
                        Toast msg = Toast.makeText(getBaseContext(), drink.getBottle() + " = " + drink.getAmount(), Toast.LENGTH_SHORT);
                        msg.show();
                    }
                });

    }

    @Override
    protected void onResume() {
        super.onResume();
        fragment = new InterfaceFragment();
        getFragmentManager().beginTransaction().add(fragment, "fragment").commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_library_show, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        fragment.resetLed();
        getFragmentManager().beginTransaction().remove(fragment).commit();
        finish();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
