package com.bilkoon.rooster.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.bilkoon.rooster.model.ChatMessage;
import com.bilkoon.rooster.model.Conversation;
import com.bilkoon.rooster.model.ConversationMessage;
import com.bilkoon.rooster.model.GcmInfo;

import java.util.ArrayList;

/**
 * Created by Ahmed on 5/7/2017.
 */

public class ConversationDataSource {
    private static final String TAG = "ConversationDataSource";
    // Database fields
    private SQLiteDatabase database;
    private ConversatianDB dbHelper;
    private String[] allColumns = { ConversatianDB.COLUMN_ID, ConversatianDB.COLUMN_ME_BUDDY,
            ConversatianDB.COLUMN_IS_MINE, ConversatianDB.COLUMN_IS_IMAGE,
            ConversatianDB.COLUMN_STANZA_ID, ConversatianDB.COLUMN_MESSAGE, ConversatianDB.COLUMN_MESSAGE_STATUS, ConversatianDB.COLUMN_TIME };

    private String[] allGcmColumns = { ConversatianDB.COLUMN_GCM_ID, ConversatianDB.COLUMN_GCM_UID,
            ConversatianDB.COLUMN_GCM_IMEI, ConversatianDB.COLUMN_GCM_REG_ID, ConversatianDB.COLUMN_GCM_TIME};

    public ConversationDataSource(Context context){
        dbHelper = new ConversatianDB(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public ConversationMessage createMessage(String me_buddy, ChatMessage message) {
        ContentValues values = new ContentValues();
        values.put(ConversatianDB.COLUMN_ME_BUDDY, me_buddy);
        int isMine = 0;
        if(message.isMine()) isMine = 1;
        values.put(ConversatianDB.COLUMN_IS_MINE, isMine);
        int isImage = 0;
        if(message.isImage()) isImage = 1;
        values.put(ConversatianDB.COLUMN_IS_IMAGE, isImage);
        values.put(ConversatianDB.COLUMN_STANZA_ID, message.getStanzaId());
        values.put(ConversatianDB.COLUMN_MESSAGE, message.getContent());
        values.put(ConversatianDB.COLUMN_MESSAGE_STATUS, message.getMessageStatus());
        values.put(ConversatianDB.COLUMN_TIME, message.getTime());
        long insertId = database.insert(ConversatianDB.TABLE_CONVERSATIONS, null,
                values);
        Cursor cursor = database.query(ConversatianDB.TABLE_CONVERSATIONS,
                allColumns, ConversatianDB.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        ConversationMessage newMessage = cursorToConversationMessage(cursor);
        cursor.close();
        return newMessage;
    }

    public ConversationMessage updateMessageStatus(long id, String status){
        Log.d(TAG, "Updating message status");
        ContentValues cv = new ContentValues();
        cv.put(ConversatianDB.COLUMN_MESSAGE_STATUS,status);
        database.update(ConversatianDB.TABLE_CONVERSATIONS , cv, ConversatianDB.COLUMN_ID + " = "+id, null);

        String query = "SELECT * FROM " + ConversatianDB.TABLE_CONVERSATIONS + " WHERE " + ConversatianDB.COLUMN_ID + " = " + id;
        Cursor cursor = database.rawQuery(query, null);
        ConversationMessage updatedMessage = null;
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            updatedMessage = cursorToConversationMessage(cursor);
            cursor.moveToNext();
        }
        cursor.close();
        return updatedMessage;
    }

    public ConversationMessage updateMessageImagePathByStanzaId(String stanza_id, String message){
        Log.d(TAG, "Updating message content");
        String query = "SELECT * FROM " + ConversatianDB.TABLE_CONVERSATIONS + " WHERE " + ConversatianDB.COLUMN_STANZA_ID + " = '" + stanza_id +"'";
        Cursor cursor = database.rawQuery(query, null);
        ConversationMessage updatedMessage = null;
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            updatedMessage = cursorToConversationMessage(cursor);
            cursor.moveToNext();
        }
        if(updatedMessage==null) return  updatedMessage;
        long id = updatedMessage.getId();
        ContentValues cv = new ContentValues();
        cv.put(ConversatianDB.COLUMN_MESSAGE, message);
        database.update(ConversatianDB.TABLE_CONVERSATIONS , cv, ConversatianDB.COLUMN_ID + " = "+id, null);
        updatedMessage.setContent(message);
        cursor.close();
        return updatedMessage;
    }

    public ConversationMessage updateMessageStatusByStanzaId(String stanza_id, String status){
        Log.d(TAG, "Updating message status");

        String query = "SELECT * FROM " + ConversatianDB.TABLE_CONVERSATIONS + " WHERE " + ConversatianDB.COLUMN_STANZA_ID + " = '" + stanza_id +"'";
        Cursor cursor = database.rawQuery(query, null);
        ConversationMessage updatedMessage = null;
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            updatedMessage = cursorToConversationMessage(cursor);
            cursor.moveToNext();
        }
        if(updatedMessage==null) return  updatedMessage;
        long id = updatedMessage.getId();
        ContentValues cv = new ContentValues();
        cv.put(ConversatianDB.COLUMN_MESSAGE_STATUS, status);
        database.update(ConversatianDB.TABLE_CONVERSATIONS , cv, ConversatianDB.COLUMN_ID + " = "+id, null);
        updatedMessage.setMessageStatus(status);
        cursor.close();
        return updatedMessage;
    }

    public void deleteMessage(ConversationMessage message) {
        long id = message.getId();
        System.out.println("Message deleted with id: " + id);
        database.delete(ConversatianDB.TABLE_CONVERSATIONS, ConversatianDB.COLUMN_ID
                + " = " + id, null);
    }

    public ArrayList<ChatMessage> getConversationByMeBuddy(String me_buddy){
        Log.d(TAG, "Getting messages of ["+me_buddy+"]");
        ArrayList<ChatMessage> messages = new ArrayList<ChatMessage>();

        String query = "SELECT * FROM " + ConversatianDB.TABLE_CONVERSATIONS + " WHERE " + ConversatianDB.COLUMN_ME_BUDDY + " = '" + me_buddy + "'";
        Cursor cursor = database.rawQuery(query, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ConversationMessage message = cursorToConversationMessage(cursor);
            ChatMessage chatMessage = (ChatMessage) message;
            messages.add(chatMessage);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return messages;
    }

    public ArrayList<String> getMeBuddyColumn(){
        Log.d(TAG, "Getting jids for all buddies");
        ArrayList<String> buddies = new ArrayList<String>();

        String query = "SELECT me_buddy FROM " + ConversatianDB.TABLE_CONVERSATIONS;
        Cursor cursor = database.rawQuery(query, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            buddies.add(cursor.getString(0));
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return buddies;
    }

    public ArrayList<ConversationMessage> getAllConversations() {
        ArrayList<ConversationMessage> messages = new ArrayList<ConversationMessage>();

        Cursor cursor = database.query(ConversatianDB.TABLE_CONVERSATIONS,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ConversationMessage message = cursorToConversationMessage(cursor);
            messages.add(message);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return messages;
    }

    private ConversationMessage cursorToConversationMessage(Cursor cursor) {
        ConversationMessage message = new ConversationMessage();
        message.setId(cursor.getLong(0));
        message.setMeBuddy(cursor.getString(1));
        boolean isMine = false;
        if(cursor.getInt(2)==1) isMine= true;
        message.setIsMine(isMine);
        boolean isImage = false;
        if(cursor.getInt(3)==1) isImage= true;
        message.setIsImage(isImage);
        message.setStanzaId(cursor.getString(4));
        message.setContent(cursor.getString(5));
        message.setMessageStatus(cursor.getString(6));
        message.setTime(cursor.getString(7));
        return message;
    }

    //-----------------GCM Methods-------------------//
    public GcmInfo createGcmInfo(GcmInfo gcmInfo){
        ContentValues values = new ContentValues();
        long id = getGcmUid(gcmInfo.getUid());
        Log.d(TAG, "ID is found ? " + id);
        if(id==-1) {
            values.put(ConversatianDB.COLUMN_GCM_UID, gcmInfo.getUid());
            values.put(ConversatianDB.COLUMN_GCM_IMEI, gcmInfo.getImei());
            values.put(ConversatianDB.COLUMN_GCM_REG_ID, gcmInfo.getRegId());
            values.put(ConversatianDB.COLUMN_GCM_TIME, gcmInfo.getTimestamp());
            long insertId = database.insert(ConversatianDB.TABLE_GCMS, null,
                    values);
            Cursor cursor = database.query(ConversatianDB.TABLE_GCMS,
                    allGcmColumns, ConversatianDB.COLUMN_GCM_ID + " = " + insertId, null,
                    null, null, null);
            cursor.moveToFirst();
            GcmInfo newGcmInfo = cursorToGCMInfo(cursor);
            cursor.close();
            return newGcmInfo;
        }else{
            Log.d(TAG, "Updating gcm info!");
            ContentValues cv = new ContentValues();
            cv.put(ConversatianDB.COLUMN_GCM_REG_ID, gcmInfo.getRegId());
            database.update(ConversatianDB.TABLE_GCMS , cv, ConversatianDB.COLUMN_GCM_ID + " = "+id, null);
        }
        return null;
    }

    public long getGcmUid(int uid){
        Cursor cursor = null;
        long id = -1;
        try{
            cursor = database.rawQuery("SELECT id FROM "+ConversatianDB.TABLE_GCMS+" WHERE uid = " + uid, null);
            if(cursor.getCount()>0) {
                cursor.moveToFirst();
                id = cursor.getLong(0);
                return id;
            }
        }finally {
            cursor.close();
        }
        return id;
    }

    public String getGcmByUid(String id){
        Cursor cursor = null;
        String gcm = null;
        int uid = Integer.parseInt(id);
        try{
            cursor = database.rawQuery("SELECT reg_id FROM "+ConversatianDB.TABLE_GCMS+" WHERE uid = " + uid, null);
            if(cursor.getCount()>0) {
                cursor.moveToFirst();
                gcm = cursor.getString(0);
                return gcm;
            }
        }finally {
            cursor.close();
        }
        return gcm;
    }

    private GcmInfo cursorToGCMInfo(Cursor cursor){
        GcmInfo gcmInfo = new GcmInfo();
        gcmInfo.setId(cursor.getLong(0));
        gcmInfo.setUid(cursor.getInt(1));
        gcmInfo.setImei(cursor.getString(2));
        gcmInfo.setRegId(cursor.getString(3));
        gcmInfo.setTimestamp(cursor.getLong(4));
        return gcmInfo;
    }
}
