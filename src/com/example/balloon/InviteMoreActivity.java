package com.example.balloon;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

public class InviteMoreActivity extends EditMeetupActivity {
	
	//we shouldn't be able to see who's invited, but we don't want to invite people twice
	private JSONArray mAlreadyInvited;
	private JSONArray mSelectedMembers;
	private JSONArray onlyTo;
	private boolean sendInvites;
	
	protected void setValues(ParseObject meetup, Bundle savedInstanceState)
	{
		super.setValues(meetup, savedInstanceState);
		mAlreadyInvited = mMembers;
		mMembers = new JSONArray();
	}
	
	public void loadView(Bundle savedInstanceState)
	{
		if (savedInstanceState == null) {
			mAfterFinalEdit = false;
			getFragmentManager().beginTransaction()
					.add(R.id.container, new SelectListFragment()).commit();
		}
	}
	
	public void next()
	{
		//we want to invite the rest of the users 
		if (mCurrentFragment == "SelectMembersFromListFragment")
		{
			if (((ListView) findViewById(R.id.memberList)).getCheckedItemCount() != 0)
			{
				SelectMembersFromListFragment.saveMembers();
				mMakeContactList = false;
				removeAlreadyInvited();
			}
			else
				Toast.makeText(this, "No friends?", Toast.LENGTH_SHORT).show();
			return;
		}
		else if (mCurrentFragment == "SelectMembersFromContactsFragment")
		{
			if (((ListView) findViewById(R.id.contactsList)).getCheckedItemCount() != 0)
			{
				SelectMembersFromContactsFragment.saveContacts();
				mMakeContactList = true;
				HashMap<String, Object> params = new HashMap<String, Object>();
				JSONArray contacts = new JSONArray();
				for (int i = 0; i < mMemberFirstNames.length; i++)
				{
					JSONObject contact = new JSONObject();
					try {
						contact.put("firstName", mMemberFirstNames[i]);
						contact.put("lastName", mMemberLastNames[i]);
						contact.put("mobileNumber", mPhoneNumbers[i]);
						System.out.println(mMemberFirstNames[i] +" " + mPhoneNumbers[i]);
					} catch (JSONException e1) {
						e1.printStackTrace();
					}
					contacts.put(contact);
				}
				params.put("contacts", contacts);
				ParseCloud.callFunctionInBackground("findOrCreateUsers", params, new
						FunctionCallback<ArrayList<ParseUser>>() {
	
					@Override
					public void done(ArrayList<ParseUser> list, ParseException e) {
						if (e == null)
						{
							// need to give all the members to makeMeetup and to create a new
							//contact list two different json arrays because send invites need
							//json objects, not parse objects
							mParseUsers = list;
							mMembers = new JSONArray();
							for (int i = 0; i < list.size(); i++)
							{
								//mContacts.put(list.get(i));
								try {
									JSONObject person = new JSONObject();
									person.put("__type", "Pointer");
									person.put("className", "_User");
									person.put("objectId", list.get(i).getObjectId());
									mMembers.put(person);
								} catch (JSONException e1) {
									e1.printStackTrace();
								}
							}
							mFinishSavingMeetup = true;
							removeAlreadyInvited();
						}
						else
							showParseException(e);
					}
				});
			}
			else
				Toast.makeText(this, "No friends?", Toast.LENGTH_SHORT).show();
			return;
		}
			
		super.next();
	}
	
	//oh my god so much of this code doesn't work and i don't even know why
	public void removeAlreadyInvited()
	{
		onlyTo = new JSONArray();
		mAlreadyInvited = meetup.getJSONArray("invitedUsers");
		sendInvites = false;
		mSelectedMembers = mMembers;
		ArrayList<String> mAlreadyInvitedIds = new ArrayList<String>();
		mAlreadyInvitedIds.add(meetup.getParseUser("creator").getObjectId());
		System.out.println(mAlreadyInvited);
		for (int i = 0; i < mAlreadyInvited.length(); i++)
		{
			try {
				mAlreadyInvitedIds.add(mAlreadyInvited.getJSONObject(i).getString("objectId"));
				System.out.println("Already invited: "+mAlreadyInvitedIds.get(mAlreadyInvitedIds.size() - 1));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("Member ids = " + mMemberIds.length);
		for (int i = 0; i < mMembers.length(); i++)
		{
			try {
				String objectId = ((ParseUser) mMembers.get(i)).getObjectId();
				System.out.println("To invite: " + objectId);
				//if we already invited this person, we don't need to put him 
				//back into members again - kinda works dumbly but whatevs
				if (mAlreadyInvitedIds.contains(objectId))
					mAlreadyInvitedIds.remove(i);
				//if not, we want to add this person to the onlyTo array
				else
				{
					sendInvites = true;
					onlyTo.put(mMembers.getJSONObject(i));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		//get rid of the creator first
		mAlreadyInvitedIds.remove(0);
		for (int i = 0; i < mAlreadyInvitedIds.size(); i++)
		{
			JSONObject member = new JSONObject();
			try {
				member.put("__type", "Pointer");
				member.put("className", "_User");
				member.put("objectId", mAlreadyInvitedIds.get(i));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mMembers.put(member);
		}
		
		saveMeetup(meetup);
	}
	
	public void sendInvite(final ParseObject meetup)
	{
		if (sendInvites)
		{
			System.out.println("Send invite id = " + meetup.getObjectId());
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("meetupId", meetup.getObjectId());
			params.put("creator", meetup.getParseUser("creator").getObjectId());
			try {
				params.put("invitedUsers", new JSONArray(meetup.getJSONArray("invitedUsers").toString()));
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			params.put("onlyTo", onlyTo);
			ParseCloud.callFunctionInBackground("sendInvites", params, new FunctionCallback<Object>(){
				@Override
				public void done(Object arg0, ParseException e) {
					if (e != null)
						showParseException(e);
					else
					{
						Toast.makeText(context, "Sent!", Toast.LENGTH_SHORT).show();
						if (mMakeContactList)
						{
							//so we don't make a group with everyone actually invited
							mMembers = mSelectedMembers;
							DialogFragment dialog = new AskToSaveListFragment();
					        dialog.show(getFragmentManager(), "NoticeDialogFragment");
						}
						else
							end();
					}
				}
			});
		}
		else
			end();
	}
	
	public static class SelectMembersFromListFragment extends
		NewInvitationActivity.SelectMembersFromListFragment
	{
		public void onResume()
		{
			super.onResume();
			mNext = getActivity().getString(R.string.invite);
			getActivity().invalidateOptionsMenu();
		}
	}
	
	public static class SelectMembersFromContactsFragment extends
		NewInvitationActivity.SelectMembersFromContactsFragment
	{
		public void onResume()
		{
			super.onResume();
			mNext = getActivity().getString(R.string.invite);
			getActivity().invalidateOptionsMenu();
		}
	}
}
