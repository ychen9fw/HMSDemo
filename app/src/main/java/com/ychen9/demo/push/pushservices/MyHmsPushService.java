package com.ychen9.demo.push.pushservices;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.Utils;
import com.onesignal.OneSignalHmsEventBridge;
import com.urbanairship.push.hms.AirshipHmsIntegration;
import com.ychen9.demo.push.PushActivity;
import com.huawei.hms.push.HmsMessageService;
import com.huawei.hms.push.RemoteMessage;
import com.huawei.hms.push.SendException;

import org.json.JSONException;

public class MyHmsPushService extends HmsMessageService {
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.i("TOKEN", s );
        sendMyBroadcast("OnNewToken",s);
        OneSignalHmsEventBridge.onNewToken(this, s);
        AirshipHmsIntegration.processNewToken(getApplicationContext());

    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String msg = "";
        //clevertap
        try {
            String ctData = remoteMessage.getData();
            @SuppressLint("RestrictedApi") Bundle extras = Utils.stringToBundle(ctData);
            CleverTapAPI.createNotification(getApplicationContext(),extras);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OneSignalHmsEventBridge.onMessageReceived(this, remoteMessage);

        // Check if message contains a data payload.
        if (remoteMessage.getData().length()> 0) {
            msg += "Message data payload: " + remoteMessage.getData();
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            msg = msg + '\n' + "Message Notification Body: " + remoteMessage.getNotification().getBody();
        }
        sendMyBroadcast("onMessageReceived",msg);
        Log.i("HMS Push Data Message: ", msg);

        AirshipHmsIntegration.processMessageSync(getApplicationContext(), remoteMessage);

    }

    private void sendMyBroadcast(String method, String msg) {
        Intent intent = new Intent();
        intent.setAction(PushActivity.TAG);
        intent.putExtra("method",method);
        intent.putExtra("msg",msg);
        //Transfer data to activity by broadcasting
        sendBroadcast(intent);
    }

    @Override
    public void onSendError(String s, Exception e) {
        super.onSendError(s, e);
        Intent intent = new Intent();
        intent.setAction(PushActivity.TAG);
        intent.putExtra("method", "onSendError");
        intent.putExtra("msg", s + "onSendError called, message id:" + s + " ErrCode:"
                + ((SendException) e).getErrorCode() + " message:" + e.getMessage());
        sendBroadcast(intent);
    }
}

