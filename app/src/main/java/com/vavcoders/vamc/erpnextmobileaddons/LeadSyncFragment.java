package com.vavcoders.vamc.erpnextmobileaddons;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.vavcoders.vamc.helper.DatabaseHelper;
import com.vavcoders.vamc.model.Settings;

import static com.vavcoders.vamc.model.Settings.LEAD_SYNC;

/**
 * Created by vamc on 1/2/18.
 */

public class LeadSyncFragment extends Fragment {
    View myView;
    Button btn_sync_toggle;
    DatabaseHelper db;
    String updated_value;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.activity_home, container, false);
        btn_sync_toggle = myView.findViewById(R.id.btn_sync_toggle);
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

        return myView;
    }
}
