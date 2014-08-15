package com.example.balloon;

import java.util.ArrayList;
import java.util.List;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

public class ContactAdapter extends ArrayAdapter<String> implements Filterable {
	 
    private Activity activity;
    private int viewType;
    
    private ArrayList<String> names;
    private ArrayList<String> phones;
    private ArrayList<String> ids;

    private ArrayList<String> filteredNames;
    private ArrayList<String> filteredPhones;
    private ArrayList<String> filteredIds;
    
    private static LayoutInflater inflater = null;
    public ImageLoader imageLoader;
	private Filter filter; 
 
    public ContactAdapter(Activity a, int type, ArrayList<String> n, ArrayList<String> p, ArrayList<String> i) {
    	super(a, type, R.id.name, n);
        activity = a;
        viewType = type;
        names = n;
        phones = p;
        ids = i;
        filteredNames = new ArrayList<String>(n);
        filteredPhones = new ArrayList<String>(p);
        filteredIds = new ArrayList<String>(i);
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
 
    public int getCount() {
        return filteredNames.size();
    }
 
    public long getItemId(int position) {
        return position;
    }
    
    public Filter getFilter() {
        if (filter == null)
            filter = new ContactFilter();
        return filter;
    }
    
    private class ContactFilter extends Filter {
        @SuppressLint("DefaultLocale")
		@Override
        protected FilterResults performFiltering(CharSequence constraint) {
        	FilterResults results = new FilterResults();
            // We implement here the filter logic
            if (constraint == null || constraint.length() == 0) {
                // No filter implemented we return all the list
                filteredNames = new ArrayList<String>(names);
                filteredPhones = new ArrayList<String>(phones);
                filteredIds = new ArrayList<String>(ids);
                results.count = names.size();
            }
            else {
                // We perform filtering operation
                filteredNames.clear();
                filteredPhones.clear();
                filteredIds.clear();
                for (int i = 0; i < names.size(); i++) {
                    if (names.get(i).toUpperCase().contains(constraint.toString().toUpperCase()) ||
                    	phones.get(i).contains(constraint))
                    {
                    	filteredNames.add(names.get(i));
                    	filteredPhones.add(phones.get(i));
                    	filteredIds.add(ids.get(i));
                    }
                }
                 
                results.count = filteredNames.size();
         
            }
            return results;
        }
     
        @Override
        protected void publishResults(CharSequence constraint,FilterResults results) {
        	notifyDataSetChanged();
        }
         
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if(convertView == null)
            vi = inflater.inflate(viewType, null);
        if (position < filteredNames.size())
        {
	        ((TextView) vi.findViewById(R.id.name)).setText(filteredNames.get(position));
	        ((TextView) vi.findViewById(R.id.number)).setText(filteredPhones.get(position));
	        ((TextView) vi.findViewById(R.id.id)).setText(filteredIds.get(position));
        }
        return vi;
    }
}
