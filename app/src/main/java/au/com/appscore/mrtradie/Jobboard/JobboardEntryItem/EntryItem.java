package au.com.appscore.mrtradie.Jobboard.JobboardEntryItem;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import au.com.appscore.mrtradie.R;
import au.com.appscore.mrtradie.utils.ControlPraser;

/**
 * Created by lijiazhou on 23/02/16.
 */
public class EntryItem extends LinearLayout {
    TextView name ;
    TextView price;
    public EntryItem(Context context, AttributeSet attrs, String name, String price) {
        super(context, attrs);
        LayoutInflater.from(getContext()).inflate(R.layout.view_jobboard_entry_item, this, true);
        this.name = ControlPraser.PraserControl(this, R.id.jobboard_entry_name);
        this.price = ControlPraser.PraserControl(this, R.id.jobboard_entry_price);
        this.name.setText(name);
        this.price.setText(price);
    }
}
