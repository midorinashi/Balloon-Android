package com.j32productions.balloon;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AccessFoursquarePhotosForAutoAdd extends AccessFoursquarePhotos{

	//we're doing this in the background lazily, so we don't want to show the spinner
	public AccessFoursquarePhotosForAutoAdd(ProgressFragment f)
	{
		super();
		fragment = f;
	}
	
	//override
	protected void onPostExecute(JSONArray items)
    {
		if (items.length() > 0)
		{
			JSONObject item;
			try {
				item = (JSONObject) items.getJSONObject(0);
		    	String url = (item.getString("prefix") + "300x100" + item.getString("suffix"));
				NewInvitationActivity.setLocationPicture(url);
				NewInvitationActivity.setHasLocationPictures(true);
			} catch (JSONException e) {
				NewInvitationActivity.setHasLocationPictures(false);
			}
			
    	}
		else
			NewInvitationActivity.setHasLocationPictures(false);
    }
}
