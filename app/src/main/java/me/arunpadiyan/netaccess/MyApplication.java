package me.arunpadiyan.netaccess;

import android.app.Application;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.FirebaseApp;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.select.Evaluator;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyApplication extends Application {
    private static Application instance;
    public static final String TAG = "MyApplication";
    public static final String USER_NAME = "user_name";
    public static final String LDAP_PASSWORD = "ldap_password";
    public static final String KEEP_ALIVE = "keep_alive";
    public static final String LOG_OUT = "log_out";
    public static final String VALID_PASS = "valid_pass";
    public static final String SERVICE_ENABLED = "service_enabled";
    public static final String NETACCESS_LOGIN  = "NETACCESS_LOGIN_ENABLED";

    public static final String NOTIFICATION_LOGIN_ENABLED = "notifcation_login";
    public static final String ANALYTICS_ENABLED = "analytics_enabled";


    private Typeface mFontCabinRegular = null;

    public static boolean thredActive = false;
    RequestQueue queue;


    Context mContext ;

    @Override
    public void onCreate() {
        super.onCreate();

        if (!FirebaseApp.getApps(this).isEmpty()) {
            //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }
        instance = this;
        new NukeSSLCerts().nuke();
      //  HttpURLConnection.setFollowRedirects(true);

        mContext = this;


    }

    public void startAuthService(){
        startService(new Intent(this, AuthService.class));

    }

    public void stopAuthService(){
        AuthService.allowDestroy = true;
        stopService(new Intent(this, AuthService.class));

    }

    public static android.content.Context getContext() {
        return instance.getApplicationContext();
    }
   /* synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker("UA-61389535-1")
                    : (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(R.xml.global_tracker)
                    : analytics.newTracker(R.xml.ecommerce_tracker);
            mTrackers.put(trackerId, t);

        }
        return mTrackers.get(trackerId);
    }*/
    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }

    //HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();



    public static class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (info != null) {



                if(!thredActive){
                    ThreadB threadB = new ThreadB();
                    threadB.start();
                    thredActive = true;
                }
                if (info.isConnected()) {
                    if (!Utils.getprefBool("notifcation_login",context)) MainActivity.createNotification(context);
                    Log.d(TAG, "connected temp");

                    Utils.saveprefBool("network",true,getContext());
                } else {
                  //  if (Utils.getprefBool("notifcation_login",context)) MainActivity.createNotification(context);
                    String ns = Context.NOTIFICATION_SERVICE;
                    NotificationManager nMgr = (NotificationManager) MyApplication.getContext().getSystemService(ns);
                    nMgr.cancel(1);

                    Log.d(TAG, "disconnected temp");
                    Utils.saveprefBool("network",false,getContext());
                }
            }
        }
    }

    static class ThreadB extends Thread {

        @Override
        public void run() {
            try {
                Log.d(TAG, "Thread sleep");
                Thread.sleep(2000);
                Log.d(TAG, "Thread resumed");
                Context context = getContext();
                thredActive = false;
                if(Utils.getprefBool("network",context)){
                    Log.d(TAG, "connected");
                    if(Utils.getprefBool(VALID_PASS,context) && Utils.getprefBool(MyApplication.SERVICE_ENABLED,context)){
                        ((MyApplication) context.getApplicationContext()).startAuthService();
                    }
                }else {
                    Log.d(TAG, "disconnected");
                    ((MyApplication) context.getApplicationContext()).stopAuthService();
                }

            } catch (Exception e) {
                System.out.println("Exception caught");
            }
        }
    }

    public Typeface getFjordOneRegular() {
        if (mFontCabinRegular == null) {
            mFontCabinRegular = Typeface.createFromAsset(this.getAssets(), "fonts/Montserrat-Regular.otf");
        }
        return mFontCabinRegular;
    }


}