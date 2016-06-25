package au.com.appscore.mrtradie;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import au.com.appscore.mrtradie.utils.ControlPraser;

/**
 * Created by adityathakar on 23/08/15.
 */
public class MyJobsFragment extends Fragment {

    SharedPreferences sharedPreferences;
    TextView textViewNoJobsLabel, textViewUpcomingLabel, textViewPastLabel;
    NonScrollListView listViewUpcomingJobs, listViewPastJobs;
    TabLayout tabLayout;
    JSONObject jsonObject;
    View delimiter;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.my_jobs_fragment,container,false);

        sharedPreferences = getActivity().getSharedPreferences(AppUtils.SharedPreferenceFileName, 0);

        // Initialise UI Elements
        textViewNoJobsLabel = (TextView) v.findViewById(R.id.textViewNoJobs);
        textViewUpcomingLabel = (TextView) v.findViewById(R.id.textViewUpcomingLabel);
        textViewPastLabel = (TextView) v.findViewById(R.id.textViewPastLabel);

        delimiter = ControlPraser.PraserControl(v, R.id.new_delimiter);
        tabLayout = ControlPraser.PraserControl(v, R.id.sorting_tabs);
        setupTabs();
//        ArrayList<String> myJobs = new ArrayList<>();
//        myJobs.add("Sample1");
//        myJobs.add("Sample2");

        listViewUpcomingJobs = (NonScrollListView) v.findViewById(R.id.listViewUpcomingJobs);
//        MyJobsCustomerAdapter myJobsListAdapterUpcoming = new MyJobsCustomerAdapter(getActivity(),myJobs);
//        listViewUpcomingJobs.setAdapter(myJobsListAdapterUpcoming);

        listViewPastJobs = (NonScrollListView) v.findViewById(R.id.listViewPastJobs);
//        MyJobsCustomerAdapter myJobsListAdapterPast = new MyJobsCustomerAdapter(getActivity(),myJobs);
//        listViewPastJobs.setAdapter(myJobsListAdapterPast);


        queryServer();

        return v;
    }

    private void sortToday()  {
        JSONObject jsonData = null;
        try {
            jsonData = jsonObject.getJSONObject("data");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String mrt_status = null;
        try {
            mrt_status = jsonData.getString("mrt_status");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (mrt_status.equals("1005")) {

            // JSON Arrays for different types of jobs
            final JSONArray jsonArrayUpcomingJobs = new JSONArray();
            final JSONArray jsonArrayPastJobs = new JSONArray();

            JSONArray jsonArrayRecords = null;
            try {
                jsonArrayRecords = jsonData.getJSONArray("records");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < jsonArrayRecords.length(); i++) {
                JSONObject jsonObjectRecord = null;
                try {
                    jsonObjectRecord = jsonArrayRecords.getJSONObject(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String jobStatus = null;
                try {
                    jobStatus = jsonObjectRecord.getString("job_staus");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String date = null;
                try {
                    date = jsonObjectRecord.getJSONObject("job").getString("date").split(" ")[0];
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String time = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
                if(!date.equals(time))
                    continue;
                if (jobStatus.equals("Completed") || jobStatus.equals("Job Rated"))
                    jsonArrayPastJobs.put(jsonObjectRecord);
                else if (jobStatus.equals("New"))
                    jsonArrayUpcomingJobs.put(jsonObjectRecord);
            }
            if (jsonArrayUpcomingJobs.length()>0)
            {
                textViewUpcomingLabel.setVisibility(View.VISIBLE);
                if (sharedPreferences.getString(AppUtils.KEY_USER_TYPE,"dummy").equals("0"))
                {
                    MyJobsCustomerAdapter myJobsCustomerAdapter = new MyJobsCustomerAdapter(getActivity(),jsonArrayUpcomingJobs);
                    listViewUpcomingJobs.setAdapter(null);
                    listViewUpcomingJobs.setAdapter(myJobsCustomerAdapter);
                    listViewUpcomingJobs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(getActivity(), JobDetailScreen.class);
                            try {
                                intent.putExtra("JobDetail",jsonArrayUpcomingJobs.getJSONObject(position).toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            startActivityForResult(intent, AppUtils.UPDATED);
                        }
                    });
                }
                else if (sharedPreferences.getString(AppUtils.KEY_USER_TYPE,"dummy").equals("1"))
                {
                    MyJobsTradieAdapter myJobsTradieAdapterAdapter = new MyJobsTradieAdapter(getActivity(),jsonArrayUpcomingJobs);
                    listViewUpcomingJobs.setAdapter(myJobsTradieAdapterAdapter);
                    listViewUpcomingJobs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(getActivity(), JobDetailScreen.class);
                            try {
                                intent.putExtra("JobDetail",jsonArrayUpcomingJobs.getJSONObject(position).toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            startActivityForResult(intent, AppUtils.UPDATED);
                        }
                    });
                }
            }
            else {
                delimiter.setVisibility(View.INVISIBLE);
                textViewUpcomingLabel.setVisibility(View.INVISIBLE);
                listViewUpcomingJobs.setVisibility(View.INVISIBLE);
                listViewUpcomingJobs.setAdapter(null);
            }
            if (jsonArrayPastJobs.length()>0)
            {
                listViewPastJobs.setVisibility(View.VISIBLE);
                delimiter.setVisibility(View.VISIBLE);
                textViewPastLabel.setVisibility(View.VISIBLE);
                if (sharedPreferences.getString(AppUtils.KEY_USER_TYPE,"dummy").equals("0"))
                {
                    MyJobsCustomerAdapter myJobsCustomerAdapter = new MyJobsCustomerAdapter(getActivity(),jsonArrayPastJobs);
                    listViewPastJobs.setAdapter(myJobsCustomerAdapter);
                    listViewPastJobs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(getActivity(), JobDetailScreen.class);
                            try {
                                intent.putExtra("JobDetail",jsonArrayPastJobs.getJSONObject(position).toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            startActivityForResult(intent, AppUtils.UPDATED);
                        }
                    });
                }
                else if (sharedPreferences.getString(AppUtils.KEY_USER_TYPE,"dummy").equals("1"))
                {
                    MyJobsTradieAdapter myJobsTradieAdapterAdapter = new MyJobsTradieAdapter(getActivity(),jsonArrayPastJobs);
                    listViewPastJobs.setAdapter(myJobsTradieAdapterAdapter);
                    listViewPastJobs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(getActivity(), JobDetailScreen.class);
                            try {
                                intent.putExtra("JobDetail",jsonArrayPastJobs.getJSONObject(position).toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            startActivityForResult(intent, AppUtils.UPDATED);
                        }
                    });
                }
            }
            else {
                if(jsonArrayUpcomingJobs.length() > 0)
                    delimiter.setVisibility(View.INVISIBLE);
                textViewPastLabel.setVisibility(View.INVISIBLE);
                listViewPastJobs.setVisibility(View.GONE);
                //listViewUpcomingJobs.setAdapter(null);
            }
            if(jsonArrayPastJobs.length() + jsonArrayUpcomingJobs.length() == 0)
                textViewNoJobsLabel.setVisibility(View.VISIBLE);
        }
        else {
            textViewNoJobsLabel.setVisibility(View.VISIBLE);
        }
    }

    private void sortWeek()  {
        JSONObject jsonData = null;
        try {
            jsonData = jsonObject.getJSONObject("data");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String mrt_status = null;
        try {
            mrt_status = jsonData.getString("mrt_status");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (mrt_status.equals("1005")) {

            // JSON Arrays for different types of jobs
            final JSONArray jsonArrayUpcomingJobs = new JSONArray();
            final JSONArray jsonArrayPastJobs = new JSONArray();

            JSONArray jsonArrayRecords = null;
            try {
                jsonArrayRecords = jsonData.getJSONArray("records");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Calendar cal = Calendar.getInstance();
            cal.set(2011, 10 - 1, 12);

            // "calculate" the start date of the week
            Calendar first = (Calendar) cal.clone();
            first.add(Calendar.DAY_OF_WEEK,
                    first.getFirstDayOfWeek() - first.get(Calendar.DAY_OF_WEEK));

            // and add six days to the end date
            Calendar last = (Calendar) first.clone();
            last.add(Calendar.DAY_OF_YEAR, 6);

            // print the result
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            Date day1 = first.getTime();
            Date day2 = last.getTime();

            for (int i = 0; i < jsonArrayRecords.length(); i++) {
                JSONObject jsonObjectRecord = null;
                try {
                    jsonObjectRecord = jsonArrayRecords.getJSONObject(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String jobStatus = null;
                try {
                    jobStatus = jsonObjectRecord.getString("job_staus");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String dateString = null;
                try {
                    dateString = jsonObjectRecord.getJSONObject("job").getString("date").split(" ")[0];
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date date = null;
                try {
                    date = formatter.parse(dateString);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if(date.compareTo(day1) < 0 && date.compareTo(day2) >0 )
                    continue;
                if (jobStatus.equals("Completed") || jobStatus.equals("Job Rated")) {
                    jsonArrayPastJobs.put(jsonObjectRecord);
                }
                else if (jobStatus.equals("New"))
                    jsonArrayUpcomingJobs.put(jsonObjectRecord);
                if(jsonArrayUpcomingJobs.length()>0 && jsonArrayPastJobs.length()>0)
                    delimiter.setVisibility(View.VISIBLE);
            }
            if (jsonArrayUpcomingJobs.length()>0)
            {
                listViewUpcomingJobs.setVisibility(View.VISIBLE);
                textViewUpcomingLabel.setVisibility(View.VISIBLE);
                if (sharedPreferences.getString(AppUtils.KEY_USER_TYPE,"dummy").equals("0"))
                {
                    MyJobsCustomerAdapter myJobsCustomerAdapter = new MyJobsCustomerAdapter(getActivity(),jsonArrayUpcomingJobs);
                    listViewUpcomingJobs.setAdapter(myJobsCustomerAdapter);
                    listViewUpcomingJobs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(getActivity(), JobDetailScreen.class);
                            try {
                                intent.putExtra("JobDetail",jsonArrayUpcomingJobs.getJSONObject(position).toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            startActivityForResult(intent, AppUtils.UPDATED);
                        }
                    });
                }
                else if (sharedPreferences.getString(AppUtils.KEY_USER_TYPE,"dummy").equals("1"))
                {
                    MyJobsTradieAdapter myJobsTradieAdapterAdapter = new MyJobsTradieAdapter(getActivity(),jsonArrayUpcomingJobs);
                    listViewUpcomingJobs.setAdapter(myJobsTradieAdapterAdapter);
                    listViewUpcomingJobs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(getActivity(), JobDetailScreen.class);
                            try {
                                intent.putExtra("JobDetail",jsonArrayUpcomingJobs.getJSONObject(position).toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            startActivityForResult(intent, AppUtils.UPDATED);
                        }
                    });
                }
            }
            else {
                listViewUpcomingJobs.setAdapter(null);
                delimiter.setVisibility(View.INVISIBLE);
                textViewUpcomingLabel.setVisibility(View.INVISIBLE);
                listViewUpcomingJobs.setVisibility(View.INVISIBLE);
            }
            if (jsonArrayPastJobs.length()>0)
            {
                listViewPastJobs.setVisibility(View.VISIBLE);
                textViewPastLabel.setVisibility(View.VISIBLE);
                if (sharedPreferences.getString(AppUtils.KEY_USER_TYPE,"dummy").equals("0"))
                {
                    MyJobsCustomerAdapter myJobsCustomerAdapter = new MyJobsCustomerAdapter(getActivity(),jsonArrayPastJobs);
                    listViewPastJobs.setAdapter(myJobsCustomerAdapter);
                    listViewPastJobs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(getActivity(), JobDetailScreen.class);
                            try {
                                intent.putExtra("JobDetail",jsonArrayPastJobs.getJSONObject(position).toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            startActivityForResult(intent, AppUtils.UPDATED);
                        }
                    });
                }
                else if (sharedPreferences.getString(AppUtils.KEY_USER_TYPE,"dummy").equals("1"))
                {
                    MyJobsTradieAdapter myJobsTradieAdapterAdapter = new MyJobsTradieAdapter(getActivity(),jsonArrayPastJobs);
                    listViewPastJobs.setAdapter(myJobsTradieAdapterAdapter);
                    listViewPastJobs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(getActivity(), JobDetailScreen.class);
                            try {
                                intent.putExtra("JobDetail",jsonArrayPastJobs.getJSONObject(position).toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            startActivityForResult(intent, AppUtils.UPDATED);
                        }
                    });
                }
            }
            else {
                if(jsonArrayUpcomingJobs.length() > 0)
                    delimiter.setVisibility(View.INVISIBLE);
                textViewPastLabel.setVisibility(View.INVISIBLE);
                listViewPastJobs.setVisibility(View.GONE);
                //listViewUpcomingJobs.setAdapter(null);
            }
            if(jsonArrayPastJobs.length() + jsonArrayUpcomingJobs.length() == 0)
                textViewNoJobsLabel.setVisibility(View.VISIBLE);
        }
        else {
            textViewNoJobsLabel.setVisibility(View.VISIBLE);
        }
    }

    private void sortPast()
    {
        JSONObject jsonData = null;
        try {
            jsonData = jsonObject.getJSONObject("data");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String mrt_status = null;
        try {
            mrt_status = jsonData.getString("mrt_status");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (mrt_status.equals("1005")) {

            // JSON Arrays for different types of jobs
            final JSONArray jsonArrayUpcomingJobs = new JSONArray();
            final JSONArray jsonArrayPastJobs = new JSONArray();

            JSONArray jsonArrayRecords = null;
            try {
                jsonArrayRecords = jsonData.getJSONArray("records");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < jsonArrayRecords.length(); i++) {
                JSONObject jsonObjectRecord = null;
                try {
                    jsonObjectRecord = jsonArrayRecords.getJSONObject(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String jobStatus = null;
                try {
                    jobStatus = jsonObjectRecord.getString("job_staus");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String date = null;
                try {
                    date = jsonObjectRecord.getString("date").split(" ")[0];
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (jobStatus.equals("Completed") || jobStatus.equals("Job Rated"))
                    jsonArrayPastJobs.put(jsonObjectRecord);
            }
            if (jsonArrayUpcomingJobs.length()>0)
            {
                textViewUpcomingLabel.setVisibility(View.VISIBLE);
                if (sharedPreferences.getString(AppUtils.KEY_USER_TYPE,"dummy").equals("0"))
                {
                    MyJobsCustomerAdapter myJobsCustomerAdapter = new MyJobsCustomerAdapter(getActivity(),jsonArrayUpcomingJobs);
                    listViewUpcomingJobs.setAdapter(myJobsCustomerAdapter);
                    listViewUpcomingJobs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(getActivity(), JobDetailScreen.class);
                            try {
                                intent.putExtra("JobDetail",jsonArrayUpcomingJobs.getJSONObject(position).toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            startActivityForResult(intent, AppUtils.UPDATED);
                        }
                    });
                }
                else if (sharedPreferences.getString(AppUtils.KEY_USER_TYPE,"dummy").equals("1"))
                {
                    MyJobsTradieAdapter myJobsTradieAdapterAdapter = new MyJobsTradieAdapter(getActivity(),jsonArrayUpcomingJobs);
                    listViewUpcomingJobs.setAdapter(myJobsTradieAdapterAdapter);
                    listViewUpcomingJobs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(getActivity(), JobDetailScreen.class);
                            try {
                                intent.putExtra("JobDetail",jsonArrayUpcomingJobs.getJSONObject(position).toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            startActivityForResult(intent, AppUtils.UPDATED);
                        }
                    });
                }
            }
            else {
                delimiter.setVisibility(View.INVISIBLE);
                textViewUpcomingLabel.setVisibility(View.INVISIBLE);
                listViewUpcomingJobs.setVisibility(View.INVISIBLE);
            }
            if (jsonArrayPastJobs.length()>0)
            {
                listViewUpcomingJobs.setAdapter(null);
                delimiter.setVisibility(View.INVISIBLE);
                listViewUpcomingJobs.setVisibility(View.INVISIBLE);
                listViewPastJobs.setVisibility(View.VISIBLE);
                textViewPastLabel.setVisibility(View.VISIBLE);
                if (sharedPreferences.getString(AppUtils.KEY_USER_TYPE,"dummy").equals("0"))
                {
                    MyJobsCustomerAdapter myJobsCustomerAdapter = new MyJobsCustomerAdapter(getActivity(),jsonArrayPastJobs);
                    listViewPastJobs.setAdapter(myJobsCustomerAdapter);
                    listViewPastJobs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(getActivity(), JobDetailScreen.class);
                            try {
                                intent.putExtra("JobDetail",jsonArrayPastJobs.getJSONObject(position).toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            startActivityForResult(intent, AppUtils.UPDATED);
                        }
                    });
                }
                else if (sharedPreferences.getString(AppUtils.KEY_USER_TYPE,"dummy").equals("1"))
                {
                    MyJobsTradieAdapter myJobsTradieAdapterAdapter = new MyJobsTradieAdapter(getActivity(),jsonArrayPastJobs);
                    listViewPastJobs.setAdapter(myJobsTradieAdapterAdapter);
                    listViewPastJobs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(getActivity(), JobDetailScreen.class);
                            try {
                                intent.putExtra("JobDetail",jsonArrayPastJobs.getJSONObject(position).toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            startActivityForResult(intent, AppUtils.UPDATED);
                        }
                    });
                }
            }
            else {
                if(jsonArrayUpcomingJobs.length() > 0)
                    delimiter.setVisibility(View.INVISIBLE);
                textViewPastLabel.setVisibility(View.INVISIBLE);
                listViewPastJobs.setVisibility(View.GONE);
                textViewNoJobsLabel.setVisibility(View.INVISIBLE);
            }
            if(jsonArrayPastJobs.length() + jsonArrayUpcomingJobs.length() == 0)
                textViewNoJobsLabel.setVisibility(View.VISIBLE);
        }
        else {
            textViewNoJobsLabel.setVisibility(View.VISIBLE);
        }
    }

    private void setupTabs()
    {
        tabLayout.addTab(tabLayout.newTab().setText("TODAY"));
        tabLayout.addTab(tabLayout.newTab().setText("THIS WEEK"));
        tabLayout.addTab(tabLayout.newTab().setText("PAST"));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            private boolean used = false;
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                used = true;
                delimiter.setVisibility(View.INVISIBLE);
                textViewNoJobsLabel.setVisibility(View.INVISIBLE);
                textViewPastLabel.setVisibility(View.INVISIBLE);
                textViewUpcomingLabel.setVisibility(View.INVISIBLE);
                textViewNoJobsLabel.setVisibility(View.INVISIBLE);
                String tabName = tab.getText().toString();
                switch (tabName)
                {
                    case "TODAY": sortToday(); break;
                    case "THIS WEEK": sortWeek(); break;
                    case "PAST": sortPast(); break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if(!used && tab.getText().toString() == "TODAY")
                    sortToday();
                used = true;
            }
        });
    }

    private void queryServer() {
        // Tag used to cancel the request
        String tag_json_obj = "json_obj_req";

        String url = AppUtils.getJobsURL(sharedPreferences.getString(AppUtils.KEY_EMAIL, null), sharedPreferences.getString(AppUtils.KEY_ACCESS_TOKEN, null));

        final ProgressDialog pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage("Fetching Jobs...");
        pDialog.setCancelable(false);
        pDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                pDialog.dismiss();

                // Parse JSON data
                try {
                    jsonObject = new JSONObject(response);
                    JSONObject jsonData = jsonObject.getJSONObject("data");

                    String mrt_status = jsonData.getString("mrt_status");

                    if (mrt_status.equals("1005"))
                    {

                        // JSON Arrays for different types of jobs
                        final JSONArray jsonArrayUpcomingJobs = new JSONArray();
                        final JSONArray jsonArrayPastJobs = new JSONArray();

                        JSONArray jsonArrayRecords = jsonData.getJSONArray("records");
                        for (int i=0;i<jsonArrayRecords.length();i++) {
                            JSONObject jsonObjectRecord = jsonArrayRecords.getJSONObject(i);
                            String jobStatus = jsonObjectRecord.getString("job_staus");

                            if (jobStatus.equals("Completed") || jobStatus.equals("Job Rated"))
                                jsonArrayPastJobs.put(jsonObjectRecord);
                            else if (jobStatus.equals("New"))
                                jsonArrayUpcomingJobs.put(jsonObjectRecord);
                        }

                        if (jsonArrayUpcomingJobs.length()>0)
                        {
                            textViewUpcomingLabel.setVisibility(View.VISIBLE);
                            if (sharedPreferences.getString(AppUtils.KEY_USER_TYPE,"dummy").equals("0"))
                            {
                                MyJobsCustomerAdapter myJobsCustomerAdapter = new MyJobsCustomerAdapter(getActivity(),jsonArrayUpcomingJobs);
                                listViewUpcomingJobs.setAdapter(myJobsCustomerAdapter);
                                listViewUpcomingJobs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        Intent intent = new Intent(getActivity(), JobDetailScreen.class);
                                        try {
                                            intent.putExtra("JobDetail",jsonArrayUpcomingJobs.getJSONObject(position).toString());
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        startActivityForResult(intent, AppUtils.UPDATED);
                                    }
                                });
                            }
                            else if (sharedPreferences.getString(AppUtils.KEY_USER_TYPE,"dummy").equals("1"))
                            {
                                MyJobsTradieAdapter myJobsTradieAdapterAdapter = new MyJobsTradieAdapter(getActivity(),jsonArrayUpcomingJobs);
                                listViewUpcomingJobs.setAdapter(myJobsTradieAdapterAdapter);
                                listViewUpcomingJobs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        Intent intent = new Intent(getActivity(), JobDetailScreen.class);
                                        try {
                                            intent.putExtra("JobDetail",jsonArrayUpcomingJobs.getJSONObject(position).toString());
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        startActivityForResult(intent, AppUtils.UPDATED);
                                    }
                                });
                            }
                        }
                        else {
                            delimiter.setVisibility(View.INVISIBLE);
                            textViewUpcomingLabel.setVisibility(View.INVISIBLE);
                            listViewUpcomingJobs.setVisibility(View.INVISIBLE);
                        }
                        if (jsonArrayPastJobs.length()>0)
                        {
                            delimiter.setVisibility(View.VISIBLE);
                            textViewPastLabel.setVisibility(View.VISIBLE);
                            if (sharedPreferences.getString(AppUtils.KEY_USER_TYPE,"dummy").equals("0"))
                            {
                                MyJobsCustomerAdapter myJobsCustomerAdapter = new MyJobsCustomerAdapter(getActivity(),jsonArrayPastJobs);
                                listViewPastJobs.setAdapter(myJobsCustomerAdapter);
                                listViewPastJobs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        Intent intent = new Intent(getActivity(), JobDetailScreen.class);
                                        try {
                                            intent.putExtra("JobDetail",jsonArrayPastJobs.getJSONObject(position).toString());
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        startActivityForResult(intent, AppUtils.UPDATED);
                                    }
                                });
                            }
                            else if (sharedPreferences.getString(AppUtils.KEY_USER_TYPE,"dummy").equals("1"))
                            {
                                MyJobsTradieAdapter myJobsTradieAdapterAdapter = new MyJobsTradieAdapter(getActivity(),jsonArrayPastJobs);
                                listViewPastJobs.setAdapter(myJobsTradieAdapterAdapter);
                                listViewPastJobs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        Intent intent = new Intent(getActivity(), JobDetailScreen.class);
                                        try {
                                            intent.putExtra("JobDetail",jsonArrayPastJobs.getJSONObject(position).toString());
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        startActivityForResult(intent, AppUtils.UPDATED);
                                    }
                                });
                            }
                        }
                        else {
                            delimiter.setVisibility(View.INVISIBLE);
                            textViewPastLabel.setVisibility(View.INVISIBLE);
                            listViewPastJobs.setVisibility(View.GONE);
                        }
                        if(jsonArrayUpcomingJobs.length() == 0 || jsonArrayPastJobs.length() == 0)
                            delimiter.setVisibility(View.INVISIBLE);
                    }
                    else {
                        textViewNoJobsLabel.setVisibility(View.VISIBLE);
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
                            }).show();
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
