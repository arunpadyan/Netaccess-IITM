package me.arunpadiyan.netaccess;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
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
import com.google.firebase.analytics.FirebaseAnalytics;

import org.greenrobot.eventbus.EventBus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.arunpadiyan.netaccess.Objects.EventBusLoading;

public class AuthService extends Service {

    public static final String TAG = "AuthService";
    public static int KEEP_AIVE_REFRESH = 1000 * 290;

    public static boolean allowDestroy = false;
    Context mContext;
    RequestQueue queue;
    int keepAliveCount = 0;
    private FirebaseAnalytics mFirebaseAnalytics;
    public static boolean isStarted = false;
    Timer t;
    long startTime = System.currentTimeMillis();
    EventBus eventBus;
    public AuthService() {
        t = new Timer();
        eventBus = EventBus.getDefault();
    }

    @Override
    public void onCreate() {

        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {

        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // mApp = (MyApplication) getApplicationContext();
        allowDestroy = false;
        mContext = this;
        queue = Volley.newRequestQueue(this);

        if (!Utils.getprefBool("notifcation_login",mContext)) startForeground(1,MainActivity.createNotification(mContext));

        /*Toast.makeText(mContext
                ,"You already have net access,if you want force " +
                        "login you can change in settings",Toast.LENGTH_LONG).show();*/
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        NewFirewallAuth();

        if (isStarted) {      //yes - do nothing
            t.cancel();
            t = new Timer();
        } else {             //no
            isStarted = true;
            t = new Timer();

        }
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                eventBus.post(new EventBusLoading(true));
                keepAliveCount += 1;
                long endTime = System.currentTimeMillis();
                Log.d("AuthService", "keepAliveCount : " + Integer.toString(keepAliveCount));
                Log.d("AuthService", "keepAliveTimeDiff : " + Long.toString(endTime - startTime));
                startTime = endTime;

                KeepAlive(Utils.getprefString(MyApplication.KEEP_ALIVE, mContext));
                if (Utils.getprefBool(MyApplication.ANALYTICS_ENABLED, MyApplication.getContext())) {
                    Bundle params = new Bundle();
                    params.putString("context", TAG);
                    mFirebaseAnalytics.logEvent("Login_try_" + TAG, params);
                }
            }

        }, 1000, KEEP_AIVE_REFRESH);


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("AuthService", "onDestroy auth count : " + Integer.toString(keepAliveCount));
        if (Utils.getprefBool(MyApplication.ANALYTICS_ENABLED, MyApplication.getContext())) {
            Bundle params = new Bundle();
            params.putString("context", TAG);
            mFirebaseAnalytics.logEvent(TAG + "_onDestroy", params);
        }
        t.cancel();
        if (!allowDestroy) {
            Intent intent = new Intent("com.android.launchService");
            sendBroadcast(intent);
        } else {
            Log.d("AuthService", "onDestroyed auth count : " + Integer.toString(keepAliveCount));
        }
    }

    public void NewFirewallAuth() {
        final String function = "NewFirewallAuth";
        String url = "http://connectivitycheck.gstatic.com/generate_204";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("AuthService", response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse response = error.networkResponse;
                if (response != null) {
                    try {
                        int mStatusCode = response.statusCode;
                        String parsed;
                        try {
                            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                        } catch (UnsupportedEncodingException e) {
                            parsed = new String(response.data);
                        }
                        Document doc = Jsoup.parse(parsed);
                        Elements links = doc.select("a[href]");
                        Log.d(TAG, function + " :" + links.get(0).attr("href"));

                        Log.d(TAG, function + " Data :" + parsed);
                        Log.d(TAG, function + " ResponseCode :" + Integer.toString(mStatusCode));

                        if (mStatusCode != 204) {
                            Log.d(TAG, " getMagic url :" + links.get(0).attr("href"));
                            getMagic(links.get(0).attr("href"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d(TAG, function + " network error");
                    eventBus.post(new EventBusLoading(false));
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
                    Log.d(TAG, function + " :" + "Data :" + parsed);
                    Log.d(TAG, function + " :" + "ResponseCode :" + Integer.toString(mStatusCode));
                    if(Utils.getprefBool(MyApplication.FORCE_LOGIN,mContext)){
                      //  AuthLogOut();
                    }else {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                eventBus.post(new EventBusLoading(false));
                                Toast.makeText(mContext
                                        ,"You already have net access",Toast.LENGTH_LONG).show();
                            }
                        });

                    }
                }
                return super.parseNetworkResponse(response);
            }
        };
        queue.add(stringRequest);
    }


    private void getMagic(final String url) {

        final String[] magic = {""};

        // HttpURLConnection.setFollowRedirects(true);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.d(TAG, "getMagic :" + getRegexString("\"magic\" value=\"(.+?)\"", response));
                        magic[0] = getRegexString("\"magic\" value=\"(.+?)\"", response);

                        NewFirewallAuthLogin(url, magic[0]);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse response = error.networkResponse;
                Log.d(TAG, " getMagic error :" + error.toString());
                if (Utils.getprefBool(MyApplication.ANALYTICS_ENABLED, MyApplication.getContext())) {
                    Bundle params = new Bundle();
                    params.putString("context", TAG);
                    mFirebaseAnalytics.logEvent("getMagic_error", params);
                }
            }
        });
        queue.add(stringRequest);
    }

    //for logout
    private void AuthLogOut() {
        eventBus.post(new EventBusLoading(true));
        final String url = Utils.getprefString(MyApplication.LOG_OUT, mContext);
        final String function = "AuthLogOut";
        final String[] magic = {""};
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.d(TAG, function + " :response :" + response);
                        getMagic(url);
                      /*  Log.d(TAG getMagic", getRegexString("\"magic\" value=\"(?<cap>.+?)\"",response));
                        magic[0] = getRegexString("\"magic\" value=\"(?<cap>.+?)\"",response);
                        NewFirewallAuthLogin(url,magic[0]);*/
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                eventBus.post(new EventBusLoading(false));
                NetworkResponse response = error.networkResponse;
                Log.d(TAG, " getMagic error :" + error.toString());
            }
        });
        queue.add(stringRequest);
    }

    //for keep alive auth
    public void KeepAlive(final String url) {
        eventBus.post(new EventBusLoading(true));
        final String[] magic = {""};
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Bundle params = new Bundle();
                        params.putString("result", "success");
                        params.putString("context", TAG);
                        mFirebaseAnalytics.logEvent("KeepAlive", params);
                        Log.d(TAG, " KeepAlive:" + url);
                        eventBus.post(new EventBusLoading(false));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                AuthLogOut();
                if (Utils.getprefBool(MyApplication.ANALYTICS_ENABLED, MyApplication.getContext())) {

                    Bundle params = new Bundle();
                    params.putString("result", "failed");
                    params.putString("context", TAG);
                    mFirebaseAnalytics.logEvent("KeepAlive", params);
                }
                NewFirewallAuth();
                NetworkResponse response = error.networkResponse;
                Log.d(TAG, " KeepAlive error :" + error.toString());
            }
        });
        queue.add(stringRequest);
    }

    private String getRegexString(String patern, String string) {
        Pattern p = Pattern.compile(patern);
        Matcher m = p.matcher(string);
        if (m.find())
            return m.group(1);

        return "";
    }

    public void NewFirewallAuthLogin(final String AuthLink, final String magic) {

        String url = "https://nfw.iitm.ac.in:1003/";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //   Log.d(TAG", response);
                        eventBus.post(new EventBusLoading(false));
                        String logout = getRegexString("location.href=\"(.+?logout.+?)\"", response);
                        String keepalive = getRegexString("location.href=\"(.+?keepalive.+?)\"", response);


                        Utils.saveprefString(MyApplication.LOG_OUT, logout, mContext);
                        Utils.saveprefString(MyApplication.KEEP_ALIVE, keepalive, mContext);


                        if (Utils.getprefBool(MyApplication.ANALYTICS_ENABLED, MyApplication.getContext())) {

                            Bundle params = new Bundle();
                            params.putString("result", "success");
                            params.putString("context", TAG);
                            mFirebaseAnalytics.logEvent("NewFirewallAuthLogin", params);
                        }
                        Log.d(TAG, "logout link : " + logout);
                        Log.d(TAG, "keepalive link : " + keepalive);
                        if (logout.trim().length() > 10) {
                            Toast.makeText(mContext, "Firewall Authentication successful \n using rollno : "
                                    + Utils.getprefString(MyApplication.USER_NAME, mContext), Toast.LENGTH_LONG).show();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Bundle params = new Bundle();


                if (Utils.getprefBool(MyApplication.ANALYTICS_ENABLED, MyApplication.getContext())) {

                    params.putString("result", "failed");
                    params.putString("context", TAG);
                    mFirebaseAnalytics.logEvent("NewFirewallAuthLogin", params);
                }
                eventBus.post(new EventBusLoading(false));
                NetworkResponse response = error.networkResponse;
                Log.d(TAG, error.toString());
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
                params.put("username", Utils.getprefString(MyApplication.USER_NAME, mContext));
                params.put("password", Utils.getprefString(MyApplication.LDAP_PASSWORD, mContext));
                params.put("4Tredir", "http://connectivitycheck.gstatic.com/generate_204");
                params.put("magic", magic);

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
                params.put("Referer", AuthLink);

                return params;
            }

        };
        queue.add(stringRequest);

    }

}
