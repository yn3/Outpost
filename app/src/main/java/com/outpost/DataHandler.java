package com.outpost;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.text.format.Time;

public class DataHandler {

    SQLiteDatabase db;
    DataBaseHelper dbHelp;
    Context context;

    public static final String NAME = "name";
    public static final String MAC = "mac";
    public static final String USER = "user";
    public static final String TIME = "timestamp";
    public static final String DATE = "date";
    public static final String URL = "url";
    public static final String DATA_BASE_NAME = "outpost.db";
    public static final String TABLE_NAME = "aura";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_CREATE = "create table aura (_id INTEGER PRIMARY KEY,name text not null,mac text not null,user text, is_outpost, timestamp text, date text, url text, UNIQUE(MAC) ON CONFLICT IGNORE);";

    public DataHandler(Context context) {

        this.context = context;
        dbHelp = new DataBaseHelper(context);

    }

    public DataHandler open() {

        db = dbHelp.getWritableDatabase();
        return this;

    }

    public long insert(String name, String mac, int is_outpost,
                       String timestamp, String date) {
        ContentValues content = new ContentValues();

        content.put(NAME, name);
        content.put(MAC, mac);
        content.put(USER, "");
        content.put("is_outpost", is_outpost);
        content.put(TIME, timestamp);
        content.put(DATE, date);
        content.put(URL, "");


        return db.insert(TABLE_NAME, null, content);

    }


    public Cursor grab() {

        return db.query(TABLE_NAME, new String[]{"rowid _id", NAME, TIME,
                DATE, URL}, null, null, null, null, null);

    }

    public String[] grabDetails(int position) {

        Cursor cursor = db.query(TABLE_NAME, new String[]{"rowid _id", NAME, MAC, "is_outpost", TIME, DATE}, null, null, null, null, null);
        cursor.moveToPosition(position);
        String[] detail = {cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3)};

        return detail;

    }


    public String grabURL(String mac) {

        String tmp = mac.replace("[", "").replace("]", "");


        String url = "";

        Cursor cursor = db.query(TABLE_NAME, new String[]{URL}, "mac=?", new String[]{tmp}, null, null, null);

        if (cursor.getCount() > 0) {

            cursor.moveToFirst();
            url = cursor.getString(0);
        }



        return url;
    }

    public Boolean checkTime(String mac) {

        String tmp = mac.replace("[", "").replace("]", "");


        String date = "";

        Cursor cursor = db.query(TABLE_NAME, new String[]{DATE}, "mac=?", new String[]{tmp}, null, null, null);

        if (cursor.getCount() > 0) {

            cursor.moveToFirst();
            date = cursor.getString(0);
        }

        Time time = new Time();
        time.clear(Time.getCurrentTimezone());
        time.setToNow();
        time.format("%d.%m.%Y");


        if(String.valueOf(time)==date){
            return true;
        }

        return false;
    }




    public long CountRows() {

        return DatabaseUtils.queryNumEntries(db, TABLE_NAME);

    }


    public void updateURL(String mac, String nick) {
        ContentValues cv = new ContentValues();
        String getMac = mac;
        String loc = Environment.getExternalStorageDirectory() + "/OutpostShare/processor/" + nick + ".png";
        cv.put("url", loc);
        String WHERE = String.format("%s='%s'", MAC, getMac);
        db.update(TABLE_NAME, cv, WHERE, null);

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

    public boolean deleteEntry(String mac)


    {
        return db.delete(DATA_BASE_NAME, "mac = ?", new String[] {mac}) > 0;
    }

}
