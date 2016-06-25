package au.com.appscore.mrtradie;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.List;

import au.com.appscore.mrtradie.utils.ControlPraser;

/**
 * Created by adityathakar on 20/08/15.
 */
public class BusinessListAdapter extends BaseAdapter implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    Context context;
    ArrayList<Business> businesses;
    LayoutInflater layoutInflater;

    public BusinessListAdapter(Context context,ArrayList<Business> businesses)
    {
        this.context = context;
        this.businesses = businesses;
        layoutInflater = ((Activity) context).getLayoutInflater();

    }


    @Override
    public int getCount() {
        return businesses.size();
    }

    @Override
    public Object getItem(int position) {
        return businesses.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //ViewHolder viewHolder;

        //if (convertView==null) {

            convertView = layoutInflater.inflate(R.layout.businesslist_item,parent,false);

            //viewHolder = new ViewHolder();
            TextView textViewBusinessName = (TextView) convertView.findViewById(R.id.textViewBusinessName);
            TextView textViewBusinessAddress = (TextView) convertView.findViewById(R.id.textViewBusinessAddress);
            ImageView imageViewGetQuote = (ImageView) convertView.findViewById(R.id.imageButtonGetQuote);
            ImageView imageViewBusinessImage = (ImageView) convertView.findViewById(R.id.imageViewBusinessImage);
            LinearLayout linearLayoutRatingBar = (LinearLayout) convertView.findViewById(R.id.ratingBar);
            TextView distance = ControlPraser.PraserControl(convertView, R.id.business_distance);
            TextView score = ControlPraser.PraserControl(convertView, R.id.business_score);

        double latitude;
        double longitude;
            Location mLastLocation = getLastKnownLocation();
        try {
             latitude = Double.parseDouble(businesses.get(position).getLatitude());
             longitude = Double.parseDouble(businesses.get(position).getLongitude());
        }catch(Exception e)
        {
            latitude = 0;
            longitude = 0;
        }
            String dis = "Uncertain ";

            int scoreValue = 0;
            Location loc = new Location("target");
            loc.setLatitude(latitude);
            loc.setLongitude(longitude);
            if(mLastLocation != null) {
                float distanceTo = mLastLocation.distanceTo(loc) / 1000;
                dis = String.format("%s", distanceTo);
            }
            //convertView.setTag(viewHolder);

//        }
//        else
//        {
//            viewHolder = (ViewHolder) convertView.getTag();
//        }

        final Business business = businesses.get(position);

        if (business!=null) {
            textViewBusinessName.setText(business.getName());
//            if (business.getFullAddress()!=null)
//                viewHolder.textViewBusinessAddress.setText(business.getStreet()+" "+business.getCity()+"\n"+business.getState()+" "+business.getCountry());
            if (!business.getFullAddress().equals("null"))
                textViewBusinessAddress.setText(business.getFullAddress());
            if (!business.getRating().equals("null"))
            {
                int ratingValue = (int) Float.parseFloat(business.getRating());
                if (ratingValue <= 5)
                {
                    for (int i=0; i < ratingValue; i++){
                        ImageView v = (ImageView) linearLayoutRatingBar.getChildAt(i);
                        v.setImageResource(R.drawable.icon_star_full);
                        scoreValue += 20;
                    }
                }

            }

            distance.setText("");
            distance.setVisibility(View.GONE);
            score.setText("");
            score.setVisibility(View.GONE);

            Ion.with(imageViewBusinessImage)
                    .load(business.getCompanyLogo());



            imageViewGetQuote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context,RequestQuoteScreen.class);
                    intent.putExtra("quote_email",business.getEmail());

                    context.startActivity(intent);
                }
            });
        }

        return convertView;
    }


    private Location getLastKnownLocation() {
        LocationManager mLocationManager;
        mLocationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }
    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

//    static class ViewHolder {
//        LinearLayout ratingBar;
//        ImageView imageViewBusinessImage;
//        ImageView imageViewGetQuoteButton;
//        TextView textViewBusinessName;
//        TextView textViewBusinessAddress;
//    }
}
