package com.example.hg_bar;

import android.app.Activity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class InterfaceFragment extends Fragment {
    //private static BTService mBTService;
    private BTService mBTService;
    private static final String TAG = "InterfaceFragment";
    private BluetoothAdapter mBluetoothAdapter = null;

    private MsgHandler mHandler;
    private static String mConnectedDeviceName = null;
    //public static boolean STRESS_TEST_STATE = false;
    private static final int REQUEST_ENABLE_BT = 1;
    //public static final int SEND_CLEAR = 0x0000;
    Intent DBServiceIntent;
    public static final int SEND_CLEAR_ALL = 0x000000;
    private static DBService dbService;

    public InterfaceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setupDb();
        //setupBt(threadSetup());

        //Checks if BT if supported
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Activity activity = getActivity();
            Toast.makeText(activity, "Bluetooth finns ej på enheten", Toast.LENGTH_LONG).show();
            activity.finish();
        }
    }

    public MsgHandler threadSetup() {
        HandlerThread thread = new HandlerThread("Handler thread ", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        Looper mainLooper = Looper.getMainLooper();
        //Looper serviceLooper = thread.getLooper();
        mHandler = new MsgHandler(mainLooper);
        return mHandler;

    }

    @Override
    public void onResume() {
        super.onResume();
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.

        if (mBTService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBTService.getState() == mBTService.STATE_NONE) {
                // Start the Bluetooth chat services
                Log.d(TAG, "mBTService start");
                mBTService = new BTService(threadSetup());
                mBTService.getmHandler();
            } else if (mBTService.getState() != mBTService.STATE_NONE) {
                mBTService.getmHandler();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        checkBtStatus();
    }

    /*BTService funktioner
     *
     */

    private void setupBt(MsgHandler handler) {
        mBTService = new BTService(handler);
        getActivity().startService(new Intent(getActivity(), BTService.class));
    }

    private void requestEnableBt() {
        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
    }


    public void checkBtStatus() {
        if (!mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "mBluetoothAdapter är inte på");
            requestEnableBt();
        } else if (mBTService == null) {
            Log.d(TAG, "mBTService är null");
            setupBt(threadSetup());
        }
    }

    public boolean checkState() {
        Log.d(TAG, "mBTService.getState() " + mBTService.getState());
        if (mBTService.getState() != BTService.STATE_CONNECTED) {
            Activity activity = getActivity();
            Toast.makeText(activity, "Kunde inte skicka drink", Toast.LENGTH_SHORT).show();
            return false;
        } else return true;
    }

    public void stopService() {
        getActivity().stopService(new Intent(getActivity(), BTService.class));
    }

    public void restart() {
        Log.d(TAG, "restart, kollar om bt är på " + mBTService.getState());
        checkBtStatus();

        if (mBluetoothAdapter.isEnabled()) {
            mBTService.restart();
        }
    }

    public void stop() {
        checkBtStatus();
        mBTService.stop();
    }

    public void resetLed() {
        mBTService.sendReset(SEND_CLEAR_ALL);
    }

    public void turnOff() {
        // mBTService.send(SEND_CLEAR);
    }

    public Intent send(String drink, Context context) {
        Drink send_drink = dbService.getRow(drink, context);

       if (checkState())
        {
            mBTService.send(send_drink.getBottlesSend());
        }

        Intent i = new Intent(context, ShowDrinkActivity.class);
        i.putExtra("name", send_drink.getName());
        i.putExtra("bottles", send_drink.getBottles().toString());
        i.putExtra("count", send_drink.getCount());
        i.putExtra("info", send_drink.getInfo());
        i.putExtra("type", send_drink.getType());

        return i;

    }

    /*DBService funktioner
     *
     */
    private void setupDb() {
        DBServiceIntent = new Intent(getActivity(), DBService.class);
        dbService = new DBService();
        getActivity().startService(new Intent(getActivity(), DBService.class));

    }

    public void clearDatabase(Context context) {
        dbService.clearDatabase(context);
    }

    public void addDatabase(Context context) {
        dbService.addDatabase(context);
    }

    public void getList(Context context) {
        Intent intent = new Intent(context, DBService.class);
        intent.setAction(DBService.DB_GET_LIST_LIB);
        Activity activity = getActivity();
        while (activity == null) {
            activity = getActivity();
            Log.e(TAG, "activity == null");
        }
        getActivity().startService(intent);
    }

    public String getRandom(String type, Context context) {
        String drink = dbService.getRandom(type, context);
        return drink;
    }

    public void getTopTen(Context context) {
        Intent intent = new Intent(context, DBService.class);
        intent.setAction(DBService.DB_GET_LIST_TOP_TEN);
        Activity activity = getActivity();
        while (activity == null) {
            activity = getActivity();
            Log.e(TAG, "activity == null");
        }
        getActivity().startService(intent);
    }
/*
    public void getOrup(Context context) {
        Drink D = dbService.getOrup(context);
        startOrup(D, context);
    }
    */
/*
    public void startOrup(final Drink D, Context context){
        final ProgressDialog mDialog = new ProgressDialog(context);
        mDialog.setMessage("Orup pågår...");
        //mDialog.setProgress(0);

        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setCancelable(true);
        mDialog.show();
        new Thread(new Runnable() {
            ArrayList bottles_codes= D.bottles_send;
            ArrayList bottles_names= D.bottles;
            @Override

            public void run() {
                while (bottles_codes.size() > 2) {
                    randomBottle(bottles_codes, bottles_names);


                    if (bottles_codes.size() == 2) {
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

        mBTService.sendReset(SEND_CLEAR_ALL);

    }
    */

    private void randomBottle(ArrayList bottle_codes, ArrayList bottle_names){
        Random rand = new Random();
        int nr;
        int randomNum;
        if (bottle_codes.size() > 0){
            int low = 1;
            int high = 3;
            int loop = rand.nextInt(high-low)+low;



            for(int i=0; i<=loop; i++){
                nr = bottle_codes.size() - 1;
                randomNum = rand.nextInt(nr);

                bottle_codes.remove(randomNum);
                bottle_names.remove(randomNum);
            }




        }
    }

    public void resetTopTen(Context context) {
        dbService.resetTopTen(context);
    }

    public ArrayList getQuickChoiceList(Context context) {
        ArrayList<String> drinks = dbService.getQuickChoiceList(context);
        return drinks;
    }

    public int setLongClickQuickChoice(String drink, Context context) {
        int val = dbService.setLongClickQuickChoice(drink, context);
        return val;
    }

    /*Handler funktioner
    *
    */

    private void setStatus(CharSequence subTitle) {
        Activity activity = getActivity();
        if (activity != null) {
            Log.d(TAG, "hit1");
            activity.setTitle(subTitle);
        } else
            Log.e(TAG, "activity är null");
    }

    private void setStatus(int resId) {
        Activity activity = getActivity();
        if (activity != null) {
            Log.d(TAG, "hit2");
            activity.setTitle(resId);
        } else
            Log.e(TAG, "activity är null");
    }

    public class MsgHandler extends Handler {
        public MsgHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Activity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BTService.STATE_CONNECTED:
                            //setStatus("Connected to " + mConnectedDeviceName);
                            setStatus(R.string.title_connected);
                            break;
                        case BTService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BTService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                        case BTService.STATE_CONNECTION_LOST:
                            System.out.println("lost 1 ");
                            setStatus(R.string.title_connection_lost);
                            break;
                        ///case BTService.STATE_RECONNECT:
                            //setStatus(R.string.title_reconnecting);
                        case BTService.STATE_ADAPTER_DISABLED:
                            System.out.println("lost 2");
                            //setStatus(R.string.title_connection_lost);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    //  byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    // String writeMessage = new String(writeBuf);
                    break;
                case Constants.MESSAGE_READ:
                    // byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    // String readMessage = new String(readBuf, 0, msg.arg1);
                    // System.out.println("msg read  " + readMessage);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }

}
