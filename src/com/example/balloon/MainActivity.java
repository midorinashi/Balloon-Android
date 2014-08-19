package com.example.balloon;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.Parse;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.PushService;
import com.squareup.picasso.Picasso;

public class MainActivity extends ProgressActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//initialize parse
		Parse.initialize(this, "iXEPNEZfJXoEOIayxLgBBgpShMZBTj7ReVoi1eqn",
				"GHtE0svPk0epFG4olYnFTnnDtmARHtENXxXuHoXp");
		PushService.setDefaultPushCallback(this, MainActivity.class);
		
		String Mao = "+18007580051";
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
			PushService.subscribe(this, "", this.getClass());
			PushService.subscribe(this, "u" + ParseUser.getCurrentUser().getObjectId(), this.getClass());
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
		
		getFragmentManager().beginTransaction()
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
			newInvitation(null);
			return true;
		}
		if (id == R.id.action_lists)
		{
			contactLists();
			return true;
		}
		if (id == R.id.action_rsvp)
		{
			upcoming(null);
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
	
	public void newInvitation(View view)
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
	
	public void upcoming(View view)
	{
		Intent intent = new Intent(this, RSVPEventsActivity.class);
		startActivity(intent);
	}
	
	public static class PracticeFragment extends ProgressFragment
	{
		private ArrayList<Handler> handlers;
		protected ArrayList<Timer> timers;
		private ViewAnimator switcher;
		private LinearLayout lin;
		public PullToRefreshBase<ScrollView> pullToRefreshView;
		private int numViews;
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_invitations, container,
					false);

	    	// Register the listener with the Location Manager to receive location updates
			handlers = new ArrayList<Handler>();
			timers = new ArrayList<Timer>();
			return rootView;
		}
		
		public void onResume()
		{
			super.onResume();
			if (switcher == null)
			{
				switcher = (ViewAnimator) getActivity().findViewById(R.id.animator);
				lin = (LinearLayout) getActivity().findViewById(R.id.invitations);
				pullToRefreshView = (PullToRefreshScrollView)
						getActivity().findViewById(R.id.scrollToRefresh);
				pullToRefreshView.setOnRefreshListener(new OnRefreshListener<ScrollView>() {
				    @Override
				    public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
				        getUpcoming();
				        new GetDataTask().execute();
				    }
				});
			}
			
			getUpcoming();
		}
		
		// to prevent memory leaks and shit
		public void onStop()
		{
			super.onStop();
			for (int i = 0; i < timers.size(); i++)
				timers.get(i).cancel();
		}
		
		public void getUpcoming()
		{
			showSpinner();
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("user", ParseUser.getCurrentUser().toString());
			ParseCloud.callFunctionInBackground("loadUpcomingMeetups", params,
					new FunctionCallback<ArrayList<Object>>(){
				public void done(ArrayList<Object> upcoming, ParseException e) {
					if (e == null)
					{
						if (upcoming.size() == 0)
							noPlans();
						else
							refreshScreen(upcoming);
					}
					else
						showParseException(e);
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
			query.whereNotContainedIn("objectId", ids);
			query.include("creator");
			
			query.findInBackground(new FindCallback<ParseObject>(){
				public void done(List<ParseObject> eventList, ParseException e) {
					if (e != null)
						showParseException(e);
					else
					{
						List<ParseObject> list = new ArrayList<ParseObject>();
						System.out.println("eventList size is "+eventList.size());
						for (ParseObject event : eventList)
						{
							list.add(event);
							System.out.println("event added");
						}
						if (list.size() == 0)
							noPlans();
						else
							makeList(list);
					}
				}
			});
		}
		
		//Takes the list of meetups from parse query and, well, lists them.
		// TODO I need to make a list of events to avoid memory leaks, also stop all timers
		public void makeList(List<ParseObject> list)
		{
			//removes all the views for now because EFFICIENCY WHAT
			lin.removeAllViews();
			ParseUser creator = new ParseUser();
			setViewCount(list.size());
			
			//THIS IS ACTUALLY SO STUPID CUSTOM VIEW WHY YOU SO TSUN
			for (int i = 0; i < list.size(); i++)
			{
				final ParseObject meetup = list.get(i); 
				
				View event = View.inflate(getActivity(), R.layout.invite_card, null);
				TextView tv;
				//event.setObjectId(meetup.getObjectId());
				creator = meetup.getParseUser("creator");
				tv = (TextView) event.findViewById(R.id.creator);
				tv.setText(creator.getString("firstName")+" "+creator.getString("lastName"));
				
				tv = (TextView) event.findViewById(R.id.agenda);
				tv.setText(ParseUser.getCurrentUser().getString("firstName") + ", " + 
						meetup.getString("agenda"));
				try {
					tv = (TextView) event.findViewById(R.id.venueInfo);
					final JSONObject venueInfo = meetup.getJSONObject("venueInfo");
					tv.setText(venueInfo.getString("name"));
					tv.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							Intent intent;
							try {
								String str = "geo:0,0?q="+venueInfo.getJSONObject("location").getDouble("lat")
										+","+venueInfo.getJSONObject("location").getDouble("lng")
										+" (" + venueInfo.getString("name") + ")";
								System.out.println(str);
								intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(str));
								if (intent.resolveActivity(getActivity().getPackageManager()) != null)
								{
									startActivity(intent);
									System.out.println("Geo works!");
								}
								else
								{
									System.out.println("Geo does not work");
									//TODO puts down a lot of markers. Don't know why
									String address = "";
									JSONArray formattedAddress;
									try {
										formattedAddress = venueInfo.getJSONObject("address")
												.getJSONArray("formattedAddress");
										for (int i = 0; i < formattedAddress.length(); i++)
											address += " " + formattedAddress.getString(i);
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									//need to get rid of first space
									String url = ("http://www.maps.google.com/maps?q="+(address).substring(1))
											.replaceAll(" ", "+");
									Uri uri = Uri.parse(url);
									intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
									if (intent.resolveActivity(getActivity().getPackageManager()) != null)
										startActivity(intent);
									else
										Toast.makeText(getActivity(), "Oops! No location found",
												Toast.LENGTH_SHORT).show();
								}
							} catch (JSONException e) {
								Toast.makeText(getActivity(), "Oops! No location found",
										Toast.LENGTH_SHORT).show();
								e.printStackTrace();
							}
						}
					});
					JSONArray urls = meetup.getJSONArray("venuePhotoURLs");
					ImageView v = ((ImageView) event.findViewById(R.id.image));
					if (urls != null && urls.length() > 0)
						Picasso.with(getActivity()).load(urls.getString(0)).into(v);
					else
						Picasso.with(getActivity()).load(R.drawable.logo).into(v);
				} catch (JSONException e) {
					// Auto-generated catch block
					e.printStackTrace();
				}

				Timer timer = new Timer("RSVPTimer");
				timers.add(timer);
				final Date expiresAt = meetup.getDate("expiresAt");
				final TextView mTimeToRSVPView = (TextView) event.findViewById(R.id.timer);
				final TextView mLeftToRSVP = (TextView) event.findViewById(R.id.leftToRSVP);
				//Handles changing the RSVP time every second with the timer
				Handler handler = new Handler() {
					public void handleMessage(Message message)
					{
						Date now = new Date();
						long timeToRSVP = expiresAt.getTime() - now.getTime();
						if (timeToRSVP < 0)
						{
							mTimeToRSVPView.setText("");
							mLeftToRSVP.setText("");
							// I want to cancel the handler, timer, and view
							int index = handlers.indexOf(this);
							timers.get(index).cancel();
							if (subtractViewCount() <= 0)
								noPlans();
						}
						else
						{
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
					}
				};
				final int index = handlers.size();
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
						showSpinner();
						HashMap<String, Object> params = new HashMap<String, Object>();
						params.put("meetupId", objectId);
						params.put("willAttend", false);
						ParseCloud.callFunctionInBackground("respondToMeetup", params,
								new FunctionCallback<Object>() {
							@Override
							public void done(Object o, ParseException e) {
								if (e == null)
								{
									((LinearLayout) v.getParent().getParent())
										.removeView((View) v.getParent());
									timers.get(index).cancel();
									removeSpinner();
									if (subtractViewCount() <= 0)
										noPlans();
								}
								else
									showParseException(e);
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
						showSpinner();
						HashMap<String, Object> params = new HashMap<String, Object>();
						params.put("meetupId", objectId);
						params.put("willAttend", false);
						ParseCloud.callFunctionInBackground("respondToMeetup", params,
								new FunctionCallback<Object>() {
							@Override
							public void done(Object o, ParseException e) {
								if (e == null)
								{
									timers.get(index).cancel();
									((LinearLayout) v.getParent().getParent())
										.removeView((View) v.getParent());
									removeSpinner();
									if (subtractViewCount() <= 0)
										noPlans();
								}
								else
									showParseException(e);
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
			System.out.println("DONE");
			showScreen(0);
		}
		
		public void setViewCount(int i)
		{
			numViews = i;
		}
		
		public int subtractViewCount()
		{
			numViews--;
			return numViews;
		}
		
		public void noPlans()
		{
			ParseCloud.callFunctionInBackground("hasPlans", new HashMap<String, Object>(), 
					new FunctionCallback<HashMap<String, Boolean>>() {
				@Override
				public void done(HashMap<String, Boolean> hasPlans, ParseException e) {
					if (e != null)
						showParseException(e);
					else if (hasPlans.get("hasPlans"))
						showScreen(1);
					else
						showScreen(2);
				}
			});
		}
		
		public void showScreen(int viewIndex)
		{
			removeSpinner();
			//we want to go to the right view now - we add three to avoid negative mods
			if ((switcher.getDisplayedChild() - viewIndex + 3) % 3 == 1)
				switcher.showPrevious();
			else if ((switcher.getDisplayedChild() - viewIndex + 3) % 3 == 2)
				switcher.showNext();
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
		
		private class GetDataTask extends AsyncTask<Void, Void, String[]> {
		    @Override
		    protected void onPostExecute(String[] result) {
		        // Call onRefreshComplete when the list has been refreshed.
		        pullToRefreshView.onRefreshComplete();
		        super.onPostExecute(result);
		    }

			@Override
			protected String[] doInBackground(Void... arg0) {
				// TODO Auto-generated method stub
				return null;
			}
		}
	}
}
