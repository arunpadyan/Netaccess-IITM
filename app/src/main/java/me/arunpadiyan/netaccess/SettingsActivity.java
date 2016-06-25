package me.arunpadiyan.netaccess;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

import static me.arunpadiyan.netaccess.MainActivity.createNotification;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatActivity {
    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Settings");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Settings");

        CheckBox Notifi = (CheckBox) findViewById(R.id.trackData);
        CheckBox ForceLogin = (CheckBox) findViewById(R.id.forcelogin);

        CheckBox save = (CheckBox) findViewById(R.id.save_password);

        Notifi.setChecked(Utils.getprefBool(MyApplication.ANALYTICS_ENABLED,this));
        Notifi.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //is chkIos checked?
                if (((CheckBox) v).isChecked()) {
                   Utils.saveprefBool(MyApplication.ANALYTICS_ENABLED,true,getApplicationContext());
                }
                else {
                    Utils.saveprefBool(MyApplication.ANALYTICS_ENABLED,false,getApplicationContext());
                }
            }
        });

        ForceLogin.setChecked(Utils.getprefBool(MyApplication.FORCE_LOGIN,this));
        ForceLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //is chkIos checked?
                if (((CheckBox) v).isChecked()) {
                    Utils.saveprefBool(MyApplication.FORCE_LOGIN,true,getApplicationContext());
                }
                else {
                    Utils.saveprefBool(MyApplication.FORCE_LOGIN,false,getApplicationContext());
                }
            }
        });
        save.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //is chkIos checked?
                if (((CheckBox) v).isChecked()) {
                    saveBool("save_password",false);
                }
                else {
                    saveBool("save_password",true);
                }


            }
        });


    }
    public static void saveBool(String key, Boolean value){
        SharedPreferences pref = MyApplication.getContext().getSharedPreferences("MyPref", 1); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(key, value);
        editor.commit();

    }
    public Boolean getBool(String key){
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 1); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        return pref.getBoolean(key, false);

    }
    public void NotificationChecker(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {
            if (!getBool("notifcation_login")&&getBool("have_name")){
                createNotification(MyApplication.getContext());
                Log.d("1","here");
            }
            else {
                String ns = Context.NOTIFICATION_SERVICE;

                NotificationManager nMgr = (NotificationManager) MyApplication.getContext().getSystemService(ns);
                nMgr.cancel(1);
            }
            Log.d("connected", "fucker");
            // Do your workateNotification();
        }
        else {
            Log.d("disconnected","fucker");
            String ns = Context.NOTIFICATION_SERVICE;

            NotificationManager nMgr = (NotificationManager) MyApplication.getContext().getSystemService(ns);
            nMgr.cancel(1);
        }



    }
}
