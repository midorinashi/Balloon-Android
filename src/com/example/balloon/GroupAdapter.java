package com.example.balloon;

import java.util.ArrayList;
import java.util.HashMap;

import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
 
// http://www.androidhive.info/2012/02/android-custom-listview-with-image-and-text/

public class GroupAdapter extends BaseAdapter {
 
    private Activity activity;
    private int viewType;
    private String[] names;
    private String[] photoURLs;
    private static LayoutInflater inflater = null;
    public ImageLoader imageLoader; 
 
    public GroupAdapter(Activity a, int type, String[] n, String[] urls) {
        activity = a;
        viewType = type;
        names = n;
        photoURLs = urls;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader = new ImageLoader(activity.getApplicationContext());
    }
 
    public int getCount() {
        return names.length;
    }
 
    public Object getItem(int position) {
        return position;
    }
 
    public long getItemId(int position) {
        return position;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if(convertView == null)
            vi = inflater.inflate(viewType, null);
        ((TextView) vi.findViewById(R.id.name)).setText(names[position]);
        //if we give it a resource id, then make it an int resource, not a string url
        if (photoURLs[position] != null && photoURLs[position].indexOf('.') == -1)
	        Picasso.with(activity).load(Integer.parseInt(photoURLs[position])).resize(140, 140)
	        	.into((ImageView) vi.findViewById(R.id.imageView1));
        else
	        Picasso.with(activity).load(photoURLs[position]).resize(140, 140)
	        	.into((ImageView) vi.findViewById(R.id.imageView1));
        return vi;
    }
}
