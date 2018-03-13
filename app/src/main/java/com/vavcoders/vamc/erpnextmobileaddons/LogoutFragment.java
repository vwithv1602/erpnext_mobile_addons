package com.vavcoders.vamc.erpnextmobileaddons;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.vavcoders.vamc.helper.DatabaseHelper;
import com.vavcoders.vamc.model.Auth;


public class LogoutFragment extends Fragment {
    View myView;
    Button btn_logout_confirm;
    DatabaseHelper db;
    String updated_value;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_logout, container, false);
        btn_logout_confirm = myView.findViewById(R.id.btn_logout_confirm);
        db = new DatabaseHelper(getActivity());


        btn_logout_confirm.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                /* >> Remove credentials in db */
                db = new DatabaseHelper(getActivity());
                db.removeLoginDetails();
                /* << Remove credentials in db */
                Intent intent = new Intent(getActivity(), SplashActivity.class);
                startActivity(intent);
            }
        });

        return myView;
    }


}
