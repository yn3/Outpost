package com.outpost;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//import c.Eins.menu;

public class f_DataCenter extends Fragment  {

    ListView list;
    DataHandler handler;
    TextureView view;
    private RenderThread mThread;
    private int mWidth;
    private int mHeight;
    TextView  out1;
    @SuppressWarnings("deprecation")

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.f_datacenter, container, false);

        view = (TextureView)v.findViewById(R.id.texture_datacenter);

        view.setSurfaceTextureListener(new CanvasListener());
        view.setOpaque(false);
        list = (ListView) v.findViewById(R.id.dc_listView);
        out1 = (TextView) v.findViewById(R.id.out1);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        handler = new DataHandler(getActivity());
        handler.open();

        Cursor grab = handler.grab();

        String[] bts = new String[]{"_id", DataHandler.NAME,
                DataHandler.TIME, DataHandler.DATE,DataHandler.URL};


        int[] list_entry = {R.id.dc_id, R.id.dc_name, R.id.dc_timestamp, R.id.dc_date,R.id.dc_url};



        MySimpleCursorAdapter ada = new MySimpleCursorAdapter(getActivity(),
                R.layout.list_datacenter, grab, bts, list_entry) {

            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                View i_prot = view.findViewById(R.id.dc_recView);

                String[] details = handler.grabDetails(position);

                String aura = details[3];
                Paint p1 = new Paint();
                p1.setColor(0xff00A199);
                int alpha = Integer.parseInt(aura)<1? 100: 255;

                i_prot.setBackgroundColor(Color.argb(alpha, 99, 160,136));

                return view;
            }



        };



        list.setAdapter(ada);
        list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    final int position, long id) {
                new AlertDialog.Builder(getActivity())

                        .setIcon(R.drawable.ic_action_network_wifi)
                        .setTitle("Delete Entry?")
                        .setPositiveButton("Yes",

                                new DialogInterface.OnClickListener() {


                                    @Override

                                    public void onClick(DialogInterface dialog, int which) {

                                        handler.open();
                                             //boolean success = handler.deleteEntry(position);

                                        handler.close();


                                    }

                                }

                        ).setNegativeButton("NO", null).show();
            }

        });


    }

    public void dataUp(String[] data) {



        out1.setText(data[0]);

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
            paint.setColor(Color.LTGRAY);


            while (mRunning && !Thread.interrupted()) {
                final Canvas canvas = view.lockCanvas(null);
                try {
                    canvas.drawColor(0x00000000, PorterDuff.Mode.CLEAR);


                    int strokeWidth = 2;
                    Path cons = new Path();
                    paint.setStrokeWidth(strokeWidth);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setColor(Color.LTGRAY);
                    cons.moveTo(- (mWidth/20), mHeight- (mHeight/20));
                    cons.lineTo(mWidth - (mWidth/20), mHeight- (mHeight/20));
                    cons.lineTo(mWidth - (mWidth/20), mHeight/2);
                    cons.lineTo(mWidth - (mWidth/6), mHeight/2);
                    cons.moveTo(mWidth - (mWidth/20), mHeight/2);
                    cons.lineTo(mWidth - (mWidth/20), mHeight/20);
                    cons.lineTo(mWidth,mHeight/20);


                    canvas.drawPath(cons, paint);
                    paint.setColor(Color.DKGRAY);
                    paint.setStyle(Paint.Style.FILL_AND_STROKE);
                    canvas.drawCircle(mWidth - (mWidth/20), mHeight- (mHeight/20), 7, paint);
                    canvas.drawCircle(mWidth - (mWidth/20), mHeight/2, 6, paint);
                    canvas.drawCircle(mWidth - (mWidth/20), mHeight/20, 5, paint);


                    paint.setStyle(Paint.Style.FILL);
                    paint.setAlpha(12);
                    canvas.drawRect( new Rect((int) (mWidth / 8.2), mHeight / 12, mWidth - mWidth / 6, mHeight - mHeight / 4), paint);
                    paint.setAlpha(120);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(10);
                    canvas.drawRect(new Rect((int) (mWidth / 8.2), mHeight / 12, mWidth - mWidth / 6, mHeight - mHeight / 4), paint);
                    paint.setAlpha(255);
                    paint.setStrokeWidth(3);

                    canvas.drawRect( new Rect((int) (mWidth / 8.2), mHeight / 12, mWidth - mWidth / 6, mHeight - mHeight / 4), paint);
                    canvas.restore();


                } finally {
                    view.unlockCanvasAndPost(canvas);
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


    public class MySimpleCursorAdapter extends SimpleCursorAdapter {

        public MySimpleCursorAdapter(Context context, int layout, Cursor c,
                                     String[] from, int[] to) {
            super(context, layout, c, from, to);
        }

        @Override
        public void setViewImage(ImageView v, String id) {

            String path = id;
            Bitmap b = BitmapFactory.decodeFile(path);
            v.setImageBitmap(b);

        }

    }
}
