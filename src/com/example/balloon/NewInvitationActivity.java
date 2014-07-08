package com.example.balloon;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class NewInvitationActivity extends ActionBarActivity {

	private static String mListName;
	private static String mAgenda;
	private String mVenueInfo;
	private static String mExpiresAt;
	private String mVenuePhotoURL;
	private static String mCurrentFragment;
	//ayyyyyy because sometimes we go outside of the flow
	private static boolean mAfterFinalEdit;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_invitation);

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
		}
		
		public void onPause()
		{
			super.onPause();
			EditText et = (EditText) getActivity().findViewById(R.id.editContactListName);
			mListName = et.getText().toString();
		}
	}
	
	public static class SelectMembersFromListFragment extends Fragment implements LoaderCallbacks<Cursor> {

		public SelectMembersFromListFragment() {
		}
		// Following code mostly from http://stackoverflow.com/questions/18199359/how-to-display-contacts-in-a-listview-in-android-for-android-api-11
		
	    private CursorAdapter mAdapter;

	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);

	        // create adapter once
	        Context context = getActivity();
	        int layout = android.R.layout.simple_list_item_1;
	        Cursor c = null; // there is no cursor yet
	        int flags = 0; // no auto-requery! Loader requeries.
	        mAdapter = new SimpleCursorAdapter(context, layout, c, FROM, TO, flags);
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
	        ListView lv = (ListView) getActivity().findViewById(R.id.memberList);
	        // each time we are started use our listadapter
	        lv.setAdapter(mAdapter);
	        // and tell loader manager to start loading
	        getLoaderManager().initLoader(0, null, this);
	    }

	    // columns requested from the database
	    private static final String[] PROJECTION = {
	        Contacts._ID, // _ID is always required
	        Contacts.DISPLAY_NAME // that's what we want to display
	    };

	    // and name should be displayed in the text1 textview in item layout
	    private static final String[] FROM = { Contacts.DISPLAY_NAME };
	    private static final int[] TO = { android.R.id.text1 };

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
			tp.setOnTimeChangedListener(mTimeListener);
			
			TextView tv = (TextView) getActivity().findViewById(R.id.deadlineTime);
			tv.setText(formatTime(tp.getCurrentHour(), tp.getCurrentMinute()));
		}
		
		//save the time! except i don't know how to sob
		public void onPause()
		{
			super.onPause();
			mExpiresAt = ((TextView) getActivity().findViewById(R.id.deadlineTime)).getText().toString();
		}
		
		public String formatTime(int hour, int minute)
		{
			//so that i can have 12s instead of 0s
			String time = "" + ((hour + 1 ) % 12 - 1) + ":";
			if (minute < 10)
				time = time + "0";
			time = time + minute;
			if (hour / 12 == 0)
				time = time + " AM";
			else
				time = time + " PM";
			return time;
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
			tv.setText(mExpiresAt);
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
			tv.setText(mExpiresAt);
			return rootView;
		}
	}
}
