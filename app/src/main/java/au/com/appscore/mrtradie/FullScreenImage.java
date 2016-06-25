package au.com.appscore.mrtradie;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

public class FullScreenImage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        ViewPager viewPager = (ViewPager)findViewById(R.id.pager);
        ArrayList<String> images = new ArrayList<>();
        images.add(getIntent().getStringExtra("photo1"));
        images.add(getIntent().getStringExtra("photo2"));
        images.add(getIntent().getStringExtra("photo3"));


        FullScreenImageAdapter fullScreenImageAdapter = new FullScreenImageAdapter(FullScreenImage.this,images);
        viewPager.setAdapter(fullScreenImageAdapter);

        viewPager.setCurrentItem(getIntent().getIntExtra("position",0));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_full_screen_image, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Adapter for view pager
    public class FullScreenImageAdapter extends PagerAdapter {


        private Activity activity;
        private ArrayList<String> images;
        private LayoutInflater inflater;


        public FullScreenImageAdapter(Activity activity, ArrayList<String> images)
        {
            this.activity = activity;
            this.images = images;
        }

        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((RelativeLayout) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView imgDisplay;

            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View viewLayout = inflater.inflate(R.layout.layout_fullscreen_image, container,
                    false);

            imgDisplay = (ImageView) viewLayout.findViewById(R.id.imgDisplay);
            Ion.with(imgDisplay).load(images.get(position));

            ((ViewPager) container).addView(viewLayout);

            return viewLayout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView((RelativeLayout) object);

        }
    }
}
