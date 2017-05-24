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

/*import com.blikoon.rooster.R;
import com.blikoon.rooster.adapter.ContactAdapter;
import com.blikoon.rooster.connection.HttpVolleyRequest;
import com.blikoon.rooster.info.XmppServersInfo;
import com.blikoon.rooster.model.Contact;
import com.blikoon.rooster.response.VolleyJsonResponse;
import com.blikoon.rooster.service.RoosterConnectionService;
import com.blikoon.rooster.util.SessionManager;*/

import com.ahmed.chatapplication.R;
import com.ahmed.chatapplication.connection.HttpVolleyRequest;
import com.ahmed.chatapplication.info.XmppServersInfo;
import com.ahmed.chatapplication.response.VolleyJsonResponse;
import com.bilkoon.rooster.adapter.ContactAdapter;
import com.bilkoon.rooster.model.Contact;
import com.bilkoon.rooster.service.RoosterConnectionService;
import com.bilkoon.rooster.util.CompareDates;
import com.bilkoon.rooster.util.SessionManager;

import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Ahmed on 5/1/2017.
 */

public class ContactsFragment extends Fragment implements VolleyJsonResponse {
    public static String TAG = "ContactsFragment";

    private SessionManager sessionManager;
    private RecyclerView contactsRecyclerView;
    private ContactAdapter mAdapter;
    private BroadcastReceiver mBroadcastReceiver;
    List<Contact> contacts;
    int userCount;
    int currentCount;
    boolean contactListStartedNotFirstTime;
    boolean mBroadCastReceiverIsRegistered = false;

    private Activity activity;

    public View mainView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.contacts_fragment, container, false);
        Log.d(TAG, "Contact Fragment is displayed");
        activity = getActivity();
        sessionManager = new SessionManager(activity);
        setupContacts();
        prepareContacts();
        return mainView;
    }
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
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

    public void setupContacts(){
        contactsRecyclerView = (RecyclerView) mainView.findViewById(R.id.contact_recycler_view);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(activity.getApplicationContext()));
        contacts = new ArrayList<Contact>();
        mAdapter = new ContactAdapter(activity, contacts);
        contactsRecyclerView.setAdapter(mAdapter);
        Log.d(TAG, "Contact List activity is created");
    }

    public void prepareContacts() {
        contactListStartedNotFirstTime = sessionManager.loadContactListStarted();
        Log.d(TAG, "ContactListNotFirstTime started :" + contactListStartedNotFirstTime);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                getUsers();
            }
        },800);
        sessionManager.saveContactListStarted(true);
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d(TAG, "Action : " + action);
                if (action.equals(RoosterConnectionService.CONTACT_LIST)) {
                    List<String> xmppcontacts = intent.getStringArrayListExtra("jids");
                    ArrayList<String> available = intent.getStringArrayListExtra("available");
                    if (xmppcontacts != null) {
                        mAdapter.removeAll();
                        Log.d(TAG, "Contacts size : " + xmppcontacts.size());
                        int count = 0;
                        for (String contact : xmppcontacts) {
                            Log.d(TAG, "Contact : " + contact + " is available " + available.get(count));
                            mAdapter.add(new Contact(contact, available.get(count)));
                            count++;
                        }
                        userCount = mAdapter.getItemCount();
                        contactsRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
                        currentCount = 0;
                        if (userCount != 0) {
                            //loadProfiles(currentCount);
                            loadLocalProfiles(currentCount);
                        }
                    }
                } else if (action.equals(RoosterConnectionService.FRIEND_ADDED)) {
                    ArrayList<HashMap<String, String>> contacts = (ArrayList<HashMap<String, String>>) intent.getSerializableExtra("contacts");
                    Log.d(TAG, "Contacts size : " + contacts.size());
                    Intent loadVcardIntent = new Intent(RoosterConnectionService.LOAD_VCARD);
                    HashMap<String, String> user = contacts.get(0);
                    loadVcardIntent.putExtra("jid", user.get("jid"));
                    activity.sendBroadcast(loadVcardIntent);
                    mAdapter.addContactGroup(contacts);
                } else if (action.equals(RoosterConnectionService.FRIEND_REMOVED)) {
                    String jid = intent.getStringExtra("jid");
                    mAdapter.remove(jid);
                } else if (action.equals(RoosterConnectionService.FRIEND_BLOCKED)) {
                    Contact contact = mAdapter.getContact(mAdapter.findContactId(intent.getStringExtra("jid")));
                    if (contact != null) {
                        contact.setAvailable(intent.getStringExtra("unavailable"));
                        mAdapter.update(contact);
                    }
                } else if (action.equals(RoosterConnectionService.FRIEND_UNBLOCKED)) {
                    Contact contact = mAdapter.getContact(mAdapter.findContactId(intent.getStringExtra("jid")));
                    //Contact contact = new Contact(intent.getStringExtra("jid"), intent.getStringExtra("available"));
                    if (contact != null) {
                        contact.setAvailable(intent.getStringExtra("available"));
                        mAdapter.update(contact);
                    }
                } else if (action.equals(RoosterConnectionService.CONTACT_AVAILABLE)) {
                    if (mAdapter.findContactId(intent.getStringExtra("jid")) != -1) {
                        Contact contact = mAdapter.getContact(mAdapter.findContactId(intent.getStringExtra("jid")));
                        contact.setAvailable(intent.getStringExtra("available"));
                        Log.d(TAG, "Setting Contact[" + contact.getJid() + "] to " + contact.getAvailable());
                        //Contact contact = new Contact(intent.getStringExtra("jid"), intent.getStringExtra("available"));
                        Log.d(TAG, "Contact [" + contact.getJid() + "] is online? " + intent.getStringExtra("available"));
                        mAdapter.update(contact);
                    }
                } else if (action.equals(RoosterConnectionService.USER_PRESENCE_RESULT)) {
                    Log.d(TAG, "User [" + intent.getStringExtra("jid") + " is " + intent.getStringExtra("available") + "]");
                } else if (action.equals(RoosterConnectionService.VCARD_LOADED)) {
                    Log.d(TAG, "VCard loaded for : " + intent.getStringExtra("name") + " whose jid is " + intent.getStringExtra("jid"));
                    if (mAdapter.findContactId(intent.getStringExtra("jid")) != -1) {
                        Contact contact = mAdapter.getContact(mAdapter.findContactId(intent.getStringExtra("jid")));
                        contact.setName(intent.getStringExtra("name"));
                        contact.setStatus(intent.getStringExtra("status"));
                        contact.setAvatar(intent.getStringExtra("ppic"));
                        mAdapter.update(contact);
                        Log.d(TAG, "Current index : " + currentCount + ", Total users size : " + userCount);
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
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(RoosterConnectionService.CONTACT_LIST);
        filter.addAction(RoosterConnectionService.FRIEND_ADDED);
        filter.addAction(RoosterConnectionService.FRIEND_REMOVED);
        filter.addAction(RoosterConnectionService.FRIEND_BLOCKED);
        filter.addAction(RoosterConnectionService.FRIEND_UNBLOCKED);
        filter.addAction(RoosterConnectionService.CONTACT_AVAILABLE);
        filter.addAction(RoosterConnectionService.USER_PRESENCE_RESULT);
        filter.addAction(RoosterConnectionService.VCARD_LOADED);
        activity.registerReceiver(mBroadcastReceiver, filter);
        mBroadCastReceiverIsRegistered = true;

        String contactJid = activity.getIntent().getStringExtra("jid");
        String available = activity.getIntent().getStringExtra("available");
        if (contactJid != null && available != null) {
            mAdapter.update(new Contact(contactJid, available));
        }
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
            if (isVcardUpdated) {
                Log.d(TAG, "vcard is already updated");
                VCard localVcard = sessionManager.loadVCardInfo(mAdapter.getContact(id).getJid());
                contact.setName(localVcard.getFirstName());
                contact.setStatus(localVcard.getField("status"));
                contact.setAvatar(localVcard.getField("avatar"));
                mAdapter.update(contact);

                Long tsLong = System.currentTimeMillis();
                DateFormat sdf = new SimpleDateFormat("yy-MM-dd hh:mm:ss a", Locale.ENGLISH);//2017-05-06 02:47:28 am ,,,,2017-05-06 05:09:27 am
                sdf.setTimeZone(TimeZone.getTimeZone("Asia/Beirut"));
                Date netDate = (new Date(tsLong));
                String dateStr = sdf.format(netDate);
                sessionManager.saveVCardTimestamp(jid, dateStr);

            }else{
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
    public void getUsers(){
        HttpVolleyRequest getJids = new HttpVolleyRequest();
        Map<String, String> params = new HashMap<String, String>();
        params.put("tag", "get_users");
        getJids.makeRequest("req_get", XmppServersInfo.URL_REGISTER + "test.php", params);
        getJids.delegate = this;
    }

    @Override
    public void processFinish(JSONObject output) {
        Log.d(TAG, "Getting registered users : " + output.toString());
        try {
            boolean error = output.getBoolean("error");
            if (!error) {
                String tag = output.getString("tag");
                Set<String> jids = new HashSet<String>();
                if (tag.equals("get_users")) {
                    JSONArray data = output.getJSONArray("data");
                    int ln = data.length();
                    Log.d(TAG, "GETTING USERS\n----------------------------------\n");
                    for (int i = 0; i < ln; i++) {
                        JSONObject userObj = (JSONObject) data.get(i);
                        int id = Integer.parseInt(userObj.getString("id"));
                        String jid = userObj.getString("jid");
                        String name = userObj.getString("name");
                        jids.add(jid);
                        Log.d(TAG, "|Data :\n" + "|ID---> " + id + "\n|JID---> " + jid + "\n|NAME---> " + name);
                    }
                    Log.d(TAG, "|-------------------------------------------------|");
                    sessionManager.saveServerJids(jids);
                }
                Intent intent = new Intent(RoosterConnectionService.CONTACT_LIST_SERVER);
                activity.sendBroadcast(intent);
            }else{
                Log.d(TAG, "Cannot start the application!");
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processError(String error) {
        if(error!=null) {
            if (error.contains("failed to connect")) {
                Log.d(TAG, "Connection failure!");
            }
        }
    }
}
