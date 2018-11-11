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
import android.widget.SearchView;
import android.widget.Toast;

import java.util.ArrayList;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class LibraryActivity extends AppCompatActivity implements SearchView.OnQueryTextListener  {
    private static final String TAG = "LibraryActivity";
    ListView list_view;
    SearchView library_search;
    SearchableAdapter Adapter;
    InterfaceFragment fragment;
    ArrayList<String> drinks, temp;
    AssetManager assetManager;

    public class BaseActivity extends Activity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Log.d(TAG ,"Error");
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
                    Log.e("Error"+Thread.currentThread().getStackTrace()[2],paramThrowable.getLocalizedMessage());
                }
            });
        }
    }

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


        setContentView(R.layout.activity_library);
        assetManager = getAssets();

        temp = new ArrayList<>();

        library_search = (SearchView) findViewById(R.id.searchView);
        list_view = (ListView) findViewById(R.id.drinkView);
        library_search.setIconifiedByDefault(false);

        Adapter = new SearchableAdapter(getApplicationContext(), assetManager, drinks, false);

        list_view.setAdapter(Adapter);

        library_search.setOnQueryTextListener(LibraryActivity.this);
        list_view.requestFocus();

        list_view.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String drink = String.valueOf(parent.getItemAtPosition(position));
                        startActivity(fragment.send(drink, getApplicationContext()));
                        getFragmentManager().beginTransaction().remove(fragment).commit();
                    }
                });


        list_view.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                String drink = String.valueOf(arg0.getItemAtPosition(pos));

                if (fragment.setLongClickQuickChoice(drink, getApplicationContext()) == 1) {
                    Toast msg = Toast.makeText(getBaseContext(),
                            "Lades till i Snabbval", Toast.LENGTH_SHORT);
                    msg.show();
                } else {
                    Toast msg = Toast.makeText(getBaseContext(),
                            "Togs bort från Snabbval", Toast.LENGTH_SHORT);
                    msg.show();
                }
                return true;
            }

        });

    }

    private final BroadcastReceiver rec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getAction();
            if (msg == DBService.DB_GET_LIST_LIB) {
                temp = intent.getExtras().getStringArrayList("drinks");
                printDatabase();
            }
        }
    };

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(DBService.DB_GET_LIST_LIB);
        registerReceiver(rec, filter);

        fragment = new InterfaceFragment();
        getFragmentManager().beginTransaction().add(fragment, "fragment").commit();
        getList();

    }

    @Override
    protected void onPause() {
        unregisterReceiver(rec);
        super.onPause();
    }

    //hämtar lista i egen tråd
    private void getList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                fragment.getList(LibraryActivity.this);
            }
        }).start();
    }

    //uppdaterar  main UI
    public void printDatabase() {
        //Adapter = new ArrayAdapter<>(getApplicationContext(),R.layout.rowlayout,R.id.label, drinks);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                drinks.clear();
                drinks.addAll(temp);

                Adapter.notifyDataSetChanged();

                list_view.invalidateViews();

                list_view.requestFocus();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.actionbar_connect, menu);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        //  Adapter.getFilter().filter(newText);
        Adapter.getFilter().filter(newText);
        return true;
        //return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

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
        getFragmentManager().beginTransaction().remove(fragment).commit();
        finish();

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
