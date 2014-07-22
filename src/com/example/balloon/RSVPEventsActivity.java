package com.example.balloon;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

public class RSVPEventsActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rsvpevents);
		setTitle(R.string.title_activity_rsvpevents);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_invites)
		{
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
			return true;
		}
		if (id == R.id.action_create)
		{
			Intent intent = new Intent(this, NewInvitationActivity.class);
			startActivity(intent);
			return true;
		}
		if (id == R.id.action_lists)
		{
			Intent intent = new Intent(this, ContactListsActivity.class);
			startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			return true;
		}
		if (id == R.id.action_settings)
		{
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends MainActivity.InvitationsFragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_rsvpevents,
					container, false);
			return rootView;
		}
		
		public void onResume()
		{
			super.onResume();
			getUpcoming();
		}
		
		public void getUpcoming()
		{
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("user", ParseUser.getCurrentUser().toString());
			ParseCloud.callFunctionInBackground("loadUpcomingMeetups", params,
					new FunctionCallback<ArrayList<HashMap<String, Object>>>(){
				public void done(ArrayList<HashMap<String, Object>> upcoming, ParseException e) {
					if (e == null)
						makeList(upcoming);
					else
						e.printStackTrace();
				}
			});
		}
		
		//Takes the list of meetups from parse query and, well, lists them.
		// TODO I need to make a list of events to avoid memory leaks, also stop all timers
		public void makeList(ArrayList<HashMap<String, Object>> upcoming)
		{
			LinearLayout lin = (LinearLayout) getActivity().findViewById(R.id.invitations);
			//removes all the views for now because EFFICIENCY WHAT
			lin.removeAllViews();
			
			for (int i = 0; i < upcoming.size(); i++)
			{
				Event event = new Event(getActivity());
				ParseObject meetup = (ParseObject) upcoming.get(i).get("meetup");
				//invite.setVenuePhoto(event.getParseFile(key));
				try {
					
					event.setObjectId(meetup.getObjectId());
					event.setCreator(meetup.getParseUser("creator").getString("firstName") + " " +
							meetup.getParseUser("creator").getString("lastName"));
					event.setAgenda(meetup.getString("agenda"));
					
					JSONArray formattedAddress = meetup.getJSONObject("venueInfo").getJSONObject("location")
							.getJSONArray("formattedAddress");
					String address = "";
					for (int j = 0; j < formattedAddress.length(); j++)
					{
						address += formattedAddress.getString(j);
						if (j != formattedAddress.length() - 1)
							address += ", ";
					}
					event.setVenueInfo(address);

					JSONArray urls = meetup.getJSONArray("venuePhotoURLs");
					if (urls != null && urls.length() > 0)
						event.setVenuePhoto(getActivity(), urls.getString(0));
					
					event.setExpiresAt((Date) meetup.get("expiresAt"));
					event.setHasResponded(true);
					event.setWillAttend(true);
					event.setIsCreator(ParseUser.getCurrentUser().getObjectId()
							.equals(meetup.getParseUser("creator").getObjectId()));
					if (!event.getIsCreator())
						event.setWillAttend(((ParseObject) upcoming.get(i).get("response"))
								.getBoolean("isAttending"));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if (i % 2 == 0)
					event.setBackgroundColor(getResources().getColor(R.color.lightBlue));
				lin.addView(event);
			}
		}
	}
}
