package au.com.appscore.mrtradie;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import au.com.appscore.mrtradie.FacebookUtils.FaceBookUtils;
import au.com.appscore.mrtradie.FacebookUtils.FriendList;
import au.com.appscore.mrtradie.Search.Activity.CustomerSearchActivity;
import au.com.appscore.mrtradie.activity.PostJobActivity;
import au.com.appscore.mrtradie.utils.ControlPraser;

public class BusinessList extends AppCompatActivity {

    private Toolbar toolbar;
    private ListView listViewBusinesses;

    String url;

    SharedPreferences sharedPreferences;

    private Button postJobButton;
    JSONArray jsonArrayBusinesses = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_list);

        sharedPreferences = getApplicationContext().getSharedPreferences(AppUtils.SharedPreferenceFileName,0);

        listViewBusinesses = (ListView) findViewById(R.id.listViewBusinesses);

        postJobButton = ControlPraser.PraserControl(this, R.id.post_job_button);
        postJobButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PostJobActivity.class);
                startActivity(intent);
            }
        });

        // Initializing Toolbar and setting it as the actionbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (sharedPreferences.getString(AppUtils.KEY_USER_TYPE,"").equals("1"))
        {
            // Open Customer profile screen
            LinearLayout layout = ControlPraser.PraserControl(this, R.id.job_request_title);
            layout.setVisibility(View.GONE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.PrimaryDarkColor));
        }

        handleIntent(getIntent());

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        FaceBookUtils faceBookUtils = new FaceBookUtils();
        if(faceBookUtils.getLoginStatus())
        {

        }
        handleIntent(intent);
    }


    private void queryFacebook(Intent intent)
    {
        FaceBookUtils faceBookUtils = new FaceBookUtils();
        FriendList friendList = faceBookUtils.getFriendList();

    }

    // Method to handle search intent
    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            // use entered text to query backend server for matching tradies
            String url = AppUtils.getSearchProfileTradieURLForQuery(sharedPreferences.getString(AppUtils.KEY_EMAIL,null),sharedPreferences.getString(AppUtils.KEY_ACCESS_TOKEN,null),query);
            queryServer(url);
        }
        else {
            // Not a search intent, perform occupation search
            if (intent.getBooleanExtra("occupation_search",false))
            {
                String url = AppUtils.getSearchProfileTradieURLForOccupation(sharedPreferences.getString(AppUtils.KEY_EMAIL, null), sharedPreferences.getString(AppUtils.KEY_ACCESS_TOKEN, null), intent.getStringExtra("occupation"),-1, intent.getDoubleExtra("latitude",0), intent.getDoubleExtra("longitude",0));
                queryServer(url);
            }
            if (intent.getBooleanExtra("occupation_button_clicked",false))
            {
                String url = AppUtils.getSearchProfileTradieURLForOccupation(sharedPreferences.getString(AppUtils.KEY_EMAIL, null), sharedPreferences.getString(AppUtils.KEY_ACCESS_TOKEN, null), "",intent.getIntExtra("category",-1), intent.getDoubleExtra("latitude",0), intent.getDoubleExtra("longitude",0));
                queryServer(url);
            }
        }
    }

    // Method to query server for tradies
    private void queryServer(String url) {
        String tag_cancel_search = "cancel_search";             // Tag used to cancel the request

        this.url = url;
        url = url.replaceAll("\\s","%20");

        final ProgressDialog pDialog = new ProgressDialog(BusinessList.this);
        pDialog.setMessage("Finding tradies...");
        pDialog.setCancelable(false);
        pDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                pDialog.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject jsonData = jsonObject.getJSONObject("data");

                    String mrt_status = jsonData.getString("mrt_status");

                    // Check if user is authenticated
                    if (mrt_status.equals("1000"))
                    {
                        // Authenticated
                        // Retrieve businesses from response and set list view adapter for the list
                        jsonArrayBusinesses = jsonData.getJSONArray("list");
                        final ArrayList<Business> businesses = new ArrayList<>();

                        for (int i=0;i<jsonArrayBusinesses.length();i++)
                        {
                            JSONObject jsonObjectBusiness = jsonArrayBusinesses.getJSONObject(i);
                            JSONObject jsonObjectAddress = jsonObjectBusiness.getJSONObject("address");
                            businesses.add(new Business(jsonObjectBusiness.getString("company_name"),jsonObjectAddress.getString("full_address"),jsonObjectAddress.getString("street"),jsonObjectAddress.getString("city"),jsonObjectAddress.getString("state"),jsonObjectAddress.getString("post_code"),jsonObjectAddress.getString("country"),jsonObjectAddress.getString("latitude"),jsonObjectAddress.getString("longitude"),jsonObjectBusiness.getString("company_logo"),jsonObjectBusiness.getString("total_stars"),jsonObjectBusiness.getString("about"),jsonObjectBusiness.getJSONArray("reviews").toString(),jsonObjectBusiness.getString("email"),jsonObjectBusiness.getString("website"),jsonObjectBusiness.getString("contact_num"),jsonObjectBusiness.getString("photo1"),jsonObjectBusiness.getString("photo2"),jsonObjectBusiness.getString("photo3")));
                        }

                        BusinessListAdapter businessListAdapter = new BusinessListAdapter(BusinessList.this,businesses);

                        listViewBusinesses.setAdapter(businessListAdapter);

                        listViewBusinesses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Intent intent = new Intent(BusinessList.this,BusinessDetail.class);
                                intent.putExtra("BusinessDetail",businesses.get(position));
                                startActivity(intent);
                            }
                        });


                    }
                    else {
                        // Not authenticated
                        String mrt_desc = jsonData.getString("mrt_desc");
                        new AlertDialog.Builder(BusinessList.this)
                                .setTitle("No Tradies Available")
                                .setMessage("Sorry. As you can see there are no tradies currently registered for this trade. Do you know a tradie for this trade? Refer them to Mr.Tradie we will get them onboard. Otherwise please try again shortly.")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(BusinessList.this)
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
                new AlertDialog.Builder(BusinessList.this)
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

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(stringRequest, tag_cancel_search);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_business_list, menu);

        // Associate searchable configuration with the SearchView
//        SearchManager searchManager =
//                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        SearchView searchView =
//                (SearchView) menu.findItem(R.id.action_search).getActionView();
//        searchView.setSearchableInfo(
//        searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_map) {
            Intent intent = new Intent(BusinessList.this,BusinessesMap.class);
            // If list it not empty, send it over to map screen with intent
            if (jsonArrayBusinesses!=null)
            {
                intent.putExtra("BusinessesList",jsonArrayBusinesses.toString());
            }
            startActivity(intent);
            return true;
        }

        if(id == R.id.action_search)
        {
            Intent intent = new Intent(this, CustomerSearchActivity.class);
            intent.putExtra("intent", getIntent());
            intent.putExtra("url", url);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
