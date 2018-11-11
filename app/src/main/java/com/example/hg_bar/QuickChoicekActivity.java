package com.example.hg_bar;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class QuickChoicekActivity extends ActionBarActivity {
    ListView list_view;
    SearchableAdapter Adapter;
    // AssetManager assetManager;
    InterfaceFragment fragment;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_quick_choice);
        list_view = (ListView) findViewById(R.id.drinkView);

        list_view.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String drink = String.valueOf(parent.getItemAtPosition(position));
                        startActivity(fragment.send(drink, getApplicationContext()));
                        getFragmentManager().beginTransaction().remove(fragment).commit();

                    }
                }
        );

        list_view.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                String drink = String.valueOf(arg0.getItemAtPosition(pos));

                if (fragment.setLongClickQuickChoice(drink,getApplicationContext()) == 1) {
                    Toast msg = Toast.makeText(getBaseContext(),
                            drink +" lades till i Snabbval", Toast.LENGTH_SHORT);
                    msg.show();
                } else {
                    Toast msg = Toast.makeText(getBaseContext(),
                            drink + " togs bort från Snabbval", Toast.LENGTH_SHORT);
                    msg.show();
                    printDrinks();

                }

                return true;
            }
        });
    }

    void printDrinks() {
        ArrayList<String> drinks = fragment.getQuickChoiceList(getApplicationContext());
        // assetManager = getAssets();
        AssetManager assetManager = getAssets();
        Adapter = new SearchableAdapter(getApplicationContext(), assetManager, drinks, false);
        list_view.setAdapter(Adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.actionbar_connect, menu);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        fragment = new InterfaceFragment();
        getFragmentManager().beginTransaction().add(fragment, "fragment").commit();
        printDrinks();

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
        // if (id == R.id.add_drink) {startActivity(new Intent(getApplicationContext(), AddNewDrink.class));}

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
