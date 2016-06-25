package au.com.appscore.mrtradie;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.facebook.login.LoginManager;
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

import java.util.HashMap;
import java.util.Map;

import au.com.appscore.mrtradie.FacebookUtils.FaceBookUtils;
import au.com.appscore.mrtradie.FacebookUtils.FacebookFriendActivity;
import au.com.appscore.mrtradie.Jobboard.JobboardActivity;
import au.com.appscore.mrtradie.activity.Premium;
import au.com.appscore.mrtradie.utils.ControlPraser;

public class MainScreen extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private Location mLastLocation;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    private double latitude = 0;
    private double longitude = 0;

    //Defining Variables
    private Menu menu;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ImageView premiumImage;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        // First we need to check availability of play services
        if (checkPlayServices()) {
            // Building the GoogleApi client
            buildGoogleApiClient();
        }

        FaceBookUtils faceBookUtils = new FaceBookUtils();


        sharedPreferences = getApplicationContext().getSharedPreferences(AppUtils.SharedPreferenceFileName,0);
        Log.d("Save this", sharedPreferences.getString(AppUtils.KEY_ACCESS_TOKEN, "dummy"));
        if(sharedPreferences.getBoolean(AppUtils.KEY_LOGGEDIN_USING_FB, false)) {
            faceBookUtils = new FaceBookUtils(true);
        }
        else{
            faceBookUtils = new FaceBookUtils();
        }
        // Initializing Toolbar and setting it as the actionbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initializing NavigationView
        navigationView = (NavigationView) findViewById(R.id.navigation_view);

        if (sharedPreferences.getString(AppUtils.KEY_USER_TYPE,"").equals("0"))
        {
            // Open Customer profile screen
            navigationView.inflateMenu(R.menu.customer_drawer);
            navigationView.getMenu().getItem(3).setEnabled(false);
            if(faceBookUtils.getLoginStatus())
                navigationView.getMenu().getItem(3).setEnabled(true);
        }
        else if (sharedPreferences.getString(AppUtils.KEY_USER_TYPE,"").equals("1"))
        {
            // Open Tradie profile screen
            navigationView.inflateMenu(R.menu.drawer);
        }

        View headerView = navigationView.findViewById(R.id.headerView);
        premiumImage = ControlPraser.PraserControl(headerView, R.id.imageViewPremium);
        //add by Jiazhou for enabling and disabling the facebook list
        //MenuItem fbView = navigationView.getMenu().findItem(R.id.fbFriends);
        //fbView.setEnabled(faceBookUtils.getLoginStatus());

        TextView textViewFullName = (TextView) headerView.findViewById(R.id.textViewFullName);
        TextView textViewEmailAddress = (TextView) headerView.findViewById(R.id.textViewEmailAddress);
        ImageButton imageButtonSettings = (ImageButton) headerView.findViewById(R.id.settingsButton);

        imageButtonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open customer or tradie profile screen accordingly
                if (sharedPreferences.getString(AppUtils.KEY_USER_TYPE,"").equals("0"))
                {
                    // Open Customer profile screen
                    Intent intent = new Intent(MainScreen.this,CustomerProfileScreen.class);
                    startActivity(intent);
                }
                else if (sharedPreferences.getString(AppUtils.KEY_USER_TYPE,"").equals("1"))
                {
                    // Open Tradie profile screen
                    Intent intent = new Intent(MainScreen.this,TradieProfileScreen.class);
                    startActivity(intent);
                }
            }
        });

        textViewFullName.setText(sharedPreferences.getString(AppUtils.KEY_FULL_NAME,""));
        textViewEmailAddress.setText(sharedPreferences.getString(AppUtils.KEY_EMAIL,""));


        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {


                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) menuItem.setChecked(false);
                else menuItem.setChecked(true);

                //Closing drawer on item click
                drawerLayout.closeDrawers();

                android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    case R.id.customer_facebook:
                        FaceBookUtils faceBookUtils = new FaceBookUtils();
                        Intent intent = new Intent(MainScreen.this, FacebookFriendActivity.class);
                        intent.putExtra("emails", faceBookUtils.getFriendList().getFriends());
                        startActivity(intent);
                        return true;

                    case R.id.customer_tips:
                        setTitle("Tips");
                        hideSearchItem();
                        TipsFragment tipsFragment = new TipsFragment();
                        fragmentTransaction.replace(R.id.frame,tipsFragment);
                        fragmentTransaction.commit();
                        return true;

                    case R.id.jobboard:
                        Intent intentj  = new Intent(MainScreen.this, JobboardActivity.class);
                        startActivity(intentj);
                        return true;
                    case R.id.premium:
                        Intent intentp  = new Intent(MainScreen.this, Premium.class);
                        startActivity(intentp);
                        return true;

                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.service:case R.id.customer_quote:
                        // Toast.makeText(getApplicationContext(), "Inbox Selected", Toast.LENGTH_SHORT).show();
                        Log.d("Debug", "Choose service fragment");
                        setTitle("Choose Service");
                        showSearchItem();
                        ContentFragment fragment = new ContentFragment();
                        Bundle bundle = new Bundle();
                        bundle.putDouble("latitude", latitude);
                        bundle.putDouble("longitude", longitude);
                        fragment.setArguments(bundle);
                        fragmentTransaction.replace(R.id.frame, fragment);
                        fragmentTransaction.commit();
                        return true;

                    // For rest of the options we just show a toast on click

                    case R.id.jobs:case R.id.customer_jobs:
                        //Toast.makeText(getApplicationContext(),"Stared Selected",Toast.LENGTH_SHORT).show();
                        setTitle("My Jobs");
                        hideSearchItem();
                        MyJobsFragment myJobsFragment = new MyJobsFragment();
                        fragmentTransaction.replace(R.id.frame, myJobsFragment);
                        fragmentTransaction.commit();
                        return true;
                    case R.id.quotes:case R.id.customer_my_quotes:
                        // Toast.makeText(getApplicationContext(),"Send Selected",Toast.LENGTH_SHORT).show();
                        setTitle("My Quotes");
                        hideSearchItem();
                        MyQuotesFragment myQuotesFragment = new MyQuotesFragment();
                        fragmentTransaction.replace(R.id.frame, myQuotesFragment);
                        fragmentTransaction.commit();
                        return true;
//                    case R.id.tips:
//                        setTitle("Tips");
//                        hideSearchItem();
//                        TipsFragment tipsFragment = new TipsFragment();
//                        fragmentTransaction.replace(R.id.frame,tipsFragment);
//                        fragmentTransaction.commit();
//                        return true;
                    case R.id.logout:
                        //Toast.makeText(getApplicationContext(),"Logout",Toast.LENGTH_SHORT).show();
                        // Logout the user
                        // Check if the user is logged in using fb or backend api
                        final SharedPreferences.Editor editor = sharedPreferences.edit();
//                        if (sharedPreferences.getBoolean(AppUtils.KEY_LOGGEDIN_USING_FB,false))
//                        {
//                            Log.d("LogoutDebug####",sharedPreferences.getString(AppUtils.KEY_EMAIL,"dummy"));
//                            Log.d("LogoutDebug####",sharedPreferences.getString(AppUtils.KEY_FULL_NAME,"dummy"));
//                            LoginManager.getInstance().logOut();
//
//                            editor.clear();
//                            editor.apply();
//
//                            // Open login screen
//                            Intent intent = new Intent(MainScreen.this, LoginScreen.class);
//                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                            startActivity(intent);
//                            finish();
//
//                        }
//                        else {

                        // Tag used to cancel the request
                        String tag_json_obj = "json_obj_req";

                        String url = AppUtils.baseURL + "logout";

                        final ProgressDialog pDialog = new ProgressDialog(MainScreen.this);
                        pDialog.setMessage("Logging Out...");
                        pDialog.setCancelable(false);
                        pDialog.show();

                        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                pDialog.dismiss();
                                // Parse JSON data
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    JSONObject jsonData = jsonObject.getJSONObject("data");

                                    String mrt_status = jsonData.getString("mrt_status");

                                    // Check if user is logged out
                                    if (mrt_status.equals("1022")) {
                                        // Logged out
                                        // Clear user info from shared preferences

                                        // Logout from FB
                                        if (sharedPreferences.getBoolean(AppUtils.KEY_LOGGEDIN_USING_FB, false))
                                            LoginManager.getInstance().logOut();


                                        editor.clear();
                                        editor.apply();
                                        // Open login screen
                                        Intent intent = new Intent(MainScreen.this, LoginScreen.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();

                                    } else {
                                        // Not logged out
                                        new AlertDialog.Builder(MainScreen.this)
                                                .setTitle("Validation")
                                                .setMessage("Could not logout the user.")
                                                .setCancelable(false)
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {

                                                    }
                                                })
                                                .show();
                                    }


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    new AlertDialog.Builder(MainScreen.this)
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
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                pDialog.dismiss();
                                new AlertDialog.Builder(MainScreen.this)
                                        .setTitle("Validation")
                                        .setMessage("Please check your internet connection.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        })
                                        .show();

                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("email", sharedPreferences.getString(AppUtils.KEY_EMAIL, "abc@abc.com"));
                                params.put("access_token", sharedPreferences.getString(AppUtils.KEY_ACCESS_TOKEN, "dummyToken"));

                                return params;
                            }
                        };

                        // Adding request to request queue
                        AppController.getInstance().addToRequestQueue(stringRequest, tag_json_obj);

                        //}


                        return true;
                    default:
                        Toast.makeText(getApplicationContext(), "Somethings Wrong", Toast.LENGTH_SHORT).show();
                        return true;

                }
            }
        });

        // Initializing Drawer Layout and ActionBarToggle
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.openDrawer, R.string.closeDrawer){

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank

                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();

        // Display Get a quote screen by default
        if(!getIntent().getBooleanExtra("go_to_tips", false)) {
            ContentFragment fragment = new ContentFragment();
            Bundle bundle = new Bundle();
            bundle.putDouble("latitude", latitude);
            bundle.putDouble("longitude", longitude);
            fragment.setArguments(bundle);
            android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, fragment);
            fragmentTransaction.commit();
        } else {
            android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            setTitle("Tips");
            TipsFragment tipsFragment = new TipsFragment();
            fragmentTransaction.replace(R.id.frame,tipsFragment);
            fragmentTransaction.commit();
        }
        setPremium();
    }

    /**
     * Method to update the location
     * */
    private void updateLocation() {

        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();

            Log.d("Debug","Latitude : "+latitude+" Longitude "+longitude);

        } else {

            Log.d("Debug", "Could not get current location");
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
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_main_screen, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        if(getIntent().getBooleanExtra("go_to_tips", false)){
            hideSearchItem();
        }
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

    private void hideSearchItem() {
        MenuItem item = menu.findItem(R.id.action_search);
        item.setVisible(false);
    }

    private void showSearchItem() {
        MenuItem item = menu.findItem(R.id.action_search);
        item.setVisible(true);
    }

    @Override
    public void onConnected(Bundle bundle) {
        updateLocation();
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

    private void setPremium()
    {
        String email  = sharedPreferences.getString(AppUtils.KEY_EMAIL,"");
        String token = sharedPreferences.getString(AppUtils.KEY_ACCESS_TOKEN,"");
        String URL = AppUtils.getPremiumUrl();
        AsyncHttpClient client = new AsyncHttpClient();
        final RequestParams params = new RequestParams();
        params.put("email", email);
        params.put("access_token", token);
        params.put("premium", 0);
        params.put("listing", 0);

        client.post(URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    JSONObject json = new JSONObject(new String(responseBody));
                    JSONObject data = json.getJSONObject("data");
                    boolean premium = data.getBoolean("premium_expired");
                    if (premium)
                        premiumImage.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        setPremium();
                    }
                });
                thread.start();
                return;
            }
        });

    }
}
