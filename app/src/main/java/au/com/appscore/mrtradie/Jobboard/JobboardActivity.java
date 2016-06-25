package au.com.appscore.mrtradie.Jobboard;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import au.com.appscore.mrtradie.AppController;
import au.com.appscore.mrtradie.AppUtils;
import au.com.appscore.mrtradie.BusinessesMap;
import au.com.appscore.mrtradie.Jobboard.SendApplication.SendApplicationActivity;
import au.com.appscore.mrtradie.Jsons.Jobboard.JobboardJson;
import au.com.appscore.mrtradie.R;
import au.com.appscore.mrtradie.utils.ControlPraser;

public class JobboardActivity extends AppCompatActivity {

    Toolbar toolbar;
    ListView listView;
    SharedPreferences sharedPreferences;
    ProgressDialog pDialog;
    JobboardJson jobboardJson;
    String userType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jobboard);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        listView = ControlPraser.PraserControl(this, R.id.jobboard_list);

        sharedPreferences = getApplicationContext().getSharedPreferences(AppUtils.SharedPreferenceFileName, 0);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userType = sharedPreferences.getString(AppUtils.KEY_USER_TYPE, "0");
        //testListView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.PrimaryDarkColor));
        }

        queryServer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.jobboard_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_map) {
            Intent intent = new Intent(JobboardActivity.this,BusinessesMap.class);
            startActivity(intent);
            return true;
        }

        if(id == R.id.action_search)
            return true;

        finish();

        return super.onOptionsItemSelected(item);
    }

    private void testListView()
    {
        JobboardAdaptor<JobboardItem> adaptor = new JobboardAdaptor<>();
        JobboardItem item1 = new JobboardItem(this, ControlPraser.GetAttr(getApplicationContext(), R.layout.jobboard_item));
        JobboardItem item2 = new JobboardItem(this, ControlPraser.GetAttr(getApplicationContext(), R.layout.jobboard_item));
        JobboardItem item3 = new JobboardItem(this, ControlPraser.GetAttr(getApplicationContext(), R.layout.jobboard_item));
        adaptor.add(item1);
        adaptor.add(item2);
        adaptor.add(item3);
        listView.setAdapter(adaptor);
    }

    private void setListView(final JobboardJson json)
    {
        JobboardAdaptor<JobboardItem> adaptor = new JobboardAdaptor<>();
        if (json.data.records == null || json.data.records.size() == 0)
            return;
        for(int i = 0; i < json.data.records.size(); i++)
        {
            JobboardItem item = new JobboardItem(this, ControlPraser.GetAttr(getApplicationContext(), R.layout.jobboard_item));
            String desc = json.data.records.get(i).quote.quote_desc;
            if(null == desc || desc.trim().length() == 0)
                desc = "No Description";
            item.setControl(desc, json.data.records.get(i).address.full_address);
            adaptor.add(item);
        }

        listView.setAdapter(null);
        listView.setAdapter(adaptor);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;
                if (userType.equals("0"))
                    intent = new Intent(getApplicationContext(), JobboardEntry.class);
                else
                    intent = new Intent(getApplicationContext(), SendApplicationActivity.class);
                intent.putExtra("sendApplicationData", json.data.records.get(position));
                startActivityForResult(intent, 1);
            }
        });
    }

    private void queryServer()
    {
        pDialog = new ProgressDialog(JobboardActivity.this);;
        final String email =  sharedPreferences.getString(AppUtils.KEY_EMAIL,null);
        final String accessToken = sharedPreferences.getString(AppUtils.KEY_ACCESS_TOKEN, null);
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
                String response = new String(responseBody);

                try {
                    pDialog.dismiss();
                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject jsonObjectData = jsonObject.getJSONObject("data");
                    JSONObject jsonObjectAddress = jsonObjectData.getJSONObject("address");

                    final String occupation = jsonObjectData.getString("occupation");
                    final String url = AppUtils.getJobboardsUrl(email, accessToken);
                    StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                pDialog.dismiss();
                                JobboardJson json = new Gson().fromJson(response, JobboardJson.class);

                                if (!json.meta.status.equals("200"))
                                    throw new Exception("Invalid json request status!");
                                jobboardJson = json;
                                setListView(json);
                            } catch (Exception e) {
                                e.printStackTrace();
                                new AlertDialog.Builder(JobboardActivity.this)
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
                    },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    pDialog.dismiss();
                                    new AlertDialog.Builder(JobboardActivity.this)
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

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                pDialog.dismiss();
                new AlertDialog.Builder(JobboardActivity.this)
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        queryServer();
    }

}
