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
    int loop_time = 500;
    boolean stateBlock = false;


    @Override
    public void onCreate() {

        BTAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.v("Scanner", "reached");
        btScanner = new ScanBT();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
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
                Toast.makeText(getApplicationContext(), "CONNECTED", Toast.LENGTH_LONG).show();

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

                /*
                if (foundDevice.contains("OP@")) {
                    int pin = 0000;
                    try {
                        Log.d("setPin()", "Try to set the PIN");
                        Method m = device.getClass().getMethod("setPasskey", int.class);
                        m.invoke(device, pin);
                        Log.d("setPin()", "Success to add the PIN.");
                    } catch (Exception e) {
                        Log.e("setPin()", e.getMessage());
                    }
                    Method m = null;
                    try {
                        m = device.getClass().getMethod("createBond", (Class[]) null);
                        try {
                            m.invoke(device, (Object[]) null);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                }
                */

                if (foundDevice.contains("OP@")) {
                    int rnd = (int) (Math.random() * 1000);
                    addressList.clear();
                    deviceList.clear();
                    addressList.add(device.getAddress());
                    deviceList.add(foundDevice);
                    transfer.start();
                    if (transfer.getState() < 3) {
                        loop_handler.postDelayed(ms_decider, rnd);
                    }
                }


            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                if (BTAdapter.isDiscovering()) {

                    BTAdapter.cancelDiscovery();

                }

                BTAdapter.startDiscovery();

            } else if (BluetoothDevice.ACTION_NAME_CHANGED.equals(action)) {

                Log.w("", "transfer.start");
                Toast.makeText(getApplicationContext(), "I'M A SERVER", Toast.LENGTH_LONG).show();


            }
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
            if (!stateBlock) {
                stateBlock = true;
                for (int i = 0; i < addressList.size(); i++) {

                    String dev_name = BTAdapter.getName();
                    int rnd = (int) Math.random() * 5;

                    BTAdapter.setName(dev_name.substring(0, dev_name.length() - 1) + String.valueOf(rnd));

                    Log.w("", "transfer connect");
                    Toast.makeText(getApplicationContext(), "I'M A CLIENT", Toast.LENGTH_LONG).show();
                    if (transfer.getState() < 3)
                        transfer.connect(BTAdapter.getRemoteDevice(addressList.get(i).toString()), false);

                }
            }
        }

    };


}
