package com.example.hmsdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;

import com.huawei.hms.ads.identifier.AdvertisingIdClient;

import java.io.IOException;

public class AdsDemo extends BaseActivity implements OaidCallback{

    private String TAG = "Ads Demo";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ads_demo);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.title_activity_ads_demo);
        }
        getIdentifierThread.start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    private Thread getIdentifierThread = new Thread() {

        @Override
        public void run() {
            getOaid();
        }
    };

    private void getOaid() {
        try {
            AdvertisingIdClient.Info info = AdvertisingIdClient.getAdvertisingIdInfo(this);
            if (null != info) {
                this.onSuccuss(info.getId(), info.isLimitAdTrackingEnabled());
            } else {
                this.onFail("oaid is null");
            }
        } catch (IOException e) {
            Log.e(TAG, "getAdvertisingIdInfo IOException");
            this.onFail("getAdvertisingIdInfo IOException");
        }
    }

    @Override
    public void onSuccuss(String oaid, boolean isOaidTrackLimited) {
        Log.i(TAG, "oiad=" + oaid + ", isLimitAdTrackingEnabled=" + isOaidTrackLimited);
        updateAdIdInfo(oaid, isOaidTrackLimited);
    }

    @Override
    public void onFail(String errMsg) {
        Log.e(TAG, "getOaid Fail: " + errMsg);
    }

    private void updateAdIdInfo(final String oaid, final boolean isLimitAdTrackingEnabled) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(oaid)) {
                    showLog("oiad=" + oaid);
                }
                showLog( "isLimitAdTrackingEnabled=" + isLimitAdTrackingEnabled);
            }
        });
    }

}
