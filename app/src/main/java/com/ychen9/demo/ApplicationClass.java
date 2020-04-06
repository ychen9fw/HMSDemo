package com.ychen9.demo;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.onesignal.OSInAppMessageAction;
import com.onesignal.OneSignal;

public class ApplicationClass extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // OneSignal Initialization
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .setInAppMessageClickHandler(new ExampleInAppMessageClickHandler())
                .init();
    }

    class ExampleInAppMessageClickHandler implements OneSignal.InAppMessageClickHandler {
        // Example of an action id you could setup on the dashboard when creating the In App Message
        private static final String ACTION_ID_MY_CUSTOM_ID = "MY_CUSTOM_ID";

        @Override
        public void inAppMessageClicked(OSInAppMessageAction result) {
            if (ACTION_ID_MY_CUSTOM_ID.equals(result.clickName)) {
                Log.i("OneSignalExample", "Custom Action took place! Starting YourActivity!");
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }
}
