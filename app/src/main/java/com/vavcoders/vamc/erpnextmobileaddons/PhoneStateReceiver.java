package com.vavcoders.vamc.erpnextmobileaddons;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.vavcoders.vamc.erpnextmobileaddons.ExportCallLogToLeadListActivity.*;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by vamc on 12/16/17.
 */

public class PhoneStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            System.out.println("Receiver start");
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            if(state.equals(TelephonyManager.EXTRA_STATE_RINGING)){
                HashMap<String, Date> hmIncomingCalls = new HashMap<String, Date>();
                hmIncomingCalls.put(incomingNumber, new Date());
                ExportCallLogToLeadListActivity expCallLogObj = new ExportCallLogToLeadListActivity();
                expCallLogObj.exportCallLog(hmIncomingCalls);
                Toast.makeText(context," Incoming call: "+incomingNumber,Toast.LENGTH_LONG).show();
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
