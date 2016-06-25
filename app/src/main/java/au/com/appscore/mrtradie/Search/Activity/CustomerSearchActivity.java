package au.com.appscore.mrtradie.Search.Activity;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import au.com.appscore.mrtradie.AppController;
import au.com.appscore.mrtradie.AppUtils;
import au.com.appscore.mrtradie.Business;
import au.com.appscore.mrtradie.BusinessDetail;
import au.com.appscore.mrtradie.R;
import au.com.appscore.mrtradie.Search.Item.SearchItemAdapter;
import au.com.appscore.mrtradie.utils.ControlPraser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CustomerSearchActivity extends AppCompatActivity {

    SearchView searchView;
    TabLayout tabLayout;
    ListView  listView;

    SharedPreferences sharedPreferences;
    static Intent passedIntent;
    ArrayList<Business> businesses;
    static String url;
    static boolean initialised;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_search);
        searchView = ControlPraser.PraserControl(this, R.id.search_view_search);
        tabLayout = ControlPraser.PraserControl(this, R.id.quotes_tab);
        listView = ControlPraser.PraserControl(this, R.id.quote_search_list_view);
        sharedPreferences = getApplicationContext().getSharedPreferences(AppUtils.SharedPreferenceFileName, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.PrimaryDarkColor));
        }

        if(null != getIntent().getStringExtra("url") || null != getIntent().getParcelableExtra("intent"))
            initialised = false;
        if(!initialised){
            url = getIntent().getStringExtra("url");
            passedIntent = getIntent().getParcelableExtra("intent");
        }
        tabLayout.addTab(tabLayout.newTab().setText("DISTANCE"));
        tabLayout.addTab(tabLayout.newTab().setText("NAME"));
        tabLayout.addTab(tabLayout.newTab().setText("FEEDBACK SCORE"));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (businesses == null)
                    return;

                SearchItemAdapter searchItemAdapter;
                switch (tab.getText().toString()) {
                    case "NAME":
                        searchItemAdapter = new SearchItemAdapter(businesses, getApplicationContext(), 1);
                        break;
                    case "DISTANCE":
                        searchItemAdapter = new SearchItemAdapter(businesses, getApplicationContext(), 2);
                        break;
                    case "FEEDBACK SCORE":
                        searchItemAdapter = new SearchItemAdapter(businesses, getApplicationContext(), 3);
                        break;
                    default:
                        searchItemAdapter = new SearchItemAdapter(businesses, getApplicationContext());
                }

                listView.setAdapter(null);
                listView.setAdapter(searchItemAdapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(CustomerSearchActivity.this, BusinessDetail.class);
                        intent.putExtra("BusinessDetail", businesses.get(position));
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = null;
                try {
                    view = ControlPraser.GetAttributeByName(searchView, "mSearchButton");
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                view.callOnClick();
            }
        });
        if(initialised)
        {
            String query = getIntent().getStringExtra(SearchManager.QUERY);
            passedIntent.removeExtra(SearchManager.QUERY);
            passedIntent.putExtra(SearchManager.QUERY, query);
            handleIntent(passedIntent);
            return;
        }
        queryServer(url);
        initialised = true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String query = intent.getStringExtra(SearchManager.QUERY);
        passedIntent.removeExtra(SearchManager.QUERY);
        passedIntent.putExtra(SearchManager.QUERY, query);
        handleIntent(passedIntent);
    }

    private void handleIntent(Intent intent) {
        // Not a search intent, perform occupation search
        String query = intent.getStringExtra(SearchManager.QUERY);
        // use entered text to query backend server for matching tradies
        String url = AppUtils.getSearchProfileTradieURLForQuery(sharedPreferences.getString(AppUtils.KEY_EMAIL,null),sharedPreferences.getString(AppUtils.KEY_ACCESS_TOKEN,null),query);
        queryServer(url);
    }

    private void queryServer(String url)
    {
        String tag_cancel_search = "cancel_search";             // Tag used to cancel the request
        url = url.replaceAll("\\s","%20");

        final ProgressDialog pDialog = new ProgressDialog(CustomerSearchActivity.this);
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
                    listView.setAdapter(null);
                    // Check if user is authenticated
                    if (mrt_status.equals("1000"))
                    {
                        // Authenticated
                        // Retrieve businesses from response and set list view adapter for the list
                        JSONArray jsonArrayBusinesses = jsonData.getJSONArray("list");
                        businesses = new ArrayList<>();

                        for (int i=0;i<jsonArrayBusinesses.length();i++)
                        {
                            JSONObject jsonObjectBusiness = jsonArrayBusinesses.getJSONObject(i);
                            JSONObject jsonObjectAddress = jsonObjectBusiness.getJSONObject("address");

                            businesses.add(new Business(jsonObjectBusiness.getString("company_name"),jsonObjectAddress.getString("full_address"),jsonObjectAddress.getString("street"),jsonObjectAddress.getString("city"),jsonObjectAddress.getString("state"),jsonObjectAddress.getString("post_code"),jsonObjectAddress.getString("country"),jsonObjectAddress.getString("latitude"),jsonObjectAddress.getString("longitude"),jsonObjectBusiness.getString("company_logo"),jsonObjectBusiness.getString("total_stars"),jsonObjectBusiness.getString("about"),jsonObjectBusiness.getJSONArray("reviews").toString(),jsonObjectBusiness.getString("email"),jsonObjectBusiness.getString("website"),jsonObjectBusiness.getString("contact_num"),jsonObjectBusiness.getString("photo1"),jsonObjectBusiness.getString("photo2"),jsonObjectBusiness.getString("photo3")));
                        }

                        SearchItemAdapter businessListAdapter = new SearchItemAdapter(businesses, getApplicationContext());

                        listView.setAdapter(businessListAdapter);

                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Intent intent = new Intent(CustomerSearchActivity.this,BusinessDetail.class);
                                intent.putExtra("BusinessDetail",businesses.get(position));
                                startActivity(intent);
                            }
                        });

                    }
                    else {
                        // Not authenticated
                        String mrt_desc = jsonData.getString("mrt_desc");
                        new AlertDialog.Builder(CustomerSearchActivity.this)
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
                    new AlertDialog.Builder(CustomerSearchActivity.this)
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
                new AlertDialog.Builder(CustomerSearchActivity.this)
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
        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

}
