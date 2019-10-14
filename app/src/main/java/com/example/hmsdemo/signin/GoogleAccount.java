package com.example.hmsdemo.signin;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

/**
 * Google Account Interface Implementation Class
 */
public class GoogleAccount implements AccountInterface {
    private static final String TAG = "GoogleAccount";

    private GoogleSignInClient signInClient;

    public GoogleAccount(Activity activity) {
        initGMS(activity);
    }

    private void initGMS(Activity activity) {
        // XXXserverClientId needs to be replaced with The actual character string.
        GoogleSignInOptions mSignInOptionsGms = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                //.requestIdToken("XXXserverClientId")
                //.requestServerAuthCode("XXXserverClientId")
                .build();
        signInClient = GoogleSignIn.getClient(activity, mSignInOptionsGms);
    }

    @Override
    public Intent getSignInIntent() {
        return signInClient.getSignInIntent();
    }

    @Override
    public AccountInfo getSignedInAccountFromIntent(Intent data) {
        AccountInfo accountInfo = new AccountInfo();
        Task<GoogleSignInAccount> taskGMS = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount googleAccount = taskGMS.getResult(ApiException.class);
            //accountInfo.setPhotoUrl(googleAccount.getPhotoUrl().toString());
            accountInfo.setDisplayName(googleAccount.getDisplayName());
            accountInfo.setAccessToken(googleAccount.getIdToken());
        } catch (com.google.android.gms.common.api.ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
        return accountInfo;
    }
}
