package com.ychen9.demo.push;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.Autopilot;
import com.urbanairship.BuildConfig;
import com.urbanairship.UAirship;
import com.ychen9.demo.R;

import static java.security.AccessController.getContext;

public class SampleAutopilot extends Autopilot {

    @Override
    public void onAirshipReady(@NonNull UAirship airship) {
        airship.getPushManager().setUserNotificationsEnabled(true);

        // Additional Airship SDK setup
        airship.setDeepLinkListener(deepLink -> {
            // Handle the deepLink
            return true;
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
