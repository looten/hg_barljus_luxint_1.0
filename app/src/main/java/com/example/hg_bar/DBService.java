package com.example.hg_bar;

import android.app.AlertDialog;
import android.app.IntentService;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Looten on 2015-09-17.
 */

public class DBService extends IntentService {
    private static final String TAG = "DBService";
    private DBHandler dbHandler;

    public static final String DB_GET_LIST_LIB = "get_list_lib";
    public static final String DB_GET_LIST_TOP_TEN = "get_list_top_ten";

    private Handler progressBarHandler = new Handler();

    public DBService() {
        super(DBService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String msg = intent.getAction();
        ArrayList<String> tmp = new ArrayList();
        Intent broadIntent = new Intent();
        Log.d(TAG, "msg:" + msg);
        if (msg == DB_GET_LIST_LIB) {
            Log.d(TAG, "i DB_GET_LIST_LIB");
            tmp = getList(getApplicationContext());
            broadIntent.setAction(DB_GET_LIST_LIB);
            broadIntent.putExtra("drinks", tmp);

            sendBroadcast(broadIntent);
        } else if (msg == DB_GET_LIST_TOP_TEN) {
            Log.d(TAG, "i DB_GET_LIST_TOP_TEN");
            tmp = getTopTen(getApplicationContext());
            broadIntent.setAction(DB_GET_LIST_TOP_TEN);
            broadIntent.putExtra("drinks", tmp);

            sendBroadcast(broadIntent);
        }

    }

    protected ArrayList getList(Context context) {
        dbHandler = DBHandler.getInstance(context);
        ArrayList<String> list = dbHandler.databaseToString();
        return list;
    }

    public void clearDatabase(Context context) {
        dbHandler = DBHandler.getInstance(context);
        new AlertDialog.Builder(context)
                .setTitle("Ta bort databas")
                .setMessage("Vill du ta bort databasen?")
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dbHandler.clearDatabase();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public void addDatabase(Context context) {
        dbHandler = DBHandler.getInstance(context);
        final ProgressDialog mDialog = new ProgressDialog(context);
        mDialog.setMessage("Läser in databas, vargod vänta...");
        mDialog.setProgress(0);
        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDialog.setCancelable(true);
        mDialog.show();
        final int max = dbHandler.getFileSize();

        mDialog.setMax(100);

        new Thread(new Runnable() {
            @Override
            public void run() {
                dbHandler.fileDrinks(max);
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {

                while (dbHandler.status < 100) {
                    //  System.out.println("STATUS " + dbHandler.status  + " MAX " + max);
                    progressBarHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mDialog.setProgress(dbHandler.status);
                        }
                    });


                    if (dbHandler.status >= 100) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mDialog.dismiss();
                    }

                }
            }
        }).start();


    }

    public Drink getRow(String drink, Context context) {
        dbHandler = DBHandler.getInstance(context);
        Drink D = dbHandler.getRow(drink);
        return D;
    }
/*
    public Drink getOrup(Context context) {
        dbHandler = DBHandler.getInstance(context);
        Drink orup = dbHandler.getOrup();
        return orup;
    }
    */

    public String getRandom(String type, Context context) {
        dbHandler = DBHandler.getInstance(context);
        String drink = dbHandler.getRandom(type);
        return drink;
    }

    public ArrayList getTopTen(Context context) {
        Log.d(TAG, "i getTopTen");
        dbHandler = DBHandler.getInstance(context);
        ArrayList<String> drink = dbHandler.getTop();
        Log.d(TAG, "LISTAN " + drink);

        return drink;
    }

    public void resetTopTen(Context context) {
        dbHandler = DBHandler.getInstance(context);
        dbHandler.resetTopTen();
    }

    public ArrayList getQuickChoiceList(Context context) {
        dbHandler = DBHandler.getInstance(context);
        ArrayList<String> drinks = dbHandler.getQuickChoice();
        return drinks;
    }

    public int setLongClickQuickChoice(String drink, Context context) {
        dbHandler = DBHandler.getInstance(context);
        int val = dbHandler.setLongClickQuickChoice(drink);
        return val;
    }

    public String getType(String drink, Context context) {
        dbHandler = DBHandler.getInstance(context);
        String type = dbHandler.getType(drink);
        return type;
    }
}