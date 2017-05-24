package com.ahmed.chatapplication.app;

/**
 * Created by Ahmed on 6/7/2015.
 */
import android.app.Application;
import android.content.Context;
//import android.support.multidex.MultiDex;
import android.text.TextUtils;

//import com.ahmed.chatapplication.BuildConfig;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

//import cn.zhaiyifan.logger.Logger;
//import cn.zhaiyifan.logger.ReleaseLogger;

public class AppController extends Application {

    public static final String TAG = AppController.class.getSimpleName();

    private RequestQueue mRequestQueue;
    private static AppController mInstance;
    public static Context ctx;


    protected void attachBaseContext(Context base)
    {
        super.attachBaseContext(base);
        //MultiDex.install(this);
    }
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        android.util.Log.i(TAG, "Preference Created.");
        ctx = getApplicationContext();
        /*if (BuildConfig.DEBUG) {
            Logger.init(this, ReleaseLogger.getInstance());
        } else {
            Logger.init(this, ReleaseLogger.getInstance());
        }*/
    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

}
