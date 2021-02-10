package com.ychen9.demo;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.clevertap.android.sdk.ActivityLifecycleCallback;
import com.onesignal.OSInAppMessageAction;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;
import com.swrve.sdk.SwrveNotificationConfig;
import com.swrve.sdk.SwrveSDK;
import com.swrve.sdk.config.SwrveConfig;

import org.json.JSONObject;

public class ApplicationClass extends Application {
    @Override
    public void onCreate() {
        ActivityLifecycleCallback.register(this);
        super.onCreate();

        // OneSignal Initialization
//        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();
        String userId = OneSignal.getPermissionSubscriptionState().getSubscriptionStatus().getUserId();
        String pushToken = OneSignal.getPermissionSubscriptionState().getSubscriptionStatus().getPushToken();
        //swirve
        try {
            SwrveConfig config = new SwrveConfig();
            // To use the EU stack, include this in your config.
            // config.setSelectedStack(SwrveStack.EU);
            NotificationChannel channel = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                channel = new NotificationChannel("123", "Devapp swrve default channel", NotificationManager.IMPORTANCE_DEFAULT);
                if (getSystemService(Context.NOTIFICATION_SERVICE) != null) {
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.createNotificationChannel(channel);
                }
            }
            SwrveNotificationConfig.Builder notificationConfig = new SwrveNotificationConfig.Builder(R.drawable.ic_launcher_foreground, R.drawable.ic_launcher_foreground, channel)
                    .activityClass(MainActivity.class)
                    .largeIconDrawableId(R.drawable.ic_launcher_foreground)
                    .accentColorHex("#3949AB");
            config.setNotificationConfig(notificationConfig.build());

            SwrveSDK.createInstance(this, 31971, "MJcdbrCQHvoFwMSXNvw", config);
        } catch (IllegalArgumentException exp) {
            Log.e("SwrveDemo", "Could not initialize the Swrve SDK", exp);
        }
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
