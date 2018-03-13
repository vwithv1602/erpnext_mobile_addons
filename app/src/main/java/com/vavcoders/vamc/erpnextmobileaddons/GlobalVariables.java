package com.vavcoders.vamc.erpnextmobileaddons;

import android.content.ContentValues;
import android.content.Context;
// import android.util.Log;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.vavcoders.vamc.helper.DatabaseHelper;
import com.vavcoders.vamc.model.Auth;
import com.vavcoders.vamc.model.LeadsQueue;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by vamc on 12/13/17.
 */

public class GlobalVariables extends DatabaseHelper{

    /* Define menu options here*/
    public static final String[][] menu = new String[][]{
            {"ExportCallLogToLeadListActivity","Export Call log to lead list"},
            {"ManifestActivity","Manifest"}
    };

    public String URL;
    public String LOGGED_IN_USER;
    public GlobalVariables(Context context) {
        super(context);
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
//                        Log.d("python","onFailure");
                    }
                });
            } catch (Exception e) {
//                Log.d("python","In Exception");
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


}
