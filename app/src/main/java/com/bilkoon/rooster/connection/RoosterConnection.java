package com.bilkoon.rooster.connection;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/*import com.blikoon.rooster.SplashActivity;
import com.blikoon.rooster.model.Contact;
import com.blikoon.rooster.model.RegistrationInfo;
import com.blikoon.rooster.service.RoosterConnectionService;
import com.blikoon.rooster.util.GenearteUID;
import com.blikoon.rooster.util.SessionManager;*/

import com.ahmed.chatapplication.ChatActivity;
import com.ahmed.chatapplication.R;
import com.ahmed.chatapplication.SplashActivity;
import com.ahmed.chatapplication.app.AppController;
import com.bilkoon.rooster.database.ConversationDataSource;
import com.bilkoon.rooster.model.ChatMessage;
import com.bilkoon.rooster.model.Contact;
import com.bilkoon.rooster.model.RegistrationInfo;
import com.bilkoon.rooster.service.RoosterConnectionService;
//import com.bilkoon.rooster.util.CompareDates;
import com.bilkoon.rooster.util.SessionManager;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackException;
//import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
/*import org.jivesoftware.smack.filter.IQReplyFilter;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.IQ;*/
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.roster.RosterLoadedListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
//import org.jivesoftware.smack.util.StringTransformer;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.muc.Affiliate;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.ping.android.ServerPingWithAlarmManager;
import org.jivesoftware.smackx.privacy.PrivacyList;
import org.jivesoftware.smackx.privacy.PrivacyListListener;
import org.jivesoftware.smackx.privacy.PrivacyListManager;
import org.jivesoftware.smackx.privacy.packet.PrivacyItem;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jivesoftware.smackx.xdata.Form;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.os.SystemClock.sleep;

/**
 * Created by gakwaya on 4/28/2016.
 */
public class RoosterConnection implements ConnectionListener {

    private static final String TAG = "RoosterConnection";

    private final Context mApplicationContext;
    private String mUsername;
    private String mPassword;
    private String mServiceName;
    private int mPort = 5223;
    private XMPPTCPConnection mConnection;
    private Roster roster;
    private List<String> mContacts;
    private List<String> filterContacts;
    private List<String> jids;
    private String myJID;
    private String chatWithJid;

    private SessionManager sessionManager;
    private RegistrationInfo registrationInfo;

    private BroadcastReceiver uiThreadMessageReceiver;//Receives messages from the ui thread.

    private  ChatMessageListener messageListener;
    private PrivacyListManager privacyManager;
    private DeliveryReceiptManager deliveryReceiptManager;
    private boolean doRegister = false;

    XMPPTCPConnectionConfiguration.Builder builder;

    private String addfriend = "";
    private String removefriend = "";

    MultiUserChatManager manager;
    MultiUserChat multiUserChat;
    private String eventsRoomOwner = "lebanexams";

    public static String xmppServer;
    private boolean appInBackground = false;
    private int disconnectAutomaticallyTimeout = 3; // 3 minutes

    private ConversationDataSource datasource;
    private String me_buddy;

    public static enum ConnectionState {
        CONNECTED ,AUTHENTICATED, CONNECTING ,DISCONNECTED, RECONNECTED;
    }

    public static enum LoggedInState {
        LOGGED_IN , LOGGED_OUT;
    }

    public RoosterConnection( Context context){
        Log.d(TAG,"RoosterConnection Constructor called.");
        datasource = new ConversationDataSource(context);
        sessionManager = new SessionManager(context);
        mApplicationContext = context.getApplicationContext();
        registrationInfo = sessionManager.loadRegistrationInfo();
        myJID = registrationInfo.getJid();
        Log.d(TAG, "Logging in as : "+myJID);
        mPassword = registrationInfo.getPassword();
        if( myJID != null){
            mUsername = myJID.split("@")[0];
            mServiceName = myJID.split("@")[1];
        }else{
            mUsername ="";
            mServiceName="";
        }

    }

    private void init(){
        filterContacts = new ArrayList<String>();
        mUsername = myJID.split("@")[0];
        mServiceName = myJID.split("@")[1];
        Log.d(TAG, "Connecting to server " + mServiceName);
        xmppServer = mServiceName;
        builder = XMPPTCPConnectionConfiguration.builder();
        builder.setServiceName(mServiceName);
        builder.setPort(mPort);

        //builder.setDebuggerEnabled(true);

        if(!doRegister) builder.setUsernameAndPassword(mUsername, mPassword);
        else builder.setUsernameAndPassword("", "");
        builder.setResource("Rooster");
        //Set up the ui thread broadcast message receiver.
        setupUiThreadBroadCastMessageReceiver();
        mConnection = new XMPPTCPConnection(builder.build());
        mConnection.addConnectionListener(this);
        try {
            mConnection.connect();
        } catch (SmackException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMPPException e) {
            e.printStackTrace();
        }
        deliveryReceiptManager = DeliveryReceiptManager.getInstanceFor(mConnection);
        deliveryReceiptManager.setAutoReceiptMode(DeliveryReceiptManager.AutoReceiptMode.always);
        deliveryReceiptManager.addReceiptReceivedListener(new ReceiptReceivedListener() {
            @Override
            public void onReceiptReceived(final String fromid,
                                          final String toid, final String msgid,
                                          final Stanza packet) {
                //Log.d(TAG, "Message is delivered from " + fromid);
                Intent intent = new Intent(RoosterConnectionService.MESSAGE_DELIVERED);
                intent.putExtra("jid", fromid);
                intent.putExtra("stanza_id", msgid);
                mApplicationContext.sendBroadcast(intent);
            }
        });
    }
    public void register(){
        doRegister = true;
        init();
        AccountManager accountManager = AccountManager.getInstance(mConnection);
        try {
           /* Collection<String> acAtr = accountManager.getAccountAttributes();
            System.out.println("Account Instructions = "+accountManager.getAccountInstructions());
            for (String string : acAtr) {
                System.out.println("ACCNT ATR = " + string);
            }*/
            Log.d(TAG, "Registering new account["+mUsername+"]");
            accountManager.createAccount(mUsername, mPassword);
            Log.d(TAG, "Created account successfully");
            Intent intent = new Intent(RoosterConnectionService.UI_REGISTERED);
            mApplicationContext.sendBroadcast(intent);
            login();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
            doRegister = false;
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
            doRegister = false;
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
            doRegister = false;
        } catch (XMPPException e) {
            e.printStackTrace();
            doRegister = false;
        } catch (IOException e) {
            e.printStackTrace();
            doRegister = false;
        } catch (SmackException e) {
            e.printStackTrace();
            doRegister = false;
        }
        if(!doRegister) ReconnectionManager.getInstanceFor(mConnection).disableAutomaticReconnection();
    }

    private boolean chatActivityIsRunning = false;
    private boolean chatCheckerTimerFinised = false;
    public void login() throws IOException,XMPPException,SmackException{
        if(!doRegister) init();
        removefriend = addfriend = "";
        mConnection.login(mUsername, mPassword);
        messageListener = new ChatMessageListener() {
            @Override
            public void processMessage(Chat chat, Message message) {
                //Log.d(TAG, "Message : "+message.toString());
                ///ADDED
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        chatActivityIsRunning = sessionManager.loadActivityIsRunning("ChatActivity");
                        chatCheckerTimerFinised = true;
                    }
                }, 300);

                while (!chatCheckerTimerFinised){

                }
                chatCheckerTimerFinised = false;

                Log.d(TAG, "message.getBody() :" + message.getBody());
                Log.d(TAG, "message.getFrom() :" + message.getFrom());

                String from = message.getFrom();
                String contactJid = "";
                String bareJid = "";
                if (from.contains("/")) {
                    contactJid = from.split("/")[0];
                    Log.d(TAG, "The real jid is :" + contactJid);
                } else {
                    contactJid = from;
                }
                bareJid = contactJid.split("@")[0];
                me_buddy = mUsername + "_"+bareJid;
                if (message.getBody()!= null) {
                    /*if(message.getBody().contains("add_me")){
                        if(contactJid.equals(mUsername)) return;
                        int id = findContactByJid(contactJid);
                        if(id==-1) addContact(new Contact(contactJid));
                        return;
                    }else if(message.getBody().contains("remove_me")){
                        if(contactJid.equals(mUsername)) return;
                        int id = findContactByJid(contactJid);
                        if(id!=-1) removeContact(new Contact(contactJid));
                    }*/
                    Intent intent = null;
                    if(message.getBody().contains("~seen")){
                        intent = new Intent(RoosterConnectionService.MESSAGE_READ);
                        String stanzaId = message.getBody().split(";")[1];
                        intent.putExtra("stanza_id", stanzaId);
                        if(chatActivityIsRunning) mApplicationContext.sendBroadcast(intent);
                        else{
                            //store information in database
                            Log.d(TAG, "Saving seen stamp in database");
                        }
                        return;
                    }else if(message.getBody().contains("~image_delivered")){
                        intent = new Intent(RoosterConnectionService.IMAGE_DELIVERED);
                        final Intent imageIntent = intent;
                        String stanzaId = message.getBody().split(";")[1];
                        intent.putExtra("stanza_id", stanzaId);
                        if(chatActivityIsRunning) mApplicationContext.sendBroadcast(imageIntent);
                        else {
                            //store information in database
                            Log.d(TAG, "Saving image delivered stamp in database");
                        }
                        return;
                    }
                    intent = new Intent(RoosterConnectionService.NEW_MESSAGE);
                    //Bundle up the intent and send the broadcast.
                    intent.setPackage(mApplicationContext.getPackageName());
                    intent.putExtra(RoosterConnectionService.BUNDLE_FROM_JID, contactJid);
                    intent.putExtra(RoosterConnectionService.BUNDLE_MESSAGE_BODY, message.getBody());
                    intent.putExtra("stanza_id", message.getStanzaId());
                    if(chatActivityIsRunning) mApplicationContext.sendBroadcast(intent);
                    else{
                        // display notification
                        // save message to display later in the interface
                        Log.d(TAG, "Saving message in database");
                        ChatMessage chatMessage = new ChatMessage(message.getBody(), false, false);
                        chatMessage.setStanzaId(message.getStanzaId());
                        chatMessage.setMessageStatus("");
                        DateFormat sdf = new SimpleDateFormat("MM-dd-yyyy hh:mm aa", Locale.ENGLISH);
                        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Beirut"));
                        String date = sdf.format(new Date());
                        chatMessage.setTime(date);
                        //database------------//
                        datasource.open();
                        datasource.createMessage(me_buddy, chatMessage);
                        datasource.close();
                        //createSoundNotification("incomming");
                        createMessageNotification("New Message", contactJid, chatMessage.getContent());
                        //--------------------//
                    }
                    Log.d(TAG, "Received message from :" + contactJid + " broadcast sent.");
                    ///ADDED
                }else if(message.getSubject()!=null){
                    String subject = message.getSubject();
                    if(chatActivityIsRunning) {
                        if (subject.contains("composing")) {
                            Log.d(TAG, contactJid + " is typing...");
                            Intent intent = new Intent(RoosterConnectionService.BUDDY_IS_COMPOSING);
                            mApplicationContext.sendBroadcast(intent);
                        } else if (subject.contains("paused")) {
                            Log.d(TAG, contactJid + " stopped");
                            Intent intent = new Intent(RoosterConnectionService.BUDDY_HAS_STOPPED_COMPOSING);
                            mApplicationContext.sendBroadcast(intent);
                        }
                    }
                }
            }
        };
        //The snippet below is necessary for the message listener to be attached to our connection.
        ChatManager.getInstanceFor(mConnection).addChatListener(new ChatManagerListener() {
            @Override
            public void chatCreated(Chat chat, boolean createdLocally) {
                //If the line below is missing ,processMessage won't be triggered and you won't receive messages.
                chat.addMessageListener(messageListener);
            }
        });
        FileTransferManager receiveFilemanager = FileTransferManager.getInstanceFor(mConnection);
        receiveFilemanager = FileTransferManager.getInstanceFor(mConnection);
        receiveFilemanager.addFileTransferListener(new FileTransferListener() {
            public void fileTransferRequest(FileTransferRequest request) {
                IncomingFileTransfer transfer = request.accept();
                Log.d(TAG, "Receiving file : "+ request.getFileName());
                try {
                    File path = Environment.getExternalStorageDirectory();
                    File dir = new File (path.getAbsolutePath() + "/rooster");
                    dir.mkdirs();
                    String extension = request.getDescription().split(";")[0];
                    String stanzaId = request.getDescription().split(";")[1];
                    //String randomName = GenearteUID.getFrom()+""+request.getDescription();
                    String randomName = stanzaId+""+extension;
                    Log.d(TAG, "Saving file : "+randomName);
                    Log.d(TAG, "file stanza id: "+stanzaId);
                    File file = new File(dir, randomName);
                    transfer.recieveFile(file);
                    Long fileSize = request.getFileSize();

                    Intent receivingInent = new Intent(RoosterConnectionService.RECEIVING_FILE);
                    receivingInent.putExtra("stanza_id", stanzaId);
                    mApplicationContext.sendBroadcast(receivingInent);
                    while(file.length()==0) {
                        //Log.d(TAG, "Receiving ... " + transfer.getProgress());
                        //Log.d(TAG, "Still saving : "+file.length()+"...file of size : "+fileSize);
                    }
                    String contactJid = "";
                    if (request.getRequestor().contains("/")) {
                        contactJid = request.getRequestor().split("/")[0];
                        Log.d(TAG, "The real jid is :" + contactJid);
                    } else {
                        contactJid = request.getRequestor();
                    }
                    //Bundle up the intent and send the broadcast.
                    Intent intent = new Intent(RoosterConnectionService.NEW_IMAGE);
                    intent.setPackage(mApplicationContext.getPackageName());
                    intent.putExtra(RoosterConnectionService.BUNDLE_FROM_JID, contactJid);
                    intent.putExtra(RoosterConnectionService.BUNDLE_MESSAGE_BODY, file.getPath());
                    intent.putExtra("stanza_id", stanzaId);
                    sendMessage("~image_delivered;"+stanzaId, contactJid, stanzaId);
                    mApplicationContext.sendBroadcast(intent);
                } catch (SmackException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        jids = new ArrayList<String>();
        setMyPresence(Presence.Type.available, "I am in office");

        ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(mConnection);
        reconnectionManager.disableAutomaticReconnection();
        /*reconnectionManager.setEnabledPerDefault(true);
        reconnectionManager.enableAutomaticReconnection();*/

        ServerPingWithAlarmManager serverPingWithAlarmManager = ServerPingWithAlarmManager.getInstanceFor(mConnection);
        serverPingWithAlarmManager.setEnabled(true);

        createGroupForEvents();
    }

    private void setupUiThreadBroadCastMessageReceiver(){
        uiThreadMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Check if the Intents purpose is to send the message.
                String action = intent.getAction();
                Log.d(TAG, "Action : "+action);
                if(action.equals(RoosterConnectionService.ME_IS_COMPOSING)) {
                    Log.d(TAG, "User ["+mUsername+"] is sending composing event to ["+intent.getStringExtra(RoosterConnectionService.BUNDLE_TO)+"]");
                    sendTypingStatus("composing", intent.getStringExtra(RoosterConnectionService.BUNDLE_TO));
                }else if(action.equals(RoosterConnectionService.ME_HAS_STOPPED_COMPOSING)){
                    Log.d(TAG, "User ["+mUsername+"] is sending pause event to ["+intent.getStringExtra(RoosterConnectionService.BUNDLE_TO)+"]");
                    sendTypingStatus("paused", intent.getStringExtra(RoosterConnectionService.BUNDLE_TO));
                }else if(action.equals(RoosterConnectionService.SEND_MESSAGE)){
                    sendMessage(intent.getStringExtra(RoosterConnectionService.BUNDLE_MESSAGE_BODY),
                            intent.getStringExtra(RoosterConnectionService.BUNDLE_TO), intent.getStringExtra("stanza_id"));
                }else if(action.equals(RoosterConnectionService.SEND_IMAGE)){
                    Log.d(TAG, "Sending image to ["+intent.getStringExtra(RoosterConnectionService.BUNDLE_TO)+"]");
                    String path = intent.getStringExtra("image_path");
                    String stanza_id = intent.getStringExtra("stanza_id");
                    sendFileToFriend(path, intent.getStringExtra(RoosterConnectionService.BUNDLE_TO), stanza_id);
                }else if(action.equals(RoosterConnectionService.DISCONNECT)){
                    Thread disconnectThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //ReconnectionManager.getInstanceFor(mConnection).disableAutomaticReconnection();
                            disconnect(false);
                        }
                    });
                    appInBackground = false;
                    disconnectThread.start();
                }else if(action.equals(RoosterConnectionService.ADD_FRIEND)){
                    addfriend = intent.getStringExtra("jid");
                    addContact(new Contact(intent.getStringExtra("jid")));
                }else if(action.equals(RoosterConnectionService.REMOVE_FRIEND)){
                    removefriend = intent.getStringExtra("jid");
                    removeContact(new Contact(intent.getStringExtra("jid")));
                }else if(action.equals(RoosterConnectionService.BLOCK_LIST)){
                    getBlockList();
                }else if(action.equals(RoosterConnectionService.BLOCK_FRIEND)){
                    blockContact(intent.getStringExtra("jid"), false);
                }else if(action.equals(RoosterConnectionService.UNBLOCK_FRIEND)){
                    blockContact(intent.getStringExtra("jid"), true);
                }else if(action.equals(RoosterConnectionService.SET_MY_PRESENCE)){
                    Log.d(TAG, "Setting My presence to available");
                    setMyPresence(Presence.Type.available, "I am in office");
                }else if(action.equals(RoosterConnectionService.GET_USER_PRESENCE)){
                    getContactPresence(intent.getStringExtra("jid"));
                }else if(action.equals(RoosterConnectionService.CONTACT_LIST_SERVER)){
                    getContacts();
                }else if(action.equals(RoosterConnectionService.LOAD_VCARD)){
                    loadVCard(intent.getStringExtra("jid"));
                }else if(action.equals(RoosterConnectionService.SAVE_VCARD)){
                    Log.d(TAG, "My JID : " + intent.getStringExtra("jid"));
                    String jid = intent.getStringExtra("jid");
                    String name = intent.getStringExtra("name");
                    String status = intent.getStringExtra("status");
                    String ppic = intent.getStringExtra("ppic");
                    Contact contact = new Contact(jid);
                    contact.setName(name);
                    contact.setStatus(status);
                    Log.d(TAG, "Saving contact vcard : "+contact.getName()+" having profile pic ["+ppic+"]");
                    if(intent.getStringExtra("ppic")!=null) contact.setAvatar(ppic);
                    if(!ppic.equals("") && !ppic.equals(null)) contact.setAvatar(ppic);
                    saveVCard(contact);
                }else if(action.equals(RoosterConnectionService.CONTACT_PROFILES)){
                    String jid = intent.getStringExtra("jid");
                    loadProfiles(jid);
                }else if(action.equals(RoosterConnectionService.VCARD_EVENT)){
                    sendEventMessage("vcard", intent.getStringExtra("jid"));
                }else if(action.equals(RoosterConnectionService.CURRENT_CONVERSATION)){
                    chatWithJid = intent.getStringExtra("jid");
                    Log.d(TAG, "Chatting with ["+chatWithJid+"]");
                }else if(action.equals(RoosterConnectionService.APP_IN_BACKGROUND)){
                    //appInBackground = true;
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            appInBackground = true;
                            disconnect(false);
                            mApplicationContext.stopService(new Intent(mApplicationContext, RoosterConnectionService.class));
                        }
                    }, 1000*60*disconnectAutomaticallyTimeout);
                    /*Thread disconnectThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            disconnect(true);
                            mApplicationContext.stopService(new Intent(mApplicationContext, RoosterConnectionService.class));
                        }
                    });
                    disconnectThread.start();*/
                }else if(action.equals(RoosterConnectionService.APP_IN_FOREGROUND)){
                    appInBackground = false;
                    Log.d(TAG, "Trying to login again ...");
                }else if(action.equals(RoosterConnectionService.CHECK_HOME_BUTTON_PRESSED)){
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Log.d(TAG, "XmppChatActivity is running? " + sessionManager.loadActivityIsRunning("XmppChatActivity"));
                            if(!sessionManager.loadActivityIsRunning("XmppChatActivity")){
                                appInBackground = true;
                                disconnect(false);
                                mApplicationContext.stopService(new Intent(mApplicationContext, RoosterConnectionService.class));
                            }
                        }
                    }, 1000*60*disconnectAutomaticallyTimeout);
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(RoosterConnectionService.ME_IS_COMPOSING);
        filter.addAction(RoosterConnectionService.ME_HAS_STOPPED_COMPOSING);
        filter.addAction(RoosterConnectionService.LOAD_VCARD);
        filter.addAction(RoosterConnectionService.SAVE_VCARD);
        filter.addAction(RoosterConnectionService.SEND_MESSAGE);
        filter.addAction(RoosterConnectionService.SEND_IMAGE);
        filter.addAction(RoosterConnectionService.SEND_FILE);
        filter.addAction(RoosterConnectionService.DISCONNECT);
        filter.addAction(RoosterConnectionService.ADD_FRIEND);
        filter.addAction(RoosterConnectionService.REMOVE_FRIEND);
        filter.addAction(RoosterConnectionService.BLOCK_LIST);
        filter.addAction(RoosterConnectionService.BLOCK_FRIEND);
        filter.addAction(RoosterConnectionService.UNBLOCK_FRIEND);
        filter.addAction(RoosterConnectionService.SET_MY_PRESENCE);
        filter.addAction(RoosterConnectionService.GET_USER_PRESENCE);
        filter.addAction(RoosterConnectionService.CONTACT_LIST_SERVER);
        filter.addAction(RoosterConnectionService.CONTACT_PROFILES);
        filter.addAction(RoosterConnectionService.VCARD_EVENT);
        filter.addAction(RoosterConnectionService.CURRENT_CONVERSATION);
        filter.addAction(RoosterConnectionService.APP_IN_BACKGROUND);
        filter.addAction(RoosterConnectionService.APP_IN_FOREGROUND);
        filter.addAction(RoosterConnectionService.CHECK_HOME_BUTTON_PRESSED);
        mApplicationContext.registerReceiver(uiThreadMessageReceiver,filter);
    }

    public void clearLoginRegistrationInfo(){
        sessionManager.saveRegistrationInfo("","", false);
    }

    private void sendMessage ( String body ,String toJid, String stanzaId){
        Log.d(TAG,"Sending message to :"+ toJid);
        if(mConnection!=null) {
            Chat chat = ChatManager.getInstanceFor(mConnection).createChat(toJid, messageListener);
            try {
                Message message = new Message();
                message.setStanzaId(stanzaId);
                message.setBody(body);
                DeliveryReceiptRequest.addTo(message);
                chat.sendMessage(message);
                Intent intent = new Intent(RoosterConnectionService.MESSAGE_SENT);
                intent.putExtra("stanza_id", stanzaId);
                mApplicationContext.sendBroadcast(intent);
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendTypingStatus(String type, String toJid){
        if(mConnection==null) {
            init();
            return;
        }
        Chat typingChat = ChatManager.getInstanceFor(mConnection).createChat(toJid,messageListener);
        try{
            Message status = new Message();
            status.setBody(null);
            status.setSubject(type);
            typingChat.sendMessage(status);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    public void disconnect(boolean keepUiThreadReceiver){
        Log.d(TAG,"Disconnecting from server "+ mServiceName);
        if (mConnection != null){
            mConnection.disconnect();
        }

        mConnection = null;
        // Unregister the message broadcast receiver.
        if(!keepUiThreadReceiver) {
            if (uiThreadMessageReceiver != null) {
                mApplicationContext.unregisterReceiver(uiThreadMessageReceiver);
                uiThreadMessageReceiver = null;
            }
        }

    }


    @Override
    public void connected(XMPPConnection connection) {
        RoosterConnectionService.sConnectionState=ConnectionState.CONNECTED;
        Log.d(TAG,"Connected Successfully");
    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        RoosterConnectionService.sConnectionState=ConnectionState.AUTHENTICATED;
        Log.d(TAG,"Authenticated Successfully");
        doRegister = false;
        setupRoster();
        showContactListActivityWhenAuthenticated();
    }

    @Override
    public void connectionClosed() {
        RoosterConnectionService.sConnectionState=ConnectionState.DISCONNECTED;
        Log.d(TAG,"Connectionclosed()");
        if(!appInBackground) {
            Intent intent = new Intent(mApplicationContext, SplashActivity.class);
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            mApplicationContext.startActivity(intent);
        }
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        RoosterConnectionService.sConnectionState=ConnectionState.DISCONNECTED;
        Intent intent = new Intent(RoosterConnectionService.CAN_SEND_MESSAGE);
        intent.putExtra("can_send_message", false);
        mApplicationContext.sendBroadcast(intent);
        Log.d(TAG,"ConnectionClosedOnError, error "+ e.toString());
    }

    @Override
    public void reconnectingIn(int seconds) {
        RoosterConnectionService.sConnectionState = ConnectionState.CONNECTING;
        Log.d(TAG,"ReconnectingIn() ");
    }

    @Override
    public void reconnectionSuccessful() {
        RoosterConnectionService.sConnectionState = ConnectionState.RECONNECTED;
        Intent intent = new Intent(RoosterConnectionService.CAN_SEND_MESSAGE);
        intent.putExtra("can_send_message", true);
        mApplicationContext.sendBroadcast(intent);
        Log.d(TAG,"ReconnectionSuccessful()");
    }

    @Override
    public void reconnectionFailed(Exception e) {
        RoosterConnectionService.sConnectionState = ConnectionState.DISCONNECTED;
        Intent intent = new Intent(RoosterConnectionService.CAN_SEND_MESSAGE);
        intent.putExtra("can_send_message", false);
        mApplicationContext.sendBroadcast(intent);
        Log.d(TAG,"ReconnectionFailed()");
    }

    private void showContactListActivityWhenAuthenticated(){
        Intent i = new Intent(RoosterConnectionService.UI_AUTHENTICATED);
        i.putExtra("can_send_message", true);
        i.setPackage(mApplicationContext.getPackageName());
        mApplicationContext.sendBroadcast(i);
        Log.d(TAG,"Sent the broadcast that we are authenticated");
    }
    //--------------------SEND FILE------------------------//
    private void sendFileToFriend(String path, String jid, String stanzaId){
        // Create the outgoing file transfer
        Log.d(TAG, "Transfering file to ["+jid+"]");
        FileTransferManager sendFilemanager = FileTransferManager.getInstanceFor(mConnection);
        final OutgoingFileTransfer transfer = sendFilemanager.createOutgoingFileTransfer(jid+"/Rooster");
        // Send the file
        try {
            File file = new File(path);
            if(file.exists()) {
                String extension = path.substring(path.lastIndexOf("."));
                Intent intent = new Intent(RoosterConnectionService.IMAGE_SENT);
                intent.putExtra("stanza_id", stanzaId);
                transfer.sendFile(file, extension+";"+stanzaId);
                mApplicationContext.sendBroadcast(intent);
                while (!transfer.isDone()) {
                    if (transfer.getStatus().equals(FileTransfer.Status.error)) {
                        Log.d(TAG, "ERROR!!! " + transfer.getError());
                    } else {
                        FileTransfer.Status status = transfer.getStatus();
                        double progress = transfer.getProgress();
                        Log.d(TAG, status.name());
                        Log.d(TAG, String.valueOf(progress));
                        Log.d(TAG, "Bytes sent : " + transfer.getBytesSent());
                    }
                    sleep(1000);
                }
            }else Log.d(TAG, "File does not exist!");
        } catch (SmackException e) {
            e.printStackTrace();
        }
    }
    //---------------------Roster--------------------------//
    private void setupRoster(){
        roster = Roster.getInstanceFor(mConnection);
        //roster.setRosterLoadedAtLogin(true);
        roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
        roster.addRosterListener(rosterListener);
        roster.addRosterLoadedListener(rosterLoadedListener);
    }
    private RosterLoadedListener rosterLoadedListener = new RosterLoadedListener() {
        @Override
        public void onRosterLoaded(Roster roster) {
            Log.d(TAG, "Roster has loaded");
            getContacts();
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    setupPrivacy(jids);
                }
            }, 2000);
        }
    };
    private RosterListener rosterListener = new RosterListener() {
        @Override
        public void entriesAdded(Collection<String> addresses) {
            Log.d(TAG, "Adding roster entry ... ");
            Intent intent = new Intent(RoosterConnectionService.FRIEND_ADDED);
            ArrayList<HashMap<String, String>> contacts = new ArrayList<HashMap<String, String>>();
            //ArrayList<HashMap<String, String>>
            for(String address : addresses){
                HashMap<String, String> userInfo = new HashMap<String, String>();
                String available = roster.getPresence(address).getType().toString();
                userInfo.put("jid", address);
                userInfo.put("available", available);
                contacts.add(userInfo);
            }
            intent.putExtra("contacts", contacts);
            mApplicationContext.sendBroadcast(intent);
            //sendMessage("add_me", addfriend);
        }

        @Override
        public void entriesUpdated(Collection<String> addresses) {

        }

        @Override
        public void entriesDeleted(Collection<String> addresses) {
            for(String address : addresses) {
                if (address.equals(removefriend)) {
                    Log.d(TAG, "Removing roster entry ... ");
                    Intent intent = new Intent(RoosterConnectionService.FRIEND_REMOVED);
                    intent.putExtra("jid", removefriend);
                    mApplicationContext.sendBroadcast(intent);
                    //sendMessage("remove_me", removefriend);
                    removefriend = "";
                    break;
                }
            }
        }

        @Override
        public void presenceChanged(Presence presence) {
            Log.d(TAG, "Presence info : " + presence);
            Intent intent = new Intent(RoosterConnectionService.CONTACT_AVAILABLE);
            String type = presence.getType().toString();
            intent.putExtra("jid", presence.getFrom().split("/")[0]);
            intent.putExtra("available", type);
            Log.d(TAG, "Presence of "+presence.getFrom().split("/")[0]+" has changed to "+ type);
            mApplicationContext.sendBroadcast(intent);
        }
    };

    private void getContacts() {
        final Intent intent = new Intent(RoosterConnectionService.CONTACT_LIST);
        ArrayList<String> contacts = new ArrayList<String>();
        ArrayList<String> available = new ArrayList<String>();
        if (mConnection != null && mConnection.isConnected()) {
            Collection<RosterEntry> entries = roster.getEntries();
            Log.d(TAG, "Number of friends is : " + entries.size());
            Log.d(TAG, "Getting contacts...");
            int count = 0;
            mContacts = new ArrayList<String>();
            for (RosterEntry entry : entries) {
                Presence entryPresence = roster.getPresence(entry.getUser());
                contacts.add(entry.getUser().toString());
                String type = entryPresence.getType().toString();
                if (type.equals("available")) available.add("available");
                else if (type.equals("unavailable")) available.add("unavailable");
                mContacts.add(entry.getUser().toString());
                jids.add(entry.getUser().toString());
                count++;
                Log.e(TAG, "USER : " + entry.getUser() + "\n" + "Available : " + type.toString());
            }
            intent.putStringArrayListExtra("jids", contacts);
            intent.putStringArrayListExtra("available", available);
            mApplicationContext.sendBroadcast(intent);
            //---------------------Creating Rosters------------------------------//
            Set<String> savedJids = sessionManager.loadServerJids();
            Iterator iter = savedJids.iterator();
            ArrayList<String> serverContacts = new ArrayList<String>();
            while (iter.hasNext()) {
                String serverJid = iter.next().toString().trim();
                if(!serverJid.equals(myJID)) serverContacts.add(serverJid);
            }
            Log.d(TAG, "Roster jids : " + mContacts.toString());
            Log.d(TAG, "Server jids : " + serverContacts.toString());
            addContacts(serverContacts);
            //------------------------------------------------------------------//
        }
    }

    private void addContacts(ArrayList<String> server_contacts) {
        int ln = server_contacts.size();
        for(int i=0;i<ln;i++){
            String server_jid = server_contacts.get(i);
            int len = mContacts.size();
            int count = 0;
            for(int j=0;j<len;j++){
                String roster_jid = mContacts.get(j);
                if(roster_jid.equals(server_jid)) break;
                count++;
            }
            if(count==len) {
                Log.d(TAG, "Server jid ["+server_jid+"] is not found in roster");
                addContact(new Contact(server_jid));
            }
        }
    }

    public void setMyPresence(Presence.Type type, String status){
        Presence presence = new Presence(type);
        presence.setStatus(status);
        presence.setPriority(24);
        try {
            mConnection.sendStanza(presence);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    private void getContactPresence(String jid){
        Intent intent = new Intent(RoosterConnectionService.USER_PRESENCE_RESULT);
        if(roster!=null && mConnection!=null){
            Presence presence = roster.getPresence(jid);
            Log.d(TAG, "Getting presence of contact ["+jid+"] : "+presence.getType().toString());
            intent.putExtra("jid", jid);
            intent.putExtra("available", presence.getType().toString());
            mApplicationContext.sendBroadcast(intent);
        }
    }

    private String getFriendPresence(String jid){
        if(roster!=null && mConnection!=null){
            Presence presence = roster.getPresence(jid);
            Log.d(TAG, "Getting prsence of contact ["+jid+"] : "+presence.getType().toString());
            return presence.getType().toString();
        }
        return null;
    }



    private void addContact(Contact user){
        if(mConnection!=null && mConnection.isConnected()){
            try {
                roster.createEntry(user.getJid(), user.getName(), null);
                Log.d(TAG, "Adding contact : "+user.getJid());
            }catch (SmackException.NotLoggedInException e) {
                e.printStackTrace();
            } catch (SmackException.NoResponseException e) {
                e.printStackTrace();
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            } catch (XMPPException.XMPPErrorException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void removeContact(Contact user){
        if(mConnection!=null && mConnection.isConnected()){
            try {
                RosterEntry entry = roster.getEntry(user.getJid());
                roster.removeEntry(entry);
                Log.d(TAG, "Removing contact : "+user.getJid());
            }catch (SmackException.NotLoggedInException e) {
                e.printStackTrace();
            } catch (SmackException.NoResponseException e) {
                e.printStackTrace();
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            } catch (XMPPException.XMPPErrorException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setupPrivacy(List<String> users) {
        if(mConnection!=null) {
            privacyManager = PrivacyListManager.getInstanceFor(mConnection);
            privacyManager.addListener(new PrivacyListListener() {
                @Override
                public void setPrivacyList(String s, List<PrivacyItem> items) {
                    Log.d(TAG, "privacy list [" + s + "]");
                    for (PrivacyItem item : items) {
                        Log.d(TAG, "Users : " + item.getValue());
                    }
                }

                @Override
                public void updatedPrivacyList(String s) {
                    Log.d(TAG, "privacy list[" + s + "] is updated");
                    List<PrivacyItem> pItems = getBlockList();
                    for (int i = 0; i < pItems.size(); i++)
                        Log.d(TAG, "User " + pItems.get(i).getValue() + " is " + pItems.get(i).isAllow());
                    if (pItems.size() > 0) {
                        PrivacyItem item = getBlockList().get(0);
                        Log.d(TAG, "User " + item.getValue() + " is " + item.isAllow());
                        Intent intent = new Intent();
                        if (!item.isAllow()) {
                            intent.setAction(RoosterConnectionService.FRIEND_BLOCKED);
                        } else {
                            intent.setAction(RoosterConnectionService.FRIEND_UNBLOCKED);
                            intent.putExtra("available", getFriendPresence(item.getValue()));
                        }
                        intent.putExtra("jid", item.getValue());
                        mApplicationContext.sendBroadcast(intent);
                    }
                }
            });
            try {
                Log.d(TAG, "Privacy is supported : " + privacyManager.isSupported());
                if (privacyManager.isSupported()) {
                    List<PrivacyItem> privacyItems = new ArrayList<PrivacyItem>();
                    List<PrivacyList> privacyLists = privacyManager.getPrivacyLists();
                    if (privacyLists.size() == 0) {
                        Log.d(TAG, "Privacylist size is " + privacyLists.size());
                        privacyItems.add(new PrivacyItem(PrivacyItem.Type.jid, "test@host.xmpp", true, 1));
                        privacyManager.createPrivacyList("newList", privacyItems);
                        privacyManager.setDefaultListName("newList");
                        privacyManager.setActiveListName("newList");
                    } else {
                        privacyManager.setDefaultListName("newList");
                        privacyManager.setActiveListName("newList");
                        PrivacyList privacyList = privacyManager.getPrivacyList("newList");
                        List<PrivacyItem> oldPrivacyItemList = privacyList.getItems();
                        for (int i = 0; i < oldPrivacyItemList.size(); i++) {
                            PrivacyItem item = oldPrivacyItemList.get(i);
                            Log.d(TAG, "User [" + item.getValue() + "] is " + item.isAllow());
                        }
                    }
                }
            } catch (SmackException.NoResponseException e) {
                e.printStackTrace();
            } catch (XMPPException.XMPPErrorException e) {
                e.printStackTrace();
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }
        }
    }

    private List<PrivacyItem> getBlockList() {
        List<PrivacyItem> privacyItems = new Vector<PrivacyItem>();
        try {
            if(privacyManager.getPrivacyList("newList") !=null){
                PrivacyList privacyList = privacyManager.getPrivacyList("newList");
                List<PrivacyItem> items = privacyList.getItems();
                for (PrivacyItem item : items) {
                    Log.d(TAG, "Users : " + item.getValue());
                    privacyItems.add(item);
                }
            }
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        return privacyItems;
    }

    private void blockContact(String user, boolean allowUser){
        String listName = "newList";
        List<PrivacyItem> privacyItems = new Vector<PrivacyItem>();
        PrivacyItem item = new PrivacyItem(PrivacyItem.Type.jid, user, allowUser, 1l);
        item.setFilterPresenceIn(allowUser);
        item.setFilterPresenceOut(allowUser);
        item.setFilterMessage(allowUser);
        item.setFilterIQ(allowUser);
        privacyItems.add(item);
        try {
            Log.d(TAG, "Blocking is allowed : "+privacyManager.isSupported()+"\nBlocking user [" + user + "] :"+ item.isAllow());
            if(privacyManager.isSupported()) privacyManager.updatePrivacyList(listName, privacyItems);
        } catch (XMPPException e) {
            System.out.println("PRIVACY_ERROR: " + e);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        }
    }

    //---------------------------------------------------------------------------------------------//
    private void loadProfiles(String jid){
        Log.d(TAG, "Loading contact VCard :[" + jid + "]");
        loadVCard(jid);
    }

    private void loadVCard(String jid){
        VCard vcard;
        VCard localVcard = new VCard();
        if(mConnection!=null) {
            Intent intent = new Intent(RoosterConnectionService.VCARD_LOADED);
            intent.putExtra("jid", jid);
            Log.d(TAG, "Loading profile of "+jid);
            VCardManager vCardManager = VCardManager.getInstanceFor(mConnection);
            try {
                vcard = vCardManager.loadVCard(jid);
                if(vcard.getFirstName()==null) {
                    intent.putExtra("name", "");
                    localVcard.setFirstName("");
                }
                else {
                    intent.putExtra("name", vcard.getFirstName());
                    localVcard.setFirstName(vcard.getFirstName());
                    Log.d(TAG, "Profile:\njid["+jid+"]\nName["+vcard.getFirstName()+"]");
                }
                if(vcard.getField("status")==null) {
                    intent.putExtra("status", "");
                    localVcard.setField("status", "");
                }
                else {
                    intent.putExtra("status", vcard.getField("status"));
                    localVcard.setField("status", vcard.getField("status"));
                    Log.d(TAG, "Status["+vcard.getField("status")+"]");
                }
                if(vcard.getAvatar()!=null) {
                    byte[] avatarByte = vcard.getAvatar();
                    if (avatarByte.length > 0) {
                        String path = Environment.getExternalStorageDirectory().getPath();
                        File dir = new File(path + "/.ppic");
                        dir.mkdirs();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(avatarByte, 0, avatarByte.length);
                        FileOutputStream out = new FileOutputStream(path + "/.ppic/ppic_" + jid + ".jpg");
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                        intent.putExtra("ppic", path + "/.ppic/ppic_" + jid + ".jpg");
                        localVcard.setField("avatar", path + "/.ppic/ppic_" + jid + ".jpg");
                        Long tsLong = System.currentTimeMillis();
                        DateFormat sdf = new SimpleDateFormat("yy-MM-dd hh:mm:ss a", Locale.ENGLISH);//2017-05-06 02:47:28 am ,,,,2017-05-06 05:09:27 am
                        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Beirut"));
                        Date netDate = (new Date(tsLong));
                        String dateStr = sdf.format(netDate);
                        sessionManager.saveVCardTimestamp(jid, dateStr);
                    }
                }
                sessionManager.saveVCardInfo(jid, localVcard);
                mApplicationContext.sendBroadcast(intent);
            } catch (SmackException.NoResponseException e) {
                e.printStackTrace();
                mApplicationContext.sendBroadcast(intent);
            } catch (XMPPException.XMPPErrorException e) {
                e.printStackTrace();
                mApplicationContext.sendBroadcast(intent);
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
                mApplicationContext.sendBroadcast(intent);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                mApplicationContext.sendBroadcast(intent);
            }
        }
    }

    public void saveVCard(Contact contact){
        Log.d(TAG, "Saving vcard info");
        VCardManager vCardManager = VCardManager.getInstanceFor(mConnection);
        VCard vcard = new VCard();
        vcard.setJabberId(contact.getJid());
        vcard.setNickName(contact.getName());
        vcard.setFirstName(contact.getName());
        vcard.setField("status", contact.getStatus());
        Log.d(TAG, "Profile image path "+contact.getAvatar());
        if(contact.getAvatar()!=null) {
            String imagePath = contact.getAvatar();
            File avatarFile = new File(imagePath);
            Log.d(TAG, "Avatar exists in [" + imagePath + "]: " + avatarFile.exists());
            if (avatarFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(avatarFile.getAbsolutePath());
                if (bitmap != null) {
                    // Take the avatar and convert it into a byte array:
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    // 90 refers the the compression quality. For PNG, the quality is ignored
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                    byte[] avatarByte = stream.toByteArray();
                    // Once you get the byte array from the image, set the byte array to the vCard avatar
                    vcard.setAvatar(avatarByte);
                }
            }
        }
        try {
            vCardManager.saveVCard(vcard);
            sendEventMessage("vcard", myJID);
            Intent intent = new Intent(RoosterConnectionService.VCARD_SAVED);
            mApplicationContext.sendBroadcast(intent);
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }
    //--------------Cutom events--------------------------------//
    private void createGroupForEvents(){
        manager = MultiUserChatManager.getInstanceFor(mConnection);
        //Log.d(TAG, mUsername + "Creating chat room : vcard@conference." + mConnection.getServiceName());
        multiUserChat = manager.getMultiUserChat("events@conference." + mConnection.getServiceName());
        Affiliate owner = null;
        if(myJID.equals(eventsRoomOwner+"@"+mConnection.getServiceName())) {
            try {
                //List<Affiliate> admins = multiUserChat.getAdmins();
                List<Affiliate> owners = multiUserChat.getOwners();
                if (owners.size() > 0) {
                    owner = owners.get(0);
                    Log.d(TAG, "Owner : " + owner.getJid());
                }
            /*if(admins.size()>0) {
                Affiliate admin = admins.get(0);
                Log.d(TAG, "Adminstrator : " + admin.getJid());
            }else{
                //multiUserChat.grantAdmin("lebanexams@"+mConnection.getServiceName());
            }*/
            } catch (SmackException.NoResponseException e) {
                e.printStackTrace();
            } catch (XMPPException.XMPPErrorException e) {
                e.printStackTrace();
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }
        }
        multiUserChat.addMessageListener(new MessageListener() {
            @Override
            public void processMessage(Message message) {
                /*System.out.println("Message listener Received message in send message: "
                        + (message != null ? message.getBody() : "NULL") + "  , Message sender :" + message.getFrom());*/
                if(message.getBody()!=null) {
                    String[] instruction = message.getBody().split(";");
                    String event = instruction[0];
                    String jid = instruction[1];
                    if (jid.equals(myJID)) return;
                    else {
                        Log.d(TAG, "Received " + event + " event from [" + jid + "]");
                        if (event.equals("vcard")) {
                            loadVCard(jid);
                        }
                    }
                }
            }
        });

        try {
            DiscussionHistory history = new DiscussionHistory();
            history.setMaxStanzas(0);
            if(owner!=null || !myJID.equals(eventsRoomOwner+"@"+mConnection.getServiceName())){
                if(owner!=null) Log.d(TAG, "Rooms hosted : "+multiUserChat.getRoom()+"\nRoom owner : " + owner.getJid());
                multiUserChat.join(mUsername, null, history, 30000);
                Log.d(TAG, "I have joined events group: " + multiUserChat.isJoined());
                return;
            }else{
                multiUserChat.create(mUsername);
                multiUserChat.join(mUsername, null, history, 30000);
                //multiUserChat.destroy("was tesing room", null);
                // room is now created by locked
                Form form = multiUserChat.getConfigurationForm();
                Form answerForm = form.createAnswerForm();
                answerForm.setAnswer("muc#roomconfig_persistentroom", true);
                multiUserChat.sendConfigurationForm(answerForm);
                // sending the configuration form unlocks the room
            }
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException e) {
            e.printStackTrace();
        }

    }

    private void sendEventMessage(String event, String msg){
        Log.d(TAG, "Sending "+event+" Event from : "+msg);
        Message message = new Message();
        message.setBody(event+";"+msg);
        
        message.setType(Message.Type.chat);
        try {
            multiUserChat.sendMessage(message);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

/*    private void createSoundNotification(String type){
        NotificationManager soundNotification = (NotificationManager) mApplicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
        Uri alarmSound;
        if(type.equals("incomming")) alarmSound = Uri.parse("android.resource://" + mApplicationContext.getPackageName() + "/" + R.raw.incoming);
        else alarmSound = Uri.parse("android.resource://" + mApplicationContext.getPackageName() + "/" + R.raw.send_message);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mApplicationContext);
        builder.setSound(alarmSound);
        soundNotification.notify(1, builder.build());
    }*/

    public NotificationManager mNotificationManager;
    public Intent resultIntent;
    public int notifyID;
    static AtomicInteger at = new AtomicInteger(9001);
    private void createMessageNotification(String title, String jid, String message){
        resultIntent = new Intent(mApplicationContext, ChatActivity.class);
        Log.d(TAG, "Notification Status : ");
        resultIntent.putExtra("jid", jid);
        //resultIntent.putExtra("status", preferences.loadSmackStatus(""+user.getServerId()));
        //resultIntent.putExtra("phone", preferences.loadSmackPhone(""+user.getServerId()));
        //resultIntent.putExtra("loadHistory", true);
        //resultIntent.putExtra("incommingMessage", true);

        Uri alarmSound = Uri.parse("android.resource://" + mApplicationContext.getPackageName() + "/" + R.raw.incoming);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(mApplicationContext, 0,
                resultIntent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder mNotifyBuilder;
        mNotificationManager = (NotificationManager) mApplicationContext.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotifyBuilder = new NotificationCompat.Builder(mApplicationContext)
                .setContentTitle(title)
                .setContentText(message)
                .setSound(alarmSound)
                .setSmallIcon(R.drawable.ic_stat_ic_notification);
        // Set pending intent
        mNotifyBuilder.setContentIntent(resultPendingIntent);
        // Set Vibrate, Sound and Light
        int defaults = 0;
        defaults = defaults | Notification.DEFAULT_LIGHTS;
        defaults = defaults | Notification.DEFAULT_VIBRATE;
        defaults = defaults | Notification.DEFAULT_SOUND;

        mNotifyBuilder.setDefaults(defaults);

        //if(isImage==1)    msg = title +" sent you an image";
        //mNotifyBuilder.setContentText(msg);
        // Set autocancel
        mNotifyBuilder.setAutoCancel(true);
        // Post a notification
        notifyID = at.getAndAdd(1);
        mNotificationManager.notify(notifyID, mNotifyBuilder.build());

        int count = sessionManager.loadNotifyCount(title);
        Log.d(TAG, "Notification count "+count);
        sessionManager.saveNotifyId(title, count, notifyID);
        sessionManager.saveNotifyCount(title, count+1);
        // Set the content for Notification
    }
}
