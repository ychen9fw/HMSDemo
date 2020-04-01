package com.ychen9.demo.ads;

import android.util.Log;

import com.ychen9.demo.BaseActivity;
import com.huawei.hms.ads.identifier.AdvertisingIdClient;

import java.io.IOException;

public class HMSAds extends BaseAds {

    private static final String TAG = "HMS Ads";

    public HMSAds(BaseActivity baseActivity){
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
        }
    }
}
