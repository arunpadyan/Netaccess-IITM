package me.arunpadiyan.netaccess;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
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

public class AuthService extends Service {
    public static  final  String TAG = "AuthService";
    Timer t;
    Context mContext;
    public static int KEEP_AIVE_REFRESH = 1000 * 190;
    RequestQueue queue;

    public AuthService() {
        t = new Timer();

    }

    @Override
    public IBinder onBind(Intent intent) {

        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
       // mApp = (MyApplication) getApplicationContext();
        mContext = this;
        queue = Volley.newRequestQueue(this);

        NewFirewallAuth();

        Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
               KeepAlive(Utils.getprefString(MyApplication.KEEP_ALIVE,mContext));
            }

        }, 0, KEEP_AIVE_REFRESH);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        t.cancel();
    }
    public void NewFirewallAuth() {
        final String function  = "NewFirewallAuth";
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
                    Log.d("MainActivity", function +" :"+"Data :" + parsed);
                    Log.d("MainActivity", function +" :"+"ResponseCode :" + Integer.toString(mStatusCode));
                    AuthLogOut();
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

                        Log.d("MainActivity getMagic", getRegexString("\"magic\" value=\"(?<cap>.+?)\"",response));
                        magic[0] = getRegexString("\"magic\" value=\"(?<cap>.+?)\"",response);

                        NewFirewallAuthLogin(url,magic[0]);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse response = error.networkResponse;
                Log.d("MainActivity", " getMagic error :" +error.toString());
            }
        });
        queue.add(stringRequest);
    }

    //for logout
    private void AuthLogOut( ) {
        final String url =Utils.getprefString(MyApplication.LOG_OUT,mContext);
        final String function = "AuthLogOut" ;
        final String[] magic = {""};
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.d(TAG ,function+" :response :"+response);
                        getMagic(url);
                      /*  Log.d("MainActivity getMagic", getRegexString("\"magic\" value=\"(?<cap>.+?)\"",response));
                        magic[0] = getRegexString("\"magic\" value=\"(?<cap>.+?)\"",response);
                        NewFirewallAuthLogin(url,magic[0]);*/
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse response = error.networkResponse;
                Log.d("MainActivity", " getMagic error :" +error.toString());
            }
        });
        queue.add(stringRequest);
    }

    //for keep alive auth
    public void KeepAlive(final String url) {
        final String[] magic = {""};
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG," KeepAlive:" +url);
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

        String url = "https://nfw.iitm.ac.in:1003/";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //   Log.d("MainActivity", response);

                        String logout = getRegexString("location.href=\"(?<cap>.+logout.+?)\"",response);
                        String keepalive = getRegexString("location.href=\"(?<cap>.+keepalive.+?)\"",response);

                        Toast.makeText(mContext,"Firewall Authentication successful \n rollno :"
                                +Utils.getprefString(MyApplication.USER_NAME,mContext),Toast.LENGTH_LONG).show();
                        Utils.saveprefString(MyApplication.LOG_OUT,logout,mContext);
                        Utils.saveprefString(MyApplication.KEEP_ALIVE,keepalive,mContext);

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
                params.put("username",  Utils.getprefString(MyApplication.USER_NAME,mContext));
                params.put("password", Utils.getprefString(MyApplication.LDAP_PASSWORD,mContext));
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

}
