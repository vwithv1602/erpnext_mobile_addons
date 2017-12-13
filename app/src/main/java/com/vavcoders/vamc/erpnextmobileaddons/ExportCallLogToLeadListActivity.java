package com.vavcoders.vamc.erpnextmobileaddons;

import android.Manifest;
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

import org.w3c.dom.Text;

import java.util.Date;

import static com.vavcoders.vamc.erpnextmobileaddons.GlobalVariables.menu;

public class ExportCallLogToLeadListActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_call_log_to_lead_list);

        /*page elements*/
        TextView tv_exportText = (TextView) findViewById(R.id.exportText);
        Button btn_exportBtn = (Button) findViewById(R.id.exportBtn);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CALL_LOG}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
            tv_exportText.setText("Allow us to read your contacts to export.");
            btn_exportBtn.setText("Allow ?");
//
        } else {
            // Android version is lesser than 6.0 or the permission is already granted.
            int incomingCallCount = getCallDetails();
            tv_exportText.setText("This will create "+incomingCallCount+" leads (of your INCOMING calls) in ERP.");
            btn_exportBtn.setText("Proceed ?");

//            Toast.makeText(getApplicationContext(),"Call method to post getCallDetails() result to api", Toast.LENGTH_SHORT).show();
        }

    }
    private int getCallDetails() {
        StringBuffer sbIncoming = new StringBuffer();
        StringBuffer sbMissed = new StringBuffer();
        Cursor managedCursor = managedQuery(CallLog.Calls.CONTENT_URI, null, null, null, null);
        int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
        sbIncoming.append("Incoming Call Log :");
        sbMissed.append("Missed Call Log :");
        int incomingCallCount = 0;
        int missedCallCount = 0;
        while (managedCursor.moveToNext()) {
            String phNumber = managedCursor.getString(number);
            String callType = managedCursor.getString(type);
            String callDate = managedCursor.getString(date);
            Date callDayTime = new Date(Long.valueOf(callDate));

            String callDuration = managedCursor.getString(duration);
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
            //callDayTime = (Date) android.text.format.DateFormat.format("yyyy-MM-dd hh:mm:ss a", new Date(callDate));
            if(dir == "INCOMING") {
                incomingCallCount++;
                sbIncoming.append("\nPhone Number: " + phNumber + "\nCall Date:--- " + callDayTime);
            }
            else if(dir == "MISSED"){
                missedCallCount++;
                sbMissed.append("\nPhone Number: " + phNumber + "\nCall Date:--- " + callDayTime);
            }
        }
        //managedCursor.close();
//        textViewIncoming.setText(sbIncoming);
//        textViewMissed.setText(sbMissed);
        return incomingCallCount;
    }
}
