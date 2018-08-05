package com.vavcoders.vamc.helper;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
// import android.util.Log;
import android.util.Log;
import android.widget.Toast;

import com.vavcoders.vamc.model.Auth;
import com.vavcoders.vamc.model.LeadsQueue;
import com.vavcoders.vamc.model.Settings;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.vavcoders.vamc.model.Auth.*;
import static com.vavcoders.vamc.model.LeadsQueue.*;
import static com.vavcoders.vamc.model.Settings.CREATE_TABLE_SETTINGS;
import static com.vavcoders.vamc.model.Settings.INSERT_DEFAULT_SETTINGS;
import static com.vavcoders.vamc.model.Settings.KEY_CONFIG;
import static com.vavcoders.vamc.model.Settings.KEY_CONFIG_VALUE;
import static com.vavcoders.vamc.model.Settings.TABLE_SETTINGS;

/**
 * Created by vamc on 12/20/17.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    // Database Name
    private static final String DATABASE_NAME = "vav_auth";
    private static final String TAG = "VamCLog";
    // Database Version
    private static final int DATABASE_VERSION = 1;
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
        // creating required tables
        db.execSQL(CREATE_TABLE_LOGIN);
        db.execSQL(CREATE_TABLE_LEADS_QUEUE);
        db.execSQL(CREATE_TABLE_SETTINGS);
        db.execSQL(INSERT_DEFAULT_SETTINGS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGIN);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);

        // create new tables
        onCreate(db);
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

    public void storeLoginDetails(Auth loginObj) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_UNAME,loginObj.getUname());
        values.put(KEY_URL,loginObj.getUrl());
        values.put(KEY_FULLNAME,loginObj.getFullname());
        values.put(KEY_IS_LOGGED_IN,loginObj.getIs_logged_in());
        //insert row
        long login_id = db.insert(TABLE_LOGIN, null, values);

        db.delete(TABLE_LOGIN, Auth.KEY_ID + " <> ?",
                new String[] { String.valueOf(login_id) });

    }
    public void removeLoginDetails(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LOGIN, "", new String[] {});
    }
    public Settings getAllSettings(){
        String selectQuery = "SELECT config,config_value FROM " + TABLE_SETTINGS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        Settings configurations = new Settings();
        Log.d(TAG,">>>>>");
        if (c.moveToFirst()) {
            Log.d(TAG,c.getString(c.getColumnIndex(KEY_CONFIG)));
            Log.d(TAG,c.getString(c.getColumnIndex(KEY_CONFIG_VALUE)));
            configurations.setConfig(c.getString(c.getColumnIndex(KEY_CONFIG)));
            configurations.setConfig_value(c.getString(c.getColumnIndex(KEY_CONFIG_VALUE)));
        }
        Log.d(TAG,"<<<<<");
        return configurations;
    }
    public String getConfigValue(String config){
        String selectQuery = "SELECT config_value FROM " + TABLE_SETTINGS+" where config='"+config+"'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        String configValue = "";
        if (c.moveToFirst()) {
            configValue = c.getString(c.getColumnIndex(KEY_CONFIG_VALUE));
        }

        return configValue;
    }
    public String updateSettings(Settings settingsObj){
        String updateQuery = "UPDATE "+TABLE_SETTINGS+" SET "+KEY_CONFIG_VALUE+"='"+settingsObj.getConfig_value()+"' WHERE "+KEY_CONFIG+"='"+settingsObj.getConfig()+"'";
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(updateQuery);
        return updateQuery;

    }
    public void storeLeadsQueue(JSONObject obj,String user){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_CALLLOG, obj.toString());
        values.put(KEY_CREATED_BY, user);
        //insert row
        db.insert(TABLE_LEADS_QUEUE, null, values);
    }
    public List<String> getAllLeadsInQueue(){
        List<String> leadList = new ArrayList<>();
        LeadsQueue leadsQueue = new LeadsQueue();
        String selectQuery = "SELECT calllog, created_by FROM " + TABLE_LEADS_QUEUE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor  cursor = db.rawQuery(selectQuery,null);

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                String calllog = cursor.getString(cursor.getColumnIndex("calllog"));
                String user = cursor.getString(cursor.getColumnIndex("created_by"));
                leadsQueue.setCalllog(calllog);
                leadsQueue.setCreated_by(user);
                leadList.add(calllog);
                cursor.moveToNext();
            }
        }
        return leadList;
    }
    public void deleteLeadsQueue(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LEADS_QUEUE, "", new String[] {});
    }
}
