package com.j32productions.balloon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Checkable;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.j32productions.balloon.NewInvitationActivity.SelectMembersFromListFragment;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class InviteMoreActivity extends EditMeetupActivity {
	
	//we shouldn't be able to see who's invited, but we don't want to invite people twice
	private static JSONArray mAlreadyInvited;
	private static ArrayList<String> mAlreadyInvitedNumbers;
	private JSONArray mSelectedMembers;
	private JSONArray onlyTo;
	private boolean isCreator;
	private boolean sendInvites;
	
	protected void setValues(ParseObject meetup, Bundle savedInstanceState)
	{
		super.setValues(meetup, savedInstanceState);
		mAlreadyInvited = mMembers;
		mAlreadyInvitedNumbers = new ArrayList<String>();
		mMembers = new JSONArray();
		isCreator = getIntent().getBooleanExtra("isCreator", false);
		if (isCreator)
		{
			ArrayList<ParseQuery<ParseUser>> queries = new ArrayList<ParseQuery<ParseUser>>();
			for (int i = 0; i < mAlreadyInvited.length(); i++)
			{
				ParseQuery<ParseUser> query = ParseUser.getQuery();
				try {
					query.whereEqualTo("objectId", mAlreadyInvited.getJSONObject(i).getString("objectId"));
					queries.add(query);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			if (queries.size() > 0)
			{
				ParseQuery<ParseUser> mainQuery = ParseQuery.or(queries);
				mainQuery.findInBackground(new FindCallback<ParseUser>() {
					@Override
					public void done(List<ParseUser> list, ParseException e) {
						if (list != null)
							for (ParseUser user : list)
								mAlreadyInvitedNumbers.add(user.getUsername());
					}
				});
			}
		}
	}
	
	public void loadView(Bundle savedInstanceState)
	{
		if (savedInstanceState == null) {
			mAfterFinalEdit = false;
			getFragmentManager().beginTransaction()
					.add(R.id.container, new SelectListFragment()).commit();
		}
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_plus)
		{
			mMakeContactList = true;
			mIsShellGroup = false;
			FragmentTransaction transaction = getFragmentManager().beginTransaction();
			transaction.replace(R.id.container, new SelectMembersFromContactsFragment());
			transaction.addToBackStack(null);
			transaction.commit();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void onMemberListSelected() {
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.replace(R.id.container, new SelectMembersFromListFragment());
		transaction.addToBackStack(null);
		transaction.commit();
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
			if (SelectMembersFromContactsFragment.getCheckedCount() != 0)
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
		
		for (int i = 0; i < mMembers.length(); i++)
		{
			try {
				
				String objectId = mMembers.get(i) instanceof ParseUser ?
						((ParseUser) mMembers.get(i)).getObjectId() :
						mMembers.getJSONObject(i).getString("objectId");
				System.out.println("To invite: " + objectId);
				//if we already invited this person, we don't need to put him 
				//back into members again - kinda works dumbly but whatevs
				if (mAlreadyInvitedIds.contains(objectId))
					mAlreadyInvitedIds.remove(objectId);
				//if not, we want to add this person to the onlyTo array
				else
				{
					sendInvites = true;
					onlyTo.put(mMembers.get(i));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		//get rid of the creator first
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
						//Toast.makeText(context, "Sent!", Toast.LENGTH_SHORT).show();
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
	
	@Override
	public void end()
	{
		finish();
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
		
		public void finishResume()
		{
			ArrayList<String> mAlreadyInvitedIds = new ArrayList<String>();
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
			for (int i = 0; i < ids.size(); i++)
			{
				if (mAlreadyInvitedIds.contains(ids.get(i)))
				{
					names.remove(i);
					ids.remove(i);
					photoURLs.remove(i);
					responseRates.remove(i);
					i--;
				}
			}
			if (ids.size() == 0)
			{
				Toast.makeText(getActivity(), "Everyone from this group is already invited!",
						Toast.LENGTH_SHORT).show();
				getActivity().getFragmentManager().popBackStack();
			}
			super.finishResume();
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
			System.out.println("resumed");
			LinearLayout ll = (LinearLayout) getActivity().findViewById(R.id.linearLayout);
			for (int i = 0; i < ll.getChildCount(); i++)
			{
				for (int j = 0; j < mAlreadyInvitedNumbers.size(); j++)
				{
					View child = ll.getChildAt(i);
					if (mAlreadyInvitedNumbers.get(j).endsWith(((TextView) child.findViewById(R.id.number))
							.getText().toString().replaceAll("[^[0-9]]", "")))
					{
						Checkable checkbox = (Checkable) child.findViewById(R.id.itemCheckBox);
						if (!checkbox.isChecked())
						{
							((Checkable) child.findViewById(R.id.itemCheckBox)).setChecked(true);
							numChecked++;
						}
						child.setOnClickListener(null);
						break;
					}
				}
			}
		}
	}
}
