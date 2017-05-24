package com.ahmed.chatapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.ahmed.chatapplication.connection.HttpVolleyRequest;
import com.ahmed.chatapplication.gcmservices.QuickstartPreferences;
import com.ahmed.chatapplication.gcmservices.RegistrationIntentService;
import com.ahmed.chatapplication.info.PermissionsInfo;
import com.ahmed.chatapplication.info.XmppServersInfo;
import com.ahmed.chatapplication.response.VolleyJsonResponse;
import com.bilkoon.rooster.connection.RoosterConnection;
import com.bilkoon.rooster.model.RegistrationInfo;
import com.bilkoon.rooster.service.RoosterConnectionService;
import com.bilkoon.rooster.util.SessionManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * A login screen that offers login via jid/password.
 */
public class LoginActivity extends AppCompatActivity implements VolleyJsonResponse, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = "LoginActivity";

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;


    // UI references.
    private AutoCompleteTextView mJidView;
    private EditText mPasswordView;
    private EditText mConfirmPasswordView;
    private EditText mName;
    private View mProgressView;
    private View mLoginFormView;
    private Button mJidSignInButton;
    private BroadcastReceiver mBroadcastReceiver;
    private CheckBox regCheckbox;
    private boolean doRegister = false;
    private SessionManager sessionManager;


    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final int RQS_GooglePlayServices = 1;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private boolean isReceiverRegistered;
    private String token;
    private boolean isFirstRun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);
        sessionManager.saveContactListStarted(false);
        //serverJids = getIntent().getStringArrayListExtra("server_jids");
        //Log.d(TAG, "Server jid array size : "+serverJids.size());
        // Set up the login form.
        mJidView = (AutoCompleteTextView) findViewById(R.id.email);
        regCheckbox = (CheckBox) findViewById(R.id.regCheckbox);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mName = (EditText) findViewById(R.id.name);
        mName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        mConfirmPasswordView = (EditText) findViewById(R.id.confirmPassword);
        mConfirmPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mJidSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mJidSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
                //sessionManager.saveLastSeenDate("ahmad@xmpp.jp", "1492780454154");
                //GenerateLastSeen.LastSeen("ahmad@xmpp.jp");
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        //--------------Testing purposes----------------//
        RegistrationInfo registrationInfo = sessionManager.loadRegistrationInfo();
        String mJid = registrationInfo.getJid();
        Log.d(TAG, "My JabberId is : "+mJid);
        String mPassword = registrationInfo.getPassword();
        if(mJid!=null && mPassword!=null) {
            mJidView.setText(mJid);
            mPasswordView.setText(mPassword);
        }
        //----------------------------------------------//
        checkFirstRun();
        if(!isFirstRun) {
            mRegistrationBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    boolean sentToken = sessionManager.loadTokenSent();
                    if (sentToken) {
                        Log.d(TAG, getString(R.string.gcm_send_message));
                        token = sessionManager.loadRegistrationToken();
                    } else {
                        Log.d(TAG, getString(R.string.token_error_message));
                    }
                }
            };
            // Registering BroadcastReceiver
            registerReceiver();
        }
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            if(!isFirstRun) {
                Intent intent = new Intent(this, RegistrationIntentService.class);
                startService(intent);
            }
        }
        //----------------------------------------------------------------//
    }

    public void regClick(View v){
        if(regCheckbox.isChecked()){
            mJidSignInButton.setText("REGISTER");
            doRegister = true;
            mConfirmPasswordView.setVisibility(View.VISIBLE);
            mName.setVisibility(View.VISIBLE);
            mName.requestFocus();
            return;
        }
        mJidSignInButton.setText("SIGN IN");
        doRegister = false;
        mConfirmPasswordView.setVisibility(View.GONE);
        mName.setVisibility(View.GONE);

    }

    public void checkFirstRun(){
        token = sessionManager.loadRegistrationToken();
        if(token!=null) sessionManager.saveIsFirstApplicationRun(false);
        isFirstRun = sessionManager.loadIsFirstApplicationRun();
        Log.d(TAG, "Is first run : "+isFirstRun);
    }
    public static int MY_PERMISSIONS_REQUESTS = 1;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "Result : " + requestCode+"-----grantResults length is "+grantResults.length);
        if (requestCode == MY_PERMISSIONS_REQUESTS) {
            if(grantResults.length==6) {
                if(sessionManager.loadIsFirstApplicationRun()) {
                    sessionManager.saveIsFirstApplicationRun(false);
                    finish();
                }
            }
            else sessionManager.saveIsFirstApplicationRun(true);
            if (grantResults.length == 6
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED && grantResults[3] == PackageManager.PERMISSION_GRANTED
                    && grantResults[4] == PackageManager.PERMISSION_GRANTED && grantResults[5] == PackageManager.PERMISSION_GRANTED
                    ) {
                // Success Stuff here
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        checkFirstRun();
        if(!isFirstRun) {
            this.unregisterReceiver(mBroadcastReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
            isReceiverRegistered = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Checking first run");
        checkFirstRun();
        if(!isFirstRun) {
            mBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    Log.d(TAG, "Received broadcast : " + action);
                    if (action.equals(RoosterConnectionService.UI_AUTHENTICATED)) {
                        Log.d(TAG, "Got a broadcast to show the main app window");
                        //Show the main app window
                        showProgress(false);
                        startActivity(new Intent(LoginActivity.this, XmppChatActivity.class));
                        finish();
                    } else if (action.equals(RoosterConnectionService.UI_REGISTERED)) {
                        String jid = mJidView.getText().toString();
                        String name = mName.getText().toString();
                        registerUser(sessionManager.loadServerId(), jid, name);
                    }

                }
            };
            IntentFilter filter = new IntentFilter(RoosterConnectionService.UI_AUTHENTICATED);
            filter.addAction(RoosterConnectionService.UI_REGISTERED);
            this.registerReceiver(mBroadcastReceiver, filter);

            registerReceiver();
        }
    }

    private void populateAutoComplete() {
        if (!mayRequestPermissions()) {
            return;
        }
        //getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.d(TAG, "NO need to ask for permissions programmatically");
            sessionManager.saveIsFirstApplicationRun(false);
            return true;
        }
        PermissionsInfo.requestPermissions(this);
        return false;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        //mProgressView.setVisibility(View.VISIBLE);
        // Reset errors.
        mJidView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mJidView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mJidView.setError(getString(R.string.error_field_required));
            focusView = mJidView;
            cancel = true;
        } else if (!isEmailValid(username)) {
            mJidView.setError(getString(R.string.error_invalid_jid));
            focusView = mJidView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            //mProgressView.setVisibility(View.GONE);
        } else {
            //Log.d(TAG, "Is connected : " + RoosterConnectionService.sConnectionState);
            //Save the credentials and login
            if (!doRegister) saveCredentialsAndLogin();
            else beginRegistration(username, password);
        }
    }
    private void beginRegistration(String username, String password){
        String confirmPassword = mConfirmPasswordView.getText().toString();
        if (!TextUtils.isEmpty(confirmPassword) && !isPasswordValid(confirmPassword)) {
            mConfirmPasswordView.setError(getString(R.string.error_invalid_password));
            mConfirmPasswordView.requestFocus();
        }else if(!confirmPassword.equals(password)) {
            mConfirmPasswordView.setError(getString(R.string.error_password_match));
            mConfirmPasswordView.requestFocus();
        }else{
            Log.d(TAG, "Registering new account ["+username+"]");
            sessionManager.saveRegistrationInfo(mJidView.getText().toString(), mPasswordView.getText().toString(), true);
            //Start the service
            Intent i1 = new Intent(this, RoosterConnectionService.class);
            i1.putExtra("register", true);
            startService(i1);
        }
    }

    private void saveCredentialsAndLogin(){
        Log.d(TAG,"saveCredentialsAndLogin() called.");
        sessionManager.saveRegistrationInfo(mJidView.getText().toString(), mPasswordView.getText().toString(), true);
        Log.d(TAG, "Is connected : "+RoosterConnectionService.sConnectionState);
        RoosterConnection.ConnectionState ConnectionState = RoosterConnectionService.sConnectionState;
        if(ConnectionState == RoosterConnection.ConnectionState.CONNECTED){
            startActivity(new Intent(LoginActivity.this, XmppChatActivity.class));
            return;
        }
        if(ConnectionState == null || ConnectionState == RoosterConnection.ConnectionState.DISCONNECTED) {
            //Start the service
            Intent i1 = new Intent(this, RoosterConnectionService.class);
            i1.putExtra("register", false);
            startService(i1);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    public void registerUser(String uid, String jid, String name){
        HttpVolleyRequest register = new HttpVolleyRequest();
        Map<String, String> params = new HashMap<String, String>();
        params.put("tag", "register_user");
        params.put("uid", uid);
        params.put("jid", jid);
        params.put("name", name);
        register.makeRequest("req_register", XmppServersInfo.URL_REGISTER + "test.php", params);
        register.delegate = this;
    }

    private void registerReceiver(){
        if(!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
            isReceiverRegistered = true;
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                createDialog("Google Play Services", "Google Play Services are not available, download?");
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void processFinish(JSONObject output) {
        Log.d(TAG, output.toString());
        try {
            boolean error = output.getBoolean("error");
            if(!error){
                String tag = output.getString("tag");
                if(tag.equals("register_user")){
                    int id = Integer.parseInt(output.getString("id"));
                    String jid = output.getString("jid");
                    String name = output.getString("name");
                    Log.d(TAG, "Data :\n"+"ID---> " + id + "\nJID---> " + jid+ "\nNAME---> " + name);
                }
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processError(String error) {

    }

    private void createDialog(String title, final String messageContent){
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View mView = layoutInflater.inflate(R.layout.services_dialog, null);
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(this);
        alertDialogBuilderUserInput.setView(mView);

        TextView titleT = (TextView) mView.findViewById(R.id.dialogTitle);
        TextView message = (TextView) mView.findViewById(R.id.messageContent);
        titleT.setText(title);
        message.setText(messageContent);

        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {
                        // ToDo get user input here
                        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
                        apiAvailability.makeGooglePlayServicesAvailable(LoginActivity.this);
                    }
                })

                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                                Intent startMain = new Intent(Intent.ACTION_MAIN);
                                startMain.addCategory(Intent.CATEGORY_HOME);
                                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(startMain);
                            }
                        });

        AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
        alertDialogAndroid.show();
    }
}

