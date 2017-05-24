package com.bilkoon.rooster.model;

/**
 * Created by Ahmed on 5/17/2017.
 */

public class GcmInfo {
    private long id;
    private int uid;
    private String imei;
    private String reg_id;
    private long timestamp;

    public GcmInfo(){

    }

    public void setId(long id){
        this.id = id;
    }

    public void setUid(int uid){
        this.uid = uid;
    }

    public void setRegId(String reg_id){
        this.reg_id = reg_id;
    }

    public void setImei(String imei){
        this.imei = imei;
    }

    public void setTimestamp(long timestamp){
        this.timestamp = timestamp;
    }

    public long getId(){
        return this.id;
    }
    public int getUid(){
        return this.uid;
    }
    public String getRegId(){
        return this.reg_id;
    }
    public String getImei(){
        return this.imei;
    }
    public long getTimestamp(){
        return this.timestamp;
    }
}
