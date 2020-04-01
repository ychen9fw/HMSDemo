package com.ychen9.demo.signin;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.ychen9.demo.BaseActivity;
import com.ychen9.demo.Constant;
import com.ychen9.demo.R;

import androidx.appcompat.widget.Toolbar;

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
        findViewById(R.id.btn_revokeAuthorization).setOnClickListener(this);
        findViewById(R.id.btn_signout).setOnClickListener(this);
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