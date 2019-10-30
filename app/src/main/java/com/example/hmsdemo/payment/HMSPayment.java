package com.example.hmsdemo.payment;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.hmsdemo.BaseActivity;
import com.example.hmsdemo.Constant;
import com.example.hmsdemo.PayDemo;
import com.example.hmsdemo.R;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.support.api.client.Status;
import com.huawei.hms.support.api.entity.iap.GetBuyIntentReq;
import com.huawei.hms.support.api.entity.iap.OrderStatusCode;
import com.huawei.hms.support.api.entity.iap.SkuDetail;
import com.huawei.hms.support.api.entity.iap.SkuDetailReq;
import com.huawei.hms.support.api.iap.BuyResultInfo;
import com.huawei.hms.support.api.iap.GetBuyIntentResult;
import com.huawei.hms.support.api.iap.HuaweiIap;
import com.huawei.hms.support.api.iap.IsBillingSupportedResult;
import com.huawei.hms.support.api.iap.SkuDetailResult;
import com.huawei.hms.support.api.iap.json.Iap;
import com.huawei.hms.support.api.iap.json.IapApiException;
import com.huawei.hms.support.api.iap.json.IapClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.Toolbar;

public class HMSPayment extends BaseActivity {

    private TextView priceView;
    private Button payBtn;
    private String TAG = "hms PAY";
    private IapClient iapClient;
    private SkuDetail skuDe;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gmspayment);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.title_activity_analysis_demo);
        }

        iapClient = Iap.getIapClient(this);
        payBtn = findViewById(R.id.payRemoveAdsBtn);
        payBtn.setEnabled(false);
        init();
        payBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                getBuyIntent();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }


    public void init() {
        showLog("check if billing is supported for current device and country ");
        priceView = (TextView) findViewById(R.id.price);
        Task<IsBillingSupportedResult> task = iapClient.isBillingSupported();
        task.addOnSuccessListener(new OnSuccessListener<IsBillingSupportedResult>() {
            @Override
            public void onSuccess(IsBillingSupportedResult isBillingSupportedResult) {
                if (isBillingSupportedResult != null) {
                    showLog("isBillingSupported success: " + isBillingSupportedResult.getReturnCode());
                    getSkuDetail();
                    Log.i(TAG, "isBillingSupported success: " + isBillingSupportedResult.getReturnCode());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof IapApiException) {
                    IapApiException apiException = (IapApiException) e;
                    Status status = apiException.getStatus();
                    if (status.getStatusCode() == OrderStatusCode.ORDER_HWID_NOT_LOGIN) {
                        showLog("user not login");
                        Log.i(TAG, "not login");
                        if (status.hasResolution()) {
                            try {
                                //If there is no login, please login  startResolutionForResult
                                status.startResolutionForResult(HMSPayment.this, Constant.REQ_CODE_NOT_LOGIN);
                            } catch (IntentSender.SendIntentException exp) {
                            }
                        }
                    } else if (status.getStatusCode() == OrderStatusCode.ORDER_ACCOUNT_AREA_NOT_SUPPORTED) {
                        showLog("IAP is not currently supported at the service location");
                        Log.i(TAG, "IAP is not currently supported at the service location");
                    }
                }
            }
        });
    }


    private void getSkuDetail() {
        showLog("Query product details");
        SkuDetailReq skuDetailReq = new SkuDetailReq();
        //skuDetailReq.priceType = priceType;
        skuDetailReq.priceType = 0;
        ArrayList<String> mIitemlist = new ArrayList<>();
        mIitemlist.add("comsumable_testproduct_001");
        skuDetailReq.skuIds = mIitemlist;
        //skuDetailReq.skuIds = skus;
        Task<SkuDetailResult> task = iapClient.getSkuDetail(skuDetailReq);
        //Task<SkuDetailResult> task = Iap.getIapClient(this.getApplicationContext()).getSkuDetail(skuDetailReq);
        task.addOnSuccessListener(new OnSuccessListener<SkuDetailResult>() {
            @Override
            public void onSuccess(SkuDetailResult result) {
                //get result
                if (result != null) {
                    List<SkuDetail> skuDetailList = result.getSkuList();

                    showLog("getSkuDetail success " + result.getReturnCode() + " skuDetail" + skuDetailList.toString());
                    for (SkuDetail skuDetail : skuDetailList) {
                        String sku = skuDetail.productId;
                        String price = skuDetail.price;
                        showLog(sku);
                        if ("removeads".equals(sku)) {
                            skuDe = skuDetail;
                            priceView.setText(price);
                            payBtn.setEnabled(true);
                        }
                        Log.i(TAG, "getSkuDetail success " + result.getReturnCode() + " skuDetail" + skuDetail.toString());
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof IapApiException) {
                    IapApiException iapApiException = (IapApiException) e;
                    Status status = iapApiException.getStatus();
                    showLog("getSkuDetail fail：" + status.getStatusCode());
                    Log.i(TAG, "getSkuDetail fail：" + status.getStatusCode());
                    Log.i(TAG, "getSkuDetail fail：" + iapApiException.getStatusMessage());
                }
            }
        });
    }


    private void getBuyIntent() {
        showLog("Buy product");
        GetBuyIntentReq getBuyIntentReq = new GetBuyIntentReq();
        getBuyIntentReq.priceType = 0;
        getBuyIntentReq.productId = skuDe.productId;
        getBuyIntentReq.developerPayload = "tespmspay";
        Task<GetBuyIntentResult> task = iapClient.getBuyIntent(getBuyIntentReq);
        task.addOnSuccessListener(new OnSuccessListener<GetBuyIntentResult>() {
            @Override
            public void onSuccess(GetBuyIntentResult result) {
                //Obtain payment results
                if (result != null) {
                    Status status = result.getStatus();
                    showLog("getBuyIntent success" + status.getStatusCode() + " " + status.getStatusMessage());
                    Log.i(TAG, "getBuyIntent success" + status.getStatusCode() + " " + status.getStatusMessage());
                    try {
                        //Pull up the Payment Cashier
                        status.startResolutionForResult(HMSPayment.this, Constant.REQ_CODE_GO_PAY);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof IapApiException) {
                    IapApiException iapApiException = (IapApiException) e;
                    Status status = iapApiException.getStatus();
                    showLog("getBuyIntent fail：" + status.getStatusCode());
                    Log.i(TAG, "getBuyIntent fail：" + status.getStatusCode());
                }
            }
        });

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.REQ_CODE_NOT_LOGIN) {
            Bundle bundle = data.getExtras();
            showLog("isBillingSupported: " + bundle.getInt("returnCode") + "");
            Log.i(TAG, "isBillingSupported: " + bundle.getInt("returnCode") + "");
        } else if (requestCode == Constant.REQ_CODE_GO_PAY) {
            //支付结果
            BuyResultInfo buyResultInfo = HuaweiIap.HuaweiIapApi.getBuyResultInfoFromIntent(data);
            showLog("pay result: " + buyResultInfo.getReturnCode());
            Log.i(TAG, "pay result: " + buyResultInfo.getReturnCode());
            if (buyResultInfo.getReturnCode() == 0) {
                String InAppPurchaseData = buyResultInfo.getInAppPurchaseData();
                try {
                    JSONObject jsonObject = new JSONObject(InAppPurchaseData);
                    showLog("order ID " + jsonObject.getString("orderId"));
                    //mPurchaseToken = jsonObject.getString("purchaseToken");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
