package com.example.hg_bar;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class TopTenActivity extends AppCompatActivity {
    private static final String TAG = "TopTenActivity";
    ListView ListView;
    SearchableAdapter Adapter;
    InterfaceFragment fragment;
    ArrayList<String> drinks, temp;
    AssetManager assetManager;


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
   // AssetManager assetManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setDisplayShowTitleEnabled(true);
        ab.setDisplayShowHomeEnabled(true);

        if(savedInstanceState != null){
            Log.d(TAG, "savedInstanceState ej null");
            ArrayList<String> temp = savedInstanceState.getStringArrayList("drinks");
            drinks = temp;
        }
        else{
            drinks = new ArrayList<>();
        }


        setContentView(R.layout.activity_top_ten);
        assetManager = getAssets();

        temp = new ArrayList<>();

        ListView = (ListView) findViewById(R.id.bottleView);

        Adapter = new SearchableAdapter(getApplicationContext(), assetManager, drinks, true);
        //Adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, drinks);
        ListView.setAdapter(Adapter);

        ListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String drink = String.valueOf(parent.getItemAtPosition(position));
                        startActivity(fragment.send(drink, getApplicationContext()));
                        getFragmentManager().beginTransaction().remove(fragment).commit();

                    }
                }
        );
    }

    private final BroadcastReceiver rec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getAction();
            if (msg == DBService.DB_GET_LIST_TOP_TEN) {
                temp = intent.getExtras().getStringArrayList("drinks");
                printDatabase();
            }
        }
    };

    public void printDatabase() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                drinks.clear();
                drinks.addAll(temp);

                Adapter.notifyDataSetChanged();

                ListView.invalidateViews();

                ListView.requestFocus();
            }
        });
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<String> drinks = Adapter.getList();
        outState.putStringArrayList("drinks",drinks);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(DBService.DB_GET_LIST_TOP_TEN);
        registerReceiver(rec, filter);

        fragment = new InterfaceFragment();
        getFragmentManager().beginTransaction().add(fragment, "fragment").commit();
        getList();
    }

    private void getList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                fragment.getTopTen(TopTenActivity.this);
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.actionbar_top_ten, menu);
        menuInflater.inflate(R.menu.menu_top_ten, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.delete_toptenfile) {
            fragment.resetTopTen(getApplicationContext());
            getList();
        }
        if (id == R.id.stop_connection) {
            fragment.stop();
        }
        if (id == R.id.restart_connection) {
            fragment.restart();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}


