package com.j32productions.balloon;
//TODO CHECK TO SEE IF THEY HAVE A CAMERA
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Contacts.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

public class NewInvitationActivity extends ProgressActivity implements OnMemberListSelectedListener {

	protected static String mListName;
	protected static boolean mPublicList;
	protected static ParseObject mListId;
	protected static String mAgenda;
	protected static String mVenueInfo;
	protected static JSONObject mVenue;
	protected static boolean mToday;
	protected static int mExpiresAtHour;
	protected static int mExpiresAtMinute;
	protected static JSONArray mVenuePhotoUrls;
	protected static JSONArray mMemberObjectIds;
	protected static String[] mPhoneNumbers;
	protected static String[] mMemberFirstNames;
	protected static String[] mMemberLastNames;
	protected static boolean mMakeContactList;
	protected static boolean mFinishSavingMeetup;
	protected static String[] mMemberIds;
	protected static JSONArray mMembers;
	protected static String mPreviewName;
	protected static boolean mInviteMore;
	protected static String mCurrentFragment;
	//ayyyyyy because sometimes we go outside of the flow
	protected static boolean mAfterFinalEdit;
	
	//views to mangae select members fragment
	protected static CheckBox mCheckbox;
	public static ListView mListView;
	protected static ContextMenu mMenu;
	protected File lastSavedFile;
	public static boolean popped;
	public static boolean hasChangedName;
	protected static ArrayList<ParseUser> mParseUsers;
	public static Date mStartDeadline;
	protected static ParseFile mContactListImage;
	
	//to manage the buttons at the top
	protected static boolean mPlus;
	protected static String mNext;
	protected static Activity context;
	public static int mLimit;
	public static int mSpotsLeft;
	public static boolean mIsFull;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ParseUser.getCurrentUser().fetchInBackground(null);
		setContentView(R.layout.activity_new_invitation);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		//set all the defaults
		defaultInfo(savedInstanceState);
	}
	
	//erase all static fields
	protected void defaultInfo(Bundle savedInstanceState)
	{
		context = this;
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
		mFinishSavingMeetup = false;
		mMemberIds = null;
		mMembers = null;
		mMemberFirstNames = null;
		mMemberLastNames = null;
		mPreviewName = null;
		mInviteMore = true;
		mCurrentFragment = "";
		mAfterFinalEdit = false;
		mCheckbox = null;
		mListView = null;
		mPlus = true;
		mContactListImage = null;
		mLimit = 0;
		mSpotsLeft = 0;
		mIsFull = false;
		
		if (savedInstanceState == null) {
			mAfterFinalEdit = false;
			getFragmentManager().beginTransaction()
					.add(R.id.container, new SelectListFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.new_invitation, menu);
		return true;
	}
	
	public boolean onPrepareOptionsMenu(final Menu menu) {
	    if (!mPlus)
	    {
	    	if (!mCurrentFragment.equals("ChooseFromExistingList")
	    			&& !mCurrentFragment.equals("PreviewFragment"))
	    	{
			    MenuItem item = menu.findItem(R.id.action_next);
		    	item.setTitle(mNext);
		    	item.setVisible(true);
	    	}
	    	menu.findItem(R.id.action_plus).setVisible(false);
	    }
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
		else if (id == R.id.action_plus)
		{
			FragmentTransaction transaction = getFragmentManager().beginTransaction();
			transaction.replace(R.id.container, new SelectMembersFromContactsFragment());
			transaction.addToBackStack(null);
			transaction.commit();
			return true;
		}
		if (id== R.id.action_next)
		{
			next();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	//basically manages entire flow
	public void next()
	{
		removeSpinner();
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		if (mCurrentFragment.equals("SelectListFragment"))
			transaction.replace(R.id.container, new SelectMembersFromContactsFragment());
		else if (mCurrentFragment.equals("CreateListFragment"))
		{
			if (((EditText) findViewById(R.id.editContactListName)).length() != 0)
				transaction.replace(R.id.container, new SelectMembersFromContactsFragment());
			else
				Toast.makeText(this, "Please name the contact list.", Toast.LENGTH_SHORT).show();
		}
		else if (mCurrentFragment.equals("SelectMembersFromContactsFragment"))
		{
			if (((ListView) findViewById(R.id.contactsList)).getCheckedItemCount() != 0)
			{
				SelectMembersFromContactsFragment.saveContacts();
				mMakeContactList = true;
				if (mAfterFinalEdit)
				{
					getFragmentManager().popBackStack();
					getFragmentManager().popBackStack();
					return;
				}
				else
					transaction.replace(R.id.container, new EditAgendaFragment());
			}
			else
				Toast.makeText(this, "No friends?", Toast.LENGTH_SHORT).show();
		}
		else if(mCurrentFragment.equals("SelectMembersFromListFragment"))
		{
			if (((ListView) findViewById(R.id.memberList)).getCheckedItemCount() != 0)
			{
				SelectMembersFromListFragment.saveMembers();
				mMakeContactList = false;
				if (mAfterFinalEdit)
				{
					getFragmentManager().popBackStack();
					getFragmentManager().popBackStack();
					return;
				}
				transaction.replace(R.id.container, new EditAgendaFragment());
			}
			else
				Toast.makeText(this, "No friends?", Toast.LENGTH_SHORT).show();
		}
		else if (mCurrentFragment.equals("EditAgendaFragment"))
		{
			//check we have a agenda
			if (((EditText) findViewById(R.id.editAgenda)).getText().length() != 0)
			{
				EditAgendaFragment.saveAgenda();
				if (mAfterFinalEdit)
				{
					getFragmentManager().popBackStack();
				}
				else
					transaction.replace(R.id.container, new ChooseLocationFragment());
			}
			else
				Toast.makeText(this, "What's the plan?", Toast.LENGTH_SHORT).show();
		}
		else if (mCurrentFragment.equals("ChooseLocationFragment"))
		{
			CharSequence text = "Please choose a location.";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(this, text, duration);
			toast.show();
		}
		else if (mCurrentFragment.equals("SetDeadlineFragment"))
		{
			SetDeadlineFragment.saveTime();
			if (mAfterFinalEdit)
			{
				getFragmentManager().popBackStack();
			}
			else
				transaction.replace(R.id.container, new FinalEditFragment());
		}
		else if (mCurrentFragment.equals("FinalEditFragment"))
		{
			makeMeetup(null);
			return;
		}
		else if (mCurrentFragment.equals("ImageListFragment"))
		{
			ImageListFragment.setSave(true);
			getFragmentManager().popBackStack();
			return;
		}
		else if (mCurrentFragment.equals("GroupSummaryFragment"))
		{
			saveNewContactList();
			return;
		}
		else if (mCurrentFragment.equals("StartTimeFragment"))
		{
			StartTimeFragment.saveStartTime();
			getFragmentManager().popBackStack();
			return;
		}
		else if (mCurrentFragment.equals("LimitFragment"))
		{
			LimitFragment.saveLimit();
			getFragmentManager().popBackStack();
			return;
		}
		else
			return;
		transaction.addToBackStack(null);
		transaction.commit();
	}
	
	//opens up the SelectMembersFromListFragment when a list is selected
	public void onMemberListSelected() {
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.replace(R.id.container, new SelectMembersFromListFragment());
		transaction.addToBackStack(null);
		transaction.commit();
	}
	/*
	// http://stackoverflow.com/questions/12413159/android-contact-picker-with-checkbox/
	public void saveContacts()
	{
		//  i get the checked contact_id
		long[] id = ((ListView) findViewById(R.id.contactsList)).getCheckedItemIds();
        mPhoneNumbers = new String[id.length];
        mMemberNames = new String[id.length];
        for (int i = 0; i < id.length; i++)
        {
            mPhoneNumbers[i] = getPhoneNumber(id[i]); // get phonenumber from selected id
            mMemberNames[i] = getName(id[i]);

			System.out.println(mMemberNames[i] +" " + mPhoneNumbers[i]);
        }
        if (id.length > 0)
        	if (mMemberNames[0].indexOf(' ') > -1)
        		mPreviewName = mMemberNames[0].substring(0, mMemberNames[0].indexOf(' '));
        	else
        		mPreviewName = mMemberNames[0];
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
        //has area code but no country code - cloud code auto adds america
        /*
        if (phone.length() == 10)
        	phone = "1" + phone;
        phone = "+" + phone;
        */
        phonesCursor.close();
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
	
	private String getName(long id) {
	    String name = null;
	    Cursor namesCursor = null;
	    namesCursor = queryName(id);
	    if (namesCursor == null || namesCursor.getCount() == 0) {
	        // No valid number
	    	System.out.println("No Name");
	        return null;
	    }
	    else {
	    	namesCursor.moveToPosition(0);
	        name = namesCursor.getString(namesCursor.getColumnIndex(StructuredName.DISPLAY_NAME));
	    }
	    if (name == null)
	    {
	        name = "";
	    }
	    if (name.indexOf(' ') > -1)
	    	name = name.substring(0, name.indexOf(' '));
	    namesCursor.close();
	    return name;
	}
	
	// http://stackoverflow.com/questions/12413159/android-contact-picker-with-checkbox/
	private Cursor queryName(long contactId) {
	    ContentResolver cr = getContentResolver();
	    Uri baseUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,
	            contactId);
	    Uri dataUri = Uri.withAppendedPath(baseUri,
	            ContactsContract.Contacts.Data.CONTENT_DIRECTORY);

	    Cursor c = cr.query(dataUri, new String[] { StructuredName.DISPLAY_NAME }, Data.MIMETYPE + "=?",
	            new String[] { StructuredName.CONTENT_ITEM_TYPE }, null);
	    if (c != null && c.moveToFirst()) {
	        return c;
	    } 
	    
	    return null;
	}
	
	//manages the checkbox in the select members fragment
	public void selectAll(View view)
	{
		// get how many have been checked
		if (mListView != null)
		{
			int items = mListView.getCount();
			int checked = mListView.getCheckedItemCount();
			boolean select = false;
			if (checked != items)
				select = true;
			mCheckbox.setChecked(select);
			for ( int i=0; i < items; i++)
			{
				mListView.setItemChecked(i, select);
			}
		}
	}
	
	//manages select all when members are clicked individually
	public static void membersListClicked(View view)
	{
		int items = mListView.getCount();
		int checked = mListView.getCheckedItemCount();
		if (checked == items)
			mCheckbox.setChecked(true);
		else
			mCheckbox.setChecked(false);
	}
	
	public void preview(View view)
	{
		// :)
		//Look, it's Brian!
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.replace(R.id.container, new PreviewFragment());
		transaction.addToBackStack(null);
		transaction.commit();
	}
	
	public void cancel(View view)
	{
		finish();
	}
	
	//this is here so that we can reuse the code when editing le meetup
	public void makeMeetup(final View view)
	{
		if (view != null)
			view.setOnClickListener(null);
		saveMeetup(new ParseObject("Meetup"));
	}
	
	public void saveMeetup(final ParseObject meetup)
	{
		showSpinner();
		//make the contact list if we need to
		if (mMakeContactList && !mFinishSavingMeetup)
		{
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
			ParseCloud.callFunctionInBackground("findOrCreateUsers", params, new FunctionCallback<ArrayList<ParseUser>>() {

				@Override
				public void done(ArrayList<ParseUser> list, ParseException e) {
					if (e == null)
					{
						// need to give all the members to makeMeetup and to create a new contact list
						/*
						ParseObject contactList = new ParseObject("ContactList");
						contactList.put("owner", ParseUser.getCurrentUser());
						contactList.put("name", mListName);
						if (mContactListImage != null)
							contactList.put("photo", mContactListImage);
						contactList.put("isVisibleToMembers", mPublicList);
						JSONArray mContacts = new JSONArray();
						*/
						//two different json arrays because send invites need json objects, not parse objects
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
						/*
						contactList.put("members", mContacts);
						mListId = contactList;
						contactList.saveInBackground(new SaveCallback() {
							@Override
							public void done(ParseException e) {
								if (e == null)
								{
									System.out.println("save success");
									saveMeetup(meetup);
								}
								else
									showParseException(e);
							}
						});
						*/
						mFinishSavingMeetup = true;
						saveMeetup(meetup);
					}
					else
						showParseException(e);
				}
				
			});
			return;
		}
		meetup.put("agenda", mAgenda);
		//make the contact list if we need to
		//contact list id??? not sure how to deal with this right now
		//gotta set both contactList and mMembers here
		if (mListId != null)
			meetup.put("contactList", mListId);
		if (!meetup.has("creator"))
			meetup.put("creator", ParseUser.getCurrentUser());
		else
			meetup.put("creator", meetup.getParseUser("creator"));
		meetup.put("expiresAt", changeToDate(mExpiresAtHour, mExpiresAtMinute));
		meetup.put("invitedUsers", mMembers);
		if (mStartDeadline != null)
			meetup.put("startsAt", mStartDeadline);
		meetup.put("venueInfo", mVenue);
		meetup.put("venuePhotoURLs", mVenuePhotoUrls);
		if (findViewById(R.id.finalEditInviteMoreBox) != null)
			meetup.put("allowInviteMore", ((CheckBox) 
					findViewById(R.id.finalEditInviteMoreBox)).isChecked());
		meetup.put("maxAttendees", mLimit);
		meetup.put("spotsLeft", mSpotsLeft);
		meetup.put("isFull", mIsFull);
		System.out.println("Save meetup id = " + meetup.getObjectId());
		meetup.saveInBackground(new SaveCallback(){

			@Override
			public void done(ParseException e) {
				if (e == null)
				{
					System.out.println("meetup created");
					sendInvite(meetup);
				}
				else
					showParseException(e);
			}
			
		});
	}
	
	public void sendInvite(final ParseObject meetup)
	{
		System.out.println("Send invite id = " + meetup.getObjectId());
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("meetupId", meetup.getObjectId());
		params.put("creator", meetup.getParseUser("creator").getObjectId());
		//params.put("invitedUsers", meetup.getJSONArray("invitedUsers"));
		//no fucking clue why this doesn't work
		try {
			params.put("invitedUsers", new JSONArray(meetup.getJSONArray("invitedUsers").toString()));
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		ParseCloud.callFunctionInBackground("sendInvites", params, new FunctionCallback<Object>(){

			@Override
			public void done(Object arg0, ParseException e) {
				if (e != null)
					showParseException(e);
				respondForCreator(meetup);
			}
			
		});
	}
	
	public void respondForCreator(ParseObject meetup)
	{
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("meetupId", meetup.getObjectId());
		params.put("willAttend", true);
		ParseCloud.callFunctionInBackground("respondToMeetup", params,
				new FunctionCallback<Object>() {
			@Override
			public void done(Object o, ParseException e) {
				if (e != null)
					showParseException(e);
				else
				{
					Toast.makeText(context, "Sent!", Toast.LENGTH_SHORT).show();
					if (mMakeContactList)
					{
						DialogFragment dialog = new AskToSaveListFragment();
				        dialog.show(getFragmentManager(), "NoticeDialogFragment");
					}
					else
						end();
				}
			}
		});
	}
	
	public static Date changeToDate(int hour, int minute)
	{
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.set(GregorianCalendar.HOUR_OF_DAY, hour);
		calendar.set(GregorianCalendar.MINUTE, minute);
		Date date = calendar.getTime();
		//if we're doing tomorrow, we need to incrememnt the dat
		if (date.compareTo(new Date()) < 0)
		{
			calendar.add(GregorianCalendar.DAY_OF_MONTH, 1);
			date = calendar.getTime();
			mToday = false;
		}
		else
			mToday = true;
		return date;
	}
	
	public void end()
	{
		Intent intent = new Intent(this, RSVPEventsActivity.class);
		startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
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
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	                                ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.photo_menu, menu);
	    mMenu = menu;
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.action_photo_foursquare:
	        	if (mVenue.has("id"))
	        	{
		        	FragmentTransaction transaction = getFragmentManager().beginTransaction();
		        	transaction.replace(R.id.container, new ImageListFragment());
		        	transaction.addToBackStack(null);
		        	transaction.commit();
	        	}
	        	else
	        		Toast.makeText(this, "No photos available", Toast.LENGTH_SHORT).show();
	            return true;
	        case R.id.action_photo_phone:
	            return true;
	        case R.id.action_photo_remove:
	        	mVenuePhotoUrls = new JSONArray();
	        	((TextView) findViewById(R.id.finalEditPhotoText))
	        		.setText(getResources().getString(R.string.final_edit_no_photos));
	            return true;
	        case R.id.action_photo_last:// Find the last picture
	        	showSpinner();
	        	String[] projection = new String[]{
	        		    MediaStore.Images.ImageColumns._ID,
	        		    MediaStore.Images.ImageColumns.DATA,
	        		    MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
	        		    MediaStore.Images.ImageColumns.DATE_TAKEN,
	        		    MediaStore.Images.ImageColumns.MIME_TYPE
	        		    };
	        		final Cursor cursor = getContentResolver()
	        		        .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, 
	        		               null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

	        		// Put it in the image view
	        		if (cursor.moveToFirst()) {
	        		    String imageLocation = cursor.getString(1);
	        		    File imageFile = new File(imageLocation);
	        		    if (imageFile.exists()) {   // TODO: is there a better way to do this?
	        		    	System.out.println("It's working!");
	        	            Bitmap bm = BitmapFactory.decodeFile(imageLocation);
	        	            saveBitmap(bm);
	        		    }
	        		} 
	        		cursor.close();
	            return true;
	        case R.id.action_photo_take:
	        	Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	        	lastSavedFile = getTempFile();
	        	intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(lastSavedFile));
	        	startActivityForResult(intent, 0);
	        	return true;
	        case R.id.action_photo_library:
	        	intent = new Intent(Intent.ACTION_PICK, 
	        			android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
	        	startActivityForResult(intent, 1);
	        	return true;
	        default:
	            return super.onContextItemSelected(item);
	    }
	}
	private File getTempFile() {
	    // Create an image file name
	    String imageFileName = "temp.png";
	    return new File(Environment.getExternalStorageDirectory(), imageFileName);
	}
	
	@Override
	//this is how we get the picture
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
        	showSpinner();
        	System.out.println("hi");
        	// if it came from the camera
			Uri uri;
			if (requestCode == 0)
        		uri = android.net.Uri.fromFile(lastSavedFile);
        	//if it came from the library
        	else
        		uri = data.getData();
        	try {
				Bitmap bm = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
				saveBitmap(bm);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
        } else if (resultCode == RESULT_CANCELED) {
        	System.out.println("canceled");
            // User cancelled the image capture
        } else {
        	System.out.println("dedd");

			CharSequence text = "Image capture failed.";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(this, text, duration);
			toast.show();
            // Image capture failed, advise user
        }
	}
	
	public void saveBitmap(Bitmap original)
	{
		if (mCurrentFragment == "FinalEditFragment")
		{
			//first crop it
			int width = original.getWidth();
			int height = original.getHeight();
			Bitmap crop;
			if (width > 3*height)
				crop = Bitmap.createBitmap(original, width/2 - (int)(1.5*height), 0, 3*height, height);
			else
				crop = Bitmap.createBitmap(original, 0, height/2 - width/6, width, width/3);
			//then resize
			Bitmap bm = Bitmap.createScaledBitmap(crop, 300, 100, true);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
			byte[] byteArray = stream.toByteArray();
			final ParseFile image = new ParseFile("profile.png", byteArray);
			image.saveInBackground(new SaveCallback(){
				public void done(ParseException e) {
					if (e == null)
					{
						removeSpinner();
						addImage(image.getUrl());
					}
					else
						showParseException(e);
				}
			});
		}
		else
		{
			//first crop it
			int width = original.getWidth();
			int height = original.getHeight();
			Bitmap crop;
			if (width > height)
				crop = Bitmap.createBitmap(original, width/2 - height/2, 0, height, height);
			else
				crop = Bitmap.createBitmap(original, 0, height/2 - width/2, width, width);
			//then resize
			Bitmap bm = Bitmap.createScaledBitmap(crop, 320, 320, true);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
			byte[] byteArray = stream.toByteArray();
			final ParseFile image = new ParseFile("profile.png", byteArray);
			image.saveInBackground(new SaveCallback(){
				public void done(ParseException e) {
					if (e == null)
					{
						mContactListImage = image;
						setContactListImage();
					}
					else
						showParseException(e);
				}
			});
		}
	}
	
	public void setContactListImage()
	{
		if (mCurrentFragment == "GroupSummaryFragment")
		{
			removeSpinner();
			ImageView view = (ImageView) findViewById(R.id.editGroup).findViewById(R.id.imageView1);
			if (view != null)
				Picasso.with(this).load(mContactListImage.getUrl()).resize(140, 140).into(view);
		}
	}
	
	public static void addImage(String imageUrl)
	{
		mVenuePhotoUrls.put(imageUrl);
		System.out.println(mVenuePhotoUrls.length() + " photos now");

		//need to replace words if saving takes longer than switching fragments
		TextView tv = (TextView) context.findViewById(R.id.finalEditPhotoText);
		int photos = mVenuePhotoUrls.length();
		if (tv != null && photos > 1)
			tv.setText(mVenuePhotoUrls.length() + " photos");
		else if (tv != null && photos == 1)
			tv.setText("1 photo");
	}
	
	public void saveNewContactList()
	{
		showSpinner();
		ParseObject contactList = new ParseObject("ContactList");
		contactList.put("name", mListName);
		contactList.put("owner", ParseUser.getCurrentUser());
		contactList.put("isVisibleToMembers", ((Checkable) findViewById(R.id.checkBox1)).isChecked());
		if (mContactListImage != null)
			contactList.put("photo", mContactListImage);
		contactList.put("members", mMembers);
		contactList.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if (e == null)
				{
					removeSpinner();
					Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show();
				}
				else
				{
					showParseException(e);
					Toast.makeText(context, "Oops! Something went wrong.", Toast.LENGTH_SHORT).show();
				}
				popped = true;
				Intent intent = new Intent(context, RSVPEventsActivity.class);
				startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
				finish();
			}
		});
	}
	
	public static class SelectListFragment extends ProgressFragment {

		protected String[] lists;
		protected String[] photoURLs;
		protected ParseObject[] ids;
		protected ArrayList<Boolean> mShellGroups;
		protected OnMemberListSelectedListener mListener;
		protected List<ParseObject> mMemberLists;
		
		public void onAttach(Activity activity) {
	        super.onAttach(activity);
	        try {
	            mListener = (OnMemberListSelectedListener) activity;
	        } catch (ClassCastException e) {
	            throw new ClassCastException(activity.toString() + " must implement OnMemberSelectedListener");
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
			showSpinner();
			getActivity().setTitle(getResources().getString(R.string.title_select_list));
			mCurrentFragment = "SelectListFragment";
			mShellGroups = new ArrayList<Boolean>();
			//set co-workers, college friends, family, frat/soro, highschool, and room to false
			for (int i = 0; i < 6; i++)
			{
				mShellGroups.add(false);
			}
			
			mPlus = true;
			getActivity().invalidateOptionsMenu();
			
			//Get all the contactlists that the owner owns
			ParseQuery<ParseObject> ownerQuery = new ParseQuery<ParseObject>("ContactList");
			ownerQuery.whereEqualTo("owner", ParseUser.getCurrentUser());
			
			//Get all the public contactlists that the owner is a part of
			ParseQuery<ParseObject> memberQuery = new ParseQuery<ParseObject>("ContactList");
			ArrayList<ParseUser> currentUser = new ArrayList<ParseUser>();
			currentUser.add(ParseUser.getCurrentUser());
			memberQuery.whereContainsAll("members", currentUser);
			memberQuery.whereEqualTo("isVisibleToMembers", true);
			memberQuery.whereNotEqualTo("isVisibleToMembers", false);
			
			ArrayList<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
			queries.add(ownerQuery);
			addMemberQueries(queries, memberQuery);
			System.out.println("queries size " + queries.size());
			
			ParseQuery<ParseObject> mainQuery = ParseQuery.or(queries);
			mainQuery.orderByAscending("name");
			mainQuery.findInBackground(new FindCallback<ParseObject>() {
				public void done(List<ParseObject> memberLists, ParseException e) {
					if (e == null && isAdded())
					{
						mMemberLists = memberLists;
						TypedArray images = getResources().obtainTypedArray(R.array.contact_list_images);
						lists = new String[memberLists.size()];
						photoURLs = new String[memberLists.size()];
						ids = new ParseObject[memberLists.size()];
						for (int i = 0; i < memberLists.size(); i++)
						{
							lists[i] = memberLists.get(i).getString("name");
							checkForShellGroups(lists[i]);
							if (memberLists.get(i).containsKey("photo"))
								photoURLs[i] = memberLists.get(i).getParseFile("photo").getUrl();
							else
								photoURLs[i] = "" + images.getResourceId(
										Math.abs(lists[i].hashCode()) % 12,
										R.drawable.color_balloon_8);
							ids[i] = memberLists.get(i);
						}
						addListsToView();
						images.recycle();
					}
					else if (e != null)
						showParseException(e);
				}
			});
		}
		
		//so i can do stuff to it later
		public void addMemberQueries(ArrayList<ParseQuery<ParseObject>> queries,
				ParseQuery<ParseObject> query)
		{
			queries.add(query);
		}
		
		public void checkForShellGroups(String name)
		{
			name = name.replaceAll("[\\W]", "").toLowerCase(Locale.US);
			if (name.contains("work") || name.contains("colleague"))
				mShellGroups.set(0, true);
			else if (name.contains("collegefriend"))
				mShellGroups.set(1, true);
			else if (name.contains("family"))
				mShellGroups.set(2, true);
			else if (name.contains("frat") || name.contains("sorority"))
				mShellGroups.set(3, true);
			else if (name.contains("school"))
				mShellGroups.set(4, true);
			else if (name.contains("roommate"))
				mShellGroups.set(5, true);
		}
		
		public void addListsToView()
		{
			if (mCurrentFragment.equals("SelectListFragment"))
			{
				GroupAdapter adapter = new GroupAdapter(getActivity(), R.layout.list_group,
						lists, photoURLs);
		        ListView lv = (ListView) getActivity().findViewById(R.id.groupList);
		        lv.setAdapter(adapter);
		        lv.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> a, View v, int position, long id) {
						System.out.println(mListId);
						mListName = lists[position];
						if (mListId != ids[position])
							mMemberIds = null;
						mListId = ids[position];
						mListener.onMemberListSelected();
					}
		        });
			}
			removeSpinner();
		}
		
		public void onPause()
		{
			super.onPause();
			mPlus = false;
		}
		
		//to prevent crashes when clicking the back button too fast
		public void onStop()
		{
			super.onStop();
			mCurrentFragment = "";
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
			getActivity().findViewById(R.id.button1).setVisibility(View.GONE);

			mNext = getResources().getString(R.string.action_next);
			getActivity().invalidateOptionsMenu();
			
			if (mListName != null)
			{
				EditText et = (EditText) getActivity().findViewById(R.id.editContactListName);
				et.setText(mListName);
			}
			if (mPublicList)
			{
				CheckBox checkbox = (CheckBox) getActivity().findViewById(R.id.publicListCheckBox);
				checkbox.setChecked(true);
			}
			ImageView view = (ImageView) getActivity().findViewById(R.id.image);
			view.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					getActivity().registerForContextMenu(v); 
				    getActivity().openContextMenu(v);
				    SubMenu s = mMenu.getItem(1).getSubMenu();
				    mMenu.performIdentifierAction(s.getItem().getItemId(), 0);
				    getActivity().unregisterForContextMenu(v);
				}
			});
			if (mContactListImage != null)
			{
				Picasso.with(getActivity()).load(mContactListImage.getUrl()).into(view);
				getActivity().findViewById(R.id.addPhoto).setVisibility(View.GONE);
			}
		}
		
		public void onPause()
		{
			super.onPause();
			EditText et = (EditText) getActivity().findViewById(R.id.editContactListName);
			mListName = et.getText().toString();
			CheckBox checkbox = (CheckBox) getActivity().findViewById(R.id.publicListCheckBox);
			mPublicList = checkbox.isChecked();

			InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
			      Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
		}
	}
	
	public static class SelectMembersFromContactsFragment extends Fragment implements OnQueryTextListener {

		// Following code mostly from http://stackoverflow.com/questions/18199359/how-to-display-contacts-in-a-listview-in-android-for-android-api-11
	    private ContactAdapter adapter;

	    // columns requested from the database
	    private static final String[] DISPLAY_NAME_PROJECTION = {
	        Contacts._ID, // _ID is always required
	        Contacts.DISPLAY_NAME, // that's what we want to display
	        Contacts.HAS_PHONE_NUMBER
	    };
	    
	    private static final String[] PHONE_PROJECTION = {
	    	Phone.TYPE,
	    	Phone.NUMBER,
	    	Phone.IS_PRIMARY
	    };
	    
	    private static final String[] STRUCTURED_NAME_PROJECTION = {
	    	StructuredName.GIVEN_NAME,
	    	StructuredName.FAMILY_NAME
	    };

	    protected String cursorFilter;

	    //indexes of shit in my contacts list for each row
	    protected static ArrayList<String> ids;
	    protected static ArrayList<String> numbers;
	    protected static ArrayList<String> displayNames;
	    protected static ArrayList<String> firstNames;
	    protected static ArrayList<String> lastNames;
	    
	    protected static ArrayList<String> selectedIds;

	    protected String search;

	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);

	        ids = new ArrayList<String>();
	        numbers = new ArrayList<String>();
	        displayNames = new ArrayList<String>();
	        firstNames = new ArrayList<String>();
	        lastNames = new ArrayList<String>();
	        selectedIds = new ArrayList<String>();
	        
	        ContentResolver resolver = getActivity().getContentResolver();
	        Cursor c = resolver.query(Contacts.CONTENT_URI, DISPLAY_NAME_PROJECTION, null, null,
	        		Phone.DISPLAY_NAME + " ASC");
	        int idNum = 0;
	        if (c != null && c.getCount() > 0)
	        {
	        	c.moveToPosition(-1);
	        	while(c.moveToNext())
	        	{
	        		if (Integer.parseInt(c.getString(c.getColumnIndex(Contacts.HAS_PHONE_NUMBER))) > 0)
	        		{
	        			String id = c.getString(c.getColumnIndex(Contacts._ID));
		        		Cursor pCur = resolver.query(
		        				Phone.CONTENT_URI,
		        				PHONE_PROJECTION,
		        				Phone.CONTACT_ID +" = ?",
		        				new String[]{id}, null);
		        		//only add their info if they do have a number!
		        		if (pCur != null && pCur.getCount() > 0)
		        		{
		        			String phone = null;
		        			pCur.moveToPosition(-1);
		        			while (pCur.moveToNext())
		        			{
		        				if (pCur.getInt(pCur.getColumnIndex(Phone.TYPE)) == Phone.TYPE_MOBILE)
		        				{
		        					phone = pCur.getString(pCur.getColumnIndex(Phone.NUMBER));
		        					break;
		        				}
		        			}
		        			//if they don't have a mobile but they do have another number
		        			if (phone == null && pCur.getCount() > 0)
		        			{
		        				pCur.moveToFirst();
		        				phone = pCur.getString(pCur.getColumnIndex(Phone.NUMBER));
		        			}
		        			//check to make sure we didn't fuck up and there is no phone
		        			if (phone != null)
		        			{
				        		ids.add(""+idNum);
				        		idNum++;
				        		numbers.add(phone);
				        		String displayName = c.getString(c.getColumnIndex(Contacts.DISPLAY_NAME));
				        		displayNames.add(displayName);
				        		Cursor nCur = resolver.query(
				        				ContactsContract.Data.CONTENT_URI,
				        				STRUCTURED_NAME_PROJECTION,
				        				ContactsContract.Data.MIMETYPE + " = ? AND " + 
				        						StructuredName.CONTACT_ID + " = ?",
				        				new String[]{StructuredName.CONTENT_ITEM_TYPE, id},
				        				StructuredName.GIVEN_NAME);
				        		String firstName = null;
				        		String lastName = null;
				        		if (nCur != null && nCur.getCount() > 0)
				        		{
				        			nCur.moveToFirst();
				        			firstName = nCur.getString(nCur.getColumnIndex
				        					(StructuredName.GIVEN_NAME));
				        			lastName = nCur.getString(nCur.getColumnIndex
				        					(StructuredName.FAMILY_NAME));
				        		}
				        		nCur.close();
				        		
			        			//if for some reason they don't have a first or a last name
			        			if (firstName == null && lastName == null)
			        			{
			        				firstNames.add(displayName);
			        				lastNames.add("");
			        			}
			        			else
			        			{
			        				if (firstName == null)
			        					firstNames.add("");
			        				else
			        					firstNames.add(firstName);
			        				if (lastName == null)
			        					lastNames.add("");
			        				else
			        					lastNames.add(lastName);
			        			}
		        			}
		        		}
		        		pCur.close();
	        		}
	        	}
	        }
	        c.close();
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

	        if (getActivity() instanceof NewInvitationActivity)
	        {
				mNext = getResources().getString(R.string.action_next);
				getActivity().invalidateOptionsMenu();
	        }
			
	        adapter = new ContactAdapter(getActivity(), R.layout.list_item_contact,
	        		displayNames, numbers, ids);
	        
	        // each time we are started use our listadapter
	        mListView = (ListView) getActivity().findViewById(R.id.contactsList);
	        System.out.println(mListView);
	        mListView.setAdapter(adapter);
	        mListView.setItemsCanFocus(false);
	        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	        adapter.notifyDataSetChanged();
	        /*
	        ((EditText) getActivity().findViewById(R.id.searchContacts))
	        	.addTextChangedListener(filterTextWatcher);*/
	        // and tell loader manager to start loading
	    }
    
	    public void onResume()
		{
			super.onResume();
			final SearchView sv = (SearchView) getActivity().findViewById(R.id.searchContacts);
			sv.setOnQueryTextListener(this);
			sv.setSubmitButtonEnabled(true);
			int searchCloseButtonId = sv.getContext().getResources()
	                .getIdentifier("android:id/search_close_btn", null, null);
			ImageView closeButton = (ImageView) sv.findViewById(searchCloseButtonId);
			
            // Set on click listener
            closeButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    //Find EditText view
        			int searchText = sv.getContext().getResources()
        	                .getIdentifier("android:id/search_src_text", null, null);
                    EditText et = (EditText) getActivity().findViewById(searchText);
                    
                    //Clear the text from EditText view
                    et.setText("");

                    //Clear query
                    search = "";
                    SparseBooleanArray checked = mListView.getCheckedItemPositions();
    	            for (int i = 0; i < mListView.getChildCount(); i++) {
    	                String id = ((TextView) mListView.getChildAt(i).findViewById(R.id.id))
    	                		.getText().toString();
    	                if (checked.get(i) && !selectedIds.contains(id))
    	                    selectedIds.add(id);
    	                else if (!checked.get(i) && selectedIds.contains(id))
    	                	selectedIds.remove(id);
    	            }
    	            adapter.getFilter().filter("");
                    adapter.notifyDataSetChanged();
    	            //then filter the results
    	            adapter.getFilter().filter("", new Filter.FilterListener() {
    	                public void onFilterComplete(int count) {
    	                    adapter.notifyDataSetChanged();
    	                    
    	                    for (int i = 0; i < mListView.getChildCount(); i ++) {
    	                    	System.out.println("Index is " + i);
    	                        // if the current (filtered) 
    	                        // listview you are viewing has the name included in the list,
    	                        // check the box
    	                    	String id = ((TextView) mListView.getChildAt(i).findViewById(R.id.id))
    	                        		.getText().toString();
    	                        if (selectedIds.contains(id)) {
    	                        	mListView.setItemChecked(i, true);
    	                        } else {
    	                        	mListView.setItemChecked(i, false);
    	                        }
    	                    }
    	
    	                }
    	            });   
                }
            });
			
			getActivity().setTitle(getResources().getString(R.string.title_select_members_from_contacts));
			if (getActivity() instanceof NewInvitationActivity)
				mCurrentFragment = "SelectMembersFromContactsFragment";
		}
	    /*
	    private TextWatcher filterTextWatcher = new TextWatcher() {

	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	            SparseBooleanArray checked = mListView.getCheckedItemPositions();
	            for (int i = 0; i < mListView.getCount(); i++) {
                    String id = ((TextView) mListView.getChildAt(i).findViewById(R.id.id))
                    		.getText().toString();
	                if (checked.get(i) && !selectedIds.contains(id))
	                    selectedIds.add(id);
	                else if (!checked.get(i) && selectedIds.contains(id))
	                	selectedIds.remove(id);
	            }           
	        } //<-- End of beforeTextChanged

	        public void onTextChanged(CharSequence s, int start, int before, int count) {           
	            adapter.getFilter().filter(s);              
	        } //<-- End of onTextChanged

	        public void afterTextChanged(Editable s) {

	            adapter.getFilter().filter(s, new Filter.FilterListener() {
	                public void onFilterComplete(int count) {
	                    adapter.notifyDataSetChanged();
	                    System.out.println("count is " + mListView.getCount());
	                    for (int i = 0; i < mListView.getChildCount(); i ++) {
	                    	System.out.println("Index is " + i);
	                        // if the current (filtered) 
	                        // listview you are viewing has the name included in the list,
	                        // check the box
	                    	String id = ((TextView) mListView.getChildAt(i).findViewById(R.id.id))
	                        		.getText().toString();
	                    	System.out.println(id);
	                        if (selectedIds.contains(id)) {
	                        	mListView.setItemChecked(i, true);
	                        } else {
	                        	mListView.setItemChecked(i, false);
	                        }
	                    }

	                }
	            });         
	        } //<-- End of afterTextChanged

	    }; //<-- End of TextWatcher
	    */

		@Override
		public boolean onQueryTextChange(String newText) {
			return true;
		}

		//search now!
		@Override
		public boolean onQueryTextSubmit(String query) {
			//first save current checks
			if (search == null || search.compareTo(query) != 0)
			{
				search = query;
				SparseBooleanArray checked = mListView.getCheckedItemPositions();
	            for (int i = 0; i < mListView.getChildCount(); i++) {
	                String id = ((TextView) mListView.getChildAt(i).findViewById(R.id.id))
	                		.getText().toString();
	                if (checked.get(i) && !selectedIds.contains(id))
	                    selectedIds.add(id);
	                else if (!checked.get(i) && selectedIds.contains(id))
	                	selectedIds.remove(id);
	            }
	            for (int i = 0; i < selectedIds.size(); i++)
	            {
	            String id = selectedIds.get(i);
				int index = ids.indexOf(id);
				System.out.println("First name of " + id + " is " + firstNames.get(index));
	            }
	            adapter.getFilter().filter(query);
                adapter.notifyDataSetChanged();
	            //then filter the results
	            adapter.getFilter().filter(query, new Filter.FilterListener() {
	                public void onFilterComplete(int count) {
	                    adapter.notifyDataSetChanged();
	                    
	                    for (int i = 0; i < mListView.getChildCount(); i ++) {
	                    	System.out.println("Index is " + i);
	                        // if the current (filtered) 
	                        // listview you are viewing has the name included in the list,
	                        // check the box
	                    	String id = ((TextView) mListView.getChildAt(i).findViewById(R.id.id))
	                        		.getText().toString();
	                        if (selectedIds.contains(id)) {
	                        	mListView.setItemChecked(i, true);
	                        } else {
	                        	mListView.setItemChecked(i, false);
	                        }
	                    }
	
	                }
	            });   
			}
			return true;
		}
		
		public static void saveContacts()
		{
			//save all the latest checkmarks
			SparseBooleanArray checked = mListView.getCheckedItemPositions();
            for (int i = 0; i < mListView.getCount(); i++) {
                String id = (String) mListView.getItemAtPosition(i);
                if (checked.get(i) && !selectedIds.contains(id))
                    selectedIds.add(id);
                else if (!checked.get(i) && selectedIds.contains(id))
                	selectedIds.remove(id);
            }
            
			mMemberFirstNames = new String[selectedIds.size()];
			mMemberLastNames = new String[selectedIds.size()];
			mPhoneNumbers = new String[selectedIds.size()];
			mPreviewName = firstNames.get(ids.indexOf(selectedIds.get(0)));
			for (int i = 0; i < selectedIds.size(); i++)
			{
				String id = selectedIds.get(i);
				int index = ids.indexOf(id);
				mMemberFirstNames[i] = firstNames.get(index);
				mMemberLastNames[i] = lastNames.get(index);
				mPhoneNumbers[i] = numbers.get(index);
			}
		}
		
		//so i can reuse this for new contact list activity
		//remember to call saveContacts first for these to work!
		public static int getCheckedCount()
		{
			return mMemberFirstNames.length;
		}
		
		public static String getFirstName(int index)
		{
			return mMemberFirstNames[index];
		}
		
		public static String getLastName(int index)
		{
			return mMemberLastNames[index];
		}
		
		public static String getPhoneNumber(int index)
		{
			return mPhoneNumbers[index];
		}
	}
	
	public static class SelectMembersFromListFragment extends ProgressFragment {
		
		// and name should be displayed in the text1 textview in item layout
		private static String[] names;
		private static String[] photoURLs;
		private static ArrayList<String> ids;
		private static JSONArray users;
		private ArrayAdapter<String> mArrayAdapter;
		protected static String[] responseRates;
		
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
		
		public void onResume()
		{
			super.onResume();
			showSpinner();

			getActivity().setTitle(getResources().getString(R.string.title_select_members_from_list));
			mCurrentFragment = "SelectMembersFromListFragment";
			
			mNext = getResources().getString(R.string.action_next);
			getActivity().invalidateOptionsMenu();

		    ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("ContactList");
		    query.whereEqualTo("objectId", mListId.getObjectId());
		    query.getFirstInBackground(new GetCallback<ParseObject>() {
				public void done(ParseObject contactList, ParseException e) {
					if (e == null)
					{
						System.out.println("I AIN'T BROKE");
						mListId = contactList;
						JSONArray members = contactList.getJSONArray("members");
						int length = members.length();
						String currentUserId = ParseUser.getCurrentUser().getObjectId();
						//if the user isn't a member, then we need to remove the member
						//from the list and add owner
						boolean ownerIsUser = false;
						if (currentUserId.compareTo(contactList.getParseUser("owner")
								.getObjectId()) == 0)
						{
							ownerIsUser = true;
						}
						names = new String[length];
						ids = new ArrayList<String>();
						int empty = 0;
						//put all member names and ids into respective arrays
						for (int i = 0; i < members.length(); i++)
						{
							if (!(ownerIsUser || !memberIsUser(members, i)))
								empty = i;
							try {
								ids.add(members.getJSONObject(i).getString("objectId"));
							} catch (JSONException e1) {
								e1.printStackTrace();
							}
								
						}
						//add the creator now -- it's not a json object
						if (!ownerIsUser)
							ids.set(empty, contactList.getParseUser("owner").getObjectId());
						fetchNames();
					}
					else
						showParseException(e);
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
			if (ids.size() == 0)
			{
				Toast.makeText(getActivity(), "Oops! This group is empty. Deleting group...", Toast.LENGTH_LONG).show();
				mListId.deleteInBackground(new DeleteCallback() {
					@Override
					public void done(ParseException e) {
						if (e != null)
							showParseException(e);
						removeSpinner();
						getActivity().finish();
					}
				});
				return;
			}
			ArrayList<ParseQuery<ParseUser>> queries = new ArrayList<ParseQuery<ParseUser>>();
			for (int i = 0; i < ids.size(); i++)
			{
				ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
				userQuery.whereEqualTo("objectId", ids.get(i));
				queries.add(userQuery);
				System.out.println("Second iteration: id " + i + " is " + ids.get(i));
			}
			if (queries.size() == 0)
			{
				Toast.makeText(getActivity(), "Odd, an empty group?", Toast.LENGTH_SHORT).show();
				return;
			}
			ParseQuery<ParseUser> mainQuery = ParseQuery.or(queries);
			mainQuery.orderByAscending("firstName");
			mainQuery.addAscendingOrder("lastName");
			mainQuery.findInBackground(new FindCallback<ParseUser>() {
				public void done(List<ParseUser> userList, ParseException e) {
					if (e == null)
					{
						names = new String[userList.size()];
						responseRates = new String[userList.size()];
						photoURLs = new String[userList.size()];
						//we want a JSONArray of users, not just of userIds
						users = new JSONArray();
						for (int i = 0; i < userList.size(); i++)
						{
							names[i] = userList.get(i).getString("firstName") + " " + 
										userList.get(i).getString("lastName");
							if (userList.get(i).containsKey("profilePhoto"))
								photoURLs[i] = userList.get(i).getParseFile("profilePhoto").getUrl();
							if (userList.get(i).containsKey("responseRate"))
								responseRates[i] = "" + (Math.round(userList.get(i)
										.getDouble("responseRate")*1000))/10.0+"%";
							else
								responseRates[i] = "100%";
							//because the ids are no longer in the same order as they used to be!
							ids.set(i, userList.get(i).getObjectId());
							users.put(userList.get(i));
							System.out.println(names[i]);
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
		    	removeSpinner();
			    if (mArrayAdapter == null)
				    mArrayAdapter = new GroupAdapter(getActivity(), R.layout.list_item_select_members,
							names, responseRates, photoURLs);
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
				if (mMemberIds != null)
					for (int i = 0; i < mMemberIds.length; i++)
						mListView.setItemChecked(ids.indexOf(mMemberIds[i]), true);
				else
				{
					mCheckbox.setChecked(true);
					for ( int i = 0; i < mListView.getCount(); i++)
						mListView.setItemChecked(i, true);
				}
		    }
		}
		
		public static void saveMembers()
		{
			SparseBooleanArray checked = ((ListView) context.findViewById(R.id.memberList))
					.getCheckedItemPositions();
			int count = ((ListView) context.findViewById(R.id.memberList)).getCheckedItemCount();
			mMemberIds = new String[count];
			mMembers = new JSONArray();
			mMemberObjectIds = new JSONArray();
			mPreviewName = null;
			System.out.println("Count is: " + count);
			int j = 0;
			for (int i = 0; i < checked.size(); i++)
			{
				int index = checked.indexOfKey(i);
				if (checked.get(index))
				{
					System.out.println("Index is checked " + index);
					if (mPreviewName == null)
					{
						mPreviewName = names[index];
						if (mPreviewName.indexOf(' ') > -1)
			        		mPreviewName = mPreviewName.substring(0, mPreviewName.indexOf(' '));
					}
					mMemberIds[j] = ids.get(index);
					j++;
					System.out.println(names[index]);
					try {
						mMembers.put(users.get(index));
						mMemberObjectIds.put(ids.get(index));
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public static class EditAgendaFragment extends Fragment
	{
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState)
		{
			View rootView = inflater.inflate(R.layout.fragment_new_agenda,
					container, false);
			if (mAfterFinalEdit)
			{
				mNext = "Save";
				getActivity().invalidateOptionsMenu();
			}
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
		
		public static void saveAgenda()
		{
			EditText et = (EditText)(context.findViewById(R.id.editAgenda));
			mAgenda = et.getText().toString();
		}
		
		public void onPause()
		{
			super.onPause();
			EditText et = (EditText)(getActivity().findViewById(R.id.editAgenda));
			InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
			      Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
		}
	}
	
	public static class ChooseLocationFragment extends ProgressFragment implements OnQueryTextListener{

		private static ListView lv;
		static Activity context;
		private static String search;

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
			context = getActivity();
			context.setTitle(getResources().getString(R.string.title_choose_location));
			mCurrentFragment = "ChooseLocationFragment";
			System.out.println("Executing query sushi");
			search = "";
			new AccessFoursquareVenues(this).execute("");
			SearchView sv = (SearchView) context.findViewById(R.id.searchLocation);
			sv.setOnQueryTextListener(this);
			sv.setSubmitButtonEnabled(true);
			
			//get the list so i can access it later
			lv = (ListView) context.findViewById(R.id.locationList);
		}

		public static void makeList(final JSONArray array) {

			final String[] names;
			//array won't show last spot of the searched array because of create new location
			if (search == "")
				names = new String[array.length()];
			else
				names = new String[array.length() + 1];
			final String[] addresses = new String[array.length()];
			JSONArray formattedAddress;
			String address;
			System.out.println("Size of the array is " + array.length());
			for (int i = 0; i < array.length(); i++)
			{
				try {
					names[i] = array.getJSONObject(i).getString("name");
					System.out.println(names[i]);
					formattedAddress = array.getJSONObject(i).getJSONObject("location")
							.getJSONArray("formattedAddress");
					address = "";
					for (int j = 0; j < formattedAddress.length(); j++)
					{
						address += formattedAddress.getString(j);
						if (j != formattedAddress.length() - 1)
							address += ", ";
					}
					addresses[i] = address;
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			System.out.println("Got names");
			if (search != "")
				names[array.length()] = "";
			ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context,
					R.layout.list_item_location, R.id.text1, names) {
				
				  @Override
				  public View getView(int position, View convertView, ViewGroup parent) {
					if (search == "")
					{
					    View view = super.getView(position, convertView, parent);
					    TextView text1 = (TextView) view.findViewById(R.id.text1);
					    TextView text2 = (TextView) view.findViewById(R.id.text2);
	
					    text1.setText(names[position]);
					    text2.setText(addresses[position]);
					    
					    text1.setMaxLines(1);
					    text2.setMaxLines(1);
					    
					    text1.setEllipsize(TextUtils.TruncateAt.END);
					    text2.setEllipsize(TextUtils.TruncateAt.END);
					    
					    System.out.println(text1.getText());
					    System.out.println(text2.getText());
					    System.out.println(position);
					    return view;
					}
					//or we need to include custom locations
					else
					{
						if (position == 0){
							convertView = ((LayoutInflater) context.getSystemService
									(Context.LAYOUT_INFLATER_SERVICE))
									.inflate(R.layout.list_item_new_location, null);
							return convertView;
						}
						else
						{
							View view = super.getView(position, null, parent);
						    TextView text1 = (TextView) view.findViewById(R.id.text1);
						    TextView text2 = (TextView) view.findViewById(R.id.text2);
		 
						    if (text1 != null)
						    {
						    	text1.setText(names[position-1]);
							    text1.setMaxLines(1);
							    text1.setEllipsize(TextUtils.TruncateAt.END);
						    }
						    if (text2 != null)
						    {
						    	text2.setText(addresses[position-1]);
							    text2.setMaxLines(1);
							    text2.setEllipsize(TextUtils.TruncateAt.END);
						    }
						    
						    return view;
						}
					}
				  }
			};
	        lv.setAdapter(arrayAdapter);
	        lv.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> a, View v, int position, long id) {
					//custom location
					if (position == 0 && search.compareTo("") != 0)
					{
						mVenueInfo = search;
						mVenue = new JSONObject();
						try {
							mVenue.put("_customVenue", true);
						mVenue.put("name", search);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					//non custom location
					else
					{
						//forget the custom location thing if necessary
						if (search.compareTo("") != 0)
							position--;
						mVenueInfo = names[position];
						try {
							mVenue = array.getJSONObject(position);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					if (mAfterFinalEdit)
						context.getFragmentManager().popBackStack();
					else
						startSetDeadlineFragment();
				}
	        });
	        System.out.println("Done");
		}

		public static void startSetDeadlineFragment()
		{
			FragmentTransaction transaction = context.getFragmentManager().beginTransaction();
			transaction.replace(R.id.container, new SetDeadlineFragment());
			transaction.addToBackStack(null);
			transaction.commit();
		}
		
		public void onStop()
		{
			removeSpinner();
			InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
			      Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(context.findViewById(R.id.searchLocation)
					.getWindowToken(), 0);
			super.onStop();
		}

		@Override
		public boolean onQueryTextChange(String newText) {
			return true;
		}

		//search now!
		@Override
		public boolean onQueryTextSubmit(String query) {
			search = query;
			new AccessFoursquareVenues(this).execute(query);
			return true;
		}
	}
	
	public static class SetDeadlineFragment extends Fragment {

		public TimePicker.OnTimeChangedListener mTimeListener;
		public static int expiresAtHour;
		public static int expiresAtMinute;

		private String TODAY;
		private String TOMORROW;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			if (mAfterFinalEdit)
			{
				mNext = "Save";
				getActivity().invalidateOptionsMenu();
			}
			View rootView = inflater.inflate(R.layout.fragment_set_deadline,
					container, false);
			TODAY = getActivity().getResources().getString(R.string.today);
			TOMORROW = getActivity().getResources().getString(R.string.tomorrow);
			mTimeListener = new TimePicker.OnTimeChangedListener() {
				@Override
				public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
					TextView tv = (TextView) getActivity().findViewById(R.id.deadlineTime);
					expiresAtHour = hourOfDay;
					expiresAtMinute = minute;
					changeToDate(expiresAtHour, expiresAtMinute);
					String day = TODAY;
					if (!mToday)
						day = TOMORROW;
					tv.setText(formatTime(hourOfDay, minute) + " " + day);
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
				mExpiresAtHour = tp.getCurrentHour() + 3;
			}
			tp.setCurrentHour(mExpiresAtHour);
			tp.setCurrentMinute(mExpiresAtMinute);
			tp.setOnTimeChangedListener(mTimeListener);
		
			TextView tv = (TextView) getActivity().findViewById(R.id.deadlineTime);
			String day = TODAY;
			if (!mToday)
				day = TOMORROW;
			tv.setText(formatTime(tp.getCurrentHour(), tp.getCurrentMinute()) + " " + day);
			
			expiresAtHour = mExpiresAtHour;
			expiresAtMinute = mExpiresAtMinute;
		}
		
		public static void saveTime()
		{
			mExpiresAtHour = expiresAtHour;
			mExpiresAtMinute = expiresAtMinute;
		}
	}
	
	public static class StartTimeFragment extends Fragment {
		
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_start_time,
					container, false);
			mCurrentFragment = "StartTimeFragment";
			mNext = getActivity().getString(R.string.save);
			getActivity().invalidateOptionsMenu();
			return rootView;
		}
		
		public void onResume()
		{
			super.onResume();
			DatePicker datePicker = (DatePicker) getActivity().findViewById(R.id.datePicker);
			//TODO this will make sure you can't set the date to before the rsvp deadline
			//I think you can still set it a couple of hours ahead tho
			//oh well
			datePicker.setMinDate(new Date().getTime()-1000*60*60*24);
			datePicker.setMaxDate(new Date().getTime()+1000*60*60*24*7);
			TimePicker timePicker = (TimePicker) getActivity().findViewById(R.id.timePicker);

			Calendar c = Calendar.getInstance();
			if (mStartDeadline != null)
				c.setTime(mStartDeadline);
			else
				c.setTime(changeToDate(mExpiresAtHour, mExpiresAtMinute));
			
			//TODO MAKE EVERYTHING USE CALENDAR IT'S BEAUTIFUL
			datePicker.updateDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH),
					c.get(Calendar.DAY_OF_MONTH));
			timePicker.setCurrentHour(c.get(Calendar.HOUR_OF_DAY));
			timePicker.setCurrentMinute(c.get(Calendar.MINUTE));
		}
		
		public static void saveStartTime()
		{
			DatePicker dp = (DatePicker) context.findViewById(R.id.datePicker);
			TimePicker tp = (TimePicker) context.findViewById(R.id.timePicker);
			GregorianCalendar gc = new GregorianCalendar(dp.getYear(), dp.getMonth(), dp.getDayOfMonth(),
					tp.getCurrentHour(), tp.getCurrentMinute());
			mStartDeadline = gc.getTime();
		}
	}
	
	public static class FinalEditFragment extends Fragment {

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
			getActivity().setTitle(getResources().getString(R.string.title_final_edit));
			mCurrentFragment = "FinalEditFragment";
			mAfterFinalEdit = false;

			mNext = getResources().getString(R.string.send);
			getActivity().invalidateOptionsMenu();
			
			TextView tv = (TextView) getActivity().findViewById(R.id.finalEditToText);
			if (mPreviewName == null || mPreviewName == "")
				mPreviewName = "1 person";
			if (mMakeContactList)
				if (mPhoneNumbers.length != 1)
					tv.setText(mPhoneNumbers.length + " people");
				else
					tv.setText(mPreviewName);
			else if (mMemberIds == null)
				if (mMembers.length() != 1)
					tv.setText(mListName + " (" + mMembers.length() + " people)");
				else
					tv.setText(mListName + " (" + mPreviewName + ")");
			else
				if (mMemberIds.length != 1)
					tv.setText(mListName + " (" + mMemberIds.length + " people)");
				else
					tv.setText(mListName + " (" + mPreviewName + ")");
			tv = (TextView) getActivity().findViewById(R.id.finalEditAgendaText);
			tv.setText(mAgenda);
			tv = (TextView) getActivity().findViewById(R.id.finalEditLocationText);
			tv.setText(mVenueInfo);
			tv = (TextView) getActivity().findViewById(R.id.finalEditDeadlineText);
			tv.setText(formatTime(mExpiresAtHour, mExpiresAtMinute));
			tv = (TextView) getActivity().findViewById(R.id.finalEditPhotoText);
			int photos = mVenuePhotoUrls.length();
			if (photos > 1)
				tv.setText(mVenuePhotoUrls.length() + " photos");
			else if (photos == 1)
				tv.setText("1 photo");
			tv = (TextView) getActivity().findViewById(R.id.finalEditStartTimeText);
			if (mStartDeadline != null)
			{
				Time t = new Time();
				t.set(mStartDeadline.getTime());
				tv.setText(t.format("%a, %b %e %I:%M %p"));
			}
			else
				tv.setText(getActivity().getString(R.string.no_start_time));
			tv = (TextView) getActivity().findViewById(R.id.finalEditLimitText);
			if (mLimit == 0)
				tv.setText(getActivity().getString(R.string.no_limit));
			else
				tv.setText(""+mLimit);
			CheckBox box = (CheckBox) getActivity().findViewById(R.id.finalEditInviteMoreBox);
			box.setChecked(mInviteMore);
			
			getActivity().findViewById(R.id.finalEditTo).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					mAfterFinalEdit = true;
					FragmentTransaction transaction = getActivity().getFragmentManager()
							.beginTransaction();
					transaction.replace(R.id.container, new SelectListFragment());
					transaction.addToBackStack(null);
					transaction.commit();
				}
			});
			
			getActivity().findViewById(R.id.finalEditAgenda).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					mAfterFinalEdit = true;
					FragmentTransaction transaction = getActivity().getFragmentManager()
							.beginTransaction();
					transaction.replace(R.id.container, new EditAgendaFragment());
					transaction.addToBackStack(null);
					transaction.commit();
				}
			});

			getActivity().findViewById(R.id.finalEditLocation).setOnClickListener(
					new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					mAfterFinalEdit = true;
					FragmentTransaction transaction = getActivity().getFragmentManager()
							.beginTransaction();
					transaction.replace(R.id.container, new ChooseLocationFragment());
					transaction.addToBackStack(null);
					transaction.commit();
				}
			});
			
			getActivity().findViewById(R.id.finalEditDeadline).setOnClickListener(
					new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					mAfterFinalEdit = true;
					FragmentTransaction transaction = getActivity().getFragmentManager()
							.beginTransaction();
					transaction.replace(R.id.container, new SetDeadlineFragment());
					transaction.addToBackStack(null);
					transaction.commit();
				}
			});
			
			getActivity().findViewById(R.id.finalEditPhoto).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					getActivity().registerForContextMenu(v); 
				    getActivity().openContextMenu(v);
				    getActivity().unregisterForContextMenu(v);
				}
			});
			
			getActivity().findViewById(R.id.finalEditStartTime).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mAfterFinalEdit = true;
					FragmentTransaction transaction = getActivity().getFragmentManager()
							.beginTransaction();
					transaction.replace(R.id.container, new StartTimeFragment());
					transaction.addToBackStack(null);
					transaction.commit();
				}
			});
			
			getActivity().findViewById(R.id.finalEditLimit).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mAfterFinalEdit = true;
					FragmentTransaction transaction = getActivity().getFragmentManager()
							.beginTransaction();
					transaction.replace(R.id.container, new LimitFragment());
					transaction.addToBackStack(null);
					transaction.commit();
				}
			});
		}
		
		//if we go back in the flow, we gotta readd the next button
		//also gotta save the invite more box cause this is the only screen it saves on
		public void onDestroyView()
		{
			mInviteMore = ((CheckBox) getActivity().findViewById(R.id.finalEditInviteMoreBox)).isChecked();
			super.onDestroyView();
			if (!mAfterFinalEdit)
			{
				mNext = getResources().getString(R.string.action_next);
				getActivity().invalidateOptionsMenu();
			}
		}
	}
	
	public static class LimitFragment extends ProgressFragment
	{
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_limit,
					container, false);
			mCurrentFragment = "LimitFragment";
			mNext = "Save";
			getActivity().invalidateOptionsMenu();
			
			NumberPicker limit = (NumberPicker) rootView.findViewById(R.id.limit);
			limit.setMaxValue(250);
			limit.setMinValue(0);
			limit.setWrapSelectorWheel(false);
			limit.setValue(mLimit);
			String[] values = new String[251];
			values[0] = getActivity().getString(R.string.no_limit);
			for (int i = 1; i <= 250; i++)
				values[i] = "" + i;
			limit.setDisplayedValues(values);
			return rootView;
		}
		
		public static void saveLimit()
		{
			NumberPicker limit = (NumberPicker) context.findViewById(R.id.limit);
			mLimit = limit.getValue();
			saveMaxAttendees();
		}
		
		public static void saveMaxAttendees()
		{
			mSpotsLeft = mLimit;
			mIsFull = false;
		}
	}
	
	public static class ImageListFragment extends AbstractImageListFragment {

		public void onResume()
		{
			super.onResume();

			getActivity().setTitle(getResources().getString(R.string.title_final_edit));
			mCurrentFragment = "ImageListFragment";
			mAfterFinalEdit = false;
			
			setSave(false);

			mNext = getResources().getString(R.string.action_done);
			getActivity().invalidateOptionsMenu();
		}
		
		@Override
		public String getVenueId() {
			String id = null;
			try {
				id = mVenue.getString("id");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return id;
		}

		public void saveUrls(JSONArray urls) {
			for (int i = 0; i < urls.length(); i++)
			{
				try {
					System.out.println("adding image " + i);
					addImage(urls.getString(i));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static class PreviewFragment extends Fragment {
		
		private Timer timer;
		public static Handler mHandler;
		
		public PreviewFragment(){
		}
		
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_preview,
					container, false);
			getActivity().setTitle(getActivity().getString(R.string.preview));
			mCurrentFragment = "PreviewFragment";
			getActivity().invalidateOptionsMenu();
			return rootView;
		}
		
		public void onResume()
		{	
			super.onResume();
			LinearLayout lin = (LinearLayout) getActivity().findViewById(R.id.invitations);
			View event = View.inflate(getActivity(), R.layout.invite_card, null);
			TextView tv = (TextView) event.findViewById(R.id.creator);
			tv.setText(ParseUser.getCurrentUser().getString("firstName") + "  " +
					ParseUser.getCurrentUser().getString("lastName"));
			tv = (TextView) event.findViewById(R.id.agenda);
			if (mPreviewName != "1 person")
				tv.setText(mPreviewName + ", " + mAgenda);
			else
				tv.setText(mAgenda);
			tv = (TextView) event.findViewById(R.id.venueInfo);
			tv.setText(mVenueInfo);
			ImageView v = ((ImageView) event.findViewById(R.id.image));
			if (mVenuePhotoUrls.length() > 0)
			{
				try {
					Picasso.with(getActivity()).load(mVenuePhotoUrls.getString(0)).into(v);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			else
				Picasso.with(getActivity()).load(R.drawable.logo).into(v);
			
			timer = new Timer("RSVPTimer");
			final Date expiresAt = changeToDate(mExpiresAtHour, mExpiresAtMinute);
			final TextView mTimeToRSVPView = (TextView) event.findViewById(R.id.timer);
			mHandler = new Handler() {
				final String LEFT_TO_RSVP = getActivity().getString(R.string.leftToRSVP);
				public void handleMessage(Message message)
				{
					Date now = new Date();
					long timeToRSVP = expiresAt.getTime() - now.getTime();
					String time = "" + (int)timeToRSVP/(60*60*1000) + ":";
					int minutes = (int)(timeToRSVP/(60*1000))%60;
					if (minutes < 10)
						time = time + "0";
					time = time + minutes + ":";
					int seconds = (int)(timeToRSVP/1000)%60;
					if (seconds < 10)
						time = time+ "0";
					time = time + seconds + " " + LEFT_TO_RSVP;
					if (mLimit > 0)
						time += " (" + getResources().getQuantityString(R.plurals.spotsLeft,
								mSpotsLeft, mSpotsLeft) + ")";
					mTimeToRSVPView.setText(time);
					mTimeToRSVPView.invalidate();
					mTimeToRSVPView.requestLayout();
					//invalidate();
					//requestLayout();
				}
			};
			timer.schedule(new RSVPTimerTask(), 10, 1000);
			Button button = (Button) event.findViewById(R.id.no);
			button.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),
					 "fonts/fontawesome-webfont.ttf"));
			button = (Button) event.findViewById(R.id.yes);
			button.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),
					 "fonts/fontawesome-webfont.ttf"));
			
			LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			lp.setMargins(20, 20, 20, 20);
			event.setLayoutParams(lp);
			
			lin.addView(event);
		}
		
		public void onStop()
		{
			super.onStop();
			timer.cancel();
		}
		
		public class RSVPTimerTask extends TimerTask
		{
			
			@Override
			public void run() {
				//System.out.println(pos);
				mHandler.obtainMessage(1).sendToTarget();
			}
		}
	}
	
	public static class AskToSaveListFragment extends DialogFragment
	{
		private boolean clearAll;
		
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			clearAll = true;
		    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		    builder.setTitle(R.string.ask_to_save_group)
		           .setItems(R.array.ask_to_save_group, new DialogInterface.OnClickListener() {
		               public void onClick(DialogInterface dialog, int which) {
		            	   if (which == 0)
		            	   {
		            		   clearAll = false;
		            		   FragmentTransaction transaction = getFragmentManager().beginTransaction();
		            		   transaction.add(R.id.container, new GroupSummaryFragment());
		            		   transaction.addToBackStack(null);
		            		   transaction.commit();
		            		   
		            	   }
		            	   else if (which == 1)
		            	   {
		            		   clearAll = false;
		            		   FragmentTransaction transaction = getFragmentManager().beginTransaction();
		            		   transaction.add(R.id.container, new ChooseFromExistingList());
		            		   transaction.addToBackStack(null);
		            		   transaction.commit();
		            	   }
		            	   else
		            	   {
		            		    Intent intent = new Intent(getActivity(), RSVPEventsActivity.class);
		            			startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
		            			getActivity().finish();
		            	   }
		           }
		    });
		    return builder.create();
		}
		
		public void onStop()
		{
			super.onStop();
			if (clearAll)
			{
				Intent intent = new Intent(getActivity(), RSVPEventsActivity.class);
    			startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    			getActivity().finish();
			}
		}
	}
	
	public static class GroupSummaryFragment extends Fragment
	{
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_contact_list_info,
					container, false);
			
			popped = false;
			hasChangedName = false;
			mCurrentFragment = "GroupSummaryFragment";
			mListName = "New Group";
			mNext = "Save";
			getActivity().invalidateOptionsMenu();
			getActivity().setTitle(getActivity().getString(R.string.title_create_list));
			rootView.findViewById(R.id.editGroup).setVisibility(View.VISIBLE);
			
			ImageView image = (ImageView) rootView.findViewById(R.id.imageView1);
			image.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					getActivity().registerForContextMenu(v); 
				    getActivity().openContextMenu(v);
				    SubMenu s = mMenu.getItem(1).getSubMenu();
				    mMenu.performIdentifierAction(s.getItem().getItemId(), 0);
				    getActivity().unregisterForContextMenu(v);
				}
			});
			TypedArray array = getResources().obtainTypedArray(R.array.contact_list_images);
			Picasso.with(getActivity()).load(array.getResourceId(Math.abs(mListName.hashCode())
					% 12,R.drawable.color_balloon_8)).resize(140, 140).into(image);
			array.recycle();
			
			((TextView) rootView.findViewById(R.id.listName)).setText(mListName);
			
			LinearLayout ll = (LinearLayout) rootView.findViewById(R.id.linearLayout);
			ll.removeAllViews();
			LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, 1);
			
			//first add the creator
			View member = View.inflate(getActivity(), R.layout.list_members, null);
			ParseUser user = ParseUser.getCurrentUser();
			((TextView) member.findViewById(R.id.name)).setText(user.getString("firstName") +
					" " + user.getString("lastName"));
			((TextView) member.findViewById(R.id.responseRate)).setText("" + 
					(Math.round(user.getDouble("responseRate")*1000)/10.0) + "%");
			if (user.containsKey("profilePhoto"))
				Picasso.with(getActivity()).load(user.getParseFile("profilePhoto").getUrl())
					.resize(140, 140).into((ImageView) member.findViewById(R.id.imageView1));
			ll.addView(member, 0);
			View line = new View(getActivity());
			line.setBackgroundColor(getActivity().getResources().getColor(R.color.lightGray));
			line.setLayoutParams(lp);
			ll.addView(line, 1);
			
			for (int i = 0; i < mParseUsers.size(); i++)
			{
				member = View.inflate(getActivity(), R.layout.list_members, null);
				user = mParseUsers.get(i);
				((TextView) member.findViewById(R.id.name)).setText(user.getString("firstName") +
						" " + user.getString("lastName"));
				if (user.containsKey("responseRate"))
					((TextView) member.findViewById(R.id.responseRate)).setText("" + 
							(Math.round(user.getDouble("responseRate")*1000)/10.0) + "%");
				else
					((TextView) member.findViewById(R.id.responseRate)).setText("100.0%");
				if (user.containsKey("profilePhoto"))
					Picasso.with(getActivity()).load(user.getParseFile("profilePhoto").getUrl())
						.resize(140, 140).into((ImageView) member.findViewById(R.id.imageView1));
				ll.addView(member, 2*i + 2);
				line = new View(getActivity());
				line.setBackgroundColor(getActivity().getResources().getColor(R.color.lightGray));
				line.setLayoutParams(lp);
				ll.addView(line, 2*i + 3);
			}
			ll.invalidate();
			ll.requestLayout();
			((ProgressActivity) getActivity()).removeSpinner();
			
			if (mListName.equals("New Group"))
			{
			    DialogFragment newFragment = new ChangeNameFragment();
			    newFragment.show(getFragmentManager(), null);
			}
			
			return rootView;
		}
		
		public void onDestroy()
		{
			if (!popped)
			{
				Intent intent = new Intent(getActivity(), RSVPEventsActivity.class);
				startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
				getActivity().finish();
			}
			super.onDestroy();
		}
	}
	
	public static class ChangeNameFragment extends DialogFragment {
		
		Dialog dialog;
		
	    @Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	        // Use the Builder class for convenient dialog construction
	        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	        builder.setView(getActivity().getLayoutInflater().inflate
	        			(R.layout.fragment_change_group_name, null))
	        	   .setTitle("Set group name")
	               .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface d, int id) {
	                	   String str = ((EditText) dialog.findViewById(R.id.changeName))
	                			   .getText().toString();
	                	   if (str.replaceAll("\\s+", "").equals(""))
	                	   {
	                		   mListName = str;
		                	   ((TextView) getActivity().findViewById(R.id.listName)).setText(mListName);
		                	   hasChangedName = true;
	                	   }
	                   }
	               });
	        if (hasChangedName)
	        {
	        	builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	           	    	ChangeNameFragment.this.getDialog().cancel();
	                   }
	               });
	        }
	        dialog = builder.create();
	        // Create the AlertDialog object and return it
	        return dialog;
	    }
	    
	    public void onResume()
	    {
	    	super.onResume();
	    	((EditText) dialog.findViewById(R.id.changeName)).setText(mListName);
	    }
	}
	
	public static class ChooseFromExistingList extends SelectListFragment {

		private final String[] SHELL_GROUP_NAMES = {"Co-workers", "College Friends", "Family", 
				"Fraternity/Sorority", "High School", "Roommates"};
		private boolean popped;
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_contact_lists,
					container, false);
			popped = false;
			mPlus = false;
			mCurrentFragment = "ChooseFromExistingList";
			getActivity().invalidateOptionsMenu();
			return rootView;
		}
		
		public void onResume()
		{
			super.onResume();
			getActivity().setTitle(getResources().getString(R.string.title_contact_list));
			mCurrentFragment = "ChooseFromExistingList";
		}
		
		//only groups that you're a creator for
		public void addMemberQueries(ArrayList<ParseQuery<ParseObject>> queries,
				ParseQuery<ParseObject> query)
		{
		}
		
		public void addListsToView()
		{
	        LinearLayout ll = (LinearLayout) getActivity().findViewById(R.id.groupList);
	        ll.removeAllViews();
	        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
	        		LayoutParams.WRAP_CONTENT);
	        LayoutInflater inflater = getActivity().getLayoutInflater();
	        for (int i = 0; i < lists.length; i++)
	        {
	        	View vi = inflater.inflate(R.layout.list_group, null);
	        	((TextView) vi.findViewById(R.id.name)).setText(lists[i]);
	            //if we give it a resource id, then make it an int resource, not a string url
	            if (photoURLs[i] != null && photoURLs[i].indexOf('.') == -1)
	    	        Picasso.with(getActivity()).load(Integer.parseInt(photoURLs[i]))
	    	        	.resize(140, 140).into((ImageView) vi.findViewById(R.id.imageView1));
	            else
	    	        Picasso.with(getActivity()).load(photoURLs[i]).resize(140, 140)
	    	        	.into((ImageView) vi.findViewById(R.id.imageView1));
	            final int position = i;
	            vi.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						showSpinner();
						ParseObject contactList = mMemberLists.get(position);
						JSONArray members = contactList.getJSONArray("members");
						ArrayList<String> curMemberIds = new ArrayList<String>();
						//add creator id to compare to also
						curMemberIds.add(contactList.getParseUser("owner").getObjectId());
						for (int i = 0; i < members.length(); i++)
						{
							try {
								curMemberIds.add(members.getJSONObject(i).getString("objectId"));
								System.out.println(members.getJSONObject(i).getString("objectId"));
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
						System.out.println("invitees");
						//then add invitees who aren't already in the list
						for (int i = 0; i < mMembers.length(); i++)
						{
							try {
								System.out.println(mMembers.getJSONObject(i).getString("objectId"));
								if (!curMemberIds.contains(mMembers.getJSONObject(i).getString("objectId")))
									members.put(mMembers.getJSONObject(i));
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
						contactList.put("members", members);
						contactList.saveInBackground(new SaveCallback() {

							@Override
							public void done(ParseException e) {
								if (e == null)
								{
									Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show();
								}
								else
								{
									showParseException(e);
									Toast.makeText(context, "Oops! Something went wrong.", Toast.LENGTH_SHORT).show();
								}
								popped = true;
								Intent intent = new Intent(getActivity(), RSVPEventsActivity.class);
								startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
								getActivity().finish();
							}
							
						});
					}
	            });
	            vi.setLayoutParams(lp);
	            ll.addView(vi);
    			ll.addView(makeDivider());
	        }
	        
	        //now deal with shell groups
	        System.out.println(mShellGroups.contains(false));
	        if (mShellGroups.contains(false))
	        {
	        	View vi = new View(getActivity());
	        	vi.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int)
    					TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, 
    					getResources().getDisplayMetrics())));
	        	ll.addView(vi);
	        	for (int i = 0; i < mShellGroups.size(); i++)
	        	{
	        		//if we don't have this shell group, add it
	        		if (!mShellGroups.get(i))
	        		{
	        			ll.addView(makeDivider());
	        			vi = inflater.inflate(R.layout.list_group, null);
	        			vi.setLayoutParams(lp);
	        			((TextView) vi.findViewById(R.id.name)).setText(SHELL_GROUP_NAMES[i]);
	        			vi.findViewById(R.id.imageView1).setVisibility(View.GONE);
	        			vi.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int)
	        					TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, 
	        					getResources().getDisplayMetrics())));
	        			final int position = i;
	        			vi.setOnClickListener(new OnClickListener() {
	    					@Override
	    					public void onClick(View v) {
	    						showSpinner();
	    						ParseObject contactList = new ParseObject("ContactList");
	    						contactList.put("name", SHELL_GROUP_NAMES[position]);
	    						contactList.put("owner", ParseUser.getCurrentUser());
	    						contactList.put("isVisibleToMembers", false);
	    						contactList.put("members", mMembers);
	    						contactList.saveInBackground(new SaveCallback() {

									@Override
	    							public void done(ParseException e) {
	    								if (e == null)
	    								{
	    									Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show();
	    								}
	    								else
	    								{
	    									showParseException(e);
	    									Toast.makeText(context, "Oops! Something went wrong.", 
	    											Toast.LENGTH_SHORT).show();
	    								}
	    								popped = true;
	    								Intent intent = new Intent(getActivity(), RSVPEventsActivity.class);
	    								startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
	    								getActivity().finish();
	    							}
	    							
	    						});
	    					}
	    	            });
	        			ll.addView(vi);
	        		}
	        	}
	        }
		}
		
		public View makeDivider()
		{
			View view = new View(getActivity());
			view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 1));
			view.setBackgroundColor(getResources().getColor(R.color.lightGray));
			return view;
		}
		
		public void onDestroy()
		{
			if (!popped)
			{
				Intent intent = new Intent(getActivity(), RSVPEventsActivity.class);
				startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
				getActivity().finish();
			}
			super.onDestroy();
		}
	}
}
