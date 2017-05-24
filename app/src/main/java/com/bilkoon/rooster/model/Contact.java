package com.bilkoon.rooster.model;

import android.net.Uri;

/**
 * Created by gakwaya on 4/16/2016.
 */
public class Contact {
    private String jid;
    private String name;
    private String available;
    private String status;
    private String avatar;

    public Contact(){
    }
    public Contact(String contactJid )
    {
        jid = contactJid;
    }
    public Contact(String contactJid, String available){
        this.jid = contactJid;
        this.available = available;
    }

    public String getJid()
    {
        return jid;
    }
    public void setJid(String jid) {
        this.jid = jid;
    }
    public String getName(){
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setAvailable(String available) {this.available = available;}
    public String getAvailable(){return available;}
    public void setStatus(String status){this.status=status;}
    public String getStatus(){return this.status;}
    public void setAvatar(String avatar){this.avatar=avatar;}
    public String getAvatar(){return this.avatar;}

}
