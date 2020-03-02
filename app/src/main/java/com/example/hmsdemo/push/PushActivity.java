package com.example.hmsdemo.push;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;

import com.example.hmsdemo.push.pushPre.BasePush;
import com.example.hmsdemo.push.pushPre.GmsPush;
import com.example.hmsdemo.push.pushPre.HmsPush;
import com.example.hmsdemo.BaseActivity;
import com.example.hmsdemo.utils.TopicDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.huawei.hms.api.HuaweiApiAvailability;
import com.example.hmsdemo.R;

import androidx.appcompat.widget.Toolbar;

public class PushActivity extends BaseActivity implements View.OnClickListener {
    public static String TAG = "push_msg";
    private String token;
    private ProgressDialog progressDialog;//load prompt box
    private BasePush myPush;//push function realize class

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0://show load prompt box
                    progressDialog.show();
                    break;
                case 1://hide load prompt box
                    progressDialog.dismiss();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ghpush);
        findViewById(R.id.btn_subscribe_topic).setOnClickListener(this);
        findViewById(R.id.btn_get_token).setOnClickListener(this);
        findViewById(R.id.btn_unsubscribe_topic).setOnClickListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.title_activity_location_demo);
        }

        //Use Interface to Judje whether Mobile Phone Supports Google MoBile Service,If supported,the result will be return to SUCCESS
        int gmsResult = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        //Use Interface to Judje whether Mobile Phone Supports Huawei MoBile Service,If supported,the result will be return to SUCCESS
        int hmsResult = HuaweiApiAvailability.getInstance().isHuaweiMobileServicesAvailable(this);

        //If both services are supported, GMS is used
        if(gmsResult == ConnectionResult.SUCCESS){
            //Initialized as GMS PUSH functional class
            myPush = new GmsPush(this);
            tag = "GMS:";
        }else if(hmsResult == com.huawei.hms.api.ConnectionResult.SUCCESS){
            //Initialized as HMS PUSH functional class
            myPush = new HmsPush(this);
            tag = "HMS:";
        }else {//If neither service supports, hide all buttons
            findViewById(R.id.btn_subscribe_topic).setVisibility(View.GONE);
            findViewById(R.id.btn_get_token).setVisibility(View.GONE);
            findViewById(R.id.btn_unsubscribe_topic).setVisibility(View.GONE);
            showLog("Unsupported services");
            return;
        }

        // Business-independent, display log usage
        MyReceiver receiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(TAG);
        this.registerReceiver(receiver, filter);
        //Initialize Load Tip Box
        progressDialog = new ProgressDialog(this);
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
    public void onClick(View view) {
        int viewId = view.getId();
        switch (viewId) {
            case R.id.btn_get_token://get token
                getToken();
                break;
            case R.id.btn_subscribe_topic://add topic
                addTopic();
                break;
            case R.id.btn_unsubscribe_topic://unsubscribe topic
                deleteTopic();
                break;
            default:
                break;
        }
    }

    /**
     * Get token
     */
    private void getToken() {
        //Set the callback operation after getting token
        myPush.setOnPushCallBack(new BasePush.OnPushCallBack() {
            @Override
            public void callBack(String result) {
                token = result;
                String msg = "token:" + ("".equals(token)?"token is empty，See the results of onNewToken ()":token);
                showLog(msg);
            }
        });
        //get token
        myPush.getToken();
    }

    /**
     * Add topic
     */
    private void addTopic() {
        //Initialize Add topic Input Box
        final TopicDialog topicDialog = new TopicDialog(this, true);
        //Set up click-to-confirm and cancel listening events
        topicDialog.setOnDialogClickListener(new TopicDialog.OnDialogClickListener() {
            @Override
            public void onConfirmClick(String topic) {//Click the OK button
                topicDialog.dismiss();

                //Display load prompt box
                handler.sendEmptyMessage(0);

                //Start Subscribing to topics
                myPush.addTopic(topic);
            }

            @Override
            public void onCancelClick() {//Click the Cancel button
                topicDialog.dismiss();
            }
        });
        //Set the callback operation after adding the topic
        myPush.setOnPushCallBack(new BasePush.OnPushCallBack() {
            @Override
            public void callBack(String result) {
                //Log Print Subscription Results
                showLog(result);
                //Hidden Load prompt box
                handler.sendEmptyMessage(1);
            }
        });
        //Show input box
        topicDialog.show();
    }

    /**
     * Cancel subscription to topic
     * Same as Add topic
     */
    private void deleteTopic() {
        final TopicDialog topicDialog = new TopicDialog(this, false);
        topicDialog.setOnDialogClickListener(new TopicDialog.OnDialogClickListener() {
            @Override
            public void onConfirmClick(String topic) { //Click the OK button
                topicDialog.dismiss();
                handler.sendEmptyMessage(0);
                myPush.deleteTopic(topic);
            }
            @Override
            public void onCancelClick() {//Click the Cancel button
                topicDialog.dismiss();
            }
        });

        myPush.setOnPushCallBack(new BasePush.OnPushCallBack() {
            @Override
            public void callBack(String result) {
                showLog(result);
                handler.sendEmptyMessage(1);
            }
        });

        topicDialog.show();
    }

    /**
     * Custom Broadcast Class
     * Used to retrieve messages from the MyGmsPushService and MyHmsPushService
     */
    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null && bundle.getString("msg") != null) {
                //If the method name is onNewToken, set the value of token to the value of `msg'.
                if ("onNewToken".equals(bundle.getString("method"))) {
                    token = bundle.getString("msg");
                }
                showLog(bundle.getString("method") + ":" + bundle.getString("msg"));
            }
        }
    }
    public Handler getHandler() {
        return handler;
    }
}
