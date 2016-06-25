package au.com.appscore.mrtradie;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class BusinessesMap extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private Location mLastLocation;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    private Toolbar toolbar;
    private ArrayList<Business> businesses = new ArrayList<>();
    private GoogleMap myMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_businesses_map);

        // Initializing Toolbar and setting it as the actionbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.PrimaryDarkColor));
        }

        // Placing marker on the map
        FragmentManager myFragmentManager = getSupportFragmentManager();
        SupportMapFragment mySupportMapFragment
                = (SupportMapFragment)myFragmentManager.findFragmentById(R.id.map);
        myMap = mySupportMapFragment.getMap();
        myMap.setMyLocationEnabled(true);

        // Retrieve businesses information
        String businessesListString = getIntent().getStringExtra("BusinessesList");
        if (businessesListString!=null)
        {
            try {
                JSONArray jsonArrayBusinesses = new JSONArray(businessesListString);
                for (int i=0;i<jsonArrayBusinesses.length();i++)
                {
                    JSONObject jsonObjectBusiness = jsonArrayBusinesses.getJSONObject(i);
                    JSONObject jsonObjectAddress = jsonObjectBusiness.getJSONObject("address");

                    businesses.add(new Business(jsonObjectBusiness.getString("company_name"),jsonObjectAddress.getString("full_address"),jsonObjectAddress.getString("street"),jsonObjectAddress.getString("city"),jsonObjectAddress.getString("state"),jsonObjectAddress.getString("post_code"),jsonObjectAddress.getString("country"),jsonObjectAddress.getString("latitude"),jsonObjectAddress.getString("longitude"),jsonObjectBusiness.getString("company_logo"),jsonObjectBusiness.getString("total_stars"),jsonObjectBusiness.getString("about"),jsonObjectBusiness.getJSONArray("reviews").toString(),jsonObjectBusiness.getString("email"),jsonObjectBusiness.getString("website"),jsonObjectBusiness.getString("contact_num"),jsonObjectBusiness.getString("photo1"),jsonObjectBusiness.getString("photo2"),jsonObjectBusiness.getString("photo3")));
                }

                for (Business business : businesses) {
                    if (!business.getLatitude().equals("null")&&!business.getLongitude().equals("null"))
                    {
                        myMap.addMarker(new MarkerOptions().position(new LatLng(Float.parseFloat(business.getLatitude()), Float.parseFloat(business.getLongitude())))
                                .snippet(business.getFullAddress())
                                .title(business.getName()))
                                .setIcon(BitmapDescriptorFactory.fromResource(R.drawable.yellow_marker));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        myMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Getting view from the layout file
                View v = getLayoutInflater().inflate(R.layout.map_infowindow_layout, null);

                TextView textViewCompanyName = (TextView) v.findViewById(R.id.textViewCompanyName);
                textViewCompanyName.setText(marker.getTitle());

                TextView textViewCompanyAddress = (TextView) v.findViewById(R.id.textViewCompanyAddress);
                textViewCompanyAddress.setText(marker.getSnippet());

                LinearLayout ratingBar = (LinearLayout) v.findViewById(R.id.ratingBar);
                Business business = businesses.get(Integer.parseInt(marker.getId().replaceAll("\\D+","")));
                if (!business.getRating().equals("null"))
                {
                    int ratingValue = (int) Float.parseFloat(business.getRating());
                    if (ratingValue<=5)
                    {
                        for (int i=0; i < ratingValue; i++){
                            ImageView r = (ImageView) ratingBar.getChildAt(i);
                            r.setImageResource(R.drawable.star_orange_full);
                        }
                    }

                }


                return v;
            }
        });

        myMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent intent = new Intent(BusinessesMap.this,BusinessDetail.class);
                intent.putExtra("BusinessDetail",businesses.get(Integer.parseInt(marker.getId().replaceAll("\\D+",""))));
                startActivity(intent);

            }
        });

        // First we need to check availability of play services
        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();
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

    /**
     * Method to display the location on UI
     * */
    private void zoomToCurrentLocation() {

        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

            Log.d("Debug","Latitude : "+latitude+" Longitude "+longitude);

            LatLng coordinate = new LatLng(latitude,longitude);
            CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 7);
            myMap.animateCamera(yourLocation);



        } else {

            Log.d("Debug","Could not get current location");
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_businesses_map, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_list) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        zoomToCurrentLocation();
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
