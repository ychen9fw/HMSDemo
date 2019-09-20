package com.example.hmsdemo;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.hmsdemo.utils.OnDialogClickListener;
import com.example.hmsdemo.utils.TopicDialog;
import com.huawei.hmf.tasks.OnCompleteListener;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.hms.aaid.entity.AAIDResult;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.push.HmsMessaging;
import com.huawei.hms.push.RemoteMessage;

public class PushDemo extends BaseActivity implements View.OnClickListener {

    private TextView tvSetPush;

    private TextView tvSetAAID;

    private String aaId;

    // appid
    private final static String appid = "101120567";

    private String token;

    private final static int GET_AAID = 1;

    private final static int DELETE_AAID = 2;

    public static String PUSH_ACTION = "push_tag";

    private MyReceiver receiver;

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case GET_AAID:
                    tvSetAAID.setText(R.string.get_aaid);
                    break;
                case DELETE_AAID:
                    tvSetAAID.setText(R.string.delete_aaid);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_demo);
        tvSetPush = findViewById(R.id.btn_set_push);
        tvSetAAID = findViewById(R.id.btn_get_aaid);

        tvSetPush.setOnClickListener(this);
        tvSetAAID.setOnClickListener(this);
        findViewById(R.id.btn_add_topic).setOnClickListener(this);
        findViewById(R.id.btn_get_token).setOnClickListener(this);
        findViewById(R.id.btn_delete_token).setOnClickListener(this);
        findViewById(R.id.btn_delete_topic).setOnClickListener(this);
        findViewById(R.id.btn_send_msg).setOnClickListener(this);

        receiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(PUSH_ACTION);
        registerReceiver(receiver, filter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.title_activity_push_demo);
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
    public void onClick(View view) {
        int viewId = view.getId();
        switch (viewId) {
            case R.id.btn_get_aaid:
                setAAID(tvSetAAID.getText().toString().equals(getString(R.string.get_aaid)));
                break;
            case R.id.btn_get_token:
                getToken();
                break;
            case R.id.btn_delete_token:
                deleteToken();
                break;
            case R.id.btn_set_push:
                setReceiveNotifyMsg(tvSetPush.getText().toString().equals(getString(R.string.set_push_enable)));
                break;
            case R.id.btn_add_topic:
                addTopic();
                break;
            case R.id.btn_delete_topic:
                deleteTopic();
                break;
            case R.id.btn_send_msg:
                sendMsg();
                break;
            default:
                break;
        }
    }

    /**
     * get AAID / delete AAID
     */
    private void setAAID(boolean isGet) {
        if (isGet) {
            Task<AAIDResult> idResult = HmsInstanceId.getInstance(this).getAAID();
            idResult.addOnSuccessListener(new OnSuccessListener<AAIDResult>() {
                @Override
                public void onSuccess(AAIDResult aaidResult) {
                    aaId = aaidResult.getId();
                    showLog("aaId:" + aaId);
                    handler.sendEmptyMessage(DELETE_AAID);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    showLog("getAAID failed");
                }
            });
        } else {
            new Thread() {
                @Override
                public void run() {
                    try {
                        showLog("deleteAAID begin");
                        HmsInstanceId.getInstance(PushDemo.this).deleteAAID();
                        showLog("deleteAAID and token success");
                        handler.sendEmptyMessage(GET_AAID);
                    } catch (Exception e) {
                        showLog("deleteAAID failed");
                    }

                }
            }.start();
        }
    }

    /**
     * get token
     */
    private void getToken() {
        showLog("get token: begin");
        new Thread() {
            @Override
            public void run() {
                try {
                    token = HmsInstanceId.getInstance(PushDemo.this).getToken(appid, "HCM");
                    showLog("token:" + token);
                } catch (Exception e) {
                    showLog("getToken failed" + "\n" + e);
                }
            }
        }.start();
    }

    /**
     * delete push token
     */
    private void deleteToken() {
        showLog("deleteToken:begin");
        new Thread() {
            @Override
            public void run() {
                try {
                    HmsInstanceId.getInstance(getBaseContext()).deleteToken(appid, "HCM");
                    showLog("deleteToken:success");
                } catch (ApiException e) {
                    e.printStackTrace();
                    showLog("deleteToken failed" + "\n" + e);
                }
            }
        }.start();

    }

    /**
     * Set up receive notification messages
     *
     * @param enable enabled or not
     */
    private void setReceiveNotifyMsg(final boolean enable) {
        showLog("enableReceiveNotifyMsg:begin");
        if (enable) {
            HmsMessaging.getInstance(this).turnOnPush().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(Task<Void> task) {
                    if (task.isSuccessful()) {
                        showLog("turnon Complete");
                        tvSetPush.setText(R.string.set_push_unable);
                    } else {
                        showLog("turnon failed: ret=" + task.getException().getMessage());
                    }
                }
            });
        } else {
            HmsMessaging.getInstance(this).turnOffPush().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(Task<Void> task) {
                    if (task.isSuccessful()) {
                        showLog("turnoff Complete");
                        tvSetPush.setText(R.string.set_push_enable);
                    } else {
                        showLog("turnoff failed: ret=" + task.getException().getMessage());
                    }
                }
            });
        }

    }

    /**
     * subscribe
     */
    private void addTopic() {
        final TopicDialog topicDialog = new TopicDialog(this, true);
        topicDialog.setOnDialogClickListener(new OnDialogClickListener() {
            @Override
            public void onConfirmClick(String msg) {
                topicDialog.dismiss();
                try {
                    HmsMessaging.getInstance(PushDemo.this)
                            .subscribe(msg)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        showLog("subscribe Complete");
                                    } else {
                                        showLog("subscribe failed: ret=" + task.getException().getMessage());
                                    }
                                }
                            });
                } catch (Exception e) {
                    showLog("subscribe failed: exception=" + e.getMessage());
                }
            }

            @Override
            public void onCancelClick() {
                topicDialog.dismiss();
            }
        });
        topicDialog.show();

    }

    /**
     * unsubscribe
     */
    private void deleteTopic() {
        final TopicDialog topicDialog = new TopicDialog(this, false);
        topicDialog.setOnDialogClickListener(new OnDialogClickListener() {
            @Override
            public void onConfirmClick(String msg) {
                topicDialog.dismiss();
                try {
                    HmsMessaging.getInstance(PushDemo.this)
                            .unsubscribe(msg)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        showLog("unsubscribe Complete");
                                    } else {
                                        showLog("unsubscribe failed: ret=" + task.getException().getMessage());
                                    }
                                }
                            });
                } catch (Exception e) {
                    showLog("unsubscribe failed: exception=" + e.getMessage());
                }
            }

            @Override
            public void onCancelClick() {
                topicDialog.dismiss();
            }
        });
        topicDialog.show();
    }

    /**
     * send upstream
     */
    private void sendMsg() {
        String messageId = String.valueOf(System.currentTimeMillis());
        RemoteMessage remoteMessage = new RemoteMessage.Builder("push@hcm.hicloud.cn").setMessageId(messageId)
                .addData("key1", "data1")
                .addData("key2", "data2")
                .build();
        try {
            HmsMessaging.getInstance(this).send(remoteMessage);
            showLog("startSend");
        } catch (Exception e) {
            showLog("sendfailed:" + e.getMessage());
        }
    }

    /**
     * MyReceiver
     */
    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null && bundle.getString("msg") != null) {
                if ("onNewToken".equals(bundle.getString("method"))) {
                    token = bundle.getString("msg");
                }
                showLog(bundle.getString("method") + ":" + bundle.getString("msg"));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}
