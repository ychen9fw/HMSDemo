package com.example.hmsdemo;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import com.huawei.api.services.drive.Drive;
import com.huawei.api.services.drive.DriveScopes;
import com.huawei.hms.auth.api.signin.HuaweiIdSignIn;
import com.huawei.hms.support.api.entity.auth.Scope;
import com.huawei.hms.support.api.hwid.HuaweiId;
import com.huawei.hms.support.api.hwid.HuaweiIdSignInOptions;
import com.huawei.hms.support.api.hwid.SignInHuaweiId;

import java.util.Set;

public class DriveDemo extends BaseActivity {

    private SignInHuaweiId huaweiId;
    private HuaweiIdSignInOptions signInOptions;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive_demo);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.title_activity_drive_demo);
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
