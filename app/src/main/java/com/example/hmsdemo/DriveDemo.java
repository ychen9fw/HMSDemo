package com.example.hmsdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.huawei.api.services.drive.Drive;
import com.huawei.api.services.drive.DriveScopes;
import com.huawei.hms.auth.api.signin.HuaweiIdSignIn;
import com.huawei.hms.support.api.entity.auth.Scope;
import com.huawei.hms.support.api.hwid.HuaweiId;
import com.huawei.hms.support.api.hwid.HuaweiIdSignInOptions;
import com.huawei.hms.support.api.hwid.SignInHuaweiId;

import java.util.Set;

public class DriveDemo extends AppCompatActivity {

    private SignInHuaweiId huaweiId;
    private HuaweiIdSignInOptions signInOptions;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive_demo);
    }


    private void userSignIn(){
        // get signed in account
        huaweiId = HuaweiIdSignIn.getLastSignedInAccount(this);

        if(huaweiId == null) {
            // if there is no signed user, redirect user to signin, create signInOptions with Scope
            signInOptions = new HuaweiIdSignInOptions.Builder(HuaweiIdSignInOptions.DEFAULT_SIGN_IN)
                    .requestAccessToken()
                    .requestIdToken("")
                    // basic permission
                    .requestScopes(HuaweiId.HUAEWEIID_BASE_SCOPE)
                    // All permission, except Drive permission
                    .requestScopes(new Scope(DriveScopes.DRIVE))
                    // Permisison to access to meta data only
                    .requestScopes(new Scope(DriveScopes.DRIVE_METADATA))
                    // Permisison to access to read meta data only
                    .requestScopes(new Scope(DriveScopes.DRIVE_METADATA_READONLY))
                    // Permisison to access to read meta data and content
                    .requestScopes(new Scope(DriveScopes.DRIVE_READONLY))
                    .build();
        }
    }

}
