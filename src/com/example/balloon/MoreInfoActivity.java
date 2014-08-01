package com.example.balloon;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Resources;
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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

public class MoreInfoActivity extends ProgressActivity
{
	private static String mObjectId;
	private static boolean mIsCreator;
	private static String mCreator;
	private static String mAgenda;
	private static String mVenueInfo;
	private static String mVenuePhotoURL;
	private static Date mExpiresAt;
	private static String mTimeToRSVP;
	private static boolean mHasResponded;
	private static boolean mWillAttend;
	protected static ParseObject mMeetup;
	protected static JSONObject mVenueLocation;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_more_info);
		mObjectId = getIntent().getExtras().getString("objectId");
		mHasResponded = getIntent().getExtras().getBoolean("hasResponded");
		mWillAttend = getIntent().getExtras().getBoolean("willAttend");
		mIsCreator = getIntent().getExtras().getBoolean("isCreator");
		System.out.println(mObjectId);
		
		if (savedInstanceState == null)
		{
			showSpinner();
			
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
							showParseException(e1);
						}
						
						mAgenda = meetup.getString("agenda");
						
						try {
							mVenueInfo = meetup.getJSONObject("venueInfo").getString("name");
						} catch (JSONException e1) {
							// Auto-generated catch block
							e1.printStackTrace();
						}
						
						try {
							mVenueLocation = meetup.getJSONObject("venueInfo")
									.getJSONObject("location");
						} catch (JSONException e1) {
							//it's a custom venue
							mVenueLocation = null;
						}
						
						try {
							if (meetup.containsKey("venuePhotoURLs") && 
									meetup.getJSONArray("venuePhotoURLs").length() > 0)
								mVenuePhotoURL = meetup.getJSONArray("venuePhotoURLs").getString(0);
						} catch (JSONException e1) {
							mVenuePhotoURL = null;
						}
						
						mExpiresAt = meetup.getDate("expiresAt");
						
						
						openInfoFragment();
					}
					else
						showParseException(e);
				}
			});
		}
	}
	
	public void openInfoFragment()
	{
		getFragmentManager().beginTransaction()
				.add(R.id.container, new InfoFragment()).commit();
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
				FragmentTransaction transaction = getFragmentManager().beginTransaction();
				transaction.replace(R.id.container, new EditAgendaFragment());
				transaction.addToBackStack(null);
				transaction.commit();
			}
			else
				showAgenda(null);
			return true;
		}
		else if (id == R.id.action_comment) {
			FragmentTransaction transaction = getFragmentManager().beginTransaction();
			transaction.replace(R.id.container, new AddCommentFragment(), "AddCommentFragment");
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
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.replace(R.id.container, new AgendaFragment());
		transaction.addToBackStack(null);
		transaction.commit();
	}
	
	public void respondNo(View view)
	{
		showSpinner();
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("meetupId", mObjectId);
		params.put("willAttend", false);
		ParseCloud.callFunctionInBackground("respondToMeetup", params, new FunctionCallback<Object>() {
			public void done(Object o, ParseException e) {
				if (e == null)
					fetchComing();
				else
					showParseException(e);
			}
		});
		view.setBackgroundColor(getResources().getColor(R.color.red));
		findViewById(R.id.yes).setBackgroundColor(getResources().getColor(R.color.buttonBlue));
	}
	
	public void respondYes(View view)
	{
		showSpinner();
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("meetupId", mObjectId);
		params.put("willAttend", true);
		ParseCloud.callFunctionInBackground("respondToMeetup", params, new FunctionCallback<Object>() {
			public void done(Object o, ParseException e) {
				if (e == null)
					fetchComing();
				else
					showParseException(e);
			}
		});
		view.setBackgroundColor(getResources().getColor(R.color.green));
		findViewById(R.id.no).setBackgroundColor(getResources().getColor(R.color.buttonBlue));
	}public void fetchComing()
	{
		//this will get who's coming, starting with all the responses
		//TODO Look into using include?? I can't seem to get first names tho
		ParseQuery<ParseObject> comingQuery = new ParseQuery<ParseObject>("Response");
		comingQuery.whereEqualTo("meetup", mMeetup);
		comingQuery.whereEqualTo("isAttending", true);
		comingQuery.include("responder");
		comingQuery.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> responses, ParseException e) {
				if (e == null)
					makeComingList(responses);
				else
					showParseException(e);
			}
		});
	}
	
	public void makeComingList(List<ParseObject> responses)
	{
		System.out.println("makeComingList activated");
	    LinearLayout lin = (LinearLayout) findViewById(R.id.coming);
	    if (lin != null)
	    {
	    	System.out.println(responses.size());
		    lin.removeAllViews();
		    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout
					.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		    for (int i = 0; i < responses.size(); i++)
		    {
				View response = View.inflate(this, R.layout.list_item_coming, null);
				ParseUser user = responses.get(i).getParseUser("responder");
				((TextView) response.findViewById(R.id.responder)).setText(user
						.getString("firstName") + " " + user.getString("lastName"));
				Picasso.with(this).load(user.getParseFile("profilePhoto").getUrl()).resize(100, 100)
					.into((ImageView) response.findViewById(R.id.profilePhoto));
				lin.addView(response, lp);
		    }
	    }
		removeSpinner();
	}

	public void addComment(View view)
	{
		showSpinner();
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
				{
					outputToast("Update added!");
				}
				else
					showParseException(e);
			}
		});
	}
	
	public void changeAgenda(View view)
	{
		showSpinner();
		EditText et = (EditText) findViewById(R.id.editAgenda);
		String agenda = et.getText().toString();
		
		mMeetup.put("agenda", agenda);
		mMeetup.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				removeSpinner();
				if (e == null)
					outputToast("Agenda changed!");
				else
					showParseException(e);
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
				String address = "";
				JSONArray formattedAddress;
				try {
					formattedAddress = mVenueLocation.getJSONArray("formattedAddress");
					for (int i = 0; i < formattedAddress.length(); i++)
						address += " " + formattedAddress.getString(i);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//need to get rid of first space
				String url = ("http://www.maps.google.com/maps?q="+(address).substring(1))
						.replaceAll(" ", "+");
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
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
		
		cancel(null);
	}
	
	public void cancel(View view)
	{
		getFragmentManager().popBackStack();
	}
	
	public static class InfoFragment extends ProgressFragment
	{
		private Handler mHandler;
		private Timer mTimer;
		public PullToRefreshBase<ScrollView> pullToRefreshView;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState)
		{
			View rootView = inflater.inflate(R.layout.fragment_info,
					container, false);
			//Handles changing the RSVP time every second with the timer
			pullToRefreshView = null;
			return rootView;
		}
		
		//make sure all the info is current
		public void onResume()
		{
			super.onResume();
			if (pullToRefreshView == null)
			{
				pullToRefreshView = (PullToRefreshScrollView)
						getActivity().findViewById(R.id.scrollToRefresh);
				pullToRefreshView.setOnRefreshListener(new OnRefreshListener<ScrollView>() {
				    @Override
				    public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
				    	showSpinner();
				        reload();
				        new GetDataTask().execute();
				    }
				});
			}
			fetchMeetup();
		}
		
		public void reload()
		{
			showSpinner();
			
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
							showParseException(e1);
						}
						
						mAgenda = meetup.getString("agenda");
						
						try {
							mVenueInfo = meetup.getJSONObject("venueInfo").getString("name");
						} catch (JSONException e1) {
							// Auto-generated catch block
							e1.printStackTrace();
						}
						
						try {
							mVenueLocation = meetup.getJSONObject("venueInfo")
									.getJSONObject("location");
						} catch (JSONException e1) {
							//it's a custom venue
							mVenueLocation = null;
						}
						
						try {
							if (meetup.containsKey("venuePhotoURLs") && 
									meetup.getJSONArray("venuePhotoURLs").length() > 0)
								mVenuePhotoURL = meetup.getJSONArray("venuePhotoURLs").getString(0);
						} catch (JSONException e1) {
							mVenuePhotoURL = null;
						}
						
						mExpiresAt = meetup.getDate("expiresAt");
						
						
						fetchMeetup();
					}
					else
						showParseException(e);
				}
			});
		}
		public void fetchMeetup()
		{
			Resources res = getResources();
			if (mExpiresAt.getTime() - new Date().getTime() > 0)
			{
				//Handles changing the RSVP time every second with the timer
				mHandler = new Handler() {
					public void handleMessage(Message message)
					{
						Date now = new Date();
						long timeToRSVP = mExpiresAt.getTime() - now.getTime();
						if (timeToRSVP < 0)
						{
							((TextView) getActivity().findViewById(R.id.timeToRSVP)).setText("");
							((TextView) getActivity().findViewById(R.id.leftToRSVP)).setText("");
							mTimer.cancel();
							mTimer.purge();
						}
						else
						{
							mTimeToRSVP = "" + (int)timeToRSVP/(60*60*1000) + ":";
							int minutes = (int)(timeToRSVP/(60*1000))%60;
							if (minutes < 10)
								mTimeToRSVP = mTimeToRSVP + "0";
							mTimeToRSVP = mTimeToRSVP + minutes + ":";
							int seconds = (int)(timeToRSVP/1000)%60;
							if (seconds < 10)
								mTimeToRSVP = mTimeToRSVP+ "0";
							mTimeToRSVP = mTimeToRSVP + seconds;
							if (getActivity() != null && getActivity().findViewById(R.id.timeToRSVP) != null)
							{
								TextView tv = (TextView) getActivity().findViewById(R.id.timeToRSVP);
								tv.setText(mTimeToRSVP);
								tv.invalidate();
								tv.requestLayout();
							}
						}
					}
				};
				mTimer = new Timer();
				mTimer.schedule(new RSVPTimerTask(), 0, 1000);
			}
			else
				((TextView) getActivity().findViewById(R.id.leftToRSVP)).setText("");
			
			Button button = (Button) getActivity().findViewById(R.id.no);
			button.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),
					 "fonts/fontawesome-webfont.ttf"));
			button = (Button) getActivity().findViewById(R.id.yes);
			button.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),
					 "fonts/fontawesome-webfont.ttf"));
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
			if (mVenuePhotoURL != null)
				Picasso.with(getActivity()).load(mVenuePhotoURL).resize(160, 160).centerCrop().into
					((ImageView) getActivity().findViewById(R.id.eventImage));
			else
				Picasso.with(getActivity()).load(R.drawable.logo).resize(160, 160).centerCrop().into
					((ImageView) getActivity().findViewById(R.id.eventImage));
			if (mHasResponded)
				if (mWillAttend)
					getActivity().findViewById(R.id.yes)
						.setBackgroundColor(getResources().getColor(R.color.green));
				else
					getActivity().findViewById(R.id.no)
						.setBackgroundColor(getResources().getColor(R.color.red));

			fetchComments();
		}

		public void fetchComments()
		{
			//this will get all the comments
			ParseQuery<ParseObject> commentQuery = new ParseQuery<ParseObject>("Comment");
			commentQuery.whereEqualTo("meetup", mMeetup);
			commentQuery.include("commenter");
			commentQuery.orderByDescending("createdAt");
			commentQuery.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> comments, ParseException e) {
					if (e == null)
						makeCommentList(comments);
					else
					{
						e.printStackTrace();
						fetchComing();
					}
				}
			});
		}
		
		public void makeCommentList(final List<ParseObject> comments)
		{
			LinearLayout lin = (LinearLayout) getActivity().findViewById(R.id.updates);
			if (lin != null)
			{
				lin.removeAllViews();
				RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout
						.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				for (int i = 0; i < comments.size(); i++)
				{
					View comment = View.inflate(getActivity(), R.layout.list_item_comment, null);
					((TextView) comment.findViewById(R.id.comment)).setText(comments.get(i)
							.getString("comment"));
					ParseUser commenter = comments.get(i).getParseUser("commenter");
					((TextView) comment.findViewById(R.id.commenter)).setText(commenter
							.getString("firstName") + " " + commenter.getString("lastName"));
					Picasso.with(getActivity()).load(commenter.getParseFile("profilePhoto")
							.getUrl()).resize(100, 100).into((ImageView) comment
							.findViewById(R.id.imageView1));
					lin.addView(comment, lp);
				}
				lin.invalidate();
				lin.requestLayout();
			}
			fetchComing();
		}
		
		public void fetchComing()
		{
			//this will get who's coming, starting with all the responses
			//TODO Look into using include?? I can't seem to get first names tho
			ParseQuery<ParseObject> comingQuery = new ParseQuery<ParseObject>("Response");
			comingQuery.whereEqualTo("meetup", mMeetup);
			comingQuery.whereEqualTo("isAttending", true);
			comingQuery.include("responder");
			comingQuery.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> responses, ParseException e) {
					if (e == null)
						makeComingList(responses);
					else
						showParseException(e);
				}
			});
		}
		
		public void makeComingList(List<ParseObject> responses)
		{
			System.out.println("makeComingList activated");
		    LinearLayout lin = (LinearLayout) getActivity().findViewById(R.id.coming);
		    if (lin != null)
		    {
		    	System.out.println(responses.size());
			    lin.removeAllViews();
			    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout
						.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			    for (int i = 0; i < responses.size(); i++)
			    {
					View response = View.inflate(getActivity(), R.layout.list_item_coming, null);
					ParseUser user = responses.get(i).getParseUser("responder");
					((TextView) response.findViewById(R.id.responder)).setText(user
							.getString("firstName") + " " + user.getString("lastName"));
					Picasso.with(getActivity()).load(user.getParseFile("profilePhoto").getUrl()).resize(100, 100)
						.into((ImageView) response.findViewById(R.id.profilePhoto));
					lin.addView(response, lp);
			    }
		    }
		    removeSpinner();
		}
		
		public void onStop()
		{
			if (mTimer != null)
			{
				mTimer.cancel();
				mTimer.purge();
			}
			super.onStop();
		}
		
		public class RSVPTimerTask extends TimerTask
		{
			public void run() {
				mHandler.obtainMessage(1).sendToTarget();
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
