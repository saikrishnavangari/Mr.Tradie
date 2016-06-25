package au.com.appscore.mrtradie.Jsons.Jobboard;

import java.io.Serializable;

/**
 * Created by lijiazhou on 9/02/2016.
 */
public class JobboardUserProfile implements Serializable {
    public String user_email;
    public String user_full_name;
    public String user_type;
    public String contact_num;
    public String user_occupation;
    public JobboardAddress address;
}
