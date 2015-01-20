package com.outpost;

import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import java.util.Random;

//import c.Eins.menu;

public class f_DataCenter extends Fragment  {

    ListView list;
    DataHandler handler;
    TextureView view;
    private RenderThread mThread;
    private int mWidth;
    private int mHeight;
    @SuppressWarnings("deprecation")

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.f_datacenter, container, false);

        view = (TextureView)v.findViewById(R.id.texture_datacenter);

        view.setSurfaceTextureListener(new CanvasListener());
        view.setOpaque(false);
        list = (ListView) v.findViewById(R.id.listView1);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

         /*
        final SoundPool sp = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        final int sound1 = sp.load(DB.this, R.raw.zip, 1);
        final int sound2 = sp.load(DB.this, R.raw.zip2, 1);
        final int sound3 = sp.load(DB.this, R.raw.zip3, 1);
        final int sound4 = sp.load(DB.this, R.raw.zip4, 1);
        final int[] sounds = {sound1, sound2, sound3, sound4};
        */
        handler = new DataHandler(getActivity());
        handler.open();

        Cursor grab = handler.grab();

        String[] wifies = new String[]{"_id", DataHandler.NAME,
                DataHandler.TIME, DataHandler.DATE};


        int[] list_entry = {R.id.id_txt, R.id.ssid, R.id.timestamp, R.id.date};
        SimpleCursorAdapter ada = new SimpleCursorAdapter(getActivity(),
                R.layout.list_db, grab, wifies, list_entry) {

            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                View i_prot = (View) view.findViewById(R.id.recView);

                String[] details = handler.grabDetails(position);

                String aura = details[3];

                int alpha = Integer.parseInt(aura)<1? 126 : 256;

                i_prot.setBackgroundColor(Color.argb(alpha, alpha, alpha, alpha));

                return view;
            }


        };


        list.setAdapter(ada);


        list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Random num = new Random();
                int rv = num.nextInt(4);
                //sp.play(sounds[rv], 1, 1, 0, 0, 1);

                String[] details = handler.grabDetails(position);
                //Intent intent = new Intent(getActivity(),
                //      DBDetails.class);
                //intent.putExtra("passage", details);
                //startActivity(intent);

            }

        });


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
