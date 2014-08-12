package com.example.balloon;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class ContactListsActivity extends Activity implements OnMemberListSelectedListener{
	
	private static String mCurrentFragment;
	private static String mListName;
	private static String mListId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_lists);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new ContactListsFragment()).commit();
		}
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
		if (id == R.id.action_plus) {
			newContactList();
			return true;
		}
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
		if (id == R.id.action_rsvp)
		{
			Intent intent = new Intent(this, RSVPEventsActivity.class);
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

	@Override
	public void onMemberListSelected() {
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.replace(R.id.container, new ShowListFragment());
		transaction.addToBackStack(null);
		transaction.commit();
	}
	
	public void newContactList()
	{
		Intent intent = new Intent(this, NewContactListActivity.class);
		startActivity(intent);
	}

	public static class ContactListsFragment extends NewInvitationActivity.SelectListFragment {

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_contact_lists,
					container, false);
			return rootView;
		}
		
		public void onResume()
		{
			super.onResume();
			getActivity().setTitle(getResources().getString(R.string.title_contact_list));
			mCurrentFragment = "SelectListFragment";
		}
		
		public void addListsToView()
		{
			GroupAdapter adapter = new GroupAdapter(getActivity(), R.layout.list_group, lists, photoURLs);
	        ListView lv = (ListView) getActivity().findViewById(R.id.groupList);
	        lv.setAdapter(adapter);
	        lv.setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> a, View v, int position, long id) {
					Intent intent = new Intent(getActivity(), ContactListInfoActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("listName", lists[position]);
					bundle.putString("listId", ids[position].getObjectId());
					intent.putExtras(bundle);
					startActivity(intent);
					/*
					mListName = lists[position];
					mListId = ids[position].getObjectId();
					mListener.onMemberListSelected();
					*/
				}
	        });
		}
	}
	
	public static class ShowListFragment extends ProgressFragment {
		protected String[] names;
		protected ArrayList<String> ids;
		protected String[] photoURLs;

		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_show_list,
					container, false);
			return rootView;
		}
		
		public void onResume()
		{
			super.onResume();
			getActivity().setTitle(mListName);
			mCurrentFragment = "SelectMembersFromListFragment";
			
			showSpinner();
			ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("ContactList");
		    query.whereEqualTo("objectId", mListId);
		    query.include("owner");
		    query.getFirstInBackground(new GetCallback<ParseObject>() {
				public void done(ParseObject contactList, ParseException e) {
					if (e == null)
					{
						JSONArray members = contactList.getJSONArray("members");
						// don't forget to include the owner
						int length = members.length();
						names = new String[length + 1];
						ids = new ArrayList<String>();
						photoURLs = new String[length + 1];
						//first add the owner
						names[0] = contactList.getParseUser("owner").getString("firstName") + 
								" " + contactList.getParseUser("owner").getString("lastName");
						if (contactList.getParseUser("owner").containsKey("profilePhoto"))
							photoURLs[0] = contactList.getParseUser("owner")
									.getParseFile("profilePhoto").getUrl();
						//put all member names and ids into respective arrays
						for (int i = 0; i < members.length(); i++)
						{
							try {
								ids.add(members.getJSONObject(i).getString("objectId"));
							} catch (JSONException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
								
						}
						fetchNames();
					}
					else
						showParseException(e);
				}
		    });
		}
		
		public void fetchNames()
		{
			if (ids.size() == 0)
			{
				Toast.makeText(getActivity(), "Oops!", Toast.LENGTH_SHORT).show();
				removeSpinner();
				return;
			}
			//this is to get the members 
			ArrayList<ParseQuery<ParseUser>> queries = new ArrayList<ParseQuery<ParseUser>>();
			for (int i = 0; i < ids.size(); i++)
			{
				ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
				userQuery.whereEqualTo("objectId", ids.get(i));
				queries.add(userQuery);
				System.out.println("Second iteration: id " + i + " is " + ids.get(i));
			}
			ParseQuery<ParseUser> mainQuery = ParseQuery.or(queries);
			mainQuery.orderByAscending("firstName");
			mainQuery.addAscendingOrder("lastName");
			mainQuery.findInBackground(new FindCallback<ParseUser>() {
				public void done(List<ParseUser> userList, ParseException e) {
					if (e == null)
					{
						for (int i = 0; i < userList.size(); i++)
						{
							//make room for the creator
							names[i + 1] = userList.get(i).getString("firstName") + " " + 
										userList.get(i).getString("lastName");
							if (userList.get(i).containsKey("profilePhoto"))
								photoURLs[i + 1] = userList.get(i).getParseFile("profilePhoto").getUrl();
						}
						finishResume();
					}
					else
						showParseException(e);
				}
			});
		}

		public void finishResume()
		{
			//so that it won't crash if i move between pages too fast
		    if (mCurrentFragment == "SelectMembersFromListFragment")
		    {
			    GroupAdapter adapter = new GroupAdapter(getActivity(),
			    		R.layout.list_members, names, photoURLs);
			    
			    removeSpinner();
			    // each time we are started use our listadapter
			    ListView lv = (ListView) getActivity().findViewById(R.id.showMembers);
			    lv.setAdapter(adapter);
			    lv.setItemsCanFocus(false);
		    }
		}
	}
}
