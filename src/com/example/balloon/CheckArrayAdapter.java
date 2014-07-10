package com.example.balloon;

import android.content.Context;
import android.widget.ArrayAdapter;

//hackiest way to make the select all work
public class CheckArrayAdapter extends ArrayAdapter<String>
{
	public CheckArrayAdapter(Context context, int resource,
			String[] objects) {
		super(context, resource, objects);
	}
	
	public boolean hasStableIds()
	{
		return true;
	}
}
		