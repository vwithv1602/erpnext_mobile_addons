package com.vavcoders.vamc.erpnextmobileaddons;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.vavcoders.vamc.erpnextmobileaddons.ExportCallLogToLeadListActivity.*;
import com.vavcoders.vamc.helper.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import static com.vavcoders.vamc.model.Settings.LEAD_SYNC;

/**
 * Created by vamc on 12/16/17.
 */

public class PhoneStateReceiver extends BroadcastReceiver {
    private static final String TAG = "VamCLog";
    DatabaseHelper db;

    @Override
    public void onReceive(Context context, Intent intent) {
        GlobalVariables gv = new GlobalVariables(context);
        try {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                // check settings
                db = new DatabaseHelper(context);
                String lead_sync_flag = db.getConfigValue(LEAD_SYNC);
                if (lead_sync_flag.equalsIgnoreCase("1")) {
                    HashMap<String, Date> hmIncomingCalls = new HashMap<String, Date>();
                    hmIncomingCalls.put(incomingNumber, new Date());
//                    Calendar calander = Calendar.getInstance();
//                    calander.add(Calendar.DATE,-2);
//                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
//                    String currentDateTimeString = simpleDateFormat.format(calander.getTime());
//                    Long millis = new SimpleDateFormat("yyyy-MM-dd").parse(currentDateTimeString).getTime();
//                    hmIncomingCalls = getCallDetails(millis, context);
                    gv.exportCallLog(hmIncomingCalls);
                    Toast.makeText(context, "Converting " + incomingNumber + " to lead", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "Lead sync is turned off ", Toast.LENGTH_LONG).show();
                }

            }
            if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                Toast.makeText(context, " Call received ", Toast.LENGTH_LONG).show();
            }
            if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                Toast.makeText(context, " Call rejected ", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HashMap<String, Date> getCallDetails(Long greaterThan, Context context) {

//        Cursor managedCursor = getActivity().managedQuery(CallLog.Calls.CONTENT_URI, null, null, null, null);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
        Cursor managedCursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
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
            if ((dir == "INCOMING" || dir == "MISSED") && Long.parseLong(callDate)>greaterThan) {
                hmIncomingCalls.put(phNumber, callDayTime);
            }
        }
        return hmIncomingCalls;
    }
}
