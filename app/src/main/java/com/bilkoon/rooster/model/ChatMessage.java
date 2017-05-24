package com.bilkoon.rooster.model;

/**
 * Created by himanshusoni on 06/09/15.
 */
public class ChatMessage {
    private boolean isImage, isMine, isSection;
    private String content;

    private String stanzaId;
    private String message_status;
    private String time;

    public ChatMessage(){

    }
    public ChatMessage(String message, boolean mine, boolean image) {
        content = message;
        isMine = mine;
        isImage = image;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isMine() {
        return isMine;
    }

    public void setIsMine(boolean isMine) {
        this.isMine = isMine;
    }

    public boolean isImage() {
        return isImage;
    }

    public void setIsImage(boolean isImage) {
        this.isImage = isImage;
    }


    public void setStanzaId(String stanzaId){ this.stanzaId = stanzaId; }
    public String getStanzaId(){return  stanzaId;}

    public void setMessageStatus(String message_status){ this.message_status = message_status; }
    public String getMessageStatus(){return  message_status;}

    public void setTime(String time){ this.time = time; }
    public String getTime(){return  this.time;}

    public void setIsSection(boolean isSection){
        this.isSection = isSection;
    }
    public boolean isSection(){
        return this.isSection;
    }

    public String toString(){
        return "{isImage:'"+isImage()+"', isMine:'"+isMine()+"', message:'"+getContent()+
                "', stanza_id:'"+getStanzaId()+"', status:'"+getMessageStatus()+"'}";
    }
}
