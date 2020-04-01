package com.ychen9.demo.signin;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.huawei.hmf.tasks.OnCompleteListener;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService;

/**
 * Huawei Account Interface Implementation Class
 */
public class HuaweiAccount implements AccountInterface {
    private static final String TAG = "HuaweiAccount";

    private HuaweiIdAuthService service;
    private Activity activity;
    HuaweiIdAuthParams authParams;
    public HuaweiAccount(Activity activity){
        this.activity = activity;
        initHMS(activity);
    }

    private void initHMS(Activity activity) {

        authParams = new HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM).setIdToken().createParams();
        service= HuaweiIdAuthManager.getService(activity, authParams);

    }

    @Override
    public Intent getSignInIntent() {
        return service.getSignInIntent();
    }

    @Override
    public AccountInfo getSignedInAccountFromIntent(Intent data) {
        AccountInfo accountInfo = new AccountInfo();
        Task<AuthHuaweiId> authHuaweiIdTask  = HuaweiIdAuthManager.parseAuthResultFromIntent(data);
        if (authHuaweiIdTask.isSuccessful()) {
            AuthHuaweiId  huaweiAccount = authHuaweiIdTask.getResult();
            accountInfo.setAccessToken(huaweiAccount.getIdToken());
            accountInfo.setDisplayName(huaweiAccount.getDisplayName());
            //accountInfo.setPhotoUrl(huaweiAccount.getPhotoUriString());
            Log.i(TAG, "signIn successful: ");
        } else {
            Log.i(TAG, "signIn failed: " + ((ApiException) authHuaweiIdTask.getException()).getStatusCode());
        }
        return accountInfo;
    }

    @Override
    public void removeAuth(){
        //showLog("revoke Auth demo");
        service.cancelAuthorization().addOnCompleteListener(new OnCompleteListener<Void>(){
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
        Task<Void> signOutTask = service.signOut();
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
