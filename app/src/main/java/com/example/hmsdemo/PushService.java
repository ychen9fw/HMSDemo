package com.example.hmsdemo;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.huawei.hms.push.HmsMessageService;
import com.huawei.hms.push.RemoteMessage;
import com.huawei.hms.push.SendException;

import java.util.ArrayList;
import java.util.List;

public class PushService extends HmsMessageService {
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        //Log.d(Constant.PUSH_TAG, s);
        Log.d("Token", "I am getting token");
        Log.d("Token", s);
        Intent intent = new Intent();
        intent.setAction(PushDemo.PUSH_ACTION);
        intent.putExtra("method", "onNewToken");
        intent.putExtra("msg", s);
        sendBroadcast(intent);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData().length() > 0) {
            Log.d(Constant.PUSH_TAG, "Message data payload: " + remoteMessage.getData());
        }
        if (remoteMessage.getNotification() != null) {
            Log.d(Constant.PUSH_TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }

    @Override
    public void onMessageSent(String s) {
        super.onMessageSent(s);
        Intent intent = new Intent();
        intent.setAction(PushDemo.PUSH_ACTION);
        intent.putExtra("method", "onMessageSent");
        intent.putExtra("msg", s);
        sendBroadcast(intent);
    }

    @Override
    public void onSendError(String s, Exception e) {
        super.onSendError(s, e);
        Intent intent = new Intent();
        intent.setAction(PushDemo.PUSH_ACTION);
        intent.putExtra("method", "onSendError");
        intent.putExtra("msg", s + "onSendError called, message id:" + s + " ErrCode:"
                + ((SendException) e).getErrorCode() + " message:" + e.getMessage());
        sendBroadcast(intent);
    }

}
