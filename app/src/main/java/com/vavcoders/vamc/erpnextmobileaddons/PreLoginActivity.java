package com.vavcoders.vamc.erpnextmobileaddons;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.*;
import android.util.Log;

import org.json.JSONException;

import java.net.MalformedURLException;
import java.net.URL;

public class PreLoginActivity extends AppCompatActivity {

    private static final String TAG = "com.vavcoders.vamc.erpnextmobileaddons";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_login);
        Button btn_login = (Button) findViewById(R.id.btnLogin);
        btn_login.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                EditText et_url = (EditText) findViewById(R.id.et_url);
                EditText et_email = (EditText) findViewById(R.id.et_email);
                EditText et_password = (EditText) findViewById(R.id.et_password);

                String generatedURL = "http://"+et_url.getText()+"/api/method/login";
                AsyncHttpClient client = new AsyncHttpClient();
                RequestParams params = new RequestParams();
                params.put("usr",et_email.getText());
                params.put("pwd",et_password.getText());
                try {
                    client.post(generatedURL,params,new JsonHttpResponseHandler(){

                        @Override
                        public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, org.json.JSONObject response) {
                            try {
                                String login_check = response.getString("message");
                                if(login_check.equalsIgnoreCase("logged in") && response.has("full_name")){
                                    Intent mainIntent = new Intent(PreLoginActivity.this,HomeActivity.class);
                                    PreLoginActivity.this.startActivity(mainIntent);
                                    PreLoginActivity.this.finish();
                                }else{
                                    Toast.makeText(getApplicationContext(),"Log in failed", Toast.LENGTH_SHORT).show();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(),"Exception", Toast.LENGTH_SHORT).show();

                            }

                        }

                        public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, org.json.JSONObject response, Throwable throwable) {

                            Toast.makeText(getApplicationContext(),"error: ", Toast.LENGTH_SHORT).show();
                        }
                    });
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(),"Exception: ", Toast.LENGTH_SHORT).show();
                }


//                Toast.makeText(getApplicationContext(),"Login Action", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
