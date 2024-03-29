package com.vavcoders.vamc.erpnextmobileaddons;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.telephony.TelephonyManager;
// import android.util.Log;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.vavcoders.vamc.helper.DatabaseHelper;
import com.vavcoders.vamc.model.Auth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class SplashActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private static final String TAG = "VamCLog";

    DatabaseHelper db;

    /**
     * Duration of wait
     **/
    private final int SPLASH_DISPLAY_LENGTH = 1000;
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
            reloadActivity(600);
        } else {
            Toast.makeText(getApplicationContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            reloadActivity(reloadActivityAfter);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ) {
            requestPermissions(new String[]{Manifest.permission.READ_CALL_LOG,Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            db = new DatabaseHelper(getApplicationContext());
            Auth loginProfile = db.getLoginProfile();
            Toast.makeText(getApplicationContext(),"Uname: "+loginProfile.getUname(), Toast.LENGTH_LONG).show();
            if(loginProfile.getUname() == null){
                Intent mainIntent = new Intent(SplashActivity.this, PreLoginActivity.class);
                SplashActivity.this.startActivity(mainIntent);
                SplashActivity.this.finish();
            }else{
                String uname = loginProfile.getUname();
                String deviceId = getDeviceId();
                HashMap<String, String> user = new HashMap<String, String>();
                user.put("username", uname);
                user.put("imei", deviceId);
                user.put("mobile", "");
                try {
                    logUser(user);
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Create an Intent that will start the Menu-Activity.
                            Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                            SplashActivity.this.startActivity(mainIntent);
                            SplashActivity.this.finish();
                        }
                    }, SPLASH_DISPLAY_LENGTH);
                }
            }


        }
    }

    public String getDeviceId() {
        String deviceId = "";
        TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return deviceId;
        }
        deviceId = tMgr.getDeviceId();
        return deviceId;
    }

    private void logUser(HashMap<String, String> user) throws JSONException {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        GlobalVariables gv = new GlobalVariables(getApplicationContext());
        JSONObject obj = new JSONObject(user);
        params.put("username", obj.get("username"));
        params.put("imei", obj.get("imei"));
        params.put("mobile", obj.get("mobile"));
        try {
            client.post("http://"+gv.URL+"/api/method/erpnext_mobile_addons.log_user", params, new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, org.json.JSONObject response) {
                    Toast.makeText(getApplicationContext(), "Authenticated", Toast.LENGTH_LONG).show();
                }

                public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, org.json.JSONObject response, Throwable throwable) {
                    Toast.makeText(getApplicationContext(), "Error in HAlU", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Exception in HAlU: ", Toast.LENGTH_SHORT).show();
        }

    }


}
