package com.vavcoders.vamc.erpnextmobileaddons;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.vavcoders.vamc.erpnextmobileaddons.ExportCallLogToLeadListActivity.*;
import com.vavcoders.vamc.helper.DatabaseHelper;

import java.util.Date;
import java.util.HashMap;

import static com.vavcoders.vamc.model.Settings.LEAD_SYNC;

/**
 * Created by vamc on 12/16/17.
 */

public class PhoneStateReceiver extends BroadcastReceiver {
    DatabaseHelper db;
    @Override
    public void onReceive(Context context, Intent intent) {
        GlobalVariables gv = new GlobalVariables(context);
        try {
            System.out.println("Receiver start");
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            if(state.equals(TelephonyManager.EXTRA_STATE_RINGING)){
                // check settings
                db = new DatabaseHelper(context);
                String lead_sync_flag = db.getConfigValue(LEAD_SYNC);
                if(lead_sync_flag.equalsIgnoreCase("1")){
                    HashMap<String, Date> hmIncomingCalls = new HashMap<String, Date>();
                    hmIncomingCalls.put(incomingNumber, new Date());
                    gv.exportCallLog(hmIncomingCalls);
                    Toast.makeText(context,"Converting "+incomingNumber+" to lead",Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(context,"Lead sync is turned off ",Toast.LENGTH_LONG).show();
                }

            }
            if(state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)){
                Toast.makeText(context," Call received ",Toast.LENGTH_LONG).show();
            }
            if(state.equals(TelephonyManager.EXTRA_STATE_IDLE)){
                Toast.makeText(context," Call rejected ",Toast.LENGTH_LONG).show();
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
