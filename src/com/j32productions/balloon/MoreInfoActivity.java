package com.j32productions.balloon;

import java.util.ArrayList;
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
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
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
	private static String mCreatorNumber;
	private static String mAgenda;
	private static String mVenueInfo;
	private static String mVenuePhotoURL;
	private static Date mExpiresAt;
	private static Date mStartsAt;
	private static int mSpotsLeft;
	private static String mTimeToRSVP;
	private static boolean mHasResponded;
	private static boolean mWillAttend;
	protected static ParseObject mMeetup;
	protected static JSONObject mVenueLocation;
	private static boolean mIsUpdate;
	private static Menu mMenu;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_more_info);
		mObjectId = getIntent().getExtras().getString("objectId");
		mHasResponded = getIntent().getExtras().getBoolean("hasResponded");
		mWillAttend = getIntent().getExtras().getBoolean("willAttend");
		mIsCreator = getIntent().getExtras().getBoolean("isCreator");
		System.out.println(mObjectId);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		if (savedInstanceState == null)
		{
			showSpinner();
			
			ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Meetup");
			query.whereEqualTo("objectId", mObjectId);
			query.include("creator");
			query.getFirstInBackground(new GetCallback<ParseObject>(){
				public void done(ParseObject meetup, ParseException e) {
					if (e == null)
					{
						mMeetup = meetup;
						ParseUser creator = meetup.getParseUser("creator");
						mCreator = creator.getString("firstName") + " " + creator.getString("lastName");
						mCreatorNumber = creator.getUsername();
						if (creator == ParseUser.getCurrentUser())
						{
							mIsCreator = true;
							invalidateOptionsMenu();
						}
						
						mAgenda = meetup.getString("agenda");
						setTitle(mAgenda);
						
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
		mMenu = menu;
	    return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == android.R.id.home)
		{
	        NavUtils.navigateUpFromSameTask(this);
	        return true;
	    }
		else if (id == R.id.action_edit) {
			Intent intent = new Intent(this, EditMeetupActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString("objectId", mObjectId);
			intent.putExtras(bundle);
			startActivity(intent);
		}
		else if (id == R.id.action_agenda) {
			showAgenda(null);
			return true;
		}
		else if (id == R.id.action_invite) {
			Intent intent = new Intent(this, InviteMoreActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString("objectId", mObjectId);
			intent.putExtras(bundle);
			startActivity(intent);
			return true;
		}
		else if (id == R.id.action_view_invitees) {
			FragmentTransaction transaction = getFragmentManager().beginTransaction();
			transaction.replace(R.id.container, new ViewInviteesFragment());
			transaction.addToBackStack(null);
			transaction.commit();
			return true;
		}
		else if (id == R.id.action_update) {
			mIsUpdate = true;
			FragmentTransaction transaction = getFragmentManager().beginTransaction();
			transaction.replace(R.id.container, new AddCommentFragment(), "AddCommentFragment");
			transaction.addToBackStack(null);
			transaction.commit();
			return true;
		}
		else if (id == R.id.action_comment) {
			mIsUpdate = false;
			FragmentTransaction transaction = getFragmentManager().beginTransaction();
			transaction.replace(R.id.container, new AddCommentFragment(), "AddCommentFragment");
			transaction.addToBackStack(null);
			transaction.commit();
			return true;
		}
		else if (id == R.id.action_contact) {
			Intent smsIntent = new Intent(Intent.ACTION_VIEW);
			smsIntent.setType("vnd.android-dir/mms-sms");
			smsIntent.putExtra("address", mCreatorNumber);
			smsIntent.putExtra("sms_body", "");
			startActivity(smsIntent);
			return true;
		}
		else if (id == R.id.action_locate) {
			locate();
			return true;
		}
		else if (id == R.id.action_add) {
			addComment();
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
	
	public void respondNo(final View view)
	{
		showSpinner();
		HashMap<String, Object> params = new HashMap<String, Object>();
		mHasResponded = true;
		mWillAttend = false;
		params.put("meetupId", mObjectId);
		params.put("responder", ParseUser.getCurrentUser().getObjectId());
		params.put("willAttend", false);
		view.setBackgroundColor(getResources().getColor(R.color.red));
		ParseCloud.callFunctionInBackground("respondToMeetup", params, new FunctionCallback<Object>() {
			public void done(Object o, ParseException e) {
				if (e == null)
				{
					fetchComing();
					findViewById(R.id.yes).setBackgroundColor(getResources().getColor(R.color.buttonBlue));
				}
				else
				{
					showParseException(e);
					view.setBackgroundColor(getResources().getColor(R.color.buttonBlue));
				}
			}
		});
	}
	
	public void respondYes(final View view)
	{
		showSpinner();
		HashMap<String, Object> params = new HashMap<String, Object>();
		mHasResponded = true;
		mWillAttend = true;
		params.put("meetupId", mObjectId);
		params.put("responder", ParseUser.getCurrentUser().getObjectId());
		params.put("willAttend", true);
		view.setBackgroundColor(getResources().getColor(R.color.green));
		ParseCloud.callFunctionInBackground("respondToMeetup", params, new FunctionCallback<Object>() {
			public void done(Object o, ParseException e) {
				if (e == null)
				{
					fetchComing();
					findViewById(R.id.no).setBackgroundColor(getResources().getColor(R.color.buttonBlue));
				}
				else
				{
					showParseException(e);
					view.setBackgroundColor(getResources().getColor(R.color.buttonBlue));
				}
			}
		});
	}
	
	public void fetchComing()
	{
		//this will get who's coming, starting with all the responses
		//TODO Look into using include?? I can't seem to get first names tho
		ParseQuery<ParseObject> comingQuery = new ParseQuery<ParseObject>("Response");
		comingQuery.whereEqualTo("meetup", mMeetup);
		if (!mIsCreator)
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
		LinearLayout comingList = (LinearLayout) findViewById(R.id.coming);
		LinearLayout notComingList = (LinearLayout) findViewById(R.id.notComing);
		
	    if (comingList != null)
	    {
	    	comingList.removeAllViews();
	    	notComingList.removeAllViews();
		    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout
					.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		    for (int i = 0; i < responses.size(); i++)
		    {
				View response = View.inflate(this, R.layout.list_item_coming, null);
				ParseUser user = responses.get(i).getParseUser("responder");
				((TextView) response.findViewById(R.id.responder)).setText(user
						.getString("firstName") + " " + user.getString("lastName"));
				((TextView) response.findViewById(R.id.responseRate)).setText("" + 
						(Math.round(user.getDouble("responseRate")*1000)/10.0) + "%");
				if (user.containsKey("profilePhoto"))
					Picasso.with(this).load(user.getParseFile("profilePhoto")
						.getUrl()).resize(100, 100).into((ImageView) response.findViewById
						(R.id.profilePhoto));
				if (responses.get(i).getBoolean("isAttending"))
					comingList.addView(response, lp);
				else
					notComingList.addView(response, lp);
		    }
	    }
		removeSpinner();
	}

	public void addComment()
	{
		showSpinner();
		EditText et = (EditText) findViewById(R.id.comment);
		String comment = et.getText().toString();
		
		ParseObject c = new ParseObject("Comment");
		c.put("comment", comment);
		c.put("commenter", ParseUser.getCurrentUser());
		c.put("meetup", mMeetup);
		c.put("isUpdate", mIsUpdate);
		c.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if (e == null)
				{
					if (mIsUpdate)
						outputToast("Update added!");
					else
						outputToast("Comment added!");
				}
				else
					showParseException(e);
			}
		});

		InputMethodManager imm = (InputMethodManager) getSystemService(
		      Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
	}
	
	public void locate()
	{
		if (mVenueLocation != null)
		{
			System.out.println(mVenueLocation.toString());
			String str = "";
			try {
				str = "geo:0,0?q="+mVenueLocation.getJSONObject("location").getDouble("lat")
						+","+mVenueLocation.getJSONObject("location").getDouble("lng")
						+" (" + mVenueLocation.getString("name") + ")";
				//need to deal with spaces
				System.out.println("This is: " + str);
				Uri uri = Uri.parse(str);
				Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
				if (intent.resolveActivity(getPackageManager()) != null)
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
						formattedAddress = mVenueLocation.getJSONObject("location").getJSONArray("formattedAddress");
						for (int i = 0; i < formattedAddress.length(); i++)
							address += " " + formattedAddress.getString(i);
						//need to get rid of first space
						address = address.substring(1);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String url = ("http://www.maps.google.com/maps?q="+address)
							.replaceAll(" ", "+");
					uri = Uri.parse(url);
					intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
					if (intent.resolveActivity(getPackageManager()) != null)
						startActivity(intent);
					else
						outputToast("Oops! No map application found.");
				}
			} catch (JSONException e1) {
				Toast.makeText(this, "Oops! No location found",
						Toast.LENGTH_SHORT).show();
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
		if (view != null)
		{
			InputMethodManager imm = (InputMethodManager) getSystemService(
			      Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}
	
	public static class InfoFragment extends ProgressFragment
	{
		private static Handler mHandler;
		private Timer mTimer;
		public PullToRefreshBase<ScrollView> pullToRefreshView;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState)
		{
			View rootView = inflater.inflate(R.layout.fragment_info,
					container, false);
			//Handles changing the RSVP time every second with the timer
			if (mIsCreator)
				rootView.findViewById(R.id.notComingHeader).setVisibility(View.VISIBLE);
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
				        reload();
				        new GetDataTask().execute();
				    }
				});
			}
			
			if (mIsCreator)
			{
				mMenu.findItem(R.id.action_edit).setVisible(true);
				mMenu.findItem(R.id.action_update).setVisible(true);
				mMenu.findItem(R.id.action_contact).setVisible(false);
				mMenu.findItem(R.id.action_invite).setVisible(true);
				mMenu.findItem(R.id.action_view_invitees).setVisible(true);
			}
			else
			{
				mMenu.findItem(R.id.action_edit).setVisible(false);
				mMenu.findItem(R.id.action_update).setVisible(false);
				mMenu.findItem(R.id.action_contact).setVisible(true);
				mMenu.findItem(R.id.action_invite).setVisible(false);
				mMenu.findItem(R.id.action_view_invitees).setVisible(false);
			}
			mMenu.findItem(R.id.action_comment).setVisible(true);
			mMenu.findItem(R.id.action_locate).setVisible(true);
			mMenu.findItem(R.id.action_add).setVisible(false);
			reload();
		}
		
		public void reload()
		{
			showSpinner();
			
			ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Meetup");
			query.whereEqualTo("objectId", mObjectId);
			query.include("creator");
			query.getFirstInBackground(new GetCallback<ParseObject>(){
				public void done(ParseObject meetup, ParseException e) {
					if (e == null)
					{
						mMeetup = meetup;
						ParseUser creator = meetup.getParseUser("creator");
						mCreator = creator.getString("firstName") + " " + creator.getString("lastName");
						if (creator == ParseUser.getCurrentUser())
						{
							mIsCreator = true;
							mMenu.findItem(R.id.action_edit).setVisible(true);
							mMenu.findItem(R.id.action_update).setVisible(true);
							mMenu.findItem(R.id.action_contact).setVisible(false);
							mMenu.findItem(R.id.action_invite).setVisible(true);
							mMenu.findItem(R.id.action_view_invitees).setVisible(true);
						}
						
						mAgenda = meetup.getString("agenda");
						
						try {
							mVenueInfo = meetup.getJSONObject("venueInfo").getString("name");
						} catch (JSONException e1) {
							// Auto-generated catch block
							e1.printStackTrace();
						}
						
						mVenueLocation = meetup.getJSONObject("venueInfo");
						
						try {
							if (meetup.containsKey("venuePhotoURLs") && 
									meetup.getJSONArray("venuePhotoURLs").length() > 0)
								mVenuePhotoURL = meetup.getJSONArray("venuePhotoURLs").getString(0);
							else
								mVenuePhotoURL = null;
						} catch (JSONException e1) {
							mVenuePhotoURL = null;
						}
						
						mExpiresAt = meetup.getDate("expiresAt");
						mStartsAt = meetup.getDate("startsAt");
						TextView tv = (TextView) getActivity().findViewById(R.id.startsAt);
						if (mStartsAt != null && tv != null)
						{
							Time t = new Time();
							t.set(mStartsAt.getTime());
							tv.setText("Starts at " + t.format("%a, %b %e %I:%M %p"));
						}
						else if (tv != null)
							tv.setText(R.string.no_start_time);
						mSpotsLeft = -1;
						if (meetup.has("spotsLeft"))
							mSpotsLeft = meetup.getInt("spotsLeft");
						if (meetup.getBoolean("allowInviteMore") && !mIsCreator &&
								mExpiresAt.getTime() - new Date().getTime() > 0)
							mMenu.findItem(R.id.action_invite).setVisible(true);
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
			//Handles changing the RSVP time every second with the timer
			mHandler = new Handler() {
				
				final String LEFT_TO_RSVP = getString(R.string.leftToRSVP);
				final String TO_CHANGE_RESPONSE = getString(R.string.to_change_response);
				final String STARTS_IN = getString(R.string.starts_in);
				final ColorStateList BLACK = ((TextView) getActivity()
						.findViewById(R.id.creator)).getTextColors();
				final int RED = getResources().getColor(R.color.red);
				
				public void handleMessage(Message message)
				{
					Date now = new Date();
					long timeToRSVP = mExpiresAt.getTime() - now.getTime();
					if (timeToRSVP < 0)
					{
						if (mStartsAt != null)
						{
							timeToRSVP = mStartsAt.getTime() - now.getTime();
							if (timeToRSVP < 0)
							{
								((TextView) getActivity().findViewById(R.id.timeToRSVP)).setText(
										getString(R.string.started));
								mTimer.cancel();
								mTimer.purge();
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
								if (getActivity() != null && getActivity().findViewById(R.id.timeToRSVP) != null)
								{
									TextView tv = (TextView) getActivity().findViewById(R.id.timeToRSVP);
									tv.setTextColor(BLACK);
									tv.setText(STARTS_IN + " " + time);
									tv.invalidate();
									tv.requestLayout();
								}
							}
						}
						else
						{
							if (getActivity() != null && getActivity().findViewById(R.id.timeToRSVP) != null)
								((TextView) getActivity().findViewById(R.id.timeToRSVP)).setText(
										getString(R.string.no_start_time));
							mTimer.cancel();
							mTimer.purge();
						}
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
						mTimeToRSVP = mTimeToRSVP + seconds + " ";
						mTimeToRSVP += mHasResponded ? TO_CHANGE_RESPONSE : LEFT_TO_RSVP;
						if (getActivity() != null && getActivity().findViewById(R.id.timeToRSVP) != null)
						{
							TextView tv = (TextView) getActivity().findViewById(R.id.timeToRSVP);
							tv.setText(mTimeToRSVP);
							tv.setTextColor(RED);
							tv.invalidate();
							tv.requestLayout();
						}
					}
				}
			};
			mTimer = new Timer();
			mTimer.schedule(new RSVPTimerTask(), 0, 1000);

			if (mSpotsLeft > -1)
			{
				TextView tv = (TextView) getActivity().findViewById(R.id.spotsLeft);
				tv.setText(getResources().getQuantityString(R.plurals.spotsLeft,
						mSpotsLeft, mSpotsLeft));
				tv.setVisibility(View.VISIBLE);
			}
			
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
			commentQuery.orderByAscending("createdAt");
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
			LinearLayout commentList = (LinearLayout) getActivity().findViewById(R.id.comments);
			if (commentList != null)
			{
				commentList.removeAllViews();
				RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout
						.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				long now = (new Date()).getTime();
				for (int i = 0; i < comments.size(); i++)
				{
					View comment = View.inflate(getActivity(), R.layout.list_item_comment, null);
					((TextView) comment.findViewById(R.id.comment)).setText(comments.get(i)
							.getString("comment"));
					ParseUser commenter = comments.get(i).getParseUser("commenter");
					String timestamp;
					long diff = now - comments.get(i).getCreatedAt().getTime();
					int quantity;
					if (diff > 24*60*60*1000)
					{
						quantity = (int) (diff/24/60/60/1000);
						timestamp = getResources().getQuantityString(R.plurals.daysAgo,
								quantity, quantity);
					}
					else if (diff > 60*60*1000)
					{
						quantity = (int) (diff/60/60/1000);
						timestamp = getResources().getQuantityString(R.plurals.hoursAgo,
								quantity, quantity);
					}
					else if (diff > 60*1000)
					{
						quantity = (int) (diff/60/1000);
						timestamp = getResources().getQuantityString(R.plurals.minutesAgo,
								quantity, quantity);
					}
					else
						timestamp = getString(R.string.less_than_a_minute);
					((TextView) comment.findViewById(R.id.commenter)).setText(commenter
							.getString("firstName") + " " + commenter.getString("lastName")
							+ " " + timestamp);
					Picasso.with(getActivity()).load(commenter.getParseFile("profilePhoto")
							.getUrl()).resize(100, 100).into((ImageView) comment
							.findViewById(R.id.imageView1));
					commentList.addView(comment, lp);
				}
				commentList.invalidate();
				commentList.requestLayout();
			}
			fetchComing();
		}
		
		public void fetchComing()
		{
			//this will get who's coming, starting with all the responses
			//TODO Look into using include?? I can't seem to get first names tho
			ParseQuery<ParseObject> comingQuery = new ParseQuery<ParseObject>("Response");
			comingQuery.whereEqualTo("meetup", mMeetup);
			if (!mIsCreator)
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
		    LinearLayout comingList = null;
		    LinearLayout notComingList = null;
			if (getActivity() != null)
			{
				comingList = (LinearLayout) getActivity().findViewById(R.id.coming);
				notComingList = (LinearLayout) getActivity().findViewById(R.id.notComing);
			}
		    if (comingList != null)
		    {
		    	System.out.println(responses.size());
		    	comingList.removeAllViews();
		    	notComingList.removeAllViews();
			    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout
						.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			    for (int i = 0; i < responses.size(); i++)
			    {
					View response = View.inflate(getActivity(), R.layout.list_item_coming, null);
					ParseUser user = responses.get(i).getParseUser("responder");
					((TextView) response.findViewById(R.id.responder)).setText(user
							.getString("firstName") + " " + user.getString("lastName"));
					((TextView) response.findViewById(R.id.responseRate)).setText("" + 
							(Math.round(user.getDouble("responseRate")*1000)/10.0) + "%");
					if (user.containsKey("profilePhoto"))
						Picasso.with(getActivity()).load(user.getParseFile("profilePhoto")
							.getUrl()).resize(100, 100).into((ImageView) response.findViewById
							(R.id.profilePhoto));
					if (responses.get(i).getBoolean("isAttending"))
						comingList.addView(response, lp);
					else
						notComingList.addView(response, lp);
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
	
	public static class AddCommentFragment extends Fragment
	{
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState)
		{
			View rootView = inflater.inflate(R.layout.fragment_add_comment,
					container, false);
			for (int i = 0; i < mMenu.size(); i++)
				mMenu.getItem(i).setVisible(false);
			mMenu.findItem(R.id.action_add).setVisible(true);
			mMenu.findItem(R.id.action_add).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			if (mIsUpdate)
				((EditText) rootView.findViewById(R.id.comment)).setHint(getActivity()
						.getString(R.string.updates_hint));
			return rootView;
		}
		
		public void onStop()
		{
			EditText et = (EditText)(getActivity().findViewById(R.id.comment));
			InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
			      Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
			super.onStop();
		}
	}
	
	public static class ViewInviteesFragment extends ProgressFragment
	{
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState)
		{
			showSpinner();
			final View rootView = inflater.inflate(R.layout.fragment_select_list,
					container, false);
			rootView.setBackgroundColor(getResources().getColor(R.color.white));
			ArrayList<ParseQuery<ParseUser>> queries = new ArrayList<ParseQuery<ParseUser>>();
			JSONArray members = mMeetup.getJSONArray("invitedUsers");
			for (int i = 0; i < members.length(); i++)
			{
				try {
					ParseQuery<ParseUser> query = ParseUser.getQuery();
					query.whereEqualTo("objectId", members.getJSONObject(i).getString("objectId"));
					queries.add(query);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			ParseQuery<ParseUser> mainQuery = ParseQuery.or(queries);
			mainQuery.orderByAscending("firstName");
			mainQuery.addAscendingOrder("lastName");
			mainQuery.findInBackground(new FindCallback<ParseUser>() {
				@Override
				public void done(List<ParseUser> list, ParseException e) {
					if (e == null)
					{
						String[] names = new String[list.size()];
						String[] responseRates = new String[list.size()];
						String[] photoURLs = new String[list.size()];
						//we want a JSONArray of users, not just of userIds
						for (int i = 0; i < list.size(); i++)
						{
							names[i] = list.get(i).getString("firstName") + " " + 
									list.get(i).getString("lastName");
							if (list.get(i).containsKey("profilePhoto"))
								photoURLs[i] = list.get(i).getParseFile("profilePhoto").getUrl();
							if (list.get(i).containsKey("responseRate"))
								responseRates[i] = "" + (Math.round(list.get(i)
										.getDouble("responseRate")*1000))/10.0+"%";
							else
								responseRates[i] = "100%";
						}
						ListView lv = (ListView) rootView.findViewById(R.id.groupList);
						lv.setAdapter(new GroupAdapter(getActivity(), R.layout.list_members,
								names, responseRates, photoURLs));
					    lv.setItemsCanFocus(false);
					    lv.setChoiceMode(ListView.CHOICE_MODE_NONE);
					    removeSpinner();
					}
					else
						showParseException(e);
				}
			});
			return rootView;
		}
	}
}
