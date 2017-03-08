package com.panicstyle.Gongdong;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.android.vending.billing.*;

public class AboutActivity extends AppCompatActivity {
    IInAppBillingService mService;

    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setTitle("공동육아앱에 대해서...");

        Context context = AboutActivity.this;
        String version;
        try {
        	PackageInfo i = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        	version = i.versionName;
        } catch (NameNotFoundException e) { 
        	version = "Unknown";
        }
        
        String versionInfo = "Version : " + version;

        TextView t = (TextView)findViewById(R.id.textVersion); 
        t.setText(versionInfo);

        Intent serviceIntent =
                new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            unbindService(mServiceConn);
        }
    }
}
