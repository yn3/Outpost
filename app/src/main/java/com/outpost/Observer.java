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
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.text.format.Time;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import external.SlidingTabLayout;


public class Observer extends FragmentActivity {

    BluetoothAdapter BTAdapter;
    Handler Looper;
    boolean loop = true;
    ArrayList arrayList;
    ArrayList nameList;
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
    public String mac_address = "";
    SharedPreferences settings;
    ArrayList urls;
    ///

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_observer);

                                                        /*-> FRAGMENT SETTINGS + SLIDEVIEWTABS <-*/

        List<Fragment> fragments = getFragments();

        pageAdapter = new PageAdapter(getSupportFragmentManager(), fragments);
        pager = (ViewPager) findViewById(R.id.viewpager);
        pager.setAdapter(pageAdapter);
        pager.setCurrentItem(2);
        ///necessary for avoid pausing RenderThreads and creating NULLPOINTERS while rendering canvas
        pager.setOffscreenPageLimit(5);
        final SlidingTabLayout tabLayout
                = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setViewPager(pager);

                                                                                     /*-> INIT <-*/
        Looper = new Handler();
        arrayList = new ArrayList<>();
        nameList = new ArrayList<>();
        urls = new ArrayList();
        i = new Intent(Observer.this, Scan_Service.class);
        if (alreadyScanning(Scan_Service.class)) bindService(i,
                CON_TO_SERVICE, Context.BIND_AUTO_CREATE);

                                                                                     /*-> MISC <-*/
        BTAdapter = BluetoothAdapter.getDefaultAdapter();
        run_forest.run();
        user_settings();

    }


    private void user_settings() {

        File dir
                = new File(Environment.getExternalStorageDirectory() + "/OutpostShare");
        boolean created = false;

        if (!dir.exists()) {

            created = dir.mkdirs();

        }

        if (created) {

            File d1 = new File(dir + "/id");
            File d2 = new File(dir + "/share_out");
            File d3 = new File(dir + "/share_in");
            File d4 = new File(dir + "/processor");
            File d5 = new File(dir + "/progress");

            d1.mkdirs();
            d2.mkdirs();
            d3.mkdirs();
            d4.mkdirs();
            d5.mkdirs();
            Log.w("OS", "created folders");

        }else{

            InputStream mIn = null;
            OutputStream mOut = null;

            Time time = new Time();
            time.clear(Time.getCurrentTimezone());
            time.setToNow();
            String devTemp =  time.format("%H_%M_%d_%m_%Y");
            File copy = new File(Environment.getExternalStorageDirectory() + "/OutpostShare/progress/" + "dump" + devTemp +".png");
            if(copy.exists()) copy.delete();
            if (!copy.exists()) {
                try {

                    mIn = new FileInputStream(Environment.getExternalStorageDirectory() + "/OutpostShare/id/" + "merge.png");
                    mOut = new FileOutputStream(copy);

                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = mIn.read(buffer)) != -1) {
                        mOut.write(buffer, 0, read);
                    }
                    mIn.close();
                    mIn = null;
                    mOut.flush();
                    mOut.close();
                    mOut = null;

                } catch (FileNotFoundException e1) {

                } catch (Exception e) {

                } finally {

                }
            }

        }


        settings = getSharedPreferences(SETTINGS, 0);
        boolean firstTimer = settings.getBoolean("firstLaunch", true);

        if (firstTimer) {

            Intent intent = new Intent(this,
                    Settings.class);
            startActivity(intent);
            finish();

        } else {

            nick = settings.getString("Nickname", null);

            if (BTAdapter.getName() != "OP@" + nick) {
                BTAdapter.setName("OP@" + nick);
            }

            mac_address = settings.getString("Mac", null);

        }
    }

                                                                            /*-> SERVICE SETUP<-*/
    void onStartScanning() throws InterruptedException {


        Intent discoverableIntent
                = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
        startActivity(discoverableIntent);

        startService(i);
        bindService(i,
                CON_TO_SERVICE, Context.BIND_AUTO_CREATE);


        run_forest.run();

    }


    void onStopScanning() {



        unbindService(CON_TO_SERVICE);
        stopService(i);
        Looper.removeCallbacks(run_forest);

    }

                                                                               /*-> LOOPTHREAD <-*/
    private Runnable run_forest = new Runnable() {

        @Override
        public void run() {



            handler = new DataHandler(getBaseContext());
            handler.open();

            if (oP.isAdded()) {

                String[] m1_trans = new String[2];
                m1_trans[0] = String.valueOf(handler.CountRows());
                dB.dataUp(m1_trans);

            }

            if(alreadyScanning(Scan_Service.class)){
                arrayList.clear();
                nameList.clear();
                arrayList.add(sS.getCurrDevices());
                nameList.add(sS.getCurrNames());

                    urls.clear();

                    for(int i = 0;i<arrayList.size();i++){
                        String tmp = handler.grabURL(String.valueOf(arrayList.get(i)));

                        urls.add(tmp);

                    }
                   pC.setConnector(urls);


            }
            handler.close();

            if (loop) start();

        }
    };

    private void start() {

        Looper.postDelayed(run_forest, 2000);

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

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

                                                                            /*-> SERVICE CHECK <-*/
    public boolean alreadyScanning(Class<?> serviceClass) {
        ActivityManager manager
                = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

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

                                                                                    /*-> SETGET <-*/
    void changeName(String name) {

        nick = name;

    }

    public int[] getMac() {

        String[] macs = mac_address.split(":");
        int[] mac = new int[macs.length];
        for (int i = 0; i < macs.length; i++) {
            mac[i] = Integer.parseInt(macs[i], 16);
        }
        return mac;
    }

                                                                                   /*-> STATES <-*/
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

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (alreadyScanning(Scan_Service.class)) unbindService(CON_TO_SERVICE);
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
}
