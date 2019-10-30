package com.example.hmsdemo.signin;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

/**
 * Google Account Interface Implementation Class
 */
public class GoogleAccount implements AccountInterface {
    private static final String TAG = "GoogleAccount";
    private Activity activity;

    private GoogleSignInClient signInClient;

    public GoogleAccount(Activity activity) {
        this.activity = activity;
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
            Log.w(TAG, "Login Successfully");
        } catch (com.google.android.gms.common.api.ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
        return accountInfo;
    }

    @Override
    public void removeAuth(){
        //showLog("revoke Auth demo");
        signInClient.revokeAccess().addOnCompleteListener(new OnCompleteListener<Void>(){
            //Perform operations after the withdrawal.
            @Override
            public void onComplete(Task<Void> task){
                if (task.isSuccessful()){
                    //do some thing while revoke success
                    //showLog("revoke Auth success");
                    String msg = "revoke Auth success";
                    Toast toast= Toast.makeText(activity,msg,Toast.LENGTH_SHORT);
                    toast.setMargin(50,50);
                    toast.show();
                    Log.i(TAG, msg);
                }else{
                    //do some thing while revoke success
                    Exception exception = task.getException();
                    if (exception instanceof ApiException){
                        int statusCode = ((ApiException) exception).getStatusCode();
                        //showLog("revoke Auth onFailure: " + statusCode);
                        Log.i(TAG, "onFailure: " + statusCode);
                    }
                }
            }
        });
    }


    @Override
    public void signOut() {
        Task<Void> signOutTask = signInClient.signOut();
        signOutTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                String msg = "Successfully Sign out";
                Toast toast= Toast.makeText(activity,msg,Toast.LENGTH_SHORT);
                toast.setMargin(50,50);
                toast.show();
                Log.i(TAG, msg);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.i(TAG, "signOut fail");
            }
        });
    }
}
