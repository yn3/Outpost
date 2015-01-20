package com.outpost;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;

public class DataHandler {

    SQLiteDatabase db;
    DataBaseHelper dbHelp;
    Context context;

    public static final String NAME = "name";
    public static final String MAC = "mac";
    public static final String USER = "user";
    public static final String TIME = "timestamp";
    public static final String DATE = "date";
    public static final String DATA_BASE_NAME = "aura.db";
    public static final String TABLE_NAME = "aura";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_CREATE = "create table aura (_id INTEGER PRIMARY KEY,name text not null,mac text not null,user text, is_aura, timestamp text, date text, UNIQUE(MAC) ON CONFLICT IGNORE);";

    public DataHandler(Context context) {

        this.context = context;
        dbHelp = new DataBaseHelper(context);

    }

    public DataHandler open() {

        db = dbHelp.getWritableDatabase();
        return this;

    }

    public long insert(String name, String mac, int is_aura,
                       String timestamp, String date) {
        ContentValues content = new ContentValues();

        content.put(NAME, name);
        content.put(MAC, mac);
        content.put(USER,"");
        content.put("is_aura", is_aura);
        content.put(TIME, timestamp);
        content.put(DATE, date);


        return db.insert(TABLE_NAME, null, content);

    }




    public Cursor grab() {

        return db.query(TABLE_NAME, new String[]{"rowid _id", NAME, TIME,
                DATE}, null, null, null, null, null);

    }

    public String[] grabDetails(int position) {

        Cursor cursor = db.query(TABLE_NAME, new String[]{"rowid _id", NAME, MAC, "is_aura", TIME, DATE}, null, null, null, null, null);
        cursor.moveToPosition(position);
        String[] detail = {cursor.getString(0), cursor.getString(1),cursor.getString(2),cursor.getString(3)};

        return detail;

    }


    public long CountRows() {

        return DatabaseUtils.queryNumEntries(db, TABLE_NAME);

    }

    public void close() {

        dbHelp.close();

    }

    private static class DataBaseHelper extends SQLiteOpenHelper {

        public DataBaseHelper(Context ctx) {

            super(ctx, DATA_BASE_NAME, null, DATABASE_VERSION);

        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            try {

                db.execSQL(TABLE_CREATE);

            } catch (SQLException e) {

                e.printStackTrace();

            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            db.execSQL("DROP TABLE IF EXISTS aura ");
            onCreate(db);

        }
    }

}
