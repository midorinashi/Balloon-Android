package com.example.balloon;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Contacts.Data;
import android.provider.ContactsContract.RawContacts;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class NewInvitationActivity extends ActionBarActivity implements OnMemberListSelectedListener {

	private static String mListName;
	private static boolean mPublicList;
	private static String mListId;
	private static String mAgenda;
	private String mVenueInfo;
	private static int mExpiresAtHour;
	private static int mExpiresAtMinute;
	private String mVenuePhotoURL;
	private static String[] mPhoneNumbers;
	private static String mCurrentFragment;
	//ayyyyyy because sometimes we go outside of the flow
	private static boolean mAfterFinalEdit;
	
	//views to mangae select members fragment
	private static CheckBox mCheckbox;
	private static ListView mListView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_invitation);

		//set unknown time
		mExpiresAtHour = -1;
		mExpiresAtMinute = 0;
		
		if (savedInstanceState == null) {
			mAfterFinalEdit = false;
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new SelectListFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.new_invitation, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings)
		{
			return true;
		}
		if (id== R.id.action_next)
		{
			next();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void next()
	{
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		if (mCurrentFragment.equals("SelectListFragment"))
			transaction.replace(R.id.container, new CreateListFragment());
		else if (mCurrentFragment.equals("CreateListFragment"))
			transaction.replace(R.id.container, new SelectMembersFromContactsFragment());
		else if (mCurrentFragment.equals("SelectMembersFromContactsFragment"))
		{
			transaction.replace(R.id.container, new EditAgendaFragment());
			saveContacts();
		}
		else if(mCurrentFragment.equals("SelectMembersFromListFragment"))
			transaction.replace(R.id.container, new EditAgendaFragment());
		else if (mCurrentFragment.equals("EditAgendaFragment"))
			transaction.replace(R.id.container, new ChooseLocationFragment());
		else if (mCurrentFragment.equals("ChooseLocationFragment"))
			transaction.replace(R.id.container, new SetDeadlineFragment());
		else if (mCurrentFragment.equals("SetDeadlineFragment"))
			transaction.replace(R.id.container, new FinalEditFragment());
		else
			return;
		transaction.addToBackStack(null);
		transaction.commit();
	}
	
	//opens up the SelectMembersFromListFragment when a list is selected
	public void onMemberListSelected() {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.container, new SelectMembersFromListFragment());
		transaction.addToBackStack(null);
		transaction.commit();
	}
	
	// http://stackoverflow.com/questions/12413159/android-contact-picker-with-checkbox/
	public void saveContacts()
	{
		//  i get the checked contact_id
		long[] id = ((ListView) findViewById(R.id.contactsList)).getCheckedItemIds();
        mPhoneNumbers = new String[id.length];
        for (int i = 0; i < id.length; i++)
        {
            mPhoneNumbers[i] = getPhoneNumber(id[i]); // get phonenumber from selected id
        }
	}
	
	/* References
	 * http://stackoverflow.com/questions/12413159/android-contact-picker-with-checkbox/
	 * http://stackoverflow.com/questions/7114573/get-contacts-mobile-number-only
	 * http://www.regular-expressions.info/shorthand.html
	 */
	private String getPhoneNumber(long id) {
	    String phone = null;
	    Cursor phonesCursor = null;
	    phonesCursor = queryPhoneNumbers(id);
	    if (phonesCursor == null || phonesCursor.getCount() == 0) {
	        // No valid number
	    	System.out.println("No valid number");
	        return null;
	    }
	    else {
	        phonesCursor.moveToPosition(-1);
	        while (phonesCursor.moveToNext()) {
		        int phoneType = phonesCursor.getInt(phonesCursor.getColumnIndex(Phone.TYPE));
		        if (phoneType == Phone.TYPE_MOBILE)
		        {
		             phone = phonesCursor.getString(phonesCursor.getColumnIndex
		            		 (ContactsContract.CommonDataKinds.Phone.DATA));
		             break;
		        }
	        }
	    }
	    if (phone == null)
	    {
	        phone = phonesCursor.getString(phonesCursor
	                .getColumnIndex(Phone.NUMBER));
	    }
        phone = phone.replaceAll("[^[0-9]]", "");
        //TODO deal with missing area codes and missing country codes - use user's number
        //has area code but no country code - auto adds 1 for america
        if (phone.length() == 10)
        	phone = "1" + phone;
        phone = "+" + phone;
	    return phone;
	}
	
	// http://stackoverflow.com/questions/12413159/android-contact-picker-with-checkbox/
	private Cursor queryPhoneNumbers(long contactId) {
	    ContentResolver cr = getContentResolver();
	    Uri baseUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,
	            contactId);
	    Uri dataUri = Uri.withAppendedPath(baseUri,
	            ContactsContract.Contacts.Data.CONTENT_DIRECTORY);

	    Cursor c = cr.query(dataUri, new String[] { Phone._ID, Phone.NUMBER,
	            Phone.IS_SUPER_PRIMARY, RawContacts.ACCOUNT_TYPE, Phone.TYPE,
	            Phone.LABEL }, Data.MIMETYPE + "=?",
	            new String[] { Phone.CONTENT_ITEM_TYPE }, null);
	    if (c != null && c.moveToFirst()) {
	        return c;
	    }
	    return null;
	}
	
	//manages the checkbox in the select members fragment
	public void selectAll(View view)
	{
		// get how many have been checked
		int items = mListView.getCount();
		int checked = mListView.getCheckedItemIds().length;
		boolean select = false;
		System.out.println("Items: " + items);
		System.out.println("Checked: " + checked);
		if (checked != items)
			select = true;
		mCheckbox.setChecked(select);
		for ( int i=0; i < items; i++)
		{
			mListView.setItemChecked(i, select);
		}
	}
	
	//manages select all when members are clicked individually
	public static void membersListClicked(View view)
	{
		int items = mListView.getCount();
		int checked = mListView.getCheckedItemIds().length;
		if (checked == items)
			mCheckbox.setChecked(true);
		else
			mCheckbox.setChecked(false);
	}
	
	public void preview(View view)
	{
		// :)
		//Look, it's Brian!
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.container, new PreviewFragment());
		transaction.addToBackStack(null);
		transaction.commit();
	}
	
	public void send(View view)
	{
		Context context = getApplicationContext();
		CharSequence text = "Sent! jk";
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
		finish();
	}

	public static String formatTime(int hour, int minute)
	{
		//so that i can have 12s instead of 0s
		String time = "" + (((hour + 11 ) % 12) + 1) + ":";
		if (minute < 10)
			time = time + "0";
		time = time + minute;
		if (hour / 12 == 0)
			time = time + " AM";
		else
			time = time + " PM";
		return time;
	}
	
	public static class SelectListFragment extends Fragment {

		private String[] lists;
		private String[] ids;
		private OnMemberListSelectedListener mListener;
		
		public void onAttach(Activity activity) {
	        super.onAttach(activity);
	        try {
	            mListener = (OnMemberListSelectedListener) activity;
	        } catch (ClassCastException e) {
	            throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
	        }
	    }

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_select_list,
					container, false);
			return rootView;
		}
		
		//fix the action bar
		public void onResume()
		{
			super.onResume();
			getActivity().setTitle(getResources().getString(R.string.title_select_list));
			mCurrentFragment = "SelectListFragment";
		}
		
		public void onActivityCreated(Bundle savedInstanceState)
		{
			super.onActivityCreated(savedInstanceState);
			
			//Get all the contactlists that the owner owns
			ParseQuery<ParseObject> ownerQuery = new ParseQuery<ParseObject>("ContactList");
			ownerQuery.whereEqualTo("owner", ParseUser.getCurrentUser());
			
			//Get all the public contactlists that the owner is a part of
			ParseQuery<ParseObject> memberQuery = new ParseQuery<ParseObject>("ContactList");
			ArrayList<ParseUser> currentUser = new ArrayList<ParseUser>();
			currentUser.add(ParseUser.getCurrentUser());
			memberQuery.whereContainsAll("members", currentUser);
			memberQuery.whereEqualTo("isVisibleToMembers", true);
			
			ArrayList<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
			queries.add(ownerQuery);
			queries.add(memberQuery);
			
			ParseQuery<ParseObject> mainQuery = ParseQuery.or(queries);
			mainQuery.orderByAscending("name");
			mainQuery.findInBackground(new FindCallback<ParseObject>() {
				public void done(List<ParseObject> memberLists, ParseException e) {
					if (e == null)
					{
						lists = new String[memberLists.size()];
						ids = new String[memberLists.size()];
						for (int i = 0; i < memberLists.size(); i++)
						{
							lists[i] = memberLists.get(i).getString("name");
							ids[i] = memberLists.get(i).getObjectId();
						}
						addListsToView();
					}
					else
						e.printStackTrace();
				}
			});
		}
		
		public void addListsToView()
		{
			ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(),
					android.R.layout.simple_list_item_1, lists);
	        ListView lv = (ListView) getActivity().findViewById(R.id.groupList);
	        lv.setAdapter(arrayAdapter);
	        lv.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> a, View v, int position, long id) {
					mListName = lists[position];
					mListId = ids[position];
					mListener.onMemberListSelected();
				}
	        });
		}
	}
	
	public static class CreateListFragment extends Fragment {

		public CreateListFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_create_list,
					container, false);
			return rootView;
		}
		
		//fix the action bar
		public void onResume()
		{
			super.onResume();
			getActivity().setTitle(getResources().getString(R.string.title_create_list));
			mCurrentFragment = "CreateListFragment";
			if (mListName != null)
			{
				EditText et = (EditText) getActivity().findViewById(R.id.editContactListName);
				et.setText(mListName);
			}
			if (mPublicList == true)
			{
				CheckBox checkbox = (CheckBox) getActivity().findViewById(R.id.publicListCheckBox);
				checkbox.setChecked(true);
			}
		}
		
		public void onPause()
		{
			super.onPause();
			EditText et = (EditText) getActivity().findViewById(R.id.editContactListName);
			mListName = et.getText().toString();
			CheckBox checkbox = (CheckBox) getActivity().findViewById(R.id.publicListCheckBox);
			mPublicList = checkbox.isChecked();
		}
	}
	
	public static class SelectMembersFromContactsFragment extends Fragment
			implements LoaderCallbacks<Cursor> {

		// Following code mostly from http://stackoverflow.com/questions/18199359/how-to-display-contacts-in-a-listview-in-android-for-android-api-11
	    private CursorAdapter mAdapter;

	    // and name should be displayed in the text1 textview in item layout
	    private static final String[] FROM = { Contacts.DISPLAY_NAME };
	    private static final int[] TO = { android.R.id.text1 };

	    // columns requested from the database
	    private static final String[] PROJECTION = {
	        Contacts._ID, // _ID is always required
	        Contacts.DISPLAY_NAME // that's what we want to display
	    };

	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);

	        // create adapter once=
	        int layout = android.R.layout.simple_list_item_multiple_choice;
	        Cursor c = null; // there is no cursor yet
	        int flags = 0; // no auto-requery! Loader requeries.
	        mAdapter = new SimpleCursorAdapter(getActivity(), layout, c, FROM, TO, flags);
	    }

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_select_members_from_contacts,
					container, false);
			return rootView;
		}

	    @Override
	    public void onActivityCreated(Bundle savedInstanceState)
	    {
	        super.onActivityCreated(savedInstanceState);

	        mCheckbox = (CheckBox) getActivity().findViewById(R.id.contactsSelectAll);
	        
	        // each time we are started use our listadapter
	        mListView = (ListView) getActivity().findViewById(R.id.contactsList);
	        mListView.setAdapter(mAdapter);
	        mListView.setItemsCanFocus(false);
	        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	        //just for managing the checkbox
	        mListView.setOnItemClickListener(new OnItemClickListener(){
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					membersListClicked(arg1);
				}
	        });
	        
	        // and tell loader manager to start loading
	        getLoaderManager().initLoader(0, null, this);
	    }
    
	    public void onResume()
		{
			super.onResume();
			getActivity().setTitle(getResources().getString(R.string.title_select_members_from_contacts));
			mCurrentFragment = "SelectMembersFromContactsFragment";
		}

	    @Override
	    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

	        // load from the "Contacts table"
	        Uri contentUri = Contacts.CONTENT_URI;

	        // no sub-selection, no sort order, simply every row
	        // projection says we want just the _id and the name column
	        return new CursorLoader(getActivity(),
	                contentUri,
	                PROJECTION,
	                null,
	                null,
	                null);
	    }

	    @Override
	    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
	        // Once cursor is loaded, give it to adapter
	        mAdapter.swapCursor(data);
	    }

	    @Override
	    public void onLoaderReset(Loader<Cursor> loader) {
	        // on reset take any old cursor away
	        mAdapter.swapCursor(null);
	    }
	}
	
	public static class SelectMembersFromListFragment extends Fragment {
		
		// and name should be displayed in the text1 textview in item layout
		private String[] names;
		private String[] ids;
		private ArrayList<String> phones;
		private ListAdapter mArrayAdapter;
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
		    super.onCreate(savedInstanceState);
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_select_members_from_list,
					container, false);
			return rootView;
		}
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState)
		{
		    super.onActivityCreated(savedInstanceState);

		    ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("ContactList");
		    query.whereEqualTo("objectId", mListId);
		    query.getFirstInBackground(new GetCallback<ParseObject>() {
				public void done(ParseObject contactList, ParseException e) {
					if (e == null)
					{
						System.out.println("I AIN'T BROKE");
						JSONArray members = contactList.getJSONArray("members");
						int length = members.length();
						String currentUserId = ParseUser.getCurrentUser().getObjectId();
						System.out.println(currentUserId.compareTo(contactList.getParseUser("owner").getObjectId()));
						//if the user isn't a member, then we need to remove the member from the list and add owner
						boolean ownerIsUser = false;
						if (currentUserId.compareTo(contactList.getParseUser("owner").getObjectId()) == 0)
						{
							ownerIsUser = true;
						}
						names = new String[length];
						ids = new String[length];
						phones = new ArrayList<String>();
						int empty = 0;
						//put all member names and ids into respective arrays
						for (int i = 0; i < members.length(); i++)
						{
							if (ownerIsUser || !memberIsUser(members, i))
								try {
									ids[i] = members.getJSONObject(i).getString("objectId");
								} catch (JSONException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							else
								empty = i;
								
						}
						//add the creator now -- it's not a json object
						if (!ownerIsUser)
							ids[empty] = contactList.getParseUser("owner").getObjectId();
						for (int i = 0; i < ids.length; i++)
							System.out.println("First iteration: id " + i + " is " + ids[i]);
						fetchNames();
					}
					else
						e.printStackTrace();
				}
		    });
		}
		
		public boolean memberIsUser(JSONArray members, int i)
		{
			String currentUserId = ParseUser.getCurrentUser().getObjectId();
			String otherUserId = "";
			try {
				otherUserId = members.getJSONObject(i).getString("objectId");
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			return (currentUserId.compareTo(otherUserId) == 0);
		}
		
		public void fetchNames()
		{
			ArrayList<ParseQuery<ParseUser>> queries = new ArrayList<ParseQuery<ParseUser>>();
			for (int i = 0; i < ids.length; i++)
			{
				ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
				userQuery.whereEqualTo("objectId", ids[i]);
				queries.add(userQuery);
				System.out.println("Second iteration: id " + i + " is " + ids[i]);
			}
			ParseQuery<ParseUser> mainQuery = ParseQuery.or(queries);
			mainQuery.orderByAscending("firstName");
			mainQuery.addAscendingOrder("lastName");
			mainQuery.findInBackground(new FindCallback<ParseUser>() {
				public void done(List<ParseUser> userList, ParseException e) {
					if (e == null)
					{
						names = new String[userList.size()];
						for (int i = 0; i < userList.size(); i++)
						{
							names[i] = userList.get(i).getString("firstName") + " " + 
										userList.get(i).getString("lastName");
							//because the ids are no longer in the same order as they used to be!
							ids[i] = userList.get(i).getObjectId();
							phones.add(userList.get(i).getUsername());
							System.out.println(names[i]);
						}
						finishCreatingActivity();
					}
					else
						e.printStackTrace();
				}
			});
		}

		public void finishCreatingActivity()
		{
		    int layout = android.R.layout.simple_list_item_multiple_choice;
		    if (mArrayAdapter == null)
			    mArrayAdapter = new CheckArrayAdapter(getActivity(),
						layout, names);
		    
		    mCheckbox = (CheckBox) getActivity().findViewById(R.id.membersSelectAll);
		    
		    // each time we are started use our listadapter
		    mListView = (ListView) getActivity().findViewById(R.id.memberList);
		    mListView.setAdapter(mArrayAdapter);
		    mListView.setItemsCanFocus(false);
		    mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		    //just for managing the checkbox
		    mListView.setOnItemClickListener(new OnItemClickListener(){
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					membersListClicked(arg1);
				}
		    });

		    //recheck people if members have been chosen before
			if (mPhoneNumbers != null)
				for (int i = 0; i < mPhoneNumbers.length; i++)
					mListView.setItemChecked(phones.indexOf(mPhoneNumbers[i]), true);
		}
		
		public void onResume()
		{
			super.onResume();
			getActivity().setTitle(getResources().getString(R.string.title_select_members_from_list));
			mCurrentFragment = "SelectMembersFromListFragment";
			//to recheck people
		}
		
		public void onPause()
		{
			super.onPause();
			long[] viewIds = ((ListView) getActivity().findViewById(R.id.memberList)).getCheckedItemIds();
			mPhoneNumbers = new String[viewIds.length];
			for (int i = 0; i < viewIds.length; i++)
			{
				mPhoneNumbers[i] = phones.get((int) viewIds[i]);
				System.out.println(mPhoneNumbers[i]);
			}
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
			return rootView;
		}
		
		@Override
		public void onResume()
		{
			super.onResume();
			getActivity().setTitle(getResources().getString(R.string.title_edit_agenda));
			mCurrentFragment = "EditAgendaFragment";
			if (mAgenda != null)
			{
				EditText et = (EditText)(getActivity().findViewById(R.id.editAgenda));
				et.setText(mAgenda);
			}
		}
		
		public void onPause()
		{
			super.onPause();
			EditText et = (EditText)(getActivity().findViewById(R.id.editAgenda));
			mAgenda = et.getText().toString();
		}
	}
	
	public static class ChooseLocationFragment extends Fragment {

		public ChooseLocationFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_choose_location,
					container, false);
			return rootView;
		}
		
		//fix the action bar
		public void onResume()
		{
			super.onResume();
			getActivity().setTitle(getResources().getString(R.string.title_choose_location));
			mCurrentFragment = "ChooseLocationFragment";
		}
	}
	
	public static class SetDeadlineFragment extends Fragment {

		public TimePicker.OnTimeChangedListener mTimeListener;
		
		public SetDeadlineFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_set_deadline,
					container, false);
			mTimeListener = new TimePicker.OnTimeChangedListener() {
				@Override
				public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
					TextView tv = (TextView) getActivity().findViewById(R.id.deadlineTime);
					mExpiresAtHour = hourOfDay;
					mExpiresAtMinute = minute;
					tv.setText(formatTime(hourOfDay, minute));
					tv.invalidate();
					tv.requestLayout();
				}
			};
			return rootView;
		}
		
		//fix the action bar
		public void onResume()
		{
			super.onResume();
			getActivity().setTitle(getResources().getString(R.string.title_set_deadline));
			mCurrentFragment = "SetDeadlineFragment";
			
			TimePicker tp = (TimePicker) getActivity().findViewById(R.id.timePicker);
			if (mExpiresAtHour == -1)
			{
				mExpiresAtHour = tp.getCurrentHour() + 1;
			}
			tp.setCurrentHour(mExpiresAtHour);
			tp.setCurrentMinute(mExpiresAtMinute);
			tp.setOnTimeChangedListener(mTimeListener);
		
			TextView tv = (TextView) getActivity().findViewById(R.id.deadlineTime);
			tv.setText(formatTime(tp.getCurrentHour(), tp.getCurrentMinute()));
		}
	}
	
	//TODO when sending the data, delete all the fields afterwards! or else we can send the same invite again
	public static class FinalEditFragment extends Fragment {
		
		public FinalEditFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_final_edit,
					container, false);
			return rootView;
		}
		
		//fix the action bar, make sure all info is current
		public void onResume()
		{
			super.onResume();
			getActivity().setTitle(getResources().getString(R.string.balloon));
			mCurrentFragment = "FinalEditFragment";
			mAfterFinalEdit = false;
			
			TextView tv = (TextView) getActivity().findViewById(R.id.finalEditTo);
			tv.setText(mListName);
			tv = (TextView) getActivity().findViewById(R.id.finalEditAgenda);
			tv.setText(mAgenda);
			tv = (TextView) getActivity().findViewById(R.id.finalEditLocation);
			//tv.setText(mVenueInfo);
			tv = (TextView) getActivity().findViewById(R.id.finalEditDeadline);
			tv.setText(formatTime(mExpiresAtHour, mExpiresAtMinute));
		}
	}
	
	public static class PreviewFragment extends Fragment {
		
		public PreviewFragment(){
		}
		
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_preview,
					container, false);
			TextView tv = (TextView) rootView.findViewById(R.id.creator);
			tv.setText(R.string.dummy_name);
			tv = (TextView) rootView.findViewById(R.id.agenda);
			tv.setText(mAgenda);
			tv = (TextView) rootView.findViewById(R.id.venueInfo);
			tv.setText(R.string.dummy_location);
			tv = (TextView) rootView.findViewById(R.id.timeToRSVP);
			tv.setText(formatTime(mExpiresAtHour, mExpiresAtMinute));
			return rootView;
		}
	}

}
