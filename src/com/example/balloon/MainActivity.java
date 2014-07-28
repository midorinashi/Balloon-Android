package com.example.balloon;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.Parse;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

public class MainActivity extends ActionBarActivity
{
	//TODO only find location during location steps
	private static double latitude;
	private static double longitude;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//initialize parse
		Parse.initialize(this, "iXEPNEZfJXoEOIayxLgBBgpShMZBTj7ReVoi1eqn",
				"GHtE0svPk0epFG4olYnFTnnDtmARHtENXxXuHoXp");
		
		String Tracey = "+19739002782";
		String Mao = "+18007580051";
		//the proxy
		String Liu = "+19739129828";
		/*
		ParseUser.logInInBackground(Tracey, "cat", new LogInCallback() {
			  public void done(ParseUser user, ParseException e) { 
			    if (user != null) {
			      // Hooray! The user is logged in.
			    	onCreateInvitationsFragment();
			    } else {
			      // Signup failed. Look at the ParseException to see what happened.
			    	System.out.println("fuck");
			    }
			  }
			});
		
		*/
		setContentView(R.layout.activity_main);
		//check to see if person is logged in
		if (ParseUser.getCurrentUser() != null)
		{
			onCreateInvitationsFragment();
		}
		else
		{
			Intent intent = new Intent(this, FirstPageActivity.class);
			startActivity(intent);
			finish();
		}
	}
	
	public void onCreateInvitationsFragment()
	{
		/*getSupportFragmentManager().beginTransaction()
			.add(R.id.container, new InvitationsFragment()).commit();*/
		
		getSupportFragmentManager().beginTransaction()
			.add(R.id.container, new PracticeFragment()).commit();
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
		if (id == R.id.action_plus || id == R.id.action_create)
		{
			newInvitation();
			return true;
		}
		if (id == R.id.action_lists)
		{
			contactLists();
			return true;
		}
		if (id == R.id.action_rsvp)
		{
			upcoming();
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
	
	public void newInvitation()
	{
		Intent intent = new Intent(this, NewInvitationActivity.class);
		System.out.println("PANIC");
		startActivity(intent);
	}
	
	public void contactLists()
	{
		Intent intent = new Intent(this, ContactListsActivity.class);
		startActivity(intent);
	}
	
	public void upcoming()
	{
		Intent intent = new Intent(this, RSVPEventsActivity.class);
		startActivity(intent);
	}
	
	public static double getLatitude()
	{
		return latitude;
	}
	
	public static double getLongitude()
	{
		return longitude;
	}
	
	/**
	 * This is the invitations screen. I think it makes sense for this to be the main activity.
	 */
	public static class InvitationsFragment extends Fragment
	{	
		public InvitationsFragment()
		{
			
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_invitations, container,
					false);
			
			//get location 
			LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
	    	Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	    	latitude = -1;
	    	if (location != null)
	    	{
		    	longitude = location.getLongitude();
		        latitude = location.getLatitude();
	    	}
	    	// Define a listener that responds to location updates
	    	LocationListener ll = new LocationListener() {
	    	    public void onLocationChanged(Location location) {
	    	    	// Called when a new location is found by the network location provider.
	    	    	longitude = location.getLongitude();
	    	        latitude = location.getLatitude();
	    	    }
	    	    
	    	    public void onStatusChanged(String provider, int status, Bundle extras) {}

	    	    public void onProviderEnabled(String provider) {}

	    	    public void onProviderDisabled(String provider) {}
	    	};

	    	// Register the listener with the Location Manager to receive location updates
	    	
	    		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);
	    	
	    	  
			//CRAPPY HARDCODED EVENTS 8D
			/*
			for (int i = 0; i < 7; i++)
			{
				LinearLayout lin = (LinearLayout) rootView.findViewById(R.id.invitations);
				Event invite = new Event(getActivity());
				invite.setCreator("Kevin");
				invite.setAgenda("Test 1, test 2");
				invite.setVenueInfo(""+i);
				invite.setVenuePhoto("http://i.stack.imgur.com/c5SvX.png");
				invite.setExpiresAt((long) 2000000000);
				if (i%2==1)
					invite.setBackgroundColor(getResources().getColor(R.color.lightBlue));
				invite.setId(i);
				invite.setMoreInfoObserver(new MoreInfoObserver(){
					public void moreInfo(Event event) {
						
					}
				});
				lin.addView(invite);
			}
			*/
			return rootView;
		}
		
		public void onResume()
		{
			super.onResume();
			getUpcoming();
		}
		
		public void onStop()
		{
			super.onStop();
		}
		
		public void getUpcoming()
		{
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("user", ParseUser.getCurrentUser().toString());
			ParseCloud.callFunctionInBackground("loadUpcomingMeetups", params,
					new FunctionCallback<ArrayList<Object>>(){
				public void done(ArrayList<Object> upcoming, ParseException e) {
					if (e == null)
						refreshScreen(upcoming);
					else
						e.printStackTrace();
				}
			});
		}
		
		/*
		 * This refreshes the invitations. We need to get the invitations that have been updated since the last
		 * refresh, that the user has not RSVPed to, and that have not expired.
		 * We need to order them by expiration date. So, we just have to keep track of the expiration time of the
		 * invitation at the top. Binary Search and insert 
		 * We also need a string list of IDs. Probably should keep a list of 100. Who even gets 100 invites 
		 * in a day...
		 * 
		 * Right now it just gets all the invites cause EFFICIENCY
		 */
		@SuppressWarnings("unchecked")
		public void refreshScreen(ArrayList<Object> upcoming)
		{
			ParseQuery<ParseObject> query = ParseQuery.getQuery("Meetup"); 
			query.orderByAscending("expiresAt");
			
			//get only ones that haven't expired
			query.whereGreaterThan("expiresAt", new Date());
			
			//get only ones that i am invited to
			ArrayList<ParseUser> currentUser = new ArrayList<ParseUser>();
			currentUser.add(ParseUser.getCurrentUser());
			query.whereContainsAll("invitedUsers", currentUser);
			ArrayList<String> ids = new ArrayList<String>();
			//each object is a hashmap
			for (Object o : upcoming)
			{
				//ugly shit
				ids.add(((ParseObject)((HashMap<String, Object>) o).get("meetup")).getObjectId());
				System.out.println(ids.get(ids.size()-1));
			}
			fixQuery(query, ids);
			query.findInBackground(new FindCallback<ParseObject>(){
				public void done(List<ParseObject> eventList, ParseException e) {
					if (e != null)
						System.out.println(e);
					else
					{
						List<ParseObject> list = new ArrayList<ParseObject>();
						System.out.println("eventList size is "+eventList.size());
						for (ParseObject event : eventList)
						{
							list.add(event);
							System.out.println("event added");
						}
						makeList(list);
					}
				}
			});
		}
		
		//removes all the upcoming invites in the main
		public void fixQuery(ParseQuery<ParseObject> query, ArrayList<String> ids)
		{
			query.whereNotContainedIn("objectId", ids);
		}
		
		//Takes the list of meetups from parse query and, well, lists them.
		// TODO I need to make a list of events to avoid memory leaks, also stop all timers
		public void makeList(List<ParseObject> list)
		{
			LinearLayout lin = (LinearLayout) getActivity().findViewById(R.id.invitations);
			//removes all the views for now because EFFICIENCY WHAT
			lin.removeAllViews();
			ParseUser creator = new ParseUser();
			
			for (int i = 0; i < list.size(); i++)
			{
				ParseObject meetup = list.get(i);
				Event event = new Event(getActivity());
				event.setObjectId(meetup.getObjectId());
				try {
					creator = meetup.getParseUser("creator").fetchIfNeeded();
					event.setCreator(creator.getString("firstName")+" "+creator.getString("lastName"));
				} catch (ParseException e) {
					// Auto-generated catch block
					e.printStackTrace();
				}
				event.setAgenda(meetup.getString("agenda"));
				try {
					event.setVenueInfo(meetup.getJSONObject("venueInfo").getString("name"));
					JSONArray urls = meetup.getJSONArray("venuePhotoURLs");
					if (urls != null && urls.length() > 0)
						event.setVenuePhoto(getActivity(), urls.getString(0));
				} catch (JSONException e) {
					// Auto-generated catch block
					e.printStackTrace();
				}
				//invite.setVenuePhoto(event.getParseFile(key));
				event.setExpiresAt(meetup.getDate("expiresAt"));
				
				if (i % 2 == 0)
					event.setBackgroundColor(getResources().getColor(R.color.lightBlue));
				lin.addView(event);
			}
		}
	}
	
	public static class PracticeFragment extends Fragment
	{
		private ArrayList<Handler> handlers;
		protected ArrayList<Timer> timers;
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_invitations, container,
					false);
			
			//get location 
			LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
	    	Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	    	latitude = -1;
	    	if (location != null)
	    	{
		    	longitude = location.getLongitude();
		        latitude = location.getLatitude();
	    	}
	    	// Define a listener that responds to location updates
	    	LocationListener ll = new LocationListener() {
	    	    public void onLocationChanged(Location location) {
	    	    	// Called when a new location is found by the network location provider.
	    	    	longitude = location.getLongitude();
	    	        latitude = location.getLatitude();
	    	    }
	    	    
	    	    public void onStatusChanged(String provider, int status, Bundle extras) {}

	    	    public void onProviderEnabled(String provider) {}

	    	    public void onProviderDisabled(String provider) {}
	    	};

	    	// Register the listener with the Location Manager to receive location updates
			handlers = new ArrayList<Handler>();
			timers = new ArrayList<Timer>();
			
	    	lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);
			return rootView;
		}
		
		public void onResume()
		{
			super.onResume();
			getUpcoming();
		}
		
		public void onStop()
		{
			super.onStop();
			for (int i = 0; i < timers.size(); i++)
				timers.get(i).cancel();
		}
		
		public void getUpcoming()
		{
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("user", ParseUser.getCurrentUser().toString());
			ParseCloud.callFunctionInBackground("loadUpcomingMeetups", params,
					new FunctionCallback<ArrayList<Object>>(){
				public void done(ArrayList<Object> upcoming, ParseException e) {
					if (e == null)
						refreshScreen(upcoming);
					else
						e.printStackTrace();
				}
			});
		}
		
		/*
		 * This refreshes the invitations. We need to get the invitations that have been updated since the last
		 * refresh, that the user has not RSVPed to, and that have not expired.
		 * We need to order them by expiration date. So, we just have to keep track of the expiration time of the
		 * invitation at the top. Binary Search and insert 
		 * We also need a string list of IDs. Probably should keep a list of 100. Who even gets 100 invites 
		 * in a day...
		 * 
		 * Right now it just gets all the invites cause EFFICIENCY
		 */
		@SuppressWarnings("unchecked")
		public void refreshScreen(ArrayList<Object> upcoming)
		{
			ParseQuery<ParseObject> query = ParseQuery.getQuery("Meetup"); 
			query.orderByAscending("expiresAt");
			
			//get only ones that haven't expired
			query.whereGreaterThan("expiresAt", new Date());
			
			//get only ones that i am invited to
			ArrayList<ParseUser> currentUser = new ArrayList<ParseUser>();
			currentUser.add(ParseUser.getCurrentUser());
			query.whereContainsAll("invitedUsers", currentUser);
			ArrayList<String> ids = new ArrayList<String>();
			//each object is a hashmap
			for (Object o : upcoming)
			{
				//ugly shit
				ids.add(((ParseObject)((HashMap<String, Object>) o).get("meetup")).getObjectId());
				System.out.println(ids.get(ids.size()-1));
			}
			fixQuery(query, ids);
			query.findInBackground(new FindCallback<ParseObject>(){
				public void done(List<ParseObject> eventList, ParseException e) {
					if (e != null)
						System.out.println(e);
					else
					{
						List<ParseObject> list = new ArrayList<ParseObject>();
						System.out.println("eventList size is "+eventList.size());
						for (ParseObject event : eventList)
						{
							list.add(event);
							System.out.println("event added");
						}
						makeList(list);
					}
				}
			});
		}
		
		//removes all the upcoming invites in the main
		public void fixQuery(ParseQuery<ParseObject> query, ArrayList<String> ids)
		{
			query.whereNotContainedIn("objectId", ids);
		}
		
		//Takes the list of meetups from parse query and, well, lists them.
		// TODO I need to make a list of events to avoid memory leaks, also stop all timers
		public void makeList(List<ParseObject> list)
		{
			LinearLayout lin = (LinearLayout) getActivity().findViewById(R.id.invitations);
			//removes all the views for now because EFFICIENCY WHAT
			lin.removeAllViews();
			ParseUser creator = new ParseUser();
			
			//THIS IS ACTUALLY SO STUPID CUSTOM VIEW WHY YOU SO TSUN
			for (int i = 0; i < list.size(); i++)
			{
				final ParseObject meetup = list.get(i);
				
				View event = View.inflate(getActivity(), R.layout.invite_card, null);
				TextView tv;
				//event.setObjectId(meetup.getObjectId());
				try {
					creator = meetup.getParseUser("creator").fetchIfNeeded();
					tv = (TextView) event.findViewById(R.id.creator);
					tv.setText(creator.getString("firstName")+" "+creator.getString("lastName"));
				} catch (ParseException e) {
					// Auto-generated catch block
					e.printStackTrace();
				}
				tv = (TextView) event.findViewById(R.id.agenda);
				tv.setText(ParseUser.getCurrentUser().getString("firstName") + ", " + 
						meetup.getString("agenda"));
				try {
					tv = (TextView) event.findViewById(R.id.venueInfo);
					tv.setText(meetup.getJSONObject("venueInfo").getString("name"));
					JSONArray urls = meetup.getJSONArray("venuePhotoURLs");
					ImageView v = ((ImageView) event.findViewById(R.id.image));
					if (urls != null && urls.length() > 0)
						Picasso.with(getActivity()).load(urls.getString(0)).into(v);
				} catch (JSONException e) {
					// Auto-generated catch block
					e.printStackTrace();
				}

				Timer timer = new Timer("RSVPTimer");
				timers.add(timer);
				final Date expiresAt = meetup.getDate("expiresAt");
				final TextView mTimeToRSVPView = (TextView) event.findViewById(R.id.timer);
				//Handles changing the RSVP time every second with the timer
				Handler handler = new Handler() {
					public void handleMessage(Message message)
					{
						Date now = new Date();
						long timeToRSVP = expiresAt.getTime() - now.getTime();
						String time = "" + (int)timeToRSVP/(60*60*1000) + ":";
						int minutes = (int)(timeToRSVP/(60*1000))%60;
						if (minutes < 10)
							time = time + "0";
						time = time + minutes + ":";
						int seconds = (int)(timeToRSVP/1000)%60;
						if (seconds < 10)
							time = time+ "0";
						time = time + seconds;
						//System.out.println(time);
						mTimeToRSVPView.setText(time);
						mTimeToRSVPView.invalidate();
						mTimeToRSVPView.requestLayout();
						//invalidate();
						//requestLayout();
					}
				};
				handlers.add(handler);
				timer.schedule(new RSVPTimerTask(handlers.size() - 1), 10, 1000);
				
				final String objectId = meetup.getObjectId();
				((LinearLayout) event.findViewById(R.id.moreInfoBox))
					.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(getActivity(), MoreInfoActivity.class);
							Bundle bundle = new Bundle();
							bundle.putString("objectId", objectId);
							bundle.putBoolean("hasResponded", false);
							bundle.putBoolean("isCreator", false);
							intent.putExtras(bundle);
							getActivity().startActivity(intent);
							
						}
				});;
				
				Button button = (Button) event.findViewById(R.id.no);
				button.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),
						 "fonts/fontawesome-webfont.ttf"));
				button.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(final View v) {
						System.out.println("no");
						v.setBackgroundColor(getResources().getColor(R.color.red));
						HashMap<String, Object> params = new HashMap<String, Object>();
						params.put("meetupId", objectId);
						params.put("willAttend", false);
						ParseCloud.callFunctionInBackground("respondToMeetup", params,
								new FunctionCallback<Object>() {
							@Override
							public void done(Object o, ParseException e) {
								if (e == null)
									((LinearLayout) v.getParent().getParent())
										.removeView((View) v.getParent());
								else
									e.printStackTrace();
							}
						});
					}
				});
				button = (Button) event.findViewById(R.id.yes);
				button.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),
						 "fonts/fontawesome-webfont.ttf"));
				button.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(final View v) {
						System.out.println("no");
						v.setBackgroundColor(getResources().getColor(R.color.green));
						HashMap<String, Object> params = new HashMap<String, Object>();
						params.put("meetupId", objectId);
						params.put("willAttend", false);
						ParseCloud.callFunctionInBackground("respondToMeetup", params,
								new FunctionCallback<Object>() {
							@Override
							public void done(Object o, ParseException e) {
								if (e == null)
									((LinearLayout) v.getParent().getParent())
										.removeView((View) v.getParent());
								else
									e.printStackTrace();
							}
						});
					}
				});
				
				LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				lp.setMargins(20, 20, 20, 20);
				event.setLayoutParams(lp);
				
				lin.addView(event);
			}
			
			//lin.addView(View.inflate(getActivity(), R.layout.invite_card, null));

			lin.invalidate();
			lin.requestLayout();
			
		}

		public class RSVPTimerTask extends TimerTask
		{ 
			private int pos;
			
			public RSVPTimerTask(int i)
			{
				super();
				pos = i;
			}
			
			@Override
			public void run() {
				//System.out.println(pos);
				handlers.get(pos).obtainMessage(1).sendToTarget();
			}
		}
	}
}
