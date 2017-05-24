package com.ahmed.chatapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ahmed.chatapplication.connection.HttpVolleyRequest;
import com.ahmed.chatapplication.info.XmppServersInfo;
import com.ahmed.chatapplication.response.VolleyJsonResponse;
import com.bilkoon.rooster.adapter.ChatMessageAdapter;
import com.bilkoon.rooster.connection.RoosterConnection;
import com.bilkoon.rooster.database.ConversationDataSource;
import com.bilkoon.rooster.model.ChatMessage;
import com.bilkoon.rooster.model.Contact;
import com.bilkoon.rooster.model.RegistrationInfo;
import com.bilkoon.rooster.service.RoosterConnectionService;
import com.bilkoon.rooster.util.GenearteUID;
import com.bilkoon.rooster.util.GenerateLastSeen;
import com.bilkoon.rooster.util.IsTypingTextWatcher;
import com.bilkoon.rooster.util.OnTypingModified;
import com.bilkoon.rooster.util.SessionManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;

import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;


public class ChatActivity extends AppCompatActivity implements OnTypingModified, VolleyJsonResponse {
    private static final String TAG ="ChatActivity";

    private String contactServerId, contactJid, contactGcmId, buddyStatus, buddyName, buddyPpic;
    private BroadcastReceiver mBroadcastReceiver;

    private TextView typingIndicator, lastseen, meJid;
    private BroadcastReceiver mTypingIndicatorReceiver;

    private RecyclerView mRecyclerView;
    private Button mButtonSend;
    private EditText mEditTextMessage;
    private ImageView mImageView;


    private ChatMessageAdapter mAdapter;
    private static final int RC_CODE_PICKER = 2000;
    private ArrayList<Image> images = new ArrayList<>();
    private boolean isAvailable = false;
    SessionManager sessionManager;
    private ImageView ppic;
    private TextView username;

    ConversationDataSource datasource;
    ArrayList<ChatMessage> storedMessages;
    int stored_ln;
    String me_buddy;
    boolean mAdapterItemsAdded = false;
    private boolean userIsXmppConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        sessionManager = new SessionManager(this);
        RegistrationInfo info = sessionManager.loadRegistrationInfo();

        typingIndicator = (TextView) findViewById(R.id.typingIndicator);
        typingIndicator.setText("");
        lastseen = (TextView) findViewById(R.id.lastseen);
        //-------------------------------------------------------------------//
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mButtonSend = (Button) findViewById(R.id.btn_send);
        mEditTextMessage = (EditText) findViewById(R.id.et_message);
        mImageView = (ImageView) findViewById(R.id.iv_image);
        ppic = (ImageView) findViewById(R.id.ppic_image);
        username = (TextView) findViewById(R.id.profileName);
        //--------------------------------------------------//
        meJid = (TextView) findViewById(R.id.meJid);
        meJid.setText("I have jid ["+info.getJid()+"]");
        Intent intent = getIntent();
        contactJid = intent.getStringExtra("jid");
        isAvailable = intent.getBooleanExtra("available", false);

        datasource = new ConversationDataSource(ChatActivity.this);
        me_buddy = info.getJid().split("@")[0] + "_" + contactJid.split("@")[0];

        datasource.open();
        storedMessages = datasource.getConversationByMeBuddy(me_buddy);
        datasource.close();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new ChatMessageAdapter(this, new ArrayList<ChatMessage>());
        mRecyclerView.setAdapter(mAdapter);
        scrollStoredMessages();

        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = mEditTextMessage.getText().toString();
                if (TextUtils.isEmpty(message)) {
                    return;
                }
                sendMessage(message);
                mEditTextMessage.setText("");
            }
        });

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start();
            }
        });
        //-------------------TypingIndication--------------------------------//
        IsTypingTextWatcher.onTypingModified = (OnTypingModified) ChatActivity.this;
        IsTypingTextWatcher isTypingTextWatcher = new IsTypingTextWatcher();
        mEditTextMessage.addTextChangedListener(isTypingTextWatcher);
        mEditTextMessage.requestFocus();
        //-------------------------------------------------------------------//
        if(intent.getStringExtra("name")!=null) {
            buddyName = intent.getStringExtra("name");
            username.setText(intent.getStringExtra("name")+" ["+contactJid+"]");
        }

        if(intent.getStringExtra("ppic")!=null) {
            buddyPpic = intent.getStringExtra("ppic");
            Glide.with(this)
                    .load(new File(intent.getStringExtra("ppic")))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(ppic);
        }

        if(isAvailable) {
            lastseen.setText("online");
            Calendar now = Calendar.getInstance(Locale.ENGLISH);
            sessionManager.saveLastSeenDate(contactJid, ""+now.getTimeInMillis());
        }
        else {
            //String date = new SimpleDateFormat("hh:mm aa", Locale.getDefault()).format(new Date());
            if(!GenerateLastSeen.LastSeen(contactJid).equals("")) lastseen.setText(GenerateLastSeen.LastSeen(contactJid));
        }
        buddyStatus = intent.getStringExtra("status");
        setTitle(contactJid);
        Intent in = new Intent(RoosterConnectionService.CURRENT_CONVERSATION);
        in.putExtra("jid", contactJid);
        sendBroadcast(in);

        contactServerId = sessionManager.loadIdOfJid(contactJid);
    }

    public void scrollStoredMessages(){
        stored_ln = storedMessages.size();
        if(stored_ln<=4) mAdapter.isAddingStoredMessages = false;
        for (int i = 0; i < stored_ln; i++) {
            mAdapter.add(storedMessages.get(i));
            mAdapter.notifyDataSetChanged();
            mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
        }
        if(stored_ln>4) {
            mAdapter.isAddingStoredMessages = false;
            for (int i = stored_ln - 4; i < stored_ln; i++) {
                mAdapter.notifyItemChanged(i);
                mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
            }
        }
        mAdapterItemsAdded = true;
        createMessageSections();
    }

    int cnt = 0;
    public void updateStoredMessageStatus() {
        stored_ln = storedMessages.size();
        //if(userIsXmppConnected) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    final ChatMessage chatMessage = storedMessages.get(cnt);
                    if (!chatMessage.isMine() && !chatMessage.getMessageStatus().equals("read")) {
                        Log.d(TAG, "Message is read? " + chatMessage.getMessageStatus());
                        Log.d(TAG, "Message has stanza id " + chatMessage.getStanzaId());
                        sendMessage("~seen;" + chatMessage.getStanzaId());
                        datasource.open();
                        datasource.updateMessageStatusByStanzaId(chatMessage.getStanzaId(), "read");
                    }
                    if (cnt < stored_ln - 1) {
                        cnt++;
                        updateStoredMessageStatus();
                        //datasource.close();
                    }else datasource.close();
                }
            }, 500);
        //}
    }

    public void createMessageSections(){
        ArrayList<String> dates = new ArrayList<String>();
        ArrayList<String> sectionPosition = new ArrayList<String>();
        for (int i = 0; i < stored_ln; i++) dates.add(storedMessages.get(i).getTime().split(" ")[0]);

        ArrayList<Integer> indexList = new ArrayList<Integer>();
        for(int i=0;i<stored_ln;i++) {
            ArrayList<Integer> tempList = indexOfAll(dates.get(i), dates);
            indexList.add(tempList.get(0));
        }
        if(indexList.size()>0) {
            for (int i=0;i<indexList.size();i++) {
                sectionPosition.add(dates.get(indexList.get(i))+";"+indexList.get(i));
            }
        }

        HashSet<String>  temps = new HashSet<String>();
        temps.addAll(sectionPosition);
        sectionPosition.clear();
        sectionPosition.addAll(temps);

        indexList.clear();
        dates.clear();
        for(int i=0;i<sectionPosition.size();i++) {
            String key = sectionPosition.get(i).split(";")[1];
            String dateStr = sectionPosition.get(i).split(";")[0];
            indexList.add(Integer.parseInt(key));
            dates.add(dateStr);
            Log.d(TAG, "Section[" + dates.get(i) + "] has index : " + indexList.get(i));
        }

        DateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Beirut"));
        String date = sdf.format(new Date());
        String[] date_arr = date.split("-");
        int now_yr = Integer.parseInt(date_arr[2]);
        int now_day = Integer.parseInt(date_arr[1]);
        int now_mnth = Integer.parseInt(date_arr[0]);

        for(int i=0;i<dates.size();i++) {
            String[] dateArr = dates.get(i).split("-");
            if(Integer.parseInt(dateArr[2])==now_yr && Integer.parseInt(dateArr[0])==now_mnth){
                int day = Integer.parseInt(dateArr[1]);
                Log.d(TAG, "Day difference : "+ (now_day-day));
                if((now_day-day)==0) dates.set(i, "Today");
                else if((now_day-day)==1) dates.set(i, "Yesterday");
            }
        }
        for(int i=0;i<dates.size();i++){
            Log.d(TAG, "Dates : "+dates.get(i));
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setIsSection(true);
            chatMessage.setTime(dates.get(i));
            mAdapter.add(indexList.get(i), chatMessage);
        }
    }

    static ArrayList<Integer> indexOfAll(Object obj, ArrayList list){
        ArrayList<Integer> indexList = new ArrayList<Integer>();
        for (int i = 0; i < list.size(); i++)
            if(obj.equals(list.get(i)))
                indexList.add(i);
        return indexList;
    }

    // Recommended builder
    public void start() {
        boolean isSingleMode = true;
        ImagePicker imagePicker = ImagePicker.create(this)
                .returnAfterFirst(false) // set whether pick action or camera action should return immediate result or not. Only works in single mode for image picker
                .folderMode(true) // set folder mode (false by default)
                .folderTitle("Folder") // folder selection title
                .imageTitle("Tap to select"); // image selection title

        if (isSingleMode) {
            imagePicker.single();
        } else {
            imagePicker.multi(); // multi mode (default mode)
        }

        imagePicker.limit(10) // max images can be selected (99 by default)
                .showCamera(true) // show camera or not (true by default)
                .imageDirectory("Camera")   // captured image directory name ("Camera" folder by default)
                .origin(images) // original selected images, used in multi mode
                .start(RC_CODE_PICKER); // start image picker activity with request code
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, final Intent data) {
        if (requestCode == RC_CODE_PICKER && resultCode == RESULT_OK && data != null) {
            new Timer().schedule(new TimerTask() {
               @Override
               public void run() {
                   Log.d(TAG, "Sending media message");
                   sendMessage(data);
               }
            }, 300);
            return;
        }
    }

    public void sendMessage(String message){
        RoosterConnection.ConnectionState state = RoosterConnectionService.getState();
        if(userIsXmppConnected) {
            if (state.equals(RoosterConnection.ConnectionState.AUTHENTICATED) || state.equals(RoosterConnection.ConnectionState.RECONNECTED)) {
                Log.d(TAG, "The client is connected to the server,Sendint Message");
                //Send the message to the server
                String stanzaId = GenearteUID.getFrom();
                Intent intent = new Intent(RoosterConnectionService.SEND_MESSAGE);
                intent.putExtra(RoosterConnectionService.BUNDLE_MESSAGE_BODY, message);
                intent.putExtra(RoosterConnectionService.BUNDLE_TO, contactJid);
                intent.putExtra("stanza_id", stanzaId);
                sendBroadcast(intent);
                ChatMessage chatMessage = new ChatMessage(message, true, false);
                chatMessage.setStanzaId(stanzaId);
                chatMessage.setMessageStatus("unsent");
                DateFormat sdf = new SimpleDateFormat("MM-dd-yyyy hh:mm aa", Locale.ENGLISH);
                sdf.setTimeZone(TimeZone.getTimeZone("Asia/Beirut"));
                String date = sdf.format(new Date());
                chatMessage.setTime(date);
                if (!message.contains("~seen")) {
                    //database---------//
                    datasource.open();
                    datasource.createMessage(me_buddy, chatMessage);
                    datasource.close();
                    //-----------------//
                    mAdapter.add(chatMessage);
                    mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
                }
            } else {
                Toast.makeText(getApplicationContext(),
                        "Client not connected to server ,Message not sent!",
                        Toast.LENGTH_LONG).show();
            }
        }else{
            if(!message.contains("seen")) sendGCMMessage(contactGcmId, message);
        }
    }

    public void sendGCMMessage(String to, String message){
        HttpVolleyRequest send = new HttpVolleyRequest();
        Map<String, String> params = new HashMap<String, String>();
        params.put("tag", "single_user");
        params.put("reg_id", to);
        params.put("message", message);
        send.makeRequest("req_send", XmppServersInfo.URL_REGISTER+"test.php", params);
        send.delegate = this;
    }

    private void mimicOtherMessage(String message, String stanzaId) {
        ChatMessage chatMessage = new ChatMessage(message, false, false);
        chatMessage.setStanzaId(stanzaId);
        chatMessage.setMessageStatus("");
        DateFormat sdf = new SimpleDateFormat("MM-dd-yyyy hh:mm aa", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Beirut"));
        String date = sdf.format(new Date());
        chatMessage.setTime(date);
        //database------------//
        datasource.open();
        datasource.createMessage(me_buddy, chatMessage);
        datasource.close();
        //--------------------//
        mAdapter.add(chatMessage);
        mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
    }

    public void sendMessage(Intent data){
        RoosterConnection.ConnectionState state = RoosterConnectionService.getState();
        if (state.equals(RoosterConnection.ConnectionState.AUTHENTICATED) || state.equals(RoosterConnection.ConnectionState.RECONNECTED)) {
            Log.d(TAG, "The client is connected to the server,SendImage Message");
            //Send the message to the server
            images = (ArrayList<Image>) ImagePicker.getImages(data);
            Log.d(TAG, "Sending image [" + images.get(0).getPath() + "]");
            final int ln = images.size();
            final String stanzaId = GenearteUID.getFrom();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ChatMessage chatMessage = new ChatMessage(images.get(ln - 1).getPath(), true, true);
                    chatMessage.setMessageStatus("unsent");
                    chatMessage.setStanzaId(stanzaId);
                    DateFormat sdf = new SimpleDateFormat("MM-dd-yyyy hh:mm aa", Locale.ENGLISH);
                    sdf.setTimeZone(TimeZone.getTimeZone("Asia/Beirut"));
                    String date = sdf.format(new Date());
                    chatMessage.setTime(date);
                    //database--//
                    datasource.open();
                    datasource.createMessage(me_buddy, chatMessage);
                    datasource.close();
                    //----------//
                    mAdapter.add(chatMessage);
                    mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
                }
            });
            Looper.prepare();
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Intent intent = new Intent(RoosterConnectionService.SEND_IMAGE);
                    intent.putExtra(RoosterConnectionService.BUNDLE_TO, contactJid);
                    String path = images.get(ln - 1).getPath();
                    intent.putExtra("image_path", path);
                    intent.putExtra("stanza_id", stanzaId);
                    intent.putExtra(RoosterConnectionService.BUNDLE_MESSAGE_BODY, "image_name");
                    sendBroadcast(intent);
                }
            }, 300);

        } else {
            Looper.prepare();
            Toast.makeText(getApplicationContext(),
                    "Client not connected to server ,Message not sent!",
                    Toast.LENGTH_LONG).show();
        }
    }

    int imageMessageIndex = -1;
    private void mimicOtherImageMessage(String path, String stanzaId) {
        Log.d(TAG, "displaying image ["+path+"]");
        ChatMessage chatMessage = new ChatMessage(path, false, true);
        chatMessage.setContent("");
        chatMessage.setStanzaId(stanzaId);
        chatMessage.setMessageStatus("");
        DateFormat sdf = new SimpleDateFormat("MM-dd-yyyy hh:mm aa", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Beirut"));
        String date = sdf.format(new Date());
        chatMessage.setTime(date);
        datasource.open();
        datasource.createMessage(me_buddy, chatMessage);
        datasource.close();
        Log.d(TAG, chatMessage.toString());
        mAdapter.add(chatMessage);
        imageMessageIndex = mAdapter.getItemCount() - 1;
        mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
    }

    private void updateMimicOtherImageMessage(int id, String path, String stanzaId) {
        Log.d(TAG, "updating image ["+path+"]");
        ChatMessage chatMessage = new ChatMessage(path, false, true);
        datasource.open();
        datasource.updateMessageImagePathByStanzaId(stanzaId, path);
        datasource.close();
        mAdapter.update(id, chatMessage);
        mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        sessionManager.saveActivityIsRunning(TAG, false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
        unregisterReceiver(mTypingIndicatorReceiver);
        sessionManager.saveActivityIsRunning(TAG, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sessionManager.saveActivityIsRunning(TAG, true);
        RoosterConnection.ConnectionState state = RoosterConnectionService.getState();
        Log.d(TAG, "User is ["+state+"]");
        if(state.equals(RoosterConnection.ConnectionState.DISCONNECTED)){
            try{
                stopService(new Intent(this, RoosterConnectionService.class));
            }catch (Exception e){e.printStackTrace();}
            Intent i1 = new Intent(this, RoosterConnectionService.class);
            i1.putExtra("register", false);
            startService(i1);
        }
        Intent presenceIntent = new Intent(RoosterConnectionService.GET_USER_PRESENCE);
        presenceIntent.putExtra("jid", contactJid);
        sendBroadcast(presenceIntent);
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d(TAG, "Receiving from Rooster connection : "+action);
                if(action.equals(RoosterConnectionService.NEW_MESSAGE)){
                    String from = intent.getStringExtra(RoosterConnectionService.BUNDLE_FROM_JID);
                    String body = intent.getStringExtra(RoosterConnectionService.BUNDLE_MESSAGE_BODY);
                    if (from.equals(contactJid)){
                        mimicOtherMessage(body, intent.getStringExtra("stanza_id"));
                        sendMessage("~seen;"+intent.getStringExtra("stanza_id"));
                    }else{
                        Log.d(TAG,"Got a message from jid :"+from);
                    }
                }else if(action.equals(RoosterConnectionService.RECEIVING_FILE)){
                    mimicOtherImageMessage(null, intent.getStringExtra("stanza_id"));
                }else if(action.equals(RoosterConnectionService.NEW_IMAGE)){
                    String from = intent.getStringExtra(RoosterConnectionService.BUNDLE_FROM_JID);
                    String image_path = intent.getStringExtra(RoosterConnectionService.BUNDLE_MESSAGE_BODY);
                    if (from.equals(contactJid)){
                        updateMimicOtherImageMessage(imageMessageIndex, image_path, intent.getStringExtra("stanza_id"));
                        sendMessage("~seen;"+intent.getStringExtra("stanza_id"));
                    }else{
                        Log.d(TAG,"Got an image from jid :"+from);
                    }
                }else if(action.equals(RoosterConnectionService.IMAGE_SENT)){
                    Log.d(TAG, "stanzaId for this image is: " + intent.getStringExtra("stanza_id"));
                    datasource.open();
                    datasource.updateMessageStatusByStanzaId(intent.getStringExtra("stanza_id"), "sent");
                    datasource.close();
                    mAdapter.updateMessageStatus(intent.getStringExtra("stanza_id"), "sent");
                }else if(action.equals(RoosterConnectionService.CONTACT_AVAILABLE)){
                    userIsXmppConnected = true;
                    Contact contact = new Contact(intent.getStringExtra("jid"), intent.getStringExtra("available"));
                    String date = new SimpleDateFormat("hh:mm aa", Locale.getDefault()).format(new Date());
                    String online = "last seen "+date;
                    if(contact.getAvailable().equals("available")) online = "online";
                    lastseen.setText(online);
                }else if(action.equals(RoosterConnectionService.MESSAGE_SENT)){
                    Log.d(TAG, "stanzaId for this message is: " + intent.getStringExtra("stanza_id"));
                    datasource.open();
                    datasource.updateMessageStatusByStanzaId(intent.getStringExtra("stanza_id"), "sent");
                    datasource.close();
                    mAdapter.updateMessageStatus(intent.getStringExtra("stanza_id"), "sent");
                }else if(action.equals(RoosterConnectionService.MESSAGE_DELIVERED)){
                    Log.d(TAG, "stanzaId for this message is: " + intent.getStringExtra("stanza_id"));
                    datasource.open();
                    datasource.updateMessageStatusByStanzaId(intent.getStringExtra("stanza_id"), "delivered");
                    datasource.close();
                    mAdapter.updateMessageStatus(intent.getStringExtra("stanza_id"), "delivered");
                }else if(action.equals(RoosterConnectionService.MESSAGE_READ)){
                    Log.d(TAG, "stanzaId for this message is: " + intent.getStringExtra("stanza_id"));
                    datasource.open();
                    datasource.updateMessageStatusByStanzaId(intent.getStringExtra("stanza_id"), "read");
                    datasource.close();
                    mAdapter.updateMessageStatus(intent.getStringExtra("stanza_id"), "read");
                }else if(action.equals(RoosterConnectionService.CAN_SEND_MESSAGE)){
                    boolean canSendMessage = intent.getBooleanExtra("can_send_message", true);
                    mButtonSend.setEnabled(canSendMessage);
                }else if(action.equals(RoosterConnectionService.IMAGE_DELIVERED)){
                    Log.d(TAG, "stanzaId for this message is: " + intent.getStringExtra("stanza_id"));
                    datasource.open();
                    datasource.updateMessageStatusByStanzaId(intent.getStringExtra("stanza_id"), "delivered");
                    datasource.close();
                    mAdapter.updateMessageStatus(intent.getStringExtra("stanza_id"), "delivered");
                }else if(action.equals(RoosterConnectionService.USER_PRESENCE_RESULT)){
                    String state = intent.getStringExtra("available");
                    if(state.equals("available")) {
                        userIsXmppConnected = true;
                        mImageView.setEnabled(true);
                    }
                    else {
                        userIsXmppConnected = false;
                        mImageView.setEnabled(false);
                        datasource.open();
                        //get gcm for contact
                        contactGcmId = datasource.getGcmByUid(contactServerId);
                        Log.d(TAG, "Contact ["+contactServerId+"] gcm : "+contactGcmId);
                        datasource.close();
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter(RoosterConnectionService.NEW_MESSAGE);
        filter.addAction(RoosterConnectionService.CONTACT_AVAILABLE);
        filter.addAction(RoosterConnectionService.RECEIVING_FILE);
        filter.addAction(RoosterConnectionService.NEW_IMAGE);
        filter.addAction(RoosterConnectionService.IMAGE_SENT);
        filter.addAction(RoosterConnectionService.IMAGE_DELIVERED);
        filter.addAction(RoosterConnectionService.MESSAGE_SENT);
        filter.addAction(RoosterConnectionService.MESSAGE_DELIVERED);
        filter.addAction(RoosterConnectionService.MESSAGE_READ);
        filter.addAction(RoosterConnectionService.CAN_SEND_MESSAGE);
        filter.addAction(RoosterConnectionService.USER_PRESENCE_RESULT);
        registerReceiver(mBroadcastReceiver,filter);

        //-------------TypingIndicator---------------------//
        mTypingIndicatorReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(action.equals(RoosterConnectionService.BUDDY_IS_COMPOSING)){
                    Log.d(TAG,"Receving typing event ...");
                    typingIndicator.setText("typing ...");
                }else if(action.equals(RoosterConnectionService.BUDDY_HAS_STOPPED_COMPOSING)){
                    Log.d(TAG,"Receving pause event ...");
                    typingIndicator.setText("");
                }
            }
        };
        IntentFilter typingFilter = new IntentFilter();
        typingFilter.addAction(RoosterConnectionService.BUDDY_IS_COMPOSING);
        typingFilter.addAction(RoosterConnectionService.BUDDY_HAS_STOPPED_COMPOSING);
        registerReceiver(mTypingIndicatorReceiver, typingFilter);

        updateStoredMessageStatus();
    }

    @Override
    public void onIsTypingModified(int isTyping) {
        if(isTyping==0){
            //Typing is paused
            Intent intent = new Intent(RoosterConnectionService.ME_HAS_STOPPED_COMPOSING);
            intent.putExtra(RoosterConnectionService.BUNDLE_TO, contactJid);
            sendBroadcast(intent);
        }
        if(isTyping==1){
            //Typing started
            Intent intent = new Intent(RoosterConnectionService.ME_IS_COMPOSING);
            intent.putExtra(RoosterConnectionService.BUNDLE_TO, contactJid);
            sendBroadcast(intent);
        }
    }

    public void buddyProfileClick(View v){
        Log.d(TAG, "Checking buddy profile");
        Intent intent = new Intent(ChatActivity.this, BuddyProfile.class);
        intent.putExtra("buddy_jid", contactJid);
        intent.putExtra("ppic", buddyPpic);
        intent.putExtra("status", buddyStatus);
        intent.putExtra("name", buddyName);
        startActivity(intent);
    }

    @Override
    public void onBackPressed(){
        goBack();
    }
    public void backClick(View v){
        goBack();
    }

    public void goBack(){
        //Intent intent = new Intent(this, ContactListActivity.class);
        Intent intent = new Intent(this, XmppChatActivity.class);
        intent.putExtra("jid", contactJid);
        intent.putExtra("available", "unavailable");
        if(isAvailable) intent.putExtra("available", "available");
        startActivity(intent);
    }

    @Override
    protected void onUserLeaveHint(){
        Log.d("onUserLeaveHint","Home button pressed");
        Intent intent = new Intent(RoosterConnectionService.CHECK_HOME_BUTTON_PRESSED);
        sendBroadcast(intent);
        super.onUserLeaveHint();

    }

    @Override
    public void processFinish(JSONObject output) {
        Log.d(TAG, output.toString());
    }

    @Override
    public void processError(String error) {

    }
}
