package com.bilkoon.rooster.util;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
/**
 * Created by Ahmed on 6/29/2016.
 */
public class IsTypingTextWatcher implements TextWatcher {

    private final static String TAG = "IsTypingTextWatcher";
    private boolean currentTypingState = false;
    private Handler handler = new Handler();
    private static int typingInterval = 800;

    public static OnTypingModified onTypingModified = null;

    public IsTypingTextWatcher(){
    }

    public IsTypingTextWatcher(int typingInterval){
        this.typingInterval = typingInterval;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if(!currentTypingState)
        {
            currentTypingState = true;
            if(onTypingModified!=null) {
                if(s.length()>0) onTypingModified.onIsTypingModified(1);
            }
        }

        handler.removeCallbacks(stoppedTypingNotifier);
        handler.postDelayed(stoppedTypingNotifier, typingInterval);
    }

    private Runnable stoppedTypingNotifier = new Runnable()
    {
        @Override
        public void run()
        {
            currentTypingState = false;
            if(onTypingModified!=null) onTypingModified.onIsTypingModified(0);
        }
    };
}
