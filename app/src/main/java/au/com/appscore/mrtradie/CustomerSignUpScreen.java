package au.com.appscore.mrtradie;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import au.com.appscore.mrtradie.FacebookUtils.FaceBookUtils;

public class CustomerSignUpScreen extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private Location mLastLocation;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    private EditText editTextAddress,editTextFullName,editTextEmail,editTextPassword,editTextContact;
    private ImageButton imageButtonAddress,imageButtonJoinAsCustomer;

    private JSONObject jsonObjectAddress = null;

    SharedPreferences sharedPreferences;
    ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_sign_up_screen);

        pDialog = new ProgressDialog(CustomerSignUpScreen.this);
        pDialog.setMessage("Signing Up...");
        sharedPreferences = getApplicationContext().getSharedPreferences(AppUtils.SharedPreferenceFileName, 0);
        // Initialise Elements
        editTextFullName = (EditText) findViewById(R.id.editTextFullName);
        editTextEmail = (EditText) findViewById(R.id.editTextEmailAddress);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        editTextContact = (EditText) findViewById(R.id.editTextContact);
        editTextAddress = (EditText) findViewById(R.id.editTextAddress);
        imageButtonAddress = (ImageButton) findViewById(R.id.imageButtonAddress);
        imageButtonJoinAsCustomer = (ImageButton) findViewById(R.id.imageButtonJoinAsCustomer);

        imageButtonJoinAsCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Debug","Perform network operation");
                // Hide Keyboard
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);

                inputManager.hideSoftInputFromWindow((null == getCurrentFocus()) ? null : getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                if (allFieldsValid())
                {
                    joinAsCustomer();
                }
                else {
                    new AlertDialog.Builder(CustomerSignUpScreen.this)
                            .setTitle("Validation")
                            .setMessage("Please fill in all fields and ensure email address, password and contact number is valid.")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                }

            }
        });

        imageButtonAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Debug","Retrieving location again");
                displayLocation();
            }
        });


        if(sharedPreferences.getBoolean(AppUtils.KEY_LOGGEDIN_USING_FB, false))
        {
            editTextFullName.setText(sharedPreferences.getString(AppUtils.KEY_FULL_NAME, ""));
            editTextEmail.setText(sharedPreferences.getString(AppUtils.KEY_EMAIL, ""));
            editTextPassword.setText(new FaceBookUtils().getFriendList().getId());
            editTextEmail.setEnabled(false);
            editTextPassword.setEnabled(false);
            editTextFullName.setEnabled(false);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.StatusBarColorLoginScreen));
        }

        // First we need to check availability of play services
        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();
        }
    }

    // Method to validate all the fields before submitting
    private Boolean allFieldsValid() {
        boolean result = true;
        // Check if any field is left blank, entered email address and password is valid
        if (TextUtils.isEmpty(editTextFullName.getText())||TextUtils.isEmpty(editTextEmail.getText())||TextUtils.isEmpty(editTextPassword.getText())||TextUtils.isEmpty(editTextAddress.getText())||TextUtils.isEmpty(editTextContact.getText()))
            result = false;
        else if (!AppUtils.isValidEmail(editTextEmail.getText())||editTextPassword.getText().toString().length()<6||editTextContact.getText().toString().length()<10)
            result = false;

        return result;
    }

    // Method to perform network operation for customer join operation
    private void joinAsCustomer() {

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        params.put("email",editTextEmail.getText().toString());
        params.put("full_name",editTextFullName.getText().toString());
        params.put("password",editTextPassword.getText().toString());
        params.put("contact_num",editTextContact.getText().toString());
        if (jsonObjectAddress!=null)
            params.put("address",jsonObjectAddress);
        params.put("device_type","android");
        params.put("device_token", AppUtils.REG_TOKEN);
        params.put("user_type", 0);

        if(sharedPreferences.getBoolean(AppUtils.KEY_LOGGEDIN_USING_FB, false))
        {
            params.put("access_token", sharedPreferences.getString(AppUtils.KEY_ACCESS_TOKEN, ""));
        }

        String api = sharedPreferences.getBoolean(AppUtils.KEY_LOGGEDIN_USING_FB, false)?"profile/update":"profile/add";
        client.post(AppUtils.baseURL + api, params, new AsyncHttpResponseHandler() {

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
                Log.d("Debug", response);
                // Parse JSON data
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject jsonData = jsonObject.getJSONObject("data");

                    String mrt_status = jsonData.getString("mrt_status");

                    if (mrt_status.equals("1007")||mrt_status.equals("1009")) {
                        // Customer successfully registered
                        // Store user's info into shared preferences
                        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(AppUtils.SharedPreferenceFileName, 0);
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        editor.putBoolean(AppUtils.KEY_IS_LOGGEDIN, true);
                        editor.putString(AppUtils.KEY_EMAIL, editTextEmail.getText().toString());
                        if(!sharedPreferences.getBoolean(AppUtils.KEY_LOGGEDIN_USING_FB, false))
                            editor.putString(AppUtils.KEY_ACCESS_TOKEN, jsonData.getString("access_token"));
                        editor.putString(AppUtils.KEY_USER_TYPE, "0");
                        editor.putString(AppUtils.KEY_FULL_NAME, editTextFullName.getText().toString());

                        editor.apply();


                        // Open main screen
                        Intent intent = new Intent(CustomerSignUpScreen.this, MainScreen.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra("go_to_tips",true);
                        startActivity(intent);
                        finish();
                    } else {
                        String mrt_desc = jsonData.getString("mrt_desc");
                        new AlertDialog.Builder(CustomerSignUpScreen.this)
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
                    new AlertDialog.Builder(CustomerSignUpScreen.this)
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
                new AlertDialog.Builder(CustomerSignUpScreen.this)
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

    /**
     * Method to display the location on UI
     * */
    private void displayLocation() {

        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            jsonObjectAddress = new JSONObject();


            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

            // Add lat and long to json object of address
            try {
                jsonObjectAddress.put("latitude",latitude);
                jsonObjectAddress.put("longitude",longitude);
            } catch (JSONException e) {
                e.printStackTrace();
            }


            Log.d("Debug","Latitude : "+latitude+" Longitude "+longitude);

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
                jsonObjectAddress.put("full_address",address+" "+city+" "+state+" "+postalCode);
                jsonObjectAddress.put("street",address);
                jsonObjectAddress.put("city",city);
                jsonObjectAddress.put("state", state);
                jsonObjectAddress.put("post_code", postalCode);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }


        } else {

            Log.d("Debug","Could not get current location");
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_customer_sign_up_screen, menu);
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
}
