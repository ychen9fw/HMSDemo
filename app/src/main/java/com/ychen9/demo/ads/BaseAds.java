package com.ychen9.demo.ads;

import com.ychen9.demo.BaseActivity;

public abstract class BaseAds {

    onAidCallback callBack;
    BaseActivity baseActivity;

    public abstract void getAdvertisingInfo();

    public interface onAidCallback {
        void onSuccuss(String aid, boolean isAidTrackLimited);

        void onFail(String errMsg);
    }

    public void setaIdCallBack(onAidCallback onaIdCallBack) {
        this.callBack = onaIdCallBack;
    }
}
