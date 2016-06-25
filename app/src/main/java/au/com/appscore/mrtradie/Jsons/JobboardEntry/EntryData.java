package au.com.appscore.mrtradie.Jsons.JobboardEntry;

import java.io.Serializable;
import java.util.List;

/**
 * Created by lijiazhou on 24/02/16.
 */
public class EntryData implements Serializable{
    public String mrt_status;
    public String mrt_desc;
    public int total;
    public List<EntryRecord> records;
}
