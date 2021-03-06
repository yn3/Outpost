package com.outpost;

import android.content.SharedPreferences;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
    TextView usr;
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
        //start_scan.setVisibility(View.GONE);
        usr = (TextView) v.findViewById(R.id.op_usr);

        hi = (TextView) v.findViewById(R.id.hi);

        b1 = (TextView) v.findViewById(R.id.b1);


        b1.setText("");
        if (((Observer) getActivity()).alreadyScanning(Scan_Service.class)) {
            start_scan.setChecked(true);
        }

        start_scan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    try {
                        ((Observer) getActivity()).onStartScanning();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    scanning = true;

                } else if(!isChecked){

                    ((Observer) getActivity()).onStopScanning();
                    scanning = false;

                }else{

                    Toast.makeText(getActivity(), "Please set an ID IMAGE in the Storage.", Toast.LENGTH_LONG).show();

                }
            }
        });



        return v;


    }

    ;




    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        arrayList = new ArrayList<>();
        SharedPreferences settings = getActivity().getSharedPreferences(SETTINGS, 0);
        String nick = settings.getString("Nickname", "");

        usr.setText(nick);
        //ara = new ArrayAdapter<String>(getActivity(),  android.R.layout.simple_list_item_1, arrayList);

        //lV.setAdapter(ara);
        mac = ((Observer) getActivity()).getMac();

    }


    public void dataUp(String[] data) {


        //hi.setText(String.valueOf(currDevices));

       // out1.setText(data[0]);
        //arrayList.clear();

        //arrayList.addAll(Arrays.asList(data));

        //ara.notifyDataSetChanged();
    }

    private class RenderThread extends Thread {

        private int sx, sy, ex, ey;
        private boolean sxToRight, syToBottom;
        private boolean exToRight, eyToBottom;

        float la;
        Matrix mat = new Matrix();
        private volatile boolean mRunning = true;
        private float clr;
        private Camera cam = new Camera();
        Ships ships;
        ArrayList arrayShips;

        @Override
        public void run() {

            Paint paint = new Paint();
            Paint p2 = new Paint();
            paint.setColor(0xff00ff00);
            p2.setColor(0xfffffff);
            paint.setColor(Color.LTGRAY);
            int cnt = 0;

            arrayShips = new ArrayList();
            for (int i = 0; i<12;i++) {
                ships = new Ships();
                arrayShips.add(ships);
            }
            clr = 128;
            while (mRunning && !Thread.interrupted()) {
                final Canvas canvas = view.lockCanvas(null);


                try {
                    canvas.drawColor(0x00000000, PorterDuff.Mode.CLEAR);
                    Paint p1 = new Paint();
                    p1.setColor(0xff63A088);

                    Paint mShadow = new Paint();
                    mShadow.setShadowLayer(2.0f, 1.0f, 2.0f, 0xFFFFF000);


                    paint.setStyle(Paint.Style.STROKE);
                    for (int i = 0; i<arrayShips.size();i++){
                        Ships s = (Ships) arrayShips.get(i);
                        paint.setAlpha(i*5);
                        s.update();
                        int[] tmp = s.draw();
                        canvas.drawPoint(tmp[0],tmp[1], paint);
                        canvas.drawPoint(tmp[0]-2,tmp[0]+2, paint);
                        canvas.drawPoint(tmp[0]+2,tmp[1]-1, paint);
                        canvas.drawPoint(tmp[0]+1,tmp[1], paint);
                        //Log.w("", String.valueOf(cons));

                    }



                    int strokeWidth = 2;
                    Path cons = new Path();
                    paint.setStrokeWidth(strokeWidth);
                    paint.setStyle(Paint.Style.STROKE);
                    mShadow.setStrokeWidth(strokeWidth);
                    mShadow.setStyle(Paint.Style.STROKE);
                    paint.setColor(Color.LTGRAY);
                    cons.moveTo(mWidth / 2, (float) (mHeight / 2));
                    cons.lineTo(mWidth / 2, mHeight - (mHeight/20));
                    cons.moveTo(mWidth, mHeight - (mHeight/20));
                    cons.lineTo(0, mHeight - (mHeight/20));


                    canvas.drawPath(cons, paint);

                    paint.setStrokeCap(Paint.Cap.ROUND);
                    paint.setStyle(Paint.Style.FILL_AND_STROKE);
                    canvas.drawCircle(mWidth / 2, mHeight / 2, 50, scanning == true ? paint : p1);
                    paint.setColor(Color.DKGRAY);
                    paint.setStrokeWidth(7);
                    canvas.drawCircle(mWidth / 2, mHeight - (mHeight/20), 8, paint);



                    paint.setStyle(Paint.Style.FILL);
                    float alpha = 355;
                    for (int i = 0; i < 45; i++) {
                        cnt++;
                        cnt %= 4720;
                        if (cnt % 4== 1 && cnt < 79) {
                            p1.setAlpha(0);
                        } else {
                            p1.setAlpha((int) (alpha / (i + 1)));
                        }

                        canvas.save();
                        if(i<16) {
                            canvas.rotate(45 - (i * 5));
                        }else{
                            canvas.rotate(45 + (i));
                        }
                        canvas.translate(mWidth/2, 0);
                        canvas.scale((float)0.5,(float)0.1);
                        canvas.drawRect(mWidth + (i * 4), (i * 4), mWidth / 23 + (i * 4), mHeight / 2 + (i * 4), p1);

                        canvas.restore();
                    }


                    paint.setStyle(Paint.Style.FILL);
                    alpha = 0;
                    for (int i = 0; i < 20; i++) {
                        cnt++;
                        cnt %= 4720;
                        if (cnt % 4== 1 && cnt < 79) {
                            p1.setAlpha(0);
                        } else {
                            p1.setAlpha((int) (alpha * (i * 5)));
                        }

                        canvas.save();
                        if(i<16) {
                            canvas.rotate(45 - (i * 3));
                        }else{
                            canvas.rotate(45 - (i));
                        }
                        canvas.translate(mWidth/2, 0);
                        canvas.scale((float)0.2,(float)0.1);
                        canvas.drawRect(mWidth + (i * 4), (i * 4), mWidth / 23 + (i * 4), mHeight / 2 + (i * 4), p1);

                        canvas.restore();
                    }



                } finally {
                    view.unlockCanvasAndPost(canvas);
                }




                if (clr < 253) clr += 1;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        getActivity().getWindow().getDecorView().setBackgroundColor(Color.rgb((int)clr, (int)clr, (int)clr));

                    }
                });



                try {
                    Thread.sleep(15);
                } catch (InterruptedException e) {

                }
            }
        }

        public void stopRendering() {
            interrupt();
            mRunning = false;
        }

    }


    public class Ships  {

        private int sx, sy, ex, ey;
        private boolean sxToRight, syToBottom;
        private boolean exToRight, eyToBottom;
        int strokeWidth = 5;
        Paint p1 = new Paint();
        int s;

        Path path = new Path();
        int[] inter = new int[2];
        public Ships() {

            this.p1.setColor(0xff63A088);
            this.p1.setStrokeWidth(strokeWidth);
            this.p1.setStyle(Paint.Style.STROKE);
            this.sx = (int) (Math.random() * mWidth);
            this.sy = (int) (Math.random() * mHeight);
            this.ex = (int) (Math.random() * mWidth);
            this.ey = (int) (Math.random() * mHeight);
            s = (int) (Math.random()*7);
        }

        public int[] draw(){

            return  inter;
        }

        public void update() {



            if (this.exToRight) {
                this.ex += 54*s;
                if (ex >= mWidth * 2) {
                    this.exToRight = false;
                }
            } else {
                this.ex -= 34*s;
                if (this.ex < -mWidth) {
                    this.exToRight = true;
                }
            }

            if (this.eyToBottom) {
                this.ey+=120*s;
                if (ey >= mWidth * 2) {
                    this.eyToBottom = false;
                }
            } else {
                this.ey-=54*s;
                if (this.ey < -mWidth) {
                    this.eyToBottom = true;
                }
            }



            inter[0] = ex;
            inter[1] = ey;
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
            } catch (Exception e) {

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




    @Override
    public void onDestroy() {
        super.onDestroy();


    }


    @Override
    public void onStop() {
        super.onStop();


    }


}
