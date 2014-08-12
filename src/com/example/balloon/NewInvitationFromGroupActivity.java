package com.example.balloon;

import org.json.JSONArray;

import android.os.Bundle;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class NewInvitationFromGroupActivity extends NewInvitationActivity {
	
	protected void defaultInfo(final Bundle savedInstanceState)
	{
		showSpinner();
		context = this;
		mPlus = false;
		mListName = null;
		mListId = null;
		mAgenda = null;
		mVenueInfo = null;
		mVenue = null;
		mToday = true;
		mExpiresAtHour = -1;
		mExpiresAtMinute = 0;
		mVenuePhotoUrls = new JSONArray();
		mPhoneNumbers = null;
		//same for makeContactList, but consistency
		mMakeContactList = false;
		mMemberIds = null;
		mMembers = null;
		mMemberNames = null;
		mPreviewName = null;
		mInviteMore = true;
		mCurrentFragment = "";
		mAfterFinalEdit = false;
		mCheckbox = null;
		mListView = null;
		mContactListImage = null;

		//since we want to do cool new things!
		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("ContactList");
		query.whereEqualTo("objectId", getIntent().getExtras().getString("listId"));
		query.getFirstInBackground(new GetCallback<ParseObject>() {
			@Override
			public void done(ParseObject o, ParseException e) {
				if (e == null)
				{
					mListId = o;
					continueStart(savedInstanceState);
				}
				else
				{
					showParseException(e);
					finish();
				}
			}
		});
	}
	
	public void continueStart(Bundle savedInstanceState)
	{
		if (savedInstanceState == null) {
			mAfterFinalEdit = false;
			getFragmentManager().beginTransaction()
					.add(R.id.container, new SelectMembersFromListFragment()).commit();
		}
	}
}
