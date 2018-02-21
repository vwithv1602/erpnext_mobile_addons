package com.vavcoders.vamc.erpnextmobileaddons;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
// import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.vavcoders.vamc.helper.DatabaseHelper;
import com.vavcoders.vamc.model.Auth;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Created by vamc on 1/2/18.
 */

public class ManifestFragment extends Fragment {
    View view;
    DatabaseHelper db;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;
    private static final String IMAGE_DIRECTORY_NAME = "ERPNextMobileAddons";
    private Uri fileUri;
    private Button btn_capture_manifest,btn_confirm_manifest,btn_try_manifest;
    private ImageView iv_manifest_preview;
    private TextView tv_manifest_intro_label,tv_form_manifest_dn_label;
    private static EditText et_manifest_dn;
    private static String manifest_file_name;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_manifest, container, false);
        btn_capture_manifest = (Button) view.findViewById(R.id.btn_capture_manifest);
        iv_manifest_preview = (ImageView) view.findViewById(R.id.iv_manifest_preview);
        et_manifest_dn = (EditText) view.findViewById(R.id.et_manifest_dn);
        tv_manifest_intro_label = (TextView) view.findViewById(R.id.tv_manifest_intro_label);
        tv_form_manifest_dn_label = (TextView) view.findViewById(R.id.tv_form_manifest_dn_label);
        btn_confirm_manifest = (Button) view.findViewById(R.id.btn_confirm_manifest);
        btn_try_manifest = (Button) view.findViewById(R.id.btn_try_manifest);
        btn_capture_manifest.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // capture picture
                captureImage();
            }
        });

        btn_confirm_manifest.setOnClickListener(new View.OnClickListener() {
        File f;
            @Override
            public void onClick(View v) {
                // confirm click event
                /*Log.d("getManifestPic","btn_confirm_manifest clicked");
                manifest_file_name = String.valueOf(et_manifest_dn.getText());
                f = getManifestPic(manifest_file_name);
                String content_type = getMimeType(f.getPath());
                Log.d("getManifestPic","content_type:" + content_type);
                String file_path = f.getAbsolutePath();
                Log.d("getManifestPic","file_path: "+file_path);
                db = new DatabaseHelper(getActivity());
                Auth loginProfile = db.getLoginProfile();
                String generatedURL = "http://"+loginProfile.getUrl()+"/api/method/erpnext_mobile_addons.upload_manifest";
                Log.d("getManifestPic","generatedURL: "+generatedURL);
                Log.d("getManifestPic","creating OkHTTPClient obj");
                OkHttpClient client = new OkHttpClient();
                Log.d("getManifestPic","creating file_body");
                RequestBody file_body = RequestBody.create(MediaType.parse(content_type),f);
                Log.d("getManifestPic","creating request_body");
                RequestBody request_body = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("type",content_type)
                        .addFormDataPart("uploaded_file",file_path.substring(file_path.lastIndexOf("/")+1), file_body)
                        .build();
                Log.d("getManifestPic","creating request");
                Log.d("getManifestPic",file_path.substring(file_path.lastIndexOf("/")+1));
                try {
                    Log.d("getManifestPic",String.valueOf(request_body.contentLength()));
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("getManifestPic","contentLenght exception");
                }
                Log.d("getManifestPic",request_body.toString());
                Request request = new Request.Builder()
                        .url(generatedURL)
                        .post(request_body)
                        .build();
                try {
                    Log.d("getManifestPic"," >> request");
                    Log.d("getManifestPic",request.method());
                    Log.d("getManifestPic",request.body().toString());
                    Log.d("getManifestPic",request.url().toString());
                    Log.d("getManifestPic"," << request");

                    Log.d("getManifestPic","creating response");
                    Response response = client.newCall(request).execute();
                    if(!response.isSuccessful()){
                        Log.d("getManifestPic","in response Exception");
                        throw new IOException("Error: "+response);
                    }
                } catch (IOException e) {
                    Log.d("getManifestPic","in response Exception catch");
                    e.printStackTrace();
                    Log.d("getManifestPic",String.valueOf(e.getMessage()));
                    Log.d("getManifestPic",String.valueOf(e.getCause()));
                }
                Log.d("getManifestPic","All Good");*/

                AsyncHttpClient client = new AsyncHttpClient();
                RequestParams params = new RequestParams();
                params.setForceMultipartEntityContentType(true);
                manifest_file_name = String.valueOf(et_manifest_dn.getText());
                try {
                    params.put("delivery_note_id",manifest_file_name);
                    params.put("manifest",getManifestPic(manifest_file_name));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    /*Log.d("getManifestPic",String.valueOf(e.toString())); */
                    Toast.makeText(getActivity(),"getManifestPic Exception", Toast.LENGTH_LONG).show();
                }
                db = new DatabaseHelper(getActivity());
                Auth loginProfile = db.getLoginProfile();
                Toast.makeText(getActivity(),"connecting to "+loginProfile.getUrl(), Toast.LENGTH_SHORT).show();
                String generatedURL = "http://"+loginProfile.getUrl()+"/api/method/erpnext_mobile_addons.upload_manifest";
                try {
                    client.post(generatedURL,params,new JsonHttpResponseHandler(){

                        @Override
                        public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, org.json.JSONObject response) {
                            Toast.makeText(getActivity(),"Manifest uploaded", Toast.LENGTH_SHORT).show();
                        }

                        public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, org.json.JSONObject response, Throwable throwable) {
                            Toast.makeText(getActivity(),"error: ", Toast.LENGTH_SHORT).show();
                        }
                    });
                }catch (Exception e){
                    Toast.makeText(getActivity(),"Exception: ", Toast.LENGTH_SHORT).show();
                }
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
        et_manifest_dn.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                enable_disable_capture(charSequence);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                enable_disable_capture(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        return view;
    }

    private String getMimeType(String path) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    private File getManifestPic(String filename) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),IMAGE_DIRECTORY_NAME);
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();

        File file = new File(mediaStorageDir.getPath() + File.separator
                + filename + ".jpg");
        return file;
    }

    private void enable_disable_capture(CharSequence charSequence) {
        if(charSequence.toString().trim().length()==0){
            btn_capture_manifest.setEnabled(false);
        } else {
            btn_capture_manifest.setEnabled(true);
        }
    }

    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }
    /**
     * returning image / video
     */
    private static File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),IMAGE_DIRECTORY_NAME);
        manifest_file_name = String.valueOf(et_manifest_dn.getText());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + manifest_file_name + ".jpg");
        return mediaFile;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // successfully captured the image
                // display it in image view
                previewCapturedImage();
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(getActivity(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to capture image
                Toast.makeText(getActivity(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }
    private void previewCapturedImage() {
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

            final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(),
                    options);

            iv_manifest_preview.setImageBitmap(bitmap);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}
