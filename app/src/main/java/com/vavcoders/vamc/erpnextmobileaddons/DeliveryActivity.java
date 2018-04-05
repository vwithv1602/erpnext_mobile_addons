package com.vavcoders.vamc.erpnextmobileaddons;

import android.app.Activity;
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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.vavcoders.vamc.helper.DatabaseHelper;
import com.vavcoders.vamc.model.Auth;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class DeliveryActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "VamCLog";
    private static final int REQUEST_CODE_RESOLUTION = 1;
    private static final  int REQUEST_CODE_OPENER = 2;
    private DriveId mFileId;
    private GoogleApiClient mGoogleApiClient;

    private static EditText et_manifest_dn;
    private Button btn_capture_manifest,btn_confirm_manifest,btn_try_manifest;
    private Uri fileUri;
    public static final int MEDIA_TYPE_IMAGE = 1;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static String manifest_file_name;
    private static final String IMAGE_DIRECTORY_NAME = "ERPNextMobileAddons";
    private TextView tv_manifest_intro_label,tv_form_manifest_dn_label;
    private ImageView iv_manifest_preview;
    public byte[] imageData;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        et_manifest_dn = (EditText) findViewById(R.id.et_manifest_dn);
        tv_manifest_intro_label = (TextView) findViewById(R.id.tv_manifest_intro_label);
        tv_form_manifest_dn_label = (TextView) findViewById(R.id.tv_form_manifest_dn_label);
        iv_manifest_preview = (ImageView) findViewById(R.id.iv_manifest_preview);
        btn_confirm_manifest = (Button) findViewById(R.id.btn_confirm_manifest);
        btn_try_manifest = (Button) findViewById(R.id.btn_try_manifest);

        btn_capture_manifest = (Button) findViewById(R.id.btn_capture_manifest);
        btn_capture_manifest.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // capture picture
                if(et_manifest_dn.getText().toString().trim().length()==0){
                    Toast.makeText(getApplicationContext(),"Please enter Delivery Note ID", Toast.LENGTH_SHORT).show();
                } else {
                    // check if DN exists and not submitted
                    AsyncHttpClient client = new AsyncHttpClient();
                    RequestParams params = new RequestParams();
                    manifest_file_name = String.valueOf(et_manifest_dn.getText());
                    params.put("delivery_note_id",manifest_file_name);
                    db = new DatabaseHelper(getApplicationContext());
                    Auth loginProfile = db.getLoginProfile();
                    final String generatedURL = "http://"+loginProfile.getUrl()+"/api/method/erpnext_mobile_addons.check_dn_for_manifest";
                    try {
                        client.post(generatedURL,params,new JsonHttpResponseHandler(){

                            @Override
                            public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, org.json.JSONObject response) {
                                try {
                                    if(response.getString("message").equalsIgnoreCase("Completed") || response.getString("message").equalsIgnoreCase("To Bill")){
                                        Toast.makeText(getApplicationContext(),"DN is already submitted.", Toast.LENGTH_LONG).show();
                                    }else if(response.getString("message").equalsIgnoreCase("Cancelled") || response.getString("message").equalsIgnoreCase("Closed")){
                                        Toast.makeText(getApplicationContext(),"DN is cancelled/closed.", Toast.LENGTH_LONG).show();
                                    }else if(response.getString("message").equalsIgnoreCase("Draft")){
                                        captureImage();
                                    }
                                } catch (JSONException e) {
//                                    Log.d("VamCLog","Exception raised in 'check_dn_for_manifest' api call");
                                    e.printStackTrace();
                                }
                            }

                            public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, org.json.JSONObject response, Throwable throwable) {
                                Toast.makeText(getApplicationContext(),"check_dn_for_manifest error: ", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }catch (Exception e){
                        Toast.makeText(getApplicationContext(),"check_dn_for_manifest Exception: ", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

        btn_confirm_manifest.setOnClickListener(new View.OnClickListener() {
            File f;
            @Override
            public void onClick(View v) {
                /* >> Upload to google drive */
                manifest_file_name = String.valueOf(et_manifest_dn.getText());
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
                et_manifest_dn.setText("");
                et_manifest_dn.setVisibility(View.VISIBLE);
                iv_manifest_preview.setVisibility(View.GONE);
                btn_capture_manifest.setVisibility(View.VISIBLE);
                btn_confirm_manifest.setVisibility(View.GONE);
                btn_try_manifest.setVisibility(View.GONE);
            }
        });
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
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),IMAGE_DIRECTORY_NAME);
        manifest_file_name = String.valueOf(et_manifest_dn.getText());
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

            // disconnect Google Android Drive API connection.
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {

        // Called whenever the API client fails to connect.
//        Log.d(TAG, "GoogleApiClient connection failed: " + result.toString());

        if (!result.hasResolution()) {

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

            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);

        } catch (IntentSender.SendIntentException e) {

//            Log.d(TAG, "Exception while starting resolution activity", e);
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
//        Log.d(TAG,"Connected to Google Drive");
    }

    @Override
    public void onConnectionSuspended(int cause) {
//        Log.d(TAG, "GoogleApiClient connection suspended");
    }

    public void onClickCreateMethod(View view) {
        // create new contents resource
        Drive.DriveApi.newDriveContents(mGoogleApiClient)
                .setResultCallback(driveContentsCallback);
    }


    /**
     * This is Result result handler of Drive contents.
     * this callback method call CreateFileOnGoogleDrive() method
     * and also call OpenFileFromGoogleDrive() method,
     * send intent onActivityResult() method to handle result.
     */
    final ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback =
            new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult result) {

                    if (result.getStatus().isSuccess()) {
//                            CreateFileOnGoogleDrive(result);
                    }

                }
            };
    /**
     * Create a file in root folder using MetadataChangeSet object.
     * @param result
     */
    public void CreateFileOnGoogleDrive(DriveApi.DriveContentsResult result){

        final DriveContents driveContents = result.getDriveContents();

        // Perform I/O off the UI thread.
        new Thread() {
            @Override
            public void run() {
                // write content to DriveContents
                OutputStream outputStream = driveContents.getOutputStream();
                Writer writer = new OutputStreamWriter(outputStream);

                try {
                    writer.write("Hello VamC!");
                    writer.close();
                } catch (IOException e) {
//                    Log.d(TAG, e.getMessage());
                }

                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setTitle("VamCDriveTest")
                        .setMimeType("text/plain")
                        .setStarred(true).build();

                // create a file in root folder
                Drive.DriveApi.getRootFolder(mGoogleApiClient)
                        .createFile(mGoogleApiClient, changeSet, driveContents)
                        .setResultCallback(fileCallback);
            }
        }.start();
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
            params.put("dn_id",et_manifest_dn.getText().toString());
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
                            Log.d("VamCLog","In Exception");
                            e.printStackTrace();
                        }
                    }

                    public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, org.json.JSONObject response, Throwable throwable) {
                        Toast.makeText(getApplicationContext(),"check_dn_for_manifest error: ", Toast.LENGTH_SHORT).show();
                    }
                });
            }catch (Exception e){
                Toast.makeText(getApplicationContext(),"check_dn_for_manifest Exception: ", Toast.LENGTH_SHORT).show();
            }
        }
    };
    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback = new
            ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(DriveFolder.DriveFileResult result) {
                    if (result.getStatus().isSuccess()) {
                        result.getDriveFile().getDriveId().getResourceId();
                        Toast.makeText(getApplicationContext(), "file created: "+""+
                                result.getDriveFile().getDriveId(), Toast.LENGTH_LONG).show();

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
            tv_manifest_intro_label.setText("Confirm to link the manifest to "+et_manifest_dn.getText());
            tv_form_manifest_dn_label.setVisibility(View.GONE);
            et_manifest_dn.setVisibility(View.GONE);
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
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),IMAGE_DIRECTORY_NAME);
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();

        File file = new File(mediaStorageDir.getPath() + File.separator
                + filename + ".jpg");
        return file;
    }
}
