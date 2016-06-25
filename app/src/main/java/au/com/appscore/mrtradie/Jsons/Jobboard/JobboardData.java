package au.com.appscore.mrtradie.Jsons.Jobboard;

import java.io.Serializable;
import java.util.List;

/**
 * Created by lijiazhou on 9/02/16.
 */
public class JobboardData implements Serializable {
    public String mrt_status;
    public String mrt_desc;
    public int total;
    public List<JobboardRecord> records;
}
