package com.vavcoders.vamc.erpnextmobileaddons;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.vavcoders.vamc.helper.DatabaseHelper;
import com.vavcoders.vamc.model.Auth;
import com.vavcoders.vamc.model.Settings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.vavcoders.vamc.model.Settings.LEAD_SYNC;

/**
 * Created by vamc on 3/29/18.
 */

public class SettingsFragment extends Fragment {
    private static final String TAG = "VamCLog";
    View myView;
    Button btn_save_settings;

    public Integer google_account_spinner_pos;
    public Spinner google_accounts_spinner,company_spinner;
    public ArrayAdapter<String> companyAdapter,googleAccountsAdapter;
    public ArrayAdapter<CharSequence> adapter;

    public String URL;
    public String LOGGED_IN_USER;
    ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        myView = inflater.inflate(R.layout.fragment_settings, container, false);

        DatabaseHelper db = new DatabaseHelper(myView.getContext());
        Auth loginProfile = db.getLoginProfile();
        URL = loginProfile.getUrl();
        LOGGED_IN_USER = loginProfile.getUname();
        progressDialog = new ProgressDialog(getActivity());

        // Populating google_accounts_spinner
        populateGoogleAccountSpinner();

        btn_save_settings = myView.findViewById(R.id.btn_save_settings);
        btn_save_settings.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String selected_google_account = (String) google_accounts_spinner.getSelectedItem();
                String selected_company = (String) company_spinner.getSelectedItem();
                GlobalVariables gv = new GlobalVariables(getActivity());
                JSONObject settingsObj = new JSONObject();
                try {
                    settingsObj.put("selected_google_account",selected_google_account);
                    settingsObj.put("selected_company",selected_company);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(myView.getContext(),"Exception in settings object creation while saving", Toast.LENGTH_LONG).show();
                }
                gv.saveSettings(settingsObj);
                Settings settings = gv.getAllSettings();
            }
        });
        return myView;
    }

    private void populateCompanySpinner() {
        String generatedURL = "http://" + URL + "/api/method/erpnext_mobile_addons.get_companies";
        AsyncHttpClient client = new AsyncHttpClient();
        progressDialog.setMessage("Fetching companies...");
        progressDialog.show();
        try {
            client.get(generatedURL,new JsonHttpResponseHandler(){

                @Override
                public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, org.json.JSONObject response) {
                    try {
                        JSONArray companyJson = response.getJSONArray("message");
                        List<String> companies = new ArrayList<>();
                        companyAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1, companies);
                        companyAdapter.clear();
                        for(i=0;i<companyJson.length();i++){
                            companyAdapter.add(companyJson.getJSONObject(i).getString("name"));
                        }
                        company_spinner = (Spinner) myView.findViewById(R.id.company_spinner);

                        companyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        company_spinner.setAdapter(companyAdapter);
                        populateSettings();
                        // set after getting response from api for imei
//                        int spinnerPosition = companyAdapter.getPosition("Usedyetnew");
//                        company_spinner.setSelection(spinnerPosition);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(myView.getContext(),"Exception in fetching companies", Toast.LENGTH_LONG).show();
                        progressDialog.hide();
                    }
                }

                public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, org.json.JSONObject response, Throwable throwable) {
                    Toast.makeText(myView.getContext(),"error: ", Toast.LENGTH_LONG).show();
                }
            });
        }catch (Exception e){
            Toast.makeText(myView.getContext(),"Exception: ", Toast.LENGTH_LONG).show();
        }
    }

    private void populateGoogleAccountSpinner() {
        String generatedURL = "http://" + URL + "/api/method/erpnext_mobile_addons.get_google_accounts";
        AsyncHttpClient client = new AsyncHttpClient();
        progressDialog.setMessage("Fetching google accounts...");
        progressDialog.show();
        try {
            client.get(generatedURL,new JsonHttpResponseHandler(){

                @Override
                public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, org.json.JSONObject response) {
                    try {
                        JSONArray companyJson = response.getJSONArray("message");
                        List<String> companies = new ArrayList<>();
                        googleAccountsAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1, companies);
                        googleAccountsAdapter.clear();
                        for(i=0;i<companyJson.length();i++){
                            googleAccountsAdapter.add(companyJson.getJSONObject(i).getString("email"));
                        }
                        google_accounts_spinner = (Spinner) myView.findViewById(R.id.google_accounts_spinner);

                        googleAccountsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        google_accounts_spinner.setAdapter(googleAccountsAdapter);
                        // Populating company_spinner
                        populateCompanySpinner();
                        // set after getting response from api for imei
//                        int spinnerPosition = googleAccountsAdapter.getPosition("Usedyetnew");
//                        google_accounts_spinner.setSelection(spinnerPosition);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(myView.getContext(),"Exception in fetching companies", Toast.LENGTH_LONG).show();
                        progressDialog.hide();
                    }
                }

                public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, org.json.JSONObject response, Throwable throwable) {
                    Toast.makeText(myView.getContext(),"error: ", Toast.LENGTH_LONG).show();
                }
            });
        }catch (Exception e){
            Toast.makeText(myView.getContext(),"Exception: ", Toast.LENGTH_LONG).show();
        }
    }

    private void populateSettings() {
        progressDialog.setMessage("Obtaining settings...");
        progressDialog.show();
        String generatedURL = "http://" + URL + "/api/method/erpnext_mobile_addons.fetch_settings";
        AsyncHttpClient client = new AsyncHttpClient();
        TelephonyManager tMgr = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(myView.getContext(),"Not permitted to read device", Toast.LENGTH_LONG).show();
        }
        String imei = tMgr.getDeviceId();
        RequestParams params = new RequestParams();
        params.put("imei", imei);
        params.put("user", this.LOGGED_IN_USER);
        try {
            client.post(generatedURL,params,new JsonHttpResponseHandler(){

                @Override
                public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, org.json.JSONObject response) {
                    try {
                        JSONArray settingsJson = response.getJSONArray("message");
                        // set after getting response from api for imei
                        int companySpinnerPosition = companyAdapter.getPosition(settingsJson.getJSONObject(0).get("company").toString());
                        company_spinner.setSelection(companySpinnerPosition);
                        int gAccountSpinnerPosition = googleAccountsAdapter.getPosition(settingsJson.getJSONObject(0).get("google_account").toString());
                        google_accounts_spinner.setSelection(gAccountSpinnerPosition);
                        progressDialog.hide();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(myView.getContext(),"Exception in fetching settings", Toast.LENGTH_LONG).show();
                        progressDialog.hide();
                    }
                }

                public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, org.json.JSONObject response, Throwable throwable) {
                    Toast.makeText(myView.getContext(),"error: ", Toast.LENGTH_LONG).show();
                }
            });
        }catch (Exception e){
            Toast.makeText(myView.getContext(),"Exception: ", Toast.LENGTH_LONG).show();
        }
    }

}
