package com.ahmed.chatapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ahmed.chatapplication.connection.HttpVolleyRequest;
import com.ahmed.chatapplication.gcmservices.QuickstartPreferences;
import com.ahmed.chatapplication.gcmservices.RegistrationIntentService;
import com.ahmed.chatapplication.info.XmppServersInfo;
import com.ahmed.chatapplication.response.VolleyJsonResponse;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements VolleyJsonResponse{

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MainActivity";

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private ProgressBar mRegistrationProgressBar;
    private TextView mInformationTextView;
    private boolean isReceiverRegistered;
    private String token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRegistrationProgressBar = (ProgressBar) findViewById(R.id.registrationProgressBar);
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mRegistrationProgressBar.setVisibility(ProgressBar.GONE);
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences.getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    mInformationTextView.setText(getString(R.string.gcm_send_message));
                    token = sharedPreferences.getString(QuickstartPreferences.REGISTRATION_TOKEN, null);

                } else {
                    mInformationTextView.setText(getString(R.string.token_error_message));
                }
            }
        };
        mInformationTextView = (TextView) findViewById(R.id.informationTextView);

        // Registering BroadcastReceiver
        registerReceiver();

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        isReceiverRegistered = false;
        super.onPause();
    }

    private void registerReceiver(){
        if(!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
            isReceiverRegistered = true;
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    public void sendMsg(View v){
        EditText messageTxt = (EditText) findViewById(R.id.messageTxt);
        String message = messageTxt.getText().toString();
        //cld2v_P_x3Q:APA91bGBFWRJQ2CD1RqJ7giN2VgdIVgm-5kQa2EG7vJuUGk_xrT-B-19LceNeCvhMJs5Cxx4ZOKb5lNYbuX7-l_4e_xy_i89ijoL13XnD07kYXelLmfLCdRvlBhY_PEh2OSM6Ly5UfvY
        //dUfMWJ0qItg:APA91bG2Ure5wCElaD2GJ-LKQ_1faCyOvHjR0ewXopAX9FTjhWhXvvzZJ_6mTkjXTZpLi4n0V1AXmhFECgUesj9megUJjQXRD588eusG0o8rGZtDixF1dGW6KpeIl1lAQWcV5WT2pDd7
        //String to = "cld2v_P_x3Q:APA91bGBFWRJQ2CD1RqJ7giN2VgdIVgm-5kQa2EG7vJuUGk_xrT-B-19LceNeCvhMJs5Cxx4ZOKb5lNYbuX7-l_4e_xy_i89ijoL13XnD07kYXelLmfLCdRvlBhY_PEh2OSM6Ly5UfvY";
        sendGCMMessage(token, message);
    }

    public void sendGCMMessage(String to, String message){
        HttpVolleyRequest send = new HttpVolleyRequest();
        Map<String, String> params = new HashMap<String, String>();
        params.put("tag", "single_user");
        params.put("reg_id", to);
        params.put("message", message);
        send.makeRequest("req_send", XmppServersInfo.URL_REGISTER+"test.php", params);
        send.delegate = this;
    }

    @Override
    public void processFinish(JSONObject output) {
        Log.d(TAG, output.toString());
    }

    @Override
    public void processError(String error) {

    }
}
