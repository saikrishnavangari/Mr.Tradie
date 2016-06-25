package au.com.appscore.mrtradie.activity;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import au.com.appscore.mrtradie.AppUtils;
import au.com.appscore.mrtradie.Billing.IabHelper;
import au.com.appscore.mrtradie.Billing.IabResult;
import au.com.appscore.mrtradie.Billing.Inventory;
import au.com.appscore.mrtradie.Billing.Purchase;
import au.com.appscore.mrtradie.R;
import au.com.appscore.mrtradie.utils.ControlPraser;

public class Premium extends AppCompatActivity {

    TextView tradieName;
    ImageView premiumStatus;
    TextView validTime;
    TextView timeLeft;
    TextView tradieTitle;
    TextView timeLeft1;
    TextView validTime1;
    ImageView premiumFootageImage;

    ImageView tenDollar;
    ImageView hunderdDollar;
    ImageView tenDollar1;

    //bussiness items
    ImageView imageViewBussinessImage;
    TextView businessName;
    TextView businessAddress;
    ImageView quoteButton;
    LinearLayout ratingBar;
    ImageView imageIndicator;

    Toolbar toolbar;
    SharedPreferences sharedPreferences;

    IabHelper iabHelper;
    LinkedList<PremiumItem> billingItems;
    IabHelper.OnIabPurchaseFinishedListener purchaseFinishedListener;

    int attempt;
    private static String tenDollarPremium = "10.dollar.premium";
    private static String hundredDollarPremium =  "100.dollar.premium";
    private static String priorityPremium =  "priority.premium";
    private static String key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuP7A8rs+LAepZl+PtanrNByPRiclRsS7vJ/RMWB4b2v1JhHH+kRGb6uCbVTuBeXPzR6DQQP+aGFJxb8UdOfJuv5QJaJPSgg1Z3rgb2OQyxY+xorxNjxZ+qFGwC97BqPxSLiikmcGz5cjXmer4ohj78hOqwvkfRnkvNPHS2pxmv9NjO97V4wNSK2o/wgFQulYOIiw2RZdcjI77xidSgzvoTxa3adkOI8Kjjp3AQhknuUvZa57Wou123Vr08CA+N0b0DuTNQkp8/iCfx6QKWlHhvTgrd+PUxTQV7n3EI7TH6LE6RuMnX6BtESdd0aM9hNiO4G3d++/vtYeN6M5D1MiXQIDAQAB";
    private android.support.v7.app.AlertDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_premium);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        sharedPreferences = getApplicationContext().getSharedPreferences(AppUtils.SharedPreferenceFileName, 0);
        setTitle("Premium");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.PrimaryDarkColor));
        }
        attempt = 0;
        init();
        initPremium();
    }

    private void initPremium()
    {
        iabHelper = new IabHelper(this, key);
        final IabHelper.QueryInventoryFinishedListener
                mQueryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {
            public void onQueryInventoryFinished(IabResult result, Inventory inventory)
            {
                if (result.isFailure()) {
                    // handle error
                    Log.d("failure check","inside premium exception");
                    Toast.makeText(Premium.this,"error", Toast.LENGTH_LONG).show();
                    return;
                }


                String tenPrice =
                        inventory.getSkuDetails(tenDollarPremium).getPrice();
                Log.d("tenprice",tenPrice);
                String hunPrice =
                        inventory.getSkuDetails(hundredDollarPremium).getPrice();
                String priorityPrice =
                        inventory.getSkuDetails(priorityPremium).getPrice();
                Log.d("tenprice",priorityPrice);
                PremiumItem tenPriceItem = new PremiumItem(tenDollarPremium, tenPrice);
                PremiumItem hunPriceItem = new PremiumItem(hundredDollarPremium, hunPrice);
                PremiumItem priorItem = new PremiumItem(priorityPremium, priorityPrice);
                billingItems.add(tenPriceItem);
                billingItems.add(hunPriceItem);
                billingItems.add(priorItem);
                // update the UI
            }
        };
        iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (result.isSuccess()) {
                    List additionalSkuList = new LinkedList();
                    additionalSkuList.add(tenDollarPremium);
                    additionalSkuList.add(hundredDollarPremium);
                    additionalSkuList.add(priorityPremium);


                    iabHelper.queryInventoryAsync(true, additionalSkuList,
                            mQueryFinishedListener);
                    // Oh noes, there was a problem.
                    Log.d("Billing", "Problem setting up In-app Billing: " + result);
                }

                if (!result.isSuccess()) {
                    iabHelper.queryInventoryAsync(mQueryFinishedListener);
                }
                // Hooray, IAB is fully set up!
            }
        });


        purchaseFinishedListener
                = new IabHelper.OnIabPurchaseFinishedListener() {
            public void onIabPurchaseFinished(IabResult result, Purchase purchase)
            {
                if (result.isFailure()) {

                    return;
                }
                else if (purchase.getSku().equals(tenDollarPremium)) {
                    // consume the gas and update the UI
                }
                else if (purchase.getSku().equals(hundredDollarPremium)) {
                    // give user access to premium content and update the UI
                }
                else if(purchase.getSku().equals(priorityPremium)){

                }
            }
        };


    }

    private void  init()
    {
        billingItems = new LinkedList<>();
        tradieName = ControlPraser.PraserControl(this, R.id.name_tradie);
        premiumStatus = ControlPraser.PraserControl(this, R.id.premium_status);
        validTime = ControlPraser.PraserControl(this, R.id.valid_time);
        timeLeft = ControlPraser.PraserControl(this, R.id.time_left);
        tradieTitle = ControlPraser.PraserControl(this, R.id.tradie_title);
        timeLeft1 = ControlPraser.PraserControl(this, R.id.time_left1);
        validTime1 = ControlPraser.PraserControl(this, R.id.valid_time1);
        premiumFootageImage = ControlPraser.PraserControl(this, R.id.premium_footage_image);
        tenDollar = ControlPraser.PraserControl(this, R.id.ten_dollar_top_up);
        hunderdDollar = ControlPraser.PraserControl(this, R.id.hundred_dollor_top_up);
        tenDollar1 = ControlPraser.PraserControl(this, R.id.ten_dollar_top_up1);
        imageViewBussinessImage = ControlPraser.PraserControl(this, R.id.imageViewBusinessImage);
        businessName = ControlPraser.PraserControl(this, R.id.textViewBusinessName);
        businessAddress = ControlPraser.PraserControl(this, R.id.textViewBusinessAddress);
        quoteButton = ControlPraser.PraserControl(this, R.id.imageButtonGetQuote);
        imageIndicator = ControlPraser.PraserControl(this, R.id.imageViewItemIndicator);


        tenDollar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iabHelper.launchPurchaseFlow(Premium.this, tenDollarPremium, 10001,
                        purchaseFinishedListener, "bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ");
            }
        });

        hunderdDollar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iabHelper.launchPurchaseFlow(Premium.this, hundredDollarPremium, 10001,
                        purchaseFinishedListener, "bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ");
            }
        });

        tenDollar1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iabHelper.launchPurchaseFlow(Premium.this, priorityPremium, 10001,
                        purchaseFinishedListener, "bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ");
            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (iabHelper != null) iabHelper.dispose();
        iabHelper = null;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("", "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (iabHelper == null) return;
        // Pass on the activity result to the helper for handling
        if (!iabHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
        else {
            Log.i("", "onActivityResult handled by IABUtil.");
        }
    }

    private void setPremium(final int type)
    {
        String email  = sharedPreferences.getString(AppUtils.KEY_EMAIL,"");
        String token = sharedPreferences.getString(AppUtils.KEY_ACCESS_TOKEN,"");
        String URL = AppUtils.getPremiumUrl();
        int premium = 0;
        int listing = 0;
        if(type  == 1)
            premium = 30;
        if(type == 2)
            premium = 365;
        if(type == 3)
            listing = 30;
        AsyncHttpClient client = new AsyncHttpClient();
        final RequestParams params = new RequestParams();
        params.put("email", email);
        params.put("access_token", token);
        params.put("premium", premium);
        params.put("listing", listing);

        client.post(URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    JSONObject json = new JSONObject(new String(responseBody));
                    JSONObject data = json.getJSONObject("data");
                    if(data.getString("mrt_status").equals("1009"))
                        new android.support.v7.app.AlertDialog.Builder(Premium.this)
                                .setTitle("Success")
                                .setMessage("You have successful purchased the item. Thanks")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                attempt ++;
                if(attempt >=4)
                        new android.support.v7.app.AlertDialog.Builder(Premium.this)
                                .setTitle("Error")
                                .setMessage("An error happened when process you purchase, please contact the service.")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .show();
                setPremium(type);
            }
        });

    }

}
