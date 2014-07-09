package com.example.balloon;

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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class NewInvitationActivity extends ActionBarActivity {

	private static String mListName;
	private static String mAgenda;
	private String mVenueInfo;
	private static int mExpiresAtHour;
	private static int mExpiresAtMinute;
	private String mVenuePhotoURL;
	private String[] mPhoneNumbers;
	private static String mCurrentFragment;
	//ayyyyyy because sometimes we go outside of the flow
	private static boolean mAfterFinalEdit;
	
	//views to mangae select members fragment
	private static CheckBox checkbox;
	private static ListView lv;
	
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
			transaction.replace(R.id.container, new SelectMembersFromListFragment());
		else if (mCurrentFragment.equals("SelectMembersFragment"))
		{
			transaction.replace(R.id.container, new EditAgendaFragment());
			saveContacts();
		}
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
	
	// http://stackoverflow.com/questions/12413159/android-contact-picker-with-checkbox/
	public void saveContacts()
	{
		//  i get the checked contact_id
		long[] id = ((ListView) findViewById(R.id.memberList)).getCheckedItemIds();
        mPhoneNumbers = new String[id.length];
        for (int i = 0; i < id.length; i++)
        {
            mPhoneNumbers[i] = getPhoneNumber(id[i]); // get phonenumber from selected id
        }
	}
	
	// http://stackoverflow.com/questions/12413159/android-contact-picker-with-checkbox/
	// http://stackoverflow.com/questions/7114573/get-contacts-mobile-number-only
	// http://www.regular-expressions.info/shorthand.html
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
		int items = lv.getCount();
		int checked = lv.getCheckedItemIds().length;
		boolean select = false;
		if (checked != items)
			select = true;
		checkbox.setChecked(select);
		for ( int i=0; i < items; i++)
		{
			   lv.setItemChecked(i, select);
		}
	}
	
	//manages select all when members are clicked individually
	public static void membersListClicked(View view)
	{
		int items = lv.getCount();
		int checked = lv.getCheckedItemIds().length;
		if (checked == items)
			checkbox.setChecked(true);
		else
			checkbox.setChecked(false);
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

		public SelectListFragment() {
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
		}
		
		public void onPause()
		{
			super.onPause();
			EditText et = (EditText) getActivity().findViewById(R.id.editContactListName);
			mListName = et.getText().toString();
		}
	}
	
	public static class SelectMembersFromListFragment extends Fragment implements LoaderCallbacks<Cursor> {

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
			View rootView = inflater.inflate(R.layout.fragment_select_members_from_list,
					container, false);
			return rootView;
		}

	    
	    public void onResume()
		{
			super.onResume();
			getActivity().setTitle(getResources().getString(R.string.title_select_members));
			mCurrentFragment = "SelectMembersFragment";
		}

	    @Override
	    public void onActivityCreated(Bundle savedInstanceState) {
	        super.onActivityCreated(savedInstanceState);

	        checkbox = (CheckBox) getActivity().findViewById(R.id.selectAll);
	        
	        // each time we are started use our listadapter
	        lv = (ListView) getActivity().findViewById(R.id.memberList);
	        lv.setAdapter(mAdapter);
	        lv.setItemsCanFocus(false);
	        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	        //just for managing the checkbox
	        lv.setOnItemClickListener(new OnItemClickListener(){
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					membersListClicked(arg1);
				}
	        });
	        
	        // and tell loader manager to start loading
	        getLoaderManager().initLoader(0, null, this);
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
