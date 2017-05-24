package com.bilkoon.rooster.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Ahmed on 5/7/2017.
 */

public class ConversatianDB extends SQLiteOpenHelper {

    public static final String TAG = "ConversatianDB";
    public static final String TABLE_CONVERSATIONS = "conversation";
    public static final String TABLE_GCMS = "gcm";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_ME_BUDDY = "me_buddy";
    public static final String COLUMN_IS_MINE = "is_mine";
    public static final String COLUMN_IS_IMAGE = "is_image";
    public static final String COLUMN_STANZA_ID = "stanza_id";
    public static final String COLUMN_MESSAGE_STATUS = "message_status";
    public static final String COLUMN_MESSAGE = "message";
    public static final String COLUMN_TIME = "time";

    public static final String COLUMN_GCM_ID = "id";
    public static final String COLUMN_GCM_UID = "uid";
    public static final String COLUMN_GCM_IMEI = "imei";
    public static final String COLUMN_GCM_REG_ID = "reg_id";
    public static final String COLUMN_GCM_TIME = "timestamp";

    private static final String DATABASE_NAME = "chat.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_CONVERSATIONS + "( " + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_ME_BUDDY
            + " text not null, " + COLUMN_IS_MINE +" integer not null, "
            + COLUMN_IS_IMAGE + " integer not null, " + COLUMN_STANZA_ID + " text not null, "
            + COLUMN_MESSAGE + " text not null, " + COLUMN_MESSAGE_STATUS + " text not null, "
            + COLUMN_TIME + " text not null );";

    // Database creation sql statement
    private static final String CREATE_GCMS = "create table "
            + TABLE_GCMS + "( " + COLUMN_GCM_ID
            + " integer primary key autoincrement, " + COLUMN_GCM_UID + " integer not null, "
            + COLUMN_GCM_IMEI + " text not null, " + COLUMN_GCM_REG_ID + " text not null, "
            + COLUMN_GCM_TIME + " integer not null );";

    public ConversatianDB(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
        db.execSQL(CREATE_GCMS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG,
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONVERSATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GCMS);
        onCreate(db);
    }
}
