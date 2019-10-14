package com.example.hmsdemo;

import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;

import com.example.hmsdemo.model.PriceType;
import com.example.hmsdemo.model.Sku;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.support.api.client.Status;
import com.huawei.hms.support.api.entity.iap.ConsumePurchaseReq;
import com.huawei.hms.support.api.entity.iap.GetBuyIntentReq;
import com.huawei.hms.support.api.entity.iap.GetBuyIntentWithPriceReq;
import com.huawei.hms.support.api.entity.iap.GetPurchaseReq;
import com.huawei.hms.support.api.entity.iap.OrderStatusCode;
import com.huawei.hms.support.api.entity.iap.SkuDetail;
import com.huawei.hms.support.api.entity.iap.SkuDetailReq;
import com.huawei.hms.support.api.iap.BuyResultInfo;
import com.huawei.hms.support.api.iap.ConsumePurchaseResult;
import com.huawei.hms.support.api.iap.GetBuyIntentResult;
import com.huawei.hms.support.api.iap.GetPurchasesResult;
import com.huawei.hms.support.api.iap.HuaweiIap;
import com.huawei.hms.support.api.iap.IsBillingSupportedResult;
import com.huawei.hms.support.api.iap.SkuDetailResult;
import com.huawei.hms.support.api.iap.json.Iap;
import com.huawei.hms.support.api.iap.json.IapApiException;
import com.huawei.hms.support.api.iap.json.IapClient;


import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.net.Uri;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.appcompat.widget.Toolbar;

public class PayDemo extends BaseActivity implements View.OnClickListener{

    private String TAG = "HMS Pay Demo";
    //appid
    private final static String mAppid = "101120567";
    private String mContinuationToken = "";
    private String mPurchaseToken = "";
    // commodity id
    private String mSkuId = "ballsub302";
    private ArrayList<String> mIitemlist;
    private Spinner spinnerSKUDetail;
    private Spinner spinnerBuyIntent;
    private Spinner spinnerGetPurchaseGoods;
    private Spinner spinnerGetPurchaseHistory;
    private ArrayList<Sku> skuList;
    private ArrayList<PriceType> priceTypeList;
    private EditText priceInput;
    private IapClient iapClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_demo);

        findViewById(R.id.hwpay_isBillingSupported).setOnClickListener(this);
        findViewById(R.id.hwpay_getPurchases).setOnClickListener(this);
        findViewById(R.id.hwpay_getSkuDetail).setOnClickListener(this);
        findViewById(R.id.hwpay_getBuyIntentWithPrice).setOnClickListener(this);
        findViewById(R.id.hwpay_getBuyIntent).setOnClickListener(this);
        findViewById(R.id.hwpay_getPurchaseHistory).setOnClickListener(this);
        findViewById(R.id.hwpay_consumePurchase).setOnClickListener(this);
        findViewById(R.id.hwpay_jupmsubscriptions).setOnClickListener(this);
        findViewById(R.id.hwpay_jupmsubscriptionsdetail).setOnClickListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.title_activity_pay_demo);
        }
        priceInput = (EditText) findViewById(R.id.purchaseAmount);
        showLog("Starting Pay Demo");
        initProductDropDownList();
        initProductTypeDropDownList();
        iapClient = Iap.getIapClient(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }
    //this is test Product
    private void createProduct(){
        skuList = new ArrayList<>();
        Sku sku0 = new Sku("Choose product", "", 0, 0);
        Sku sku1 = new Sku("consumable test product 001", "comsumable_testproduct_001", Constant.PRODUCT_TYPE_CONSUMABLE, 3.01);
        Sku sku2 = new Sku("non consumable test product 001", "nonComsumable_testproduct_001", Constant.PRODUCT_TYPE_NON_CONSUMABLE, 3.01);
        skuList.add(sku0);
        skuList.add(sku1);
        skuList.add(sku2);
    }

    //create Product Type
    private void createType(){
        priceTypeList = new ArrayList<>();
        PriceType type0 = new PriceType("Choose Commodity Type", 0);
        PriceType consumable = new PriceType("Consumable Goods", Constant.PRODUCT_TYPE_CONSUMABLE);
        PriceType nonConsumable = new PriceType("Non-Consumable Goods", Constant.PRODUCT_TYPE_NON_CONSUMABLE);
        PriceType subscription = new PriceType("Subscription Goods", Constant.PRODUCT_TYPE_AUTOMATIC_RENEWAL);
        priceTypeList.add(type0);
        priceTypeList.add(consumable);
        priceTypeList.add(nonConsumable);
        priceTypeList.add(subscription);
    }

    private void initProductDropDownList(){
        //Initiate product list to get product details.
        createProduct();
        spinnerSKUDetail = (Spinner) findViewById(R.id.spinner_getSkuDetail);
        ArrayAdapter skuAdapter = new ArrayAdapter<Sku>(this, android.R.layout.simple_spinner_item, skuList);
        skuAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSKUDetail.setAdapter(skuAdapter);
        spinnerSKUDetail.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    Sku sku = skuList.get(position);
                    ArrayList<String> products1 = new ArrayList<>();
                    products1.add(sku.getProductId());
                    //getSkuDetail accepts a list.
                    getSkuDetail(sku.getPriceType(), products1);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // initiate product list for buy intent
        spinnerBuyIntent = (Spinner) findViewById(R.id.spinner_getBuyIntent);
        ArrayAdapter buyAdapter= new ArrayAdapter<Sku>(this, android.R.layout.simple_spinner_item, skuList);
        buyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBuyIntent.setAdapter(buyAdapter);
        spinnerBuyIntent.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    Sku sku = skuList.get(position);
                    //call getBuyIntent to buy the product
                    getBuyIntent(sku.getPriceType(),sku.getProductId());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    private void initProductTypeDropDownList(){
        //initiate Price Type drop downlist to get Purchases goods
        createType();
        spinnerGetPurchaseGoods = (Spinner) findViewById(R.id.spinner_getPurchaseGoods);
        ArrayAdapter getPurchaseAdapter= new ArrayAdapter<PriceType>(this, android.R.layout.simple_spinner_item, priceTypeList);
        getPurchaseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGetPurchaseGoods.setAdapter(getPurchaseAdapter);

        spinnerGetPurchaseGoods.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    PriceType type = priceTypeList.get(position);
                    //call getBuyIntent to buy the product
                    getPurchases(type.getID());

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //initiate Price Type drop downlist to get Purchases history

        spinnerGetPurchaseHistory = (Spinner) findViewById(R.id.spinner_getPurchaseHistory);
        ArrayAdapter purchaseHistoryAdapter= new ArrayAdapter<PriceType>(this, android.R.layout.simple_spinner_item, priceTypeList);
        purchaseHistoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGetPurchaseHistory.setAdapter(purchaseHistoryAdapter);

        spinnerGetPurchaseHistory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    PriceType type = priceTypeList.get(position);
                    //call getBuyIntent to buy the product
                    getPurchaseHistory(type.getID());

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    /**
     * isBillingSupported by isBillingSupported
     * <p>
     * Determine whether the current version of Huawei Mobile Services is in the version supporting payment services,
     * and support querying whether the service country where the Huawei account number currently logged in is
     * in the country or region supporting settlement of Huawei IAP Payment Services.
     */
    private void isBillingSupported() {
        showLog("check if billing is supported for current device and country ");

        Task<IsBillingSupportedResult> task = Iap.getIapClient(PayDemo.this).isBillingSupported();
        task.addOnSuccessListener(new OnSuccessListener<IsBillingSupportedResult>() {
            @Override
            public void onSuccess(IsBillingSupportedResult isBillingSupportedResult) {
                if (isBillingSupportedResult != null) {
                    showLog("isBillingSupported success: " + isBillingSupportedResult.getReturnCode());
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
                                status.startResolutionForResult(PayDemo.this, Constant.REQ_CODE_NOT_LOGIN);
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

    /**
     * This API is used to query information about all subscribed in-app products, including consumables, non-consumables, and auto-renewable subscriptions.
     * ● If consumables are returned, the in-app products might not be delivered due to some exceptions. In this case, your app needs to check whether the in-app products are delivered. If the in-app products are not delivered, the system needs to deliver them and calls the 1.4.1.6 consumePurchase API to consume the products.
     * ● If non-consumables are returned, the in-app products do not need to be consumed.
     * ● If subscriptions are returned, all existing subscription relationships of the user under the app are returned. The subscription relationships are as follows:
     * - Renewal (normal use and normal renewal for the next period)
     * - Expiring (expiration instead of renewal when the next period starts)
     * - Expired (The subscribed service is unavailable but can still be found in the subscription history.)
     * Commodities include consumable goods, non-consumable goods and automatic renewal subscriptions
     */
    private void getPurchases(int priceType) {
        showLog("Query all ordered goods information of user");
        GetPurchaseReq getPurchaseReq = new GetPurchaseReq();
        //priceType 0 : consumable goods 1 : non-consumable goods 2 : automatic renewal subscriptions 3  : Reserved value for non-renewable subscriptions
        getPurchaseReq.priceType = priceType;
        //continuationToken  Data Location Markers Supporting Paging Queries。
        //This parameter may not be passed in the first query, and it will be included in the return information after calling the interface.
        //If paging query is needed in the next invocation of the interface, it can be entered in the second invocation.
        if (!mContinuationToken.isEmpty()) {
            getPurchaseReq.continuationToken = mContinuationToken;
        }
        Task<GetPurchasesResult> task = Iap.getIapClient(PayDemo.this).getPurchases(getPurchaseReq);
        task.addOnSuccessListener(new OnSuccessListener<GetPurchasesResult>() {
            @Override
            public void onSuccess(GetPurchasesResult result) {
                //Get query results
                if (result != null) {
                    mContinuationToken = result.getContinuationToken();
                    mIitemlist = result.getItemList();
                    showLog("getPurchases onSuccess:" + result.getItemList().toString());
                    Log.i(TAG, "getPurchases onSuccess:" + result.getItemList().toString());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof IapApiException) {
                    Status status = ((IapApiException) e).getStatus();
                    int statuscode = status.getStatusCode();
                    showLog("getPurchases fail code:" + statuscode);
                    Log.i(TAG, "getPurchases fail code:" + statuscode);
                    Log.i(TAG, "getPurchases fail: " + e.getMessage());
                }
            }
        });
    }

    /**
     * This API is used to obtain in-app product details configured in AppGallery Connect. If you use Huawei's PMS to price in-app products, you can use this API to obtain in-app product details from the PMS to ensure that the in-app product information in your app is the same as that displayed on the checkout page of HUAWEI IAP.
     * Avoid obtaining in-app product information from your own server to prevent inconsistency in price information between your app and the checkout page.
     * priceType  0 : consumable goods 1 : non-consumable goods 2 : automatic renewal subscriptions 3  : Reserved value for non-renewable subscriptions
     * skuIds List of commodity ID to be queried
     */
    private void getSkuDetail(int priceType, ArrayList<String> skus) {
        showLog("Query product details");
        SkuDetailReq skuDetailReq = new SkuDetailReq();
        //skuDetailReq.priceType = priceType;
        skuDetailReq.priceType = 0;
        mIitemlist = new ArrayList<>();
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
                    List<SkuDetail> skuDetail = result.getSkuList();
                    showLog("getSkuDetail success " + result.getReturnCode() + " skuDetail" + skuDetail.toString());
                    Log.i(TAG, "getSkuDetail success " + result.getReturnCode() + " skuDetail" + skuDetail.toString());
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

    /**
     * This interface can be invoked to set the commodity price and complete the payment, instead of obtaining the price from PMS
     * productId : Apply a custom commodity ID, which is used to uniquely identify a commodity and cannot be repeated.
     * priceType  Commodity types
     * 0 : consumable goods
     * 1 : non-consumable goods
     * 2 : automatic renewal subscriptions
     * 3  : Reserved value for non-renewable subscriptions
     * productName Commodity name, customized by application
     * amount      the amount of goods, the amount of goods to be paid. This amount will be shown to the user for confirmation at the time of payment.。
     * country     Optional parameter country codes. Recommendation no special needs, no transmission
     * currency    Optional parameter currency selection. It is recommended not to pass this parameter without special need. Currently only CNY is supported, default CNY
     * sdkChannel  Channel information
     * 0 represents own application, no channel
     * 1 represents application market channel
     * 2 represents pre-installed channel
     * 3 represents game center 4 represents Sports Health Channel
     * serviceCatalog  Types of products to which commodities belong
     * X4: Theme; X5: Application Store; X6: Game; X7: Skylink; X8: Cloud Space; X9: E-book; X10: Huawei Learning; X11: Music; X12 Video; V0: vmall Entity Goods; X31: Telephone Charge; X32: Airfare / Hotel; X33: Film Ticket; X34: Group Purchase; X35: Mobile Pre; X36: Public Payment; X37: Fund Finance; X38: Lottery; X39: Lottery Flow recharge
     * developerPayload Business side retains information
     * If the field has a value, it will be returned to the application as it is in the callback result after successful payment.
     */
    private void getBuyIntentWithPrice() {
        showLog("Buy product with Price");
        GetBuyIntentWithPriceReq getBuyIntentWithPriceReq = new GetBuyIntentWithPriceReq();
        getBuyIntentWithPriceReq.productId = String.valueOf(System.currentTimeMillis());
        getBuyIntentWithPriceReq.priceType = Constant.PRODUCT_TYPE_CONSUMABLE;
        getBuyIntentWithPriceReq.productName = "Test Product";
        getBuyIntentWithPriceReq.amount = priceInput.getText().toString();
        getBuyIntentWithPriceReq.sdkChannel = "1";
        getBuyIntentWithPriceReq.serviceCatalog = "X38";
        getBuyIntentWithPriceReq.developerPayload = "testpay";

        Task<GetBuyIntentResult> task = Iap.getIapClient(PayDemo.this).getBuyIntentWithPrice(getBuyIntentWithPriceReq);
        task.addOnSuccessListener(new OnSuccessListener<GetBuyIntentResult>() {
            @Override
            public void onSuccess(GetBuyIntentResult result) {
                if (result != null) {
                    //Get execution results
                    Status status = result.getStatus();
                    showLog("getBuyIntentWithPrice" + status.getStatusCode() + status.getStatusMessage() + status.getErrorString());
                    Log.i(TAG, "getBuyIntentWithPrice" + status.getStatusCode() + status.getStatusMessage() + status.getErrorString());
                    try {
                        //Pull up the Payment Cashier
                        status.startResolutionForResult(PayDemo.this, Constant.REQ_CODE_GO_PAY);
                    } catch (SendIntentException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof IapApiException) {
                    IapApiException apiException = (IapApiException) e;
                    Status status = apiException.getStatus();
                    if (status.getStatusCode() == OrderStatusCode.ORDER_HWID_NOT_LOGIN) {
                        // Unlogged Scenario
                        if (status.hasResolution()) {
                            try {
                                //Pull up Huawei account login page
                                status.startResolutionForResult(PayDemo.this, Constant.REQ_CODE_NOT_LOGIN);
                            } catch (IntentSender.SendIntentException exp) {
                            }
                        }
                    } else if (status.getStatusCode() == OrderStatusCode.ORDER_ITEM_ALREADY_OWNED) {
                        showLog("order item already owned");
                        Log.i(TAG, "order item already owned");
                    } else {
                        showLog("getBuyIntentWithPrice statuscode：" + status.getStatusCode());
                        Log.i(TAG, "getBuyIntentWithPrice statuscode：" + status.getStatusCode());
                    }
                }
            }
        });
    }

    /**
     * This API is used to obtain the historical consumption information about a consumable in-app product or all subscription receipts of a subscription.
     * -For consumable in-app products, this API returns information about in-app products that have been delivered or consumed in the in-app product list.
     * -For non-consumable in-app products, this API does not return in-app product information.
     * -For subscriptions, this API returns all subscription receipts of the current user in this app.
     * continuationToken  Data Location Markers Supporting Paging Queries。
     * This parameter may not be passed in the first query, and it will be included in the return information after calling the interface.
     * If paging query is needed in the next invocation of the interface, it can be entered in the second invocation.
     */
    private void getPurchaseHistory(int priceType) {
        showLog("Get Purchase History");
        GetPurchaseReq getPurchaseReq = new GetPurchaseReq();
        getPurchaseReq.priceType = priceType;
        if (!mContinuationToken.isEmpty()) {
            getPurchaseReq.continuationToken = mContinuationToken;
        }
        Task<GetPurchasesResult> task = Iap.getIapClient(PayDemo.this).getPurchaseHistory(getPurchaseReq);
        task.addOnSuccessListener(new OnSuccessListener<GetPurchasesResult>() {
            @Override
            public void onSuccess(GetPurchasesResult result) {
                //Get execution results
                if (result != null) {
                    showLog("getPurchaseHistory success " + result.getReturnCode());
                    for (String product: result.getInAppPurchaseDataList()){
                        showLog(product);
                    }
                    Log.i(TAG, "getPurchaseHistory success " + result.getReturnCode());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof IapApiException) {
                    IapApiException iapApiException = (IapApiException) e;
                    Status status = iapApiException.getStatus();
                    Log.i(TAG, "getPurchaseHistory fail" + status.getStatusCode());
                }
            }
        });
    }

    /**
     * This API is used to create orders for in-app products in the PMS. After creating an in-app product in AppGallery Connect, you can call this API to open the HUAWEI IAP checkout page and display the product, price, and payment method. This API supports in-app product purchase and subscription. Huawei can adjust in-app product prices as foreign exchange rates change. To ensure price consistency, the app needs to call the getSkuDetail API to obtain in-app product details from Huawei instead of your own server.
     * This API can return only the PendingIntent object of the checkout page. The app needs to open the checkout page. For details about how to open the checkout page, see the description of status in GetBuyIntentResult After the user completes payment, the app can obtain the payment result from the onActivityResult callback method.
     * priceType  Commodity types
     * 0 : consumable goods
     * 1 : non-consumable goods
     * 2 : automatic renewal subscriptions
     * 3  : Reserved value for non-renewable subscriptions
     * productId Goods ID to be paid
     * developerPayload  Business side retains information。
     * If the field has a value, it will be returned to the application as it is in the callback result after successful payment.
     */
    private void getBuyIntent(int priceType, String productId) {
        showLog("Buy product");
        GetBuyIntentReq getBuyIntentReq = new GetBuyIntentReq();
        //getBuyIntentReq.priceType = priceType;
        //getBuyIntentReq.productId = productId;
        getBuyIntentReq.priceType = 0;
        getBuyIntentReq.productId = "comsumable_testproduct_001";
        getBuyIntentReq.developerPayload = "tespmspay";
        Task<GetBuyIntentResult> task = Iap.getIapClient(PayDemo.this).getBuyIntent(getBuyIntentReq);
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
                        status.startResolutionForResult(PayDemo.this, Constant.REQ_CODE_GO_PAY);
                    } catch (SendIntentException e) {
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

    /**
     * For consumable goods, after successful payment, the application needs to call this interface to consume the consumable goods before it can distribute the goods to users
     * purchaseToken
     * The logo of the goods purchased by the user is generated by the payment server when the goods are paid and returned to the application in the inAppPurchase Data.
     * Apply this parameter to the payment server to update the order status before issuing the goods。
     * developerChallenge
     * The developer's custom challenge word uniquely identifies the consumption request. After successful consumption, the challenge word is recorded in the purchase information and returned
     */
    private void consumePurchase() {
        showLog("Consume Comsumeable Product");
        ConsumePurchaseReq consumePurchaseReq = new ConsumePurchaseReq();
        consumePurchaseReq.purchaseToken = mPurchaseToken;
        consumePurchaseReq.developerChallenge = "test consumePurchase";
        Task<ConsumePurchaseResult> task = Iap.getIapClient(PayDemo.this).consumePurchase(consumePurchaseReq);
        task.addOnSuccessListener(new OnSuccessListener<ConsumePurchaseResult>() {
            @Override
            public void onSuccess(ConsumePurchaseResult result) {
                //Get execution results
                if (result != null) {
                    Log.i(TAG, "consumePurchase success " + result.getReturnCode());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof IapApiException) {
                    IapApiException iapApiException = (IapApiException) e;
                    Status status = iapApiException.getStatus();
                    Log.i(TAG, "consumePurchase fail：" + status.getStatusCode());
                }
            }
        });
    }

    /**
     * Jump to the Management Subscription Page
     */
    private void jupmsubscriptions() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("pay://com.huawei.hwid.external/subscriptions?package=" + getPackageName() + "&appid=" + mAppid));
        startActivity(intent);
    }

    /**
     * Jump to the Subscription Details page
     */
    private void jupmsubscriptionsdetail() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("pay://com.huawei.hwid.external/subscriptions?package=" + getPackageName() + "&appid=" + mAppid + "&sku=" + mSkuId));
        startActivity(intent);
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
                    mPurchaseToken = jsonObject.getString("purchaseToken");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.hwpay_isBillingSupported:
                isBillingSupported();
                break;
            case R.id.hwpay_getPurchases:
                getPurchases(0);
                break;
            case R.id.hwpay_getSkuDetail:
                getSkuDetail(0, null);
                break;
            case R.id.hwpay_getBuyIntentWithPrice:
                if(priceInput.getText().toString().matches("")){
                    break;
                }
                getBuyIntentWithPrice();
                break;
            case R.id.hwpay_getPurchaseHistory:
                getPurchaseHistory(0);
                break;
            case R.id.hwpay_getBuyIntent:
                getBuyIntent(0, "");
                break;
            case R.id.hwpay_consumePurchase:
                consumePurchase();
                break;
            case R.id.hwpay_jupmsubscriptions:
                jupmsubscriptions();
                break;
            case R.id.hwpay_jupmsubscriptionsdetail:
                jupmsubscriptionsdetail();
                break;

            default:
                break;
        }
    }
}
