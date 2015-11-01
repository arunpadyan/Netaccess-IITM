package me.arunpadiyan.netaccess;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_me);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("About");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        getSupportActionBar().setTitle("About");

    }
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
    public void onClick(View v){

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "arunpadyan@gmail.com", null));

        switch(v.getId()){
            case R.id.developer_icon:
                DeveloperContact newObject = DeveloperContact.getInstance(this);
                newObject.show(getFragmentManager(), "Developer Contact Dialog");
                break;

            case R.id.rate_on_play_store_button:
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id="+getApplicationContext().getPackageName()));
                startActivity(intent);
                break;

            case R.id.about_submit_feedback_button:

                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);

                String debug_info = "\n\n\n Device information \n -------------------------------";
                try{
                    debug_info += "\n Netaccess App version: " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                }catch (PackageManager.NameNotFoundException nne){
                    Log.e("About", "Name not found exception");
                }
                debug_info += "\n Android Version: " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT+ ") \n Model (and product): " + android.os.Build.MODEL + " ("+ android.os.Build.PRODUCT + ") \n Device: " + android.os.Build.DEVICE;

                emailIntent.putExtra(Intent.EXTRA_TEXT, debug_info);
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Netaccess App: Feedback / bug report");
                startActivity(Intent.createChooser(emailIntent, "Send feedback / bug report"));
                break;

            case R.id.gplus_container:
                String URL = "https://www.facebook.com/arunpadyan";
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URL)));
                break;

            case R.id.email_container:
                emailIntent.putExtra(Intent.EXTRA_TEXT, "\n\n\n - Bunk-o-Meter user");
                startActivity(Intent.createChooser(emailIntent, "Send email"));
                break;

        }
    }
}
