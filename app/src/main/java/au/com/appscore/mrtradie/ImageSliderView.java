package au.com.appscore.mrtradie;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.daimajia.slider.library.SliderTypes.BaseSliderView;

/**
 * Created by macmini6 on 27/10/2015.
 */


public class ImageSliderView extends BaseSliderView {

    public ImageSliderView(Context context) {
        super(context);
    }

    @Override
    public View getView() {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.image_slider_view, null);
        ImageView target = (ImageView) v.findViewById(R.id.imageView2);
        setScaleType(ScaleType.CenterInside);
        bindEventAndShow(v, target);
        return v;
    }
}
