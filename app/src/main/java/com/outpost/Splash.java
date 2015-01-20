package com.outpost;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by andre on 11.01.2015.
 */
public class Splash extends Activity {


    ImageView splash;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        anim_splash();
        splash = (ImageView)findViewById(R.id.splash);
        splash.setVisibility(View.VISIBLE);

    }

    private void anim_splash() {

        new Handler().postDelayed(new Runnable() {


            @Override
            public void run() {
                splash.setVisibility(View.INVISIBLE);
            }
        },1);


        new Handler().postDelayed(new Runnable() {


            @Override
            public void run() {
                splash.setVisibility(View.VISIBLE);
            }
        },1);



        new Handler().postDelayed(new Runnable() {


            @Override
            public void run() {
                Intent i = new Intent(Splash.this, Observer.class);
                startActivity(i);

                finish();
            }
        },1);

    }

}
