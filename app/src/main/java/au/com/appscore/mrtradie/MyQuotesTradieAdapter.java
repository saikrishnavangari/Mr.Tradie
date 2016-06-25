package au.com.appscore.mrtradie;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by adityathakar on 9/09/15.
 */
public class MyQuotesTradieAdapter extends BaseAdapter {

    Context context;
    JSONArray jsonArrayRecords;

    public MyQuotesTradieAdapter(Context context, JSONArray jsonArrayRecords) {
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
            return jsonArrayRecords.getJSONObject(position);
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
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView==null) {

            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(R.layout.quotes_list_item,parent,false);

            viewHolder = new ViewHolder();
            viewHolder.textViewName = (TextView) convertView.findViewById(R.id.textViewName);
            viewHolder.textViewQuoteStatus = (TextView) convertView.findViewById(R.id.textViewQuoteStatus);
            viewHolder.textViewQuoteLabel = (TextView) convertView.findViewById(R.id.textViewQuoteLabel);
            viewHolder.textViewQuote = (TextView) convertView.findViewById(R.id.textViewQuote);
            viewHolder.textViewAddress = (TextView) convertView.findViewById(R.id.textViewAddress);
            viewHolder.imageViewArrowMore = (ImageView) convertView.findViewById(R.id.imageViewArrowMore);

            convertView.setTag(viewHolder);

        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        try {
            JSONObject jsonObjectRecord = jsonArrayRecords.getJSONObject(position);
            JSONObject jsonObjectQuote = jsonObjectRecord.getJSONObject("quote");
            JSONObject jsonObjectAddress = jsonObjectRecord.getJSONObject("address");
            JSONObject jsonObjectUserProfile = jsonObjectQuote.getJSONObject("user_profile");
            viewHolder.textViewName.setText(jsonObjectUserProfile.getString("user_full_name"));
            viewHolder.textViewAddress.setText(jsonObjectAddress.getString("full_address"));

            viewHolder.imageViewArrowMore.setVisibility(View.VISIBLE);

            if (jsonObjectQuote.getString("quote_status").equals("New"))
                viewHolder.textViewQuoteStatus.setText("Not Replied");
            else
                viewHolder.textViewQuoteStatus.setText(jsonObjectQuote.getString("quote_status"));


            if (jsonObjectQuote.getString("quote_status").equals("Replied")&&!jsonObjectQuote.getString("budget").equals("0"))
            {
                viewHolder.textViewQuoteLabel.setVisibility(View.VISIBLE);
                viewHolder.textViewQuote.setVisibility(View.VISIBLE);
                viewHolder.textViewQuote.setText("$"+jsonObjectQuote.getString("budget"));
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
        TextView textViewQuoteStatus;
        TextView textViewQuoteLabel;
        TextView textViewQuote;
        TextView textViewAddress;
        ImageView imageViewArrowMore;
    }
}
