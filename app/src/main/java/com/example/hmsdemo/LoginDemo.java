package com.example.hmsdemo;

import android.os.Bundle;

import android.view.MenuItem;
import android.view.View;

import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;
import android.content.Intent;
import android.support.v7.widget.Toolbar;
//HMS Account Library
import com.example.hmsdemo.utils.HTTPManager;
import com.example.hmsdemo.utils.JSONConverter;
import com.huawei.hmf.tasks.OnCompleteListener;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.auth.api.signin.HuaweiIdSignIn;
import com.huawei.hms.auth.api.signin.HuaweiIdSignInClient;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.support.api.entity.auth.Scope;
import com.huawei.hms.support.api.hwid.HuaweiIdSignInOptions;
import com.huawei.hms.support.api.hwid.SignInHuaweiId;

import org.json.JSONObject;

import java.util.Set;


public class LoginDemo extends BaseActivity implements View.OnClickListener{
    private static final String TAG = "Login Activity";

    HuaweiIdSignInOptions signInOptions;
    HuaweiIdSignInClient client;
    String token = "";
    String userName = "";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_demo);

        findViewById(R.id.btn_SignInIDToken).setOnClickListener(this);
        findViewById(R.id.btn_revokeAuthorization).setOnClickListener(this);
        findViewById(R.id.btn_silentSignin).setOnClickListener(this);
        findViewById(R.id.btn_signout).setOnClickListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.title_activity_login_demo);
        }

        signInOptions = new HuaweiIdSignInOptions.Builder(HuaweiIdSignInOptions.DEFAULT_SIGN_IN).requestIdToken("").build();
        client = HuaweiIdSignIn.getClient(LoginDemo.this, signInOptions);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    private void signInbyIDToken() {
        startActivityForResult(client.getSignInIntent(), 8888);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        showLog("Login Demo");
        //Process the sign-in and authorization result and obtain an ID token from SignInHuaweiId.
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 8888) {
            Task<SignInHuaweiId> signInHuaweiIdTask = HuaweiIdSignIn.getSignedInAccountFromIntent(data);
            if (signInHuaweiIdTask.isSuccessful()) {
                //The sign-in is successful, and the user's HUAWEI ID information and ID token are obtained.
                SignInHuaweiId huaweiAccount = signInHuaweiIdTask.getResult();
                token = huaweiAccount.getIdToken();
                String displayName = huaweiAccount.getDisplayName();
                String emailaddress = huaweiAccount.getEmail();
                Set<Scope> scopeSet = huaweiAccount.getGrantedScopes();

                showLog("displayName:" + displayName);
                showLog("emailaddress:" + emailaddress);
                showLog("idToken:" + huaweiAccount.getIdToken());
                Log.i(TAG, "idToken:" + huaweiAccount.getIdToken());

                //Verify idToken, usually it is done on backend server
                tokenVerifyCon task = new tokenVerifyCon(LoginDemo.this);
                task.execute();
            }
            else
            {
                //The sign-in failed.
                Log.e(TAG, "sign in failed : " + ((ApiException)signInHuaweiIdTask.getException()).getStatusCode());
            }
        }
    }


    private class tokenVerifyCon extends AsyncTask<String, Void, String>  {


        public tokenVerifyCon(LoginDemo activity) {
        }

        @Override
        protected void onPreExecute() {
            showLog("verifying token");
        }

        @Override
        public String doInBackground(String... params) {

            //Verify Tokeninfo: HTTP GET request on get_url
            String get_url = "https://oauth-login.cloud.huawei.com/oauth2/v3/tokeninfo?id_token="+token;;
            JSONObject json = HTTPManager.getData(get_url);
            //parse JSON to get user info
            if (json != null) {
                userName = JSONConverter.parseFeed(json);
            }
            return userName;
        }

        @Override
        protected void onPostExecute(String result){
            showLog("verify token successfully, user name is" + userName);
/*            Toast toast=Toast.makeText(getApplicationContext(),"Welcome "+ userName + " to Log In",Toast.LENGTH_SHORT);
            toast.setMargin(50,50);
            toast.show();*/
        }

    }

    private void revokeAuth(){
        showLog("revoke Auth demo");
        client.revokeAccess().addOnCompleteListener(new OnCompleteListener<Void>(){
            //Perform operations after the withdrawal.
            @Override
            public void onComplete(Task<Void> task){
                if (task.isSuccessful()){
                    //do some thing while revoke success
                    showLog("revoke Auth success");
                    Log.i(TAG, "onSuccess: ");
                }else{
                    //do some thing while revoke success
                    Exception exception = task.getException();
                    if (exception instanceof ApiException){
                        int statusCode = ((ApiException) exception).getStatusCode();
                        showLog("revoke Auth onFailure: " + statusCode);
                        Log.i(TAG, "onFailure: " + statusCode);
                    }
                }
            }
        });
    }

    private void silentSignIn(){
        showLog("silent sign in demo");
        Task<SignInHuaweiId> task = client.silentSignIn();
        task.addOnSuccessListener(new OnSuccessListener<SignInHuaweiId>() {
            @Override
            public void onSuccess(SignInHuaweiId signInHuaweiId) {
                showLog("Successfully Sign in");
                Log.i(TAG, "silentSignIn success");
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                //if Failed use getSignInIntent
                if (e instanceof ApiException) {
                    ApiException apiException = (ApiException) e;
                    showLog(e.toString());
                    signInbyIDToken();
                }
            }
        });

    }

    /**
     *  After this method is called, the next time you call signIn will pull the activity, please call carefully. Do not call if you are unsure.
     */
    private void signOut(){
        showLog("sign out demo");
        Task<Void> signOutTask = client.signOut();
        signOutTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                showLog("Successfully Sign out");
                Log.i(TAG, "signOut Success");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.i(TAG, "signOut fail");
            }
        });

    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_SignInIDToken:
                signInbyIDToken();
                break;
            case R.id.btn_revokeAuthorization:
                revokeAuth();
                break;
            case R.id.btn_silentSignin:
                silentSignIn();
                break;
            case R.id.btn_signout:
                signOut();
                break;
            default:
        }
    }
}
