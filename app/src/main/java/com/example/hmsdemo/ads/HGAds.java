package com.example.hmsdemo.ads;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.hmsdemo.BaseActivity;
import com.example.hmsdemo.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.huawei.hms.api.HuaweiApiAvailability;

import java.io.IOException;

import androidx.appcompat.widget.Toolbar;

public class HGAds extends BaseActivity {

    private BaseAds AdsService;
    private String TAG = "AdsActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hgads);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.title_activity_ads_demo);
        }

        int gmsResult = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        //Use Interface to Judje whether Mobile Phone Supports Huawei MoBile Service,If supported,the result will be return to SUCCESS
        int hmsResult = HuaweiApiAvailability.getInstance().isHuaweiMobileServicesAvailable(this);
        if(gmsResult == ConnectionResult.SUCCESS){
            //Initialized as GMS PUSH functional class
            AdsService = new GMSAds(this);
            tag = "GMS:";
        }else if(hmsResult == com.huawei.hms.api.ConnectionResult.SUCCESS){
            //Initialized as HMS PUSH functional class
            AdsService = new HMSAds(this);
            tag = "HMS:";
        }else {//If neither service supports, hide all buttons
            return;
        }

        getIdentifierThread.start();
        AdsService.setaIdCallBack(new BaseAds.onAidCallback() {
            @Override
            public void onSuccuss(String aid, boolean isAidTrackLimited) {
                Log.i(TAG, "oiad=" + aid + ", isLimitAdTrackingEnabled=" + isAidTrackLimited);
                updateAdIdInfo(aid, isAidTrackLimited);
            }

            @Override
            public void onFail(String errMsg) {
                Log.e(TAG, "getOaid Fail: " + errMsg);
            }
        });

        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                Toast.makeText(HGAds.this, "Ad failed: " + errorCode, Toast.LENGTH_LONG).show();
            }

        });
        mAdView.loadAd(adRequest);
    }

    private Thread getIdentifierThread = new Thread() {

        @Override
        public void run() {
            getOaid();
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    private void getOaid() {

        AdsService.getAdvertisingInfo();
    }


    private void updateAdIdInfo(final String aid, final boolean isLimitAdTrackingEnabled) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(aid)) {
                    showLog("advertising id=" + aid);
                }
                showLog( "isLimitAdTrackingEnabled=" + isLimitAdTrackingEnabled);
            }
        });
    }
}
