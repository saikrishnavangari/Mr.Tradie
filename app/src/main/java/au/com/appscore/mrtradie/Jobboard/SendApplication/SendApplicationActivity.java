package au.com.appscore.mrtradie.Jobboard.SendApplication;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import au.com.appscore.mrtradie.AppUtils;
import au.com.appscore.mrtradie.Jsons.Jobboard.JobboardRecord;
import au.com.appscore.mrtradie.Jsons.JobboardEntry.EntryRecord;
import au.com.appscore.mrtradie.NonScrollListView;
import au.com.appscore.mrtradie.R;
import au.com.appscore.mrtradie.utils.ControlPraser;

public class SendApplicationActivity extends AppCompatActivity {

    EditText estimate;
    EditText description;
    Button clearEstimate;
    Button sendApplication;
    ImageView email;
    ImageView call;
    TextView user;
    TextView userDescription;
    TextView address;
    NonScrollListView nonScrollListView;

    String emailAddress;
    String phoneNumber;

    SharedPreferences sharedPreferences;

    JobboardRecord jobboardRecord;
    EntryRecord entryJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_application);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.PrimaryDarkColor));
        }
        sharedPreferences = getApplicationContext().getSharedPreferences(AppUtils.SharedPreferenceFileName, 0);
        final String type =  sharedPreferences.getString(AppUtils.KEY_USER_TYPE, "0");
        estimate = ControlPraser.PraserControl(this, R.id.calc_txt_Prise);
        description = ControlPraser.PraserControl(this, R.id.sendapp_description);
        clearEstimate = ControlPraser.PraserControl(this, R.id.calc_clear_estmate);
        sendApplication = ControlPraser.PraserControl(this, R.id.sendapp_button);
        email = ControlPraser.PraserControl(this, R.id.sendapp_mail_button);
        call = ControlPraser.PraserControl(this, R.id.sendapp_call_button);
        user = ControlPraser.PraserControl(this, R.id.sendapp_user_text);
        userDescription = ControlPraser.PraserControl(this, R.id.sendapp_user_description);
        address = ControlPraser.PraserControl(this, R.id.sendapp_address);
        nonScrollListView = ControlPraser.PraserControl(this, R.id.listViewAvailability);

        if("1".equals(type)) {
            jobboardRecord = (JobboardRecord) getIntent().getSerializableExtra("sendApplicationData");
            emailAddress = jobboardRecord.quote.owner_email;
            phoneNumber = jobboardRecord.quote.user_profile.contact_num;
            String name = jobboardRecord.quote.user_profile.user_full_name;
            name = (name == null || name.trim().isEmpty()) ? "No name specified" : name;
            user.setText("Client: " + name);
            address.setText(jobboardRecord.address.full_address);
            description.setText(jobboardRecord.quote.quote_desc);

        }
        int selection = -1;
        if("0".equals(type)) {
            entryJson = (EntryRecord) getIntent().getSerializableExtra("sendApplicationData");
            emailAddress = entryJson.company_email;
            phoneNumber = entryJson.company_contact_number;
            sendApplication.setText("Accept");
            estimate.setEnabled(false);
            nonScrollListView.setEnabled(false);
            user.setText("Traide: " + entryJson.company_name);
            address.setText(entryJson.address.full_address);
            address.setEnabled(false);
            description.setText(entryJson.description);
            address.setEnabled(false);
            selection = 1;
        }

        sendApplication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if("1".equals(type))
                    sendApplication();
                else
                    accept();
            }
        });

        List<String> availability = jobboardRecord == null ? entryJson.getAvailability():jobboardRecord.getAvailability();
        ArrayAdapter<String> itemsAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, availability);
        nonScrollListView.setAdapter(itemsAdapter);
        if(-1!= selection)
            nonScrollListView.setSelection(selection);

        clearEstimate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                estimate.setText("");
            }
        });
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                    new AlertDialog.Builder(SendApplicationActivity.this)
                            .setTitle("Error")
                            .setMessage("No registered phone number.")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                    return;
                }

                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
                startActivity(intent);
            }
        });

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (AppUtils.isValidEmail(emailAddress))
                {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto",emailAddress, null));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Mr. Tradie enquiry");
                    startActivity(Intent.createChooser(emailIntent, "Send email..."));
                }
                else {
                    new AlertDialog.Builder(SendApplicationActivity.this)
                            .setTitle("Error")
                            .setMessage("This Tradie does not have a valid email address on record.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                }
            }
        });

        if(sharedPreferences.getString(AppUtils.KEY_USER_TYPE, "").equals("0"))
        {
            sendApplication.setText("Accept");
            estimate.setEnabled(false);
            estimate.setText(entryJson.budget);
            nonScrollListView.setSelection(0);
            email.setVisibility(View.INVISIBLE);
            call.setVisibility(View.INVISIBLE);
        }
    }

    private void sendApplication()
    {
        if(estimate.getText().toString().isEmpty() || nonScrollListView.getCheckedItemPosition() == -1)
        {
            new AlertDialog.Builder(SendApplicationActivity.this)
                    .setTitle("Error")
                    .setMessage("Please fill estimate and select the availability.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();
            return;
        }

        final ProgressDialog pDialog = new ProgressDialog(SendApplicationActivity.this);
        pDialog.setMessage("Accepting this quote...");
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        params.put("email",sharedPreferences.getString(AppUtils.KEY_EMAIL, null));
        params.put("access_token", sharedPreferences.getString(AppUtils.KEY_ACCESS_TOKEN, null));
        params.put("budget", estimate.getText().toString());
        params.put("job_id", jobboardRecord.quote.quote_id);
        String json = new Gson().toJson(jobboardRecord.availability.get(nonScrollListView.getCheckedItemPosition()));
        params.put("availability", json);

        client.post(AppUtils.getJobboardApplyUrl(), params, new AsyncHttpResponseHandler() {

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

                    if (mrt_status.equals("1007") || mrt_status.equals("1009")) {
                        // Successfully declined the quote
                        new AlertDialog.Builder(SendApplicationActivity.this)
                                .setTitle("Successfully applied")
                                .setMessage("You've successfully applied this job. The customer will be informed.")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Set result code to OK and close this activity
                                        setResult(AppUtils.RESULT_OK);
                                        finish();
                                    }
                                })
                                .show();
                    } else {
                        String mrt_desc = jsonData.getString("mrt_desc");
                        new AlertDialog.Builder(SendApplicationActivity.this)
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
                    new AlertDialog.Builder(SendApplicationActivity.this)
                            .setTitle("Server Error")
                            .setMessage("Sorry ! Could not get data from server.")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                }
                finish();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                pDialog.dismiss();
                new AlertDialog.Builder(SendApplicationActivity.this)
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

    public void accept()
    {
        String url  = AppUtils.acceptJobUrl();
        String jobid = entryJson.id;
        final ProgressDialog pDialog = new ProgressDialog(SendApplicationActivity.this);
        pDialog.setMessage("Accepting this quote...");
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        params.put("email",sharedPreferences.getString(AppUtils.KEY_EMAIL, null));
        params.put("access_token", sharedPreferences.getString(AppUtils.KEY_ACCESS_TOKEN, null));
        params.put("job_id", jobid);

        client.post(url, params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                pDialog.show();
                pDialog.setCancelable(false);

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                pDialog.dismiss();
                finish();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                pDialog.dismiss();
                new AlertDialog.Builder(SendApplicationActivity.this)
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
}
