package au.com.appscore.mrtradie.Jsons.Jobboard;

import java.io.Serializable;

/**
 * Created by lijiazhou on 9/02/2016.
 */
public class JobboardQuote implements Serializable {
    public String quote_id;
    public String owner_email;
    public String quote_status;
    public String quote_desc;
    public String budget;
    public String comment;
    public JobboardUserProfile user_profile;
}
