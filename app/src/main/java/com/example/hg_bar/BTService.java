package com.example.hg_bar;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Looten on 2015-06-17.
 */
public class BTService extends Service {
    private static final int REQUEST_ENABLE_BT = 1;

    private static final String TAG = "BTService";

    private static BluetoothAdapter mbtAdapter;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String module_1_address = "98:D3:31:90:2F:0A";//BT MODUL 1
    private static String module_2_address = "98:D3:31:50:38:6F"; //BT MODUL 2
    private static BluetoothDevice mbtDevice;
    private static String address = module_2_address;
    private ConnectThread mBtConnect;
    private static ConnectedThread mBtConnected;

    private static int mState;
    //private static Handler mHandler;
    private final Handler mHandler;
    public static final int STATE_ADAPTER_DISABLED = -2;
    public static final int STATE_CONNECTION_LOST = -1;
    public static final int STATE_NONE = 0;     // we're doing nothing
    //public static final int STATE_RECONNECT = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 1; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 2;  //


    public static BluetoothSocket socket;

    public BTService(){
        mHandler = null;
    }

     public BTService(Handler handler) {
        mbtAdapter = BluetoothAdapter.getDefaultAdapter();

        mHandler = handler;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        //   Toast.makeText(this, "The new Service was Created", Toast.LENGTH_LONG).show();
        mbtAdapter = BluetoothAdapter.getDefaultAdapter();

        setState(STATE_NONE);
        checkConnection();
        connect();
    }

    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        try {
            mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
        } catch (NullPointerException e) {
            Log.e(TAG, "nullptr i setState");
        }
    }

    public void getmHandler() {
        mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, mState, -1).sendToTarget();
    }

    public synchronized int getState() {
        return mState;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Toast.makeText(this, startId + " Service Started", Toast.LENGTH_LONG).show();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Service avslutad", Toast.LENGTH_LONG).show();
    }

    public synchronized void connect() {
        mbtDevice = mbtAdapter.getRemoteDevice(address);
        Log.d(TAG, "Connect to: " + mbtDevice);
        if (mBtConnected != null) {
            mBtConnected.cancel();
            mBtConnected = null;
        }
        //ensureDiscoverable();

        mBtConnect = new ConnectThread(mbtDevice);
        mBtConnect.start();
        setState(STATE_CONNECTING);
    }

    public synchronized void restart() {
        Log.d(TAG, "restart");

        stop();
        //if(getState() == STATE_CONNECTED)
        //setState(STATE_RECONNECTIN);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 1s = 1000ms
                connect();
            }
        }, 1000);
    }

    public synchronized void stop() {
        //stop all threads
        Log.d(TAG, "stop");
        if (mBtConnect != null) {
            mBtConnect.cancel();
            mBtConnect = null;
        }

        if (mBtConnected != null) {
            mBtConnected.cancel();
            mBtConnected = null;
        }

        if (getState()!= STATE_NONE)
            setState(STATE_NONE);
    }

    public void sendReset(int data) {
        ConnectedThread c;
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            c = mBtConnected;
        }
        c.resetLed(data);
    }

    public void send(int data) {
        ConnectedThread c;
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            c = mBtConnected;
        }
        c.sendData(data);

    }

    public void send(ArrayList<Integer> data) {
        ConnectedThread c;
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            c = mBtConnected;
        }

        for (int x : data)
            c.sendData(x);

    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.d(TAG, "connected");
        // Cancel the thread that completed the connection
        if (mBtConnect != null) {
            mBtConnect.cancel();
            mBtConnect = null;
        }

        // Cancel any thread currently running a connection
        if (mBtConnected != null) {
            mBtConnected.cancel();
            mBtConnected = null;
        }

        // Start the thread to manage the connection and perform transmissions
        if (getState() != STATE_NONE) {
            mBtConnected = new ConnectedThread(socket);
            mBtConnected.start();
            try {
                Message msg = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
                Bundle bundle = new Bundle();
                bundle.putString(Constants.DEVICE_NAME, device.getName());
                msg.setData(bundle);
                mHandler.sendMessage(msg);
            } catch (NullPointerException e) {
                Log.e(TAG, "nullptr i connected");
            }


        }
    }

    public void checkConnection() {
        Log.d(TAG, "Kollar connection, lägger till brodrec");

        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(MyReceiver, filter);

    }

    private final BroadcastReceiver MyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "I BroadcastReceiver ");
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.d(TAG, "1");
                // Get the BluetoothDevice object from the Intent
                // BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // if (device.getAddress().equals(address)) {
                //   Log.d(TAG, "1.1");
                // connectToExistingBT(device);

                //}
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                Log.d(TAG, "2");
                // BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Check if the connected device is one we had comm with
                // if (device.getAddress().equals(address)) {
                //}

            } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                Log.d(TAG, "3");
                stop();

            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                Log.d(TAG, "4");
                setState(STATE_CONNECTION_LOST);
            } else if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 1) == BluetoothAdapter.STATE_TURNING_OFF) {
                Log.d(TAG, "5");
                setState(STATE_CONNECTION_LOST);

            } else if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 1) == BluetoothAdapter.STATE_OFF) {
                Log.d(TAG, "6");
                setState(STATE_ADAPTER_DISABLED);

            }
        }

    };

    private void connectToExistingBT(BluetoothDevice device) {
        new ConnectThread(device);
    }

    private class ConnectThread extends Thread {
        //AsyncTask
        private final BluetoothSocket mmbtSocket;
        private BluetoothDevice mmbtDevice;

        // Insert your bluetooth devices MAC address

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;

            mmbtDevice = device;

            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = mmbtDevice.createRfcommSocketToServiceRecord(MY_UUID);

            } catch (IOException e) {
                Log.e(TAG, "¤In ConnectThread(): Could not make socket. ");
                connectionLost();
            }

            mmbtSocket = tmp;
            socket = mmbtSocket;
        }

        public void run() {
            mbtAdapter.cancelDiscovery();

            try {
                mmbtSocket.connect();
                Log.d(TAG, "¤In ConnectThread(): Getting data link. ");
                setState(STATE_CONNECTED);
            } catch (IOException e) {
                try {
                    Log.e(TAG, "¤Stänger socket då det inte gick att skapa connection till device");
                    mmbtSocket.close();
                    connectError();

                } catch (IOException e2) {
                    Log.e(TAG, "¤In ConnectThread(): Could not close socket connection. ");
                    connectError();
                }
            }

            synchronized (BTService.this) {
                mBtConnect = null;
            }

            if (BTService.this.getState() != STATE_NONE)
                connected(mmbtSocket, mmbtDevice);
        }

        public void cancel() {
            try {
                mmbtSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "In ConnectThread(): Could not close socket");
            }
        }

        private void mconnectionLost() {
            BTService.this.stop();
            try {
                Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString(Constants.TOAST, "Could not connect to device");
                setState(STATE_NONE);
                msg.setData(bundle);
                mHandler.sendMessage(msg);
            } catch (NullPointerException e) {
                Log.e(TAG, "nullptr i ConnectThread");
            }
        }

    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final OutputStream mmOutStream;
        private final InputStream mmInStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            OutputStream tmpOut = null;
            InputStream tmpIn = null;
            try {
                tmpOut = socket.getOutputStream();
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "%Forsokte gora outpt stream");
                connectionLost();
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            Log.d(TAG, "%Skapade outputstream " + mmOutStream);

        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes = 0;
            while (true) {
                /*try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);


                    // Send the obtained bytes to the UI Activity
                    //mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
                */
            }


        }

        public void sendData(int message) {
            try {
                byte[] bytes = ByteBuffer.allocate(4).putInt(message).array();

                mmOutStream.write(bytes[2]);
                mmOutStream.write(bytes[3]);

            } catch (IOException e) {
                Log.e(TAG, "%Forsokte skicka: " + message + " men lyckades inte. " + e.getMessage());
                connectionLost();
            }
        }

        public void resetLed(int message) {
            try {
                byte[] bytes = ByteBuffer.allocate(4).putInt(message).array();
                mmOutStream.write(bytes[1]);
                mmOutStream.write(bytes[2]);
                mmOutStream.write(bytes[3]);

            } catch (IOException e) {
                Log.e(TAG, "%Forsokte skicka: " + message + " men lyckades inte. " + e.getMessage());
                connectionLost();
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "In ConnectedThread(): Could not close socket");
            }
        }
    }

    private void connectionLost() {
        try {
            setState(STATE_NONE);
            BTService.this.stop();
            Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            bundle.putString(Constants.TOAST, "Device connection was lost");
            msg.setData(bundle);
            mHandler.sendMessage(msg);

        } catch (NullPointerException e) {
            Log.e(TAG, "nullptr i connectionLost()");
        }

    }

    private void connectError() {
        try {
            setState(STATE_NONE);
            BTService.this.stop();
            Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            bundle.putString(Constants.TOAST, "Could not connect to device");
            msg.setData(bundle);
            mHandler.sendMessage(msg);

        } catch (NullPointerException e) {
            Log.e(TAG, "nullptr i connectError()");
        }

    }

}

