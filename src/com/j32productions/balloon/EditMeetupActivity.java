package com.j32productions.balloon;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

//TODO fix rsvp deadline for editting after the rsvp deadline
public class EditMeetupActivity extends NewInvitationActivity {

	protected ParseObject meetup;
	protected JSONObject mOldVenue;
	protected Date mOldStartTime;
	
	//erase all static fields
	@Override
	protected void defaultInfo(final Bundle savedInstanceState)
	{
		mCurrentFragment = "FinalEditFragment";
		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Meetup");
		query.whereEqualTo("objectId", getIntent().getExtras().getString("objectId"));
		query.include("creator");
		query.include("contactList");
		query.getFirstInBackground(new GetCallback<ParseObject>() {
			@Override
			public void done(ParseObject meetup, ParseException e) {
				if (e == null)
					setValues(meetup, savedInstanceState);
				else
					showParseException(e);
			}
		});
	}
	
	protected void setValues(ParseObject meetup, Bundle savedInstanceState)
	{
		this.meetup = meetup;
		context = this;
		if (meetup.getParseObject("contactList") != null)
		{
			mListId = meetup.getParseObject("contactList");
			mListName = meetup.getParseObject("contactList").getString("name");
		}
		mAgenda = meetup.getString("agenda");
		try {
			mVenueInfo = meetup.getJSONObject("venueInfo").getString("name");
		} catch (JSONException e) {
			mVenueInfo = null;
			e.printStackTrace();
		}
		mVenue = meetup.getJSONObject("venueInfo");
		mOldVenue = mVenue;
		
		//oh my god i don't want to deal with this
		Date deadline = meetup.getDate("expiresAt");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(deadline);
		mExpiresAtHour = (int) calendar.get(Calendar.HOUR_OF_DAY);
		mExpiresAtMinute = (int) calendar.get(Calendar.MINUTE);

		//because what the fuck am i doing
		GregorianCalendar c = new GregorianCalendar();
		c.set(GregorianCalendar.HOUR_OF_DAY, mExpiresAtHour);
		c.set(GregorianCalendar.MINUTE, mExpiresAtMinute);
		Date date = c.getTime();
		//if we're doing tomorrow, we need to incrememnt the dat
		if (date.compareTo(new Date()) > 0)
			mToday = true;
		
		mVenuePhotoUrls = meetup.getJSONArray("venuePhotoURLs");
		if (mVenuePhotoUrls == null)
			mVenuePhotoUrls = new JSONArray();

		mStartDeadline = null;
		if (meetup.has("startsAt"))
			mStartDeadline = meetup.getDate("startsAt");
		mOldStartTime = mStartDeadline;
		
		//do i need these fields??
		mPhoneNumbers = null;
		mMakeContactList = false;
		mFinishSavingMeetup = false;
		mMemberIds = null;
		mMemberFirstNames = null;
		mMemberLastNames = null;
		
		mMembers = meetup.getJSONArray("invitedUsers");
		//TODO mPreviewName = mMembers.get(0);
		mInviteMore = meetup.getBoolean("allowInviteMore");
		mAfterFinalEdit = false;
		mCheckbox = null;
		mListView = null;
		mPlus = false;
		mContactListImage = null;
		if (meetup.has("maxAttendees"))
		{
			mLimit = meetup.getInt("maxAttendees");
			mSpotsLeft = meetup.getInt("spotsLeft");
			mIsFull = meetup.getBoolean("isFull");
			System.out.println("Spots left " + mSpotsLeft);
		}
		else
		{
			mLimit = 0;
			mSpotsLeft = 0;
			mIsFull = false;
		}
		
		loadView(savedInstanceState);
	}
	
	public void loadView(Bundle savedInstanceState)
	{
		if (savedInstanceState == null) {
			mAfterFinalEdit = false;
			getFragmentManager().beginTransaction()
					.add(R.id.container, new EditFragment()).commit();
		}
	}
	
	//don't make a new meetup
	@Override
	public void makeMeetup(final View view)
	{
		saveMeetup(meetup);
	}
	
	@Override
	//Sorry for the messy code
	//Since we are just editing the meetup, we don't need to resend the invites. My code is just sloppy
	//This actually just ends the activity and sends relevant pushes
	//Sorry again
	public void sendInvite(final ParseObject meetup)
	{
		if (!mVenue.equals(mOldVenue))
		{
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("meetupId", meetup.getObjectId());
			ParseCloud.callFunctionInBackground("changeOfVenue", params, new FunctionCallback<Object> () {
				@Override
				public void done(Object arg0, ParseException arg1) {
					mOldVenue = mVenue;
					sendInvite(meetup);
				}
			});
		}
		else if (mStartDeadline != null && !mStartDeadline.equals(mOldStartTime))
		{
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("meetupId", meetup.getObjectId());
			ParseCloud.callFunctionInBackground("changeOfStartTime", params, new FunctionCallback<Object> () {
				@Override
				public void done(Object arg0, ParseException arg1) {
					end();
				}
			});
		}
		else
			end();
	}
	
	public void end()
	{

		Context context = getApplicationContext();
		CharSequence text = "Saved!";
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
		finish();
	}
	
	public static class EditFragment extends FinalEditFragment
	{
		public void onResume()
		{
			super.onResume();
			//gotta change the send button to a save button
			Button save = (Button) getActivity().findViewById(R.id.send);
			save.setText(R.string.save);
			mNext = "Save";
			getActivity().invalidateOptionsMenu();

			int gray = getResources().getColor(R.color.lightGray);
			//things that just shouldn't be changed
			TextView tv = (TextView) getActivity().findViewById(R.id.finalEditToHeader);
			tv.setTextColor(gray);
			tv = (TextView) getActivity().findViewById(R.id.finalEditToText);
			tv.setTextColor(gray);
			tv = (TextView) getActivity().findViewById(R.id.finalEditAgendaHeader);
			tv.setTextColor(gray);
			tv = (TextView) getActivity().findViewById(R.id.finalEditAgendaText);
			tv.setTextColor(gray);
			tv = (TextView) getActivity().findViewById(R.id.finalEditLimitHeader);
			tv.setTextColor(gray);
			tv = (TextView) getActivity().findViewById(R.id.finalEditLimitText);
			tv.setTextColor(gray);
			
			getActivity().findViewById(R.id.finalEditTo).setOnClickListener(null);
			getActivity().findViewById(R.id.finalEditAgenda).setOnClickListener(null);
			getActivity().findViewById(R.id.finalEditLimit).setOnClickListener(null);
			
			if ((new Date()).after(changeToDate(mExpiresAtHour, mExpiresAtMinute)))
				((TextView) getActivity().findViewById(R.id.finalEditDeadlineText)).setText("Expired");;
		}
	}
}
