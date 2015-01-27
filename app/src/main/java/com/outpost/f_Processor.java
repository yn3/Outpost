package com.outpost;

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
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by andre on 14.01.2015.
 */
public class f_Processor extends Fragment {

    TextureView view;
    private RenderThread mThread;
    private int mWidth;
    private int mHeight;
    ImageView image;
    int intArray[],intArray_remote[];
    public boolean lock = true;
    String remoteURL = "";
    BitmapFactory.Options options;
    Bitmap bmp,remoteBmp;
    double cnt;
    String tmpPic = "";
    double setmode = 0;
    Transfer transfer;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.f_processor, container, false);
        //list = (ListView) v.findViewById(R.id.listView1);

        view = (TextureView)v.findViewById(R.id.texture_processor);

        view.setSurfaceTextureListener(new CanvasListener());
        view.setOpaque(false);
        ////////////////// MISSING DECODE ETC

        image = (ImageView)v.findViewById(R.id.processor_out);
        options = new BitmapFactory.Options();
        options.inMutable = true;
        transfer = new Transfer(getActivity());
        bmp =BitmapFactory.decodeFile( Environment.getExternalStorageDirectory() + "/OutpostShare/processor/merge.png",options);
        intArray = new int[bmp.getWidth()*bmp.getHeight()];
        bmp.getPixels(intArray, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());


        return v;
    }



    public void setConnector(ArrayList arrayList){

        if(arrayList.size()>0 && arrayList.get(0).toString().length()>1) {

            if(!tmpPic.equals(String.valueOf(arrayList.get(0)))) {
                remoteBmp = BitmapFactory.decodeFile((String) arrayList.get(0), options);
                intArray_remote = new int[remoteBmp.getWidth() * remoteBmp.getHeight()];
                remoteBmp.getPixels(intArray_remote, 0, remoteBmp.getWidth(), 0, 0, remoteBmp.getWidth(), remoteBmp.getHeight());
                lock = false;
                cnt = Math.random() * intArray.length;
                setmode = (int) (Math.random() *4.9);

                Log.w("trying to save", String.valueOf(setmode));

            }
            tmpPic = String.valueOf(arrayList.get(0));
        } else{

            lock = true;
            Log.w("trying to save", "LOCKSTATE OFF");
            tmpPic = String.valueOf(arrayList.get(0));
        }
    }






    public void saveBMP(Bitmap bit) throws IOException {

        OutputStream fOut = null;
        File file = new File(Environment.getExternalStorageDirectory() + "/OutpostShare/processor/merge.png");
        if(file.exists())file.delete();
        fOut = new FileOutputStream(file);
        bit.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        fOut.flush();
        fOut.close();

    }


    public class RenderThread extends Thread {
        private volatile boolean mRunning = true;
        private int sx, sy, ex, ey;
        private boolean sxToRight, syToBottom;
        private boolean exToRight, eyToBottom;
        int timer = 0;
        int bx,by;
        public RenderThread(){


            bx = bmp.getWidth();
            by = bmp.getHeight();
            cnt = Math.random()*intArray.length;
        }

        @Override
        public void run() {
            Paint paint = new Paint();
            paint.setColor(0xff00ff00);
            paint.setColor(Color.LTGRAY);

            sx = (int) (Math.random() * mWidth);
            sy = (int) (Math.random() * mHeight);
            ex = (int) (Math.random() * mWidth);
            ey = (int) (Math.random() * mHeight);

            int tmp = (int) (cnt*bmp.getWidth()+bmp.getHeight());
            Path cons = new Path();

            int strokeWidth = 2;
            Bitmap bit;

            while (mRunning && !Thread.interrupted()) {
                final Canvas canvas = view.lockCanvas(null);
                try {
                    canvas.drawColor(0x00000000, PorterDuff.Mode.CLEAR);
                    //canvas.drawColor(Color.WHITE);
                    cnt++;
                    timer++;
                    timer%=10000;
                    cnt%=intArray.length-2;




                    if(!lock) {


                        switch((int)setmode) {
                            case 0:
                                intArray[((int) cnt)] = intArray_remote[((int) cnt)];

                                break;
                            case 1:
                                if(cnt%500==1){
                                    cnt = Math.random()*intArray.length;

                                }
                                intArray[((int) cnt)] = intArray_remote[((int) cnt)];

                                break;
                            case 2:
                                intArray[((int) cnt)] = intArray_remote[intArray_remote.length-((int) cnt)];

                                break;

                            case 3:
                                tmp = (int) (cnt*bmp.getWidth()+bmp.getHeight());
                                if(tmp>intArray.length-1){
                                    cnt = Math.random()*bmp.getWidth();


                                }else{
                                    intArray[tmp] =  intArray_remote[tmp];

                                }
                                break;
                            case 4:
                                if(cnt%500==1){
                                    cnt = Math.random()*bmp.getWidth();

                                }
                                tmp = (int) (cnt*bmp.getWidth()+bmp.getHeight());
                                if(tmp>intArray.length-1){
                                    cnt = Math.random()*bmp.getWidth();


                                }else{
                                    intArray[tmp] =  intArray_remote[tmp];
                                }



                                break;


                        }
                        /*
                        paint.setStrokeWidth(strokeWidth);
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setColor(Color.LTGRAY);
                        paint.setAlpha(255);
                        dire.moveTo(mWidth, (mHeight / 20));
                        int x = (int) ((cnt*bmp.getWidth()+bmp.getHeight())%bmp.getWidth());
                        int y = (int) ((cnt*bmp.getWidth()+bmp.getHeight())%bmp.getHeight());
                        dire.lineTo(x,y);
                        canvas.drawPath(dire, paint);

                    */

                    }

                    bit = Bitmap.createBitmap(intArray, bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
                    canvas.save();
                    canvas.translate(0, -mHeight / 11);
                    canvas.drawBitmap(bit, new Rect(0, 0, bx, by), new Rect(mWidth / 6, mHeight / 6, mWidth - mWidth / 6, mHeight - mHeight / 4), paint);
                    paint.setStrokeWidth(10);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setAlpha(120);
                    canvas.drawRect(new Rect(mWidth / 6, mHeight / 6, mWidth - mWidth / 6, mHeight - mHeight / 4), paint);
                    paint.setAlpha(255);
                    paint.setStrokeWidth(3);

                    canvas.drawRect( new Rect(mWidth / 6, mHeight / 6, mWidth - mWidth / 6, mHeight - mHeight / 4), paint);
                    canvas.restore();


                    paint.setStrokeWidth(strokeWidth);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setColor(Color.LTGRAY);
                    paint.setAlpha(255);
                    cons.moveTo(mWidth, (mHeight / 20));
                    cons.lineTo(mWidth - (mWidth / 20), (mHeight / 20));
                    cons.lineTo(mWidth - (mWidth / 20), mHeight - (mHeight / 20));
                    cons.lineTo(mWidth/2, mHeight- (mHeight/20));
                    cons.lineTo(mWidth/2, mHeight- (mHeight/3)-10);
                    canvas.drawPath(cons, paint);

                    paint.setColor(Color.DKGRAY);
                    paint.setStyle(Paint.Style.FILL_AND_STROKE);
                    canvas.drawCircle(mWidth - (mWidth/20), (mHeight/20), 6, paint);
                    canvas.drawCircle(mWidth - (mWidth/20), mHeight- (mHeight/20), 5, paint);
                    canvas.drawCircle(mWidth/2, mHeight- (mHeight/20), 4, paint);


                    if (timer % 1000 == 1 && !lock) {

                        saveBMP(bit);

                    }


                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    view.unlockCanvasAndPost(canvas);
                }



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
