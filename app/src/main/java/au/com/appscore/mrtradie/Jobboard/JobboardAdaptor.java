package au.com.appscore.mrtradie.Jobboard;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

/**
 * Created by lijiazhou on 31/01/2016.
 */
public class JobboardAdaptor<T extends View> extends BaseAdapter {

    ArrayList<T> list;

    public JobboardAdaptor()
    {
        list = new ArrayList<>();
    }

    public void add(T t)
    {
        list.add(t);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public T getItem(int position) {
        if(position >= getCount())
            return null;
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        if(position >= getCount())
            return -1;
        return list.get(position).hashCode() * position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView != null)
            return convertView;
        if(position >= getCount())
            return null;
        convertView = list.get(position);
        return convertView;
    }
}
