package com.ychen9.demo.signin;
import android.app.Activity;

/**
 * account policy
 * If the Google account is available, use the Google account first.
 */
public class AccountPolicy {

    /**
     * Check whether the Google account is available.
     * @param activity activity
     * @return boolean true or false
     */
    public static boolean isGoogleAccount(Activity activity) {
        // If the Google account is available, use the Google account first.
        boolean gmsResult = AccountManager.getInstance().isGoogleAccountAvailable(activity);
        if (gmsResult) {
            return true;
        }
        return false;
    }
}

