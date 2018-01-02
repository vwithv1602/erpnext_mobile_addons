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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Created by vamc on 1/2/18.
 */

public class ManifestFragment extends Fragment {
    View view;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;
    private static final String IMAGE_DIRECTORY_NAME = "ERPNextMobileAddons";
    private Uri fileUri;
    private Button btn_capture_manifest,btn_confirm_manifest,btn_try_manifest;
    private ImageView iv_manifest_preview;
    private TextView tv_manifest_intro_label,tv_form_manifest_dn_label;
    private EditText et_manifest_dn;
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

            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),
                        "Post image to API", Toast.LENGTH_SHORT)
                        .show();
                // confirm click event


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
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
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
