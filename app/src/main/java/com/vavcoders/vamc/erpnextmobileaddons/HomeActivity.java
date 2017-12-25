package com.vavcoders.vamc.erpnextmobileaddons;

import android.Manifest;
import android.content.Context;
import android.content.Intent;

import java.lang.*;
import java.util.HashMap;

import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import static com.vavcoders.vamc.erpnextmobileaddons.GlobalVariables.*;
import static com.vavcoders.vamc.erpnextmobileaddons.ManifestActivity.*;

public class HomeActivity extends AppCompatActivity {
    GridView gridView;
    GridViewCustomAdapter grisViewCustomeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        TextView tv_home_message = (TextView)findViewById(R.id.home_message);
        tv_home_message.setText("All the incoming calls will be converted into leads. Leads get updated if customer already exists.\n\n YOU NEED TO UNINSTALL THIS APP IF YOU DON'T WANT TO CONVERT YOUR INCOMING CALLS ON THIS DEVICE INTO LEADS. \n\n version: 1.1");
        /*gridView=(GridView)findViewById(R.id.gridViewCustom);
        // Create the Custom Adapter Object
        grisViewCustomeAdapter = new GridViewCustomAdapter(this);
        // Set the Adapter to GridView
        gridView.setAdapter(grisViewCustomeAdapter);

        // Handling touch/click Event on GridView Item
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                String selectedItem;
                selectedItem= String.valueOf(position);
                Class activityClassToLoad = Class.class;
                //Toast.makeText(getApplicationContext(),"Loading activity "+menu[position][0]+" ...", Toast.LENGTH_SHORT).show();
                try {
                    activityClassToLoad = Class.forName("com.vavcoders.vamc.erpnextmobileaddons."+menu[position][0]);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),"In Exception for "+menu[position][0], Toast.LENGTH_SHORT).show();
                }
                Intent mainIntent = new Intent(HomeActivity.this,activityClassToLoad);
                HomeActivity.this.startActivity(mainIntent);
                HomeActivity.this.finish();

            }
        });*/
    }


}
