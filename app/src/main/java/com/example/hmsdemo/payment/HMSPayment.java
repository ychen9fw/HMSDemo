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
import com.example.hmsdemo.R;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.iap.Iap;
import com.huawei.hms.iap.IapApiException;
import com.huawei.hms.iap.IapClient;
import com.huawei.hms.iap.entity.IsEnvReadyResult;
import com.huawei.hms.iap.entity.OrderStatusCode;
import com.huawei.hms.iap.entity.ProductInfo;
import com.huawei.hms.iap.entity.ProductInfoReq;
import com.huawei.hms.iap.entity.ProductInfoResult;
import com.huawei.hms.iap.entity.PurchaseIntentReq;
import com.huawei.hms.iap.entity.PurchaseIntentResult;
import com.huawei.hms.iap.entity.PurchaseResultInfo;
import com.huawei.hms.support.api.client.Status;


import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.Toolbar;

public class HMSPayment extends BaseActivity {

    private TextView priceView;
    private Button payBtn;
    private TextView subPriceView;
    private Button subButton;
    private String TAG = "hms PAY";
    private IapClient iapClient;
    private ProductInfo productDe;
    private ProductInfo subProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hmspayment);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("HMS Payment");
        }

        iapClient = Iap.getIapClient(this);
        payBtn = findViewById(R.id.payRemoveAdsBtn);
        priceView = findViewById(R.id.price);
        payBtn.setEnabled(false);
        subButton = findViewById(R.id.btn_subscribe);
        subPriceView = findViewById(R.id.subscribe_price);
        subButton.setEnabled(false);
        init();
        payBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                getBuyIntent(productDe, 0);
            }
        }); //priceType: 0: consumable; 1: non-consumable; 2: auto-renewable subscription
        subButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                getBuyIntent(subProduct, 2);
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
        Task<IsEnvReadyResult> task = iapClient.isEnvReady();
        task.addOnSuccessListener(new OnSuccessListener<IsEnvReadyResult>() {
            @Override
            public void onSuccess(IsEnvReadyResult  result) {
                if (result != null) {
                    showLog("isEnvReadyResult success: " + result.getReturnCode());
                    getSkuDetail();
                    getSubscribeDetail();
                    Log.i(TAG, "isBillingSupported success: " + result.getReturnCode());
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
        List<String> productIdList = new ArrayList<>();
        // The product ID is the same as that set by a developer when configuring product information in AppGallery Connect.
        productIdList.add("removeads");
        productIdList.add("testConsumable01");
        ProductInfoReq req = new ProductInfoReq();

        // priceType: 0: consumable; 1: non-consumable; 2: auto-renewable subscription
        req.setPriceType(0);
        req.setProductIds(productIdList);

        Task<ProductInfoResult> task = iapClient.obtainProductInfo(req);
        //Task<SkuDetailResult> task = Iap.getIapClient(this.getApplicationContext()).getSkuDetail(skuDetailReq);
        task.addOnSuccessListener(new OnSuccessListener<ProductInfoResult>() {
            @Override
            public void onSuccess(ProductInfoResult result) {
                //get result
                if (result != null) {
                    List<ProductInfo> productList  =  result.getProductInfoList();
                    for (ProductInfo product : productList) {
                        String sku = product.getProductId();
                        String price = product.getPrice();
                        showLog(sku);
                        if ("testConsumable01".equals(sku)) {
                            productDe = product;
                            priceView.setText(price);
                            payBtn.setEnabled(true);
                        }
                        Log.i(TAG, "get product detail success " + result.getReturnCode() + " product is: " + product.toString());
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

    private void getSubscribeDetail() {
        showLog("Query subscription product details");
        List<String> productIdList = new ArrayList<>();
        // The product ID is the same as that set by a developer when configuring product information in AppGallery Connect.
        productIdList.add("subscription01");
        ProductInfoReq req = new ProductInfoReq();

        // priceType: 0: consumable; 1: non-consumable; 2: auto-renewable subscription
        req.setPriceType(2);
        req.setProductIds(productIdList);

        Task<ProductInfoResult> task = iapClient.obtainProductInfo(req);
        //Task<SkuDetailResult> task = Iap.getIapClient(this.getApplicationContext()).getSkuDetail(skuDetailReq);
        task.addOnSuccessListener(new OnSuccessListener<ProductInfoResult>() {
            @Override
            public void onSuccess(ProductInfoResult result) {
                //get result
                if (result != null) {
                    List<ProductInfo> productList  =  result.getProductInfoList();
                    for (ProductInfo product : productList) {
                        String sku = product.getProductId();
                        String price = product.getPrice();
                        showLog(sku);
                        if ("subscription01".equals(sku)) {
                            subProduct = product;
                            subPriceView.setText(price);
                            subButton.setEnabled(true);
                        }
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



    private void getBuyIntent(ProductInfo product, int priceType) {
        showLog("Buy product");
        PurchaseIntentReq req  = new PurchaseIntentReq();
        req.setPriceType(priceType);
        req.setProductId(product.getProductId());
        req.setDeveloperPayload("test");
        Task<PurchaseIntentResult> task = iapClient.createPurchaseIntent(req);
        task.addOnSuccessListener(new OnSuccessListener<PurchaseIntentResult>() {
            @Override
            public void onSuccess(PurchaseIntentResult result) {
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
            if (data != null) {
                // Obtain the execution result.
                int returnCode = data.getIntExtra("returnCode", 1);
                showLog("isBillingSupported: " + returnCode);
                Log.i(TAG, "isBillingSupported: " + returnCode);
            }

        } else if (requestCode == Constant.REQ_CODE_GO_PAY) {
            //支付结果
            if (data == null) {
                showLog("onActivityResult data is null");
                return;
            }
            PurchaseResultInfo purchaseResultInfo  = iapClient.parsePurchaseResultInfoFromIntent(data);
            showLog("pay result: " + purchaseResultInfo.getReturnCode());
            Log.i(TAG, "pay result: " + purchaseResultInfo.getReturnCode());
            switch(purchaseResultInfo.getReturnCode()) {
                case OrderStatusCode.ORDER_STATE_CANCEL:
                    // User cancel payment.
                    showLog("User canceled payment");
                    break;
                case OrderStatusCode.ORDER_STATE_FAILED:
                    showLog("Order state failed");
                    break;
                case OrderStatusCode.ORDER_PRODUCT_OWNED:
                    showLog("Product is already owned");
                    break;
                case OrderStatusCode.ORDER_STATE_SUCCESS:
                    // pay success.
                    showLog("Payment Successed!");
                    String inAppPurchaseData = purchaseResultInfo.getInAppPurchaseData();
                    String inAppPurchaseDataSignature = purchaseResultInfo.getInAppDataSignature();
                    // use the public key of your app to verify the signature.
                    // If ok, you can deliver your products.
                    // If the user purchased a consumable product, call the consumeOwnedPurchase API to consume it after successfully delivering the product.
                    break;
                default:
                    break;
            }
            return;
        }

    }
}
