package com.ychen9.demo.ads;

import android.util.Log;

import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.banner.BannerView;
import com.ychen9.demo.BaseActivity;
import com.huawei.hms.ads.identifier.AdvertisingIdClient;
import com.ychen9.demo.R;

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

        BannerView bottomBannerView = baseActivity.findViewById(R.id.hw_banner_view);
        AdParam adParam = new AdParam.Builder().build();
        bottomBannerView.loadAd(adParam);

        BannerView testbottomBannerView = baseActivity.findViewById(R.id.test_banner_view);
        AdParam testadParam = new AdParam.Builder().build();
        testbottomBannerView.loadAd(testadParam);
    }
}
