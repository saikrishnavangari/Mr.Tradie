package au.com.appscore.mrtradie;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.baoyz.actionsheet.ActionSheet;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.koushikdutta.ion.Ion;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import au.com.appscore.mrtradie.FacebookUtils.FaceBookUtils;
import au.com.appscore.mrtradie.camerautility.AlbumStorageDirFactory;
import au.com.appscore.mrtradie.camerautility.BaseAlbumDirFactory;
import au.com.appscore.mrtradie.camerautility.FroyoAlbumDirFactory;

public class TradieProfileScreen extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ActionSheet.ActionSheetListener {

    // Variables for camera utility
    private static int IMAGE_PICKER_SELECT = 1;

    private static int CAPTURE_PHOTO_SELECT = 2;

    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";

    private AlbumStorageDirFactory mAlbumStorageDirFactory = null;

    String mCurrentPhotoPath;
    String companyLogoPath = null;
    String photo1Path = null;
    String photo2Path = null;
    String photo3Path = null;
    int photoType=0;

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    private SharedPreferences sharedPreferences;
    private ProgressDialog pDialog;
    private EditText editTextFullName,editTextEmailAddress, editTextPassword, editTextCompanyName, editTextABN, editTextTaxNo, editTextRegNumber, editTextAboutCompany, editTextFullAddress, editTextTelNo, editTextWebsite;
    private ImageView imageViewLogoUpload, imageViewAddPhoto1, imageViewAddPhoto2, imageViewAddPhoto3;
    private SwitchCompat switchCompatPublicLiability;
    private Spinner spinnerOccupation;
    private ImageButton imageButtonAddress;
    private Button imageButtonUpdateTradie;
    private JSONObject jsonObjectAddress = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tradie_profile_screen);

        initialiseElements();

        getTradieInfo();

        // First we need to check availability of play services
        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();
        }
    }

    private void initialiseElements() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.StatusBarColorLoginScreen));
        }

        // Camera Utility

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
        } else {
            mAlbumStorageDirFactory = new BaseAlbumDirFactory();
        }

        jsonObjectAddress = new JSONObject();

        final boolean[] passwordCleared = {false};

        pDialog = new ProgressDialog(TradieProfileScreen.this);
        sharedPreferences = getApplicationContext().getSharedPreferences(AppUtils.SharedPreferenceFileName,0);

        editTextFullName = (EditText) findViewById(R.id.editTextFullName);
        editTextEmailAddress = (EditText) findViewById(R.id.editTextEmailAddress);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        editTextCompanyName = (EditText) findViewById(R.id.editTextCompanyName);
        editTextABN = (EditText) findViewById(R.id.editTextABN);
        editTextTaxNo = (EditText) findViewById(R.id.editTextTaxNo);
        editTextRegNumber = (EditText) findViewById(R.id.editTextRegistrationNumber);
        switchCompatPublicLiability = (SwitchCompat) findViewById(R.id.switchPublicLiability);
        editTextAboutCompany = (EditText) findViewById(R.id.editTextAboutCompany);
        spinnerOccupation = (Spinner) findViewById(R.id.spinnerOccupation);
        editTextFullAddress = (EditText) findViewById(R.id.editTextAddress);
        imageButtonAddress = (ImageButton) findViewById(R.id.imageButtonAddress);
        editTextTelNo = (EditText) findViewById(R.id.editTextTelNo);
        editTextWebsite = (EditText) findViewById(R.id.editTextWebsite);
        imageViewLogoUpload = (ImageView) findViewById(R.id.imageButtonLogoUpload);
        imageViewAddPhoto1 = (ImageView) findViewById(R.id.imageButtonAddPhoto1);
        imageViewAddPhoto2 = (ImageView) findViewById(R.id.imageButtonAddPhoto2);
        imageViewAddPhoto3 = (ImageView) findViewById(R.id.imageButtonAddPhoto3);
        imageButtonUpdateTradie = (Button) findViewById(R.id.imageButtonUpdateTradie);

        // Todo: Setup camera utility
        imageViewLogoUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showActionSheet(0);
            }
        });

        imageViewAddPhoto1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showActionSheet(1);
            }
        });
        imageViewAddPhoto2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showActionSheet(2);
            }
        });
        imageViewAddPhoto3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showActionSheet(3);
            }
        });

        // Clearing password on pressing delete button for the first time
        editTextPassword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (keyCode == KeyEvent.KEYCODE_DEL&&!passwordCleared[0])
                {
                    passwordCleared[0] = true;
                    editTextPassword.setText("");
                    return true;
                }

                return false;
            }
        });

        // Activate address button
        imageButtonAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayLocation();
            }
        });

        if(sharedPreferences.getBoolean(AppUtils.KEY_LOGGEDIN_USING_FB, false))
        {
            FaceBookUtils faceBookUtils = new FaceBookUtils();
            String password = sharedPreferences.getString(AppUtils.KEY_EMAIL, "");
            String email = sharedPreferences.getString(AppUtils.KEY_EMAIL, "");
            String name = sharedPreferences.getString(AppUtils.KEY_FULL_NAME, "");
            editTextEmailAddress.setText(email);
            editTextEmailAddress.setEnabled(false);
            editTextFullName.setText(name);
            editTextFullName.setEnabled(false);
            editTextPassword.setText(password);
            editTextPassword.setEnabled(false);
        }

        // Make server call to update info
        imageButtonUpdateTradie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (allFieldsValid())
                {
                    // Todo: Make server call
                    pDialog.show();
                    pDialog.setCancelable(false);
                    // Get lat long from address
                    try {
                        LatLng latLng = getLocationFromAddress(editTextFullAddress.getText().toString());
                        if (latLng != null) {
                            // Add lat long fields to json object of address
                            Log.d("Debug", latLng.toString());
                            jsonObjectAddress.put("latitude", latLng.latitude);
                            jsonObjectAddress.put("longitude", latLng.longitude);

                        }

                    } catch (Exception e) {
                        Log.d("Debug", e.toString());
                    }

                    // Make server call to update customer info
                    updateTradie();
                }
                else {
                    new AlertDialog.Builder(TradieProfileScreen.this)
                            .setTitle("Validation")
                            .setMessage("Please fill in all fields and ensure email address, password and contact number is valid.")
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

    // Get lat long from address
    public LatLng getLocationFromAddress(String strAddress) {

        Geocoder coder = new Geocoder(TradieProfileScreen.this);
        List<Address> address;
        LatLng p1 = null;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng(location.getLatitude(), location.getLongitude() );

        } catch (Exception ex) {

            ex.printStackTrace();
        }

        return p1;
    }

    // Method to validate all the fields before submitting
    private Boolean allFieldsValid() {
        boolean result = true;
        // Check if any field is left blank, entered email address and password is valid
        if (TextUtils.isEmpty(editTextFullName.getText())||TextUtils.isEmpty(editTextEmailAddress.getText())||TextUtils.isEmpty(editTextPassword.getText())||TextUtils.isEmpty(editTextCompanyName.getText())||TextUtils.isEmpty(editTextABN.getText())||TextUtils.isEmpty(editTextTaxNo.getText())||TextUtils.isEmpty(editTextRegNumber.getText())||TextUtils.isEmpty(editTextAboutCompany.getText())||TextUtils.isEmpty(editTextFullAddress.getText())||TextUtils.isEmpty(editTextTelNo.getText()))
            result = false;
        else if (!AppUtils.isValidEmail(editTextEmailAddress.getText())||editTextPassword.getText().toString().length()<6||editTextTelNo.getText().toString().length()<10)
            result = false;

        return result;
    }

    // Method to retrieve customer info from back-end
    private void getTradieInfo() {
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
                pDialog.dismiss();
                String response = new String(responseBody);

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject jsonObjectData = jsonObject.getJSONObject("data");
                    JSONObject jsonObjectAddress = jsonObjectData.getJSONObject("address");

                    if (jsonObjectData.getInt("mrt_status") == 1000) {
                        editTextFullName.setText(jsonObjectData.getString("full_name"));
                        editTextEmailAddress.setText(jsonObjectData.getString("email"));
                        editTextPassword.setText(jsonObjectData.getString("password"));
                        editTextTelNo.setText(jsonObjectData.getString("contact_num"));
                        editTextFullAddress.setText(jsonObjectAddress.getString("full_address"));
                        editTextCompanyName.setText(jsonObjectData.getString("company_name"));
                        editTextABN.setText(jsonObjectData.getString("abn"));
                        editTextTaxNo.setText(jsonObjectData.getString("tax_no"));
                        editTextRegNumber.setText(jsonObjectData.getString("license"));
                        if (jsonObjectData.getInt("liability_status") == 1)
                            switchCompatPublicLiability.setChecked(true);
                        else
                            switchCompatPublicLiability.setChecked(false);
                        editTextAboutCompany.setText(jsonObjectData.getString("about"));
                        editTextWebsite.setText(jsonObjectData.getString("website"));
                        spinnerOccupation.setSelection(((ArrayAdapter<String>) spinnerOccupation.getAdapter()).getPosition(jsonObjectData.getString("occupation")));
                        Ion.with(imageViewLogoUpload).load(jsonObjectData.getString("company_logo"));
                        Ion.with(imageViewAddPhoto1).load(jsonObjectData.getString("photo1"));
                        Ion.with(imageViewAddPhoto2).load(jsonObjectData.getString("photo2"));
                        Ion.with(imageViewAddPhoto3).load(jsonObjectData.getString("photo3"));
                    } else {
                        new AlertDialog.Builder(TradieProfileScreen.this)
                                .setTitle("Server Error")
                                .setMessage("Could not retrieve information from server.")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                pDialog.dismiss();
                new AlertDialog.Builder(TradieProfileScreen.this)
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

    // Method to update Tradie
    private void updateTradie() {
        pDialog.setMessage("Updating profile...");
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        params.put("email",editTextEmailAddress.getText().toString());
        params.put("full_name",editTextFullName.getText().toString());
        params.put("password",editTextPassword.getText().toString());
        params.put("company_name",editTextCompanyName.getText().toString());
        params.put("abn",editTextABN.getText().toString());
        params.put("tax_no",editTextTaxNo.getText().toString());
        params.put("license",editTextRegNumber.getText().toString());
        params.put("about",editTextAboutCompany.getText().toString());
        params.put("occupation", spinnerOccupation.getItemAtPosition(spinnerOccupation.getSelectedItemPosition()).toString());
        try {
            jsonObjectAddress.put("full_address",editTextFullAddress.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (jsonObjectAddress!=null)
            params.put("address",jsonObjectAddress);
        params.put("contact_num",editTextTelNo.getText().toString());
        params.put("user_type",1);
        params.put("access_token", sharedPreferences.getString(AppUtils.KEY_ACCESS_TOKEN, ""));
        if (!TextUtils.isEmpty(editTextWebsite.getText()))
            params.put("website",editTextWebsite.getText());
        if (switchCompatPublicLiability.isChecked())
            params.put("liability_status",1);
        else
            params.put("liability_status",0);
        try {
            if (companyLogoPath!=null)
                params.put("company_logo",new File(companyLogoPath));
            if (photo1Path!=null)
                params.put("photo1",new File(photo1Path));
            if (photo2Path!=null)
                params.put("photo2",new File(photo2Path));
            if (photo3Path!=null)
                params.put("photo3",new File(photo3Path));
        } catch (FileNotFoundException e) {
            new AlertDialog.Builder(TradieProfileScreen.this)
                    .setTitle("Error")
                    .setMessage("Image Error")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();
            e.printStackTrace();
        }

        client.post(AppUtils.baseURL + "profile/update", params, new AsyncHttpResponseHandler() {

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
                Log.d("Debug", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject jsonData = jsonObject.getJSONObject("data");

                    String mrt_status = jsonData.getString("mrt_status");

                    if (mrt_status.equals("1009")) {
                        // Record updated successfully
                        new AlertDialog.Builder(TradieProfileScreen.this)
                                .setTitle("Request Successful")
                                .setMessage("Your profile has been updated successfully.")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                                .show();
                    } else {
                        new AlertDialog.Builder(TradieProfileScreen.this)
                                .setTitle("Server Error")
                                .setMessage("Could not update profile.")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                pDialog.dismiss();
                new AlertDialog.Builder(TradieProfileScreen.this)
                        .setTitle("Server Error")
                        .setMessage("Could not update profile.")
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tradie_profile_screen, menu);
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
    private void displayLocation() {

        Location mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {



            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

            Log.d("Debug","Latitude : "+latitude+" Longitude "+longitude);

            // Getting address from latitude and longitude
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this, Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String postalCode = addresses.get(0).getPostalCode();
                Log.d("Debug",address+" "+city+" "+state+" "+postalCode);
                editTextFullAddress.setText(address+" "+city+" "+state+" "+postalCode);

            } catch (IOException e) {
                e.printStackTrace();
            }


        } else {
            Log.d("Debug","Could not get current location");
            new AlertDialog.Builder(TradieProfileScreen.this)
                    .setTitle("Location Error")
                    .setMessage("Could not retrieve current location.")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();
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
    public void onConnected(Bundle bundle) {
        //displayLocation();
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

    // Methods for camera utility
    // Method to display action sheet
    private void showActionSheet(int photoType) {
        this.photoType = photoType;
        ActionSheet.createBuilder(this, getSupportFragmentManager())
                .setCancelButtonTitle("Cancel")
                .setOtherButtonTitles("Take Photo", "Choose from Camera Roll")
                .setCancelableOnTouchOutside(true).setListener(this).show();
    }

    @Override
    public void onDismiss(ActionSheet actionSheet, boolean b) {

    }

    @Override
    public void onOtherButtonClick(ActionSheet actionSheet, int i) {
//        Toast.makeText(getApplicationContext(), "click item index = " + i,
//                Toast.LENGTH_SHORT).show();

        switch (i)
        {
            case 0:
                // Take Photo
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                File f = null;

                try {
                    f = setUpPhotoFile();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));

                } catch (IOException e) {
                    e.printStackTrace();
                    f = null;
                    mCurrentPhotoPath = null;
                }



                startActivityForResult(takePictureIntent, CAPTURE_PHOTO_SELECT);
                break;
            case 1:
                // Choose from camera roll
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,IMAGE_PICKER_SELECT);
                break;
            default:
                break;
        }
    }

    public static String getImagePathFromCameraData(Intent data, Context context){ Uri selectedImage = data.getData(); String[] filePathColumn = { MediaStore.Images.Media.DATA }; Cursor cursor = context.getContentResolver().query(selectedImage,filePathColumn, null, null, null); cursor.moveToFirst(); int columnIndex = cursor.getColumnIndex(filePathColumn[0]); String picturePath = cursor.getString(columnIndex); cursor.close(); return picturePath; }

    private String getAlbumName() {
        return getString(R.string.album_name);
    }

    private File getAlbumDir() {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());

            if (storageDir != null) {
                if (! storageDir.mkdirs()) {
                    if (! storageDir.exists()){
                        Log.d("CameraSample", "failed to create directory");
                        return null;
                    }
                }
            }

        } else {
            Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }

        return storageDir;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        File albumF = getAlbumDir();
        File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
        return imageF;
    }

    private File setUpPhotoFile() throws IOException {

        File f = createImageFile();
        mCurrentPhotoPath = f.getAbsolutePath();

        return f;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        TradieProfileScreen.this.sendBroadcast(mediaScanIntent);
    }

    // Method to update file content with scaled bitmap
    private void updateFileWithScaledBitmap(String path, Bitmap bmp) {
        File file = new File(path);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file,false);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==IMAGE_PICKER_SELECT && resultCode== Activity.RESULT_OK)
        {

            final String path = getImagePathFromCameraData(data,TradieProfileScreen.this);

            File imgFile = new  File(path);

            if(imgFile.exists()){

                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                // Scale down bitmap
                int nh = (int) ( myBitmap.getHeight() * (512.0 / myBitmap.getWidth()) );
                Bitmap scaled = Bitmap.createScaledBitmap(myBitmap, 512, nh, true);

                switch (this.photoType) {
                    case 0:
                        // Company Logo
                        imageViewLogoUpload.setImageBitmap(scaled);
                        companyLogoPath = path;
                        break;
                    case 1:
                        // Photo1
                        imageViewAddPhoto1.setImageBitmap(scaled);
                        photo1Path = path;
                        break;
                    case 2:
                        // Photo2
                        imageViewAddPhoto2.setImageBitmap(scaled);
                        photo2Path = path;
                        break;
                    case 3:
                        // Photo3
                        imageViewAddPhoto3.setImageBitmap(scaled);
                        photo3Path = path;
                        break;

                }



            }

        }
        else if (requestCode==CAPTURE_PHOTO_SELECT && resultCode==Activity.RESULT_OK)
        {
            galleryAddPic();

            File imgFile = new  File(mCurrentPhotoPath);

            if(imgFile.exists()){

                //Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                //Bitmap myBitmap = null;
                try {
                    Bitmap myBitmap = checkOrientation(mCurrentPhotoPath);



                    updateFileWithScaledBitmap(mCurrentPhotoPath,myBitmap);

                    switch (this.photoType) {
                        case 0:
                            // Company Logo
                            imageViewLogoUpload.setImageBitmap(myBitmap);
                            companyLogoPath = mCurrentPhotoPath;
                            break;
                        case 1:
                            // Photo1
                            imageViewAddPhoto1.setImageBitmap(myBitmap);
                            photo1Path = mCurrentPhotoPath;
                            break;
                        case 2:
                            // Photo2
                            imageViewAddPhoto2.setImageBitmap(myBitmap);
                            photo2Path = mCurrentPhotoPath;
                            break;
                        case 3:
                            // Photo3
                            imageViewAddPhoto3.setImageBitmap(myBitmap);
                            photo3Path = mCurrentPhotoPath;
                            break;

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }




            }


        }}

    // Method to detect image rotation and change orientation if the image has been rotated
    private Bitmap checkOrientation(String file) throws IOException {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file, bounds);

        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bm = BitmapFactory.decodeFile(file, opts);

        // Scale down bitmap
        int nh = (int) ( bm.getHeight() * (512.0 / bm.getWidth()) );
        Bitmap scaled = Bitmap.createScaledBitmap(bm, 512, nh, true);

        ExifInterface exif = new ExifInterface(file);
        String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int orientation = orientString != null ? Integer.parseInt(orientString) :  ExifInterface.ORIENTATION_NORMAL;

        int rotationAngle = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;

        Matrix matrix = new Matrix();
        matrix.setRotate(rotationAngle, (float) scaled.getWidth() / 2, (float) scaled.getHeight() / 2);
        Bitmap rotatedBitmap = Bitmap.createBitmap(scaled, 0, 0, scaled.getWidth(), scaled.getHeight(), matrix, true);
        return rotatedBitmap;
    }
}
