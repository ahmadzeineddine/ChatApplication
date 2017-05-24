package com.ahmed.chatapplication.connection;

import android.util.Log;

import com.ahmed.chatapplication.app.AppController;
import com.ahmed.chatapplication.response.VolleyJsonResponse;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;


import org.json.JSONException;
import org.json.JSONObject;
import java.util.Map;

/**
 * Created by Ahmed on 6/24/2015.
 */
public class HttpVolleyRequest{

    public VolleyJsonResponse delegate = null;
    private static final String TAG = HttpVolleyRequest.class.getSimpleName();
    public HttpVolleyRequest(){
    }
    public void makeRequest(final String tag_string_req, String url,final Map<String, String> urlParams){
        StringRequest strReq = new StringRequest(Request.Method.POST,
            url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "RECEIVING["+tag_string_req+"] : " + response.toString());
                try {
                    JSONObject jObj = new JSONObject(response);
                    delegate.processFinish(jObj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //error.printStackTrace();
                if (error.networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        // Show timeout error message
                        Log.e(tag_string_req, "Error [TimeoutError]");
                        delegate.processError("TimeoutError");
                        return;
                    }
                }
                Log.e(tag_string_req, tag_string_req + " Error: " + error.getLocalizedMessage());
                delegate.processError("Some Error occured");
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                    return urlParams;
                }
        };
        int socketTimeout = 50000; // 30 seconds. You can change it
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        strReq.setRetryPolicy(policy);
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

}