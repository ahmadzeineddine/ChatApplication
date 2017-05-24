package com.bilkoon.rooster.util;

/**
 * Created by Ahmed on 5/1/2017.
 */

public class CompareDates {
    public static boolean compare(String date1, String date2){
        //boolean isDate2AfterDate1 = false;
        if(date1==null || date1.equals("")) return true;
        else if(date2==null || date2.equals("")) return false;
        String[] date1_arr = date1.split(" ");
        String[] date2_arr = date2.split(" ");

        int year1 = Integer.parseInt(date1_arr[0].split("-")[0]);
        int month1 = Integer.parseInt(date1_arr[0].split("-")[1]);
        int day1 = Integer.parseInt(date1_arr[0].split("-")[2]);

        int year2 = Integer.parseInt(date2_arr[0].split("-")[0]);
        int month2 = Integer.parseInt(date2_arr[0].split("-")[1]);
        int day2 = Integer.parseInt(date2_arr[0].split("-")[2]);

        int hr1 = Integer.parseInt(date1_arr[1].split(":")[0]);
        int min1 = Integer.parseInt(date1_arr[1].split(":")[1]);
        int sec1 = Integer.parseInt(date1_arr[1].split(":")[2]);

        int hr2 = Integer.parseInt(date2_arr[1].split(":")[0]);
        int min2 = Integer.parseInt(date2_arr[1].split(":")[1]);
        int sec2 = Integer.parseInt(date2_arr[1].split(":")[2]);

        String am_pm1 = date1_arr[2];
        String am_pm2 = date2_arr[2];

        if(year2>year1) return true;         //isDate2AfterDate1 = true;
        else if(year2==year1 && month2>month1) return true; //isDate2AfterDate1 = true;
        else if(year2==year1 && month2==month1&& day2>day1) return true; //isDate2AfterDate1 = true;
        else if(year2==year1 && month2==month1&& day2==day1){
            if(am_pm2.equals("pm") && am_pm1.equals("am")) return true; //isDate2AfterDate1 = true;
            else if((am_pm2.equals("am") && am_pm1.equals("am")) || (am_pm2.equals("pm") && am_pm1.equals("pm"))){
                if(hr2>hr1) return true; //isDate2AfterDate1 = true;
                else if(hr2==hr1 && min2>min1) return true; //isDate2AfterDate1 = true;
                else if(hr2==hr1 && min2==min1 && sec2>sec1) return true; //isDate2AfterDate1 = true;
            }
        }
        //return isDate2AfterDate1;
        return false;
    }
}
