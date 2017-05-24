package com.bilkoon.rooster.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import com.bilkoon.rooster.connection.RoosterConnection;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.IOException;

/**
 * Created by gakwaya on 4/28/2016.
 */
public class RoosterConnectionService extends Service {
    private static final String TAG ="RoosterService";

    public static final String UI_REGISTERED = "com.blikoon.rooster.uiregistered";
    public static final String CAN_SEND_MESSAGE = "com.blikoon.rooster.uicansendmessage";
    public static final String UI_AUTHENTICATED = "com.blikoon.rooster.uiauthenticated";
    public static final String SAVE_VCARD = "com.blikoon.rooster.savevcard";
    public static final String LOAD_VCARD = "com.blikoon.rooster.loadvcard";
    public static final String VCARD_SAVED = "com.blikoon.rooster.vcardsaved";
    public static final String VCARD_LOADED = "com.blikoon.rooster.vcardloaded";
    public static final String SEND_MESSAGE = "com.blikoon.rooster.sendmessage";
    public static final String SEND_IMAGE = "com.blikoon.rooster.sendimage";
    public static final String RECEIVING_FILE = "com.blikoon.rooster.receivefile";
    public static final String SEND_FILE = "com.blikoon.rooster.sendfile";
    public static final String DISCONNECT = "com.blikoon.rooster.disconnect";
    public static final String ADD_FRIEND = "com.blikoon.rooster.addfriend";
    public static final String FRIEND_ADDED = "com.blikoon.rooster.friendadded";
    public static final String REMOVE_FRIEND = "com.blikoon.rooster.removefriend";

    public static final String BLOCK_LIST = "com.blikoon.rooster.blocklist";
    public static final String BLOCK_FRIEND = "com.blikoon.rooster.blockfriend";
    public static final String FRIEND_BLOCKED = "com.blikoon.rooster.friendblocked";
    public static final String UNBLOCK_FRIEND = "com.blikoon.rooster.unblockfriend";
    public static final String FRIEND_UNBLOCKED = "com.blikoon.rooster.friendunblocked";

    public static final String FRIEND_REMOVED = "com.blikoon.rooster.friendremoved";
    public static final String GET_USER_PRESENCE = "com.blikoon.rooster.getuserpresence";
    public static final String SET_MY_PRESENCE = "com.blikoon.rooster.setmypresence";
    public static final String CONTACT_AVAILABLE = "com.blikoon.rooster.contactavailable";

    public static final String BUNDLE_MESSAGE_BODY = "b_body";
    public static final String BUNDLE_TO = "b_to";

    public static final String CONTACT_LIST_SERVER = "com.blikoon.rooster.contactlistserver";
    public static final String CONTACT_LIST = "com.blikoon.rooster.contactlist";
    public static final String CONTACT_PROFILES = "com.blikoon.rooster.contactprofiles";
    public static final String NEW_MESSAGE = "com.blikoon.rooster.newmessage";
    public static final String NEW_IMAGE = "com.blikoon.rooster.newimage";
    public static final String USER_PRESENCE_RESULT = "com.blikoon.rooster.userpresenceresult";
    public static final String BUNDLE_FROM_JID = "b_from";

    public static final String CURRENT_CONVERSATION = "com.blikoon.rooster.currentconversation";
    public static final String ME_IS_COMPOSING = "com.blikoon.rooster.meiscomposing";
    public static final String ME_HAS_STOPPED_COMPOSING = "com.blikoon.rooster.mehasstoppedcomposing";
    public static final String BUDDY_IS_COMPOSING = "com.blikoon.rooster.buddyiscomposing";
    public static final String BUDDY_HAS_STOPPED_COMPOSING = "com.blikoon.rooster.buddyhasstoppedcomposing";

    public static final String MESSAGE_SENT = "com.blikoon.rooster.messagesent";
    public static final String MESSAGE_DELIVERED = "com.blikoon.rooster.messagedelivered";
    public static final String MESSAGE_READ = "com.blikoon.rooster.messageread";

    public static final String IMAGE_SENT = "com.blikoon.rooster.imagesent";
    public static final String IMAGE_DELIVERED = "com.blikoon.rooster.imagedelivered";

    public static final String VCARD_EVENT = "com.blikoon.rooster.vcardevent";
    public static final String APP_IN_BACKGROUND = "com.blikoon.rooster.appinbackground";
    public static final String APP_IN_FOREGROUND = "com.blikoon.rooster.aappinforeground";

    public static final String CHECK_HOME_BUTTON_PRESSED = "com.blikoon.rooster.checkhomebuttonpressed";

    public static RoosterConnection.ConnectionState sConnectionState;
    public static RoosterConnection.LoggedInState sLoggedInState;
    private boolean mActive;//Stores whether or not the thread is active
    private Thread mThread;
    private Handler mTHandler;//We use this handler to post messages to
    //the background thread.
    public RoosterConnection mConnection;
    private boolean doRegister = false;

    private BroadcastReceiver mBroadcastReceiver;
    public static RoosterConnectionService roosterConnectionService;

    public RoosterConnectionService() {

    }
    public static RoosterConnection.ConnectionState getState(){
        if (sConnectionState == null){
            return RoosterConnection.ConnectionState.DISCONNECTED;
        }
        return sConnectionState;
    }

    public static RoosterConnection.LoggedInState getLoggedInState(){
        if (sLoggedInState == null)
        {
            return RoosterConnection.LoggedInState.LOGGED_OUT;
        }
        return sLoggedInState;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"onCreate()");
        roosterConnectionService = this;
    }

    private void initConnection(){
        Log.d(TAG,"initConnection()");
        if( mConnection == null){
            mConnection = new RoosterConnection(this);
        }
        try{
            if(!doRegister) mConnection.login();
            else mConnection.register();
        }catch (IOException |SmackException |XMPPException e){
            Log.d(TAG,"Something went wrong while connecting ,make sure the credentials are right and try again");
            e.printStackTrace();
            //Stop the service all together.
            stopSelf();
        }

    }


    public void start(){
        Log.d(TAG," Service Start() function called.");
        if(!mActive){
            mActive = true;
            if( mThread ==null || !mThread.isAlive()){
                mThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        mTHandler = new Handler();
                        initConnection();
                        //THE CODE HERE RUNS IN A BACKGROUND THREAD.
                        Looper.loop();
                    }
                });
                mThread.start();
            }
            return;
        }if(mActive){
            if(mConnection!=null){
                if(mThread.isAlive())  mThread.interrupt();
                mThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        mTHandler = new Handler();
                        initConnection();
                        //THE CODE HERE RUNS IN A BACKGROUND THREAD.
                        Looper.loop();
                    }
                });
                mThread.start();
            }
        }
    }

    public void stop(){
        Log.d(TAG,"stop()");
        mActive = false;
        mTHandler.post(new Runnable() {
            @Override
            public void run() {
                if( mConnection != null){
                    mConnection.disconnect(false);
                }
            }
        });

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"onStartCommand()");
        try {
            Log.d(TAG, "Do register ? " + intent.getBooleanExtra("register", true));
            doRegister = intent.getBooleanExtra("register", false);
        }catch (Exception e){
            e.printStackTrace();
        }
        start();
        return Service.START_STICKY;
        //RETURNING START_STICKY CAUSES OUR CODE TO STICK AROUND WHEN THE APP ACTIVITY HAS DIED.
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"onDestroy()");
        super.onDestroy();
        stop();
    }

}
