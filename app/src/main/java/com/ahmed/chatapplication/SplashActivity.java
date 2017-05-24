package com.ahmed.chatapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;


import com.ahmed.chatapplication.connection.HttpVolleyRequest;

import com.ahmed.chatapplication.info.XmppServersInfo;
import com.ahmed.chatapplication.response.VolleyJsonResponse;
import com.bilkoon.rooster.database.ConversationDataSource;
import com.bilkoon.rooster.model.GcmInfo;
import com.bilkoon.rooster.util.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Ahmed on 4/15/2017.
 */

public class SplashActivity extends AppCompatActivity implements VolleyJsonResponse {
    public static final String TAG = "SplashActivity";

    private RelativeLayout pLayout;
    //private boolean backLogin = false;
    private SessionManager sessionManager;

    ConversationDataSource datasource;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Log.d(TAG, "Splash is starting...");
        //Logger.d(TAG, "Splash is starting...");
        //Logger.d(TAG, this);
        //Logger.e(TAG, "123", new NullPointerException());
        pLayout = (RelativeLayout) findViewById(R.id.pLayout);
        sessionManager = new SessionManager(this);

        //comment this after application testing is finished!
        sessionManager.saveIsFirstApplicationRun(true);

        datasource = new ConversationDataSource(SplashActivity.this);

    }


    @Override
    protected void onResume() {
        super.onResume();
        //backLogin = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("back_login_pressed",false);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                getUsers();
            }
        }, 1200);
    }


    public void getUsers() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pLayout.setVisibility(View.VISIBLE);
            }
        });
        HttpVolleyRequest getJids = new HttpVolleyRequest();
        Map<String, String> params = new HashMap<String, String>();
        params.put("tag", "get_users");
        getJids.makeRequest("req_get", XmppServersInfo.URL_REGISTER + "test.php", params);
        getJids.delegate = this;
    }

    @Override
    public void processFinish(JSONObject output) {
        Log.d(TAG, "Getting registered users : " + output.toString());
        try {
            boolean error = output.getBoolean("error");
            if (!error) {
                String tag = output.getString("tag");
                Set<String> jids = new HashSet<String>();
                if (tag.equals("get_users")) {
                    JSONArray data = output.getJSONArray("data");
                    int ln = data.length();
                    Log.d(TAG, "GETTING USERS\n----------------------------------\n");
                    for (int i = 0; i < ln; i++) {
                        JSONObject userObj = (JSONObject) data.get(i);
                        String id = userObj.getString("id");
                        String uid = userObj.getString("uid");
                        String jid = userObj.getString("jid");
                        String name = userObj.getString("name");
                        sessionManager.saveServerVCardTimestamp(jid, userObj.getString("vcardtimestamp"));
                        sessionManager.saveIdOfJid(jid, uid);
                        jids.add(jid);
                        Log.d(TAG, "|Data :\n" + "|ID---> " + id + "\n|JID---> " + jid + "\n|NAME---> " + name);
                    }
                    Log.d(TAG, "|-------------------------------------------------|\n");
                    getUpdatedGCMSFromServer();
                    sessionManager.saveServerJids(jids);
                }else if(tag.equals("get_updated_gcms")){
                    JSONArray gcm = output.getJSONArray("gcm");
                    datasource.open();
                    int len = gcm.length();
                    for (int i = 0; i < len; i++) {
                        JSONObject gcmObj = (JSONObject) gcm.get(i);
                        Log.d(TAG, "GCM for [" + gcmObj.getString("id") + "] is : " + gcmObj.getString("reg_id") + "\n");
                        GcmInfo gcmInfo = new GcmInfo();
                        gcmInfo.setUid(Integer.parseInt(gcmObj.getString("id")));
                        gcmInfo.setImei(gcmObj.getString("imei"));
                        gcmInfo.setRegId(gcmObj.getString("reg_id"));
                        gcmInfo.setTimestamp(Long.parseLong(gcmObj.getString("timestamp")));
                        datasource.createGcmInfo(gcmInfo);
                    }
                    datasource.close();
                }

                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
            } else {
                Log.d(TAG, "Cannot start the application!");
                finish();
            }
            pLayout.setVisibility(View.GONE);
        } catch (JSONException e) {
            e.printStackTrace();
            pLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void processError(String error) {
        pLayout.setVisibility(View.GONE);
        if (error != null) {
            if (error.contains("failed to connect")) {
                Log.d(TAG, "Connection failure!");
                finish();
            }
        } else {
            finish();
        }
    }

    public void getUpdatedGCMSFromServer(){
        HttpVolleyRequest getUpdatedGcms = new HttpVolleyRequest();
        Map<String, String> params = new HashMap<String, String>();
        params.put("tag", "get_updated_gcms");
        params.put("timestamp", "0");
        try {
            if (sessionManager.loadIsFirstApplicationRun()) params.put("timestamp", "0");
        }catch (Exception e){
            params.put("timestamp", "0");
            getUpdatedGcms.makeRequest("req_getUGcms", XmppServersInfo.URL_REGISTER + "test.php", params);
            getUpdatedGcms.delegate = this;
            return;
        }
        getUpdatedGcms.makeRequest("req_getUGcms", XmppServersInfo.URL_REGISTER + "test.php", params);
        getUpdatedGcms.delegate = this;
    }
}