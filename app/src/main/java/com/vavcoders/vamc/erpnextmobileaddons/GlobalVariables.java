package com.vavcoders.vamc.erpnextmobileaddons;

import android.content.Context;

import com.vavcoders.vamc.helper.DatabaseHelper;
import com.vavcoders.vamc.model.Auth;

/**
 * Created by vamc on 12/13/17.
 */

public class GlobalVariables extends DatabaseHelper{

    /* Define menu options here*/
    public static final String[][] menu = new String[][]{
            {"ExportCallLogToLeadListActivity","Export Call log to lead list"},
            {"ManifestActivity","Manifest"}
    };

    public String URL;
    public String LOGGED_IN_USER;

    public GlobalVariables(Context context) {
        super(context);
        DatabaseHelper db = new DatabaseHelper(context);
        Auth loginProfile = db.getLoginProfile();
        URL = loginProfile.getUrl();
        LOGGED_IN_USER = loginProfile.getUname();
    }
}
