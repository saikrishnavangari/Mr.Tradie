package au.com.appscore.mrtradie.Search.Item;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;

import au.com.appscore.mrtradie.R;
import au.com.appscore.mrtradie.utils.ControlPraser;

/**
 * Created by lijiazhou on 4/02/16.
 */
public class SearchItem extends LinearLayout {

    ImageView companyImage;
    TextView company;
    TextView address;
    TextView distance;
    TextView score;
    ImageView quoteButton;
    LinearLayout ratingBar;

    public SearchItem(Context context)
    {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.businesslist_item, this, true);
        //this.setBackgroundColor(0xFFFFFFFF);
        companyImage = ControlPraser.PraserControl(this, R.id.imageViewBusinessImage);
        company = ControlPraser.PraserControl(this, R.id.textViewBusinessName);
        address = ControlPraser.PraserControl(this, R.id.textViewBusinessAddress);
        distance = ControlPraser.PraserControl(this, R.id.business_distance);
        score = ControlPraser.PraserControl(this, R.id.business_score);
        quoteButton = ControlPraser.PraserControl(this, R.id.imageButtonGetQuote);
        ratingBar = ControlPraser.PraserControl(this, R.id.ratingBar);
        //distance.setVisibility(View.GONE);
        //score.setVisibility(View.GONE);
        company.setTextColor(0xFF000000);
        address.setTextColor(0xFF000000);
        distance.setTextColor(0xFF000000);
        score.setTextColor(0xFF000000);
    }

    public SearchItem(Context context, AttributeSet attrs) {
        super(context, attrs);

        //LayoutInflater.from(context).inflate(R.layout.businesslist_item, this, true);
        //this.setBackgroundColor(0xFFFFFFFF);
        companyImage = ControlPraser.PraserControl(this, R.id.imageViewBusinessImage);
        company = ControlPraser.PraserControl(this, R.id.textViewBusinessName);
        address = ControlPraser.PraserControl(this, R.id.textViewBusinessAddress);
        distance = ControlPraser.PraserControl(this, R.id.business_distance);
        score = ControlPraser.PraserControl(this, R.id.business_score);
        quoteButton = ControlPraser.PraserControl(this, R.id.imageButtonGetQuote);
        ratingBar = ControlPraser.PraserControl(this, R.id.ratingBar);
        //distance.setVisibility(View.GONE);
        //score.setVisibility(View.GONE);
        company.setTextColor(0xFF000000);
        address.setTextColor(0xFF000000);
        distance.setTextColor(0xFF000000);
        score.setTextColor(0xFF000000);
    }

    public void setCompanyImage(String url)
    {
        Ion.with(companyImage).load(url);
    }

    public void setCompany(String companyName)
    {
        company.setText(companyName);
    }

    public void setAddress(String addressString)
    {
        address.setText(addressString);
    }

    public void setDistance(String distanceString)
    {
        distance.setText(distanceString);
    }

    public void setScore(String scoreValue)
    {
        score.setText(scoreValue);
    }

    public void setQuoteButtonListener(OnClickListener listener)
    {
        quoteButton.setOnClickListener(listener);
    }

    public void setRatingBar(int rating)
    {
        int scoreValue = 0;
        rating = (rating <= 5 ? rating : 5);
        for (int i=0; i < rating; i++){
            ImageView v = (ImageView) ratingBar.getChildAt(i);
            v.setImageResource(R.drawable.icon_star_full);
            scoreValue += 20;
        }
        setScore(String.valueOf(scoreValue) + "% feedback score");
    }
}
