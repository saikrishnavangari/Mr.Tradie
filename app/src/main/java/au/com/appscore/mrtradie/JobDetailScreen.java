package au.com.appscore.mrtradie;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class JobDetailScreen extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView textViewPrice, textViewJobDesc, textViewClientCompanyName, textViewQuoteAddress;
    private ImageButton imageButtonAction, imageButtonCloseRating, imageButtonSubmitRating;
    private EditText editTextComment;
    private NonScrollListView listViewAvailability;
    private RatingBar ratingBarComment;
    private SharedPreferences sharedPreferences;
    ProgressDialog pDialog;
    String jobID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail_screen);

        sharedPreferences = getApplicationContext().getSharedPreferences(AppUtils.SharedPreferenceFileName, 0);
        pDialog = new ProgressDialog(JobDetailScreen.this);

        initialiseElementsForJobDetailScreen();
    }

    // Method to initialise elements for Job Detail layout
    private void initialiseElementsForJobDetailScreen() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.PrimaryDarkColor));
        }

        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Initialise UI Elements
        textViewPrice = (TextView) toolbar.findViewById(R.id.textViewPrice);
        textViewJobDesc = (TextView) toolbar.findViewById(R.id.textViewQuoteDesc);
        textViewClientCompanyName = (TextView) findViewById(R.id.textViewClientCompanyName);
        textViewQuoteAddress = (TextView) findViewById(R.id.textViewQuoteAddress);
        imageButtonAction = (ImageButton) findViewById(R.id.imageButtonAction);
        listViewAvailability = (NonScrollListView) findViewById(R.id.listViewAvailability);

        // Retrieve Job details from intent
        String jobDetail = getIntent().getStringExtra("JobDetail");
        try {
            JSONObject jsonObjectJobDetail = new JSONObject(jobDetail);
            JSONObject jsonObjectJob = jsonObjectJobDetail.getJSONObject("job");
            String jobStatus = jsonObjectJobDetail.getString("job_staus");
            JSONObject jsonObjectAddress = jsonObjectJobDetail.getJSONObject("address");
            JSONObject jsonObjectUserProfile = jsonObjectJob.getJSONObject("user_profile");
            jobID = jsonObjectJob.getString("job_id");
            textViewClientCompanyName.setText(jsonObjectUserProfile.getString("user_full_name"));
            textViewQuoteAddress.setText(jsonObjectAddress.getString("full_address"));
            textViewPrice.setText("$"+jsonObjectJob.getString("budget"));
            textViewJobDesc.setText(jsonObjectJob.getString("job_desc"));

            // Test for availability
            JSONArray jsonArrayAvailability = jsonObjectJobDetail.getJSONArray("availability");
            ArrayList<String> arrayListAvailability = new ArrayList<>();
            for (int i=0;i<jsonArrayAvailability.length();i++) {
                JSONObject jsonObject = jsonArrayAvailability.getJSONObject(i);
                arrayListAvailability.add(jsonObject.getString("start_date")+" - "+jsonObject.getString("end_date"));
            }
            ArrayAdapter<String> itemsAdapter =
                    new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayListAvailability);
            listViewAvailability.setAdapter(itemsAdapter);
            // End test

            // Set the action button and its click listener according to user type and job status
            if (sharedPreferences.getString(AppUtils.KEY_USER_TYPE,"").equals("0"))
            {
                // User is a Customer
                if (jobStatus.equals("New")) {
                    imageButtonAction.setBackgroundResource(R.drawable.button_closejob);
                    imageButtonAction.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new AlertDialog.Builder(JobDetailScreen.this)
                                    .setTitle("Invoice Paid")
                                    .setMessage("Job will close")
                                    .setCancelable(false)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            closeJob();
                                        }
                                    })
                                    .setNegativeButton("Cancel", null)
                                    .show();
                        }
                    });
                }
                if (jobStatus.equals("Job Rated")) {
                    imageButtonAction.setBackgroundResource(R.drawable.button_rate);
                    imageButtonAction.setEnabled(false);
                    imageButtonAction.setClickable(false);
                }
                if (jobStatus.equals("Completed")) {
                    imageButtonAction.setBackgroundResource(R.drawable.button_rate);
                    imageButtonAction.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            initialiseElementsForRatingsScreen();
                        }
                    });
                }
            }
            if (sharedPreferences.getString(AppUtils.KEY_USER_TYPE,"").equals("1"))
            {
                // User is a Tradie
                if (jobStatus.equals("New")) {
                    imageButtonAction.setBackgroundResource(R.drawable.button_closejob_inactive);
                    imageButtonAction.setEnabled(false);
                    imageButtonAction.setClickable(false);
                    imageButtonAction.setBackgroundResource(R.drawable.button_closejob);
//                    imageButtonAction.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            new AlertDialog.Builder(JobDetailScreen.this)
//                                    .setTitle("Invoice Paid")
//                                    .setMessage("Job will close")
//                                    .setCancelable(false)
//                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                                        public void onClick(DialogInterface dialog, int which) {
//                                            closeJob();
//                                        }
//                                    })
//                                    .setNegativeButton("Cancel",null)
//                                    .show();
//                        }
//                    });
                }
                if (jobStatus.equals("Job Rated")||jobStatus.equals("Completed")) {
                    imageButtonAction.setBackgroundResource(R.drawable.button_closejob_inactive);
                    imageButtonAction.setEnabled(false);
                    imageButtonAction.setClickable(false);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Method to initialise elements for Ratings layout
    private void initialiseElementsForRatingsScreen() {
        setContentView(R.layout.rating_screen);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.StatusBarColorLoginScreen));
        }

        editTextComment = (EditText) findViewById(R.id.editTextComment);
        ratingBarComment = (RatingBar) findViewById(R.id.ratingBarComment);
        imageButtonSubmitRating = (ImageButton) findViewById(R.id.imageButtonSubmitRating);
        imageButtonCloseRating = (ImageButton) findViewById(R.id.imageButtonClose);
        imageButtonCloseRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.activity_job_detail_screen);
                initialiseElementsForJobDetailScreen();
            }
        });
        imageButtonSubmitRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(editTextComment.getText()))
                    rateJob(ratingBarComment.getRating(),editTextComment.getText().toString());
                else {
                    new AlertDialog.Builder(JobDetailScreen.this)
                            .setTitle("Validation")
                            .setMessage("Tell us something about your experience!")
                            .setCancelable(false)
                            .setPositiveButton("OK", null)
                            .show();
                }
            }
        });

    }

    // Method to update Job Status
    private void closeJob() {
        pDialog.setMessage("Closing this Job...");
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        params.put("email",sharedPreferences.getString(AppUtils.KEY_EMAIL,null));
        params.put("access_token",sharedPreferences.getString(AppUtils.KEY_ACCESS_TOKEN,null));
        params.put("job_id",jobID);
        params.put("job_status", "Completed");

        client.post(AppUtils.getUpdateJobURL(), params, new AsyncHttpResponseHandler() {

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
                // Parse JSON data
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject jsonData = jsonObject.getJSONObject("data");

                    String mrt_status = jsonData.getString("mrt_status");

                    if (mrt_status.equals("1009")) {
                        // Successfully closed the job
                        setResult(AppUtils.RESULT_OK);
                        finish();
                    } else {
                        String mrt_desc = jsonData.getString("mrt_desc");
                        new AlertDialog.Builder(JobDetailScreen.this)
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
                    new AlertDialog.Builder(JobDetailScreen.this)
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
                new AlertDialog.Builder(JobDetailScreen.this)
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
    }

    // Method to rate the job
    private void rateJob(float rating, String comment) {
        pDialog.setMessage("Rating the Tradie...");
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        params.put("email",sharedPreferences.getString(AppUtils.KEY_EMAIL,null));
        params.put("access_token",sharedPreferences.getString(AppUtils.KEY_ACCESS_TOKEN,null));
        params.put("job_id",jobID);
        params.put("comments", comment);
        params.put("stars",rating);

        client.post(AppUtils.getRateJobURL(), params, new AsyncHttpResponseHandler() {

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

                // Parse JSON data
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject jsonData = jsonObject.getJSONObject("data");

                    String mrt_status = jsonData.getString("mrt_status");

                    if (mrt_status.equals("1009")) {
                        // Successfully rated the job
                        setResult(AppUtils.RESULT_OK);
                        finish();
                    } else {
                        String mrt_desc = jsonData.getString("mrt_desc");
                        new AlertDialog.Builder(JobDetailScreen.this)
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
                    new AlertDialog.Builder(JobDetailScreen.this)
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
                new AlertDialog.Builder(JobDetailScreen.this)
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
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home)
        {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
