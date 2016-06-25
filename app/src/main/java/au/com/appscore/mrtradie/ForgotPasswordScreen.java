package au.com.appscore.mrtradie;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ForgotPasswordScreen extends AppCompatActivity {

    private EditText editTextEmailAddress;
    private ImageButton imageButtonRetrieve;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_screen);

        editTextEmailAddress = (EditText) findViewById(R.id.editTextEmailAddress);
        imageButtonRetrieve = (ImageButton) findViewById(R.id.imageButtonRetrieve);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.StatusBarColorLoginScreen));
        }

        imageButtonRetrieve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hide Keyboard
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);

                inputManager.hideSoftInputFromWindow((null == getCurrentFocus()) ? null : getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                if (AppUtils.isValidEmail(editTextEmailAddress.getText())) {
                    // Tag used to cancel the request
                    String tag_json_obj = "json_obj_req";

                    String url = AppUtils.getForgotPasswordURL();

                    final ProgressDialog pDialog = new ProgressDialog(ForgotPasswordScreen.this);
                    pDialog.setMessage("Resetting password...");
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

                                // Check if user is authenticated
                                if (mrt_status.equals("1000")||mrt_status.equals("1009"))
                                {
                                    new AlertDialog.Builder(ForgotPasswordScreen.this)
                                            .setTitle("Request Successful")
                                            .setMessage("Please check your email for a temporary password.")
                                            .setCancelable(false)
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    finish();
                                                }
                                            })
                                            .show();
                                }
                                else {
                                    // Not authenticated
                                    String mrt_desc = jsonData.getString("mrt_desc");
                                    new AlertDialog.Builder(ForgotPasswordScreen.this)
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
                                new AlertDialog.Builder(ForgotPasswordScreen.this)
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
                            new AlertDialog.Builder(ForgotPasswordScreen.this)
                                    .setTitle("Validation")
                                    .setMessage("Please check your Internet connection.")
                                    .setCancelable(false)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })
                                    .show();
                        }
                    }){
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String,String> params = new HashMap<String, String>();
                            params.put("email",editTextEmailAddress.getText().toString());
                            params.put("device_token", AppUtils.REG_TOKEN);

                            return params;
                        }
                    };

                    // Adding request to request queue
                    AppController.getInstance().addToRequestQueue(stringRequest, tag_json_obj);
                }
                else {
                    new AlertDialog.Builder(ForgotPasswordScreen.this)
                            .setTitle("Validation")
                            .setMessage("Please enter a valid Email address.")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_forgot_password_screen, menu);
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
}