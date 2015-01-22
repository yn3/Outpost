package com.outpost;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import external.SlidingTabLayout;


public class Observer extends FragmentActivity {

    BluetoothAdapter BTAdapter;
    Handler Looper;
    boolean loop = true;
    ArrayList arrayList;
    ArrayList distanceList;
    PageAdapter pageAdapter;
    ViewPager pager;
    Intent i;
    f_DataCenter dB = new f_DataCenter();
    f_Outpost oP = new f_Outpost();
    Scan_Service sS = new Scan_Service();
    f_Processor pC = new f_Processor();
    f_Storage sT = new f_Storage();
    f_CargoBay cB = new f_CargoBay();
    DataHandler handler;
    public static final String SETTINGS = "user_settings";
    boolean checked = false;
    String nick = "";
    public  String mac_address = "";
    ///

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_observer);


                                                        /*-> FRAGMENT SETTINGS + SLIDEVIEWTABS <-*/

        List<Fragment> fragments = getFragments();

        pageAdapter = new PageAdapter(getSupportFragmentManager(), fragments);

        pager = (ViewPager)findViewById(R.id.viewpager);
        pager.setAdapter(pageAdapter);
        pager.setCurrentItem(2);

        final SlidingTabLayout tabLayout = (SlidingTabLayout)findViewById(R.id.sliding_tabs);
        tabLayout.setViewPager(pager);

        Log.w("", String.valueOf(Environment.getExternalStorageDirectory()));
                                                                                     /*-> INIT <-*/

        Looper = new Handler();
        arrayList = new ArrayList<>();
        distanceList = new ArrayList<Integer>();
        i = new Intent(Observer.this, Scan_Service.class);
        if(alreadyScanning(Scan_Service.class))bindService(i, CON_TO_SERVICE, Context.BIND_AUTO_CREATE);

                                                                          /*-> BLUETOOTH SETUP <-*/


        BTAdapter = BluetoothAdapter.getDefaultAdapter();



        run_forest.run();
        user_settings();


    }

    private void user_settings() {
        File dir = new File(Environment.getExternalStorageDirectory() + "/OutpostShare");
        boolean exists = true;
        if (!dir.exists()) {
            exists = dir.mkdir();
           File dir_in = new File(Environment.getExternalStorageDirectory() + "/OutpostShare/in");
           File dir_out = new File(Environment.getExternalStorageDirectory() + "/OutpostShare/out");
            exists = dir_in.mkdir();
            exists = dir_out.mkdir();

        }
        SharedPreferences settings = getSharedPreferences(SETTINGS,0);
        boolean firstTimer = settings.getBoolean("firstLaunch",true);

        if(firstTimer){

            Intent intent = new Intent(this,
                    Settings.class);
            startActivity(intent);
            finish();

        }else{

            nick = settings.getString("Nickname",null);
            Log.w("NAME",nick);
            if(BTAdapter.getName() != "OP@" + nick){
                BTAdapter.setName("OP@" + nick);
            }
            mac_address = settings.getString("Mac", null);
        }

    }

    /*-> SERVICE SETUP<-*/
    void onStartScanning(){

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
        startActivity(discoverableIntent);


        startService(i);
        if(!alreadyScanning(Scan_Service.class))bindService(i, CON_TO_SERVICE, Context.BIND_AUTO_CREATE);

        run_forest.run();

    }





    void onStopScanning(){




        //if(alreadyScanning(Scan_Service.class))unbindService(CON_TO_SERVICE);
        stopService(i);
        Looper.removeCallbacks(run_forest);

    }



                                                                               /*-> LOOPTHREAD <-*/

    private Runnable run_forest = new Runnable() {

        @Override
        public void run() {

            if (oP.isAdded()) {
                handler = new DataHandler(getBaseContext());
                handler.open();
                arrayList.clear();
                String[] m1_trans = new String[2];
                // m1_trans[1] = String.valueOf(sS.getCurrDevices().size());
                m1_trans[0] = String.valueOf(handler.CountRows());
                oP.dataUp(m1_trans);


                handler.close();
            }




            if (loop) start();

        }
    };



    private void start() {

        Looper.postDelayed(run_forest, 1500);

    }



                                                                                /*-> FRAGMENTS <-*/

    private List<Fragment> getFragments() {

        List<Fragment> fList = new ArrayList<Fragment>();

        fList.add(pC);
        fList.add(cB);
        fList.add(oP);
        fList.add(dB);
        fList.add(sT);

        return fList;

    }

                                                                           /*-> BIND TO SERVICE <-*/

    public ServiceConnection CON_TO_SERVICE = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            Scan_Service.LocalBinder binder = (Scan_Service.LocalBinder) service;
            sS = binder.getService();

            Log.v("BOUND", "SERVICECONNECTION");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };



                                                                            /*-> SERVICE CHECK <-*/

    public boolean alreadyScanning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager

                .getRunningServices(Integer.MAX_VALUE)) {

            if (serviceClass.getName().equals(service.service.getClassName())) {
                checked = true;
                return true;

            } else {

                checked = false;
            }
        }

        return false;
    }



                                                                                    /*-> SETTER <-*/

       void changeName(String name){

           nick = name;

       }



                                                                                   /*-> STATES <-*/

    @Override
    protected void onDestroy() {
        super.onDestroy();


        if(alreadyScanning(Scan_Service.class))unbindService(CON_TO_SERVICE);
        loop = false;
        Looper.removeCallbacks(run_forest);


    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
        //unbindService(CON_TO_SERVICE);

    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder ex = new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(" Quit ");

        if (alreadyScanning(Scan_Service.class)) {

            ex.setMessage("Service still running in Background! Continue to exit App?");

        } else {

            ex.setMessage("Exit App?");

        }
        ex.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                System.exit(0);

            }
        });

        ex.setNegativeButton("No ", null).show();

    }



    public int[] getMac() {

        String[] macs = mac_address.split(":");
        int[] mac = new int[macs.length];
        for (int i = 0; i < macs.length; i++) {
            mac[i] = Integer.parseInt(macs[i], 16);
        }
        return mac;
    }


}
