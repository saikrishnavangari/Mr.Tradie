package au.com.appscore.mrtradie.Search.Item;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import au.com.appscore.mrtradie.Business;
import au.com.appscore.mrtradie.RequestQuoteScreen;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by lijiazhou on 5/02/16.
 */
public class SearchItemAdapter extends BaseAdapter {

    LinkedList<SearchItem> items;
    List<Business> dataList;
    Context context;

    public SearchItemAdapter(List<Business> list, Context contex)
    {
        items = new LinkedList<>();
        dataList = list;
        this.context = contex;
        initComponents();
    }

    public SearchItemAdapter(List<Business> list, Context context, int sort)
    {
        items = new LinkedList<>();
        dataList = list;
        this.context = context;
        sort(sort);
        initComponents();
    }

    private void sort(int sort)
    {
        switch (sort)
        {
            case 2:
                dataList = sortByDistance();
                break;
            case 3:
                dataList = sortByScore();
                break;
            case 1:
                dataList = sortByName();
                break;
        }
    }

    private LinkedList sortByDistance()
    {
        LinkedList<Business> sortedList = new LinkedList<>();
        for(int i = 0; i < dataList.size(); i++)
        {
            if(sortedList.size() == 0) {
                sortedList.add(dataList.get(i));
                continue;
            }
            for(int j = 0; j < sortedList.size(); j++)
            {
                if(dataList.get(i).distance < sortedList.get(j).distance)
                {
                    sortedList.add(j, dataList.get(i));
                    break;
                }
                if(j == sortedList.size() - 1 ) {
                    sortedList.add(dataList.get(i));
                    break;
                }
            }
        }
        return sortedList;
    }

    private LinkedList sortByScore()
    {
        LinkedList<Business> sortedList = new LinkedList<>();
        for(int i = 0; i < dataList.size(); i++)
        {
            if(sortedList.size() == 0) {
                sortedList.add(dataList.get(i));
                continue;
            }
            for(int j = 0; j < sortedList.size(); j++)
            {
                if(dataList.get(i).score > sortedList.get(j).score)
                {
                    sortedList.add(j, dataList.get(i));
                    break;
                }
                if(j == sortedList.size() - 1 ) {
                    sortedList.add(dataList.get(i));
                    break;
                }
            }
        }
        return sortedList;
    }

    private LinkedList sortByName()
    {
        String a = "a";
        a.compareToIgnoreCase(a);
        LinkedList<Business> sortedList = new LinkedList<>();
        for(int i = 0; i < dataList.size(); i++)
        {
            if(sortedList.size() == 0) {
                sortedList.add(dataList.get(i));
                continue;
            }
            for(int j = 0; j < sortedList.size(); j++)
            {
                if(dataList.get(i).getName().compareToIgnoreCase(sortedList.get(j).getName()) <= 0)
                {
                    sortedList.add(j, dataList.get(i));
                    break;
                }
                if(j == sortedList.size() - 1 ) {
                    sortedList.add(dataList.get(i));
                    break;
                }
            }
        }

        return sortedList;
    }

    private void initComponents()
    {
        Location mLastLocation = getLastKnownLocation();
        for(Object obj : dataList)
        {
            SearchItem item = new SearchItem(context);//, ControlPraser.GetAttr(context, R.layout.businesslist_item));
            final Business business = (Business)obj;
            item.setCompany(business.getName());
            item.setAddress(business.getFullAddress());
            if(!business.getRating().equals("null")) {
                item.setRatingBar((int) Float.parseFloat(business.getRating()));
                business.score = (business.equals("null")? 0 : (int)Float.parseFloat(business.getRating())) * 20;
            }
            else
                item.setRatingBar(0);
            item.setCompanyImage(business.getCompanyLogo());
            double latitude;
            double longitude;
            try {
                latitude = Double.parseDouble(business.getLatitude());
                longitude = Double.parseDouble(business.getLongitude());
            }catch(Exception e)
            {
                latitude = 0;
                longitude = 0;
            }
            String distance = "UnCertain";
            if(mLastLocation != null && latitude + longitude != 0)
            {
                Location loc = new Location("target");
                loc.setLatitude(latitude);
                loc.setLongitude(longitude);
                float distanceTo = mLastLocation.distanceTo(loc) / 1000;
                distance = String.format("%s", distanceTo) + " km";
                business.distance = distanceTo;
            }

            item.setDistance(distance);
            item.setQuoteButtonListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, RequestQuoteScreen.class);
                    intent.putExtra("quote_email", business.getEmail());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        context.startActivity(intent);
                    } catch (Exception e) {
                        e.fillInStackTrace();
                        return;
                    }
                }
            });
            items.add(item);
        }
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
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = items.get(position);
        return convertView;
    }
}
