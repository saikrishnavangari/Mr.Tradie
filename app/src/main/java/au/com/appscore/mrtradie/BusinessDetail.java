package au.com.appscore.mrtradie;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.daimajia.slider.library.SliderLayout;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import au.com.appscore.mrtradie.Dialog.ReportRatingDialog;

public class BusinessDetail extends ActionBarActivity {

    Toolbar toolbar;
    private GoogleMap myMap;
    private Location location;
    private ImageView roundedImageViewBusiness;
    private TextView textViewReviewsCount, textViewCompanyAddress, textViewCompanyDesc;
    private LinearLayout imageButtonWebsite, imageButtonEmail, imageButtonCall, imageButtonShare;
    private ImageView ivGetQuote;
   // private ImageView imageViewPhoto1, imageViewPhoto2, imageViewPhoto3;
    private LinearLayout linearLayoutReviews, linearLayoutReviewsList;
    private SliderLayout sliderLayout;
    CallbackManager callbackManager;
    ShareDialog shareDialog;
    Business business;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_detail);

        // Initializing Toolbar and setting it as the actionbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);

        ((WorkaroundMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).setListener(new WorkaroundMapFragment.OnTouchListener() {
            @Override
            public void onTouch() {
                scrollView.requestDisallowInterceptTouchEvent(true);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.PrimaryDarkColor));
        }

        // Retrieve business details
        business = (Business) getIntent().getSerializableExtra("BusinessDetail");

        initialiseUIElements();


        JSONArray jsonArrayReviews = null;
        try {
            jsonArrayReviews = new JSONArray(business.getJsonArrayReviews());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Set Business details
        getSupportActionBar().setTitle(business.getName());
        Ion.with(roundedImageViewBusiness).load(business.getCompanyLogo());
        //Ion.with(imageViewPhoto1).load(business.getPhoto1());
        //Ion.with(imageViewPhoto2).load(business.getPhoto2());
        //Ion.with(imageViewPhoto3).load(business.getPhoto3());
        if (!business.getFullAddress().equals("null"))
            textViewCompanyAddress.setText(business.getFullAddress());
        if (!business.getAboutCompany().equals("null"))
        {
            textViewCompanyDesc.setText(business.getAboutCompany());
        }
        else
        {
            textViewCompanyDesc.setText("No information available");
        }

        if (jsonArrayReviews!=null&&jsonArrayReviews.length()>0) {
            textViewReviewsCount.setText(jsonArrayReviews.length() == 1 ? "1 Review" : jsonArrayReviews.length() + " Reviews");
            for (int i=0;i<jsonArrayReviews.length();i++)
            {
                View view = getLayoutInflater().inflate(R.layout.reviews_layout, null);
                TextView textViewReview = (TextView) view.findViewById(R.id.textViewReview);
                LinearLayout linearLayoutReviewsStars = (LinearLayout) view.findViewById(R.id.linearLayoutReviews);
                    try {
                    JSONObject jsonObjectReview = jsonArrayReviews.getJSONObject(i);
                    textViewReview.setText(jsonObjectReview.getString("comments"));
                        int ratingValue = (int) Float.parseFloat(jsonObjectReview.getString("star_ranking"));
                        if (ratingValue<=5)
                        {
                            for (int j=0; j < ratingValue; j++){
                                ImageView v = (ImageView) linearLayoutReviewsStars.getChildAt(j);
                                v.setImageResource(R.drawable.star_orange_full);
                            }
                        }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                linearLayoutReviewsList.addView(view);
            }
        }
        else {
            View view = getLayoutInflater().inflate(R.layout.reviews_layout, null);
            TextView textViewReview = (TextView) view.findViewById(R.id.textViewReview);
            LinearLayout linearLayoutReviewsStars = (LinearLayout) view.findViewById(R.id.linearLayoutReviews);
            linearLayoutReviewsStars.setVisibility(View.GONE);
            textViewReview.setText("No Reviews");
            linearLayoutReviewsList.addView(view);

        }

        // Set Rating stars
        if (!business.getRating().equals("null"))
        {
            int ratingValue = (int) Float.parseFloat(business.getRating());
            if (ratingValue<=5)
            {
                for (int i=0; i < ratingValue; i++){
                    ImageView v = (ImageView) linearLayoutReviews.getChildAt(i);
                    v.setImageResource(R.drawable.star_white_full);
                }
            }

        }

        // Placing marker on the map
        FragmentManager myFragmentManager = getSupportFragmentManager();
        SupportMapFragment mySupportMapFragment
                = (SupportMapFragment)myFragmentManager.findFragmentById(R.id.map);
        myMap = mySupportMapFragment.getMap();
        myMap.setMyLocationEnabled(true);

        if (!business.getLatitude().equals("null")&&!business.getLongitude().equals("null"))
        {
            myMap.addMarker(new MarkerOptions().position(new LatLng(Float.parseFloat(business.getLatitude()), Float.parseFloat(business.getLongitude()))).title(business.getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.yellow_marker)));
            LatLng coordinate = new LatLng(Float.parseFloat(business.getLatitude()), Float.parseFloat(business.getLongitude()));
            CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 10);
            myMap.animateCamera(yourLocation);

        }

        ArrayList<String> additionalPhotos = new ArrayList<>();
        additionalPhotos.add(business.getPhoto1());
        additionalPhotos.add(business.getPhoto2());
        additionalPhotos.add(business.getPhoto3());

        for (String url: additionalPhotos)
        {
            ImageSliderView defaultSliderView = new ImageSliderView(BusinessDetail.this);
            defaultSliderView.image(url);
            sliderLayout.addSlider(defaultSliderView);
        }
    }

    private void initialiseUIElements() {
        imageButtonWebsite = (LinearLayout) findViewById(R.id.imageButtonWebsite);
        imageButtonEmail = (LinearLayout) findViewById(R.id.imageButtonEmail);
        imageButtonCall = (LinearLayout) findViewById(R.id.imageButtonCall);
        imageButtonShare = (LinearLayout) findViewById(R.id.imageButtonShare);
        //imageViewPhoto1 = (ImageView) findViewById(R.id.imageViewPhoto1);
        //imageViewPhoto2 = (ImageView) findViewById(R.id.imageViewPhoto2);
        //imageViewPhoto3 = (ImageView) findViewById(R.id.imageViewPhoto3);
        roundedImageViewBusiness = (ImageView) findViewById(R.id.imageViewBusinessImage);
        linearLayoutReviews = (LinearLayout) findViewById(R.id.linearLayoutReviews);
        textViewReviewsCount = (TextView) findViewById(R.id.textViewReviewsCount);
        textViewCompanyAddress = (TextView) findViewById(R.id.textViewCompanyAddress);
        textViewCompanyDesc = (TextView) findViewById(R.id.textViewCompanyDesc);
        linearLayoutReviewsList = (LinearLayout) findViewById(R.id.businessDetailContent);
        sliderLayout = (SliderLayout) findViewById(R.id.slider);
        sliderLayout.stopAutoCycle();

        ivGetQuote = (ImageView) findViewById(R.id.get_quote);

        ivGetQuote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BusinessDetail.this,RequestQuoteScreen.class);
                intent.putExtra("quote_email",business.getEmail());
                startActivity(intent);
            }
        });


        imageButtonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ShareDialog.canShow(ShareLinkContent.class)) {
                    ShareLinkContent linkContent = new ShareLinkContent.Builder()
                            .setContentTitle("Mr. Tradie")
                            .setContentDescription(
                                    "I found "+business.getName()+" on Mr. Tradie Android App")
                            .setContentUrl(Uri.parse("http://www.com.appscore.adityathakar.com.appscore.adityathakar.mrtradie.com.au/"))
                            .build();

                    shareDialog.show(linkContent);
                }
            }
        });

        imageButtonWebsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Patterns.WEB_URL.matcher(business.getWebsite()).matches())
                {
                    // Add http:// as suffix if the web address does not contain it
                    String url = null;
                    if (business.getWebsite().contains("http://"))
                        url = business.getWebsite();
                    else
                        url = "http://" + business.getWebsite();
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }
                else
                {
                    new AlertDialog.Builder(BusinessDetail.this)
                            .setTitle("Error")
                            .setMessage("This Tradie does not have a valid website on record.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                }
            }
        });

        imageButtonEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AppUtils.isValidEmail(business.getEmail()))
                {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto",business.getEmail(), null));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Mr. Tradie enquiry");
                    startActivity(Intent.createChooser(emailIntent, "Send email..."));
                }
                else {
                    new AlertDialog.Builder(BusinessDetail.this)
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

        imageButtonCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + business.getPhoneNumber()));
                startActivity(intent);
            }
        });

        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                Log.d("Debug","Shared on FB");
            }

            @Override
            public void onCancel() {
                Log.d("Debug","Canceled sharing on FB");
            }

            @Override
            public void onError(FacebookException e) {
                new AlertDialog.Builder(BusinessDetail.this)
                        .setTitle("Error")
                        .setMessage("Please check your Internet connection")
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
        getMenuInflater().inflate(R.menu.menu_business_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.home) {
            finish();
            return true;
        }

        if (id == R.id.action_report_rating)
        {
            ReportRatingDialog dialog = new ReportRatingDialog();
            dialog.show(getFragmentManager(), "");
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}
