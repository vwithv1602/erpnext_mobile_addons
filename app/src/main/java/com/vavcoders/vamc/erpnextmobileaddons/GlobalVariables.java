package com.vavcoders.vamc.erpnextmobileaddons;

import android.content.ContentValues;
import android.content.Context;
// import android.util.Log;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;
import com.vavcoders.vamc.helper.DatabaseHelper;
import com.vavcoders.vamc.model.Auth;
import com.vavcoders.vamc.model.LeadsQueue;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by vamc on 12/13/17.
 */

public class GlobalVariables extends DatabaseHelper{
    /* Define menu options here*/
    public static final String[][] menu = new String[][]{
            {"ExportCallLogToLeadListActivity","Export Call log to lead list"},
            {"ManifestActivity","Manifest"}
    };

    private static final String TAG = "VamCLog";
    public String URL;
    public String LOGGED_IN_USER;
    public Context context;
    public GlobalVariables(Context context) {
        super(context);
        this.context = context;
        DatabaseHelper db = new DatabaseHelper(context);
        Auth loginProfile = db.getLoginProfile();
        URL = loginProfile.getUrl();
        LOGGED_IN_USER = loginProfile.getUname();
    }

    public void processQueueIfInternetAvailable() {
            new Thread(new Runnable(){

                @Override
                public void run() {
                    InetAddress ipAddr = null;
                    try {
                        ipAddr = InetAddress.getByName("google.com");
                        if(!ipAddr.equals("")){
                            // Internet available
                            processLeadQueue();
                        }
                    } catch (UnknownHostException e) {
//                        Log.d("GlobalVariables","In exception "+ e);
                    }
                }

            }).start();
    }

    public void processLeadQueue(){
        // get all leads from lead queue
        List<String> leadList = this.getAllLeadsInQueue();
        // foreach lead queue, upload and if return came from server, remove the record from leadqueue
        for (int i=0;i<leadList.size();i++) {
            SyncHttpClient client = new SyncHttpClient();
            RequestParams params = new RequestParams();
            params.put("calls", leadList.get(i).toString());
            params.put("user", this.LOGGED_IN_USER);
            String test = leadList.get(i).getClass().getName();
            try {
                client.post("http://"+this.URL+"/api/method/erpnext_mobile_addons.exportCallLog", params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, org.json.JSONObject response) {
                        try {
                            String lead_uploaded = response.getString("message");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, org.json.JSONObject response, Throwable throwable) {
                    }
                });
            } catch (Exception e) {
            }

        }

        this.deleteLeadsQueue();


    }
    public void exportCallLog(HashMap<String, Date> incomingCalls){

        JSONObject obj = new JSONObject(incomingCalls);
        /* >> Store in LeadsQueue*/
        this.storeLeadsQueue(obj,this.LOGGED_IN_USER);
        /* << Store in LeadsQueue*/
        this.processQueueIfInternetAvailable();

    }

    public void saveSettings(String selected_google_account){
        String generatedURL = "http://"+this.URL+"/api/method/erpnext_mobile_addons.save_settings";
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("username",this.LOGGED_IN_USER);
        params.put("selected_google_account",selected_google_account);
        try {
            client.post(generatedURL,params,new JsonHttpResponseHandler(){

                @Override
                public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, org.json.JSONObject response) {
                    try {
                        String save_result = response.getString("message");
                        ObjectMapper mapper = new ObjectMapper();
                        try {
                            String[] arr = mapper.readValue(save_result, String[].class);
                            Log.d(TAG,arr[0]);
                        } catch (IOException e) {
                            Log.d(TAG,"json array parsing exception");
                            e.printStackTrace();
                        }
                        Toast.makeText(context,save_result, Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(context,"Exception in parsing result", Toast.LENGTH_SHORT).show();
                    }
                }
                public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, org.json.JSONObject response, Throwable throwable) {
                    Toast.makeText(context,"Failure: ", Toast.LENGTH_SHORT).show();
                }
            });
        }catch (Exception e){
            Toast.makeText(context,"Exception occurred in requesting API", Toast.LENGTH_SHORT).show();
        }
    }


}
