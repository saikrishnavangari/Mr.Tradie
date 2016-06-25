package au.com.appscore.mrtradie;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.viewpagerindicator.CirclePageIndicator;

/**
 * Created by adityathakar on 24/08/15.
 */
public class TipsFragment extends Fragment {

    private ViewPager mPager;

    private PagerAdapter mPagerAdapter;

    private CirclePageIndicator circlePageIndicator;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tips_fragment,container,false);

        mPager = (ViewPager) v.findViewById(R.id.viewPagerTips);

        mPagerAdapter = new TipsAdapter(getActivity().getSupportFragmentManager());

        mPager.setAdapter(mPagerAdapter);

        circlePageIndicator = (CirclePageIndicator) v.findViewById(R.id.indicator);
        circlePageIndicator.setViewPager(mPager);

        return v;
    }


    private class TipsAdapter extends FragmentStatePagerAdapter {

        public TipsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position==0)
                return new Tip1Fragment();
            else
                return new Tip2Fragment();
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    public static class Tip1Fragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            ViewGroup rootView = (ViewGroup) inflater.inflate(
                    R.layout.tip_layout, container, false);

            return rootView;
        }

    }

    public static class Tip2Fragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            ViewGroup rootView = (ViewGroup) inflater.inflate(
                    R.layout.tip1_layout, container, false);

            return rootView;
        }

    }



}
