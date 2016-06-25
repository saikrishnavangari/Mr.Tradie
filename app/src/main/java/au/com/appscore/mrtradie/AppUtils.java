package au.com.appscore.mrtradie;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;
import java.util.List;

/**
 * Created by adityathakar on 26/08/15.
 */
public class AppUtils {

    Context context;

    public AppUtils(Context context) {
        this.context = context;
    }

    public static String baseURL = "http://admin.mrtradie.com.au/";

    public static String SharedPreferenceFileName = "MrTradiePreferences";

    public static final int UPDATED = 1;

    public static final int RESULT_OK = 1001;

    // Keys for shared preference
    public static String KEY_IS_LOGGEDIN = "is_loggedin";
    public static String KEY_EMAIL = "email";
    public static String KEY_ACCESS_TOKEN = "access_token";
    public static String KEY_FULL_NAME = "full_name";
    public static String KEY_USER_TYPE = "user_type";
    public static String KEY_LOGGEDIN_USING_FB = "loggedin_using_fb";

    public static String REG_TOKEN=null;

    public void getTokenInBackground() {
        try {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        InstanceID instanceID = InstanceID.getInstance(context);
                        REG_TOKEN = instanceID.getToken("665603170097", GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                        Log.d("Debug", "Retrieved token is :" + REG_TOKEN);
                    } catch (final IOException e) {
                        Log.d("Debug", "Could not get registration token");
                    }
                    return null;
                }
            }.execute();
        }catch (Exception ex){
            String st = ex.getLocalizedMessage();
        }

    }

    public static String getSearchProfileTradieURLForQuery(String email, String access_token,String query) {
        return baseURL+"profile/search?email="+email+"&access_token="+access_token+"&profile_type=1"+"&text="+query;
    }

    // Request quote URL
    public static String addQuoteUrlString() {
        return baseURL+"quote/add";
    }

    // Quote records URL
    public static String getQuotesURL(String email, String access_token) {
        return baseURL+"quote/records?email="+email+"&access_token="+access_token+"&limit=-1";
    }

    // Method to accept quote
    public static String getAcceptQuoteURL() {
        return baseURL+"quote/accept";
    }

    // Method to decline quote
    public static String getDeclineQuoteURL() {
        return baseURL+"quote/decline";
    }

    // Method to update quote
    public static String getUpdateQuoteURL() {
        return baseURL+"quote/update";
    }

    // Method to update job
    public static String getUpdateJobURL() {
        return baseURL+"job/changestatus";
    }

    // Method to update password
    public static String getForgotPasswordURL() {
        return baseURL+"forgotpassword";
    }

    // Method to get URL for rating job
    public static String getRateJobURL() {
        return baseURL+"job/rate";
    }

    // Method to get social login URL
    public static String getSocialLoginURL() {
        return baseURL+"loginsocial";
    }

    // Jobs records URL
    public static String getJobsURL(String email, String access_token) {
        return baseURL+"job/records?email="+email+"&access_token="+access_token+"&limit=-1";
    }

    // Search URL for a particular occupation
    public static String getSearchProfileTradieURLForOccupation(String email, String access_token,String query,int category,double latitude, double longitude) {
        // Exclude latitude and longitude parameters if its not available
        if ((latitude==0&&longitude==0)||(latitude==-1&&longitude==-1))
            return baseURL+"profile/search?email="+email+"&access_token="+access_token+"&profile_type=1"+"&text="+query+"&category="+category;
        else
            return baseURL+"profile/search?email="+email+"&access_token="+access_token+"&profile_type=1"+"&text="+query+"&category="+category+"&latitude="+latitude+"&longitude="+longitude;
    }

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    public final static String getJobboardsUrl(String email, String access_token, String category)
    {
        return getJobboardsUrl(email, access_token) + "&category=" + category;
    }

    public final static String getJobboardsUrl(String email, String access_token)
    {
        return baseURL + "jobboard/records?email=" + email + "&access_token=" + access_token;
    }

    public final static String getNewJobUrl()
    {
        return baseURL + "/jobboard/add";
    }

    public final static String getFacebookTradie(String email, String accesstoken, List<String> emails)
    {
        String url = baseURL+"profile/search?email="+email+"&access_token="+accesstoken;
        for(Object object : emails.toArray())
        {
            url += "&facebook_ids[]=" + object.toString();
        }

        return url;
    }

    public final static String getJobboardApplyUrl()
    {
        return baseURL + "jobboard/apply";
    }

    public  final static  String acceptJobUrl()
    {
        return baseURL + "jobboard/accept";
    }

    public final static String getJobboardApplicationsUrl(String email, String accessToken, String quoteId, String format)
    {
        return baseURL + "jobboard/applications?email="+email+"&access_token="+accessToken+"&quote_id="+quoteId+"&format="+ format;
    }

    public final static String getJobboardApplicationsUrl(String email, String accessToken, String quoteId)
    {
        return baseURL + "jobboard/applications?email="+email+"&access_token="+accessToken+"&quote_id="+quoteId;
    }

    public final static String getPremiumUrl()
    {
        return baseURL + "profile/premium";
    }
}
