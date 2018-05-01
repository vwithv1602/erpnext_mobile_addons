package com.vavcoders.vamc.erpnextmobileaddons;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.vavcoders.vamc.helper.DatabaseHelper;
import com.vavcoders.vamc.model.Settings;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import static com.vavcoders.vamc.model.Settings.LEAD_SYNC;

/**
 * Created by vamc on 1/2/18.
 */

public class LeadSyncFragment extends Fragment {
    View myView;
    Button btn_sync_toggle,btn_force_sync;
    DatabaseHelper db;
    String updated_value;
    EditText et_force_datetime;
    ProgressDialog progressDialog;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.activity_home, container, false);
        btn_sync_toggle = myView.findViewById(R.id.btn_sync_toggle);
        progressDialog = new ProgressDialog(getActivity());
        db = new DatabaseHelper(getActivity());
        String lead_sync_flag = db.getConfigValue(LEAD_SYNC);

        if(lead_sync_flag.equalsIgnoreCase("1")){
            btn_sync_toggle.setText("STOP SYNC");
            updated_value = "0";
        }else{
            btn_sync_toggle.setText("START SYNC");
            updated_value = "1";
        }
        btn_sync_toggle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Settings settingsObj = new Settings();
                settingsObj.setConfig(LEAD_SYNC);
                settingsObj.setConfig_value(updated_value);
                db.updateSettings(settingsObj);
                Fragment frg = null;
                frg = getFragmentManager().findFragmentByTag("tag_lead_sync_fragment");
                final FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.detach(frg);
                ft.attach(frg);
                ft.commit();
            }
        });
        Calendar calander = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        String currentDateTimeString = simpleDateFormat.format(calander.getTime());
        et_force_datetime = myView.findViewById(R.id.et_force_datetime);
        et_force_datetime.setText(currentDateTimeString.toString());
        btn_force_sync = myView.findViewById(R.id.btnForceSync);
        btn_force_sync.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                progressDialog.setMessage("Please wait...");
                progressDialog.show();
                // clear the database queue table
                DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
                dbHelper.deleteLeadsQueue();
                // upload all calls greater than the given date to lead list
                String forceDateTimeFrom = et_force_datetime.getText().toString();
                if(forceDateTimeFrom.isEmpty()){
                    Calendar calander = Calendar.getInstance();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String currentDateTimeString = simpleDateFormat.format(calander.getTime());
                    et_force_datetime.setText(currentDateTimeString.toString());
                }
                try {
                    Long millis = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(forceDateTimeFrom).getTime();
                    HashMap<String, Date> callsForForcingLeads = getCallDetails(millis);
                    GlobalVariables gv = new GlobalVariables(getActivity());
                    gv.exportCallLog(callsForForcingLeads);
                    progressDialog.setMessage("Force sync successful");
                    progressDialog.show();
                    new android.os.Handler().postDelayed(
                            new Runnable() {
                                public void run() {
                                    progressDialog.hide();
                                }
                            }, 1000);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
        });
        return myView;
    }
    private HashMap<String, Date> getCallDetails(Long greaterThan) {

        Cursor managedCursor = getActivity().managedQuery(CallLog.Calls.CONTENT_URI, null, null, null, null);
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
