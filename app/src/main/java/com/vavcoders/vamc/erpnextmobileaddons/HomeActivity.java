package com.vavcoders.vamc.erpnextmobileaddons;

import android.Manifest;
import android.content.Intent;
import java.lang.*;

import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;
import static com.vavcoders.vamc.erpnextmobileaddons.GlobalVariables.*;
import static com.vavcoders.vamc.erpnextmobileaddons.ManifestActivity.*;
public class HomeActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    GridView gridView;
    GridViewCustomAdapter grisViewCustomeAdapter;

    private int reloadActivityAfter = 1000;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CALL_LOG}, PERMISSIONS_REQUEST_READ_CONTACTS);
        }

        gridView=(GridView)findViewById(R.id.gridViewCustom);
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
        });
    }
}
