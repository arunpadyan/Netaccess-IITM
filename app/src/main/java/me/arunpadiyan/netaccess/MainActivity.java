package me.arunpadiyan.netaccess;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MainActivity extends ActionBarActivity implements
        GoogleApiClient.OnConnectionFailedListener {
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_INVITE = 0;
    static CookieManager cm = new CookieManager();
    // SwipeRefreshLayout mSwipeRefreshLayout;
  //  static Tracker t;
    ProgressDialog pDialog;
    TextView test;
    EditText rollno, ldap;
    Context context;
    String regid;
    boolean requstGoing = true;
    RecyclerView UsageRecyclerView;
    SwipeRefreshLayout mSwipeRefreshLayout;
    MyApplication mApp;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAnalytics mFirebaseAnalytics;
    private InterstitialAd mInterstitialAd;

    /**
     * @return Application's version code from the {@code PackageManager}.
     */

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }


    public static void hideSoftKeyboard(ActionBarActivity activity, View view) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    public static Notification createNotification(Context cont) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager notificationManager = (NotificationManager) cont.getSystemService(ns);

        Notification notification = new Notification(R.drawable.ic_launcher_notif, null, System.currentTimeMillis());
        RemoteViews notificationView = new RemoteViews(cont.getPackageName(), R.layout.notification_layout);


        //the intent that is started when the notification is clicked (works)
        Intent notificationIntent = new Intent(cont, SplashActivity.class);
        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(cont, 0, notificationIntent, 0);

        notification.contentView = notificationView;
        notification.contentIntent = pendingNotificationIntent;
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_NO_CLEAR;
        // notification.flags = Notification.VISIBILITY_PUBLIC;
        //  notification.Builder.setVisibility(Notification.VISIBILITY_PUBLIC);

        //this is the intent that is supposed to be called when the button is clicked
        Intent switchIntent = new Intent(MyApplication.getContext(), switchButtonListener.class);
        switchIntent.putExtra("sdgfahg", notificationView);
        PendingIntent pendingSwitchIntent = PendingIntent.getBroadcast(MyApplication.getContext(), 0, switchIntent, 0);

        notificationView.setOnClickPendingIntent(R.id.imageButton, pendingSwitchIntent);

        notificationManager.notify(1, notification);
        return notification;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mApp = (MyApplication) getApplicationContext();
        super.onCreate(savedInstanceState);

        if(!Utils.getprefBool("first_time_login1",this)){
            Utils.saveprefBool(MyApplication.SERVICE_ENABLED,true,this);
            Utils.saveprefBool(MyApplication.NETACCESS_LOGIN,true,this);
            Utils.saveprefBool(MyApplication.ANALYTICS_ENABLED,true,this);
          //  Utils.saveprefBool(MyApplication.FORCE_LOGIN,true,this);
            Utils.saveprefBool("first_time_login1",true,this);
           // showCustomDialog();

        }
        if(!Utils.getprefBool("first_time_login12",this)){
            if(Build.VERSION.SDK_INT <21){
                showCustomDialog();
            }
            //  Utils.saveprefBool(MyApplication.FORCE_LOGIN,true,this);
            Utils.saveprefBool("first_time_login12",true,this);
            // showCustomDialog();

        }
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");
        myRef.setValue("Hello, World!");
        context = this;
        setContentView(R.layout.activity_main);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        MobileAds.initialize(getApplicationContext(), "ca-app-pub-5514295486090543~8789911718");

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest request = new AdRequest.Builder()
                .addTestDevice("98E9534D9298ECC93E2F46F2D815F745")
                .build();
        mAdView.loadAd(request);




        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        CookieHandler.setDefault(cm);

        UsageRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        UsageRecyclerView.setHasFixedSize(true);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        UsageRecyclerView.setLayoutManager(layoutManager);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("NETACCESS");
        }
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimaryDark);

        test = (TextView) findViewById(R.id.test);
        rollno = (EditText) findViewById(R.id.edit_text_rollno);
        ldap = (EditText) findViewById(R.id.edit_text__pass);
        Button approve = (Button) findViewById(R.id.button_login);
        Button logout = (Button) findViewById(R.id.button_logout);

        rollno.setText(Utils.getprefString(mApp.USER_NAME, this));
        ldap.setText(Utils.getprefString(mApp.LDAP_PASSWORD, this));

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        approve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotificationChecker();
                if(Utils.getprefBool(MyApplication.NETACCESS_LOGIN,context)){
                    if (requstGoing) new Login().execute();
                }else if(Utils.getprefBool(MyApplication.VALID_PASS,context)){
                    if (Utils.getprefBool(MyApplication.SERVICE_ENABLED,context)) {
                  //      ((MyApplication) getApplicationContext()).stopAuthService();
                        ((MyApplication) getApplicationContext()).startAuthService();
                    }
                }

            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AuthLogOut();
            }
        });
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                NotificationChecker();

                if(Utils.getprefBool(MyApplication.NETACCESS_LOGIN,context)){
                    if (requstGoing) new Login().execute();
                }else if(Utils.getprefBool(MyApplication.VALID_PASS,context)){
                    if (Utils.getprefBool(MyApplication.SERVICE_ENABLED,context)) {
                    //    ((MyApplication) getApplicationContext()).stopAuthService();
                        ((MyApplication) getApplicationContext()).startAuthService();
                    }
                }
            }
        });

        CheckBox Notifi = (CheckBox) findViewById(R.id.trackData);
        CheckBox cbService = (CheckBox) findViewById(R.id.service);
        CheckBox cbNetAccess = (CheckBox) findViewById(R.id.netacces);

        Notifi.setChecked(!Utils.getprefBool(MyApplication.NOTIFICATION_LOGIN_ENABLED,context));
        cbService.setChecked(Utils.getprefBool(MyApplication.SERVICE_ENABLED,context));
        cbNetAccess.setChecked(Utils.getprefBool(MyApplication.NETACCESS_LOGIN,context));

        cbService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //is chkIos checked?
                if (((CheckBox) v).isChecked()) {
                 //   ((MyApplication) getApplicationContext()).stopAuthService();
                    ((MyApplication) getApplicationContext()).startAuthService();
                    Utils.saveprefBool(MyApplication.SERVICE_ENABLED, true,context);
                } else {
                    Utils.saveprefBool(MyApplication.SERVICE_ENABLED, false,context);
                    ((MyApplication) getApplicationContext()).stopAuthService();
                }
                NotificationChecker();
            }
        });

        Notifi.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //is chkIos checked?
                if (((CheckBox) v).isChecked()) {
                    Utils.saveprefBool(MyApplication.NOTIFICATION_LOGIN_ENABLED, false,context);
                } else {
                    Utils.saveprefBool(MyApplication.NOTIFICATION_LOGIN_ENABLED, true,context);
                }
                NotificationChecker();
            }
        });

        cbNetAccess.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //is chkIos checked?
                if (((CheckBox) v).isChecked()) {
                    Utils.saveprefBool(MyApplication.NETACCESS_LOGIN, true,context);
                } else {
                    Utils.saveprefBool(MyApplication.NETACCESS_LOGIN, false,context);
                }
              //  NotificationChecker();
            }
        });


        //app invite
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(AppInvite.API)
                .enableAutoManage(this, this)
                .build();

        // Check for App Invite invitations and launch deep-link activity if possible.
        // Requires that an Activity is registered in AndroidManifest.xml to handle
        // deep-link URLs.
        boolean autoLaunchDeepLink = true;
        AppInvite.AppInviteApi.getInvitation(mGoogleApiClient, this, autoLaunchDeepLink)
                .setResultCallback(
                        new ResultCallback<AppInviteInvitationResult>() {
                            @Override
                            public void onResult(AppInviteInvitationResult result) {
                                Log.d(TAG, "getInvitation:onResult:" + result.getStatus());
                                // Because autoLaunchDeepLink = true we don't have to do anything
                                // here, but we could set that to false and manually choose
                                // an Activity to launch to handle the deep link here.
                            }
                        });
        Log.d("MainActivity",Utils.getCertificateSHA1Fingerprint(this));


        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-5514295486090543/7748609313");
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                //requestNewInterstitial();
                finish();
            }
        });
        requestNewInterstitial();


    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(getString(R.string.my_device_id))
                .build();

        mInterstitialAd.loadAd(adRequest);
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            finish();
        }
    }

    private void AuthLogOut( ) {
        final String url =Utils.getprefString(MyApplication.LOG_OUT,this);
        final String function = "AuthLogOut" ;
        final String[] magic = {""};

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ((MyApplication) getApplicationContext()).stopAuthService();

                        Log.d(TAG ,function+" :response :"+response);
                        Toast.makeText(context,"Logout successful",Toast.LENGTH_SHORT).show();
                        Bundle params = new Bundle();
                        params.putString("result", "success");
                        params.putString("context", "MainActivity");
                        mFirebaseAnalytics.logEvent("Logout", params);
                      //  getMagic(url);
                      /*  Log.d("MainActivity getMagic", getRegexString("\"magic\" value=\"(?<cap>.+?)\"",response));
                        magic[0] = getRegexString("\"magic\" value=\"(?<cap>.+?)\"",response);
                        NewFirewallAuthLogin(url,magic[0]);*/
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Bundle params = new Bundle();

                params.putString("result", "failed");
                params.putString("context", "MainActivity");
                mFirebaseAnalytics.logEvent("Logout", params);
                NetworkResponse response = error.networkResponse;
                Log.d("MainActivity", " AuthLogOut error : "+Utils.getprefString(MyApplication.LOG_OUT,context)+"  " +error.toString());
            }
        });
        Volley.newRequestQueue(this).add(stringRequest);
    }

    public void NotificationChecker() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {
            if (!Utils.getprefBool("notifcation_login",context) && Utils.getprefBool(mApp.VALID_PASS,context)) {
                createNotification(MyApplication.getContext());
                Log.d("1", "here");
            } else {
                String ns = Context.NOTIFICATION_SERVICE;

                NotificationManager nMgr = (NotificationManager) MyApplication.getContext().getSystemService(ns);
                nMgr.cancel(1);
            }
            Log.d("connected", "fucker");
            // Do your workateNotification();
        } else {
            Log.d("disconnected", "fucker");
            String ns = Context.NOTIFICATION_SERVICE;

            NotificationManager nMgr = (NotificationManager) MyApplication.getContext().getSystemService(ns);
            nMgr.cancel(1);
        }


    }


    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        //GoogleAnalytics.getInstance(MainActivity.this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
       // GoogleAnalytics.getInstance(MainActivity.this).reportActivityStop(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent openNewActivity= new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(openNewActivity);


            return true;
        }else
        if(id == R.id.action_about){
            Intent openNewActivity= new Intent(getApplicationContext(), AboutActivity.class);
            startActivity(openNewActivity);

        }else
        if (id == R.id.app_share) {
            Intent intent = new AppInviteInvitation.IntentBuilder("invite others to use this App")
                    .setMessage("Since the NetAccess cups frequently these days ,this app is definitely a time saver for you")
                    .setCallToActionText("Install")
                    .build();
            startActivityForResult(intent, REQUEST_INVITE);

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Check how many invitations were sent and log a message
                // The ids array contains the unique invitation ids for each invitation sent
                // (one for each contact select by the user). You can use these for analytics
                // as the ID will be consistent on the sending and receiving devices.
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                //Log.d(TAG, getString(R.string.sent_invitations_fmt, ids.length));
            } else {
                // Sending failed or it was canceled, show failure message to the user
                // showMessage(getString(R.string.send_failed));
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        // showMessage(getString(R.string.google_play_services_error));
    }

    public void saveString(String key, String value) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 1); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public void openWebPage(String url) {
        Uri webpage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public static class switchButtonListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("TAG", "test");
            if(Utils.getprefBool(MyApplication.NETACCESS_LOGIN,context)){
                new LoginNotif().execute();
            }else if(Utils.getprefBool(MyApplication.VALID_PASS,context)){
                if (Utils.getprefBool(MyApplication.SERVICE_ENABLED,context)) {
                   // ((MyApplication) context.getApplicationContext()).stopAuthService();
                    ((MyApplication) context.getApplicationContext()).startAuthService();
                }
            }
        }

    }

    private static class LoginNotif extends AsyncTask<String, String, String> {
        String responseBody;
        private String resp;

        @Override
        protected String doInBackground(String... paramso) {
           /* CookieManager cookieManager = new CookieManager();
            CookieHandler.setDefault(cookieManager);*/
            try {
                URL url1 = new URL("https://netaccess.iitm.ac.in/account/login");
                URL url2 = new URL("https://netaccess.iitm.ac.in/account/approve");

                String requestURL1, requestURL2;
                Map<String, String> params = new HashMap<String, String>();
                requestURL1 = "https://netaccess.iitm.ac.in/account/login";
                requestURL2 = "https://netaccess.iitm.ac.in/account/approve";
                params.put("userPassword", Utils.getprefString(MyApplication.LDAP_PASSWORD, MyApplication.getContext()));
                params.put("userLogin", Utils.getprefString(MyApplication.USER_NAME, MyApplication.getContext()));
                params.put("duration", "1");
                params.put("approveBtn", "");

                try {
                    HttpUtility.sendPostRequest(requestURL1, params);
                    responseBody = HttpUtility.readMultipleLinesRespone();

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                try {
                    HttpUtility.sendPostRequest(requestURL2, params);
                    responseBody = HttpUtility.readMultipleLinesRespone();

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                HttpUtility.disconnect();
            } catch (Exception e) {
                Log.d("", e.getLocalizedMessage());
            }

            return responseBody;
        }

        @Override
        protected void onPostExecute(String result) {
            Element loginform = null;
            String toast;

            if (responseBody != null) {
                Document doc = Jsoup.parse(responseBody);
                loginform = doc.select("div.alert-success").first();
                if (loginform == null) {
                    loginform = doc.select("div.alert-info").first();
                }
            }
            if (loginform == null) {
                toast = "you are not connected to insti network";

            } else if (300 < loginform.text().length()) {
                toast = "wrong password ";
            } else {
                Vibrator v = (Vibrator) MyApplication.getContext().getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(60);

                if (Utils.getprefBool(MyApplication.SERVICE_ENABLED,MyApplication.getContext())) {
                   // ((MyApplication) MyApplication.getContext()).stopAuthService();
                    ((MyApplication) MyApplication.getContext()).startAuthService();

                }
                FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(MyApplication.getContext());

                if(Utils.getprefBool(MyApplication.ANALYTICS_ENABLED,MyApplication.getContext())){
                    Bundle params = new Bundle();
                    params.putString("result", "success");
                    mFirebaseAnalytics.logEvent("Notification_Login", params);

                }
                toast = loginform.text();

            }
            Toast.makeText(MyApplication.getContext(), toast,
                    Toast.LENGTH_SHORT).show();

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            CookieHandler.setDefault(cm);
            CookieStore cookieStore = cm.getCookieStore();

        }

        @Override
        protected void onProgressUpdate(String... text) {

        }
    }

    private class Login extends AsyncTask<String, String, String> {
        String responseBody;

        @Override
        protected String doInBackground(String... paramso) {


            try {
                URL url1 = new URL("https://netaccess.iitm.ac.in/account/login");
                URL url2 = new URL("https://netaccess.iitm.ac.in/account/approve");

                String requestURL1, requestURL2;
                Map<String, String> params = new HashMap<String, String>();
                requestURL1 = "https://netaccess.iitm.ac.in/account/login";
                requestURL2 = "https://netaccess.iitm.ac.in/account/approve";
                params.put("userPassword", ldap.getText().toString());
                params.put("userLogin", rollno.getText().toString());
                params.put("duration", "1");
                params.put("approveBtn", "");

                try {
                    HttpUtility.sendPostRequest(requestURL1, params);
                    responseBody = HttpUtility.readMultipleLinesRespone();

                } catch (IOException ex) {
                    ex.printStackTrace();
                    Utils.saveprefBool("Network_error", true,context);

                }
                try {
                    HttpUtility.sendPostRequest(requestURL2, params);
                    responseBody = HttpUtility.readMultipleLinesRespone();

                } catch (IOException ex) {
                    ex.printStackTrace();
                    Utils.saveprefBool("Network_error", true,context);

                }
                HttpUtility.disconnect();
            } catch (Exception e) {
                Log.d(TAG, e.getLocalizedMessage());
            }


            return responseBody;
        }


        @Override
        protected void onPostExecute(String result) {
            final SwipeRefreshLayout mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
            requstGoing = true;

            mSwipeRefreshLayout.setRefreshing(false);
            if (pDialog.isShowing())
                pDialog.dismiss();

            if (responseBody != null) {
                CookieStore cookieStore = cm.getCookieStore();
                cookieStore.removeAll();
                //Log.d("here",responseBody.toString());
                Document doc = Jsoup.parse(responseBody);
                //Log.d("res",responseBody);
                Element loginform = doc.select("div.alert-success").first();
                test.setTextColor(Color.WHITE);

                if (loginform == null) {
                    loginform = doc.select("div.alert-info").first();
                    test.setTextColor(Color.RED);
                }
                if (loginform == null) {
                    test.setTextColor(Color.RED);
                    test.setText("wrong password ");

                    if(Utils.getprefBool(MyApplication.ANALYTICS_ENABLED,context)){
                        Bundle params = new Bundle();
                        params.putString("result", "wrong password");
                        mFirebaseAnalytics.logEvent("MainActivity_Login", params);
                    }

                } else {
                    test.setText(loginform.text());
                    saveString(mApp.USER_NAME, rollno.getText().toString()); // Storing string
                    saveString(mApp.LDAP_PASSWORD, ldap.getText().toString());
                    Utils.saveprefBool(mApp.VALID_PASS, true,context);
                    if (!Utils.getprefBool("notifcation_login",context)) createNotification(MainActivity.this);
                   // context.stopvice(new Intent(context, AuthService.class));

                }
                if (300 < loginform.text().length()) {
                    test.setTextColor(Color.RED);
                    test.setText("wrong password ");
                } else {
                    test.setText(loginform.text());
                    saveString(mApp.USER_NAME, rollno.getText().toString()); // Storing string
                    saveString(mApp.LDAP_PASSWORD, ldap.getText().toString());
                    Utils.saveprefBool(mApp.VALID_PASS, true,context);

                    if(Utils.getprefBool(MyApplication.ANALYTICS_ENABLED,context)){
                        Bundle params = new Bundle();
                        params.putString("result", "success");
                        mFirebaseAnalytics.logEvent("MainActivity_Login", params);
                    }

                    /*t.send(new HitBuilders.EventBuilder()
                            .setCategory("Login")
                            .setAction("Success")
                            .build());*/

                    if (Utils.getprefBool(MyApplication.SERVICE_ENABLED,context)) {
                        ((MyApplication) getApplicationContext()).stopAuthService();
                        ((MyApplication) getApplicationContext()).startAuthService();
                    }
                }
                // Elements par = loginform.select("[p]");

                // String name = doc.attr(".alert-success");
                String usage;
                String link;
                String ip;
                boolean active;
                String date;
                ArrayList<Usage> arrayList = new ArrayList<Usage>();
                try {

                    Element table = doc.select("table.table").get(0);

                    if (table != null) {
                        Elements rows = table.select("tr");
                        for (int i = 1; i < rows.size(); i++) {
                            Elements data = rows.get(i).select("td");
                            /*Log.d("---------------------------------------------------------",data.get(1).text());
                            Log.d("ip",data.get(1).text());
                            Log.d("date", data.get(2).text());
                            Log.d("usage",data.get(3).text());*/
                            usage = data.get(3).text();
                            ip = data.get(1).text();
                            date = data.get(2).text();
                            active = (rows.get(i).select("span.label-success").first() != null);
                            if (active)
                                link = "https://netaccess.iitm.ac.in" + rows.get(i).select("a[href]").first().attr("href");
                            else link = "";
                            Usage test = new Usage(ip, usage, date, link, active);
                            arrayList.add(test);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                UsageRecyclerAdapter adapter = new UsageRecyclerAdapter(MainActivity.this, arrayList, mSwipeRefreshLayout);
                UsageRecyclerView.setAdapter(adapter);


            }
            if (Utils.getprefBool("Network_error",context)) {
               /* t.send(new HitBuilders.EventBuilder()
                        .setCategory("Login")
                        .setAction("fail")
                        .build());*/
                Toast.makeText(getBaseContext(), "You are not connected to insti network",
                        Toast.LENGTH_SHORT).show();
                Utils.saveprefBool("Network_error",false,context );
            }
            //new Approveve().execute();
        }

        @Override
        protected void onPreExecute() {
            requstGoing = false;
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            // pDialog.show();
            //
          /*  t.send(new HitBuilders.EventBuilder()
                    .setCategory("Login")
                    .setAction("Hit")
                    .build());*/
            final SwipeRefreshLayout mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
            if (!mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(true);
                mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimaryDark);
            }

        }

        @Override
        protected void onProgressUpdate(String... text) {

        }
    }
    protected void showCustomDialog() {


        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialog = inflater.inflate(R.layout.product_chat_dialog, null);
        TextView  tvCancel,tvSend;

        //final EditText editText = (EditText)dialog.findViewById(R.id.editText1);
        // cancel = (Button)dialog.findViewById(R.id.cancel);

        tvCancel = (TextView) dialog.findViewById(R.id.tv_cancel);
        tvSend = (TextView) dialog.findViewById(R.id.tv_send);



        alertDialogBuilder.setView(dialog);
        alertDialogBuilder.setCancelable(true);
        final AlertDialog Dialog = alertDialogBuilder.create();
        Dialog.getWindow().getDecorView().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Dialog.setCancelable(false);
        Dialog.show();

        tvSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  Utils.saveprefBool(MyApplication.ANALYTICS_ENABLED,true,context);
                Dialog.cancel();

            }
        });
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  Utils.saveprefBool(MyApplication.ANALYTICS_ENABLED,false,context);

                Dialog.cancel();
            }
        });

    }


}
