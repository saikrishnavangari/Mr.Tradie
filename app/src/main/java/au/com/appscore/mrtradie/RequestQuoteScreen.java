package au.com.appscore.mrtradie;

import android.app.Activity;
import android.app.Dialog;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.actionsheet.ActionSheet;
import com.github.jjobes.slidedatetimepicker.SlideDateTimeListener;
import com.github.jjobes.slidedatetimepicker.SlideDateTimePicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import au.com.appscore.mrtradie.camerautility.AlbumStorageDirFactory;
import au.com.appscore.mrtradie.camerautility.BaseAlbumDirFactory;
import au.com.appscore.mrtradie.camerautility.FroyoAlbumDirFactory;
import au.com.appscore.mrtradie.utils.ControlPraser;

public class RequestQuoteScreen extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ActionSheet.ActionSheetListener  {

    // Variables for camera utility
    private static int IMAGE_PICKER_SELECT = 1;

    private static int CAPTURE_PHOTO_SELECT = 2;

    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";

    private AlbumStorageDirFactory mAlbumStorageDirFactory = null;

    String mCurrentPhotoPath;
    String photo1Path = null;
    String photo2Path = null;
    String photo3Path = null;
    int photoType=0;

    // Variables for Google API

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private Location mLastLocation;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    private Toolbar toolbar;
    private EditText editTextQuoteDesc, editTextAddress,editTextTime;
    private ImageView imageViewAddress;
    private TextView textViewStartTime,textViewEndTime;
    private LinearLayout linearLayoutStartEndTime;
    private Switch switchAllDay;
    private ImageView imageViewAddPhoto1, imageViewAddPhoto2, imageViewAddPhoto3;
    private SwitchCompat calloutSwitch;

    private String quote_email;
    private JSONObject jsonObjectAddress = null;
    private JSONArray jsonArrayAvailability = new JSONArray();
    private SimpleDateFormat mFormatter = new SimpleDateFormat("EEE dd MMM yyyy hh:mm a ");

    private SharedPreferences sharedPreferences;

    ProgressDialog pDialog;

    private SlideDateTimeListener listenerStartTime = new SlideDateTimeListener() {

        @Override
        public void onDateTimeSet(Date date)
        {
            Toast.makeText(RequestQuoteScreen.this,
                    mFormatter.format(date), Toast.LENGTH_SHORT).show();
            textViewStartTime.setText(" " + mFormatter.format(date));
        }

        // Optional cancel listener
        @Override
        public void onDateTimeCancel()
        {
            Toast.makeText(RequestQuoteScreen.this,
                    "Canceled", Toast.LENGTH_SHORT).show();
        }
    };

    private SlideDateTimeListener listenerEndTime = new SlideDateTimeListener() {

        @Override
        public void onDateTimeSet(Date date)
        {
            Toast.makeText(RequestQuoteScreen.this,
                    mFormatter.format(date), Toast.LENGTH_SHORT).show();
            textViewEndTime.setText(" " + mFormatter.format(date));
        }

        // Optional cancel listener
        @Override
        public void onDateTimeCancel()
        {
            Toast.makeText(RequestQuoteScreen.this,
                    "Canceled", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_quote_screen);

        sharedPreferences = getApplicationContext().getSharedPreferences(AppUtils.SharedPreferenceFileName,0);

        // Camera Utility

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
        } else {
            mAlbumStorageDirFactory = new BaseAlbumDirFactory();
        }

        pDialog = new ProgressDialog(RequestQuoteScreen.this);
        pDialog.setMessage("Requesting quote...");

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.PrimaryDarkColor));
        }

        // Retrieve business email
        quote_email = getIntent().getStringExtra("quote_email");
        calloutSwitch = ControlPraser.PraserControl(this, R.id.call_out_switch);
        calloutSwitch.setChecked(false);
        // Retrieve edit text for quote description
        Toolbar viewToolbar = (Toolbar)findViewById(R.id.toolbar);
        editTextQuoteDesc = (EditText) viewToolbar.findViewById(R.id.editTextQuoteDesc);
        editTextAddress = (EditText) findViewById(R.id.editTextAddress);
        imageViewAddress = (ImageView) findViewById(R.id.imageViewAddress);
        editTextTime = (EditText) findViewById(R.id.editTextTime);
        linearLayoutStartEndTime = (LinearLayout) findViewById(R.id.linearLayoutStartEndTime);
        imageViewAddPhoto1 = (ImageView) findViewById(R.id.imageViewAddPhoto1);
        imageViewAddPhoto2 = (ImageView) findViewById(R.id.imageViewAddPhoto2);
        imageViewAddPhoto3 = (ImageView) findViewById(R.id.imageViewAddPhoto3);

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

        editTextTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // custom dialog
                final Dialog dialog = new Dialog(RequestQuoteScreen.this);
                dialog.setContentView(R.layout.addtime_dialog_layout);
                dialog.setTitle("Select Timing");
                dialog.setCancelable(false);

                // set the custom dialog components - text, image and button

                Button dialogButtonOK = (Button) dialog.findViewById(R.id.buttonOK);
                Button dialogButtonCancel = (Button) dialog.findViewById(R.id.buttonCancel);
                textViewStartTime = (TextView) dialog.findViewById(R.id.textViewStartTime);
                textViewEndTime = (TextView) dialog.findViewById(R.id.textViewEndTime);
                switchAllDay = (Switch) dialog.findViewById(R.id.switchAllDay);

                textViewStartTime.setText(mFormatter.format(new Date()));
                textViewStartTime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new SlideDateTimePicker.Builder(getSupportFragmentManager())
                                .setListener(listenerStartTime)
                                .setInitialDate(new Date())
                                .setMinDate(new Date())
                                        //.setMaxDate(maxDate)
                                        //.setIs24HourTime(true)
                                        //.setTheme(SlideDateTimePicker.HOLO_DARK)
                                        //.setIndicatorColor(getResources().getColor(R.color.PrimaryColor))
                                .build()
                                .show();
                    }
                });

                // Add 8 hours to current time
                final Calendar cal = Calendar.getInstance(); // creates calendar
                cal.setTime(new Date()); // sets calendar time/date
                cal.add(Calendar.HOUR_OF_DAY, 8); // adds eight hour
                textViewEndTime.setText(mFormatter.format(cal.getTime()));
                textViewEndTime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        new SlideDateTimePicker.Builder(getSupportFragmentManager())
                                .setListener(listenerEndTime)
                                .setInitialDate(cal.getTime())
                                .setMinDate(new Date())
                                        //.setMaxDate(maxDate)
                                        //.setIs24HourTime(true)
                                        //.setTheme(SlideDateTimePicker.HOLO_DARK)
                                        //.setIndicatorColor(getResources().getColor(R.color.PrimaryColor))
                                .build()
                                .show();
                    }
                });
                // if button is clicked, close the custom dialog
                dialogButtonOK.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Display Start End Time
                        View view = getLayoutInflater().inflate(R.layout.start_end_time_layout, null);
                        TextView textViewStartTimeToDisplay = (TextView) view.findViewById(R.id.textViewStartTime);
                        TextView textViewEndTimeToDisplay = (TextView) view.findViewById(R.id.textViewEndTime);
                        textViewStartTimeToDisplay.setText(textViewStartTime.getText());
                        textViewEndTimeToDisplay.setText(textViewEndTime.getText());

                        linearLayoutStartEndTime.addView(view);

                        // Add record to availability array
                        JSONObject jsonObjectAvailability = new JSONObject();
                        try {
                            jsonObjectAvailability.put("is_available",1);
                            jsonObjectAvailability.put("start_date",textViewStartTime.getText());
                            jsonObjectAvailability.put("end_date",textViewEndTime.getText());

                            if (switchAllDay.isChecked())
                                jsonObjectAvailability.put("all_day",1);
                            else
                                jsonObjectAvailability.put("all_day", 0);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        jsonArrayAvailability.put(jsonObjectAvailability);

                        dialog.dismiss();
                    }
                });
                dialogButtonCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });


        imageViewAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayLocation();
            }
        });

        // First we need to check availability of play services
        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();
        }
    }

    // Method to check if all the fields are valid
    private boolean allFieldsValid() {
        if (TextUtils.isEmpty(editTextQuoteDesc.getText())||TextUtils.isEmpty(editTextAddress.getText())||linearLayoutStartEndTime.getChildCount()==0)
            return false;
        else
            return true;
    }

    // Method to display action sheet
    private void showActionSheet(int photoType) {
        this.photoType = photoType;
        ActionSheet.createBuilder(this, getSupportFragmentManager())
                .setCancelButtonTitle("Cancel")
                .setOtherButtonTitles("Take Photo", "Choose from Camera Roll")
                .setCancelableOnTouchOutside(true).setListener(this).show();
    }

    /**
     * Method to display the location on UI
     * */
    private void displayLocation() {

        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            jsonObjectAddress = new JSONObject();


            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

            // Add lat and long to json object of address
            try {
                jsonObjectAddress.put("latitude",latitude);
                jsonObjectAddress.put("longitude",longitude);
            } catch (JSONException e) {
                e.printStackTrace();
            }


            Log.d("Debug","Latitude : "+latitude+" Longitude "+longitude);

            // Test display address
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
                editTextAddress.setText(address+" "+city+" "+state+" "+postalCode);

                // Add additional address fields to json object of address
                jsonObjectAddress.put("full_address",address+" "+city+" "+state+" "+postalCode);
                jsonObjectAddress.put("street",address);
                jsonObjectAddress.put("city",city);
                jsonObjectAddress.put("state",state);
                jsonObjectAddress.put("post_code",postalCode);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }


        } else {

            Log.d("Debug","Could not get current location");
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_request_quote_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_done) {
            // Check if all required fields are filled
            if (allFieldsValid())
            {
                // Submit quote to the server
                AsyncHttpClient client = new AsyncHttpClient();
                client.setResponseTimeout(120000);
                final RequestParams params = new RequestParams();

                    // Add parameters

                params.put("access_token",sharedPreferences.getString(AppUtils.KEY_ACCESS_TOKEN,null));
                params.put("budget",0);
                params.put("comments","");
                params.put("email",sharedPreferences.getString(AppUtils.KEY_EMAIL,null));
                params.put("quote_desc",editTextQuoteDesc.getText());
                params.put("quote_email",quote_email);
                params.put("address",jsonObjectAddress);
                params.put("availability",jsonArrayAvailability);
                params.put("is_call_out", calloutSwitch.isChecked()?1:0);

                try {
                    if (photo1Path!=null)
                        params.put("photo1",new File(photo1Path));
                    if (photo2Path!=null)
                        params.put("photo2",new File(photo2Path));
                    if (photo3Path!=null)
                        params.put("photo3",new File(photo3Path));
                } catch (FileNotFoundException e) {
                    new AlertDialog.Builder(RequestQuoteScreen.this)
                            .setTitle("Error")
                            .setMessage("Image Error")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                    e.printStackTrace();
                }

                client.post(AppUtils.addQuoteUrlString(), params, new AsyncHttpResponseHandler() {

                    @Override
                    public void onStart() {
                        super.onStart();
                        pDialog.show();

                    }


                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        pDialog.dismiss();
                        String response = new String(responseBody);
                        Log.d("Debug", response);
                        // Parse JSON data
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONObject jsonData = jsonObject.getJSONObject("data");

                            String mrt_status = jsonData.getString("mrt_status");

                            if (mrt_status.equals("1007"))
                            {
                                // Close the RequestQuoteScreen on success
                                finish();
                            }
                            else {
                                String mrt_desc = jsonData.getString("mrt_desc");
                                new AlertDialog.Builder(RequestQuoteScreen.this)
                                        .setTitle("Error")
                                        .setMessage(mrt_desc)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        })
                                        .show();
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                            new AlertDialog.Builder(RequestQuoteScreen.this)
                                    .setTitle("Server Error")
                                    .setMessage("Sorry ! Could not get data from server.")
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
                        new AlertDialog.Builder(RequestQuoteScreen.this)
                                .setTitle("Server Error")
                                .setMessage("Could not request quote at the moment.")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .show();
                    }
                });

            }
            else
            {
                new AlertDialog.Builder(RequestQuoteScreen.this)
                        .setTitle("Validation")
                        .setMessage("Please fill in all fields and ensure date and time is added.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        displayLocation();
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

    @Override
    public void onDismiss(ActionSheet actionSheet, boolean b) {
        //Toast.makeText(getApplicationContext(), "dismissed isCancle = " + b,Toast.LENGTH_SHORT).show();
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

    // Methods for camera utility

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
        RequestQuoteScreen.this.sendBroadcast(mediaScanIntent);
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

            final String path = getImagePathFromCameraData(data,RequestQuoteScreen.this);

            File imgFile = new  File(path);

            if(imgFile.exists()){

                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                // Scale down bitmap
                int nh = (int) ( myBitmap.getHeight() * (512.0 / myBitmap.getWidth()) );
                Bitmap scaled = Bitmap.createScaledBitmap(myBitmap, 512, nh, true);

                switch (this.photoType) {
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
        Bitmap bm = BitmapFactory.decodeFile        (file, opts);

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
