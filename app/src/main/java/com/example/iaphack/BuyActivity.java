package com.example.iaphack;

import com.example.iaphack.R;

import com.example.iaphack.google.util.Base64;
import com.example.iaphack.google.util.IabHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

public class BuyActivity extends Activity {

    public static final String TAG = "BillingHack";

    public static final String BUY_INTENT = "com.example.iaphack.BUY";
    public static final String EXTRA_PACKAGENAME = "packageName";
    public static final String EXTRA_PRODUCT_ID = "product";
    public static final String EXTRA_DEV_PAYLOAD = "payload";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!BUY_INTENT.equals(getIntent().getAction())) {
            finish();
        }
        Log.d(TAG, "Buy intent!");
        setContentView(R.layout.buy_dialog);

        String packageName = getIntent().getExtras().getString(EXTRA_PACKAGENAME);
        String productId = getIntent().getExtras().getString(EXTRA_PRODUCT_ID);
        String devPayload = getIntent().getExtras().getString(EXTRA_DEV_PAYLOAD);
        Log.d(TAG, "packageName: " + packageName);
        Log.d(TAG, "productId: " + productId);
        Log.d(TAG, "devPayload: " + devPayload);

        try {
            long timeNow = System.currentTimeMillis();
            Random r = new Random(System.currentTimeMillis());
            long num1 = (long)(r.nextDouble()*10E20);
            long num2 = (long)(r.nextDouble()*10E16);

            JSONObject pData = new JSONObject();
            String purchaseToken = Base64.encode(String.valueOf(num1+num2).getBytes());
            pData.put("orderId", String.valueOf(num1) + "." + String.valueOf(num2));
            pData.put("packageName", packageName);
            pData.put("productId", productId);
            pData.put("purchaseTime", timeNow);
            pData.put("purchaseToken", purchaseToken);
            pData.put("purchaseState", IabHelper.BILLING_RESPONSE_RESULT_OK);
            pData.put("developerPayload", devPayload);
            String signature = "fake_signature";
            new MaterialAlertDialogBuilder(this)
                .setTitle("IAP Hack")
                .setMessage("Do you want to get this item for free?\nproductId: " + productId)
                .setCancelable(false)
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    Intent data = new Intent();
                    Bundle extras = new Bundle();
                    DbHelper dbHelper = new DbHelper(getApplicationContext());
                    extras.putInt(IabHelper.RESPONSE_CODE, IabHelper.BILLING_RESPONSE_RESULT_OK);
                    extras.putString(IabHelper.RESPONSE_INAPP_PURCHASE_DATA, pData.toString());
                    extras.putString(IabHelper.RESPONSE_INAPP_SIGNATURE, signature);
                    Log.d(TAG, "bundle: " + extras);
                    data.putExtras(extras);

                    setResult(RESULT_OK, data);
                    dbHelper.addPurchaseHistory(pData);
                    finish();
                })
                .setNegativeButton("No", (dialogInterface, i) -> {
                    Intent data = new Intent();
                    Bundle extras = new Bundle();
                    extras.putInt(IabHelper.RESPONSE_CODE, IabHelper.BILLING_RESPONSE_RESULT_USER_CANCELED);
                    data.putExtras(extras);

                    setResult(RESULT_CANCELED, data);
                    finish();
                })
                .show();
        } catch (JSONException ex) {
            Log.e(TAG, "error: ", ex);
        }
    }

}
