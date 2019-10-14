package com.example.hmsdemo.signin;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.huawei.hmf.tasks.Task;
import com.huawei.hms.auth.api.signin.HuaweiIdSignIn;
import com.huawei.hms.auth.api.signin.HuaweiIdSignInClient;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.support.api.hwid.HuaweiIdSignInOptions;
import com.huawei.hms.support.api.hwid.SignInHuaweiId;

/**
 * Huawei Account Interface Implementation Class
 */
public class HuaweiAccount implements AccountInterface {
    private static final String TAG = "HuaweiAccount";

    private HuaweiIdSignInClient signInClient;

    public HuaweiAccount(Activity activity){
        initHMS(activity);
    }

    private void initHMS(Activity activity) {
        HuaweiIdSignInOptions mSignInOptions = new HuaweiIdSignInOptions.Builder(HuaweiIdSignInOptions.DEFAULT_SIGN_IN)
                .requestAccessToken()
                .requestIdToken("").build();
        signInClient = HuaweiIdSignIn.getClient(activity, mSignInOptions);
    }

    @Override
    public Intent getSignInIntent() {
        return signInClient.getSignInIntent();
    }

    @Override
    public AccountInfo getSignedInAccountFromIntent(Intent data) {
        AccountInfo accountInfo = new AccountInfo();
        Task<SignInHuaweiId> signInHuaweiIdTask = HuaweiIdSignIn.getSignedInAccountFromIntent(data);
        if (signInHuaweiIdTask.isSuccessful()) {
            SignInHuaweiId huaweiAccount = signInHuaweiIdTask.getResult();
            accountInfo.setAccessToken(huaweiAccount.getAccessToken());
            accountInfo.setDisplayName(huaweiAccount.getDisplayName());
            //accountInfo.setPhotoUrl(huaweiAccount.getPhotoUriString());
        } else {
            Log.i(TAG, "signIn failed: " + ((ApiException) signInHuaweiIdTask.getException()).getStatusCode());
        }
        return accountInfo;
    }
}
