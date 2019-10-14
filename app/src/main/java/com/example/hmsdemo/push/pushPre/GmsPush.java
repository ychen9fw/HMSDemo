package com.example.hmsdemo.push.pushPre;

import android.util.Log;
import com.example.hmsdemo.BaseActivity;
import com.example.hmsdemo.push.PushActivity;
import com.example.hmsdemo.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import androidx.annotation.NonNull;

/**
 * GMS Push Function Implementation Class
 */
public class GmsPush extends BasePush {

    public GmsPush(BaseActivity baseActivity) {
        this.baseActivity = baseActivity;
    }

    @Override
    public void getToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(PushActivity.TAG, "getInstanceId failed", task.getException());
                            return;
                        }
                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        if(callBack != null){
                            //Callback the acquired token as a parameter of callback to the user
                            callBack.callBack(token);
                            //Callback completed,set callback empty
                            callBack = null;
                        }
                    }
                });
    }

    @Override
    public void addTopic(String topic) {
        try {
            FirebaseMessaging.getInstance().subscribeToTopic(topic)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            String msg = baseActivity.getString(R.string.msg_subscribed);
                            if (!task.isSuccessful()) {
                                msg = baseActivity.getString(R.string.msg_subscribe_failed) + task.getException();
                            }
                            if(callBack != null){
                                //Callback subscription results to users
                                callBack.callBack(msg);
                                //Callback completed,set callback empty
                                callBack = null;
                            }
                        }
                    });
        }catch (Exception e){
            //Acquisition failure, print logs
            baseActivity.showLog("subscribe failed: exception=" + e.getMessage());
            //Hidden Load Tip Box
            ((PushActivity)baseActivity).getHandler().sendEmptyMessage(1);
        }
    }

    @Override
    public void deleteTopic(String topic) {
        try {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            String msg = baseActivity.getString(R.string.msg_unsubscribed);
                            if (!task.isSuccessful()) {
                                msg = baseActivity.getString(R.string.msg_unsubscribe_failed) + task.getException();
                            }
                            if(callBack != null){
                                //Callback subscription results to users
                                callBack.callBack(msg);
                                //Callback completed,set callback empty
                                callBack = null;
                            }
                        }
                    });
        }catch (Exception e){
            //Acquisition failure, print logs
            baseActivity.showLog("subscribe failed: exception=" + e.getMessage());
            //Hidden Load Tip Box
            ((PushActivity)baseActivity).getHandler().sendEmptyMessage(1);
        }
    }
}

