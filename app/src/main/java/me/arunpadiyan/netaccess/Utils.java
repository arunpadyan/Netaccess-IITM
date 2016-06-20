package me.arunpadiyan.netaccess;

/**
 * Created by Arun Padiyan on 21-Jun-16.
 */
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.EditText;

public class Utils {
    public static void saveprefString(String key, String value, Context context) {
        SharedPreferences pref = context.getSharedPreferences("MyPref", 1); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getprefString(String key, Context cont) {
        SharedPreferences pref = cont.getSharedPreferences("MyPref", 1); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        return pref.getString(key, "");
    }

    public static void saveprefBool(String key, Boolean value, Context context) {
        SharedPreferences pref = context.getSharedPreferences("MyPref", 1); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(key, value);
        editor.commit();

    }
    public static void saveprefInt(String key, int value, Context context) {
        SharedPreferences pref = context.getSharedPreferences("MyPref", 1); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(key, value);
        editor.commit();
    }
    public static int getprefInt(String key, Context cont) {
        SharedPreferences pref = cont.getSharedPreferences("MyPref", 1); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        return pref.getInt(key, 0);
    }

    public static Boolean getprefBool(String key, Context context) {
        SharedPreferences pref = context.getSharedPreferences("MyPref", 1); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        return pref.getBoolean(key, false);

    }

    public static void clearpref(Context context) {
        SharedPreferences pref = context.getSharedPreferences("MyPref", 1);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    public static boolean isEmpty(EditText etText) {
        if (etText.getText().toString().trim().length() > 0) {
            return false;
        } else {
            return true;
        }
    }
}