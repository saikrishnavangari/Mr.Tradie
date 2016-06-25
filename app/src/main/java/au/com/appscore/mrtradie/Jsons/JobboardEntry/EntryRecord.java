package au.com.appscore.mrtradie.Jsons.JobboardEntry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import au.com.appscore.mrtradie.Jsons.Jobboard.JobboardAddress;
import au.com.appscore.mrtradie.Jsons.Jobboard.JobboardAvailability;

/**
 * Created by lijiazhou on 24/02/16.
 */
public class EntryRecord implements Serializable {
    public String id;
    public String budget;
    public String description;
    public List<JobboardAvailability> availability;
    public String owner_email;
    public String owner_name;
    public String owner_contact_number;
    public String company_name;
    public String company_email;
    public JobboardAddress address;
    public String photo1;
    public String photo2;
    public String photo3;
    public String company_contact_number;
    public ArrayList<String> getAvailability()
    {
        ArrayList<String> arrayListAvailability = new ArrayList<>();
        for (int i=0;i<availability.size();i++) {
            arrayListAvailability.add(availability.get(i).start_date + " - " + availability.get(i).end_date);
        }
        return arrayListAvailability;
    }
}
