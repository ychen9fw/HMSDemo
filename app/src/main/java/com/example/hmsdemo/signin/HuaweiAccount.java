package com.example.hmsdemo.signin;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.hmsdemo.MainActivity;
import com.huawei.hmf.tasks.OnCompleteListener;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
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
    private Activity activity;
    public HuaweiAccount(Activity activity){
        this.activity = activity;
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
            accountInfo.setAccessToken(huaweiAccount.getIdToken());
            accountInfo.setDisplayName(huaweiAccount.getDisplayName());
            //accountInfo.setPhotoUrl(huaweiAccount.getPhotoUriString());
            Log.i(TAG, "signIn successful: ");
        } else {
            Log.i(TAG, "signIn failed: " + ((ApiException) signInHuaweiIdTask.getException()).getStatusCode());
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
