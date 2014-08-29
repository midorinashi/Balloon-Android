package com.j32productions.balloon;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
 
// http://www.androidhive.info/2012/02/android-custom-listview-with-image-and-text/

public class GroupAdapter extends ArrayAdapter<String> implements Filterable {
 
    private Activity activity;
    private int viewType;
    private String[] names;
    private String[] responseRates;
    private String[] photoURLs;
    private static LayoutInflater inflater = null;
    public ImageLoader imageLoader; 
 
    public GroupAdapter(Activity a, int type, String[] n, String[] urls) {
    	super(a, type, R.id.name, n);
        activity = a;
        viewType = type;
        names = n;
        photoURLs = urls;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader = new ImageLoader(activity.getApplicationContext());
    }
    
    public GroupAdapter(Activity a, int type, String[] n, String[] rates, String[] urls)
    {
    	this(a, type, n, urls);
    	responseRates = rates;
    }
 
    public int getCount() {
        return names.length;
    }
    
    public String getItem(int position)
    {
    	return "" + position;
    }
 
    public long getItemId(int position) {
        return position;
    }
    
    public Filter getFilter()
    {
    	return super.getFilter();
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
        else if (photoURLs[position] == null)
        {
        	Uri uri = Uri.parse("android.resource://com.j32productions.balloon/drawable/user280");
        	try {
				InputStream stream = activity.getContentResolver().openInputStream(uri);
		        Picasso.with(activity).load(uri).resize(140, 140)
	        		.into((ImageView) vi.findViewById(R.id.imageView1));
		        stream.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        else
	        Picasso.with(activity).load(photoURLs[position]).resize(140, 140)
	        	.into((ImageView) vi.findViewById(R.id.imageView1));
        if (responseRates != null)
        	((TextView) vi.findViewById(R.id.responseRate)).setText(responseRates[position]);
        return vi;
    }
}
