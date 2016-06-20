package me.arunpadiyan.netaccess;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.select.Evaluator;

import java.io.UnsupportedEncodingException;
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

    public static boolean thredActive = false;



    Context mContext ;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        new NukeSSLCerts().nuke();
        mContext = this;
    }

    public static android.content.Context getContext() {
        return instance.getApplicationContext();
    }
    synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker("UA-61389535-1")
                    : (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(R.xml.global_tracker)
                    : analytics.newTracker(R.xml.ecommerce_tracker);
            mTrackers.put(trackerId, t);

        }
        return mTrackers.get(trackerId);
    }
    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }

    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    public void NewFirewallAuth() {
        final String function  = "NewFirewallAuth";
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://connectivitycheck.gstatic.com/generate_204";
        //HttpURLConnection.setFollowRedirects(true);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("MainActivity", response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse response = error.networkResponse;
                if(response != null){
                    int mStatusCode = response.statusCode;
                    String parsed;
                    try {
                        parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                    } catch (UnsupportedEncodingException e) {
                        parsed = new String(response.data);
                    }
                    Document doc = Jsoup.parse(parsed);
                    Elements links = doc.select("a[href]");
                    Log.d(TAG,function +" :"+ links.get(0).attr("href"));

                    Log.d(TAG,function+ " Data :" + parsed);
                    Log.d(TAG,function+ " ResponseCode :" + Integer.toString(mStatusCode));

                    if(mStatusCode != 204){
                        getMagic(links.get(0).attr("href"));
                    }
                }else {
                    Log.d(TAG,function+ " network error" );

                }
            }
        }) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                if (response != null) {
                    int mStatusCode = response.statusCode;
                    String parsed;
                    try {
                        parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                    } catch (UnsupportedEncodingException e) {
                        parsed = new String(response.data);
                    }
                    Log.d("MainActivity", "Data :" + parsed);
                    Log.d("MainActivity", "ResponseCode :" + Integer.toString(mStatusCode));
                }
                return super.parseNetworkResponse(response);
            }
        };
// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }


    private void getMagic(final String url) {
        RequestQueue queue = Volley.newRequestQueue(this);
        final String[] magic = {""};

        // HttpURLConnection.setFollowRedirects(true);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.d("MainActivity getMagic", getRegexString("\"magic\" value=\"(?<cap>.+?)\"",response));
                        magic[0] = getRegexString("\"magic\" value=\"(?<cap>.+?)\"",response);

                        NewFirewallAuthLogin(url,magic[0]);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse response = error.networkResponse;
                Log.d("MainActivity", " getMagic error :" +error.toString());
                // Log.d("Main Activity",response);
            }
        });
// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void KeepAlive(final String url) {
        RequestQueue queue = Volley.newRequestQueue(this);
        final String[] magic = {""};

        // HttpURLConnection.setFollowRedirects(true);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.d(TAG," KeepAlive:" +response);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse response = error.networkResponse;
                Log.d(TAG, " KeepAlive error :" +error.toString());
            }
        });
        queue.add(stringRequest);
    }

    private String getRegexString(String patern,String string){
        Pattern p = Pattern.compile(patern);
        Matcher m = p.matcher(string);
        if(m.find())
            return m.group(1);

        return "";
    }

    public void NewFirewallAuthLogin(final String AuthLink,final String magic) {

        // new NukeSSLCerts().nuke();

        // HttpsTrustManager.allowAllSSL();
        String  tag_string_req = "string_req";
        String url = "https://nfw.iitm.ac.in:1003/";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                     //   Log.d("MainActivity", response);

                        String logout = getRegexString("location.href=\"(?<cap>.+logout.+?)\"",response);
                        String keepalive = getRegexString("location.href=\"(?<cap>.+keepalive.+?)\"",response);
                        Utils.saveprefString(LOG_OUT,logout,mContext);
                        Utils.saveprefString(KEEP_ALIVE,keepalive,mContext);

                        Log.d(TAG,"logout link : "+logout);
                        Log.d(TAG,"keepalive link : "+keepalive);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse response = error.networkResponse;
                Log.d("MainActivity",error.toString());
                //  int mStatusCode = response.statusCode;
                /*String parsed;
                try {
                    parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                } catch (UnsupportedEncodingException e) {
                    parsed = new String(response.data);
                }*/
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", "ch13b010");
                params.put("password", "Kannan!123");
                params.put("4Tredir", "http://connectivitycheck.gstatic.com/generate_204");
                params.put("magic",magic);

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
                params.put("Referer", AuthLink);

                return params;
            }

        };
        queue.add(stringRequest);

    }


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
                    Log.d(TAG, "connected temp");
                    Utils.saveprefBool("network",true,getContext());
                } else {
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
                    context.startService(new Intent(context, AuthService.class));
                }else {
                    Log.d(TAG, "disconnected");
                    context.stopService(new Intent(context, AuthService.class));
                }

            } catch (Exception e) {
                System.out.println("Exception caught");
            }
        }
    }



}