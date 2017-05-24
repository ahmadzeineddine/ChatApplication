package com.ahmed.chatapplication.gcmservices;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;

import com.ahmed.chatapplication.R;
import com.ahmed.chatapplication.connection.HttpVolleyRequest;
import com.ahmed.chatapplication.info.XmppServersInfo;
import com.ahmed.chatapplication.response.VolleyJsonResponse;
import com.bilkoon.rooster.util.SessionManager;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Ahmed on 5/2/2017.
 */

public class RegistrationIntentService extends IntentService implements VolleyJsonResponse{
    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = {"global"};
    private SessionManager sessionManager;
    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        sessionManager = new SessionManager(this);
        //SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            // [START register_for_gcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
            // See https://developers.google.com/cloud-messaging/android/start for details on this file.
            // [START get_token]
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            // [END get_token]
            Log.i(TAG, "GCM Registration Token: " + token);
            // TODO: Implement this method to send any registration to your app's servers.
            sendRegistrationToServer(token);
            // Subscribe to topic channels
            subscribeTopics(token);
            // You should store a boolean that indicates whether the generated token has been
            // sent to your server. If the boolean is false, send the token to your server,
            // otherwise your server should have already received the token.
            //sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, true).apply();
            sessionManager.saveTokenSent(true);
            //sharedPreferences.edit().putString(QuickstartPreferences.REGISTRATION_TOKEN, token).apply();
            sessionManager.saveRegistrationToken(token);
            // [END register_for_gcm]EN_TO_SERVER
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            //sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false).apply();
            sessionManager.saveTokenSent(false);
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(QuickstartPreferences.REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.
        HttpVolleyRequest storeToken = new HttpVolleyRequest();
        Map<String, String> params = new HashMap<String, String>();
        params.put("tag", "save_reg_id");
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String imei = telephonyManager.getDeviceId();
        sessionManager.saveImei(imei);
        params.put("imei", imei);
        params.put("reg_id", token);
        storeToken.makeRequest("req_reg_id", XmppServersInfo.URL_REGISTER + "test.php", params);
        storeToken.delegate = this;
    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    // [START subscribe_topics]
    private void subscribeTopics(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }

    @Override
    public void processFinish(JSONObject output) {
        Log.d(TAG, output.toString());
        try {
            boolean error = output.getBoolean("error");
            if (!error) {
                String tag = output.getString("tag");
                if (tag.equals("save_reg_id")) {
                    sessionManager.saveServerId(output.getString("uid"));
                    Log.d(TAG, "Saved my server id "+sessionManager.loadServerId());
                }
            }else Log.d(TAG, output.getString("error_msg"));
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processError(String error) {

    }
    // [END subscribe_topics]
}
