package com.outpost;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by andre on 14.01.2015.
 */
public class f_CargoBay extends Fragment {
    TextureView view;
    private RenderThread mThread;
    private int mWidth;
    private int mHeight;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.f_cargobay, container, false);
        view = (TextureView) v.findViewById(R.id.texture_cargobay);

        view.setSurfaceTextureListener(new CanvasListener());
        view.setOpaque(false);

        return v;
    }


    private class RenderThread extends Thread {
        private volatile boolean mRunning = true;
        private int sx, sy, ex, ey;
        private boolean sxToRight, syToBottom;
        private boolean exToRight, eyToBottom;
        int cnt ;

        @Override
        public void run() {
            Paint paint = new Paint();
            paint.setColor(0xff00ff00);
            paint.setColor(Color.LTGRAY);

            sx = (int) (Math.random() * mWidth);
            sy = (int) (Math.random() * mHeight);
            ex = (int) (Math.random() * mWidth);
            ey = (int) (Math.random() * mHeight);
            cnt = 0;
            while (mRunning && !Thread.interrupted()) {
                final Canvas canvas = view.lockCanvas(null);
                try {
                    canvas.drawColor(0x00000000, PorterDuff.Mode.CLEAR);


                    int strokeWidth = 2;
                    Path cons = new Path();
                    paint.setStrokeWidth(strokeWidth);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setColor(Color.LTGRAY);
                    cons.moveTo(mWidth, mHeight - (mHeight/20));
                    cons.lineTo(mWidth - (mWidth/20), mHeight - (mHeight/20));
                    cons.lineTo(mWidth - (mWidth/20), (mHeight/20));
                    cons.lineTo(0, (mHeight/20));
                    //->*Math.cos(Math.toRadians(degrees));


                    canvas.drawPath(cons, paint);

                    paint.setColor(Color.DKGRAY);
                    paint.setStyle(Paint.Style.FILL_AND_STROKE);
                    canvas.drawCircle(mWidth - (mWidth/20), mHeight - (mHeight/20), 7, paint);
                    canvas.drawCircle(mWidth - (mWidth/20), (mHeight/20), 6, paint);
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
}
