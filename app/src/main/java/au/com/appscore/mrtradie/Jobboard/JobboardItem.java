package au.com.appscore.mrtradie.Jobboard;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import au.com.appscore.mrtradie.R;
import au.com.appscore.mrtradie.utils.ControlPraser;


/**
 * Created by lijiazhou on 31/01/2016.
 */
public class JobboardItem extends RelativeLayout {

    TextView itemName;
    TextView streetAddress;
    ImageView contact;
    public JobboardItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.jobboard_item, this, true);
        itemName = ControlPraser.PraserControl(this, R.id.jobboard_item_name);
        streetAddress = ControlPraser.PraserControl(this, R.id.jobboard_street_address);
        contact = ControlPraser.PraserControl(this, R.id.jobboard_item_contact);
    }

    public void setControl(String name, String address)
    {
        itemName.setText(name);
        streetAddress.setText(address);
    }


}
