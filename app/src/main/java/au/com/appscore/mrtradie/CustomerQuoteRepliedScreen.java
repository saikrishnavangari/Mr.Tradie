package au.com.appscore.mrtradie;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import au.com.appscore.mrtradie.Dialog.TradieQuoteDialog;


public class CustomerQuoteRepliedScreen extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView textViewPriceLabel, textViewPrice, textViewQuoteDesc, textViewCompanyName, textViewQuoteAddress,textViewQuoteStatus;
    private ImageButton buttonAccept, buttonDecline;
    private SharedPreferences sharedPreferences;
    private String quoteID;
    ProgressDialog pDialog;
    private NonScrollListView listViewAvailability;
    JSONArray jsonArrayAvailability;
    JSONObject jsonObjectQuote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_quote_replied_screen);

        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.PrimaryDarkColor));
        }

        sharedPreferences = getApplicationContext().getSharedPreferences(AppUtils.SharedPreferenceFileName, 0);
        pDialog = new ProgressDialog(CustomerQuoteRepliedScreen.this);

        // Initialise UI Elements
        textViewPriceLabel = (TextView) toolbar.findViewById(R.id.textViewPriceLabel);
        textViewPrice = (TextView) toolbar.findViewById(R.id.textViewPrice);
        textViewQuoteDesc = (TextView) toolbar.findViewById(R.id.textViewQuoteDesc);
        textViewCompanyName = (TextView) findViewById(R.id.textViewCompanyName);
        textViewQuoteAddress = (TextView) findViewById(R.id.textViewQuoteAddress);
        listViewAvailability = (NonScrollListView) findViewById(R.id.listViewAvailability);
        textViewQuoteStatus = (TextView) findViewById(R.id.textViewQuoteStatus);
        buttonAccept = (ImageButton) findViewById(R.id.buttonAccept);
        buttonDecline = (ImageButton) findViewById(R.id.buttonDecline);
        buttonDecline.setVisibility(View.GONE);
        buttonAccept.setVisibility(View.GONE);

        // Retrieve Quote details from intent
        String quoteDetail = getIntent().getStringExtra("QuoteDetail");
        try {
            JSONObject jsonObjectQuoteDetail = new JSONObject(quoteDetail);
            jsonObjectQuote = jsonObjectQuoteDetail.getJSONObject("quote");
            JSONObject jsonObjectAddress = jsonObjectQuoteDetail.getJSONObject("address");
            JSONObject jsonObjectUserProfile = jsonObjectQuote.getJSONObject("user_profile");
            quoteID = jsonObjectQuote.getString("quote_id");
            //String stringAvailability = jsonObjectQuoteDetail.getString("availability");
            //JSONArray jsonArrayAvailability = new JSONArray(stringAvailability);
            // Test for availability
            jsonArrayAvailability = jsonObjectQuoteDetail.getJSONArray("availability");
            ArrayList<String> arrayListAvailability = new ArrayList<>();
            for (int i=0;i<jsonArrayAvailability.length();i++) {
                JSONObject jsonObject = jsonArrayAvailability.getJSONObject(i);
                arrayListAvailability.add(jsonObject.getString("start_date")+" - "+jsonObject.getString("end_date"));
            }
            ArrayAdapter<String> itemsAdapter =
                    new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayListAvailability);
            listViewAvailability.setAdapter(itemsAdapter);
            // End test

            if (jsonObjectQuote.getString("quote_status").equals("Replied")&&!jsonObjectQuote.getString("budget").equals("0"))
            {
                textViewPriceLabel.setVisibility(View.VISIBLE);
                textViewPrice.setVisibility(View.VISIBLE);
                textViewPrice.setText("$" + jsonObjectQuote.getString("budget"));
                textViewQuoteStatus.append("Replied");

                // Set On Click listeners for Accept and Decline buttons
                buttonDecline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(CustomerQuoteRepliedScreen.this)
                                .setTitle("Confirmation")
                                .setMessage("Do you want to decline this quote?")
                                .setCancelable(false)
                                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Request server to decline the quote
                                        Log.d("Debug","Decline quote id :"+quoteID);
                                        declineQuote();
                                    }
                                })
                                .setNegativeButton("NO",null)
                                .show();
                    }
                });

                buttonAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        acceptQuote();
                    }
                });

            }
            else {
                textViewPriceLabel.setVisibility(View.GONE);
                textViewPrice.setVisibility(View.GONE);
                textViewQuoteStatus.append("Not Replied");
                buttonAccept.setEnabled(false);
                buttonDecline.setEnabled(false);
                buttonAccept.setBackgroundResource(R.drawable.icon_accept_disabled);
                buttonDecline.setBackgroundResource(R.drawable.icon_decline_disabled);
            }

            textViewQuoteDesc.setText(jsonObjectQuote.getString("quote_desc"));
            textViewCompanyName.setText(jsonObjectUserProfile.getString("company"));
            textViewQuoteAddress.setText(jsonObjectAddress.getString("full_address"));

//            for (int i=0;i<jsonArrayAvailability.length();i++) {
//                JSONObject jsonObject = jsonArrayAvailability.getJSONObject(i);
//                textViewAvailability.append(jsonObject.getString("start_date")+"-"+jsonObject.getString("end_date")+"\n");
//            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.action_tradie_quote_settings)
        {
            TradieQuoteDialog dialog = new TradieQuoteDialog();
            dialog.setListener(new View.OnClickListener() {
                                   @Override
                                   public void onClick(View v) {
                                       buttonAccept.callOnClick();
                                   }
                               },
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            buttonDecline.callOnClick();
                        }
                    });
            dialog.show(getFragmentManager(), "");
        }
        else {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Method to decline the quote
    private void declineQuote() {
        pDialog.setMessage("Declining this quote...");
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        params.put("email",sharedPreferences.getString(AppUtils.KEY_EMAIL,null));
        params.put("access_token",sharedPreferences.getString(AppUtils.KEY_ACCESS_TOKEN,null));
        params.put("quote_id",quoteID);

        client.post(AppUtils.getDeclineQuoteURL(), params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                pDialog.show();
                pDialog.setCancelable(false);

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                pDialog.dismiss();
                String response = new String(responseBody);
                // Parse JSON data
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject jsonData = jsonObject.getJSONObject("data");

                    String mrt_status = jsonData.getString("mrt_status");

                    if (mrt_status.equals("1007")||mrt_status.equals("1009"))
                    {
                        // Successfully declined the quote
                        new AlertDialog.Builder(CustomerQuoteRepliedScreen.this)
                                .setTitle("Quote declined")
                                .setMessage("You've successfully declined this quotation. The tradie will be informed.")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Set result code to OK and close this activity
                                        setResult(AppUtils.RESULT_OK);
                                        finish();
                                    }
                                })
                                .show();
                    }
                    else {
                        String mrt_desc = jsonData.getString("mrt_desc");
                        new AlertDialog.Builder(CustomerQuoteRepliedScreen.this)
                                .setTitle("Error")
                                .setMessage(mrt_desc)
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .show();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(CustomerQuoteRepliedScreen.this)
                            .setTitle("Server Error")
                            .setMessage("Sorry ! Could not get data from server.")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                pDialog.dismiss();
                new AlertDialog.Builder(CustomerQuoteRepliedScreen.this)
                        .setTitle("Error")
                        .setMessage("Please check your Internet connection.")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            }
        });
    }

    // Method to accept the quote
    private void acceptQuote() {
        pDialog.setMessage("Accepting this quote...");
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        params.put("email",sharedPreferences.getString(AppUtils.KEY_EMAIL,null));
        params.put("access_token",sharedPreferences.getString(AppUtils.KEY_ACCESS_TOKEN,null));
        params.put("quote_id",quoteID);

        client.post(AppUtils.getAcceptQuoteURL(), params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                pDialog.show();
                pDialog.setCancelable(false);

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                pDialog.dismiss();
                String response = new String(responseBody);
                // Parse JSON data
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject jsonData = jsonObject.getJSONObject("data");

                    String mrt_status = jsonData.getString("mrt_status");

                    if (mrt_status.equals("1007") || mrt_status.equals("1009")) {
                        // Successfully declined the quote
                        new AlertDialog.Builder(CustomerQuoteRepliedScreen.this)
                                .setTitle("Quote accepted")
                                .setMessage("You've successfully accepted this quotation. Your tradie will be in contact.")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Set result code to OK and close this activity
                                        setResult(AppUtils.RESULT_OK);
                                        finish();
                                    }
                                })
                                .show();
                    } else {
                        String mrt_desc = jsonData.getString("mrt_desc");
                        new AlertDialog.Builder(CustomerQuoteRepliedScreen.this)
                                .setTitle("Error")
                                .setMessage(mrt_desc)
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .show();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(CustomerQuoteRepliedScreen.this)
                            .setTitle("Server Error")
                            .setMessage("Sorry ! Could not get data from server.")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                pDialog.dismiss();
                new AlertDialog.Builder(CustomerQuoteRepliedScreen.this)
                        .setTitle("Error")
                        .setMessage("Please check your Internet connection.")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        try {
            if (jsonObjectQuote.getString("quote_status").equals("New"))
                return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        getMenuInflater().inflate(R.menu.menu_tradie_quote_reply_screen, menu);
        return true;
    }
}
