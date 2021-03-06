package com.j32productions.balloon;

import org.json.JSONArray;

import android.app.Activity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

public abstract class AbstractImageListFragment extends ProgressFragment {
	
	private static ListView list;
	private static String[] urls;
	private static LazyAdapter adapter;
	private static Activity context;
	private static boolean toSave;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_image_list,
				container, false);
		return rootView;
	}
	
	public void onResume()
	{
		super.onResume();
		context = getActivity();
		list = (ListView) getActivity().findViewById(R.id.imageList);
        list.setItemsCanFocus(false);
        list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		new AccessFoursquarePhotos(this).execute(getVenueId());
	}
	
	public abstract String getVenueId();
	
	public static void setURLs(String[] strings)
	{
		urls = strings;
		if (strings.length > 0)
		{
			System.out.println(urls.length + " urls found");
			adapter = new LazyAdapter(context, urls);
			list.setAdapter(adapter);
		}
		else
		{
    		Toast.makeText(context, "No location photos available", Toast.LENGTH_SHORT).show();
    		
		}
	}
	
	public void onPause()
	{
		super.onPause();
		if (toSave)
		{
			//don't forget - sparsebooleanarray looks up KEYS not positions
			SparseBooleanArray checked = list.getCheckedItemPositions();
			JSONArray urlsToSave = new JSONArray();
			if (urls != null)
			{
				for (int i = 0; i < urls.length; i++)
				{
					if (checked.get(i, false))
						urlsToSave.put(urls[i]);
				}
			}
			saveUrls(urlsToSave);
		}
	}
	
	public static void setSave(boolean b)
	{
		toSave = b;
	}
	
	public abstract void saveUrls(JSONArray urls);
}
