package com.example.balloon;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
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
import android.text.TextUtils;
import android.util.SparseBooleanArray;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

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

	private static String mListName;
	private static boolean mPublicList;
	private static String mListId;
	private static String mAgenda;
	private static String mVenueInfo;
	private static JSONObject mVenue;
	private static boolean mToday;
	private static int mExpiresAtHour;
	private static int mExpiresAtMinute;
	private static JSONArray mVenuePhotoUrls;
	private static JSONArray mMemberObjectIds;
	private static String[] mPhoneNumbers;
	private static String[] mMemberNames;
	private static boolean mMakeContactList;
	private static String[] mMemberIds;
	private static JSONArray mMembers;
	private static String mPreviewName;
	private static String mCurrentFragment;
	//ayyyyyy because sometimes we go outside of the flow
	private static boolean mAfterFinalEdit;
	
	//views to mangae select members fragment
	private static CheckBox mCheckbox;
	private static ListView mListView;
	private static ContextMenu mMenu;
	private File lastSavedFile;
	protected static ParseFile mContactListImage;
	
	//to manage the buttons at the top
	private static boolean mPlus;
	private static String mNext;
	private static Activity context;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_invitation);

		//set all the defaults
		deleteInfo();
		
		if (savedInstanceState == null) {
			mAfterFinalEdit = false;
			getFragmentManager().beginTransaction()
					.add(R.id.container, new SelectListFragment()).commit();
		}
	}
	
	//erase all static fields
	private void deleteInfo()
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
		mMemberIds = null;
		mMembers = null;
		mMemberNames = null;
		mPreviewName = null;
		mCurrentFragment = "";
		mAfterFinalEdit = false;
		mCheckbox = null;
		mListView = null;
		mPlus = true;
		mContactListImage = null;
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
	    	if (!mCurrentFragment.equals("FinalEditFragment"))
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
		if (id == R.id.action_plus)
		{
			FragmentTransaction transaction = getFragmentManager().beginTransaction();
			transaction.replace(R.id.container, new CreateListFragment());
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
			transaction.replace(R.id.container, new CreateListFragment());
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
				saveContacts();
				mMakeContactList = true;
				if (mAfterFinalEdit)
				{
					getFragmentManager().popBackStack();
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
			transaction.replace(R.id.container, new FinalEditFragment());
		}
		else if (mCurrentFragment.equals("ImageListFragment"))
		{
			ImageListFragment.setSave(true);
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
	
	public void makeMeetup(final View view)
	{
		showSpinner();
		//make the contact list if we need to
		if (mMakeContactList)
		{
			HashMap<String, Object> params = new HashMap<String, Object>();
			JSONArray contacts = new JSONArray();
			for (int i = 0; i < mMemberNames.length; i++)
			{
				JSONObject contact = new JSONObject();
				try {
					contact.put("firstName", mMemberNames[i]);
					contact.put("mobileNumber", mPhoneNumbers[i]);
					System.out.println(mMemberNames[i] +" " + mPhoneNumbers[i]);
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
						ParseObject contactList = new ParseObject("ContactList");
						contactList.put("owner", ParseUser.getCurrentUser());
						contactList.put("name", mListName);
						if (mContactListImage != null)
							contactList.put("photo", mContactListImage);
						contactList.put("isVisibleToMembers", mPublicList);
						JSONArray mContacts = new JSONArray();
						//two different json arrays because send invites need json objects, not parse objects
						mMembers = new JSONArray();
						for (int i = 0; i < list.size(); i++)
						{
							mContacts.put(list.get(i));
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
						contactList.put("members", mContacts);
						contactList.saveInBackground(new SaveCallback() {
							@Override
							public void done(ParseException e) {
								if (e == null)
								{
									System.out.println("save success");
									makeMeetup(view);
								}
								else
									showParseException(e);
							}
						});
					}
					else
						showParseException(e);
				}
				
			});
			mMakeContactList = false;
			return;
		}
		final ParseObject meetup = new ParseObject("Meetup");
		meetup.put("agenda", mAgenda);
		//make the contact list if we need to
		//contact list id???
		//gotta set both contactList and mMembers here
		
		//params.put("contactList", mListId);
		meetup.put("creator", ParseUser.getCurrentUser());
		meetup.put("expiresAt", changeToDate());
		meetup.put("invitedUsers", mMembers);
		meetup.put("venueInfo", mVenue);
		if (mVenuePhotoUrls != null && mVenuePhotoUrls.length() != 0)
			meetup.put("venuePhotoURLs", mVenuePhotoUrls);
		//TODO learn where invite more happens
		meetup.put("allowInviteMore", true);
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
	
	public void sendInvite(ParseObject meetup)
	{
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("meetupId", meetup.getObjectId());
		params.put("creator", meetup.getParseUser("creator").getObjectId());
		params.put("invitedUsers", meetup.getJSONArray("invitedUsers"));
	
		ParseCloud.callFunctionInBackground("sendInvites", params, new FunctionCallback<Object>(){

			@Override
			public void done(Object arg0, ParseException e) {
				if (e != null)
					showParseException(e);
				end();
			}
			
		});
	}
	
	public static Date changeToDate()
	{
		GregorianCalendar calendar = new GregorianCalendar();
		//TODO Using current timeLocale see if fix is needed
		calendar.set(GregorianCalendar.HOUR_OF_DAY, mExpiresAtHour);
		calendar.set(GregorianCalendar.MINUTE, mExpiresAtMinute);
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
		//Show message
		Context context = getApplicationContext();
		CharSequence text = "Sent!";
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
	        	FragmentTransaction transaction = getFragmentManager().beginTransaction();
	        	transaction.replace(R.id.container, new ImageListFragment());
	        	transaction.addToBackStack(null);
	        	transaction.commit();
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
		if (mCurrentFragment == "CreateListFragment")
		{
			removeSpinner();
			ImageView view = (ImageView) findViewById(R.id.image);
			Picasso.with(this).load(mContactListImage.getUrl()).into(view);
			findViewById(R.id.addPhoto).setVisibility(View.GONE);
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
	
	public static class SelectListFragment extends ProgressFragment {

		protected String[] lists;
		protected String[] photoURLs;
		protected String[] ids;
		protected OnMemberListSelectedListener mListener;
		
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
			queries.add(memberQuery);
			
			ParseQuery<ParseObject> mainQuery = ParseQuery.or(queries);
			mainQuery.orderByAscending("name");
			mainQuery.findInBackground(new FindCallback<ParseObject>() {
				public void done(List<ParseObject> memberLists, ParseException e) {
					if (e == null)
					{
						TypedArray images = getResources().obtainTypedArray(R.array.contact_list_images);
						lists = new String[memberLists.size()];
						photoURLs = new String[memberLists.size()];
						ids = new String[memberLists.size()];
						for (int i = 0; i < memberLists.size(); i++)
						{
							lists[i] = memberLists.get(i).getString("name");
							if (memberLists.get(i).containsKey("photo"))
								photoURLs[i] = memberLists.get(i).getParseFile("photo").getUrl();
							else
								photoURLs[i] = "" + images.getResourceId(
										Math.abs(lists[i].hashCode()) % 12,
										R.drawable.color_balloon_8);
							ids[i] = memberLists.get(i).getObjectId();
						}
						addListsToView();
						removeSpinner();
						images.recycle();
					}
					else
						showParseException(e);
				}
			});
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
						mListName = lists[position];
						if (mListId != ids[position])
							mMemberIds = null;
						mListId = ids[position];
						mListener.onMemberListSelected();
					}
		        });
			}
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
			if (getActivity() instanceof NewInvitationActivity)
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
	
	public static class SelectMembersFromListFragment extends ProgressFragment {
		
		// and name should be displayed in the text1 textview in item layout
		private String[] names;
		private String[] photoURLs;
		private ArrayList<String> ids;
		private JSONArray users;
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
		
		public void onResume()
		{
			super.onResume();
			showSpinner();
			getActivity().setTitle(getResources().getString(R.string.title_select_members_from_list));
			mCurrentFragment = "SelectMembersFromListFragment";
			
			mNext = getResources().getString(R.string.action_next);
			getActivity().invalidateOptionsMenu();

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
						names = new String[userList.size()];
						photoURLs = new String[userList.size()];
						//we want a JSONArray of users, not just of userIds
						users = new JSONArray();
						for (int i = 0; i < userList.size(); i++)
						{
							names[i] = userList.get(i).getString("firstName") + " " + 
										userList.get(i).getString("lastName");
							if (userList.get(i).containsKey("profilePhoto"))
								photoURLs[i] = userList.get(i).getParseFile("profilePhoto").getUrl();
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
							names, photoURLs);
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
		    }
		}
		
		public void onPause()
		{
			super.onPause();
			
			SparseBooleanArray checked = ((ListView) getActivity().findViewById(R.id.memberList))
					.getCheckedItemPositions();
			int count = ((ListView) getActivity().findViewById(R.id.memberList)).getCount();
			mMemberIds = new String[count];
			mMembers = new JSONArray();
			mMemberObjectIds = new JSONArray();
			mPreviewName = null;
			
			for (int i = 0; i < count; i++)
			{
				int index = checked.indexOfKey(i);
				if (checked.get(index))
				{
					if (mPreviewName == null)
					{
						mPreviewName = names[index];
						if (names[0].indexOf(' ') > -1)
			        		mPreviewName = names[0].substring(0, names[0].indexOf(' '));
			        	else
			        		mPreviewName = names[0];
					}
					mMemberIds[i] = ids.get(index);
					try {
						mMembers.put(users.get(index));
						mMemberObjectIds.put(ids.get(index));
					} catch (JSONException e) {
						e.printStackTrace();
					}
					System.out.println(mMemberIds[i]);
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
	
	public static class ChooseLocationFragment extends ProgressFragment implements OnQueryTextListener{

		private static ListView lv;
		static Activity context;
		

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
			context = getActivity();
			context.setTitle(getResources().getString(R.string.title_choose_location));
			mCurrentFragment = "ChooseLocationFragment";
			System.out.println("Executing query sushi");
			
			new AccessFoursquareVenues(this).execute("");
			SearchView sv = (SearchView) context.findViewById(R.id.searchLocation);
			sv.setOnQueryTextListener(this);
			sv.setSubmitButtonEnabled(true);
			
			//get the list so i can access it later
			lv = (ListView) context.findViewById(R.id.locationList);
		}

		public static void makeList(final JSONArray array) {
			final String[] names = new String[array.length()];
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
			ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context,
					R.layout.list_item_location, R.id.text1, names) {
				  @Override
				  public View getView(int position, View convertView, ViewGroup parent) {
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
			};
	        lv.setAdapter(arrayAdapter);
	        lv.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> a, View v, int position, long id) {
					mVenueInfo = names[position];
					try {
						mVenue = array.getJSONObject(position);
					} catch (JSONException e) {
						e.printStackTrace();
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
			super.onStop();
		}

		@Override
		public boolean onQueryTextChange(String newText) {
			return true;
		}

		//search now!
		@Override
		public boolean onQueryTextSubmit(String query) {

			new AccessFoursquareVenues(this).execute(query);
			return true;
		}
	}
	
	public static class SetDeadlineFragment extends Fragment {

		public TimePicker.OnTimeChangedListener mTimeListener;

		private String TODAY;
		private String TOMORROW;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_set_deadline,
					container, false);
			TODAY = getActivity().getResources().getString(R.string.today);
			TOMORROW = getActivity().getResources().getString(R.string.tomorrow);
			mTimeListener = new TimePicker.OnTimeChangedListener() {
				@Override
				public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
					TextView tv = (TextView) getActivity().findViewById(R.id.deadlineTime);
					mExpiresAtHour = hourOfDay;
					mExpiresAtMinute = minute;
					changeToDate();
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
				mExpiresAtHour = tp.getCurrentHour() + 1;
			}
			tp.setCurrentHour(mExpiresAtHour);
			tp.setCurrentMinute(mExpiresAtMinute);
			tp.setOnTimeChangedListener(mTimeListener);
		
			TextView tv = (TextView) getActivity().findViewById(R.id.deadlineTime);
			String day = TODAY;
			if (!mToday)
				day = TOMORROW;
			tv.setText(formatTime(tp.getCurrentHour(), tp.getCurrentMinute()) + " " + day);
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
			getActivity().setTitle(getResources().getString(R.string.title_final_edit));
			mCurrentFragment = "FinalEditFragment";
			mAfterFinalEdit = false;

			mNext = getResources().getString(R.string.action_done);
			getActivity().invalidateOptionsMenu();
			
			TextView tv = (TextView) getActivity().findViewById(R.id.finalEditToText);
			tv.setText(mListName);
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
			
		}
		
		//if we go back in the flow, we gotta readd the next button
		public void onDestroyView()
		{
			super.onDestroyView();
			if (!mAfterFinalEdit)
			{
				mNext = getResources().getString(R.string.action_next);
				getActivity().invalidateOptionsMenu();
			}
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
			tv.setText(mPreviewName + ", " + mAgenda);
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
			final Date expiresAt = changeToDate();
			final TextView mTimeToRSVPView = (TextView) event.findViewById(R.id.timer);
			mHandler = new Handler() {
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
					time = time + seconds;
					System.out.println(time);
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
}
