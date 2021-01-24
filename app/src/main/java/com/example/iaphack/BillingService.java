package com.example.iaphack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.example.iaphack.google.util.IabHelper;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;
import org.json.JSONObject;

public class BillingService extends Service {

    public static final String TAG = "BillingHack";
    private DbHelper mdbHelper;
    private Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        mdbHelper = new DbHelper(mContext);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IInAppBillingService.Stub mBinder = new IInAppBillingService.Stub() {

        @Override
        public int isBillingSupported(int apiVersion, String packageName, String type)
                throws RemoteException {
            Log.d(TAG, "================= isBillingSupported =================");

            return IabHelper.BILLING_RESPONSE_RESULT_OK;
        }

        @Override
        public Bundle getSkuDetails(int apiVersion, String packageName, String type,
                                    Bundle skusBundle) throws RemoteException {
            Log.d(TAG, "================= getSkuDetails =================");
            Log.d(TAG, "apiVersion: " + apiVersion);
            Log.d(TAG, "packageName: " + packageName);
            Log.d(TAG, "type: " + type);
            Log.d(TAG, "skusBundle: " + skusBundle);

            // https://developer.android.com/google/play/billing/billing_reference#getSkuDetails
            // If getSkuDetails() method is successful, Google Play sends a response Bundle. The
            // query results are stored in the Bundle within a String ArrayList mapped to the
            // DETAILS_LIST key. Each String in the details list contains product details for a
            // single product in JSON format. The fields in the JSON string with the product details
            // are summarized in table 5.

            Bundle bundle = new Bundle();
            bundle.putInt(IabHelper.RESPONSE_CODE, IabHelper.BILLING_RESPONSE_RESULT_OK);

            ArrayList<String> productDetails = new ArrayList<String>();

//            if (type.equals(IabHelper.ITEM_TYPE_INAPP)) {
                ArrayList<String> items = skusBundle.getStringArrayList("ITEM_ID_LIST");
                int length = items.size();
                for(int i=0;i<length;i++){
                    System.out.println(i);
                    String itemId = items.get(i);

                    try {
                        JSONObject productInfo = new JSONObject();
                        productInfo.put("productId", itemId);
                        productInfo.put("type", type);
                        productInfo.put("price", "$1.00");
                        productInfo.put("price_currency_code", "USD");
                        productInfo.put("price_amount_micros", "1000000");
                        productInfo.put("title", itemId);
                        productInfo.put("description", "This is an example description");
                        productDetails.add(productInfo.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
//            }

            Log.d(TAG, productDetails.toString());

            bundle.putStringArrayList(IabHelper.RESPONSE_GET_SKU_DETAILS_LIST, productDetails);

            return bundle;
        }

        @Override
        public Bundle getBuyIntent(int apiVersion, String packageName, String sku, String type,
                                   String developerPayload) throws RemoteException {
            Log.d(TAG, "================= getBuyIntent =================");
            Log.d(TAG, "apiVersion: " + apiVersion);
            Log.d(TAG, "packageName: " + packageName);
            Log.d(TAG, "sku: " + sku);
            Log.d(TAG, "type: " + type);
            Log.d(TAG, "developerPayload: " + developerPayload);

            Bundle bundle = new Bundle();
            bundle.putInt(IabHelper.RESPONSE_CODE, IabHelper.BILLING_RESPONSE_RESULT_OK);

            PendingIntent pendingIntent;
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), BuyActivity.class);
            intent.setAction(BuyActivity.BUY_INTENT);
            intent.putExtra(BuyActivity.EXTRA_PACKAGENAME, packageName);
            intent.putExtra(BuyActivity.EXTRA_PRODUCT_ID, sku);
            intent.putExtra(BuyActivity.EXTRA_DEV_PAYLOAD, developerPayload);
            pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            bundle.putParcelable(IabHelper.RESPONSE_BUY_INTENT, pendingIntent);

            return bundle;
        }

        @Override
        public Bundle getPurchases(int apiVersion, String packageName, String type,
                                   String continuationToken) throws RemoteException {
            Log.d(TAG, "================= getPurchases =================");
            Log.d(TAG, "apiVersion: " + apiVersion);
            Log.d(TAG, "packageName: " + packageName);
            Log.d(TAG, "type: " + type);
            Log.d(TAG, "continuationToken: " + continuationToken);

            ArrayList<String> itemList = new ArrayList();
            ArrayList<String> dataList = new ArrayList();
            ArrayList<String> sigList = new ArrayList();
//            if (type.equals(IabHelper.ITEM_TYPE_INAPP)) {
                try {
                    ArrayList<JSONObject> purchaseHistory = mdbHelper.getPurchaseHistory(packageName);
                    if (purchaseHistory.size() > 0) {
                        for (JSONObject record : purchaseHistory) {
                            itemList.add(record.getString("productId"));
                            dataList.add(record.toString());
                            sigList.add("fake_signature");
                        }
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "error: ", ex);
                }
//            }

            Bundle bundle = new Bundle();
            bundle.putInt(IabHelper.RESPONSE_CODE, IabHelper.BILLING_RESPONSE_RESULT_OK);
            bundle.putStringArrayList(IabHelper.RESPONSE_INAPP_ITEM_LIST, itemList);
            bundle.putStringArrayList(IabHelper.RESPONSE_INAPP_PURCHASE_DATA_LIST, dataList);
            bundle.putStringArrayList(IabHelper.RESPONSE_INAPP_SIGNATURE_LIST, sigList);

            Log.d(TAG, "bundle: " + bundle);
            return bundle;
        }

        @Override
        public int consumePurchase(int apiVersion, String packageName, String purchaseToken)
                throws RemoteException {
            Log.d(TAG, "================= consumePurchase =================");

            return IabHelper.BILLING_RESPONSE_RESULT_OK;
        }

        @Override
        public int stub(int apiVersion, String packageName, String type) throws RemoteException {
            Log.d(TAG, "================= stub =================");
            return 0;
        }

        @Override
        public Bundle getBuyIntentToReplaceSkus(int apiVersion, String packageName, List<String> oldSkus, String newSku, String type, String developerPayload) throws RemoteException {
            Log.d(TAG, "================= getBuyIntentToReplaceSkus =================");
            return null;
        }

        @Override
        public Bundle getBuyIntentExtraParams(int apiVersion, String packageName, String sku, String type, String developerPayload, Bundle extraParams) throws RemoteException {
            Log.d(TAG, "================= getBuyIntentExtraParams =================");
            Log.d(TAG, "apiVersion: " + apiVersion);
            Log.d(TAG, "packageName: " + packageName);
            Log.d(TAG, "sku: " + sku);
            Log.d(TAG, "type: " + type);
            Log.d(TAG, "developerPayload: " + developerPayload);
            Log.d(TAG, "extraParams: " + extraParams);

            Bundle bundle = new Bundle();
            bundle.putInt(IabHelper.RESPONSE_CODE, IabHelper.BILLING_RESPONSE_RESULT_OK);

            PendingIntent pendingIntent;
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), BuyActivity.class);
            intent.setAction(BuyActivity.BUY_INTENT);
            intent.putExtra(BuyActivity.EXTRA_PACKAGENAME, packageName);
            intent.putExtra(BuyActivity.EXTRA_PRODUCT_ID, sku);
            intent.putExtra(BuyActivity.EXTRA_DEV_PAYLOAD, developerPayload);
            pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            bundle.putParcelable(IabHelper.RESPONSE_BUY_INTENT, pendingIntent);

            return bundle;
        }

        @Override
        public Bundle getPurchaseHistory(int apiVersion, String packageName, String type, String continuationToken, Bundle extraParams) throws RemoteException {
            Log.d(TAG, "================= getPurchaseHistory =================");
            Log.d(TAG, "apiVersion: " + apiVersion);
            Log.d(TAG, "packageName: " + packageName);
            Log.d(TAG, "type: " + type);
            Log.d(TAG, "continuationToken: " + continuationToken);

            Bundle bundle = new Bundle();
            bundle.putInt(IabHelper.RESPONSE_CODE, IabHelper.BILLING_RESPONSE_RESULT_OK);
            bundle.putStringArrayList(IabHelper.RESPONSE_INAPP_ITEM_LIST, new ArrayList<String>());
            bundle.putStringArrayList(IabHelper.RESPONSE_INAPP_PURCHASE_DATA_LIST, new ArrayList<String>());
            bundle.putStringArrayList(IabHelper.RESPONSE_INAPP_SIGNATURE_LIST, new ArrayList<String>());

            Log.d(TAG, "bundle: " + bundle);
            return bundle;
        }

        @Override
        public int isBillingSupportedExtraParams(int apiVersion, String packageName, String type, Bundle extraParams) throws RemoteException {
            Log.d(TAG, "================= isBillingSupportedExtraParams =================");
            return 0;
        }

    };
}
