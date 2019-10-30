package com.example.hmsdemo.ads;

import android.util.Log;

import com.example.hmsdemo.BaseActivity;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import java.io.IOException;

public class GMSAds extends BaseAds{

    private static final String TAG = "HMS Ads";

    public GMSAds(BaseActivity baseActivity){
        this.baseActivity = baseActivity;
    }

    @Override
    public void getAdvertisingInfo() {
        baseActivity.showLog("getting ad id" );
        try {
            AdvertisingIdClient.Info info = AdvertisingIdClient.getAdvertisingIdInfo(baseActivity);
            if (null != info) {
                callBack.onSuccuss(info.getId(), info.isLimitAdTrackingEnabled());
            } else {
                callBack.onFail("aid is null");
            }
        } catch (IOException e) {
            Log.e(TAG, "getAdvertisingIdInfo IOException");
            callBack.onFail("getAdvertisingIdInfo IOException");
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        }
    }
}
