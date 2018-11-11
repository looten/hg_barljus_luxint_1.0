package com.example.hg_bar;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;

import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class RandomActivity extends AppCompatActivity {
    Button button_soda, button_juice,
            button_milk, button_random;
    InterfaceFragment fragment;
    private static final String TAG = "RandomActivity";

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

        ActionBar ab = getSupportActionBar();
        ab.setDisplayShowTitleEnabled(true);
        ab.setDisplayShowHomeEnabled(true);

        setContentView(R.layout.activity_random_new);
        button_soda = (Button) findViewById(R.id.button_soda);
        button_juice = (Button) findViewById(R.id.button_juice);
        button_milk = (Button) findViewById(R.id.button_milk);
        button_random = (Button) findViewById(R.id.button_random);

        button_soda.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        send("S");
                    }
                }
        );

        button_juice.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        send("J");
                    }
                }
        );

        button_milk.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        send("M");
                    }
                }
        );

        button_random.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        send("R");
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        fragment = new InterfaceFragment();
        getFragmentManager().beginTransaction().add(fragment, "fragment").commit();
    }

    private void send(String c) {
        String drink = "";
        drink = fragment.getRandom(c, getApplicationContext());
        if (drink.isEmpty()) {
            Toast msg = Toast.makeText(getBaseContext(),
                    "Kunde inte slumpa drink då ingen databas finns", Toast.LENGTH_SHORT);
            msg.show();
        } else {
            Log.e(TAG, "drink.length" + drink.length());
            startActivity(fragment.send(drink, getApplicationContext()));
            getFragmentManager().beginTransaction().remove(fragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.actionbar_connect, menu);
        return true;
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
