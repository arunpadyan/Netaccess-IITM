package me.arunpadiyan.netaccess;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.util.Timer;
import java.util.TimerTask;

public class AuthService extends Service {

    MyApplication mApp;
    Timer t;
    public static int KEEP_AIVE_REFRESH = 1000 * 190;

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
        mApp = (MyApplication) getApplicationContext();

        mApp.NewFirewallAuth();

        Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mApp.KeepAlive(Utils.getprefString(mApp.KEEP_ALIVE,mApp));
            }

        }, 0, KEEP_AIVE_REFRESH);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        t.cancel();
    }
}
