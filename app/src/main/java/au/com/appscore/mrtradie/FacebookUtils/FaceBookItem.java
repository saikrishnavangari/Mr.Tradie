package au.com.appscore.mrtradie.FacebookUtils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ActionMenuView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.InputStream;
import java.net.URL;

import au.com.appscore.mrtradie.R;

/**
 * Created by lijiazhou on 25/01/16.
 */
public class FaceBookItem extends LinearLayout {

    private String name;
    private String imageUrl;
    private ImageView image;
    private TextView text;
    private ActionMenuView menuView;
    private boolean downloadCompleted;

    public FaceBookItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.com_facebook_activity_layout, this, false);
        image = (ImageView)findViewById(R.id.facebook_image);
        text = (TextView)findViewById(R.id.facebook_name);
        menuView = (ActionMenuView)findViewById(R.id.facebook_menu);
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setImageUrl(String url)
    {
        imageUrl = url;
    }

    public void downLoad()
    {
        try {
            InputStream is = (InputStream) new URL(imageUrl).getContent();
            Drawable drawable = Drawable.createFromStream(is, "Facebook " + name);
            image.setImageDrawable(drawable);

        } catch (Exception e) {

        }
    }

    public boolean isDownloadCompleted()
    {
        return downloadCompleted;
    }
}
