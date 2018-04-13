package com.vavcoders.vamc.erpnextmobileaddons;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;

import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;

import com.google.api.services.youtube.model.*;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.vavcoders.vamc.helper.DatabaseHelper;
import com.vavcoders.vamc.model.Auth;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


public class VideoUploadActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks {
    private static final String TAG = "VamCLog";
    private String sinv_id;
    private static String manifest_file_name;
    GoogleAccountCredential mCredential;
    private TextView mOutputText,tv_manifest_intro_label,tv_form_manifest_dn_label,tv_form_manifest_cust_label;
    private Button mCallApiButton,btn_capture_video,btn_confirm_manifest,btn_try_manifest;
    ProgressDialog progressDialog;
    private static AutoCompleteTextView actv_manifest_customer;
    DatabaseHelper db;
    public String[] customers = {};
    private Spinner spinner_manifest_dn_si;
    private Uri fileUri;

    public static final int MEDIA_TYPE_VIDEO = 1;
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 100;
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String VIDEO_DIRECTORY_NAME = "ERPNextMobileAddons";

    private static final String BUTTON_TEXT = "Call YouTube Data API";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    File file;

    /** Application name. */
    private static final String APPLICATION_NAME = "API Sample";

    /** Directory to store user credentials for this application. */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
            System.getProperty("user.home"), ".credentials/java-youtube-api-tests");

    /** Global instance of the {@link FileDataStoreFactory}. */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;

    /** Global instance of the scopes required by this quickstart.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/drive-java-quickstart
     */
    private static final String[] SCOPES = {YouTubeScopes.YOUTUBE_READONLY,YouTubeScopes.YOUTUBE_FORCE_SSL,YouTubeScopes.YOUTUBEPARTNER};

//    static {
//        try {
//            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
//            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
//        } catch (Throwable t) {
//            t.printStackTrace();
//            System.exit(1);
//        }
//    }

    /**
     * Create the main activity.
     * @param savedInstanceState previously saved instance data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_upload);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Packing Video Upload");

        actv_manifest_customer = (AutoCompleteTextView) findViewById(R.id.actv_manifest_customer);
        actv_manifest_customer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                getSalesInvoiceForCustomer(actv_manifest_customer.getText().toString());
            }
        });
        progressDialog = new ProgressDialog(this);
        mCallApiButton = (Button) findViewById(R.id.btn_test_youtube);
        mOutputText = (TextView) findViewById(R.id.tv_manifest_intro_label);
        tv_form_manifest_dn_label = (TextView) findViewById(R.id.tv_form_manifest_dn_label);
        tv_form_manifest_dn_label.setVisibility(View.GONE);
        tv_form_manifest_cust_label = (TextView) findViewById(R.id.tv_form_manifest_cust_label);
        btn_confirm_manifest = (Button) findViewById(R.id.btn_confirm_manifest);
        btn_try_manifest = (Button) findViewById(R.id.btn_try_manifest);
        btn_capture_video = (Button) findViewById(R.id.btn_capture_video);
        btn_capture_video.setVisibility(View.GONE);
        spinner_manifest_dn_si = (Spinner) findViewById(R.id.spinner_manifest_dn_si);
        spinner_manifest_dn_si.setVisibility(View.GONE);
        getAllCustomersFromERP();
        btn_capture_video.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                progressDialog.setMessage("Looking for delivery note...");
                progressDialog.show();
                // check if DN exists for spinner_manifest_dn_si
                // create DN if doesn't exists
                // capture picture
                AsyncHttpClient client = new AsyncHttpClient();
                RequestParams params = new RequestParams();
                sinv_id = String.valueOf(spinner_manifest_dn_si.getSelectedItem());
                db = new DatabaseHelper(getApplicationContext());
                Auth loginProfile = db.getLoginProfile();
                params.put("sinv_id",sinv_id);
                params.put("owner",loginProfile.getUname());
                final String generatedURL = "http://"+loginProfile.getUrl()+"/api/method/erpnext_mobile_addons.get_dn_for_sinv";
                try {
                    client.post(generatedURL,params,new JsonHttpResponseHandler(){

                        @Override
                        public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, org.json.JSONObject response) {
                            try {
                                String result = response.getString("message");
                                if(!result.equalsIgnoreCase("404")){
                                    manifest_file_name = response.getString("message");
                                    captureVideo();
                                    progressDialog.hide();
                                    Toast.makeText(getApplicationContext(),"Record video of packing which will link with "+response.getString("message"), Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(getApplicationContext(),"Some error occurred in DN check. Please contact admin.", Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
//                                    Log.d("VamCLog","Exception raised in 'check_dn_for_manifest' api response");
                                e.printStackTrace();
                                progressDialog.hide();
                            }
                        }

                        public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, org.json.JSONObject response, Throwable throwable) {
//                                Log.d("VamCLog","Exception raised in 'check_dn_for_manifest' api call");
                            progressDialog.hide();
                        }
                    });
                }catch (Exception e){
//                        Log.d("VamCLog","Exception raised in 'check_dn_for_manifest' api call");
                    progressDialog.hide();
                }


            }
        });

        mCallApiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallApiButton.setEnabled(false);
                mOutputText.setText("");
                getResultsFromApi();
                mCallApiButton.setEnabled(true);
            }
        });




        progressDialog.setMessage("Calling YouTube Data API ...");

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
    }
    private void captureVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
        Log.d(TAG,"fileURI: "+fileUri.getPath());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        // start the image capture Intent
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
    }
    public Uri getOutputMediaFileUri(int type) {
        return FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", getOutputMediaFile(type));
    }
    private static File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),VIDEO_DIRECTORY_NAME);
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + manifest_file_name + ".mp4");
        return mediaFile;
    }
    public void getAllCustomersFromERP(){
        db = new DatabaseHelper(getApplicationContext());
        Auth loginProfile = db.getLoginProfile();
        String customerUrl = "http://"+loginProfile.getUrl()+"/api/method/erpnext_mobile_addons.get_all_customers";
        AsyncHttpClient client = new AsyncHttpClient();
        try {
            client.get(customerUrl,new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        customers = mapper.readValue(response.getString("message").toString(), String[].class);
                        ArrayAdapter<String> adapter =
                                new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, customers);
                        actv_manifest_customer.setThreshold(1);
                        actv_manifest_customer.setAdapter(adapter);
//                        spinner_manifest_dn_si.setAdapter(adapter);
                    } catch (IOException e) {
//                        Log.d(TAG,"IOException in get_all_customers call");
                        e.printStackTrace();
                    } catch (JSONException e) {
//                        Log.d(TAG,"JSONException in get_all_customers call");
                        e.printStackTrace();
                    }
                }
            });
        }catch (Exception e){
//            Log.d(TAG,"Exception in get_all_customers call");
        }
//        Log.d(TAG,String.valueOf(customers.length));
    }
    private void getSalesInvoiceForCustomer(String customer) {
        db = new DatabaseHelper(getApplicationContext());
        Auth loginProfile = db.getLoginProfile();
        String salesInvoiceForCustUrl = "http://"+loginProfile.getUrl()+"/api/method/erpnext_mobile_addons.get_sinv_for_customer";
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("customer",customer);
        try {
            client.post(salesInvoiceForCustUrl ,params,new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    ObjectMapper mapper = new ObjectMapper();
                    ArrayAdapter<String> adapter;
                    try {
                        customers = mapper.readValue(response.getString("message").toString(), String[].class);
                        adapter =
                                new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, customers);
                        spinner_manifest_dn_si.setAdapter(adapter);
                        tv_form_manifest_dn_label.setVisibility(View.VISIBLE);
                        spinner_manifest_dn_si.setVisibility(View.VISIBLE);
                        btn_capture_video.setVisibility(View.VISIBLE);
                    } catch (IOException e) {
//                        Log.d(TAG,"IOException");
                        adapter =
                                new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, new String[]{});
                        spinner_manifest_dn_si.setAdapter(adapter);
                        tv_form_manifest_dn_label.setVisibility(View.GONE);
                        spinner_manifest_dn_si.setVisibility(View.GONE);
                        btn_capture_video.setVisibility(View.GONE);
                        e.printStackTrace();
                    } catch (JSONException e) {
//                        Log.d(TAG,"JSONException");
                        adapter =
                                new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, new String[]{});
                        spinner_manifest_dn_si.setAdapter(adapter);

                        tv_form_manifest_dn_label.setVisibility(View.GONE);
                        spinner_manifest_dn_si.setVisibility(View.GONE);
                        btn_capture_video.setVisibility(View.GONE);
                        e.printStackTrace();
                    }
                }
            });
        }catch (Exception e){
//            Log.d(TAG,"Exception");
        }
    }

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi() {
        Log.d(TAG,"In getResultsFromApi");
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            mOutputText.setText("No network connection available.");
        } else {
            new MakeRequestTask(mCredential).execute();
        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    mOutputText.setText(
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.");
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
            case CAMERA_CAPTURE_VIDEO_REQUEST_CODE:
                if(resultCode == RESULT_OK){
                    Log.d(TAG,"ResultOK");
                    progressDialog.setMessage("Saving video to device");
                    progressDialog.show();
                    /* >> Storing in another location*/
                    try{
                        AssetFileDescriptor videoAsset = getContentResolver().openAssetFileDescriptor(fileUri, "r");
                        FileInputStream fis = videoAsset.createInputStream();
                        File root=new File(Environment.getExternalStorageDirectory(),"ERPNextMobileAddonsVideos");

                        if (!root.exists()) {
                            root.mkdirs();
                        }

                        file=new File(root,manifest_file_name+".mp4" );
                        Log.d(TAG,"File storage absolute path: "+file.getAbsolutePath());
                        Log.d(TAG,"File storage path: "+file.getPath());
                        Log.d(TAG,"File storage canonical path: "+file.getCanonicalPath());
                        Log.d(TAG,"FileURI: "+fileUri.getPath());
                        FileOutputStream fos = new FileOutputStream(file);
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = fis.read(buf)) > 0) {
                            fos.write(buf, 0, len);
                        }
                        fis.close();
                        fos.close();
                        getResultsFromApi();
                    }catch (Exception e){
                        Log.d(TAG,"File storing in another location exception");
                        Log.d(TAG,e.getMessage());
                    }

                    /* << Storing in another location*/

                }else if(resultCode == RESULT_CANCELED){
                    Log.d(TAG,"ResultCancelled");
                }else{
                    Log.d(TAG,"Something bad happened... onActivityResult");
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                VideoUploadActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * An asynchronous task that handles the YouTube Data API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.youtube.YouTube mService = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.youtube.YouTube.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("YouTube Data API Android Quickstart")
                    .build();
        }

        /**
         * Background task to call YouTube Data API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
//                return getDataFromApi();
                return uploadVideoToYoutube(fileUri.getPath());
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch information about the "GoogleDevelopers" YouTube channel.
         * @return List of Strings containing information about the channel.
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException {
            // Get a list of up to 10 files.
            List<String> channelInfo = new ArrayList<String>();
            ChannelListResponse result = mService.channels().list("snippet,contentDetails,statistics")
                    .setForUsername("GoogleDevelopers")
                    .execute();
            List<Channel> channels = result.getItems();
            if (channels != null) {
                Channel channel = channels.get(0);
                channelInfo.add("This channel's ID is " + channel.getId() + ". " +
                        "Its title is '" + channel.getSnippet().getTitle() + ", " +
                        "and it has " + channel.getStatistics().getViewCount() + " views.");
            }
            return channelInfo;
        }

        private List<String> uploadVideoToYoutube(String media_filename) throws IOException{
            progressDialog.setMessage("Uploading video to youtube");
            progressDialog.show();
            try{
                String mime_type = "video/*";
//                String media_filename = "sample_video.flv";
                HashMap<String, String> parameters = new HashMap<>();
                parameters.put("part", "snippet,status");


                Video video = new Video();
                VideoSnippet snippet = new VideoSnippet();
                snippet.set("categoryId", "22");
                snippet.set("description", "Packing video of the product");
                snippet.set("title", actv_manifest_customer.getText() + " - " + manifest_file_name);
                VideoStatus status = new VideoStatus();
                status.set("privacyStatus", "private");

                video.setSnippet(snippet);
                video.setStatus(status);

                Log.d(TAG,"TRYING TO GET");
                Log.d(TAG,Environment.getExternalStorageDirectory().getPath()+"/ERPNextMobileAddonsVideos/"+manifest_file_name+".mp4");
                Log.d(TAG,"1");

//                InputStreamContent mediaContent = new InputStreamContent(mime_type,
//                        VideoUploadActivity.class.getResourceAsStream(Environment.getExternalStorageDirectory().getPath()+"ERPNextMobileAddonsVideos/"+manifest_file_name+".mp4"));
                FileInputStream fileInputStream = new FileInputStream(Environment.getExternalStorageDirectory().getPath()+"/ERPNextMobileAddonsVideos/"+manifest_file_name+".mp4");
                InputStreamContent mediaContent = new InputStreamContent(mime_type,fileInputStream);
                Log.d(TAG,"2");
                YouTube.Videos.Insert videosInsertRequest = mService.videos().insert(parameters.get("part").toString(), video, mediaContent);
                Log.d(TAG,"3");
                MediaHttpUploader uploader = videosInsertRequest.getMediaHttpUploader();
                Log.d(TAG,"4");


                uploader.setDirectUploadEnabled(false);
                Log.d(TAG,"5");
                MediaHttpUploaderProgressListener progressListener = new MediaHttpUploaderProgressListener() {
                    public void progressChanged(MediaHttpUploader uploader) throws IOException {
                        Log.d(TAG,"6");
                        switch (uploader.getUploadState()) {
                            case INITIATION_STARTED:
                                Log.d(TAG,"Initiation Started");
                                break;
                            case INITIATION_COMPLETE:
                                Log.d(TAG,"Initiation Completed");
                                break;
                            case MEDIA_IN_PROGRESS:
                                Log.d(TAG,"Upload in progress");
                                Log.d(TAG,"Upload percentage: " + uploader.getProgress());
                                break;
                            case MEDIA_COMPLETE:
                                Log.d(TAG,"Upload Completed!");
                                break;
                            case NOT_STARTED:
                                Log.d(TAG,"Upload Not Started!");
                                break;
                        }
                    }
                };
                uploader.setProgressListener(progressListener);
                Video response = videosInsertRequest.execute();
            } catch (GoogleJsonResponseException e) {
                Log.d(TAG,"In GoogleJsonResponseException");
                e.printStackTrace();
                System.err.println("There was a service error: " + e.getDetails().getCode() + " : " + e.getDetails().getMessage());
            } catch (Throwable t) {
                Log.d(TAG,"In Throwable exception");
                Log.d(TAG,t.toString());
                Log.d(TAG,t.getLocalizedMessage());
                Log.d(TAG,t.getMessage());
                t.printStackTrace();
            }
            return Arrays.asList();
        }

        @Override
        protected void onPreExecute() {
            mOutputText.setText("");
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            progressDialog.hide();
            if (output == null || output.size() == 0) {
                mOutputText.setText("No results returned.");
            } else {
                output.add(0, "Data retrieved using the YouTube Data API:");
                mOutputText.setText(TextUtils.join("\n", output));
            }
        }

        @Override
        protected void onCancelled() {
            progressDialog.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            VideoUploadActivity.REQUEST_AUTHORIZATION);
                } else {
                    mOutputText.setText("The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                mOutputText.setText("Request cancelled.");
            }
        }
    }
}