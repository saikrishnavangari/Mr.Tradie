package au.com.appscore.mrtradie.Jobboard;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import au.com.appscore.mrtradie.AppController;
import au.com.appscore.mrtradie.AppUtils;
import au.com.appscore.mrtradie.Jobboard.JobboardEntryItem.EntryItem;
import au.com.appscore.mrtradie.Jobboard.SendApplication.SendApplicationActivity;
import au.com.appscore.mrtradie.Jsons.Jobboard.JobboardRecord;
import au.com.appscore.mrtradie.Jsons.JobboardEntry.EntryJson;
import au.com.appscore.mrtradie.R;
import au.com.appscore.mrtradie.utils.ControlPraser;

public class JobboardEntry extends AppCompatActivity {

    Toolbar toolbar;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jobboard_entry);
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        listView = ControlPraser.PraserControl(this, R.id.jobboard_entry_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.PrimaryDarkColor));
        }
        queryServer();
    }


    public void queryServer()
    {
        String url = AppUtils.getJobboardApplicationsUrl(getSharedPreferences(AppUtils.SharedPreferenceFileName, 0).getString(AppUtils.KEY_EMAIL, ""),
                getSharedPreferences(AppUtils.SharedPreferenceFileName, 0).getString(AppUtils.KEY_ACCESS_TOKEN, ""),
                ((JobboardRecord)getIntent().getSerializableExtra("sendApplicationData")).quote.quote_id, "short");
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Finding jobs...");
        pDialog.setCancelable(false);
        pDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                pDialog.dismiss();
                try {
                    final EntryJson json = new Gson().fromJson(response, EntryJson.class);
                    if(json.data == null || 0 == json.data.records.size())
                        return;
                    // Check if user is authenticated
                    if (json.data.mrt_status.equals("1000") || json.data.mrt_status.equals("1005"))
                    {
                        // Authenticated
                        // Retrieve businesses from response and set list view adapter for the list
                        JobboardAdaptor<EntryItem> adaptor = new JobboardAdaptor<>();

                        if(json.data.records.size() == 0) {
                            new AlertDialog.Builder(JobboardEntry.this)
                                    .setTitle("Error")
                                    .setMessage("Not job found!")
                                    .setCancelable(false)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })
                                    .show();
                            finish();
                        }

                        for(int i = 0; i < json.data.records.size(); i++)
                        {
                            EntryItem item = new EntryItem(getApplicationContext(),
                                    ControlPraser.GetAttr(getApplicationContext(), R.layout.activity_jobboard_entry),
                                    json.data.records.get(i).company_name,
                                    json.data.records.get(i).budget);
                            adaptor.add(item);
                        }
                        listView.setAdapter(null);
                        listView.setAdapter(adaptor);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Intent intent;
                                intent = new Intent(getApplicationContext(), SendApplicationActivity.class);
                                intent.putExtra("sendApplicationData", json.data.records.get(position));
                                startActivityForResult(intent, 1);
                            }
                        });
                    }
                    else {
                        // Not authenticated
                        String mrt_desc = json.data.mrt_desc;
                        new AlertDialog.Builder(JobboardEntry.this)
                                .setTitle("Error")
                                .setMessage(mrt_desc)
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .show();
                        finish();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(JobboardEntry.this)
                            .setTitle("Server Error")
                            .setMessage("Sorry ! Could not get data from server.")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                    finish();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.dismiss();
                new AlertDialog.Builder(JobboardEntry.this)
                        .setTitle("Error")
                        .setMessage("Please check your Internet connection.")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
                finish();
            }
        });
        AppController.getInstance().addToRequestQueue(stringRequest, "cancel_search");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_quote_accepted_screen, menu);
        menu.getItem(0).setVisible(false);
        // Associate searchable configuration with the SearchView
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        finish();

        return super.onOptionsItemSelected(item);
    }
}
