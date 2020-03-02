package com.example.hmsdemo.signin;

import android.app.Activity;
import android.content.Intent;

import com.google.android.gms.common.GoogleApiAvailability;
import com.huawei.hms.api.ConnectionResult;
import com.huawei.hms.api.HuaweiApiAvailability;

/**
 * Mobile Service Manager
 * which is used to shield the differences between different interfaces.
 */
public class AccountManager {

    private static AccountManager instance = new AccountManager();

    private AccountInterface accountService = null;

    private Object lock = new Object();

    public static AccountManager getInstance() {
        return instance;
    }

    /**
     * Check whether the Google account is available.
     * @param activity activity
     * @return boolean true or false
     */
    public boolean isGoogleAccountAvailable(Activity activity) {
        boolean result = false;
        int gmsResult = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity);
        if (gmsResult == com.google.android.gms.common.ConnectionResult.SUCCESS) {
            return true;
        }
        return result;
    }

    /**
     * Check whether the Huawei account is available.
     * @param activity activity
     * @return boolean true or false
     */
    public boolean isHuaweiAccountAvailable(Activity activity) {
        boolean result = false;
        int huaweiResult = HuaweiApiAvailability.getInstance().isHuaweiMobileServicesAvailable(activity);
        if (huaweiResult == ConnectionResult.SUCCESS) {
            return true;
        }
        return result;
    }

    /**
     * signIn
     * @param activity activity
     */
    public void signIn(Activity activity, int requestCode) {
        activity.startActivityForResult(getSignInIntent(activity), requestCode);
    }

    /**
     * getSignedInAccount
     * @param activity context
     * @return Intent Intent
     */
    public Intent getSignInIntent(Activity activity) {
        return getAccountService(activity).getSignInIntent();
    }

    /**
     * Obtaining Login Account Information
     * @param data     data
     * @param activity activity
     * @return AccountInfo AccountInfo
     */
    public AccountInfo getSignedInAccountFromIntent(Intent data, Activity activity) {
        return getAccountService(activity).getSignedInAccountFromIntent(data);
    }

    public void removeAuth(Activity activity){
        getAccountService(activity).removeAuth();
    }

    public void signOut(Activity activity) {getAccountService(activity).signOut();}

    private AccountInterface getAccountService(Activity activity) {
        if (null == this.accountService) {
            synchronized (this.lock) {
                if (null == this.accountService) {
                    // If the Google account is available, use the Google account first.
                    if (AccountPolicy.isGoogleAccount(activity)){
                        this.accountService = new GoogleAccount(activity);
                    } else {
                        this.accountService = new HuaweiAccount(activity);
                    }
                }
            }
        }
        return this.accountService;
    }
}
