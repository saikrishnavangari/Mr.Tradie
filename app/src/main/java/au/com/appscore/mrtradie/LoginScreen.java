package au.com.appscore.mrtradie;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import au.com.appscore.mrtradie.FacebookUtils.FaceBookUtils;

public class LoginScreen extends Activity {

    ImageButton imageButtonNoAccount;
    ImageButton imageButtonSignIn;
    ImageButton imageButtonClose;
    ImageButton imageButtonTradie;
    ImageButton imageButtonCustomer;
    LoginButton loginButtonFB;
    EditText editTextEmailAddress, editTextPassword;
    TextView textViewForgotPasswordLabel;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        callbackManager = CallbackManager.Factory.create();

        // If the user is logged in, redirect to Main Screen
        sharedPreferences = getApplicationContext().getSharedPreferences(AppUtils.SharedPreferenceFileName,0);
        editor = sharedPreferences.edit();

        if (sharedPreferences.getBoolean(AppUtils.KEY_IS_LOGGEDIN,false))
        {
            Intent intent = new Intent(LoginScreen.this, MainScreen.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }

        initialiseElementsForLoginScreen();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.StatusBarColorLoginScreen));
        }
    }

    private void initialiseElementsForLoginScreen() {
        editTextEmailAddress = (EditText) findViewById(R.id.editTextEmailAddress);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        imageButtonNoAccount = (ImageButton) findViewById(R.id.imageViewNoAccount);
        imageButtonSignIn = (ImageButton) findViewById(R.id.imageButtonSignIn);
        loginButtonFB = (LoginButton) findViewById(R.id.loginButtonWithFacebook);
        textViewForgotPasswordLabel = (TextView) findViewById(R.id.textViewForgotPassword);
        //loginButtonFB.setPublishPermissions();
        loginButtonFB.setReadPermissions(Arrays.asList("public_profile ,email, user_friends"));


        textViewForgotPasswordLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginScreen.this,ForgotPasswordScreen.class);
                startActivity(intent);
            }
        });

        loginButtonFB.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                final ProgressDialog pDialog = new ProgressDialog(LoginScreen.this);
                pDialog.setMessage("Logging In...");
                pDialog.setCancelable(false);
                pDialog.show();

                //Add by Jiazhou for new feature of letting users to select FaceBook friends
                FaceBookUtils faceBookUtils = new FaceBookUtils(true);

                Profile profile = Profile.getCurrentProfile();

                // Store user's info into shared preferences
                editor.putBoolean(AppUtils.KEY_LOGGEDIN_USING_FB,true);
                editor.putBoolean(AppUtils.KEY_IS_LOGGEDIN, true);
                editor.putString(AppUtils.KEY_ACCESS_TOKEN, loginResult.getAccessToken().getToken());
                editor.putString(AppUtils.KEY_USER_TYPE, "0");
                editor.commit();

                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject me, GraphResponse response) {
                                if (response.getError() != null) {
                                    // handle error
                                    Log.d("Debug###", "Error in request");
                                } else {
                                    Log.d("Debug###", me.toString());

                                    String email = null;
                                    String name = null;
                                    String id = null;
                                    try {
                                        email = me.getString("email");
                                        name = me.getString("name");
                                        id = me.getString("id");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    editor.putString(AppUtils.KEY_EMAIL, email);
                                    editor.putString(AppUtils.KEY_FULL_NAME, name);
                                    editor.commit();
                                    pDialog.dismiss();
                                    queryServerForSocialLogin(email, id);
                                    //Open main screen
//                                    Intent intent = new Intent(LoginScreen.this, MainScreen.class);
//                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                                    startActivity(intent);
//                                    finish();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "email,name");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {


            }

            @Override
            public void onError(FacebookException e) {
                new AlertDialog.Builder(LoginScreen.this)
                        .setTitle("Validation")
                        .setMessage("Please check your internet connection.")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            }
        });

        imageButtonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Hide Keyboard
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);

                inputManager.hideSoftInputFromWindow((null == getCurrentFocus()) ? null : getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                // Check if both the fields are not empty and entered email address is valid
                if (AppUtils.isValidEmail(editTextEmailAddress.getText())&&!TextUtils.isEmpty(editTextPassword.getText())) {

                    // Tag used to cancel the request
                    String tag_json_obj = "json_obj_req";

                    String url = AppUtils.baseURL+"login";

                    final ProgressDialog pDialog = new ProgressDialog(LoginScreen.this);
                    pDialog.setMessage("Logging In...");
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
                                if (mrt_status.equals("1000"))
                                {
                                    // Authenticated
                                    // Save user info into shared preferences

                                    editor.putBoolean(AppUtils.KEY_IS_LOGGEDIN,true);
                                    editor.putString(AppUtils.KEY_EMAIL, jsonData.getString("email"));
                                    editor.putString(AppUtils.KEY_ACCESS_TOKEN, jsonData.getString("access_token"));
                                    editor.putString(AppUtils.KEY_USER_TYPE, jsonData.getString("user_type"));
                                    editor.putString(AppUtils.KEY_FULL_NAME, jsonData.getString("full_name"));
                                    editor.apply();

                                    // Open main screen
                                    Intent intent = new Intent(LoginScreen.this, MainScreen.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                                else {
                                    // Not authenticated
                                    String mrt_desc = jsonData.getString("mrt_desc");
                                    new AlertDialog.Builder(LoginScreen.this)
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
                                new AlertDialog.Builder(LoginScreen.this)
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
                            new AlertDialog.Builder(LoginScreen.this)
                                    .setTitle("Validation")
                                    .setMessage("Please check your internet connection.")
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
                            params.put("password",editTextPassword.getText().toString());
                            params.put("device_type","android");
                            params.put("device_token", AppUtils.REG_TOKEN);

                            return params;
                        }
                    };

                    // Adding request to request queue
                    AppController.getInstance().addToRequestQueue(stringRequest, tag_json_obj);

                }
                else {
                    new AlertDialog.Builder(LoginScreen.this)
                            .setTitle("Validation")
                            .setMessage("Please enter valid username and password")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                }
            }
        });

        imageButtonNoAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setContentView(R.layout.selection_screen);
                initialiseElementsForSelectionScreen();
            }
        });
    }

    private void initialiseElementsForSelectionScreen() {
        imageButtonClose = (ImageButton) findViewById(R.id.imageButtonClose);
        imageButtonTradie = (ImageButton) findViewById(R.id.imageButtonTradie);
        imageButtonCustomer = (ImageButton) findViewById(R.id.imageButtonCustomer);
        imageButtonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.activity_login_screen);
                initialiseElementsForLoginScreen();
            }
        });

        imageButtonTradie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginScreen.this,TradieSignUpScreen.class);
                startActivity(intent);
            }
        });

        imageButtonCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginScreen.this,CustomerSignUpScreen.class);
                startActivity(intent);
            }
        });
    }

    // Method to perform social login
    private void queryServerForSocialLogin(final String userEmail, final String id) {
        // Tag used to cancel the request
        String tag_json_obj = "json_obj_req";

        String url = AppUtils.getSocialLoginURL();

        final ProgressDialog pDialog = new ProgressDialog(LoginScreen.this);
        pDialog.setMessage("Logging In...");
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
                    if (mrt_status.equals("1000") || mrt_status.equals("1009"))
                    {
                        // Authenticated
                        // Save user info into shared preferences
                        int firstTime = jsonData.getInt("is_first_time");
                        firstTime = 1;
                        editor.putBoolean(AppUtils.KEY_IS_LOGGEDIN,true);
                        editor.putString(AppUtils.KEY_EMAIL, jsonData.getString("email"));
                        editor.putString(AppUtils.KEY_ACCESS_TOKEN, jsonData.getString("access_token"));
                        editor.putString(AppUtils.KEY_USER_TYPE, jsonData.getString("user_type"));
                        editor.putString(AppUtils.KEY_FULL_NAME, jsonData.getString("full_name"));
                        editor.putString("facebook_id", id);
                        editor.putBoolean(AppUtils.KEY_LOGGEDIN_USING_FB, true);
                        editor.apply();

                        checkFirstTime();
                    }
                    else {
                        // Not authenticated
                        String mrt_desc = jsonData.getString("mrt_desc");
                        new AlertDialog.Builder(LoginScreen.this)
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
                    new AlertDialog.Builder(LoginScreen.this)
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
                new AlertDialog.Builder(LoginScreen.this)
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
                params.put("email",userEmail);
                params.put("social_type","1");
                params.put("device_type","android");
                params.put("device_token", AppUtils.REG_TOKEN);
                params.put("facebook_id", id);
                //params.put("password", userEmail);
                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(stringRequest, tag_json_obj);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login_screen, menu);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode,resultCode,data);
    }

    private void checkFirstTime()
    {
        // Hide Keyboard
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow((null == getCurrentFocus()) ? null : getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        // Check if both the fields are not empty and entered email address is valid
        if (true) {

            // Tag used to cancel the request
            String tag_json_obj = "json_obj_req";

            String url = AppUtils.baseURL+"login";

            final ProgressDialog pDialog = new ProgressDialog(LoginScreen.this);
            pDialog.setMessage("Logging In...");
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
                        if (mrt_status.equals("1000"))
                        {
                            // Authenticated
                            // Save user info into shared preferences

                            editor.putBoolean(AppUtils.KEY_IS_LOGGEDIN,true);
                            editor.putString(AppUtils.KEY_EMAIL, jsonData.getString("email"));
                            editor.putString(AppUtils.KEY_ACCESS_TOKEN, jsonData.getString("access_token"));
                            editor.putString(AppUtils.KEY_USER_TYPE, jsonData.getString("user_type"));
                            editor.putString(AppUtils.KEY_FULL_NAME, jsonData.getString("full_name"));
                            editor.apply();

                            // Open main screen
                            Intent intent = new Intent(LoginScreen.this, MainScreen.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            // Not authenticated
                            imageButtonNoAccount.callOnClick();
                            return;
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        new AlertDialog.Builder(LoginScreen.this)
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
                    new AlertDialog.Builder(LoginScreen.this)
                            .setTitle("Validation")
                            .setMessage("Please check your internet connection.")
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
                    params.put("email",sharedPreferences.getString(AppUtils.KEY_EMAIL, ""));
                    params.put("password", sharedPreferences.getString(AppUtils.KEY_EMAIL, ""));
                    params.put("device_type","android");
                    params.put("device_token", AppUtils.REG_TOKEN);

                    return params;
                }
            };

            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(stringRequest, tag_json_obj);

        }
        else {
            new AlertDialog.Builder(LoginScreen.this)
                    .setTitle("Validation")
                    .setMessage("Please enter valid username and password")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();
                }
    }

}
