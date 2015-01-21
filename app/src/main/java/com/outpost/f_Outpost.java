package com.outpost;

import android.content.SharedPreferences;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andre on 11.01.2015.
 */
public class f_Outpost extends Fragment {


    public static final String SETTINGS = "user_settings";
    List<String> arrayList;
    TextView hi, b1;
    Switch start_scan;
    TextView usr,out1;
    TextureView view;
    private RenderThread mThread;
    private int mWidth;
    private int mHeight;
    boolean scanning;
    int[] mac;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View v = inflater.inflate(R.layout.f_outpost, container, false);
        view = (TextureView) v.findViewById(R.id.texture_outpost);

        view.setSurfaceTextureListener(new CanvasListener());
        view.setOpaque(false);

        start_scan = (Switch) v.findViewById(R.id.switch1);
        usr = (TextView) v.findViewById(R.id.op_usr);

        hi = (TextView) v.findViewById(R.id.hi);
        b1 = (TextView) v.findViewById(R.id.b1);
        out1 = (TextView) v.findViewById(R.id.out1);
        b1.setText("");
        if (((Observer) getActivity()).alreadyScanning(Scan_Service.class)) {
            start_scan.setChecked(true);
        }

        start_scan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    ((Observer) getActivity()).onStartScanning();

                    scanning = true;
                    Toast.makeText(getActivity(), "Scanning started.", Toast.LENGTH_LONG).show();
                } else {

                    ((Observer) getActivity()).onStopScanning();
                    scanning = false;
                    Toast.makeText(getActivity(), "Scanning stopped.", Toast.LENGTH_LONG).show();

                }
            }
        });


        return v;


    }

    ;

    public void curr(String dev) {

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        arrayList = new ArrayList<>();
        SharedPreferences settings = getActivity().getSharedPreferences(SETTINGS, 0);
        String nick = settings.getString("Nickname", "");

        usr.setText(nick);
        //ara = new ArrayAdapter<String>(getActivity(),  android.R.layout.simple_list_item_1, arrayList);

        //lV.setAdapter(ara);
        mac = ((Observer)getActivity()).getMac();

    }


    public void dataUp(String[] data) {


        //hi.setText(String.valueOf(currDevices));

        out1.setText(data[0]);
        //arrayList.clear();

        //arrayList.addAll(Arrays.asList(data));

        //ara.notifyDataSetChanged();
    }

    private class RenderThread extends Thread {
        float la;
        Matrix mat = new Matrix();
        private volatile boolean mRunning = true;

        private float clr;
        private Camera cam = new Camera();
        Ships ships;
        ArrayList arrayShips;
        @Override
        public void run() {

            Log.w("", String.valueOf(mWidth));
            Log.w("", String.valueOf(mHeight));
            Paint paint = new Paint();
            paint.setColor(0xff00ff00);
            paint.setColor(Color.WHITE);
            int cnt = 0;

            clr = 10;
            while (mRunning && !Thread.interrupted()) {
                final Canvas canvas = view.lockCanvas(null);


                try {
                    canvas.drawColor(0x00000000, PorterDuff.Mode.CLEAR);
                    Paint p1 = new Paint();
                    p1.setColor(0xff63A088);

                    Path cons = new Path();


                    arrayShips = new ArrayList();
                    for (int i = 0; i<20;i++) {
                        ships = new Ships(canvas, p1);
                        arrayShips.add(ships);
                    }


                    for (int i = 0; i<arrayShips.size();i++){
                        Ships s = (Ships) arrayShips.get(i);
                        s.update();
                    }

                    Paint mShadow = new Paint();
// radius=10, y-offset=2, color=black
                    mShadow.setShadowLayer(2.0f, 1.0f, 2.0f, 0xFF000000);
// in onDraw(Canvas)



                    int strokeWidth = 2;
                    mShadow.setStrokeWidth(strokeWidth);
                    mShadow.setStyle(Paint.Style.FILL_AND_STROKE);
                    cons.moveTo(mWidth / 2, (float) (mHeight / 2));
                    cons.lineTo(mWidth / 2, mHeight);

                    //->*Math.cos(Math.toRadians(degrees));
                    cons.moveTo(mWidth / 3, mHeight);
                    cons.lineTo(mWidth / 3, (float) (mHeight / 1.04));
                    cons.lineTo((float) (mWidth / 3.4), (float) (mHeight / 1.04));
                    cons.lineTo((float) (mWidth / 3.4), (float) (mHeight / 1.16));
                    cons.lineTo((float) (mWidth / 4.1), (float) (mHeight / 1.16));
                    cons.lineTo((float) (mWidth / 4.1), (float) (mHeight / 1.06));
                    cons.lineTo((float) (mWidth / 6.2), (float) (mHeight / 1.06));
                    cons.lineTo((float) (mWidth / 6.2), (float) (mHeight / 1.02));
                    cons.lineTo((float) (0), (float) (mHeight / 1.02));
                    canvas.drawPath(cons, paint);


                    canvas.drawCircle(mWidth / 2, mHeight / 2, 50, scanning==true? paint: p1);
                    strokeWidth = 5;
                    paint.setStrokeWidth(strokeWidth);
                    paint.setStyle(Paint.Style.STROKE);



                    paint.setStyle(Paint.Style.FILL);
                    float alpha = 255;
                    for (int i = 0; i < 12; i++) {
                        cnt++;
                        cnt %= 1720;
                        if (cnt % 10 == 1 && cnt < 60) {
                            p1.setAlpha(0);
                        } else {
                            p1.setAlpha((int) (alpha / (i + 1)));
                        }

                        canvas.save();
                        canvas.rotate(45 - (i * 4));
                        canvas.translate(0, mHeight / 2);
                        canvas.drawRect(mWidth - (i * 4), -(i * 4), mWidth / 23 - (i * 4), mHeight / 2 + (i * 4), p1);

                        canvas.restore();
                    }
                    //

                            /*
                    cam.save();
                    cam.rotateX(0);
                    cam.rotateY(0);
                    cam.rotateZ((float) 3.1);
                    cam.getMatrix(mat);
                    canvas.setMatrix(mat);
                    canvas.drawRect(300,0,mWidth,mHeight/3,paint);
                    cam.restore();


                    */

                    //canvas.concat(mat);
                    //cam.restore();


                } finally {
                    view.unlockCanvasAndPost(canvas);
                }


                if (clr < 33) clr += 0.1;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        getActivity().getWindow().getDecorView().setBackgroundColor(Color.rgb((int) clr, (int) (clr * 1.7), (int) (clr * 2.13)));

                    }
                });




                try {
                    Thread.sleep(15);
                } catch (InterruptedException e) {
                    // Interrupted
                }
            }
        }

        public void stopRendering() {
            interrupt();
            mRunning = false;
        }

    }


    private class CanvasListener implements TextureView.SurfaceTextureListener {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface,
                                              int width, int height) {
            try {
                mThread = new RenderThread();
                mThread.start();
                mWidth = view.getWidth();
                mHeight = view.getHeight();
            }catch(Exception e){

            }
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {

            if (mThread != null) {
                mThread.stopRendering();
            }
            return true;
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface,
                                                int width, int height) {

            try {
                mWidth = view.getWidth();
                mHeight = view.getHeight();
            } catch (Exception e) {

            }
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    }

    public class Ships {

        private int sx, sy, ex, ey;
        private boolean sxToRight, syToBottom;
        private boolean exToRight, eyToBottom;
        Canvas can;

        Paint p = new Paint();


        public Ships(Canvas canvas, Paint paint) {
           can=canvas;
            p=paint;
            sx = (int) (Math.random() * mWidth);
            sy = (int) (Math.random() * mHeight);
            ex = (int) (Math.random() * mWidth);
            ey = (int) (Math.random() * mHeight);

        }

        public void update() {

            Path path = new Path();
            path.moveTo(sx, sy);
            path.lineTo(ex, ey);
            path.moveTo(ex - 10, ey - 10);
            path.lineTo(sx - 10, sy - 10);

            can.drawPath(path, p);

            if (sxToRight) {
                sx += 12;
                if (sx >= mWidth * 2) {
                    sxToRight = false;
                }
            } else {
                sx -= 3;
                if (sx < -mWidth) {
                    sxToRight = true;
                }
            }

            if (syToBottom) {
                sy += 3;
                if (sy >= mHeight * 2) {
                    syToBottom = false;
                }
            } else {
                sy -= 3;
                if (sy < -mHeight) {
                    syToBottom = true;
                }
            }

            if (exToRight) {
                ex += 3;
                if (ex >= mWidth * 2) {
                    exToRight = false;
                }
            } else {
                ex -= 3;
                if (ex < -mWidth) {
                    exToRight = true;
                }
            }

            if (eyToBottom) {
                ey++;
                if (ey >= mHeight * 2) {
                    eyToBottom = false;
                }
            } else {
                ey--;
                if (ey < -mHeight) {
                    eyToBottom = true;
                }
            }



        }

    }
}
