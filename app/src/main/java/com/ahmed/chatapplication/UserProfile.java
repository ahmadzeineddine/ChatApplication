package com.ahmed.chatapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.ahmed.chatapplication.connection.HttpVolleyRequest;
import com.ahmed.chatapplication.info.XmppServersInfo;
import com.ahmed.chatapplication.response.VolleyJsonResponse;
import com.bilkoon.rooster.service.RoosterConnectionService;
import com.bilkoon.rooster.util.SessionManager;
import com.bumptech.glide.Glide;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class UserProfile extends AppCompatActivity implements VolleyJsonResponse {

    private static String TAG = "UserProfile";

    private String myJid;
    ImageView profilePic;
    EditText nameViewTxt, statusViewTxt;
    private BroadcastReceiver mBroadcastReceiver;

    private static int RC_CODE_PROFILE_PICKER = 1;
    private ArrayList<Image> images = new ArrayList<>();
    SessionManager sessionManager;
    private String image, currentImage;
    private boolean isChoosingImage = false, hasChosenPpic = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        sessionManager = new SessionManager(this);
        myJid = getIntent().getStringExtra("my_jid");

        nameViewTxt = (EditText) findViewById(R.id.nameViewTxt);
        statusViewTxt = (EditText) findViewById(R.id.statusViewTxt);
        profilePic = (ImageView) findViewById(R.id.profileViewPicImage);
        currentImage = sessionManager.loadMyImagePath();
    }


    // Recommended builder
    public void start() {
        isChoosingImage = true;
        boolean isSingleMode = true;
        ImagePicker imagePicker = ImagePicker.create(this)
                .returnAfterFirst(false) // set whether pick action or camera action should return immediate result or not. Only works in single mode for image picker
                .folderMode(true) // set folder mode (false by default)
                .folderTitle("Folder") // folder selection title
                .imageTitle("Tap to select"); // image selection title

        if (isSingleMode) {
            imagePicker.single();
        } else {
            imagePicker.multi(); // multi mode (default mode)
        }

        imagePicker.limit(10) // max images can be selected (99 by default)
                .showCamera(true) // show camera or not (true by default)
                .imageDirectory("Camera")   // captured image directory name ("Camera" folder by default)
                .origin(images) // original selected images, used in multi mode
                .start(RC_CODE_PROFILE_PICKER); // start image picker activity with request code
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, final Intent data) {
        Log.d(TAG, "Result code : " + resultCode);
        if (requestCode == RC_CODE_PROFILE_PICKER && resultCode == RESULT_OK && data != null) {
            isChoosingImage = true;
            hasChosenPpic = true;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    images = (ArrayList<Image>) ImagePicker.getImages(data);
                    Log.d(TAG, "Image path ["+images.get(0).getPath()+"]");
                    image = images.get(0).getPath();
                    sessionManager.saveMyImagePath(image);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(getApplicationContext())
                                    .load(new File(image))
                                    .into(profilePic);
                        }
                    });
                }
            }, 300);
            return;
        }else hasChosenPpic = false;
    }

    public void choosePicClick(View v){
        start();
    }

    public void saveProfileClick(View v){
        Intent saveProfile = new Intent(RoosterConnectionService.SAVE_VCARD);
        saveProfile.putExtra("jid", myJid);
        saveProfile.putExtra("name", nameViewTxt.getText().toString());
        if(hasChosenPpic) saveProfile.putExtra("ppic", image);
        else {
            saveProfile.putExtra("ppic", currentImage);
        }
        saveProfile.putExtra("status", statusViewTxt.getText().toString());
        sendBroadcast(saveProfile);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
    }

    public void updateMyVcardOnServer(){
        HttpVolleyRequest updatevcard = new HttpVolleyRequest();
        Map<String, String> params = new HashMap<String, String>();
        params.put("tag", "update_user_vcard");
        params.put("jid", myJid);
        updatevcard.makeRequest("req_update", XmppServersInfo.URL_REGISTER + "test.php", params);
        updatevcard.delegate = this;
    }
    @Override
    protected void onResume() {
        super.onResume();
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                String action = intent.getAction();
                Log.d(TAG, "Action : " + action);
                if (action.equals(RoosterConnectionService.VCARD_SAVED)) {
                    //startActivity(new Intent(UserProfile.this, ContactListActivity.class));
                    //finish();
                    updateMyVcardOnServer();
                }else if(action.equals(RoosterConnectionService.VCARD_LOADED)){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String name = intent.getStringExtra("name");
                            String status = intent.getStringExtra("status");
                            String image = null;
                            if(intent.getStringExtra("ppic")!=null) {
                                image = intent.getStringExtra("ppic");
                            }
                            nameViewTxt.setText(name);
                            statusViewTxt.setText(status);
                            try {
                                if(image!=null) {
                                    Log.d(TAG, "Image path : " + image);
                                    File imgFile = new File(image);
                                    if (imgFile.exists()) {
                                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                                        profilePic.setImageBitmap(myBitmap);
                                    }
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        };
        IntentFilter filter = new IntentFilter(RoosterConnectionService.VCARD_SAVED);
        filter.addAction(RoosterConnectionService.VCARD_LOADED);
        registerReceiver(mBroadcastReceiver, filter);

        if(!isChoosingImage) {
            Intent intent = new Intent(RoosterConnectionService.LOAD_VCARD);
            intent.putExtra("jid", myJid);
            sendBroadcast(intent);
        }
    }

    @Override
    public void processFinish(JSONObject output) {
        Log.d(TAG, output.toString());
        try {
            boolean error = output.getBoolean("error");
            if(!error){
                String tag = output.getString("tag");
                if(tag.equals("update_user_vcard")){
                    JSONObject user = (JSONObject) output.getJSONObject("user");
                    String jid = user.getString("jid");
                    String vcardtimestamp = user.getString("vcardDate");
                    String vcardDate = user.getString("vcardDate");
                    Log.d(TAG, "JID---> " + jid+ "\nVCardDate---> " + vcardDate);
                    Log.d(TAG, "Updated VCard on ["+vcardDate+"]");
                }
            }
            finish();
        }catch (JSONException e) {
            e.printStackTrace();
            finish();
        }
    }

    @Override
    public void processError(String error) {

    }
}
