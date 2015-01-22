


package com.outpost;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;


public class Scan_Service extends Service {

    private static boolean alreadyRunning = false;
    private final IBinder mBinder = new LocalBinder();
    ScanBT btScanner;
    DataHandler handler;
    ArrayList deviceList, distanceList, addressList;
    Handler loop_handler = new Handler();
    boolean loop = true;
    BluetoothAdapter BTAdapter;
    String con_device = "";
    Transfer transfer;
    int loop_time = 1500;
    boolean stateBlock = false;
    boolean setex = false;


    @Override
    public void onCreate() {

        BTAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.v("Scanner", "reached");
        btScanner = new ScanBT();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        IntentFilter filter3 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
        this.registerReceiver(btScanner, filter1);
        this.registerReceiver(btScanner, filter2);
        this.registerReceiver(btScanner, filter3);
        registerReceiver(btScanner, filter);
        transfer = new Transfer(getApplicationContext());

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        deviceList = new ArrayList<>();
        addressList = new ArrayList<>();
        distanceList = new ArrayList<Integer>();
        if (BTAdapter.isDiscovering()) {

            BTAdapter.cancelDiscovery();

        }

        BTAdapter.startDiscovery();
        if (!alreadyRunning) {
            run_forest.run();
        }



        transfer.start();
        return Service.START_NOT_STICKY;

    }

    public void stopLoop() {
        loop = false;

    }

    protected void start() {

        loop = true;
        loop_handler.postDelayed(run_forest, loop_time);

    }

    public void onDestroy() {

        unregisterReceiver(btScanner);
        loop = false;
        Log.v("service", "destroyed");
        alreadyRunning = false;
        loop_handler.removeCallbacks(run_forest);

    }


    public ArrayList getCurrDevices() {

        if (deviceList != null) {
            return deviceList;
        } else {

            return null;
        }
    }


    public int getCurrRows() {

        return deviceList.size();

    }

    private Runnable run_forest = new Runnable() {

        @Override
        public void run() {
            alreadyRunning = true;
            handler = new DataHandler(getBaseContext());
            handler.open();

            if (transfer.getState() > 2) {
               // Toast.makeText(getApplicationContext(), "CONNECTED", Toast.LENGTH_LONG).show();

            }

            if(BTAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
            {
               Log.w("IAM NOT DISCOVERABLE ANYMORE","IAM NOT DISCOVERABLE ANYMORE");
            }


            for (int i = 0; i < deviceList.size(); i++) {

                Time time = new Time();
                Time date;
                time.clear(Time.getCurrentTimezone());
                time.setToNow();
                date = time;
                int is_aura = 0;
                String devTemp = String.valueOf(deviceList.get(i));
                if (devTemp.contains("OP@")) {
                    is_aura = 1;
                }
                long what = handler.insert(devTemp, String.valueOf(addressList.get(i)), is_aura,
                        time.format("%H:%M"), date.format("%d.%m.%Y"));
            }

            handler.close();




            if(transfer.getState()== transfer.STATE_CONNECTED) {
                int rnd = (int) Math.random() * 5;
                String txt = "JO FUCK YEA" + String.valueOf(rnd);
                byte[] bytes = new byte[0];
                try {

                    bytes = txt.getBytes("UTF-8");
                    transfer.write(bytes);

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            if (loop) {

                start();

            }

        }

    };


    class ScanBT extends BroadcastReceiver {

        public void onReceive(Context c, Intent intent) {
            String action = intent.getAction();


            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                String foundDevice = device.getName();
                loop_handler.removeCallbacks(ms_decider);


                if (foundDevice.contains("OP@")) {
                    int rnd = ((int) (Math.random() * 2000)+500);
                    addressList.clear();
                    deviceList.clear();
                    addressList.add(device.getAddress());
                    deviceList.add(foundDevice);
                    if (transfer.getState()!=transfer.STATE_CONNECTED ) {
                        //loop_handler.postDelayed(ms_decider, rnd);
                    }


                }


            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                if (BTAdapter.isDiscovering()) {

                    BTAdapter.cancelDiscovery();

                }

                BTAdapter.startDiscovery();
                Log.w("","connecting again");
            }
            else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
               Log.w("connected","connected");


            }

            else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                Log.w("disco request","disco request");
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                Log.w("disconnected","disconnected");
                stateBlock =false;
            }
                /*
            if (BluetoothDevice.ACTION_NAME_CHANGED.equals(action)) {
                loop_handler.removeCallbacks(ms_decider);
                stateBlock = true;
                Log.w("","device changed name");
            }
            */
        }
    }


    public String devi() {
        return con_device;

    }

    @Override
    public IBinder onBind(Intent intent) {

        return mBinder;
    }

    public class LocalBinder extends Binder {

        Scan_Service getService() {
            return Scan_Service.this;
        }

    }


    private Runnable ms_decider = new Runnable() {

        @Override
        public void run() {

                    for (int i = 0; i < addressList.size(); i++) {

                        String dev_name = BTAdapter.getName();
                        int rnd = (int) Math.random() * 5;

                       // BTAdapter.setName(dev_name + String.valueOf(rnd));

                        Toast.makeText(getApplicationContext(), "I'M A CLIENT", Toast.LENGTH_LONG).show();
                        if (transfer.getState() < 3)
                            transfer.connect(BTAdapter.getRemoteDevice(addressList.get(i).toString()), false);

                    }
                }


    };


}
