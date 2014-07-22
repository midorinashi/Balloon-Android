package com.example.balloon;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class MoreInfoActivity extends ActionBarActivity
{
	private static String mObjectId;
	private static boolean mIsCreator;
	private static String mCreator;
	private static String mAgenda;
	private static String mVenueInfo;
	private static Date mExpiresAt;
	private static String mTimeToRSVP;
	private static boolean mHasResponded;
	private static boolean mWillAttend;
	protected static ParseObject mMeetup;
	protected JSONObject mVenueLocation;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_more_info);
		mObjectId = getIntent().getExtras().getString("objectId");
		System.out.println(mObjectId);
		
		if (savedInstanceState == null) {

			ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Meetup");
			query.whereEqualTo("objectId", mObjectId);
			query.getFirstInBackground(new GetCallback<ParseObject>(){
				public void done(ParseObject meetup, ParseException e) {
					if (e == null)
					{
						mMeetup = meetup;
						ParseUser creator = meetup.getParseUser("creator");
						try {
							//should be a fetch in background
							creator.fetchIfNeeded();
							mCreator = creator.getString("firstName") + " " + creator.getString("lastName");
							if (creator == ParseUser.getCurrentUser())
								mIsCreator = true;
						} catch (ParseException e1) {
							e1.printStackTrace();
						}
						
						mAgenda = meetup.getString("agenda");
						
						try {
							mVenueInfo = meetup.getJSONObject("venueInfo").getString("name");
						} catch (JSONException e1) {
							// Auto-generated catch block
							e1.printStackTrace();
						}
						
						try {
							mVenueLocation = meetup.getJSONObject("venueInfo").getJSONObject("location");
						} catch (JSONException e1) {
							//it's a custom venue
							mVenueLocation = null;
						}
						
						mExpiresAt = meetup.getDate("expiresAt");
						
						//this will get all the comments
						ParseQuery<ParseObject> commentQuery = new ParseQuery<ParseObject>("Comment");
						commentQuery.whereEqualTo("meetup", meetup);
						commentQuery.include("commenter");
						commentQuery.orderByAscending("createdAt");
						commentQuery.findInBackground(new FindCallback<ParseObject>() {

							@Override
							public void done(List<ParseObject> comments, ParseException e) {
								if (e == null)
								{
									System.out.println(comments.size());
									if (comments.size() > 0)
										makeCommentList(comments);
								}
								else
									e.printStackTrace();
							}
						});
						
						//this will get who's coming, starting with all the responses
						//TODO Look into using include?? I can't seem to get first names tho
						ParseQuery<ParseObject> comingQuery = new ParseQuery<ParseObject>("Response");
						comingQuery.whereEqualTo("meetup", meetup);
						comingQuery.whereEqualTo("isAttending", true);
						comingQuery.findInBackground(new FindCallback<ParseObject>() {

							@Override
							public void done(List<ParseObject> responses, ParseException e) {
								if (e == null)
								{
									if (responses.size() > 0)
										fetchNames(responses, false);
									else
										makeComingList(new String[0]);
								}
								else
									e.printStackTrace();
							}
						});
						
						makeMoreInfoFragment();
					}
					else
						System.out.println(e);
				}
			});
		}
		mHasResponded = getIntent().getExtras().getBoolean("hasResponded");
		mWillAttend = getIntent().getExtras().getBoolean("willAttend");
		mIsCreator = getIntent().getExtras().getBoolean("isCreator");
	}
	
	public void makeCommentList(final List<ParseObject> comments)
	{
		for (ParseObject c : comments)
		{
			System.out.println(c.getString("comment"));
		}
		ArrayAdapter<ParseObject> arrayAdapter = new ArrayAdapter<ParseObject>(this,
				android.R.layout.simple_list_item_2, android.R.id.text1, comments) {
			  @Override
			  public View getView(int position, View convertView, ViewGroup parent) {
			    View view = super.getView(position, convertView, parent);
			    ParseUser user = comments.get(position).getParseUser("commenter");
			    TextView text1 = (TextView) view.findViewById(android.R.id.text1);
			    TextView text2 = (TextView) view.findViewById(android.R.id.text2);

			    text1.setText(user.getString("firstName") + " " + user.getString("lastName"));
			    text2.setText(comments.get(position).getString("comment"));
			    
			    System.out.println(text1.getText());
			    System.out.println(text2.getText());
			    System.out.println(position);
			    return view;
			  }
		};
	    ListView lv = (ListView) findViewById(R.id.commentsList);
	    lv.setAdapter(arrayAdapter);
	    System.out.println("num list items is : " + lv.getCount());
	}
	
	//forComments is true when fetching names of commenters, false when it's for responses
	public void fetchNames(List<ParseObject> list, boolean forComments)
	{
		ArrayList<ParseQuery<ParseUser>> queries = new ArrayList<ParseQuery<ParseUser>>();
		for (int i = 0; i < list.size(); i++)
		{
			ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
			userQuery.whereEqualTo("objectId", list.get(i).getParseUser("responder").getObjectId());
			queries.add(userQuery);
		}
		ParseQuery<ParseUser> mainQuery = ParseQuery.or(queries);
		mainQuery.orderByAscending("firstName");
		mainQuery.addAscendingOrder("lastName");
		mainQuery.findInBackground(new FindCallback<ParseUser>() {
			public void done(List<ParseUser> userList, ParseException e) {
				if (e == null)
				{
					String[] names = new String[userList.size()];
					for (int i = 0; i < userList.size(); i++)
						names[i] = userList.get(i).getString("firstName") + " " + 
									userList.get(i).getString("lastName");
					makeComingList(names);
				}
				else
					e.printStackTrace();
			}
		});
	}
	
	public void makeComingList(String[] names)
	{
		System.out.println("Making coming list names# = " + names.length);
		int layout = android.R.layout.simple_list_item_1;
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, layout, names);
	    
	    // each time we are started use our listadapter
	    ListView lv = (ListView) findViewById(R.id.comingList);
	    lv.setAdapter(arrayAdapter);
	}
	
	private void makeMoreInfoFragment()
	{
		getSupportFragmentManager().beginTransaction()
				.add(R.id.moreinfocontainer, new MoreInfoFragment()).commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.more_info, menu);
		return true;
	}
	
	public boolean onPrepareOptionsMenu(final Menu menu) {
		//set to edit text string if necessary
		if (mIsCreator)
			menu.findItem(R.id.action_agenda).setTitle(getResources()
					.getString(R.string.action_edit_agenda));
	    return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_agenda) {
			if (mIsCreator)
			{
				FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
				transaction.replace(R.id.moreinfocontainer, new EditAgendaFragment());
				transaction.addToBackStack(null);
				transaction.commit();
			}
			else
				showAgenda(null);
			return true;
		}
		else if (id == R.id.action_comment) {
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			transaction.replace(R.id.moreinfocontainer, new AddCommentFragment(), "AddCommentFragment");
			transaction.addToBackStack(null);
			transaction.commit();
			return true;
		}
		else if (id == R.id.action_locate) {
			locate();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void showAgenda(View view)
	{
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.moreinfocontainer, new AgendaFragment());
		transaction.addToBackStack(null);
		transaction.commit();
	}
	
	public void respondNo(View view)
	{
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("meetupId", mObjectId);
		params.put("willAttend", false);
		ParseCloud.callFunctionInBackground("respondToMeetup", params, new FunctionCallback<Object>() {
			public void done(Object o, ParseException e) {
				if (e == null)
				{
					//gotta refetch the people coming cause i changed everything
					ParseQuery<ParseObject> comingQuery = new ParseQuery<ParseObject>("Response");
					comingQuery.whereEqualTo("meetup", mMeetup);
					comingQuery.whereEqualTo("isAttending", true);
					comingQuery.findInBackground(new FindCallback<ParseObject>() {

						@Override
						public void done(List<ParseObject> responses, ParseException e) {
							if (e == null)
							{
								if (responses.size() > 0)
									fetchNames(responses, false);
								else
									makeComingList(new String[0]);
							}
							else
								e.printStackTrace();
						}
					});
				}
				else
					e.printStackTrace();
			}
		});
		view.setBackgroundColor(getResources().getColor(R.color.red));
		findViewById(R.id.yes).setBackgroundColor(getResources().getColor(R.color.buttonBlue));
	}
	
	public void respondYes(View view)
	{
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("meetupId", mObjectId);
		params.put("willAttend", true);
		ParseCloud.callFunctionInBackground("respondToMeetup", params, new FunctionCallback<Object>() {
			public void done(Object o, ParseException e) {
				if (e == null)
				{
					//gotta refetch the people coming cause i changed everything
					ParseQuery<ParseObject> comingQuery = new ParseQuery<ParseObject>("Response");
					comingQuery.whereEqualTo("meetup", mMeetup);
					comingQuery.whereEqualTo("isAttending", true);
					comingQuery.findInBackground(new FindCallback<ParseObject>() {

						@Override
						public void done(List<ParseObject> responses, ParseException e) {
							if (e == null)
							{
								if (responses.size() > 0)
									fetchNames(responses, false);
								else
									makeComingList(new String[0]);
							}
							else
								e.printStackTrace();
						}
					});
				}
				else
					e.printStackTrace();
			}
		});
		view.setBackgroundColor(getResources().getColor(R.color.green));
		findViewById(R.id.no).setBackgroundColor(getResources().getColor(R.color.buttonBlue));
	}

	public void addComment(View view)
	{
		EditText et = (EditText) findViewById(R.id.comment);
		String comment = et.getText().toString();
		
		ParseObject c = new ParseObject("Comment");
		c.put("comment", comment);
		c.put("commenter", ParseUser.getCurrentUser());
		c.put("meetup", mMeetup);
		c.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if (e == null)
					outputToast("Comment added!");
				else
					e.printStackTrace();
			}
		});
	}
	
	public void changeAgenda(View view)
	{
		EditText et = (EditText) findViewById(R.id.editAgenda);
		String agenda = et.getText().toString();
		
		mMeetup.put("agenda", agenda);
		mMeetup.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if (e == null)
					outputToast("Agenda changed!");
				else
					e.printStackTrace();
			}
		});
	}
	
	public void locate()
	{
		if (mVenueLocation != null)
		{
			String lat = "";
			String lng = "";
			try {
				lat = mVenueLocation.getString("lat");
				lng = mVenueLocation.getString("lng");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String uriBegin = Uri.encode("geo:" + lat + "," + lng);
			String uriQuery = Uri.encode(lat + "," + lng + "(" + mVenueInfo + ")");
			//need to deal with spaces
			System.out.println("Begin: " + uriBegin);
			System.out.println("Query: " + uriQuery);
			Uri uri = Uri.parse(uriBegin + "?q=" + uriQuery);
			Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
			if (intent.resolveActivity(getPackageManager()) != null)
				startActivity(intent);
			else
			{
				//TODO puts down a lot of markers. Don't know why
				String url = ("https://www.google.com/maps/place/"+mVenueInfo+"/@"+lat+","+lng)
						.replaceAll(" ", "%20");
				uri = Uri.parse(url);
				intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
				if (intent.resolveActivity(getPackageManager()) != null)
					startActivity(intent);
				else
					outputToast("Oops! No map application found.");
			}
		}
		else
			outputToast("Oops! This is a custom location with no known address.");
		
	}
	
	public void outputToast(CharSequence text)
	{
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(this, text, duration);
		toast.show();
		
		cancel(null);
	}
	
	public void cancel(View view)
	{
		getSupportFragmentManager().popBackStack();
	}
	
	public static class MoreInfoFragment extends Fragment
	{
		private Handler mHandler;
		private Timer mTimer;

		public MoreInfoFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState)
		{
			View rootView = inflater.inflate(R.layout.fragment_more_info,
					container, false);
			//Handles changing the RSVP time every second with the timer
			mHandler = new Handler() {
				public void handleMessage(Message message)
				{
					Date now = new Date();
					long timeToRSVP = mExpiresAt.getTime() - now.getTime();
					mTimeToRSVP = "" + (int)timeToRSVP/(60*60*1000) + ":";
					int minutes = (int)(timeToRSVP/(60*1000))%60;
					if (minutes < 10)
						mTimeToRSVP = mTimeToRSVP + "0";
					mTimeToRSVP = mTimeToRSVP + minutes + ":";
					int seconds = (int)(timeToRSVP/1000)%60;
					if (seconds < 10)
						mTimeToRSVP = mTimeToRSVP+ "0";
					mTimeToRSVP = mTimeToRSVP + seconds;
					TextView tv = (TextView) getActivity().findViewById(R.id.timeToRSVP);
					tv.setText(mTimeToRSVP);
					tv.invalidate();
					tv.requestLayout();
				}
			};
			
			mTimer = new Timer();
			mTimer.schedule(new RSVPTimerTask(), 0, 1000);
			return rootView;
		}
		
		//make sure all the info is current
		public void onResume()
		{
			super.onResume();
			
			Resources res = getResources();
			
			if (mCreator == null)
			{
				mCreator = String.format(res.getString(R.string.dummy_name));
				mAgenda = String.format(res.getString(R.string.dummy_agenda));
				mVenueInfo = String.format(res.getString(R.string.dummy_location));
			}
			
			TextView tv = (TextView) getActivity().findViewById(R.id.creator);
			System.out.println("RAWR");
			tv.setText(mCreator);
			tv = (TextView) getActivity().findViewById(R.id.agenda);
			tv.setText(mAgenda);
			tv = (TextView) getActivity().findViewById(R.id.venueInfo);
			tv.setText(mVenueInfo);
			if (mHasResponded)
				if (mWillAttend)
					getActivity().findViewById(R.id.yes)
						.setBackgroundColor(getResources().getColor(R.color.green));
				else
					getActivity().findViewById(R.id.no)
						.setBackgroundColor(getResources().getColor(R.color.red));
		}
		
		public void onStop()
		{
			super.onStop();
			mTimer.cancel();
			mTimer.purge();
		}
		
		public class RSVPTimerTask extends TimerTask
		{
			public void run() {
				mHandler.obtainMessage(1).sendToTarget();
			}
		}
	}
	
	//TODO get correct kind of agenda frag to pop up
	public static class AgendaFragment extends Fragment
	{
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState)
		{
			View rootView = inflater.inflate(R.layout.fragment_agenda,
					container, false);
			TextView tv = (TextView) rootView.findViewById(R.id.largeAgenda);
			tv.setText(mAgenda);
			return rootView;
		}
	}

	public static class EditAgendaFragment extends Fragment
	{
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState)
		{
			View rootView = inflater.inflate(R.layout.fragment_edit_agenda,
					container, false);
			EditText et = (EditText) rootView.findViewById(R.id.editAgenda);
			et.setText(mAgenda);
			return rootView;
		}
		
		@Override
		public void onPause()
		{
			super.onPause();
			EditText et = (EditText) getActivity().findViewById(R.id.editAgenda);
			mAgenda = et.getText().toString();
		}
	}
	
	public static class AddCommentFragment extends Fragment
	{
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState)
		{
			View rootView = inflater.inflate(R.layout.fragment_add_comment,
					container, false);
			return rootView;
		}
	}
}
