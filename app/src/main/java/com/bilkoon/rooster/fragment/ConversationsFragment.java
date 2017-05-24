package com.bilkoon.rooster.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ahmed.chatapplication.R;
import com.ahmed.chatapplication.XmppChatActivity;
import com.bilkoon.rooster.adapter.BuddyAdapter;
import com.bilkoon.rooster.connection.RoosterConnection;
import com.bilkoon.rooster.database.ConversationDataSource;
import com.bilkoon.rooster.model.Contact;
import com.bilkoon.rooster.service.RoosterConnectionService;
import com.bilkoon.rooster.util.CompareDates;
import com.bilkoon.rooster.util.SessionManager;

import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by Ahmed on 5/1/2017.
 */

public class ConversationsFragment extends Fragment {
    public static String TAG = "ConversationsFragment";

    public View mainView;

    ConversationDataSource datasource;
    SessionManager sessionManager;
    private static String myJid;

    ArrayList<String> buddyJids;
    List<Contact> buddies;

    private RecyclerView buddyRecyclerView;
    private BuddyAdapter mAdapter;

    private BroadcastReceiver mBroadcastReceiver;
    boolean mBroadCastReceiverIsRegistered = false;

    Activity activity;

    int userCount;
    int currentCount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.conversations_fragment, container, false);

        activity = getActivity();

        datasource = new ConversationDataSource(activity);
        sessionManager = new SessionManager(activity);
        buddyJids = new ArrayList<String>();
        buddies = new ArrayList<Contact>();
        myJid = XmppChatActivity.myJid;

        getBuddies();
        setupBuddies();

        userCount = mAdapter.getItemCount();
        loadLocalProfiles(0);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d(TAG, "Action : " + action);
                if (action.equals(RoosterConnectionService.VCARD_LOADED)) {
                    Log.d(TAG, "VCard loaded for : " + intent.getStringExtra("name") + " whose jid is " + intent.getStringExtra("jid"));
                    Contact contact = mAdapter.getContact(mAdapter.findContactId(intent.getStringExtra("jid")));
                    contact.setName(intent.getStringExtra("name"));
                    contact.setStatus(intent.getStringExtra("status"));
                    contact.setAvatar(intent.getStringExtra("ppic"));
                    mAdapter.update(contact);
                    if (currentCount < userCount) {
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                sessionManager.saveCurrentVCardCount(currentCount + 1);
                                currentCount = sessionManager.loadCurrentVCardCount();
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loadLocalProfiles(currentCount);
                                    }
                                });

                            }
                        }, 800);
                    }
                }else if (action.equals(RoosterConnectionService.CONTACT_LIST)) {
                    List<String> xmppcontacts = intent.getStringArrayListExtra("jids");
                    ArrayList<String> available = intent.getStringArrayListExtra("available");
                    if (xmppcontacts != null) {
                        Log.d(TAG, "Contacts size : " + xmppcontacts.size());
                        int count = 0;
                        for (String contact : xmppcontacts) {
                            Log.d(TAG, "Contact : " + contact + " is available " + available.get(count));
                            count++;
                        }
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(RoosterConnectionService.VCARD_LOADED);
        filter.addAction(RoosterConnectionService.CONTACT_LIST);
        activity.registerReceiver(mBroadcastReceiver, filter);
        mBroadCastReceiverIsRegistered = true;
        return mainView;
    }

    public void loadLocalProfiles(final int id){
        int nextId = id;
        if(nextId<userCount){
            Contact contact = mAdapter.getContact(id);
            String jid = mAdapter.getContact(id).getJid();

            String localDate = sessionManager.loadVCardTimestamp(jid);
            String serverDate = sessionManager.loadServerVCardTimestamp(jid);
            boolean isVcardUpdated = CompareDates.compare(serverDate, localDate);

            Log.d(TAG, "Local vcard date for ["+jid+"]"+localDate+"----server vcard date for ["+jid+"]"+serverDate);
            if(serverDate==null) {
                Log.d(TAG, "Ommiting vcard");
                contact.setName("");
                contact.setStatus("");
                mAdapter.update(contact);
                nextId = id+1;
                loadLocalProfiles(nextId);
                return;
            }
            if(!isVcardUpdated){
                currentCount = id;
                loadProfiles(id);
                return;
            }
            nextId = id+1;
            loadLocalProfiles(nextId);
        }
    }

    public void loadProfiles(int id){
        if(id<mAdapter.getItemCount()) {
            Intent intent = new Intent(RoosterConnectionService.CONTACT_PROFILES);
            intent.putExtra("jid", mAdapter.getContact(id).getJid());
            activity.sendBroadcast(intent);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mBroadCastReceiverIsRegistered){
            activity.unregisterReceiver(mBroadcastReceiver);
            mBroadCastReceiverIsRegistered = false;
        }
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        if(mBroadCastReceiverIsRegistered){
            activity.unregisterReceiver(mBroadcastReceiver);
            mBroadCastReceiverIsRegistered = false;
        }
    }

    private void getBuddies(){
        //fix to get only the me_buddy column from database
        datasource.open();
        buddyJids = datasource.getMeBuddyColumn();
        datasource.close();
        HashSet<String> values = new HashSet<String>();
        values.addAll(buddyJids);
        buddyJids.clear();
        buddyJids.addAll(values);
        int ln = buddyJids.size();
        for(int i=0;i<ln;i++){
            String buddyJid = buddyJids.get(i).split("_")[1]+"@"+ RoosterConnection.xmppServer;
            Contact contact = new Contact(buddyJid);
            VCard vcard = sessionManager.loadVCardInfo(contact.getJid());
            contact.setName(vcard.getFirstName());
            contact.setStatus(vcard.getField("status"));
            contact.setAvatar(vcard.getField("avatar"));
            buddies.add(contact);
        }
    }

    public void setupBuddies(){
        buddyRecyclerView = (RecyclerView) mainView.findViewById(R.id.buddy_recycler_view);
        buddyRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        mAdapter = new BuddyAdapter(getActivity(), buddies);
        buddyRecyclerView.setAdapter(mAdapter);
        Log.d(TAG, "Contact List activity is created");
    }

}