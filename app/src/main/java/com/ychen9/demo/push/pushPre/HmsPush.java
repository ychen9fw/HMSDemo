package com.ychen9.demo.push.pushPre;

import android.text.TextUtils;
import android.util.Log;

import com.ychen9.demo.BaseActivity;
import com.ychen9.demo.push.PushActivity;
import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.hmf.tasks.OnCompleteListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.hms.push.HmsMessaging;

/**
 * HMS Push Function Implementation Class
 */
public class HmsPush extends BasePush {
    private String appId = null;//HMS Push needs AAID to get token
    public HmsPush(BaseActivity baseActivity) {
        this.baseActivity = baseActivity;
    }

    @Override
    public void getToken() {
        //If there is no aaid, get the AAID first
        if(appId == null){
            appId = AGConnectServicesConfig.fromContext(baseActivity).getString("client/app_id");
        }
        new Thread() {
            @Override
            public void run() {
                try {
                    if(callBack!=null){
                        //get token
                        String token = HmsInstanceId.getInstance(baseActivity).getToken(appId, "HCM");
                        //Callback the acquired token as a parameter of callback to the user
                        if(!TextUtils.isEmpty(token)) {
                            Log.i("TOKEN", "get token:" + token);
                            callBack.callBack(token);
                            callBack = null;
                        }
                        //Callback completed,set callback empty
                    }
                } catch (Exception e) {
                    //Acquisition failure, print logs
                    baseActivity.showLog("getToken failed" + "\n" + e);
                }
            }
        }.start();
    }

    @Override
    public void addTopic(String topic) {
        try {
            HmsMessaging.getInstance(baseActivity).subscribe(topic)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(Task<Void> task) {
                            String msg = "";
                            if (task.isSuccessful()) {//Subscription success
                                msg = "subscribe Complete";
                            } else {//Subscription failure
                                msg = "subscribe failed: ret=" + task.getException().getMessage();
                            }
                            if(callBack!=null){
                                //Callback subscription results to users
                                callBack.callBack(msg);
                                //Callback completed,set callback empty
                                callBack = null;
                            }

                        }
                    });
        } catch (Exception e) {
            //Acquisition failure, print logs
            baseActivity.showLog("subscribe failed: exception=" + e.getMessage());
            //Hidden Load Tip Box
            ((PushActivity)baseActivity).getHandler().sendEmptyMessage(1);
        }
    }

    @Override
    public void deleteTopic(String topic) {
        try {
            HmsMessaging.getInstance(baseActivity).unsubscribe(topic)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(Task<Void> task) {
                            String msg = "";
                            if (task.isSuccessful()) {
                                msg =  "unsubscribe Complete";
                            } else {
                                msg = "unsubscribe failed: ret=" + task.getException().getMessage();
                            }
                            if(callBack!=null){
                                //Callback subscription results to users
                                callBack.callBack(msg);
                                //Callback completed,set callback empty
                                callBack = null;
                            }
                        }
                    });
        } catch (Exception e) {
            //Acquisition failure, print logs
            baseActivity.showLog("unsubscribe failed: exception=" + e.getMessage());
            //Hidden Load Tip Box
            ((PushActivity)baseActivity).getHandler().sendEmptyMessage(1);
        }
    }

    @Override
    public void sendMessage(String msg) {

    }
}

