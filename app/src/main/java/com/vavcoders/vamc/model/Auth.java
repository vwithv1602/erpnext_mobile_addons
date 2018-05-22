package com.vavcoders.vamc.model;

/**
 * Created by vamc on 12/20/17.
 */

public class Auth {
    int id;
    String uname;
    String pwd;
    String fullname;
    Boolean is_logged_in;
    String url;
    String created_on;
    String modified_on;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public Boolean getIs_logged_in() {
        return is_logged_in;
    }

    public void setIs_logged_in(Boolean is_logged_in) {
        this.is_logged_in = is_logged_in;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCreated_on() {
        return created_on;
    }

    public void setCreated_on(String created_on) {
        this.created_on = created_on;
    }

    public String getModified_on() {
        return modified_on;
    }

    public void setModified_on(String modified_on) {
        this.modified_on = modified_on;
    }

    // >> DATABASE RELATED
    // Table Names
    public static final String TABLE_LOGIN = "login";
    // Login Table - column names
    public static final String KEY_ID = "id";
    public static final String KEY_CREATED_ON = "created_on";
    public static final String KEY_MODIFIED_ON = "modified_on";
    public static final String KEY_UNAME = "uname";
    public static final String KEY_PWD = "pwd";
    public static final String KEY_FULLNAME = "fullname";
    public static final String KEY_IS_LOGGED_IN = "is_logged_in";
    public static final String KEY_URL = "url";

    // Login table create statement
    public static final String CREATE_TABLE_LOGIN = "CREATE TABLE IF NOT EXISTS "
            + TABLE_LOGIN + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_UNAME
            + " TEXT," + KEY_PWD + " TEXT," + KEY_FULLNAME + " TEXT," + KEY_IS_LOGGED_IN + " INTEGER," + KEY_URL + " TEXT," + KEY_CREATED_ON
            + " DATETIME," + KEY_MODIFIED_ON + " DATETIME" + ")";
    // << DATABASE RELATED
}
