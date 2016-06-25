package au.com.appscore.mrtradie;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by adityathakar on 24/08/15.
 */
public class MyQuotesFragment extends Fragment {

    SharedPreferences sharedPreferences;
    ListView listViewQuotes;
    TextView textViewNoQuotesLabel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.my_quotes_fragment,container,false);

        sharedPreferences = getActivity().getSharedPreferences(AppUtils.SharedPreferenceFileName, 0);

        listViewQuotes = (ListView) v.findViewById(R.id.listViewQuotes);
        textViewNoQuotesLabel = (TextView) v.findViewById(R.id.textViewNoQuotes);

        queryServer();

        return v;
    }

    private void queryServer() {
        // Tag used to cancel the request
        String tag_json_obj = "json_obj_req";

        String url = AppUtils.getQuotesURL(sharedPreferences.getString(AppUtils.KEY_EMAIL,null),sharedPreferences.getString(AppUtils.KEY_ACCESS_TOKEN,null));

        final ProgressDialog pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage("Fetching Quotes...");
        pDialog.show();
        pDialog.setCancelable(false);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                pDialog.dismiss();

                // Parse JSON data
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject jsonData = jsonObject.getJSONObject("data");

                    String mrt_status = jsonData.getString("mrt_status");

                    if (mrt_status.equals("1005"))
                    {
                        final JSONArray jsonArrayRecords = jsonData.getJSONArray("records");

                        if (sharedPreferences.getString(AppUtils.KEY_USER_TYPE,"dummy").equals("0"))
                        {
                            MyQuotesCustomerAdapter myQuotesCustomerAdapter = new MyQuotesCustomerAdapter(getActivity(),jsonArrayRecords);
                            listViewQuotes.setAdapter(myQuotesCustomerAdapter);
                            listViewQuotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Intent intent = new Intent(getActivity(), CustomerQuoteRepliedScreen.class);
                                    try {
                                        intent.putExtra("QuoteDetail", jsonArrayRecords.getJSONObject(position).toString());
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    startActivityForResult(intent,AppUtils.UPDATED);
                                }
                            });
                        }
                        else if (sharedPreferences.getString(AppUtils.KEY_USER_TYPE,"dummy").equals("1"))
                        {
                            MyQuotesTradieAdapter myQuotesTradieAdapter = new MyQuotesTradieAdapter(getActivity(),jsonArrayRecords);
                            listViewQuotes.setAdapter(myQuotesTradieAdapter);
                            listViewQuotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Intent intent = new Intent(getActivity(),TradieQuoteReplyScreen.class);
                                    try {
                                        intent.putExtra("QuoteDetail", jsonArrayRecords.getJSONObject(position).toString());
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    startActivityForResult(intent, AppUtils.UPDATED);
                                }
                            });
                        }




                    }
                    else {
                        textViewNoQuotesLabel.setVisibility(View.VISIBLE);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(getActivity())
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
                new AlertDialog.Builder(getActivity())
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

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(stringRequest, tag_json_obj);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == AppUtils.UPDATED) {
            // Make sure the request was successful
            if (resultCode == AppUtils.RESULT_OK) {
               queryServer();
            }
        }
    }
}
