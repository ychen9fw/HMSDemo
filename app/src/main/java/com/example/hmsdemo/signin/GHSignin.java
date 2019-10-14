package com.example.hmsdemo.signin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.example.hmsdemo.BaseActivity;
import com.example.hmsdemo.Constant;
import com.example.hmsdemo.R;

/**
 * login test
 */
public class GHSignin extends BaseActivity implements OnClickListener {

    /**
     * TAG
     */
    public static final String TAG = "IdActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ghsignin);
        findViewById(R.id.btn_SignInIDToken).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_SignInIDToken:
                AccountManager.getInstance().signIn(
                        this, Constant.REQUEST_SIGN_IN_LOGIN);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constant.REQUEST_SIGN_IN_LOGIN) {
            AccountInfo accountInfo = AccountManager.getInstance().getSignedInAccountFromIntent(
                    data, this);
            showLog(accountInfo.getDisplayName() + " signIn success ");
            showLog("AccessToken: " + accountInfo.getAccessToken());
            //Log.i(TAG, "Display Photo url: " + accountInfo.getPhotoUrl());
        }

        if (requestCode == Constant.REQUEST_SIGN_IN_LOGIN_GMS) {
            AccountInfo accountInfo = AccountManager.getInstance().getSignedInAccountFromIntent(
                    data, this);
            showLog(accountInfo.getDisplayName() + " signIn success ");
            showLog("AccessToken: " + accountInfo.getAccessToken());
            //Log.i(TAG, "Display Photo url: " + accountInfo.getPhotoUrl());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}