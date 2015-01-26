


package com.outpost;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;
import java.util.ArrayList;


public class Scan_Service extends Service {

    private static boolean alreadyRunning = false;
    private final IBinder mBinder = new LocalBinder();
    ScanBT btScanner;
    DataHandler handler;
    ArrayList remoteNames, remoteAddress,tmpN,tmpA;
    Handler loop_handler = new Handler();
    boolean loop = true;
    BluetoothAdapter BTAdapter;
    Transfer transfer;
    int loop_time = 2500;
    boolean stateBlock = false;
    boolean enag = true;
    boolean shareFiles = false;


    @Override
    public void onCreate() {

        BTAdapter = BluetoothAdapter.getDefaultAdapter();
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

        remoteNames = new ArrayList<>();
        remoteAddress = new ArrayList<>();
        tmpA = new ArrayList<>();
        tmpN= new ArrayList<>();
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

        if ( tmpA!=null && !tmpA.isEmpty()) {

            return  tmpA;
        } else {

            return null;
        }
    }


    public int getCurrRows() {

        if (remoteAddress!=null && !remoteAddress.isEmpty()) {
            return remoteNames.size();
        }else{

            return 0;
        }

    }


    public ArrayList getCurrNames() {


        if (remoteAddress!=null ) {

            return remoteNames;
        } else {

            return null;
        }
    }

    private Runnable run_forest = new Runnable() {

        @Override
        public void run() {
            alreadyRunning = true;

            handler = new DataHandler(getBaseContext());
            handler.open();

            for (int i = 0; i < remoteNames.size(); i++) {

                Time time = new Time();
                Time date;
                time.clear(Time.getCurrentTimezone());
                time.setToNow();
                date = time;
                int is_aura = 0;
                String devTemp = String.valueOf(remoteNames.get(i));

                if (devTemp.contains("OP@")) {
                    is_aura = 1;
                }
                long what = handler.insert(devTemp, String.valueOf( remoteAddress.get(i)), is_aura,
                        time.format("%H:%M"), date.format("%d.%m.%Y"));
            }

            if (BTAdapter.isDiscovering()) {

                BTAdapter.cancelDiscovery();

            }

            BTAdapter.startDiscovery();


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
                BluetoothDevice device
                        = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                String foundDevice = device.getName();
                loop_handler.removeCallbacks(clearThread);
                remoteAddress.clear();
                remoteNames.clear();
                tmpA.clear();
                remoteAddress.add(device.getAddress());
                remoteNames.add(foundDevice);
                if (foundDevice.contains("OP@")) {

                    tmpA.add(device.getAddress());


                    loop_handler.postDelayed(clearThread, 10000);
                    if (transfer.getState() != transfer.STATE_CONNECTED) {
                        handler.open();
                        if (handler.grabURL(String.valueOf(device.getAddress())).length() < 2) {
                            //if(!handler.checkTime(String.valueOf(device.getAddress())) || shareFiles)
                            loop_handler.postDelayed(ms_decider, 100);
                            Log.w("INIT", "CONNECT LOOP");
                        }
                        handler.close();
                    }

                }


            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {


                Log.w("","connecting again");
            }
            else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
               Log.w("connected","connected");


            }

            else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                Log.w("disco request","disco request");
            }
            else if (BluetoothDevice.ACTION_NAME_CHANGED.equals(action)) {

                stateBlock =false;
                //Log.w("Other device changed name","");
            }

        }
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

                    for (int i = 0; i <  remoteAddress.size(); i++) {


                        Toast.makeText(getApplicationContext(), "I'M A CLIENT", Toast.LENGTH_LONG).show();
                        if (transfer.getState() < 3)
                            transfer.connect(BTAdapter.getRemoteDevice
                                    ( remoteAddress.get(i).toString()), false);

                    }
                }


    };


    private Runnable clearThread = new Runnable() {

        @Override
        public void run() {

            remoteAddress.clear();
            remoteNames.clear();
            tmpA.clear();
            Log.w("", "cleared all");
        }


    };

}
