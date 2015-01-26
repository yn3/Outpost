package com.outpost;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by andre on 14.01.2015.
 */
public class f_Storage extends ListFragment {
    TextureView view;
    private RenderThread mThread;
    private int mWidth;
    private int mHeight;
    private List<String> item = null;
    private List<String> path = null;
    private String root = "";
    private TextView myPath;
    public static final String SETTINGS = "user_settings";
    SharedPreferences settings;
    ImageView image;
    TextView shareSize;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.f_storage, container, false);
        view = (TextureView) v.findViewById(R.id.texture_storage);
        view.setSurfaceTextureListener(new CanvasListener());
        view.setOpaque(false);

        myPath = (TextView) v.findViewById(R.id.path);
        root = Environment.getExternalStorageDirectory() + "/OutpostShare/";
        getDir(root);
        settings = getActivity().getSharedPreferences(SETTINGS, 0);
        shareSize = (TextView)v.findViewById(R.id.shareSize);

        image = (ImageView)v.findViewById(R.id.storage_id);
        final File loc = new File (Environment.getExternalStorageDirectory() + "/OutpostShare/id/id.png");

        if(loc.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(loc.getAbsolutePath());
            image.setImageBitmap(myBitmap);
        }
        long sum =0;
        sum += loc.length();
        String path = Environment.getExternalStorageDirectory().toString()+"/OutpostShare/share_out/";
        File f = new File(path);
        File file[] = f.listFiles();

        for (int i=0; i < file.length; i++)
        {
             sum += file[i].length();
        }
        String oo = getFileSize(sum);

        shareSize.setText(oo);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                new AlertDialog.Builder(getActivity())

                        .setIcon(R.drawable.ic_action_network_wifi)
                        .setTitle("Delete IMAGE?")
                        .setPositiveButton("Yes",

                                new DialogInterface.OnClickListener() {


                                    @Override

                                    public void onClick(DialogInterface dialog, int which) {

                                        File f = new File(Environment.getExternalStorageDirectory() + "/OutpostShare/id/" + "id.png");
                                        f.delete();
                                        Toast.makeText(getActivity(), "IMG Deleted!", Toast.LENGTH_LONG).show();

                                    }

                                }

                        ).setNegativeButton("NO", null).show();


            }
        });





        return v;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        //ara = new ArrayAdapter<String>(getActivity(),  android.R.layout.simple_list_item_1, arrayList);
    }

    private void getDir(String dirPath) {


        item = new ArrayList<String>();
        path = new ArrayList<String>();
        File f = new File(dirPath);
        File[] files = f.listFiles();

        if (!dirPath.equals(root)) {

            item.add(root);
            path.add(root);
            item.add("../");
            path.add(f.getParent());

        }

        for (int i = 0; i < files.length; i++) {

            File file = files[i];
            path.add(file.getPath());

            if (file.isDirectory())

                item.add(file.getName() + "/");

            else

                item.add(file.getName());

        }

        ArrayAdapter<String> fileList =

                new ArrayAdapter<String>(getActivity(), R.layout.list_storage, item);

        setListAdapter(fileList);

    }

    public static String getFileSize(long size) {
        if (size <= 0)
            return "0";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        final File file = new File(path.get(position));
        myPath.setText("Location: "+  file.getPath());
        if (file.isDirectory()) {
            getDir(path.get(position));

        } else {

            String fileArray[] = file.getName().split("\\.");
            String ext = fileArray[fileArray.length - 1];
            if(!file.getAbsolutePath().contains("/OutpostShare/")) {
                if (ext.contains("jpg") || ext.contains("png") || ext.contains("jpeg") || ext.contains("bmp")) {


                    new AlertDialog.Builder(getActivity())

                            .setIcon(R.drawable.ic_action_network_wifi)

                            .setTitle("Set as PROFILE PICTURE or as SHARE:")
                            .setNeutralButton("SHARE", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    setSharing(file);
                                }
                            })

                            .setPositiveButton("PROFILE",

                                    new DialogInterface.OnClickListener() {

                                        @Override

                                        public void onClick(DialogInterface dialog, int which) {


                                            Bitmap b = BitmapFactory.decodeFile(file.getAbsolutePath());
                                            int width = b.getWidth();
                                            int height = b.getHeight();

                                            if (width > height) {

                                                float ratio = (float) width / 360;
                                                width = 360;
                                                height = (int) ((int) height / ratio);

                                            } else if (height > width) {

                                                float ratio = (float) height / 640;
                                                height = 640;
                                                width = (int) ((int) width / ratio);

                                            } else {

                                                height = 640;
                                                width = 360;
                                            }
                                            Bitmap out = Bitmap.createScaledBitmap(b, width, height, true);
                                            String dir = Environment.getExternalStorageDirectory() + "/OutpostShare/id/" + "id.png";
                                            File id_file = new File(dir);
                                            if (id_file.exists()) id_file.delete();
                                            FileOutputStream fOut;
                                            try {
                                                fOut = new FileOutputStream(id_file);
                                                out.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                                                fOut.flush();
                                                fOut.close();
                                                b.recycle();
                                                out.recycle();

                                            } catch (Exception e) { // TODO

                                            } finally {
                                                Toast.makeText(getActivity(), "Image successfully set!", Toast.LENGTH_LONG).show();
                                            }

                                            InputStream mIn = null;
                                            OutputStream mOut = null;
                                            File copy = new File(Environment.getExternalStorageDirectory() + "/OutpostShare/processor/" + "merge.png");
                                            if (!copy.exists()) copy.delete();


                                                try {

                                                    mIn = new FileInputStream(Environment.getExternalStorageDirectory() + "/OutpostShare/id/" + "id.png");
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
                                                    Bitmap bmp = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/OutpostShare/id/" + "id.png");
                                                    image.setImageBitmap(bmp);
                                                }



                                        }

                                    }

                            ).setNegativeButton("Cancel", null).show();

                } else{

                    new AlertDialog.Builder(getActivity())
                            .setIcon(R.drawable.ic_action_network_wifi)
                            .setTitle("Set as SHARE:")
                            .setPositiveButton("SHARE",

                                   new DialogInterface.OnClickListener() {

                                        @Override

                                        public void onClick(DialogInterface dialog, int which) {

                                            setSharing(file);

                                        }

                                    }

                            ).setNegativeButton("Cancel", null).show();


                }
            } else {

                new AlertDialog.Builder(getActivity())

                        .setIcon(R.drawable.ic_action_network_wifi)
                        .setTitle("Delete File?")
                        .setPositiveButton("Yes",

                                new DialogInterface.OnClickListener() {


                                    @Override

                                    public void onClick(DialogInterface dialog, int which) {

                                        file.delete();
                                        Toast.makeText(getActivity(), "File Deleted!", Toast.LENGTH_LONG).show();

                                    }

                                }

                        ).setNegativeButton("NO", null).show();


            }

        }

    }


    private void setSharing(File file){


        InputStream mIn = null;
        OutputStream mOut = null;
        File copy = new File(Environment.getExternalStorageDirectory() + "/OutpostShare/share_out/" + file.getName());
        if (!copy.exists()) {
            try {

                mIn = new FileInputStream(file.getAbsolutePath());
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
                Toast.makeText(getActivity(), "aborted", Toast.LENGTH_LONG).show();
            } finally {
                Toast.makeText(getActivity(), "Created Share!", Toast.LENGTH_LONG).show();
            }
        }
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

            sx = (int) (Math.random() * mWidth);
            sy = (int) (Math.random() * mHeight);
            ex = (int) (Math.random() * mWidth);
            ey = (int) (Math.random() * mHeight);

            while (mRunning && !Thread.interrupted()) {
                final Canvas canvas = view.lockCanvas(null);
                try {
                    canvas.drawColor(0x00000000, PorterDuff.Mode.CLEAR);



                    int strokeWidth = 2;
                    Path cons = new Path();
                    paint.setStrokeWidth(strokeWidth);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setColor(Color.LTGRAY);
                    cons.moveTo(0, (mHeight/20));
                    cons.lineTo((mWidth/20), (mHeight/20));
                    cons.lineTo(mWidth/2, (mHeight/20));
                    cons.lineTo(mWidth/2, (float) (mHeight)-mHeight/8);


                    canvas.drawPath(cons, paint);


                    paint.setColor(Color.DKGRAY);
                    paint.setStyle(Paint.Style.FILL_AND_STROKE);

                    canvas.drawCircle(mWidth/2, (mHeight/20), 3, paint);
                    canvas.drawCircle(mWidth/2, mHeight/3, 2, paint);


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
