package com.vavcoders.vamc.erpnextmobileaddons;

import android.content.Context;
// import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.vavcoders.vamc.helper.DatabaseHelper;
import com.vavcoders.vamc.model.Auth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;

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
    public void exportCallLog(HashMap<String, Date> incomingCalls){
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JSONObject obj = new JSONObject(incomingCalls);
        params.put("calls", obj);
        params.put("user", this.LOGGED_IN_USER);
        try {

            client.post("http://"+this.URL+"/api/method/erpnext_mobile_addons.exportCallLog", params, new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, org.json.JSONObject response) {
                    try {
                        String login_check = response.getString("message");
//                        Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
//                        Toast.makeText(getApplicationContext(), "Exception", Toast.LENGTH_SHORT).show();

                    }

                }

                public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, org.json.JSONObject response, Throwable throwable) {

//                    Toast.makeText(getApplicationContext(), "error: ", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            // Log.e("Exception","E-GV-eCL-postapi");
        }
    }
}
