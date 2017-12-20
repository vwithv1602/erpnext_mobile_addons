package com.vavcoders.vamc.helper;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.vavcoders.vamc.model.Auth;

import static com.vavcoders.vamc.model.Auth.*;

/**
 * Created by vamc on 12/20/17.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    // Database Name
    private static final String DATABASE_NAME = "vav_auth";
    // Database Version
    private static final int DATABASE_VERSION = 1;
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public Auth getLoginProfile() {
        String selectQuery = "SELECT uname,fullname,url,is_logged_in FROM " + TABLE_LOGIN + " LIMIT 1";
        Boolean is_logged_in = false;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        Auth loginProfile = new Auth();
        if (c.moveToFirst()) {
            is_logged_in = c.getInt(c.getColumnIndex(KEY_IS_LOGGED_IN)) == 1;

            loginProfile.setUname(c.getString(c.getColumnIndex(KEY_UNAME)));
            loginProfile.setFullname(c.getString(c.getColumnIndex(KEY_FULLNAME)));
            loginProfile.setUrl(c.getString(c.getColumnIndex(KEY_URL)));
            loginProfile.setIs_logged_in(is_logged_in);
        }

        return loginProfile;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // creating required tables
        db.execSQL(CREATE_TABLE_LOGIN);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGIN);

        // create new tables
        onCreate(db);
    }

    public void storeLoginDetails(Auth loginObj) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_UNAME,loginObj.getUname());
        values.put(KEY_URL,loginObj.getUrl());
        values.put(KEY_FULLNAME,loginObj.getFullname());
        values.put(KEY_IS_LOGGED_IN,loginObj.getIs_logged_in());
        //insert row
        long login_id = db.insert(TABLE_LOGIN, null, values);

        db.delete(TABLE_LOGIN, KEY_ID + " <> ?",
                new String[] { String.valueOf(login_id) });

    }
}
