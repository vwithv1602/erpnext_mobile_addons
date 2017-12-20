package com.vavcoders.vamc.erpnextmobileaddons;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.CallLog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.vavcoders.vamc.commonrest.Make;


import static android.provider.ContactsContract.CommonDataKinds.Website.URL;
import static com.vavcoders.vamc.erpnextmobileaddons.GlobalVariables.menu;

public class ExportCallLogToLeadListActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    GlobalVariables gv = new GlobalVariables(getApplicationContext());
    public void exportCallLog(HashMap<String, Date> incomingCalls){
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        JSONObject obj = new JSONObject(incomingCalls);
        params.put("calls", obj);
        params.put("user", "Administrator");
        try {

            client.post("http://"+gv.URL+"/api/method/erpnext_mobile_addons.exportCallLog", params, new JsonHttpResponseHandler() {

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
//            Toast.makeText(getApplicationContext(), "Exception: ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_call_log_to_lead_list);

        /*page elements*/
        TextView tv_exportText = (TextView) findViewById(R.id.exportText);
        Button btn_exportBtn = (Button) findViewById(R.id.exportBtn);

        btn_exportBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // call getLog function to get all incoming & missed call numbers
                HashMap<String, Date> sb_incomingCalls = getCallDetails();
                // post to api to save in lead list
                exportCallLog(sb_incomingCalls);

            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CALL_LOG}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
            tv_exportText.setText("Allow us to read your contacts to export.");
            btn_exportBtn.setVisibility(View.INVISIBLE);


//
        } else {
            // Android version is lesser than 6.0 or the permission is already granted.
            HashMap<String, Date> incomingCalls = getCallDetails();
            /*StringTokenizer str = new StringTokenizer(incomingCalls.toString());
            String[] incomingCallsArray = incomingCalls.toString().split(",");
            int incomingCallCount = incomingCallsArray.length;*/
            int incomingCallCount = incomingCalls.size();
            tv_exportText.setText("'Sync All Now' will create " + incomingCallCount + " leads (of your INCOMING and MISSED calls) in ERP.");
            btn_exportBtn.setVisibility(View.VISIBLE);
            btn_exportBtn.setText("Sync All Now");
        }

    }

    private int reloadActivityAfter = 2000;

    private void reloadActivity(int reloadActivityAfter) {
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        finish();
                        startActivity(getIntent());
                    }
                },
                reloadActivityAfter);
    }

    public void onRequestPermissionsResult(int statusCode, String[] textContent, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Permission granted", Toast.LENGTH_SHORT).show();
            reloadActivity(600);
        } else {
            Toast.makeText(getApplicationContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            reloadActivity(reloadActivityAfter);
        }
    }

    private HashMap<String, Date> getCallDetails() {
        Cursor managedCursor = managedQuery(CallLog.Calls.CONTENT_URI, null, null, null, null);
        int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        HashMap<String, Date> hmIncomingCalls = new HashMap<String, Date>();
        while (managedCursor.moveToNext()) {
            String phNumber = managedCursor.getString(number);
            String callType = managedCursor.getString(type);
            String callDate = managedCursor.getString(date);
            Date callDayTime = new Date(Long.valueOf(callDate));
            String dir = null;
            int dircode = Integer.parseInt(callType);
            switch (dircode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    dir = "OUTGOING";
                    break;
                case CallLog.Calls.INCOMING_TYPE:
                    dir = "INCOMING";
                    break;
                case CallLog.Calls.MISSED_TYPE:
                    dir = "MISSED";
                    break;
            }
            if (dir == "INCOMING" || dir == "MISSED") {
                hmIncomingCalls.put(phNumber, callDayTime);
            }
        }
        return hmIncomingCalls;
    }
}
