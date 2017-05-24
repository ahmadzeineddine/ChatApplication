package com.bilkoon.rooster.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.ahmed.chatapplication.gcmservices.QuickstartPreferences;
import com.bilkoon.rooster.model.RegistrationInfo;

import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by Ahmed on 4/21/2017.
 */

public class SessionManager {
    private static String TAG = SessionManager.class.getSimpleName();
    // Shared Preferences
    SharedPreferences pref;

    SharedPreferences.Editor editor;
    Context _context;
    // Shared pref mode
    int PRIVATE_MODE = 0;
    // Shared preferences file name
    private static final String PREF_NAME = "RoosterApp";
    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void saveIsFirstApplicationRun(boolean isFirstAppRun){
        editor.putBoolean("is_first_run", isFirstAppRun).apply();
    }
    public boolean loadIsFirstApplicationRun(){
        return pref.getBoolean("is_first_run", true);
    }

    public void saveServerId(String serverId){
        editor.putString("server_id", serverId).apply();
    }
    public String loadServerId(){
        return pref.getString("server_id", null);
    }

    public void saveServerJids(Set<String> jids){
        editor.putStringSet("server_jids", jids).apply();
    }

    public void saveRegistrationInfo(String mJid, String password, boolean mLoggedin){
        editor.putString("xmpp_jid", mJid)
              .putString("xmpp_password", password)
              .putBoolean("xmpp_logged_in",mLoggedin)
              .apply();
    }

    public void saveLastSeenDate(String mJid, String timestamp){
        editor.putString("last_seen_"+mJid, timestamp).apply();
    }

    public Set<String> loadServerJids(){return pref.getStringSet("server_jids", null);}
    public RegistrationInfo loadRegistrationInfo(){
        return new RegistrationInfo(pref.getString("xmpp_jid", null), pref.getString("xmpp_password", null), pref.getBoolean("xmpp_logged_in", false));
    }
    public String loadLastSeenDate(String mJid){return pref.getString("last_seen_"+mJid, null);}

    public void saveCurrentVCardCount(int count){
        editor.putInt("vcard_count", count).apply();
    }
    public int loadCurrentVCardCount(){return pref.getInt("vcard_count", 0);}

    public void saveContactListStarted(boolean started){
        editor.putBoolean("list_started", started).apply();
    }
    public boolean loadContactListStarted(){return pref.getBoolean("list_started", false);}
    public void saveMyImagePath(String path){
        editor.putString("ppic", path).apply();
    }
    public String loadMyImagePath(){return pref.getString("ppic", null);}

    public void saveImei(String imei){
        editor.putString("imei", imei).apply();
    }
    public String loadImei(){
        return pref.getString("imei", null);
    }
    public void saveTokenSent(boolean tokenSent){
        editor.putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, true).apply();
    }
    public boolean loadTokenSent(){
        return pref.getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
    }
    public void saveRegistrationToken(String token){
        editor.putString(QuickstartPreferences.REGISTRATION_TOKEN, token).apply();
    }
    public String loadRegistrationToken(){
        return pref.getString(QuickstartPreferences.REGISTRATION_TOKEN, null);
    }
    public void saveServerVCardTimestamp(String jid, String timestamp){
        editor.putString(jid+"_vcardserver", timestamp).apply();
    }
    public String loadServerVCardTimestamp(String jid){
        return pref.getString(jid+"_vcardserver", null);
    }

    public void saveVCardTimestamp(String jid, String timestamp){
        String[] temp = timestamp.split(" ");
        String[] y_m_d = temp[0].split("-");
        String year = "20"+y_m_d[0];
        String am_pm = temp[2].toLowerCase();
        String ts = year+"-"+y_m_d[1]+"-"+y_m_d[2]+" "+ temp[1]+" "+ am_pm;
        //Log.d("Session", "formatted date : "+ ts);
        editor.putString(jid+"_vcard", ts).apply();
    }
    public String loadVCardTimestamp(String jid){
        return pref.getString(jid+"_vcard", null);
    }
    public void saveVCardInfo(String jid, VCard vCard){
        editor.putString("firstname_"+jid, vCard.getFirstName())
                .putString("status_"+jid, vCard.getField("status"))
                .putString("avatar_"+jid, vCard.getField("avatar"))
                .apply();
    }

    public VCard loadVCardInfo(String jid){
        VCard vCard = new VCard();
        vCard.setFirstName(pref.getString("firstname_"+jid, null));
        vCard.setField("status", pref.getString("status_"+jid, null));
        vCard.setField("avatar", pref.getString("avatar_"+jid, null));
        return vCard;
    }

    public void saveActivityIsRunning(String name, boolean isRunning){
        editor.putBoolean(name, isRunning).apply();
    }
    public boolean loadActivityIsRunning(String name){
        return pref.getBoolean(name, false);
    }
    public void saveIdOfJid(String jid, String uid ){
        editor.putString("xmppuser_"+jid, uid).apply();
    }
    public String loadIdOfJid(String jid){
        return pref.getString("xmppuser_"+jid, null);
    }

    public void saveNotifyCount(String jid, int count){
        editor.putInt("xmppNotify_"+jid, count).apply();
    }
    public int loadNotifyCount(String jid){
        return pref.getInt("xmppNotify_"+jid, 0);
    }

    public void saveNotifyId(String jid, int count, int notifyId){
        editor.putInt("xmppNotify_"+jid+"_"+count, notifyId).apply();
    }
    public int loadNotifyId(String jid, int count){
        return pref.getInt("xmppNotify_"+jid+"_"+count, 0);
    }
}
