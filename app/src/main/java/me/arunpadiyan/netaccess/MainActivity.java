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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.facebook.ads.*;
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
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
import java.util.Random;

import me.arunpadiyan.netaccess.Objects.CircleView;
import me.arunpadiyan.netaccess.Objects.EventBusLoading;
import me.arunpadiyan.netaccess.Objects.EventBusSuccess;


public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener {
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int MODE_SUCCESS = 2;
    private static final int MODE_FAILED = 1;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_INVITE = 0;
    static CookieManager cm = new CookieManager();

    ProgressDialog pDialog;
    TextView test;
    EditText rollno, ldap;
    CircleView Circle;
    CircleView CircleBack;
    Toolbar toolbar;
    RecyclerView UsageRecyclerView;
    SwipeRefreshLayout mSwipeRefreshLayout;

    Button approve;
    Button logout;
    CheckBox Notifi;
    CheckBox cbService;
    CheckBox cbNetAccess;

    Context context;
    boolean requstGoing = true;
    boolean mNetaccess;
    boolean mFirewall;
    private AdView adView;


    MyApplication mApp;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAnalytics mFirebaseAnalytics;
    FirebaseRemoteConfig mFirebaseRemoteConfig;


    int CurrentNetworkMode = 0;

    private InterstitialAd mInterstitialAd1;
    private InterstitialAd mInterstitialAd2;


    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mApp = (MyApplication) getApplicationContext();
        super.onCreate(savedInstanceState);
        if (!Utils.getprefBool("first_time_login22", this)) {
            Utils.saveprefBool(MyApplication.SERVICE_ENABLED, false, this);
            Utils.saveprefBool(MyApplication.NETACCESS_LOGIN, true, this);
            Utils.saveprefBool(MyApplication.ANALYTICS_ENABLED, true, this);
            //  Utils.saveprefBool(MyApplication.FORCE_LOGIN,true,this);
            Utils.saveprefBool("first_time_login22", true, this);
            // showCustomDialog();

        }
        if (!Utils.getprefBool("first_time_login12", this)) {
            if (Build.VERSION.SDK_INT < 21) {
                showCustomDialog();
            }
            //  Utils.saveprefBool(MyApplication.FORCE_LOGIN,true,this);
            Utils.saveprefBool("first_time_login12", true, this);
            // showCustomDialog();

        }
        context = this;
        setContentView(R.layout.activity_main);
        initComponents();

    }

    private void initComponents() {
        initFirebase();
        initViws();
        initButtons();
        initAnimation();
        initAd();
        initAppinvite();
        showUpdateDialog();

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        CookieHandler.setDefault(cm);

        // checking remote config loading
        Log.d(TAG, "test : " + mFirebaseRemoteConfig.getString("test"));

    }

    private void initAppinvite() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(AppInvite.API)
                .enableAutoManage(this, this)
                .build();


        boolean autoLaunchDeepLink = true;
        AppInvite.AppInviteApi.getInvitation(mGoogleApiClient, this, autoLaunchDeepLink)
                .setResultCallback(
                        new ResultCallback<AppInviteInvitationResult>() {
                            @Override
                            public void onResult(AppInviteInvitationResult result) {
                                Log.d(TAG, "getInvitation:onResult:" + result.getStatus());
                            }
                        });
        Log.d("MainActivity", Utils.getCertificateSHA1Fingerprint(this));

    }

    private void initButtons() {

        approve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotificationChecker();
                if (Utils.getprefBool(MyApplication.NETACCESS_LOGIN, context)) {
                    if (requstGoing) new Login().execute();
                } else if (Utils.getprefBool(MyApplication.VALID_PASS, context)) {
                    if (Utils.getprefBool(MyApplication.SERVICE_ENABLED, context)) {
                        //      ((MyApplication) getApplicationContext()).stopAuthService();
                        if (Utils.isNetworkAvailable(MainActivity.this)) {
                            ((MyApplication) getApplicationContext()).startAuthService();
                        } else {
                            mApp.showToast("No internet connection");

                        }

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

                if (Utils.getprefBool(MyApplication.NETACCESS_LOGIN, context)) {
                    if (requstGoing) new Login().execute();
                } else if (Utils.getprefBool(MyApplication.VALID_PASS, context)) {
                    if (Utils.getprefBool(MyApplication.SERVICE_ENABLED, context)) {
                        //    ((MyApplication) getApplicationContext()).stopAuthService();
                        ((MyApplication) getApplicationContext()).startAuthService();
                    }
                }
            }
        });


    }

    private void initCheckBox() {
        cbService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //is chkIos checked?
                if (((CheckBox) v).isChecked()) {
                    //   ((MyApplication) getApplicationContext()).stopAuthService();
                    ((MyApplication) getApplicationContext()).startAuthService();
                    Utils.saveprefBool(MyApplication.SERVICE_ENABLED, true, mApp);
                } else {
                    Utils.saveprefBool(MyApplication.SERVICE_ENABLED, false, mApp);
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
                    createNotification(MainActivity.this);
                    Utils.saveprefBool(MyApplication.NOTIFICATION_LOGIN_ENABLED, false, mApp);
                } else {

                    String ns = Context.NOTIFICATION_SERVICE;
                    NotificationManager nMgr = (NotificationManager) MyApplication.getContext().getSystemService(ns);
                    nMgr.cancel(1);
                    Utils.saveprefBool(MyApplication.NOTIFICATION_LOGIN_ENABLED, true, mApp);
                    mApp.stopAuthService();
                    mApp.startAuthService();
                }
                NotificationChecker();
            }
        });

        cbNetAccess.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //is chkIos checked?
                if (((CheckBox) v).isChecked()) {
                    Utils.saveprefBool(MyApplication.NETACCESS_LOGIN, true, mApp);
                } else {
                    Utils.saveprefBool(MyApplication.NETACCESS_LOGIN, false, mApp);
                }
                //  NotificationChecker();
            }
        });

    }

    private void initAd() {

        RelativeLayout adContainer = (RelativeLayout) findViewById(R.id.ad_view);
        adView = new AdView(this
                , mFirebaseRemoteConfig.getString(getString(R.string.banner_1))
                , AdSize.BANNER_HEIGHT_50);
        adContainer.addView(adView);
        adView.setAdListener(new AbstractAdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                super.onError(ad, adError);
            }

            @Override
            public void onAdClicked(Ad ad) {
                Bundle params = new Bundle();
                mFirebaseAnalytics.logEvent("ad_clicked_banner", params);
                super.onAdClicked(ad);
            }
        });

        if (show((int) mFirebaseRemoteConfig.getLong(getString(R.string.banner_1) + "_p"))) {
            adView.loadAd();
        }

        mInterstitialAd1 = new InterstitialAd(this
                ,mFirebaseRemoteConfig.getString(getString(R.string.interstetial_1)));
        mInterstitialAd1.loadAd();
        mInterstitialAd1.setAdListener(new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {

            }

            @Override
            public void onInterstitialDismissed(Ad ad) {

            }

            @Override
            public void onError(Ad ad, AdError adError) {

            }

            @Override
            public void onAdLoaded(Ad ad) {

            }

            @Override
            public void onAdClicked(Ad ad) {
                Bundle params = new Bundle();
                mFirebaseAnalytics.logEvent("ad_clicked_interstitial_1", params);
            }

            @Override
            public void onLoggingImpression(Ad ad) {

            }
        });

        mInterstitialAd2 = new InterstitialAd(this
                , mFirebaseRemoteConfig.getString(getString(R.string.interstetial_1)));
        mInterstitialAd2.loadAd();
        mInterstitialAd2.setAdListener(new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {

            }

            @Override
            public void onInterstitialDismissed(Ad ad) {

            }

            @Override
            public void onError(Ad ad, AdError adError) {

            }

            @Override
            public void onAdLoaded(Ad ad) {

            }

            @Override
            public void onAdClicked(Ad ad) {
                Bundle params = new Bundle();
                mFirebaseAnalytics.logEvent("ad_clicked_interstitial_2", params);
            }

            @Override
            public void onLoggingImpression(Ad ad) {

            }
        });

    }

    private void initAnimation() {
        setNetworkMode(MODE_FAILED);
    }

    private void initFirebase() {

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
        fetchWelcome();

        mNetaccess = mFirebaseRemoteConfig.getBoolean(getString(R.string.netaccess_enabled));
        mFirewall = mFirebaseRemoteConfig.getBoolean(getString(R.string.firewall_enabled));
        // fetchRemoteConfig();

    }

    private void initViws() {
        Circle = (CircleView) findViewById(R.id.border);
        CircleBack = (CircleView) findViewById(R.id.border2);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        UsageRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        test = (TextView) findViewById(R.id.test);
        rollno = (EditText) findViewById(R.id.edit_text_rollno);
        ldap = (EditText) findViewById(R.id.edit_text__pass);
        approve = (Button) findViewById(R.id.button_login);
        logout = (Button) findViewById(R.id.button_logout);
        Notifi = (CheckBox) findViewById(R.id.trackData);
        cbService = (CheckBox) findViewById(R.id.service);
        cbNetAccess = (CheckBox) findViewById(R.id.netacces);


        Notifi.setChecked(!Utils.getprefBool(MyApplication.NOTIFICATION_LOGIN_ENABLED, mApp));
        cbService.setChecked(Utils.getprefBool(MyApplication.SERVICE_ENABLED, mApp));
        cbNetAccess.setChecked(Utils.getprefBool(MyApplication.NETACCESS_LOGIN, mApp));

        initCheckBox();

        rollno.setText(Utils.getprefString(mApp.USER_NAME, this));
        ldap.setText(Utils.getprefString(mApp.LDAP_PASSWORD, this));


        UsageRecyclerView.setHasFixedSize(true);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        UsageRecyclerView.setLayoutManager(layoutManager);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("");
        }
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimaryDark);

    }


    private void setNetworkMode(int mode) {
        if (CurrentNetworkMode != mode) {
            if (mode == MODE_FAILED) {
                CircleBack.setColor(R.color.logo_green);
                Circle.setColor(R.color.logo_red);
                CircleBack.setSweepAngle(360);
                Circle.animateArc(0, 360, 2000);
            } else if (mode == MODE_SUCCESS) {
                CircleBack.setColor(R.color.logo_red);
                Circle.setColor(R.color.logo_green);
                CircleBack.setSweepAngle(360);
                Circle.animateArc(0, 360, 2000);
            }
        }
        CurrentNetworkMode = mode;

    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventBusLoading event) {
        if (event != null) {
            mSwipeRefreshLayout.setRefreshing(event.isLoading);


        }
    }


    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventBusSuccess event) {
        if (event != null) {
            if (show((int) mFirebaseRemoteConfig.getLong(getString(R.string.interstetial_1) + "_p"))) {
                if (mInterstitialAd1.isAdLoaded())
                    mInterstitialAd1.show();
            }
            // Chartboost.showInterstitial(CBLocation.LOCATION_DEFAULT);

            if (event.isSuccess) {
                setNetworkMode(MODE_SUCCESS);
                // ((ImageView) findViewById(R.id.logo)).setImageDrawable(ContextCompat.getDrawable(this,R.drawable.logo_green));
            } else {
                setNetworkMode(MODE_FAILED);
                //  ((ImageView) findViewById(R.id.logo)).setImageDrawable(ContextCompat.getDrawable(this,R.drawable.logo_red));
            }
        }
    }

    /*private void showGreenLogoBorder(){
        ObjectAnimator anim = ObjectAnimator.ofFloat(findViewById(R.id.green_border), "sweepAngle", 0, 360);
        anim.setDuration(1000);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator)
            {
                // calling invalidate(); will trigger onDraw() to execute
                invalidate();
            }
        });
        anim.start();
    }*/

    @Override
    public void onBackPressed() {
        // super.onBackPressed();

        if (mInterstitialAd2.isAdLoaded()
                && show((int) mFirebaseRemoteConfig.getLong(getString(R.string.interstetial_2) + "_p"))) {
            mInterstitialAd2.show();
        } else {
            finish();
        }

    }

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


    public static Notification createNotification(Context cont) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager notificationManager = (NotificationManager) cont.getSystemService(ns);

        Notification notification = new Notification(R.drawable.ic_knight_firewall_only, null, System.currentTimeMillis());
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


    private void AuthLogOut() {
        final String url = Utils.getprefString(MyApplication.LOG_OUT, this);
        final String function = "AuthLogOut";
        final String[] magic = {""};

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // ((MyApplication) getApplicationContext()).stopAuthService();

                        Log.d(TAG, function + " :response :" + response);
                        mApp.showToast("Logout successful");
                        onMessageEvent(new EventBusSuccess(false));

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
                Log.d("MainActivity", " AuthLogOut error : " + Utils.getprefString(MyApplication.LOG_OUT, context) + "  " + error.toString());
            }
        });
        Volley.newRequestQueue(this).add(stringRequest);
    }

    public void NotificationChecker() {
        if (Utils.isNetworkAvailable(mApp)) {
            if (!Utils.getprefBool("notifcation_login", mApp) && Utils.getprefBool(mApp.VALID_PASS, context)) {
                MainActivity.createNotification(context);

                if (Utils.getprefBool(MyApplication.SERVICE_ENABLED, context)) {
                    // ((MyApplication) context.getApplicationContext()).stopAuthService();
                    mApp.startAuthService();
                }
                Log.d("connected", "notif");
            } else {
                String ns = Context.NOTIFICATION_SERVICE;
                NotificationManager nMgr = (NotificationManager) MyApplication.getContext().getSystemService(ns);
                nMgr.cancel(1);
                if (isNotificationVisible())
                    mApp.stopAuthService();
                Log.d("connected", "no_notif");
            }
        } else {
            String ns = Context.NOTIFICATION_SERVICE;
            NotificationManager nMgr = (NotificationManager) MyApplication.getContext().getSystemService(ns);
            nMgr.cancel(1);
        }


    }


    private boolean isNotificationVisible() {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent test = PendingIntent.getActivity(context, 1, notificationIntent, PendingIntent.FLAG_NO_CREATE);
        return test != null;
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
            Intent openNewActivity = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(openNewActivity);


            return true;
        } else if (id == R.id.action_about) {
            Intent openNewActivity = new Intent(getApplicationContext(), AboutActivity.class);
            startActivity(openNewActivity);

        } else if (id == R.id.app_share) {
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
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
            } else {
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        // showMessage(getString(R.string.google_play_services_error));
    }

    public void saveString(String key, String value) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
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
            if (Utils.getprefBool(MyApplication.NETACCESS_LOGIN, context)) {
                new LoginNotif().execute();
            } else if (Utils.getprefBool(MyApplication.VALID_PASS, context)) {
                if (Utils.getprefBool(MyApplication.SERVICE_ENABLED, context)) {
                    // ((MyApplication) context.getApplicationContext()).stopAuthService();
                    ((MyApplication) context.getApplicationContext()).startAuthService();
                }
            }
        }

    }

    private static class LoginNotif extends AsyncTask<String, String, String> {
        String responseBody;
        private String resp;
        FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

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
                params.put("duration", firebaseRemoteConfig.getString("duration"));
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

                if (Utils.getprefBool(MyApplication.SERVICE_ENABLED, MyApplication.getContext())) {
                    // ((MyApplication) MyApplication.getContext()).stopAuthService();
                    ((MyApplication) MyApplication.getContext()).startAuthService();

                }
                EventBus.getDefault().post(new EventBusSuccess(true));
                FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(MyApplication.getContext());

                if (Utils.getprefBool(MyApplication.ANALYTICS_ENABLED, MyApplication.getContext())) {
                    Bundle params = new Bundle();
                    params.putString("result", "success");
                    mFirebaseAnalytics.logEvent("Notification_Login", params);

                }
                toast = loginform.text();

            }
            ((MyApplication) MyApplication.getContext()).showToast(toast);


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
                params.put("duration", mFirebaseRemoteConfig.getString("duration"));
                params.put("approveBtn", "");

                try {
                    HttpUtility.sendPostRequest(requestURL1, params);
                    responseBody = HttpUtility.readMultipleLinesRespone();

                } catch (IOException ex) {
                    ex.printStackTrace();
                    Utils.saveprefBool("Network_error", true, context);

                }
                try {
                    HttpUtility.sendPostRequest(requestURL2, params);
                    responseBody = HttpUtility.readMultipleLinesRespone();

                } catch (IOException ex) {
                    ex.printStackTrace();
                    Utils.saveprefBool("Network_error", true, context);

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

                    if (Utils.getprefBool(MyApplication.ANALYTICS_ENABLED, context)) {
                        Bundle params = new Bundle();
                        params.putString("result", "wrong password");
                        mFirebaseAnalytics.logEvent("MainActivity_Login", params);
                    }

                } else {
                   /* test.setText(loginform.text());
                    saveString(mApp.USER_NAME, rollno.getText().toString()); // Storing string
                    saveString(mApp.LDAP_PASSWORD, ldap.getText().toString());
                    Utils.saveprefBool(mApp.VALID_PASS, true,context);
                    if (!Utils.getprefBool("notifcation_login",context)) createNotification(MainActivity.this);
                   // context.stopvice(new Intent(context, AuthService.class));
                */

                    if (300 < loginform.text().length()) {
                        test.setTextColor(Color.RED);
                        test.setText("wrong password ");
                    } else {
                        onMessageEvent(new EventBusSuccess(true));
                        if (!Utils.getprefBool("notifcation_login", context))
                            createNotification(MainActivity.this);

                        test.setText(loginform.text());
                        saveString(mApp.USER_NAME, rollno.getText().toString()); // Storing string
                        saveString(mApp.LDAP_PASSWORD, ldap.getText().toString());
                        Utils.saveprefBool(mApp.VALID_PASS, true, context);

                        if (Utils.getprefBool(MyApplication.ANALYTICS_ENABLED, context)) {
                            Bundle params = new Bundle();
                            params.putString("result", "success");
                            mFirebaseAnalytics.logEvent("MainActivity_Login", params);
                        }

                        if (Utils.getprefBool(MyApplication.SERVICE_ENABLED, context)) {
                            ((MyApplication) getApplicationContext()).stopAuthService();
                            ((MyApplication) getApplicationContext()).startAuthService();
                        }
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
            if (Utils.getprefBool("Network_error", context)) {
               /* t.send(new HitBuilders.EventBuilder()
                        .setCategory("Login")
                        .setAction("fail")
                        .build());*/
                mApp.showToast("You are not connected to insti network");
                Utils.saveprefBool("Network_error", false, context);
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
        TextView tvCancel, tvSend;

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

    protected void showUpdateDialog() {


        int versionCode = BuildConfig.VERSION_CODE;
        if (mFirebaseRemoteConfig.getLong(getString(R.string.force_update_version)) > versionCode) {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View dialog = inflater.inflate(R.layout.update_dialoge, null);
            TextView tvSend;

            //final EditText editText = (EditText)dialog.findViewById(R.id.editText1);
            // cancel = (Button)dialog.findViewById(R.id.cancel);

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
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("market://details?id=" + getApplicationContext().getPackageName()));
                    startActivity(intent);

                }
            });

        }


    }

    private void fetchWelcome() {

        long cacheExpiration = 3600; // 1 hour in seconds.
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }
        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                           /* Toast.makeText(MainActivity.this, "Fetch Succeeded",
                                    Toast.LENGTH_SHORT).show();
*//**/
                            // After config data is successfully fetched, it must be activated before newly fetched
                            // values are returned.
                            mFirebaseRemoteConfig.activateFetched();
                        } else {
                           /* Toast.makeText(MainActivity.this, "Fetch Failed",
                                    Toast.LENGTH_SHORT).show();*/
                        }
                        Log.d(TAG, "test : " + mFirebaseRemoteConfig.getString("test"));

                    }
                });
        // [END fetch_config_with_callback]
    }

    boolean show(int value) {
        Random ran = new Random();
        int x = ran.nextInt(100) + 1;
        if (x < value) return true;
        return false;
    }

}
