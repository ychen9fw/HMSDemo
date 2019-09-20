package com.example.hmsdemo;

public class Constant {

    public static final int IS_LOG = 1;
    //login
    public static final int REQUEST_SIGN_IN_LOGIN = 1002;
    //Not logged in to pull up Huawei account login page
    public static final int REQ_CODE_NOT_LOGIN = 1003;
    //Pull up the Payment Cashier
    public static final int REQ_CODE_GO_PAY = 1004;
    //Social Request
    public static final int REQUEST_SEND_SNS_MSG = 1005;
    //Start Huawei Message Service Interface
    public static final int REQUEST_GET_UI_INTENT = 1006;
    //User Authorization
    public static final int REQUEST_SIGN_IN_AUTH = 1007;
    //Pull up the Achievement List page
    public static final int REQUEST_GAME_ACHIEVEMENTINTENT = 1008;
    //Request Location Permission
    public static final int REQUEST_LOCATION_PERMISSION = 1009;
    // priceType  0 : consumable goods 1 : non-consumable goods 2 : automatic renewal subscriptions 3  : Reserved value for non-renewable subscriptions
    public static final int PRODUCT_TYPE_CONSUMABLE = 0;
    public static final int PRODUCT_TYPE_NON_CONSUMABLE = 1;
    public static final int PRODUCT_TYPE_AUTOMATIC_RENEWAL = 2;
    public static final int PRODUCT_TYPE_RESERVED_NON_RENEWWABLE = 3;

    public static final String PUSH_TAG = "Push Message";
}
