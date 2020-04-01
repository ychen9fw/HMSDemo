package com.ychen9.demo.payment;


import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.ychen9.demo.BaseActivity;
import com.ychen9.demo.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

public class GMSPayment extends BaseActivity implements PurchasesUpdatedListener {

    private BillingClient billingClient;
    private SkuDetails skuDe;
    private TextView priceView;
    private Button payBtn;
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
            getSupportActionBar().setTitle("Pay Demo");
        }
        payBtn = findViewById(R.id.payRemoveAdsBtn);
        payBtn.setEnabled(false);
        init();
        payBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                pay();
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

    public void init(){

        priceView = (TextView) findViewById(R.id.price);
        billingClient = BillingClient.newBuilder(this).setListener(this).build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(int responseCode) {
                if (responseCode == BillingClient.BillingResponse.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    showLog("Billing Client Connected");
                    querySkuDetails();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });
    }

    public void querySkuDetails(){
        List<String> skuList = new ArrayList<>();
        skuList.add("removeads");
        skuList.add("add2level");
        //skuList.add("android.test.purchased");//test purchase
        showLog("Query product details");
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        billingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
                        if (responseCode == BillingClient.BillingResponse.OK && skuDetailsList != null) {
                            showLog("getSkuDetail success " + responseCode + " skuDetail" + skuDetailsList.toString());
                            showLog(String.valueOf(skuDetailsList.size()));
                            for (SkuDetails skuDetails : skuDetailsList) {
                                String sku = skuDetails.getSku();
                                String price = skuDetails.getPrice();
                                showLog(sku);
                                if ("removeads".equals(sku)) {
                                    skuDe = skuDetails;
                                    priceView.setText(price);
                                    payBtn.setEnabled(true);
                                }
                               /* if ("android.test.purchased".equals(sku)) {
                                    skuDe = skuDetails;
                                    priceView.setText(price);
                                    payBtn.setEnabled(true);
                                }*/
                            }
                        }
                    }
                });
    }


    public void pay(){
        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDe)
                .build();
        int responseCode = billingClient.launchBillingFlow(this, flowParams);
    }

    @Override
    public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {
        if (responseCode == BillingClient.BillingResponse.OK
                && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            showLog("user canceled payment");
        } else {
            // Handle any other error codes.
        }
    }

    private void handlePurchase(Purchase purchase) {

        showLog("Got a verified purchase: " + purchase);
    }
}
