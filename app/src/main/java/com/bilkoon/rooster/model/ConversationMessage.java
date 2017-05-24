package com.bilkoon.rooster.model;

import java.util.ArrayList;

/**
 * Created by Ahmed on 5/7/2017.
 */

public class ConversationMessage extends ChatMessage{
    private long id;
    private String me_buddy;
    public ConversationMessage(){

    }

    public ConversationMessage(String me_buddy, String message, boolean mine, boolean image){
        super(message, mine, image);
    }
    public ConversationMessage(String message, boolean mine, boolean image) {
        super(message, mine, image);
    }

    public void setId(long id){
        this.id = id;
    }
    public long getId(){
        return this.id;
    }

    public void setMeBuddy(String me_buddy){
        this.me_buddy = me_buddy;
    }
    public String getMeBuddy(){
        return this.me_buddy;
    }
}
