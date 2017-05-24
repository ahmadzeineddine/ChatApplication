package com.ahmed.chatapplication.info;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.bilkoon.rooster.util.SessionManager;

/**
 * Created by Ahmed on 4/26/2017.
 */

public class PermissionsInfo {
    public static int MY_PERMISSIONS_REQUESTS = 1;
    public static SessionManager sessionManager;
    static String[] mPermission = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET, Manifest.permission.CHANGE_NETWORK_STATE};

    public static void requestPermissions(Activity context){
        try {
            if (        ActivityCompat.checkSelfPermission(context, mPermission[0]) != PackageManager.PERMISSION_GRANTED
                    ||  ActivityCompat.checkSelfPermission(context, mPermission[1])!= PackageManager.PERMISSION_GRANTED
                    ||  ActivityCompat.checkSelfPermission(context, mPermission[2])!= PackageManager.PERMISSION_GRANTED
                    ||  ActivityCompat.checkSelfPermission(context, mPermission[3])!= PackageManager.PERMISSION_GRANTED
                    ||  ActivityCompat.checkSelfPermission(context, mPermission[4])!= PackageManager.PERMISSION_GRANTED
                    ||  ActivityCompat.checkSelfPermission(context, mPermission[5])!= PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(context, mPermission, MY_PERMISSIONS_REQUESTS);
                // If any permission above not allowed by user, this condition will execute every tim, else your else part will work
                sessionManager = new SessionManager(context);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
