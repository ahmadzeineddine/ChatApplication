package com.bilkoon.rooster.model;

/**
 * Created by Ahmed on 4/21/2017.
 */

public class RegistrationInfo {
    private String jid;
    private String password;
    private boolean registered;

    public RegistrationInfo(String jid, String password, boolean registered){
        this.jid = jid;
        this.password = password;
        this.registered = registered;
    }
    public void setJid(String jid){
        this.jid = jid;
    }
    public void setPassword(String password){
        this.password = password;
    }
    public void setRegistered(boolean registered){
        this.registered = registered;
    }

    public String getJid(){
        return this.jid;
    }
    public String getPassword(){
        return this.password;
    }
    public boolean getRegistered(){
        return this.registered;
    }
}
