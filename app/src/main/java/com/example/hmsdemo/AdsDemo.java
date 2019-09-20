package com.example.hmsdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.huawei.hms.ads.identifier.AdvertisingIdClient;

import java.io.IOException;

public class AdsDemo extends BaseActivity implements OaidCallback{

    private String TAG = "Ads Demo";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ads_demo);

        getIdentifierThread.start();
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
