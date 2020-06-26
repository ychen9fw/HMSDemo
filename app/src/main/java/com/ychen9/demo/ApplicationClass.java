package com.ychen9.demo;

import android.app.Application;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.onesignal.OSInAppMessageAction;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import org.json.JSONObject;

public class ApplicationClass extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // OneSignal Initialization
//        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();
        String userId = OneSignal.getPermissionSubscriptionState().getSubscriptionStatus().getUserId();
        String pushToken = OneSignal.getPermissionSubscriptionState().getSubscriptionStatus().getPushToken();
    }

    class ExampleNotificationOpenedHandler implements OneSignal.NotificationOpenedHandler {
        @Override
        public void notificationOpened(OSNotificationOpenResult result) {

            JSONObject data = result.notification.payload.additionalData;
            String customKey;

            if (data != null) {
                customKey = data.optString("customkey", null);
                Log.i("DeepLink", "customkey set with value: " + customKey);
            }
        }
    }

    class ExampleInAppMessageClickHandler implements OneSignal.InAppMessageClickHandler {
        // Example of an action id you could setup on the dashboard when creating the In App Message
        private static final String ACTION_ID_MY_CUSTOM_ID = "MY_CUSTOM_ID";

        @Override
        public void inAppMessageClicked(OSInAppMessageAction result) {
            Log.i("ychen9 demo", "In App Message" + result.toJSONObject().toString());
            Toast.makeText(ApplicationClass.this, "ychen9 demo " + "In App Message " + result.toJSONObject().toString(), Toast.LENGTH_LONG).show();
            if (ACTION_ID_MY_CUSTOM_ID.equals(result.clickName)) {
                Log.i("OneSignalExample", "Custom Action took place! Starting YourActivity!");
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }
}
