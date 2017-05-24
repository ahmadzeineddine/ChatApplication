package com.ahmed.chatapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;

/**
 * Created by Ahmed on 4/30/2017.
 */

public class BuddyProfile extends AppCompatActivity {
    private static String TAG = "BuddyProfile";

    private String buddyJid;
    ImageView buddyProfilePic;
    TextView buddyNameTxt, buddyStatusTxt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buddy_profile);

        buddyProfilePic = (ImageView) findViewById(R.id.profileBuddyImage);
        buddyNameTxt = (TextView) findViewById(R.id.buddyNameTxt);
        buddyStatusTxt = (TextView) findViewById(R.id.buddyStatusTxt);

        buddyJid = getIntent().getStringExtra("buddy_jid");
        if(getIntent().getStringExtra("ppic")!=null) {
            Glide.with(this)
                    .load(new File(getIntent().getStringExtra("ppic")))
                    .into(buddyProfilePic);
        }
        if(getIntent().getStringExtra("name")!=null){
            buddyNameTxt.setText(getIntent().getStringExtra("name"));
        }
        if(getIntent().getStringExtra("status")!=null){
            buddyStatusTxt.setText(getIntent().getStringExtra("status"));
        }
    }

    public void okProfileClick(View v){
        finish();
    }
}
