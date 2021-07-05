package com.example.iaphack;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DbHelper extends SQLiteOpenHelper {
    public static final String TAG = "DbHelper";
    static final String dbName = "BillingTransactions";
    private String createTableQuery =
            "CREATE TABLE IF NOT EXISTS '%s'(" +
                    "id INTEGER PRIMARY KEY," +
                    "orderId TEXT," +
                    "packageName TEXT," +
                    "productId TEXT," +
                    "purchaseTime INTEGER," +
                    "purchaseToken TEXT," +
                    "purchaseState INTEGER," +
                    "developerPayload TEXT" +
            ")";

    public DbHelper(Context context) {
        super(context, dbName, null, 48);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    private void createTable(SQLiteDatabase db, String packagename) {
        String query = String.format(createTableQuery, packagename);
        db.execSQL(query);
    }

    public boolean addPurchaseHistory(JSONObject purchaseInfo) {
        Log.d(TAG, "addPurchaseHistory: " + purchaseInfo.toString());
        try {
            String orderId = purchaseInfo.getString("orderId");
            String packageName = purchaseInfo.getString("packageName");
            String productId = purchaseInfo.getString("productId");
            String purchaseTime = purchaseInfo.getString("purchaseTime");
            String purchaseToken = purchaseInfo.getString("purchaseToken");
            int purchaseState = purchaseInfo.getInt("purchaseState");
            String developerPayload = purchaseInfo.optString("developerPayload", "");

            ContentValues values = new ContentValues();
            values.put("orderId", orderId);
            values.put("packageName", packageName);
            values.put("productId", productId);
            values.put("purchaseTime", purchaseTime);
            values.put("purchaseToken", purchaseToken);
            values.put("purchaseState", purchaseState);
            values.put("developerPayload", developerPayload);

            SQLiteDatabase db = this.getWritableDatabase();
            createTable(db, packageName);
            db.insert("'" + packageName + "'", null , values);
            db.close();
        } catch (Exception ex) {
            Log.e(TAG, "error: ", ex);
            return false;
        }
        return true;
    }

    public ArrayList<JSONObject> getPurchaseHistory(String packageName) {
        ArrayList<JSONObject> arrayList = new ArrayList<>();
        try {
            String selectQuery = String.format("SELECT * FROM '%s'", packageName);
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);
            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    JSONObject pData = new JSONObject();
                    pData.put("orderId",cursor.getString(1));
                    pData.put("packageName",cursor.getString(2));
                    pData.put("productId",cursor.getString(3));
                    pData.put("purchaseTime",cursor.getInt(4));
                    pData.put("purchaseToken",cursor.getString(5));
                    pData.put("purchaseState",cursor.getInt(6));
                    pData.put("developerPayload",cursor.getString(7));
                    arrayList.add(pData);
                } while (cursor.moveToNext());
            }
        } catch (Exception ex) {
            Log.e(TAG, "error: ", ex);
        }
        return arrayList;
    }

    public void deleteAll() {
    }

}
