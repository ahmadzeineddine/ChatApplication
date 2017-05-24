package com.ahmed.chatapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.bilkoon.rooster.connection.RoosterConnection;
import com.bilkoon.rooster.fragment.ContactsFragment;
import com.bilkoon.rooster.fragment.ConversationsFragment;
import com.bilkoon.rooster.fragment.StudentsHallFragment;
import com.bilkoon.rooster.model.RegistrationInfo;
import com.bilkoon.rooster.service.RoosterConnectionService;
import com.bilkoon.rooster.util.SessionManager;

/**
 * Created by Ahmed on 5/1/2017.
 */

public class XmppChatActivity extends AppCompatActivity {//implements VolleyJsonResponse {
    private static String TAG = "XmppChatActivity";

    public static String myJid;
    Toolbar toolbar;
    private SessionManager sessionManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_1);

        initializeToolbar();
        if(savedInstanceState==null){
            switchToFragment(new ConversationsFragment(), ConversationsFragment.TAG);
        }

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Chats"));
        tabLayout.addTab(tabLayout.newTab().setText("Social"));
        tabLayout.addTab(tabLayout.newTab().setText("Contacts"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d(TAG, "Current Fragment "+tab.getPosition());
                int id = tab.getPosition();
                if(id==0){
                    switchToFragment(new ConversationsFragment(), ConversationsFragment.TAG);
                }else if(id==1){
                    switchToFragment(new StudentsHallFragment(), StudentsHallFragment.TAG);
                }else if(tab.getPosition()==2){
                    switchToFragment(new ContactsFragment(), ContactsFragment.TAG);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    public void initializeToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void switchToFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (fragmentManager.findFragmentByTag(tag) != null) {
            // We are already showing this fragment
            return;
        }

        fragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left)
                .replace(R.id.container, fragment, tag)
                .commit();
    }

    public void getPresenceClick(View v){
        /*Intent intent = new Intent(RoosterConnectionService.VCARD_EVENT);
        intent.putExtra("jid", myJid);
        sendBroadcast(intent);*/
    }
    public void setPresenceClick(View v){
        Log.d(TAG, "Setting My Presence");
        //Intent intent = new Intent(RoosterConnectionService.SET_MY_PRESENCE);
        //sendBroadcast(intent);
    }

    @Override
    public void onDestroy(){
        Intent intent = new Intent(RoosterConnectionService.APP_IN_BACKGROUND);
        sendBroadcast(intent);
        super.onDestroy();
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.d(TAG, TAG +" is paused");
        sessionManager.saveActivityIsRunning(TAG, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, TAG +" is resumed");
        sessionManager = new SessionManager(this);
        sessionManager.saveActivityIsRunning(TAG, true);
        RegistrationInfo registrationInfo = sessionManager.loadRegistrationInfo();
        myJid = registrationInfo.getJid();
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
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(RoosterConnectionService.APP_IN_BACKGROUND);
        sendBroadcast(intent);
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id==R.id.disconnect) {
            Intent intent = new Intent();
            intent.setAction(RoosterConnectionService.DISCONNECT);
            sendBroadcast(intent);
            return true;
        }else if(id==R.id.addfriend){
            createContactDialog("Add Friend", "ADD");
            return true;
        }else if(id==R.id.removefriend){
            createContactDialog("Remove Friend", "REMOVE");
            return true;
        }else if(id==R.id.blocklist){
            Intent intent = new Intent(RoosterConnectionService.BLOCK_LIST);
            sendBroadcast(intent);
            return true;
        }else if(id==R.id.blockfriend){
            createContactDialog("Block Friend", "BLOCK");
            return true;
        }else if(id==R.id.unblockfriend){
            createContactDialog("Unblock Friend", "UNBLOCK");
            return true;
        }else if(id==R.id.profile){
            Intent profileIntent = new Intent(this, UserProfile.class);
            profileIntent.putExtra("my_jid", myJid);
            startActivity(profileIntent);
            return true;
        }
        return true;
    }

    private void createContactDialog(String title, final String type){
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View mView = layoutInflater.inflate(R.layout.user_input_dialog, null);
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(this);
        alertDialogBuilderUserInput.setView(mView);

        TextView titleT = (TextView) mView.findViewById(R.id.dialogTitle);
        titleT.setText(title);

        final EditText contactEdit = (EditText) mView.findViewById(R.id.contactDialog);

        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton(type, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {
                        // ToDo get user input here
                        String JID = contactEdit.getText().toString().trim();
                        Log.d(TAG, "Contact JID : "+ JID);
                        Intent intent = new Intent();
                        if(type.equals("ADD")){
                            intent.setAction(RoosterConnectionService.ADD_FRIEND);
                        }else if(type.equals("REMOVE")){
                            intent.setAction(RoosterConnectionService.REMOVE_FRIEND);
                        }else if(type.equals("BLOCK")){
                            intent.setAction(RoosterConnectionService.BLOCK_FRIEND);
                        }else if(type.equals("UNBLOCK")){
                            intent.setAction(RoosterConnectionService.UNBLOCK_FRIEND);
                        }
                        intent.putExtra("jid", JID);
                        sendBroadcast(intent);
                    }
                })

                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        });

        AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
        alertDialogAndroid.show();
    }

}
