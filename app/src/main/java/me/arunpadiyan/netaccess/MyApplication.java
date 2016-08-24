package me.arunpadiyan.netaccess;

import android.app.Application;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.google.firebase.FirebaseApp;

public class MyApplication extends Application {
    public static final String FORCE_LOGIN = "force_login";
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


    public static class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWifi != null) {



               /* if(!thredActive){
                    ThreadB threadB = new ThreadB();
                    threadB.start();
                    thredActive = true;
                }*/
                if (mWifi.isConnected()) {
                   // if (!Utils.getprefBool("notifcation_login",context)) MainActivity.createNotification(context);
                    Log.d(TAG, "connected");
                    if(Utils.getprefBool(VALID_PASS,context) && Utils.getprefBool(MyApplication.SERVICE_ENABLED,context)){
                        ((MyApplication) context.getApplicationContext()).startAuthService();
                    }
                } else {
                  //  if (Utils.getprefBool("notifcation_login",context)) MainActivity.createNotification(context);
                    String ns = Context.NOTIFICATION_SERVICE;
                    NotificationManager nMgr = (NotificationManager) MyApplication.getContext().getSystemService(ns);
                    nMgr.cancel(1);
                    Log.d(TAG, "disconnected");
                    ((MyApplication) context.getApplicationContext()).stopAuthService();
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

    public static class StartService extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("Service Stops", "Ohhhhhhh");
            ((MyApplication) context.getApplicationContext()).startAuthService();
            }

    }

}