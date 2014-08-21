package com.example.balloon;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class EditMeetupActivity extends NewInvitationActivity {

	private ParseObject meetup;
	
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
	
	private void setValues(ParseObject meetup, Bundle savedInstanceState)
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
		
		//do i need these fields??
		mPhoneNumbers = null;
		mMakeContactList = false;
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
		}
		else
		{
			mLimit = 0;
			mSpotsLeft = 0;
			mIsFull = false;
		}
		
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
	//This actually just ends the activity
	//Sorry again
	public void sendInvite(final ParseObject meetup)
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
		}
	}
}
