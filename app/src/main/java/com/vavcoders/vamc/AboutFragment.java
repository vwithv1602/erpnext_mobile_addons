package com.vavcoders.vamc;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vavcoders.vamc.erpnextmobileaddons.R;

/**
 * Created by vamc on 1/2/18.
 */

public class AboutFragment extends Fragment {
    View myView;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.about_layout, container, false);
        return myView;
    }
}
