package com.vavcoders.vamc.erpnextmobileaddons;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.vavcoders.vamc.helper.DatabaseHelper;
import com.vavcoders.vamc.model.Auth;
import com.vavcoders.vamc.model.Settings;

import org.json.JSONException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import static com.vavcoders.vamc.model.Settings.LEAD_SYNC;

/**
 * Created by vamc on 3/29/18.
 */

public class SettingsFragment extends Fragment {
    View myView;
    Button btn_save_settings;

    public Integer google_account_spinner_pos;
    public Spinner google_accounts_spinner;

    public String URL;
    public String LOGGED_IN_USER;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        myView = inflater.inflate(R.layout.fragment_settings, container, false);

        DatabaseHelper db = new DatabaseHelper(myView.getContext());
        Auth loginProfile = db.getLoginProfile();
        URL = loginProfile.getUrl();
        LOGGED_IN_USER = loginProfile.getUname();



        // Populating google_accounts_spinner
        google_accounts_spinner = (Spinner) myView.findViewById(R.id.google_accounts_spinner);
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.planets_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        google_accounts_spinner.setAdapter(adapter);

        btn_save_settings = myView.findViewById(R.id.btn_save_settings);
        btn_save_settings.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String selected_google_account = (String) google_accounts_spinner.getSelectedItem();
                GlobalVariables gv = new GlobalVariables(getActivity());
                gv.saveSettings(selected_google_account);
            }
        });
        return myView;
    }
}
