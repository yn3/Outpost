package com.outpost;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;

import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;


/**
 * Created by andre on 14.01.2015.
 */
public class f_Storage extends Fragment {
    TextureView view;
    TextView in,out;
    private RenderThread mThread;
    private int mWidth;
    private int mHeight;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.f_storage, container, false);
        view = (TextureView)v.findViewById(R.id.texture_storage);

        view.setSurfaceTextureListener(new CanvasListener());
        view.setOpaque(false);
        in = (TextView)v.findViewById(R.id.in_files);
       out = (TextView)v.findViewById(R.id.out_files);
        return v;
    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        File f_in = new File(Environment.getExternalStorageDirectory()+"/OutpostShare/in/");
        File[] files = f_in.listFiles();
        String inlist = "";
        for (int i = 0; i< files.length;i++) {

                inlist += files[i].toString();

        }
        in.setText(inlist);

        File f_out = new File(Environment.getExternalStorageDirectory()+"/OutpostShare/out/");
        File[] files_b = f_out.listFiles();
        String outlist = "";
        for (int i = 0; i< files_b.length;i++) {

            outlist += files_b[i].toString();

        }
        out.setText(outlist);



        //ara = new ArrayAdapter<String>(getActivity(),  android.R.layout.simple_list_item_1, arrayList);


    }


    private class RenderThread extends Thread {
        private volatile boolean mRunning = true;
        private int sx, sy, ex, ey;
        private boolean sxToRight, syToBottom;
        private boolean exToRight, eyToBottom;

        @Override
        public void run() {
            Paint paint = new Paint();
            paint.setColor(0xff00ff00);
            paint.setColor(Color.WHITE);

            sx = (int) (Math.random() * mWidth);
            sy = (int) (Math.random() * mHeight);
            ex = (int) (Math.random() * mWidth);
            ey = (int) (Math.random() * mHeight);

            while (mRunning && !Thread.interrupted()) {
                final Canvas canvas = view.lockCanvas(null);
                try {
                    canvas.drawColor(0x00000000, PorterDuff.Mode.CLEAR);


                    int strokeWidth = 5;
                    paint.setStrokeWidth(strokeWidth);
                    paint.setStyle(Paint.Style.STROKE);

                    Path path = new Path();
                    path.moveTo(sx, sy);
                    path.lineTo(ex, ey);

                    canvas.drawPath(path, paint);

                } finally {
                    view.unlockCanvasAndPost(canvas);
                }

                if (sxToRight) {
                    sx += 3;
                    if (sx >= mWidth) {
                        sxToRight = false;
                    }
                } else {
                    sx -= 3;
                    if (sx < 0) {
                        sxToRight = true;
                    }
                }

                if (syToBottom) {
                    sy += 3;
                    if (sy >= mHeight) {
                        syToBottom = false;
                    }
                } else {
                    sy -= 3;
                    if (sy < 0) {
                        syToBottom = true;
                    }
                }

                if (exToRight) {
                    ex += 3;
                    if (ex >= mWidth) {
                        exToRight = false;
                    }
                } else {
                    ex -= 3;
                    if (ex < 0) {
                        exToRight = true;
                    }
                }

                if (eyToBottom) {
                    ey++;
                    if (ey >= mHeight) {
                        eyToBottom = false;
                    }
                } else {
                    ey--;
                    if (ey < 0) {
                        eyToBottom = true;
                    }
                }

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
}
