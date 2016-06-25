package au.com.appscore.mrtradie.Jsons.Jobboard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lijiazhou on 9/02/16.
 */
public class JobboardRecord implements Serializable{
    public String id;
    public JobboardQuote quote;
    public JobboardAddress address;
    public List<JobboardAvailability> availability;
    public String photo1;
    public String photo2;
    public String photo3;

    public ArrayList<String> getAvailability()
    {
        ArrayList<String> arrayListAvailability = new ArrayList<>();
        for (int i=0;i<availability.size();i++) {
            arrayListAvailability.add(availability.get(i).start_date + " - " + availability.get(i).end_date);
        }
        return arrayListAvailability;
    }
}
