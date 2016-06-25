package au.com.appscore.mrtradie;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by adityathakar on 23/08/15.
 */
public class MyJobsCustomerAdapter extends BaseAdapter {

    Context context;
    JSONArray jsonArrayRecords;

    public MyJobsCustomerAdapter(Context context, JSONArray jsonArrayRecords) {
        this.context = context;
        this.jsonArrayRecords = jsonArrayRecords;
    }

    @Override
    public int getCount() {
        return jsonArrayRecords.length();
    }

    @Override
    public Object getItem(int position) {
        try {
            return jsonArrayRecords.get(position);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView==null) {

            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(R.layout.jobs_list_item,parent,false);

            viewHolder = new ViewHolder();
            viewHolder.textViewName = (TextView) convertView.findViewById(R.id.textViewName);
            viewHolder.textViewQuoteLabel = (TextView) convertView.findViewById(R.id.textViewQuoteLabel);
            viewHolder.textViewQuote = (TextView) convertView.findViewById(R.id.textViewQuote);
            viewHolder.textViewQuoteDateLabel = (TextView) convertView.findViewById(R.id.textViewQuoteDateLabel);
            viewHolder.textViewQuoteDate = (TextView) convertView.findViewById(R.id.textViewQuoteDate);
            viewHolder.textViewAddress = (TextView) convertView.findViewById(R.id.textViewAddress);

            convertView.setTag(viewHolder);

        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        try {
            JSONObject jsonObjectRecord = jsonArrayRecords.getJSONObject(position);
            JSONObject jsonObjectJob = jsonObjectRecord.getJSONObject("job");
            JSONObject jsonObjectAddress = jsonObjectRecord.getJSONObject("address");
            JSONObject jsonObjectUserProfile = jsonObjectJob.getJSONObject("user_profile");
            viewHolder.textViewName.setText(jsonObjectUserProfile.getString("company"));
            viewHolder.textViewAddress.setText(jsonObjectAddress.getString("full_address"));


            if (!jsonObjectJob.getString("budget").equals("0"))
            {
                viewHolder.textViewQuoteLabel.setVisibility(View.VISIBLE);
                viewHolder.textViewQuote.setVisibility(View.VISIBLE);
                viewHolder.textViewQuote.setText("$" + jsonObjectJob.getString("budget"));

            }
            else {
                viewHolder.textViewQuoteLabel.setVisibility(View.INVISIBLE);
                viewHolder.textViewQuote.setVisibility(View.INVISIBLE);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }


        return convertView;
    }

    static class ViewHolder {
        TextView textViewName;
        TextView textViewQuoteLabel;
        TextView textViewQuote;
        TextView textViewQuoteDateLabel;
        TextView textViewQuoteDate;
        TextView textViewAddress;
    }
}
