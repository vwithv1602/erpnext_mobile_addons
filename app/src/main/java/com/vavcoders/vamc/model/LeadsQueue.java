package com.vavcoders.vamc.model;

/**
 * Created by vamc on 3/13/18.
 */

public class LeadsQueue {
    int id;
    String calllog;
    String created_by;
    String created_on;
    String modified_on;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCalllog() {
        return calllog;
    }

    public void setCalllog(String calllog) {
        this.calllog = calllog;
    }

    public String getCreated_by() {
        return created_by;
    }

    public void setCreated_by(String created_by) {
        this.created_by = created_by;
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

    public static String getTableLeadsQueue() {
        return TABLE_LEADS_QUEUE;
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

    public static String getKeyCalllog() {
        return KEY_CALLLOG;
    }

    public static String getKeyCreatedBy() {
        return KEY_CREATED_BY;
    }

    public static String getCreateTableLeadsQueue() {
        return CREATE_TABLE_LEADS_QUEUE;
    }

    // >> DATABASE RELATED
    // Table Names
    public static final String TABLE_LEADS_QUEUE = "leads_queue";
    // Login Table - column names
    public static final String KEY_ID = "id";
    public static final String KEY_CREATED_ON = "created_on";
    public static final String KEY_MODIFIED_ON = "modified_on";
    public static final String KEY_CALLLOG = "calllog";
    public static final String KEY_CREATED_BY = "created_by";

    // Login table create statement
    public static final String CREATE_TABLE_LEADS_QUEUE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_LEADS_QUEUE + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_CALLLOG
            + " TEXT," +KEY_CREATED_BY + " TEXT," + KEY_CREATED_ON + " DATETIME,"
            + KEY_MODIFIED_ON + " DATETIME" + ")";
    // << DATABASE RELATED

}
