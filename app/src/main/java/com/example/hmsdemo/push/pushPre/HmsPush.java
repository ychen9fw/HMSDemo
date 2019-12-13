package com.example.hmsdemo.push.pushPre;

import com.example.hmsdemo.BaseActivity;
import com.example.hmsdemo.push.PushActivity;
import com.huawei.hmf.tasks.OnCompleteListener;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.hms.aaid.entity.AAIDResult;
import com.huawei.hms.push.HmsMessaging;

/**
 * HMS Push Function Implementation Class
 */
public class HmsPush extends BasePush {
    private String aaId = null;//HMS Push needs AAID to get token
    public HmsPush(BaseActivity baseActivity) {
        this.baseActivity = baseActivity;
    }

    @Override
    public void getToken() {
        //If there is no aaid, get the AAID first
        if(aaId == null){
            getAAId();
        }
        new Thread() {
            @Override
            public void run() {
                try {
                    if(callBack!=null){
                        //get token
                        String token = HmsInstanceId.getInstance(baseActivity).getToken(aaId, "HCM");
                        //Callback the acquired token as a parameter of callback to the user
                        callBack.callBack(token);
                        //Callback completed,set callback empty
                        callBack = null;
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

    /**
     * get aaid
     */
    private void getAAId(){
        Task<AAIDResult> idResult =  HmsInstanceId.getInstance(baseActivity).getAAID();
        idResult.addOnSuccessListener(new OnSuccessListener<AAIDResult>() {
            @Override
            public void onSuccess(AAIDResult aaidResult) {
                aaId = aaidResult.getId();
                baseActivity.showLog("get aaId:" + aaId );
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                baseActivity.showLog("get aaId failed");
            }
        });
    }
}

