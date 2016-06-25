package au.com.appscore.mrtradie;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class CustomerProfileScreen extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    private ProgressDialog pDialog;
    private SharedPreferences sharedPreferences;
    private EditText editTextFullName, editTextEmailAddress, editTextPassword, editTextFullAddress, editTextContactNumber;
    private JSONObject jsonObjectAddress = null;
    private String errorMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profile_screen);

        initialiseElements();

        getCustomerInfo();

        // First we need to check availability of play services
        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();
        }
    }

    // Method to initialise elements
    private void initialiseElements(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.StatusBarColorLoginScreen));
        }

        jsonObjectAddress = new JSONObject();

        sharedPreferences = getApplicationContext().getSharedPreferences(AppUtils.SharedPreferenceFileName,0);
        final boolean[] passwordCleared = {false};

        pDialog = new ProgressDialog(CustomerProfileScreen.this);


        editTextFullName = (EditText) findViewById(R.id.editTextFullName);
        editTextEmailAddress = (EditText) findViewById(R.id.editTextEmailAddress);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        editTextFullAddress = (EditText) findViewById(R.id.editTextAddress);
        editTextContactNumber = (EditText) findViewById(R.id.editTextContact);
        ImageButton imageButtonAddress = (ImageButton) findViewById(R.id.imageButtonAddress);
        Button imageButtonUpdateCustomer = (Button) findViewById(R.id.imageButtonUpdateCustomer);
        imageButtonAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayLocation();
            }
        });

        // Clearing password on pressing delete button for the first time
        editTextPassword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (keyCode == KeyEvent.KEYCODE_DEL&&!passwordCleared[0])
                {
                    passwordCleared[0] = true;
                    editTextPassword.setText("");
                    return true;
                }

                return false;
            }
        });

        imageButtonUpdateCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hide Keyboard
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);

                inputManager.hideSoftInputFromWindow((null == getCurrentFocus()) ? null : getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                if (isAllFieldsValid()) {
                    pDialog.show();
                    pDialog.setCancelable(false);
                    // Get lat long from address
                    try {
                        LatLng latLng = getLocationFromAddress(editTextFullAddress.getText().toString());
                        if (latLng != null) {
                            // Add lat long fields to json object of address
                            Log.d("Debug", latLng.toString());
                            jsonObjectAddress.put("latitude", latLng.latitude);
                            jsonObjectAddress.put("longitude", latLng.longitude);

                        }

                    } catch (Exception e) {
                        Log.d("Debug", e.toString());
                    }

                    // Make server call to update customer info
                    updateCustomer();
                } else {
                    new AlertDialog.Builder(CustomerProfileScreen.this)
                            .setTitle("Validation Error")
                            .setMessage(errorMsg)
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                }

            }
        });

        if(sharedPreferences.getBoolean(AppUtils.KEY_LOGGEDIN_USING_FB, false))
        {
            editTextFullName.setText(sharedPreferences.getString(AppUtils.KEY_FULL_NAME, ""));
            editTextEmailAddress.setText(sharedPreferences.getString(AppUtils.KEY_EMAIL, ""));
            editTextPassword.setText(sharedPreferences.getString(AppUtils.KEY_FULL_NAME, ""));
            editTextEmailAddress.setEnabled(false);
            editTextPassword.setEnabled(false);
            editTextFullName.setEnabled(false);
        }
    }

    // Get lat long from address
    public LatLng getLocationFromAddress(String strAddress) {

        Geocoder coder = new Geocoder(CustomerProfileScreen.this);
        List<Address> address;
        LatLng p1 = null;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng(location.getLatitude(), location.getLongitude() );

        } catch (Exception ex) {

            ex.printStackTrace();
        }

        return p1;
    }

    // Method to perform network operation for customer profile update
    private void updateCustomer() {
        pDialog.setMessage("Updating profile...");

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        params.put("email",editTextEmailAddress.getText().toString());
        params.put("full_name",editTextFullName.getText().toString());
        params.put("password",editTextPassword.getText().toString());
        params.put("contact_num",editTextContactNumber.getText().toString());
        try {
            jsonObjectAddress.put("full_address",editTextFullAddress.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        params.put("address",jsonObjectAddress);
        params.put("user_type", 0);
        params.put("access_token", sharedPreferences.getString(AppUtils.KEY_ACCESS_TOKEN, ""));


        client.post(AppUtils.baseURL + "profile/update", params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();


            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                pDialog.dismiss();
                String response = new String(responseBody);
                Log.d("Debug", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject jsonData = jsonObject.getJSONObject("data");

                    String mrt_status = jsonData.getString("mrt_status");

                    if (mrt_status.equals("1009")) {
                        // Record updated successfully
                        new AlertDialog.Builder(CustomerProfileScreen.this)
                                .setTitle("Request Successful")
                                .setMessage("Your profile has been updated successfully.")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                                .show();
                    } else {
                        new AlertDialog.Builder(CustomerProfileScreen.this)
                                .setTitle("Server Error")
                                .setMessage("Could not update profile.")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                pDialog.dismiss();
                new AlertDialog.Builder(CustomerProfileScreen.this)
                        .setTitle("Server Error")
                        .setMessage("Could not sign up.")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            }
        });
    }


        // Method to validate all the fields
    private boolean isAllFieldsValid() {
        boolean valid = true;
        errorMsg = "";
        if (TextUtils.isEmpty(editTextFullName.getText()))
        {
            errorMsg = "Please enter your full name";
            valid = false;
        }
        else if (!AppUtils.isValidEmail(editTextEmailAddress.getText()))
        {
            errorMsg = "Please enter a valid email address";
            valid = false;
        }
        else if (editTextPassword.getText().toString().length()<6)
        {
            errorMsg = "Password must be six characters long";
            valid = false;
        }
        else if (TextUtils.isEmpty(editTextFullAddress.getText()))
        {
            errorMsg = "Please enter your address";
            valid = false;
        }
        else if (TextUtils.isEmpty(editTextContactNumber.getText()))
        {
            errorMsg = "Please enter your contact number";
            valid = false;
        }

        return valid;
    }

    // Method to retrieve customer info from back-end
    private void getCustomerInfo() {
        pDialog.setMessage("Fetching information...");
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        params.put("email", sharedPreferences.getString(AppUtils.KEY_EMAIL, ""));
        params.put("access_token", sharedPreferences.getString(AppUtils.KEY_ACCESS_TOKEN, ""));
        Log.d("Debug", "Email :" + sharedPreferences.getString(AppUtils.KEY_EMAIL, "") + " Access Token :" + sharedPreferences.getString(AppUtils.KEY_ACCESS_TOKEN, ""));


        client.get(AppUtils.baseURL + "profile/info", params, new AsyncHttpResponseHandler() {

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

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject jsonObjectData = jsonObject.getJSONObject("data");
                    JSONObject jsonObjectAddress = jsonObjectData.getJSONObject("address");

                    if (jsonObjectData.getInt("mrt_status") == 1000) {
                        editTextFullName.setText(jsonObjectData.getString("full_name"));
                        editTextEmailAddress.setText(jsonObjectData.getString("email"));
                        editTextPassword.setText(jsonObjectData.getString("password"));
                        editTextFullAddress.setText(jsonObjectAddress.getString("full_address"));
                        editTextContactNumber.setText(jsonObjectData.getString("contact_num"));
                    } else {
                        new AlertDialog.Builder(CustomerProfileScreen.this)
                                .setTitle("Server Error")
                                .setMessage("Could not retrieve information from server.")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                pDialog.dismiss();
                new AlertDialog.Builder(CustomerProfileScreen.this)
                        .setTitle("Server Error")
                        .setMessage("Could not retrieve information from server.")
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_customer_profile_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Method to verify google play services on the device
     * */
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

    /**
     * Method to display the location on UI
     * */
    private void displayLocation() {

        Location mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {



            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

            Log.d("Debug","Latitude : "+latitude+" Longitude "+longitude);

            // Getting address from latitude and longitude
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
                editTextFullAddress.setText(address+" "+city+" "+state+" "+postalCode);

            } catch (IOException e) {
                e.printStackTrace();
            }


        } else {
            Log.d("Debug","Could not get current location");
            new AlertDialog.Builder(CustomerProfileScreen.this)
                    .setTitle("Location Error")
                    .setMessage("Could not retrieve current location.")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkPlayServices();
    }

    @Override
    public void onConnected(Bundle bundle) {
        //displayLocation();
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
}
