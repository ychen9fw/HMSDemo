package com.example.hmsdemo.signin;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.auth0.android.Auth0;
import com.auth0.android.authentication.AuthenticationException;
import com.auth0.android.provider.AuthCallback;
import com.auth0.android.provider.WebAuthProvider;
import com.auth0.android.result.Credentials;
import com.example.hmsdemo.BaseActivity;
import com.example.hmsdemo.Constant;
import com.example.hmsdemo.R;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

/**
 * login test
 */
public class GHSignin extends BaseActivity implements OnClickListener {

    /**
     * TAG
     */
    public static final String TAG = "IdActivity";
    private Auth0 auth0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ghsignin);
        findViewById(R.id.btn_SignInIDToken).setOnClickListener(this);
        findViewById(R.id.btn_revokeAuthorization).setOnClickListener(this);
        findViewById(R.id.btn_signout).setOnClickListener(this);
        findViewById(R.id.btn_GoogleSignIn).setOnClickListener(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.title_activity_login_demo);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_SignInIDToken:
                AccountManager.getInstance().signIn(
                        this, Constant.REQUEST_SIGN_IN_LOGIN);
                break;
            case R.id.btn_revokeAuthorization:
                AccountManager.getInstance().removeAuth(this);
                break;
            case R.id.btn_signout:
                AccountManager.getInstance().signOut(this);
                break;
            case R.id.btn_GoogleSignIn:
                googleSignin();
                break;
            default:
                break;
        }
    }

    private void googleSignin() {
        auth0 = new Auth0(this);
        auth0.setOIDCConformant(true);
        WebAuthProvider.login(auth0)
                .withScheme("demo")
                .withAudience(String.format("https://%s/userinfo", getString(R.string.com_auth0_domain)))
                .start(GHSignin.this, new AuthCallback() {
                    @Override
                    public void onFailure(@NonNull Dialog dialog) {
                        // Show error Dialog to user
                        showLog("signIn failed ");
                    }

                    @Override
                    public void onFailure(AuthenticationException exception) {
                        // Show error to user
                        showLog("signIn failed ");
                    }

                    @Override
                    public void onSuccess(@NonNull Credentials credentials) {
                        showLog("credentials " + credentials.getAccessToken());
                        // Store credentials
                        // Navigate to your main activity
                    }
                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constant.REQUEST_SIGN_IN_LOGIN) {
            AccountInfo accountInfo = AccountManager.getInstance().getSignedInAccountFromIntent(
                    data, this);
            showLog(accountInfo.getDisplayName() + " signIn success ");
            //showLog("AccessToken: " + accountInfo.getAccessToken());
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