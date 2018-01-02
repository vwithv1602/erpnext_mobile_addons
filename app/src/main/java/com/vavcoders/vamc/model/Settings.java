package com.vavcoders.vamc.model;

/**
 * Created by vamc on 1/2/18.
 */

public class Settings {
    int id;
    String config;
    String config_value;
    String created_on;
    String modified_on;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getConfig_value() {
        return config_value;
    }

    public void setConfig_value(String config_value) {
        this.config_value = config_value;
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

    public static String getTableSettings() {
        return TABLE_SETTINGS;
    }

    public static String getKeyId() {
        return KEY_ID;
    }

    public static String getKeyCreatedOn() {
        return KEY_CREATED_ON;
    }

    public static String getKeyModifiedOn() {
        return KEY_MODIFIED_ON;
    }

    public static String getKeyConfig() {
        return KEY_CONFIG;
    }

    public static String getKeyConfigValue() {
        return KEY_CONFIG_VALUE;
    }

    public static String getCreateTableSettings() {
        return CREATE_TABLE_SETTINGS;
    }

    // >> DATABASE RELATED
    // Table Names
    public static final String TABLE_SETTINGS = "settings";
    // Settings Table - column names
    public static final String KEY_ID = "id";
    public static final String KEY_CREATED_ON = "created_on";
    public static final String KEY_MODIFIED_ON = "modified_on";
    public static final String KEY_CONFIG = "config";
    public static final String KEY_CONFIG_VALUE = "config_value";

    // Settings table create statement
    public static final String CREATE_TABLE_SETTINGS = "CREATE TABLE IF NOT EXISTS "
            + TABLE_SETTINGS + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_CONFIG
            + " TEXT," + KEY_CONFIG_VALUE + " TEXT," + KEY_CREATED_ON
            + " DATETIME," + KEY_MODIFIED_ON + " DATETIME" + ")";
    // >> Configuration labels
    public static final String LEAD_SYNC = "LEAD_SYNC";
    // << Configuration labels
    // Inserting default settings
    public static final String INSERT_DEFAULT_SETTINGS = "INSERT INTO "+ TABLE_SETTINGS+" ('"+KEY_CONFIG+"','"+KEY_CONFIG_VALUE+"') VALUES('"+LEAD_SYNC+"','1')";
    // << DATABASE RELATED
}
