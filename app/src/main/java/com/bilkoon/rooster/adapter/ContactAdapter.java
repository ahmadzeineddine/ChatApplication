package com.bilkoon.rooster.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ahmed.chatapplication.ChatActivity;
import com.ahmed.chatapplication.R;
import com.bilkoon.rooster.model.Contact;
import com.bilkoon.rooster.util.SessionManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by Ahmed on 4/8/2017.
 */

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactHolder> {
    private final String TAG = "ContactAdapter";
    private List<Contact> mContacts;
    private Context mContext;
    SessionManager sessionManager;
    public ContactAdapter(Context context, List<Contact> contactList){
        mContacts = contactList;
        mContext = context;
        sessionManager = new SessionManager(context);
    }
    @Override
    public int getItemCount() {
        return mContacts.size();
    }
    @Override
    public ContactHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.list_item_contact, parent,false);
        return new ContactHolder(view);
    }
    public void add(Contact contact){
        mContacts.add(contact);
        notifyItemInserted(mContacts.size() - 1);
    }
    public void add(String jid){
        int id = findContactId(jid);
        if(id==-1){
            mContacts.add(new Contact(jid));
            notifyItemInserted(mContacts.size() - 1);
        }
    }
    /*public void addJidGroup(ArrayList<String> jids){
        int ln = jids.size();
        for(int i=0;i<ln;i++) add(jids.get(i));
    }*/

    public void addContactGroup(ArrayList<HashMap<String, String>> contacts){
        if(contacts!=null) {
            int ln = contacts.size();
            for (int i = 0; i < ln; i++) {
                HashMap<String, String> user = contacts.get(i);
                Log.d(TAG, "Adding user ["+user.get("jid")+"]");
                Contact contact = new Contact(user.get("jid"), user.get("available"));
                add(contact);
            }
        }
    }

    public void remove(String jid){
        int id = findContactId(jid);
        if(id!=-1) {
            Contact contact = mContacts.get(id);
            mContacts.remove(contact);
            notifyItemRemoved(id);
        }
    }

    public void update(Contact contact){
        int id = findContactId(contact.getJid());
        if(id!=-1){
            mContacts.set(id, contact);
            notifyItemChanged(id);
        }
    }

    public void updateAll(List<Contact> updatedContacts){
        mContacts = updatedContacts;
        notifyDataSetChanged();
    }
    public int findContactId(String jid){
        for(int i=0;i<mContacts.size();i++) if(mContacts.get(i).getJid().equals(jid)) return i;
        return -1;
    }
    public Contact getContact(int id){
        if(id!=-1)  return mContacts.get(id);
        return null;
    }
    public void removeAll(){
        mContacts.removeAll(mContacts);
        notifyDataSetChanged();
    }
    @Override
    public void onBindViewHolder(ContactHolder holder, int position) {
        Contact contact = mContacts.get(position);
        holder.jid = contact.getJid();
        GradientDrawable bgShape = (GradientDrawable) holder.availableView.getBackground();
        if(contact.getAvailable()!=null) {
            if (contact.getAvailable().equals("unavailable")) {
                bgShape.setColor(ContextCompat.getColor(mContext, R.color.unavailable));
                holder.isAvailable = false;
            } else {
                bgShape.setColor(ContextCompat.getColor(mContext, R.color.available));
                holder.isAvailable = true;
                Calendar now = Calendar.getInstance(Locale.ENGLISH);
                sessionManager.saveLastSeenDate(contact.getJid(), ""+now.getTimeInMillis());
            }
        }else bgShape.setColor(ContextCompat.getColor(mContext, R.color.unavailable));
        if(contact.getAvatar()!=null){
            Glide.with(mContext)
                    .load(new File(contact.getAvatar()))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(holder.profileImage);
        }else{
            Glide.with(mContext)
                    .load(R.drawable.default_avatar)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(holder.profileImage);
        }
        if(contact.getName()!=null){
            holder.contactTextView.setText(contact.getName()+"-["+holder.jid+"]");
        }else{
            holder.contactTextView.setText(holder.jid);
        }
        if(contact.getStatus()!=null){
            holder.status = contact.getStatus();
            holder.statusTextView.setText(contact.getStatus());
        }
        if(contact.getName()!=null){
            holder.name = contact.getName();
        }else holder.name = "";
        if(contact.getAvatar()!=null){
            holder.ppic_path = contact.getAvatar();
        }
    }

    class ContactHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView contactTextView, statusTextView, lastseenTextView;
        ImageView availableView, profileImage;
        String jid, name, ppic_path, status;
        boolean isAvailable = false;
        public ContactHolder(final View itemView) {
            super(itemView);
            contactTextView = (TextView) itemView.findViewById(R.id.contact_jid);
            statusTextView = (TextView) itemView.findViewById(R.id.statusTextView);
            lastseenTextView = (TextView) itemView.findViewById(R.id.lastSeenContactTextView);
            availableView = (ImageView) itemView.findViewById(R.id.availableView);
            profileImage = (ImageView) itemView.findViewById(R.id.userProfile);
            itemView.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {
            //Inside here we start the chat activity
            Intent intent = new Intent(mContext, ChatActivity.class);
            intent.putExtra("jid", jid);
            intent.putExtra("available", isAvailable);
            intent.putExtra("name", name);
            intent.putExtra("ppic", ppic_path);
            intent.putExtra("status", status);
            mContext.startActivity(intent);
        }
    }

}
