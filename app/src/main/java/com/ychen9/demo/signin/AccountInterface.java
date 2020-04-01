package com.ychen9.demo.signin;

import android.content.Intent;

/**
 * Account interface definition, which is used to shield the differences between different interfaces.
 */
public interface AccountInterface {
    /**
     * get signin intent
     * @return intent intent
     */
    Intent getSignInIntent();

    /**
     * get account info
     * @param data data
     * @return AccountInfo AccountInfo
     */
    AccountInfo getSignedInAccountFromIntent(Intent data);
    void removeAuth();
    void signOut();
}
