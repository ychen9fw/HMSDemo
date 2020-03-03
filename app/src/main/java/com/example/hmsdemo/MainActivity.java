package com.example.hmsdemo;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.hmsdemo.ads.HGAds;
import com.example.hmsdemo.cameraKit.CameraMenuActivity;
import com.example.hmsdemo.location.GHLocation;
import com.example.hmsdemo.map.MapActivity;
import com.example.hmsdemo.payment.GMSPayment;
import com.example.hmsdemo.payment.HMSPayment;
import com.example.hmsdemo.push.PushActivity;
import com.example.hmsdemo.scan.ScanActivity;
import com.example.hmsdemo.signin.GHSignin;
import com.example.hmsdemo.utils.PermissionManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.huawei.hms.api.HuaweiApiAvailability;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.TokenResponse;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private PermissionManager permissionManager;
    private final int REQUEST_LOCATION = 1;
    private static final String SHARED_PREFERENCES_NAME = "AuthStatePreference";
    private static final String AUTH_STATE = "AUTH_STATE";
    private static final String USED_INTENT = "USED_INTENT";
    private String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setToolbar();

        if(!isNetworkConnected()){
            Toast.makeText(this, "No Available Network. Please try again later", Toast.LENGTH_LONG).show();
            return;
        }

        permissionManager = new PermissionManager(this);
        findViewById(R.id.btn_HuaweiIDDemo).setOnClickListener(this);
        findViewById(R.id.btn_HuaweiLocationDemo).setOnClickListener(this);
        findViewById(R.id.btn_HuaweiPushDemo).setOnClickListener(this);
        findViewById(R.id.btn_HuaweiAdsDemo).setOnClickListener(this);
        findViewById(R.id.btn_HuaweiPayDemo).setOnClickListener(this);
        findViewById(R.id.btn_HuaweiMapDemo).setOnClickListener(this);
        findViewById(R.id.btn_HuaweiMLDemo).setOnClickListener(this);
        findViewById(R.id.btn_HuaweiScanDemo).setOnClickListener(this);
        findViewById(R.id.btn_HuaweiCameraDemo).setOnClickListener(this);
        findViewById(R.id.btn_GoogleSignIn).setOnClickListener(this);
    }

    public void setToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        // Click on button
        switch (id) {
            case R.id.btn_HuaweiIDDemo:
                goToHuaweiIDDemo();
                break;
            case R.id.btn_HuaweiLocationDemo:
                checkPermission();
                break;
            case R.id.btn_HuaweiPayDemo:
                goToHuaweiPayDemo();
                break;
            case R.id.btn_HuaweiPushDemo:
                goToHuaweiPushDemo();
                break;
            case R.id.btn_HuaweiAdsDemo:
                goToHuaweiAdsDemo();
                break;
            case R.id.btn_HuaweiMapDemo:
                goToHuaweiMapDemo();
                break;
            case R.id.btn_HuaweiMLDemo:
                goToHuaweiMLDemo();
                break;
            case R.id.btn_HuaweiScanDemo:
                goToHuaweiScanDemo();
                break;
            case R.id.btn_HuaweiCameraDemo:
                goToHuaweiCameraDemo();
                break;
            case R.id.btn_GoogleSignIn:
                GoogleSignin();
                break;
            default:
        }
    }

    private void goToHuaweiCameraDemo() {
        Intent intent = new Intent(this, CameraMenuActivity.class);
        startActivity(intent);
    }

    private void goToHuaweiScanDemo() {
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
    }

    private void goToHuaweiMLDemo() {
        Intent intent = new Intent(this, com.example.hmsdemo.ml.menu.MainActivity.class);
        startActivity(intent);
    }

    private void goToHuaweiMapDemo() {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }


    private void goToHuaweiAdsDemo() {
        Intent intent = new Intent(this, HGAds.class);
        startActivity(intent);
    }

    private void goToHuaweiPushDemo() {
        if(!isActivityActive("pushDemo")){
            setActiveStatus("pushDemo",true);
            Intent intent = new Intent(this, PushActivity.class);
            startActivity(intent);
        }
    }

    private void goToHuaweiPayDemo() {
        Intent intent = null;
        int gmsResult = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        //Use Interface to Judje whether Mobile Phone Supports Huawei MoBile Service,If supported,the result will be return to SUCCESS
        int hmsResult = HuaweiApiAvailability.getInstance().isHuaweiMobileServicesAvailable(this);
        if(gmsResult == ConnectionResult.SUCCESS){
            //Initialized as GMS PAY functional class
            intent = new Intent(this,GMSPayment.class);
        }else if(hmsResult == com.huawei.hms.api.ConnectionResult.SUCCESS){
            //Initialized as HMS PAY functional class
            intent = new Intent(this, HMSPayment.class);
        }else {//If neither service supports, hide all buttons
            return;
        }

        startActivity(intent);
    }

    private void goToHuaweiLocationDemo() {
        Intent intent = new Intent(this, GHLocation.class);
        startActivity(intent);
    }

    private void goToHuaweiIDDemo() {
        Intent intent = new Intent(this, GHSignin.class);
        startActivity(intent);
    }

    private boolean isActivityActive(String activity){
        SharedPreferences sp = getSharedPreferences("HmsDemoPref", MODE_PRIVATE);
        return sp.getBoolean(activity, false);
    }

    public void setActiveStatus(String activity, boolean active){
        SharedPreferences sp = getSharedPreferences("HmsDemoPref", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sp.edit();
        myEdit.putBoolean(activity, active);
    }

    //Check and request user to give permission
    private void checkPermission(){
        permissionManager.checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION, new PermissionManager.PermissionAskListener() {
            @Override
            public void onNeedPermission() {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            }

            @Override
            public void onPermissionPreviouslyDenied() {
                showPermisonRational();
            }

            @Override
            public void onPermissionPreviouslyDeniedWithNeverAskAgain() {
                dialogForSettings("Permission Denied", "Please go to settings to enable location permission.");
            }

            @Override
            public void onPermissionGranted() {
                goToHuaweiLocationDemo();
            }
        });

    }

    /**
     * Google OAuth Login, reference: https://codelabs.developers.google.com/codelabs/appauth-android-codelab/#0
     *
     * Kicks off the authorization flow.
     */
    public void GoogleSignin(){
        AuthorizationServiceConfiguration serviceConfiguration = new AuthorizationServiceConfiguration(
                Uri.parse("https://accounts.google.com/o/oauth2/v2/auth") /* auth endpoint */,
                Uri.parse("https://www.googleapis.com/oauth2/v4/token") /* token endpoint */
        );

        String clientId = "511828570984-fuprh0cm7665emlne3rnf9pk34kkn86s.apps.googleusercontent.com";
        Uri redirectUri = Uri.parse("com.google.codelabs.appauth:/oauth2callback");
        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(
                serviceConfiguration,
                clientId,
                AuthorizationRequest.RESPONSE_TYPE_CODE,
                redirectUri
        );
        builder.setScopes("profile");
        AuthorizationRequest request = builder.build();

        AuthorizationService authorizationService = new AuthorizationService(this);

        String action = "com.google.codelabs.appauth.HANDLE_AUTHORIZATION_RESPONSE";
        Intent postAuthorizationIntent = new Intent(action);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, request.hashCode(), postAuthorizationIntent, 0);
        authorizationService.performAuthorizationRequest(request, pendingIntent);
    }


    /** Exchanges the code, for the {@link TokenResponse}.
     *
     * @param intent represents the {@link Intent} from the Custom Tabs or the System Browser.
     *
     */
    private void handleAuthorizationResponse(@NonNull Intent intent) {
        AuthorizationResponse response = AuthorizationResponse.fromIntent(intent);
        AuthorizationException error = AuthorizationException.fromIntent(intent);
        final AuthState authState = new AuthState(response, error);

        if (response != null) {
            Log.i(TAG, String.format("Handled Authorization Response %s ", authState.toJsonString()));
            AuthorizationService service = new AuthorizationService(this);
            service.performTokenRequest(response.createTokenExchangeRequest(), new AuthorizationService.TokenResponseCallback() {
                @Override
                public void onTokenRequestCompleted(@Nullable TokenResponse tokenResponse, @Nullable AuthorizationException exception) {
                    if (exception != null) {
                        Log.i(TAG,"Token Exchange failed " + exception);
                    } else {
                        if (tokenResponse != null) {
                            authState.update(tokenResponse, exception);
                            persistAuthState(authState);
                            Log.i(TAG, String.format("Token Response [ Access Token: %s, ID Token: %s ]", tokenResponse.accessToken, tokenResponse.idToken));
                            authState.performActionWithFreshTokens(new AuthorizationService(getApplicationContext()), new AuthState.AuthStateAction() {
                                @Override
                                public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException exception) {
                                    new AsyncTask<String, Void, JSONObject>() {
                                        @Override
                                        protected JSONObject doInBackground(String... tokens) {
                                            OkHttpClient client = new OkHttpClient();
                                            Request request = new Request.Builder()
                                                    .url("https://www.googleapis.com/oauth2/v3/userinfo")
                                                    .addHeader("Authorization", String.format("Bearer %s", tokens[0]))
                                                    .build();

                                            try {
                                                Response response = client.newCall(request).execute();
                                                String jsonBody = response.body().string();
                                                Log.i(TAG,String.format("User Info Response %s", jsonBody));
                                                return new JSONObject(jsonBody);
                                            } catch (Exception exception) {
                                                Log.i(TAG, exception.toString());
                                            }
                                            return null;
                                        }

                                        @Override
                                        protected void onPostExecute(JSONObject userInfo) {
                                            if (userInfo != null) {
                                                String fullName = userInfo.optString("name", null);
                                                String givenName = userInfo.optString("given_name", null);
                                                String familyName = userInfo.optString("family_name", null);
                                                String imageUrl = userInfo.optString("picture", null);
                                                Toast.makeText(getApplicationContext(), "welcome " + fullName  + " log in", Toast.LENGTH_LONG).show();
                                                String message;
                                                if (userInfo.has("error")) {
                                                    message = String.format("%s [%s]", "failed request", userInfo.optString("error_description", "No description"));
                                                } else {
                                                    message = "request completed";
                                                }
                                            }
                                        }
                                    }.execute(accessToken);
                                }
                            });
                        }
                    }
                }
            });
        }
    }

    private void persistAuthState(@NonNull AuthState authState) {
        getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
                .putString(AUTH_STATE, authState.toJsonString())
                .commit();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        checkIntent(intent);
    }

    private void checkIntent(@Nullable Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            switch (action) {
                case "com.google.codelabs.appauth.HANDLE_AUTHORIZATION_RESPONSE":
                    if (!intent.hasExtra(USED_INTENT)) {
                        handleAuthorizationResponse(intent);
                        intent.putExtra(USED_INTENT, true);
                    }
                    break;
                default:
                    // do nothing
            }
        }
    }


    /*Check Network*/
    private boolean isNetworkConnected(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    /* for permission rational*/
    private void showPermisonRational() {
        new AlertDialog.Builder(this).setTitle("Permission Denied").setMessage("Location permission is essential for this app, without location permission, you are not able to get resource around you!")
                .setCancelable(false)
                .setNegativeButton("I'M SURE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("RETRY", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
                        dialog.dismiss();
                    }
                }).show();

    }

    private void dialogForSettings(String title, String msg) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(msg)
                .setCancelable(false)
                .setNegativeButton("NOT NOW", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("SETTINGS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        goToSettings();
                        dialog.dismiss();
                    }
                }).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permissionIsGranted = true;
                    goToHuaweiLocationDemo();
                } else {
                    // Permission was denied.......
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }

        }
    }

    private void goToSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.parse("package:" + getPackageName());
        intent.setData(uri);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkIntent(getIntent());
    }

}
