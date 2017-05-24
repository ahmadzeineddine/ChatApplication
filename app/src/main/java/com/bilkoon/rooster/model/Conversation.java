package com.bilkoon.rooster.model;

import java.util.ArrayList;

/**
 * Created by Ahmed on 5/7/2017.
 */

public class Conversation {
    private ArrayList<ConversationMessage> messages;

    public Conversation(){
        this.messages = new ArrayList<ConversationMessage>();
    }

    public int getMessageCount(){
        return messages.size();
    }
    public void addMessages(ArrayList<ConversationMessage> new_messages){
        int ln = new_messages.size();
        for(int i=0;i<ln;i++) messages.add(new_messages.get(i));
    }

    public void addMessage(ConversationMessage message){
        messages.add(message);
    }

    public void removeMessage(int id){
        messages.remove(id);
    }

    public ConversationMessage getMessage(int id){
        return messages.get(id);
    }

    public void updateMessage(int id, ConversationMessage message){
        messages.set(id, message);
    }
}
