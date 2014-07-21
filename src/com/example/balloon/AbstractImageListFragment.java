package com.example.balloon;

import org.json.JSONArray;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public abstract class AbstractImageListFragment extends Fragment {
	
	private static ListView list;
	private static String[] urls;
	private static LazyAdapter adapter;
	private static FragmentActivity context;
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
		new AccessFoursquarePhotos().execute(getVenueId());
	}
	
	public abstract String getVenueId();
	
	public static void setURLs(String[] strings)
	{
		urls = strings;
		System.out.println(urls.length + " urls found");
		adapter = new LazyAdapter(context, urls);
		list.setAdapter(adapter);
	}
	
	public void onPause()
	{
		super.onPause();
		if (toSave)
		{
			SparseBooleanArray checked = list.getCheckedItemPositions();
			long [] ids = list.getCheckedItemIds();
			//TODO Doesn't always get all the images? fun buns example
			JSONArray urlsToSave = new JSONArray();
			for (int i = 0; i < urls.length; i++)
			{
				if (checked.get(i, false))
					urlsToSave.put(urls[i]);
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
