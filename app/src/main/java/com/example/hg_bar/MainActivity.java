package com.example.hg_bar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    Button button_library, button_top_ten,
            button_quick_choice, button_random;

    InterfaceFragment fragment;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    //ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)..build();
    //ImageLoader.getInstance().init(config);

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

        setContentView(R.layout.activity_main);


        button_library = (Button) findViewById(R.id.button_library);
        button_top_ten = (Button) findViewById(R.id.button_top_ten);
        button_quick_choice = (Button) findViewById(R.id.button_quick_choice);
        button_random = (Button) findViewById(R.id.button_random);

        button_library.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        getFragmentManager().beginTransaction().remove(fragment).commit();
                        startActivity(new Intent(getApplicationContext(), LibraryActivity.class));
                    }
                }
        );

        button_top_ten.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        getFragmentManager().beginTransaction().remove(fragment).commit();
                        startActivity(new Intent(getApplicationContext(), TopTenActivity.class));
                    }
                }
        );

        button_quick_choice.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        getFragmentManager().beginTransaction().remove(fragment).commit();
                        startActivity(new Intent(getApplicationContext(), QuickChoicekActivity.class));
                    }
                }
        );

        button_random.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        getFragmentManager().beginTransaction().remove(fragment).commit();
                        startActivity(new Intent(getApplicationContext(), RandomActivity.class));
                    }
                }
        );
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fragment = new InterfaceFragment();
        getFragmentManager().beginTransaction().add(fragment, "fragment").commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        menuInflater.inflate(R.menu.actionbar_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.clear_database) {
            fragment.clearDatabase(this);
        }
        if (id == R.id.add_database) {
            fragment.addDatabase(this);
        }
        if (id == R.id.stop_connection) {
            fragment.stop();
        }
        if (id == R.id.restart_connection) {
            fragment.restart();
        }
        if (id == R.id.reset_led) {
            fragment.resetLed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Log.d(TAG, "innan ApplicationGlobal");
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.a_interface_v10/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.a_interface_v10/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
