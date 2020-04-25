package com.ychen9.demo.push;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.Autopilot;
import com.urbanairship.BuildConfig;
import com.urbanairship.UAirship;
import com.urbanairship.push.NotificationActionButtonInfo;
import com.urbanairship.push.NotificationInfo;
import com.urbanairship.push.NotificationListener;
import com.ychen9.demo.R;

import static java.security.AccessController.getContext;

public class SampleAutopilot extends Autopilot {

    private String TAG = "Airship";

    @Override
    public void onAirshipReady(@NonNull UAirship airship) {
        airship.getPushManager().setUserNotificationsEnabled(true);

        // Additional Airship SDK setup
        airship.setDeepLinkListener(deepLink -> {
            // Handle the deepLink
            return true;
        });

        airship.getPushManager().setNotificationListener(new NotificationListener() {
            @Override
            public void onNotificationPosted(@NonNull NotificationInfo notificationInfo) {
                Log.i(TAG, "Notification posted. Alert: " + notificationInfo.getMessage().getAlert() + ". NotificationId: " + notificationInfo.getNotificationId());
            }

            @Override
            public boolean onNotificationOpened(@NonNull NotificationInfo notificationInfo) {
                Log.i(TAG, "Notification opened. Alert: " + notificationInfo.getMessage().getAlert() + ". NotificationId: " + notificationInfo.getNotificationId());
                // Return false here to allow Airship to auto launch the launcher activity
                return false;
            }

            @Override
            public boolean onNotificationForegroundAction(@NonNull NotificationInfo notificationInfo, @NonNull NotificationActionButtonInfo actionButtonInfo) {
                Log.i(TAG, "Notification foreground action. Button ID: " + actionButtonInfo.getButtonId() + ". NotificationId: " + notificationInfo.getNotificationId());
                return false;
            }

            @Override
            public void onNotificationBackgroundAction(@NonNull NotificationInfo notificationInfo, @NonNull NotificationActionButtonInfo actionButtonInfo) {
                Log.i(TAG, "Notification background action. Button ID: " + actionButtonInfo.getButtonId() + ". NotificationId: " + notificationInfo.getNotificationId());
            }

            @Override
            public void onNotificationDismissed(@NonNull NotificationInfo notificationInfo) {
                Log.i(TAG, "Notification dismissed. Alert: " + notificationInfo.getMessage().getAlert() + ". Notification ID: " + notificationInfo.getNotificationId());
            }

        });

        airship.getPushManager().addPushListener((message, notificationPosted) -> {
            Log.i(TAG, "Received push message. Alert: " + message.getAlert() + ". posted notification: " + notificationPosted);
        });

    }


//    @Override
//    public AirshipConfigOptions createAirshipConfigOptions(@NonNull Context context) {
//        AirshipConfigOptions options = new AirshipConfigOptions.Builder()
//                .setDevelopmentAppKey("Your Development App Key")
//                .setDevelopmentAppSecret("Your Development App Secret")
//                .setProductionAppKey("Your Production App Key")
//                .setProductionAppSecret("Your Production App Secret")
//                .setInProduction(!BuildConfig.DEBUG)
//                .setNotificationIcon(R.drawable.ic_notification)
//                .setNotificationAccentColor(ContextCompat(getContext(), R.color.accent))
//                .setNotificationChannel("customChannel")
//                .build();
//
//        return options;
//    }
}
