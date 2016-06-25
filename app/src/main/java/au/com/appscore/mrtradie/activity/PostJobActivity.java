package au.com.appscore.mrtradie.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.actionsheet.ActionSheet;
import com.github.jjobes.slidedatetimepicker.SlideDateTimeListener;
import com.github.jjobes.slidedatetimepicker.SlideDateTimePicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import au.com.appscore.mrtradie.AppUtils;
import au.com.appscore.mrtradie.R;
import au.com.appscore.mrtradie.utils.ControlPraser;

public class PostJobActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ActionSheet.ActionSheetListener {



    // Variables for Google API

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private Location mLastLocation;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    private Toolbar toolbar;
    private EditText editTextQuoteDesc, editTextAddress,editTextTime;
    private ImageView imageViewAddress;
    private TextView textViewStartTime,textViewEndTime;
    private LinearLayout linearLayoutStartEndTime;
    private Switch switchAllDay;
    private ImageView imageViewAddPhoto1, imageViewAddPhoto2, imageViewAddPhoto3;

    private String quote_email;
    private JSONObject jsonObjectAddress = null;
    private JSONArray jsonArrayAvailability = new JSONArray();
    private SimpleDateFormat mFormatter = new SimpleDateFormat("EEE dd MMM yyyy hh:mm a");

    private SharedPreferences sharedPreferences;


    private Spinner category;
    ProgressDialog pDialog;

    private SlideDateTimeListener listenerStartTime = new SlideDateTimeListener() {

        @Override
        public void onDateTimeSet(Date date)
        {
            Toast.makeText(PostJobActivity.this,
                    mFormatter.format(date), Toast.LENGTH_SHORT).show();
            textViewStartTime.setText(" " + mFormatter.format(date));
        }

        // Optional cancel listener
        @Override
        public void onDateTimeCancel()
        {
            Toast.makeText(PostJobActivity.this,
                    "Canceled", Toast.LENGTH_SHORT).show();
        }
    };

    private SlideDateTimeListener listenerEndTime = new SlideDateTimeListener() {

        @Override
        public void onDateTimeSet(Date date)
        {
            Toast.makeText(PostJobActivity.this,
                    mFormatter.format(date), Toast.LENGTH_SHORT).show();
            textViewEndTime.setText(" " + mFormatter.format(date));
        }

        // Optional cancel listener
        @Override
        public void onDateTimeCancel()
        {
            Toast.makeText(PostJobActivity.this,
                    "Canceled", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_job);
        jsonObjectAddress = new JSONObject();
        sharedPreferences = getApplicationContext().getSharedPreferences(AppUtils.SharedPreferenceFileName,0);

        // Camera Utility
        category = ControlPraser.PraserControl(this, R.id.spinnerOccupation);
        pDialog = new ProgressDialog(PostJobActivity.this);
        pDialog.setMessage("Requesting quote...");

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.PrimaryDarkColor));
        }

        // Retrieve business email
        quote_email = getIntent().getStringExtra("quote_email");

        // Retrieve edit text for quote description
        Toolbar viewToolbar = (Toolbar)findViewById(R.id.toolbar);
        editTextQuoteDesc = (EditText) viewToolbar.findViewById(R.id.editTextQuoteDesc);
        editTextAddress = (EditText) findViewById(R.id.editTextAddress);
        imageViewAddress = (ImageView) findViewById(R.id.imageViewAddress);
        editTextTime = (EditText) findViewById(R.id.editTextTime);
        linearLayoutStartEndTime = (LinearLayout) findViewById(R.id.linearLayoutStartEndTime);
        imageViewAddPhoto1 = (ImageView) findViewById(R.id.imageViewAddPhoto1);
        imageViewAddPhoto2 = (ImageView) findViewById(R.id.imageViewAddPhoto2);
        imageViewAddPhoto3 = (ImageView) findViewById(R.id.imageViewAddPhoto3);

        editTextTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // custom dialog
                final Dialog dialog = new Dialog(PostJobActivity.this);
                dialog.setContentView(R.layout.addtime_dialog_layout);
                dialog.setTitle("Select Timing");
                dialog.setCancelable(false);

                // set the custom dialog components - text, image and button

                Button dialogButtonOK = (Button) dialog.findViewById(R.id.buttonOK);
                Button dialogButtonCancel = (Button) dialog.findViewById(R.id.buttonCancel);
                textViewStartTime = (TextView) dialog.findViewById(R.id.textViewStartTime);
                textViewEndTime = (TextView) dialog.findViewById(R.id.textViewEndTime);
                switchAllDay = (Switch) dialog.findViewById(R.id.switchAllDay);

                textViewStartTime.setText(mFormatter.format(new Date()));
                textViewStartTime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new SlideDateTimePicker.Builder(getSupportFragmentManager())
                                .setListener(listenerStartTime)
                                .setInitialDate(new Date())
                                .setMinDate(new Date())
                                        //.setMaxDate(maxDate)
                                        //.setIs24HourTime(true)
                                        //.setTheme(SlideDateTimePicker.HOLO_DARK)
                                        //.setIndicatorColor(getResources().getColor(R.color.PrimaryColor))
                                .build()
                                .show();
                    }
                });

                // Add 8 hours to current time
                final Calendar cal = Calendar.getInstance(); // creates calendar
                cal.setTime(new Date()); // sets calendar time/date
                cal.add(Calendar.HOUR_OF_DAY, 8); // adds eight hour
                textViewEndTime.setText(mFormatter.format(cal.getTime()));
                textViewEndTime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        new SlideDateTimePicker.Builder(getSupportFragmentManager())
                                .setListener(listenerEndTime)
                                .setInitialDate(cal.getTime())
                                .setMinDate(new Date())
                                        //.setMaxDate(maxDate)
                                        //.setIs24HourTime(true)
                                        //.setTheme(SlideDateTimePicker.HOLO_DARK)
                                        //.setIndicatorColor(getResources().getColor(R.color.PrimaryColor))
                                .build()
                                .show();
                    }
                });
                // if button is clicked, close the custom dialog
                dialogButtonOK.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Display Start End Time
                        View view = getLayoutInflater().inflate(R.layout.start_end_time_layout, null);
                        TextView textViewStartTimeToDisplay = (TextView) view.findViewById(R.id.textViewStartTime);
                        TextView textViewEndTimeToDisplay = (TextView) view.findViewById(R.id.textViewEndTime);
                        textViewStartTimeToDisplay.setText(textViewStartTime.getText());
                        textViewEndTimeToDisplay.setText(textViewEndTime.getText());

                        linearLayoutStartEndTime.addView(view);

                        // Add record to availability array
                        JSONObject jsonObjectAvailability = new JSONObject();
                        try {
                            jsonObjectAvailability.put("is_available",1);
                            jsonObjectAvailability.put("start_date",textViewStartTime.getText());
                            jsonObjectAvailability.put("end_date",textViewEndTime.getText());

                            if (switchAllDay.isChecked())
                                jsonObjectAvailability.put("all_day",1);
                            else
                                jsonObjectAvailability.put("all_day", 0);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        jsonArrayAvailability.put(jsonObjectAvailability);

                        dialog.dismiss();
                    }
                });
                dialogButtonCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

        imageViewAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayLocation();
            }
        });

        // First we need to check availability of play services
        if (checkPlayServices()) {
            // Building the GoogleApi client
            buildGoogleApiClient();
        }
    }

    private void displayLocation() {

        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {



            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

            // Add lat and long to json object of address
            try {
                jsonObjectAddress.put("latitude",latitude);
                jsonObjectAddress.put("longitude",longitude);
            } catch (JSONException e) {
                e.printStackTrace();
            }


            Log.d("Debug", "Latitude : " + latitude + " Longitude " + longitude);

            // Test display address
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this, Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String postalCode = addresses.get(0).getPostalCode();
                Log.d("Debug",address+" "+city+" "+state+" "+postalCode);
                editTextAddress.setText(address+" "+city+" "+state+" "+postalCode);

                // Add additional address fields to json object of address
                //jsonObjectAddress.put("full_address",address+" "+city+" "+state+" "+postalCode);
                jsonObjectAddress.put("street",address);
                jsonObjectAddress.put("city",city);
                jsonObjectAddress.put("state",state);
                jsonObjectAddress.put("post_code",postalCode);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }


        } else {

            Log.d("Debug","Could not get current location");
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        displayLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("Debug", "Connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }

    @Override
    public void onDismiss(ActionSheet actionSheet, boolean isCancel) {

    }

    @Override
    public void onOtherButtonClick(ActionSheet actionSheet, int index) {

    }

    private void postNewJob()
    {
        if(category.getSelectedItemId() < 1 || editTextAddress.getText().length() == 0 ||jsonArrayAvailability.length() == 0 )
        {
            new AlertDialog.Builder(PostJobActivity.this)
                    .setTitle("Error")
                    .setMessage("Sorry ! Please fill all the required filed.")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();
            return;
        }
        String url = AppUtils.getNewJobUrl();
        String email = sharedPreferences.getString(AppUtils.KEY_EMAIL, "");
        String accessToken = sharedPreferences.getString(AppUtils.KEY_ACCESS_TOKEN, "");
        String quote_category = category.getSelectedItem().toString();
        String quote_desc = editTextQuoteDesc.getText().toString();
        AsyncHttpClient client = new AsyncHttpClient();
        final RequestParams params = new RequestParams();
        params.put("email", email);
        params.put("access_token", accessToken);
        params.put("quote_category", quote_category);
        params.put("quote_desc", quote_desc);
        try {
            jsonObjectAddress.put("full_address", editTextAddress.getText().toString());
        }catch (Exception e)
        {

        }
        params.put("address",jsonObjectAddress);

        params.put("availability",jsonArrayAvailability.toString());
        client.post(url, params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                pDialog.show();

            }


            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                pDialog.dismiss();
                String response = new String(responseBody);
                Log.d("Debug", response);
                // Parse JSON data
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject jsonData = jsonObject.getJSONObject("data");

                    String mrt_status = jsonData.getString("mrt_status");

                    if (mrt_status.equals("1007")) {
                        // Close the RequestQuoteScreen on success
                        finish();
                    } else {
                        String mrt_desc = jsonData.getString("mrt_desc");
                        new AlertDialog.Builder(PostJobActivity.this)
                                .setTitle("Error")
                                .setMessage(mrt_desc)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .show();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(PostJobActivity.this)
                            .setTitle("Server Error")
                            .setMessage("Sorry ! Could not get data from server.")
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
                new AlertDialog.Builder(PostJobActivity.this)
                        .setTitle("Server Error")
                        .setMessage("Could not request quote at the moment.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_request_quote_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_done) {
            postNewJob();
            return true;
        }
        else {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
