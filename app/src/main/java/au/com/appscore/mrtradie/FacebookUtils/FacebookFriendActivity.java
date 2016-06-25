package au.com.appscore.mrtradie.FacebookUtils;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import au.com.appscore.mrtradie.AppController;
import au.com.appscore.mrtradie.AppUtils;
import au.com.appscore.mrtradie.Business;
import au.com.appscore.mrtradie.BusinessDetail;
import au.com.appscore.mrtradie.BusinessListAdapter;
import au.com.appscore.mrtradie.R;
import au.com.appscore.mrtradie.utils.ControlPraser;

public class FacebookFriendActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    JSONArray jsonArrayBusinesses = null;
    ListView listViewBusinesses;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook_friend);
        List<String> ids = new FaceBookUtils().getFriendList().getFriends();
        listViewBusinesses = ControlPraser.PraserControl(this, R.id.facebookList);
        //ArrayList<String> emails = (ArrayList)intent.getSerializableExtra("emails");
        sharedPreferences = getApplicationContext().getSharedPreferences(AppUtils.SharedPreferenceFileName,0);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        sharedPreferences = getApplicationContext().getSharedPreferences(AppUtils.SharedPreferenceFileName, 0);
        setTitle("Facebook Tradies");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.PrimaryDarkColor));
        }

        queryServer(ids);
    }

    private void queryServer(List<String> ids)
    {
        if(ids.size() == 0)
        {
            new AlertDialog.Builder(FacebookFriendActivity.this)
                    .setTitle("Error")
                    .setMessage("No Facebook friend found!")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
            return;
        }


        final ProgressDialog pDialog = new ProgressDialog(FacebookFriendActivity.this);
        String url  = AppUtils.getFacebookTradie(sharedPreferences.getString(AppUtils.KEY_EMAIL, ""), sharedPreferences.getString(AppUtils.KEY_ACCESS_TOKEN, ""), ids);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject jsonData = jsonObject.getJSONObject("data");
                    String mrt_status = jsonData.getString("mrt_status");
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

                        BusinessListAdapter businessListAdapter = new BusinessListAdapter(FacebookFriendActivity.this,businesses);

                        listViewBusinesses.setAdapter(businessListAdapter);

                        listViewBusinesses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Intent intent = new Intent(FacebookFriendActivity.this,BusinessDetail.class);
                                intent.putExtra("BusinessDetail",businesses.get(position));
                                startActivity(intent);
                            }
                        });


                    }
                    else {
                        // Not authenticated
                        String mrt_desc = jsonData.getString("mrt_desc");
                        new AlertDialog.Builder(FacebookFriendActivity.this)
                                .setTitle("Error")
                                .setMessage(mrt_desc)
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .show();
                    }
                }catch (Exception e)
                {
                    e.printStackTrace();
                    new AlertDialog.Builder(FacebookFriendActivity.this)
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
                new AlertDialog.Builder(FacebookFriendActivity.this)
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
        AppController.getInstance().addToRequestQueue(stringRequest, "cancel_search");
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        finish();
        return super.onOptionsItemSelected(item);
    }
}
