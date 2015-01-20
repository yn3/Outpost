package com.outpost;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by andre on 14.01.2015.
 */
public class Settings extends FragmentActivity {
    public static final String SETTINGS = "user_settings";
    BluetoothAdapter BTAdapter;
    String mac;
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        final EditText userNick = (EditText)findViewById(R.id.field_nickName);
        Button confirm_nick = (Button)findViewById(R.id.nick_confirm);
        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);

        if(wifiManager.isWifiEnabled()) {
            WifiInfo info = wifiManager.getConnectionInfo();
            mac = info.getMacAddress();
        } else {
            wifiManager.setWifiEnabled(true);
            WifiInfo info = wifiManager.getConnectionInfo();
            mac = info.getMacAddress();
        }
        confirm_nick.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {


                String nick = userNick.getText().toString();
                if(nick.length()<2) {
                    Toast.makeText(getApplicationContext(), "Nick must be at least 2 chars long", Toast.LENGTH_LONG).show();
                }else if(nick.length()>10){
                    Toast.makeText(getApplicationContext(), "Maximum length is 10 chars", Toast.LENGTH_LONG).show();
                }else{
                    SharedPreferences settings = getSharedPreferences(SETTINGS,0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean("firstLaunch",false).commit();
                    editor.putString("Nickname",nick).commit();
                    editor.putString("Mac",mac).commit();
                    Observer observer = new Observer();
                    observer.changeName(nick);
                    Toast.makeText(getApplicationContext(), "Set Nickname to " + nick + " ", Toast.LENGTH_LONG).show();
                    BTAdapter = BluetoothAdapter.getDefaultAdapter();
                    BTAdapter.setName("OP@" + nick + " ");
                    Intent i = new Intent(getApplicationContext(),Observer.class);
                    startActivity(i);
                    finish();
                }
            }

        });

    }

}
