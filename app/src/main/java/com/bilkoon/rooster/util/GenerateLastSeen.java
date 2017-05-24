package com.bilkoon.rooster.util;

import android.text.format.DateUtils;
import android.util.Log;


import com.ahmed.chatapplication.app.AppController;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Ahmed on 4/21/2017.
 */

public class GenerateLastSeen {
    private static String TAG = "GenerateLastSeen";
    private static SessionManager sessionManager;

    public static String LastSeen(String jid){
        String result = "";
        sessionManager = new SessionManager(AppController.getInstance());

        if(sessionManager.loadLastSeenDate(jid)!=null) {
            String lastSeenTimeStamp = sessionManager.loadLastSeenDate(jid);
            long timestampLong = Long.parseLong(lastSeenTimeStamp);
            Date lastseenDate = new Date(timestampLong);
            Calendar lastSeenCal = Calendar.getInstance(Locale.ENGLISH);
            lastSeenCal.setTime(lastseenDate);

            int oldDay = lastSeenCal.get(Calendar.DAY_OF_MONTH);
            int oldMonth = lastSeenCal.get(Calendar.MONTH);
            int oldYear = lastSeenCal.get(Calendar.YEAR);

            Calendar today = Calendar.getInstance(Locale.ENGLISH);
            //Log.d(TAG, today.toString());

            int currentDay = today.get(Calendar.DAY_OF_MONTH);
            int currentMonth = today.get(Calendar.MONTH);
            int currentYear = today.get(Calendar.YEAR);

            //Log.d(TAG, "Current Day : "+currentDay+"\nCurrent Month : "+currentMonth+"\nCurrent Year : "+currentYear);
            //Log.d(TAG, "Old Day : "+oldDay+"\nOld Month : "+oldMonth+"\nOld Year : "+oldYear);

            int dayDiff = currentDay - oldDay;
            int monthDiff = currentMonth - oldMonth;
            int yearDiff = currentYear - oldYear;

            String date = new SimpleDateFormat("hh:mm aa", Locale.getDefault()).format(lastseenDate);
            String dayName = new SimpleDateFormat("EEEE", Locale.US).format(lastseenDate);
            String monthName = new SimpleDateFormat("MMM", Locale.US).format(lastseenDate);

            if (monthDiff == 0 && yearDiff == 0) {
                if (dayDiff == 0) {
                    result = "today ";
                    long difference = today.getTimeInMillis() - lastSeenCal.getTimeInMillis();
                    long differenceInSeconds = difference / DateUtils.SECOND_IN_MILLIS;
                    String formatted = DateUtils.formatElapsedTime(differenceInSeconds);
                    String[] temp = formatted.split(":");
                    if (formatted.length() > 5) {
                        if (Integer.parseInt(temp[0]) > 1) result += date;
                        else result = temp[0] + " hrs ," + temp[1] + " mins ago";
                    } else if (formatted.length() <= 5) {
                        if (differenceInSeconds < 60) result = differenceInSeconds + " seconds ago";
                        else {
                            if (Integer.parseInt(temp[0]) < 10) {
                                if (Integer.parseInt(temp[0]) == 1)
                                    result = Integer.parseInt(temp[0]) + " minute ago";
                                else result += Integer.parseInt(temp[0]) + " minutes ago";
                            } else result = temp[0] + " minutes ago";
                        }
                    }
                } else if (dayDiff == 1) {
                    result = "yesterday ";
                    result += date;
                } else if (dayDiff > 1) {
                    result += dayName + ", " + date;
                }
            } else if (monthDiff > 0 && yearDiff == 0) {
                result += monthName + " " + oldDay + ", " + date;
            } else if (yearDiff > 0) {
                result += oldYear + "-" + oldMonth + "-" + oldDay;
            }
        }
        return result;
    }
}
