package com.vavcoders.vamc.erpnextmobileaddons;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.google.android.gms.drive.events.ChangeEvent;
import com.google.android.gms.drive.events.ChangeListener;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.vavcoders.vamc.helper.DatabaseHelper;
import com.vavcoders.vamc.model.Auth;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import cz.msebera.android.httpclient.Header;

public class DeliveryActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "VamCLog";
    private static final int REQUEST_CODE_RESOLUTION = 1;
    private static final  int REQUEST_CODE_OPENER = 2;
    private DriveId mFileId;
    private GoogleApiClient mGoogleApiClient;

    private static AutoCompleteTextView actv_manifest_customer;
    private Button btn_capture_manifest,btn_confirm_manifest,btn_try_manifest;
    private Uri fileUri;
    public static final int MEDIA_TYPE_IMAGE = 1;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static String manifest_file_name;
    private static final String IMAGE_DIRECTORY_NAME = "ERPNextMobileAddons";
    private TextView tv_manifest_intro_label,tv_form_manifest_dn_label,tv_form_manifest_cust_label;
    private ImageView iv_manifest_preview;
    private Spinner spinner_manifest_dn_si;
    public byte[] imageData;
    DatabaseHelper db;
    private String sinv_id;
    public String[] customers = {};
    ProgressBar progressBar;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Manifest Upload");
        progressDialog = new ProgressDialog(this);

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
        /*actv_manifest_customer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    // get SI for selected customer (actv_manifest_customer)
                    getSalesInvoiceForCustomer(actv_manifest_customer.getText().toString());
                    Log.d(TAG,"get SI for: "+actv_manifest_customer.getText());

                }
            }
        });*/
        spinner_manifest_dn_si = (Spinner) findViewById(R.id.spinner_manifest_dn_si);
        spinner_manifest_dn_si.setVisibility(View.GONE);
        tv_manifest_intro_label = (TextView) findViewById(R.id.tv_manifest_intro_label);
        tv_form_manifest_dn_label = (TextView) findViewById(R.id.tv_form_manifest_dn_label);
        tv_form_manifest_cust_label = (TextView) findViewById(R.id.tv_form_manifest_cust_label);
        tv_form_manifest_dn_label.setVisibility(View.GONE);
        iv_manifest_preview = (ImageView) findViewById(R.id.iv_manifest_preview);
        btn_confirm_manifest = (Button) findViewById(R.id.btn_confirm_manifest);
        btn_try_manifest = (Button) findViewById(R.id.btn_try_manifest);
        btn_capture_manifest = (Button) findViewById(R.id.btn_capture_manifest);
        btn_capture_manifest.setVisibility(View.GONE);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);
        getAllCustomersFromERP();
        btn_capture_manifest.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
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
                                    captureImage();
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(getApplicationContext(),"Capture manifest to link with "+response.getString("message"), Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(getApplicationContext(),"Some error occurred in DN check. Please contact admin.", Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
//                                    Log.d("VamCLog","Exception raised in 'check_dn_for_manifest' api response");
                                e.printStackTrace();
                                progressBar.setVisibility(View.GONE);
                            }
                        }

                        public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, org.json.JSONObject response, Throwable throwable) {
//                                Log.d("VamCLog","Exception raised in 'check_dn_for_manifest' api call");
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }catch (Exception e){
//                        Log.d("VamCLog","Exception raised in 'check_dn_for_manifest' api call");
                    progressBar.setVisibility(View.GONE);
                }


            }
        });

        btn_confirm_manifest.setOnClickListener(new View.OnClickListener() {
            File f;
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("Uploading to drive...");
                progressDialog.show();
                /* >> Upload to google drive */
                Bitmap bitmap = BitmapFactory.decodeFile(getManifestPic(manifest_file_name).getAbsolutePath());
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                imageData = stream.toByteArray();
                final ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback =
                        new ResultCallback<DriveApi.DriveContentsResult>() {
                            @Override
                            public void onResult(DriveApi.DriveContentsResult result) {
                                if (result.getStatus().isSuccess()) {
                                    CreateImageOnGoogleDrive(result,imageData,manifest_file_name);
                                }
                            }
                        };
                Drive.DriveApi.newDriveContents(mGoogleApiClient)
                        .setResultCallback(driveContentsCallback);

                /* << Upload to google drive */
            }
        });
        btn_try_manifest.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // try again click event
                tv_manifest_intro_label.setText("Save manifest in just a few clicks. Manifests will be saved in your google drive (configured in ERP) and linked with the specified Delivery Note in ERP");
                tv_form_manifest_dn_label.setVisibility(View.VISIBLE);
                actv_manifest_customer.setText("");
                actv_manifest_customer.setVisibility(View.VISIBLE);
                iv_manifest_preview.setVisibility(View.GONE);
                btn_capture_manifest.setVisibility(View.VISIBLE);
                btn_confirm_manifest.setVisibility(View.GONE);
                btn_try_manifest.setVisibility(View.GONE);
            }
        });
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
                        btn_capture_manifest.setVisibility(View.VISIBLE);
                    } catch (IOException e) {
//                        Log.d(TAG,"IOException");
                        adapter =
                                new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, new String[]{});
                        spinner_manifest_dn_si.setAdapter(adapter);
                        tv_form_manifest_dn_label.setVisibility(View.GONE);
                        spinner_manifest_dn_si.setVisibility(View.GONE);
                        btn_capture_manifest.setVisibility(View.GONE);
                        e.printStackTrace();
                    } catch (JSONException e) {
//                        Log.d(TAG,"JSONException");
                        adapter =
                                new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, new String[]{});
                        spinner_manifest_dn_si.setAdapter(adapter);

                        tv_form_manifest_dn_label.setVisibility(View.GONE);
                        spinner_manifest_dn_si.setVisibility(View.GONE);
                        btn_capture_manifest.setVisibility(View.GONE);
                        e.printStackTrace();
                    }
                }
            });
        }catch (Exception e){
//            Log.d(TAG,"Exception");
        }
    }

    public void CreateImageOnGoogleDrive(DriveApi.DriveContentsResult result, final byte[] imageDataInput,final String manifest_file_name){

        final DriveContents driveContents = result.getDriveContents();

        // Perform I/O off the UI thread.
        new Thread() {
            @Override
            public void run() {
                OutputStream outputStream = driveContents.getOutputStream();
                try {
                    outputStream.write(imageDataInput);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                /* >> creates folder(if not exists) and uploads manifest to that folder */
                Query query = new Query.Builder().addFilter(Filters.and(Filters.eq(SearchableField.TITLE, IMAGE_DIRECTORY_NAME), Filters.eq(SearchableField.TRASHED, false))).build();
                Drive.DriveApi.query(mGoogleApiClient, query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
                    @Override
                    public void onResult(@NonNull DriveApi.MetadataBufferResult result)
                    {
                        if (!result.getStatus().isSuccess())
                        {
//                            Log.d(TAG, "Cannot create folder in the root.");
                        } else
                        {
                            boolean isFound = false;
                            for (Metadata m : result.getMetadataBuffer())
                            {
                                if (m.getTitle().equals(IMAGE_DIRECTORY_NAME)) {
//                                    Log.d(TAG, "Folder exists");
                                    isFound = true;
                                    DriveId driveId = m.getDriveId();
                                    UploadFileToFolder(driveId,driveContents);
                                    break;
                                }
                            }
                            if (!isFound)
                            {
//                                Log.d(TAG, "Folder not found; creating it.");
                                MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(IMAGE_DIRECTORY_NAME).build();
                                Drive.DriveApi.getRootFolder(mGoogleApiClient)
                                        .createFolder(mGoogleApiClient, changeSet)
                                        .setResultCallback(new ResultCallback<DriveFolder.DriveFolderResult>() {
                                            @Override
                                            public void onResult(@NonNull DriveFolder.DriveFolderResult result)
                                            {
                                                if (!result.getStatus().isSuccess())
                                                {
//                                                    Log.d(TAG, "Error while trying to create the folder");
                                                } else {
//                                                    Log.d(TAG, "Created a folder");
                                                    DriveId driveId = result.getDriveFolder().getDriveId();
                                                    UploadFileToFolder(driveId,driveContents);
                                                }
                                            }
                                        });
                            }
                        }
                    }
                });
                /* << creates folder(if not exists) and uploads manifest to that folder */
            }
        }.start();
    }
    private void UploadFileToFolder(final DriveId driveId,final DriveContents driveContents){
        DriveFolder folder = driveId.asDriveFolder();
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(manifest_file_name+".png")
                .setMimeType("image/jpeg")
                .setStarred(true).build();
        // create a file in root folder
        folder
                .createFile(mGoogleApiClient, changeSet, driveContents)
                .setResultCallback(fileCallback);
    }
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        // start the image capture Intent
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }
    public Uri getOutputMediaFileUri(int type) {
        return FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", getOutputMediaFile(type));
    }
    private static File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"");
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + manifest_file_name + ".jpg");
        return mediaFile;
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient == null) {

            /**
             * Create the API client and bind it to an instance variable.
             * We use this instance as the callback for connection and connection failures.
             * Since no account name is passed, the user is prompted to choose.
             */
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

        }

        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null) {

//            Toast.makeText(getApplicationContext(), "Disconnecting from Google Drive", Toast.LENGTH_LONG).show();
            // disconnect Google Android Drive API connection.
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {

        // Called whenever the API client fails to connect.
//        Log.d(TAG, "GoogleApiClient connection failed: " + result.toString());
//        Toast.makeText(getApplicationContext(), "Google Drive connection failure: "+result.toString(), Toast.LENGTH_LONG).show();
        if (!result.hasResolution()) {
//            Log.d(TAG, "hasResolution");

            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;
        }

        /**
         *  The failure has a resolution. Resolve it.
         *  Called typically when the app is not yet authorized, and an  authorization
         *  dialog is displayed to the user.
         */

        try {
//            Log.d(TAG,"trying startResolutionForResult");
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);

        } catch (IntentSender.SendIntentException e) {

//            Log.d(TAG, "Exception while starting resolution activity", e);
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
//        Log.d(TAG,"Connected to Google Drive");
        Toast.makeText(getApplicationContext(), "Connected to Google Drive.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionSuspended(int cause) {
//        Log.d(TAG, "GoogleApiClient connection suspended");
    }

    /**
     * Handle result of Created file
     */
    String driveId;
    final private ChangeListener uploadedFileUrlToERP = new ChangeListener()
    {
        @Override
        public void onChange(ChangeEvent event)
        {
            String driveId =  event.getDriveId().getResourceId();
            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            params.put("drive_id",driveId);
            params.put("dn_id",manifest_file_name);
            db = new DatabaseHelper(getApplicationContext());
            Auth loginProfile = db.getLoginProfile();
            final String generatedURL = "http://"+loginProfile.getUrl()+"/api/method/erpnext_mobile_addons.update_manifest_link_in_dn";
            try {
                client.post(generatedURL,params,new JsonHttpResponseHandler(){

                    @Override
                    public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, org.json.JSONObject response) {
                        try {
                            if(response.getString("message")=="Success"){

                            }
                        } catch (JSONException e) {
//                            Log.d("VamCLog","Exception raised in update_manifest_link_in_dn api response");
                            e.printStackTrace();
                        }
                        progressDialog.setMessage("Completed successfully");
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }

                    public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, org.json.JSONObject response, Throwable throwable) {
//                        Log.d("VamCLog","Exception raised in update_manifest_link_in_dn api call");
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }catch (Exception e){
//                Log.d("VamCLog","Exception raised in update_manifest_link_in_dn api call");
                progressBar.setVisibility(View.GONE);
            }
        }
    };
    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback = new
            ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(DriveFolder.DriveFileResult result) {
                    if (result.getStatus().isSuccess()) {
                        result.getDriveFile().getDriveId().getResourceId();
//                        Log.d(TAG,"file created: "+""+result.getDriveFile().getDriveId());
                        Toast.makeText(getApplicationContext(), "Uploaded to Google Drive", Toast.LENGTH_LONG).show();

                        DriveId File_Uncompleted_Id = result.getDriveFile().getDriveId();

                        DriveFile file = Drive.DriveApi.getFile(mGoogleApiClient, File_Uncompleted_Id);
                        file.addChangeListener(mGoogleApiClient, uploadedFileUrlToERP);
//                        String driveId = result.getDriveFile().getDriveId().toString();


                    }


                    return;

                }
            };
    /**
     *  Handle Response of selected file
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(final int requestCode,
                                    final int resultCode, final Intent data) {

        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // successfully captured the image display it in image view
                previewCapturedImage();
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(this,
                        "You have cancelled", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to capture image
                Toast.makeText(this,
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }
    private void previewCapturedImage(){

        try {
            tv_manifest_intro_label.setText("Confirm to link the manifest to "+manifest_file_name);
            tv_form_manifest_dn_label.setVisibility(View.GONE);
            actv_manifest_customer.setVisibility(View.GONE);
            tv_form_manifest_cust_label.setVisibility(View.GONE);
            spinner_manifest_dn_si.setVisibility(View.GONE);
            iv_manifest_preview.setVisibility(View.VISIBLE);
            btn_capture_manifest.setVisibility(View.GONE);
            btn_confirm_manifest.setVisibility(View.VISIBLE);
            btn_try_manifest.setVisibility(View.VISIBLE);
            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();
            // downsizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 8;
            Bitmap bitmap = BitmapFactory.decodeFile(getManifestPic(manifest_file_name).getAbsolutePath());
            iv_manifest_preview.setImageBitmap(bitmap);
        } catch (NullPointerException e) {
//            Log.d("VamCLog","Exception raised in previewCapturedImage()");
//            Log.d("VamCLog",e.getMessage());
            e.printStackTrace();
        }
    }
    private File getManifestPic(String filename) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"");
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();

        File file = new File(mediaStorageDir.getPath() + File.separator
                + filename + ".jpg");
        return file;
    }
}
