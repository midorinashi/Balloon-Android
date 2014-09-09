package com.j32productions.balloon;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

public class RSVPEventsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rsvpevents);
		setTitle(R.string.title_activity_rsvpevents);
		getActionBar().setDisplayHomeAsUpEnabled(false);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_no_plus, menu);
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
	
	public void newInvitation(View view)
	{
		Intent intent = new Intent(this, NewInvitationActivity.class);
		startActivity(intent);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends ProgressFragment {

		private ArrayList<Handler> handlers;
		protected ArrayList<Timer> timers;
		private ViewAnimator switcher;
		public PullToRefreshBase<ScrollView> pullToRefreshView;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_rsvpevents,
					container, false);
			showSpinner();
			handlers = new ArrayList<Handler>();
			timers = new ArrayList<Timer>();
			return rootView;
		}
		
		public void onResume()
		{
			super.onResume();
			showSpinner();
			if (switcher == null)
			{
				switcher = (ViewAnimator) getActivity().findViewById(R.id.animator);
				pullToRefreshView = (PullToRefreshScrollView)
						getActivity().findViewById(R.id.scrollToRefresh);
				pullToRefreshView.setOnRefreshListener(new OnRefreshListener<ScrollView>() {
				    @Override
				    public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
				        showSpinner();
				        getUpcoming();
				        new GetDataTask().execute();
				    }
				});
			}
			getUpcoming();
		}
		
		public void getUpcoming()
		{
			// TODO make meetups that are have expired go away
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("user", ParseUser.getCurrentUser().toString());
			ParseCloud.callFunctionInBackground("loadUpcomingMeetups", params,
					new FunctionCallback<ArrayList<HashMap<String, Object>>>(){
				public void done(ArrayList<HashMap<String, Object>> upcoming, ParseException e) {
					if (e == null)
						makeList(upcoming);
					else
						showParseException(e);
				}
			});
		}
		
		//Takes the list of meetups from parse query and, well, lists them.
		// TODO I need to make a list of events to avoid memory leaks, also stop all timers
		public void makeList(ArrayList<HashMap<String, Object>> upcoming)
		{
			if (getActivity() instanceof RSVPEventsActivity)
			{
				if (upcoming.size() == 0)
				{
					if (switcher.getDisplayedChild() == 0)
						switcher.showNext();
				}
				else
				{
					System.out.println("size is " + upcoming.size());
					final LinearLayout lin = (LinearLayout) getActivity().findViewById(R.id.invitations);
					System.out.println(lin);
					//removes all the views for now because EFFICIENCY WHAT
					lin.removeAllViews();
					final LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, 1);
					View firstLine = new View(getActivity());
					firstLine.setBackgroundColor(getActivity().getResources().getColor(R.color.lineGray));
					firstLine.setLayoutParams(lp);
					lin.addView(firstLine);
					
					for (int i = 0; i < upcoming.size(); i++)
					{
						final View line = new View(getActivity());
						final View event = View.inflate(getActivity(), R.layout.list_item_plans, null);
						ParseObject meetup = (ParseObject) upcoming.get(i).get("meetup");
						
						//invite.setVenuePhoto(event.getParseFile(key));
						try {
							((TextView) event.findViewById(R.id.creator)).setText(meetup
									.getParseUser("creator").getString("firstName") + " " + 
									meetup.getParseUser("creator").getString("lastName"));
							((TextView) event.findViewById(R.id.agenda)).setText(meetup.getString("agenda"));
							
							
							((TextView) event.findViewById(R.id.venueInfo)).setText
								(meetup.getJSONObject("venueInfo").getString("name"));
		 
							JSONArray urls = meetup.getJSONArray("venuePhotoURLs");
							if (urls != null && urls.length() > 0)
								Picasso.with(getActivity()).load(urls.getString(0)).resize(150, 150).centerCrop()
									.into((ImageView) event.findViewById(R.id.eventImage));
							else
								Picasso.with(getActivity()).load(R.drawable.logo).resize(150, 150).centerCrop()
									.into((ImageView) event.findViewById(R.id.eventImage));
							
							Timer timer = new Timer("RSVPTimer");
							timers.add(timer);
							final Date expiresAt = meetup.getDate("expiresAt");
							final Date startsAt = meetup.getDate("startsAt");
							if (meetup.has("spotsLeft"))
							{
								int spotsLeft = meetup.getInt("spotsLeft");
								TextView tv = (TextView) event.findViewById(R.id.spotsLeft);
								if (spotsLeft == 0)
									tv.setText(R.string.no_spots_left);
								else
									tv.setText(getResources().getQuantityString(R.plurals.spotsLeft,
											spotsLeft, spotsLeft));
								tv.setVisibility(View.VISIBLE);
							}
							final TextView mTimeToRSVPView = (TextView) event.findViewById(R.id.timer);
							//Handles changing the RSVP time every second with the timer
							Handler handler = new Handler() {
								
								final String TO_CHANGE_RESPONSE = getString(R.string.to_change_response);
								final String STARTS_IN = getString(R.string.starts_in);
								final String STARTED = getString(R.string.started);
								final ColorStateList BLACK = ((TextView) event
										.findViewById(R.id.creator)).getTextColors();
								
								public void handleMessage(Message message)
								{
									Date now = new Date();
									long timeToRSVP = expiresAt.getTime() - now.getTime();
									if (timeToRSVP < 0)
									{
										if (startsAt != null)
										{
											timeToRSVP = startsAt.getTime() - now.getTime();
											if (timeToRSVP < 0)
											{
												mTimeToRSVPView.setTextColor(BLACK);
												mTimeToRSVPView.setText(STARTED);
												// I want to cancel the handler, timer, and view
												int index = handlers.indexOf(this);
												timers.get(index).cancel();
											}
											else
											{
												mTimeToRSVPView.setTextColor(BLACK);
												mTimeToRSVPView.setText(String.format(Locale.US, STARTS_IN,
														timeToRSVP/(24*60*60*1000),
														(int)(timeToRSVP/(60*60*1000)%24),
														(int)(timeToRSVP/(60*1000)%60),
														(int)(timeToRSVP/1000)%60));
												mTimeToRSVPView.invalidate();
												mTimeToRSVPView.requestLayout();
											}
										}
										else
										{
											mTimeToRSVPView.setTextColor(BLACK);
											mTimeToRSVPView.setText(getString(R.string.deadline_passed));
											// I want to cancel the handler, timer, and view
											int index = handlers.indexOf(this);
											timers.get(index).cancel();
										}
									}
									else
									{
										String time = "" + timeToRSVP/(60*60*1000) + ":";
										int minutes = (int)(timeToRSVP/(60*1000))%60;
										if (minutes < 10)
											time = time + "0";
										time = time + minutes + ":";
										int seconds = (int)(timeToRSVP/1000)%60;
										if (seconds < 10)
											time = time+ "0";
										time = time + seconds;
										//System.out.println(time);
										String str = time + " " + TO_CHANGE_RESPONSE;
										mTimeToRSVPView.setText(str);
										mTimeToRSVPView.invalidate();
										mTimeToRSVPView.requestLayout();
										//invalidate();
										//requestLayout();
									}
								}
							};
							handlers.add(handler);
							timer.schedule(new RSVPTimerTask(handlers.size() - 1), 10, 1000);
								
							
							
							//to set the onclick listener
							final String objectId = meetup.getObjectId();
							final boolean hasResponded = true;
							final boolean isCreator = ParseUser.getCurrentUser().getObjectId()
									.equals(meetup.getParseUser("creator").getObjectId());
							final boolean willAttend = ((ParseObject) upcoming.get(i)
									.get("response")).getBoolean("isAttending");
	
							event.setOnClickListener(new View.OnClickListener() {
								public void onClick(View v) {
									Intent intent = new Intent(getActivity(), MoreInfoActivity.class);
									Bundle bundle = new Bundle();
									bundle.putString("objectId", objectId);
									bundle.putBoolean("hasResponded", hasResponded);
									bundle.putBoolean("willAttend", willAttend);
									bundle.putBoolean("isCreator", isCreator);
									intent.putExtras(bundle);
									(getActivity()).startActivity(intent);
									
								}
							});

							event.setVisibility(View.GONE);
							line.setVisibility(View.GONE);
							
							ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Response");
							query.whereEqualTo("meetup", meetup);
							query.whereEqualTo("responder", ParseUser.getCurrentUser());
							query.getFirstInBackground(new GetCallback<ParseObject>() {
								@Override
								public void done(ParseObject response,
										ParseException e) {
									if (response != null)
									{
										int color = response.getBoolean("isAttending") ?
												getActivity().getResources().getColor(R.color.darkGreen) :
												getActivity().getResources().getColor(R.color.red);
										event.findViewById(R.id.RSVP).setBackgroundColor(color);
										if (response.getBoolean("isAttending") ||
												expiresAt.getTime() - new Date().getTime() > 0)
										{
											event.setVisibility(View.VISIBLE);
											line.setVisibility(View.VISIBLE);
											if (switcher.getDisplayedChild() == 1)
												switcher.showPrevious();
										}
									}
								}
							});
						} catch (JSONException e) {
							e.printStackTrace();
						}
						lin.addView(event);
						line.setBackgroundColor(getActivity().getResources()
								.getColor(R.color.lineGray));
						line.setLayoutParams(lp);
						lin.addView(line);
					}
				}
			}
			removeSpinner();
		}
		
		public void onStop()
		{
			removeSpinner();
			super.onStop();
			for (int i = 0; i < timers.size(); i++)
				timers.get(i).cancel();
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
