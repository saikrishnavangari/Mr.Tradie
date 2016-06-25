package au.com.appscore.mrtradie;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import au.com.appscore.mrtradie.Dialog.TradieQuoteDialog;

public class TradieQuoteReplyScreen extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView textViewPriceLabel, textViewQuoteDesc, textViewClientName, textViewQuoteAddress,textViewQuoteStatus;
    private EditText editTextPrice, editTextComment;
    private NonScrollListView listViewAvailability;
    private ImageButton buttonAccept, buttonDecline;
    private ImageView imageViewPhoto1, imageViewPhoto2, imageViewPhoto3;
    private SharedPreferences sharedPreferences;
    private String quoteID;
    ProgressDialog pDialog;
    JSONArray jsonArrayAvailability;
    JSONObject jsonObjectQuote;
    String ownerEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tradie_quote_reply_screen);

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
        pDialog = new ProgressDialog(TradieQuoteReplyScreen.this);

        // Initialise UI Elements
        textViewPriceLabel = (TextView) toolbar.findViewById(R.id.textViewPriceLabel);
        editTextPrice = (EditText) toolbar.findViewById(R.id.editTextPrice);
        editTextComment = (EditText) findViewById(R.id.editTextComments);
        imageViewPhoto1 = (ImageView) findViewById(R.id.imageViewPhoto1);
        imageViewPhoto2 = (ImageView) findViewById(R.id.imageViewPhoto2);
        imageViewPhoto3 = (ImageView) findViewById(R.id.imageViewPhoto3);
        textViewQuoteDesc = (TextView) toolbar.findViewById(R.id.textViewQuoteDesc);
        textViewClientName = (TextView) findViewById(R.id.textViewClientName);
        textViewQuoteAddress = (TextView) findViewById(R.id.textViewQuoteAddress);
        listViewAvailability = (NonScrollListView) findViewById(R.id.listViewAvailability);
        textViewQuoteStatus = (TextView) findViewById(R.id.textViewQuoteStatus);
        buttonAccept = (ImageButton) findViewById(R.id.buttonAccept);
        buttonDecline = (ImageButton) findViewById(R.id.buttonDecline);



        // Retrieve Quote details from intent
        String quoteDetail = getIntent().getStringExtra("QuoteDetail");
        try {
            final JSONObject jsonObjectQuoteDetail = new JSONObject(quoteDetail);
            jsonObjectQuote = jsonObjectQuoteDetail.getJSONObject("quote");
            ownerEmail = jsonObjectQuote.getString("owner_email");
            if(ownerEmail.equals(sharedPreferences.getString(AppUtils.KEY_EMAIL, "")))
                editTextPrice.setEnabled(false);
            JSONObject jsonObjectAddress = jsonObjectQuoteDetail.getJSONObject("address");
            // Test for availability
            Log.d("Debug","Access Token : "+sharedPreferences.getString(AppUtils.KEY_ACCESS_TOKEN,""));
            jsonArrayAvailability = jsonObjectQuoteDetail.getJSONArray("availability");
            ArrayList<String> arrayListAvailability = new ArrayList<>();
            for (int i=0;i<jsonArrayAvailability.length();i++) {
                JSONObject jsonObject = jsonArrayAvailability.getJSONObject(i);
                arrayListAvailability.add(jsonObject.getString("start_date")+" - "+jsonObject.getString("end_date"));
            }
            ArrayAdapter<String> itemsAdapter =
                    new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, arrayListAvailability);
            listViewAvailability.setAdapter(itemsAdapter);
            // End test
            JSONObject jsonObjectUserProfile = jsonObjectQuote.getJSONObject("user_profile");
            quoteID = jsonObjectQuote.getString("quote_id");
            //String stringAvailability = jsonObjectQuoteDetail.getString("availability");
            //JSONArray jsonArrayAvailability = new JSONArray(stringAvailability);


            if (jsonObjectQuote.getString("quote_status").equals("Replied"))
            {
                // Quote has been replied already
                editTextPrice.setText("$" + jsonObjectQuote.getString("budget"));
                if (!jsonObjectQuote.getString("comment").equals("null"))
                    editTextComment.setText(jsonObjectQuote.getString("comment"));
                textViewQuoteStatus.append("Replied");
                editTextPrice.setEnabled(false);
                editTextComment.setEnabled(false);
                buttonAccept.setEnabled(false);
                buttonDecline.setEnabled(false);
                //buttonAccept.setBackgroundResource(R.drawable.icon_accept_disabled);
                //buttonDecline.setBackgroundResource(R.drawable.icon_decline_disabled);
            }
            else {
                // Quote hasn't been replied yet
                textViewQuoteStatus.append("Not Replied");

                // Set On click listeners for Accept and Decline buttons
                buttonDecline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(TradieQuoteReplyScreen.this)
                                .setTitle("Confirmation")
                                .setMessage("Do you want to decline this quote?")
                                .setCancelable(false)
                                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Request server to decline the quote
                                        Log.d("Debug", "Decline quote id :" + quoteID);
                                        declineQuote();
                                    }
                                })
                                .setNegativeButton("NO", null)
                                .show();
                    }
                });

                buttonAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!TextUtils.isEmpty(editTextPrice.getText())&&listViewAvailability.getCheckedItemPosition()!= AdapterView.INVALID_POSITION)
                            acceptQuote();
                        else
                        {
                            new AlertDialog.Builder(TradieQuoteReplyScreen.this)
                                    .setTitle("Validation")
                                    .setMessage("You need to fill in estimated price and select a time to accept the quote.")
                                    .setCancelable(false)
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    }
                });
            }
            textViewQuoteDesc.setText(jsonObjectQuote.getString("quote_desc"));
            textViewClientName.setText(jsonObjectUserProfile.getString("user_full_name"));
            textViewQuoteAddress.setText(jsonObjectAddress.getString("full_address"));
            Ion.with(imageViewPhoto1).load(jsonObjectQuoteDetail.getString("photo1"));
            Ion.with(imageViewPhoto2).load(jsonObjectQuoteDetail.getString("photo2"));
            Ion.with(imageViewPhoto3).load(jsonObjectQuoteDetail.getString("photo3"));

            imageViewPhoto1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(TradieQuoteReplyScreen.this, FullScreenImage.class);
                    try {
                        intent.putExtra("photo1", jsonObjectQuoteDetail.getString("photo1"));
                        intent.putExtra("photo2", jsonObjectQuoteDetail.getString("photo2"));
                        intent.putExtra("photo3", jsonObjectQuoteDetail.getString("photo3"));
                        intent.putExtra("position",0);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    startActivity(intent);
                }
            });
            imageViewPhoto2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(TradieQuoteReplyScreen.this, FullScreenImage.class);
                    try {
                        intent.putExtra("photo1", jsonObjectQuoteDetail.getString("photo1"));
                        intent.putExtra("photo2", jsonObjectQuoteDetail.getString("photo2"));
                        intent.putExtra("photo3", jsonObjectQuoteDetail.getString("photo3"));
                        intent.putExtra("position",1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    startActivity(intent);
                }
            });
            imageViewPhoto3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(TradieQuoteReplyScreen.this, FullScreenImage.class);
                    try {
                        intent.putExtra("photo1", jsonObjectQuoteDetail.getString("photo1"));
                        intent.putExtra("photo2", jsonObjectQuoteDetail.getString("photo2"));
                        intent.putExtra("photo3", jsonObjectQuoteDetail.getString("photo3"));
                        intent.putExtra("position",2);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    startActivity(intent);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
                        new AlertDialog.Builder(TradieQuoteReplyScreen.this)
                                .setTitle("Quote declined")
                                .setMessage("You've successfully declined this quotation. The customer will be informed.")
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
                        new AlertDialog.Builder(TradieQuoteReplyScreen.this)
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
                    new AlertDialog.Builder(TradieQuoteReplyScreen.this)
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
                new AlertDialog.Builder(TradieQuoteReplyScreen.this)
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
        params.put("access_token", sharedPreferences.getString(AppUtils.KEY_ACCESS_TOKEN, null));
        params.put("quote_id", quoteID);
        params.put("budget", editTextPrice.getText());
        if (!TextUtils.isEmpty(editTextComment.getText()))
            params.put("comments",editTextComment.getText());
        JSONArray jsonArray = new JSONArray();
        try {
            jsonArray.put(jsonArrayAvailability.get(listViewAvailability.getCheckedItemPosition()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        params.put("availability",jsonArray);

        client.post(AppUtils.getUpdateQuoteURL(), params, new AsyncHttpResponseHandler() {

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
                        new AlertDialog.Builder(TradieQuoteReplyScreen.this)
                                .setTitle("Quote accepted")
                                .setMessage("You've successfully accepted this quotation. Your customer will be informed.")
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
                        new AlertDialog.Builder(TradieQuoteReplyScreen.this)
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
                    new AlertDialog.Builder(TradieQuoteReplyScreen.this)
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
                new AlertDialog.Builder(TradieQuoteReplyScreen.this)
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id == R.id.action_tradie_quote_settings)
        {
            final TradieQuoteDialog dialog = new TradieQuoteDialog();
            dialog.setListener(new View.OnClickListener() {
                                   @Override
                                   public void onClick(View v) {
                                       if (!TextUtils.isEmpty(editTextPrice.getText())&&listViewAvailability.getCheckedItemPosition()!= AdapterView.INVALID_POSITION)
                                           acceptQuote();
                                       else
                                       {
                                           new AlertDialog.Builder(TradieQuoteReplyScreen.this)
                                                   .setTitle("Validation")
                                                   .setMessage("You need to fill in estimated price and select a time to accept the quote.")
                                                   .setCancelable(false)
                                                   .setPositiveButton("OK", null)
                                                   .show();
                                       }

                                       dialog.dismiss();
                                   }
                               },
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new AlertDialog.Builder(TradieQuoteReplyScreen.this).setTitle("Confirmation")
                                    .setMessage("Do you want to decline this quote?")
                                    .setCancelable(false)
                                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            // Request server to decline the quote
                                            Log.d("Debug", "Decline quote id :" + quoteID);
                                            declineQuote();
                                        }
                                    })
                                    .setNegativeButton("NO", null)
                                    .show();

                            dialog.dismiss();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(ownerEmail == null)
            return true;
        try {
            if(jsonObjectQuote.getString("quote_status").equals("Replied") && !ownerEmail.equals(sharedPreferences.getString(AppUtils.KEY_EMAIL, "")))
                return true;
            if(jsonObjectQuote.getString("quote_status").equals("New") && ownerEmail.equals(sharedPreferences.getString(AppUtils.KEY_EMAIL, "")))
                return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        getMenuInflater().inflate(R.menu.menu_tradie_quote_reply_screen, menu);
        return true;
    }
}
