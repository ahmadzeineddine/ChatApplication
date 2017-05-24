package com.bilkoon.rooster.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ahmed.chatapplication.R;


/**
 * Created by Ahmed on 5/1/2017.
 */

public class StudentsHallFragment extends Fragment {
    public static String TAG = "StudentsHallFragment";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.student_hall_fragment, container, false);
    }
}
