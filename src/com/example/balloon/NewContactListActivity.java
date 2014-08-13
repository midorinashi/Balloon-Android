package com.example.balloon;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

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
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Contacts.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

public class NewContactListActivity extends ProgressActivity {

	public static String mListName;
	public static boolean mPublicList;
	public static ParseFile mContactListImage;
	private static String nextTitle;
	private static ListView mListView;
	private static CheckBox mCheckbox;
	private static JSONArray mContacts;
	private File lastSavedFile;
	private static ContextMenu mMenu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_contact_list);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		//Set default values for first screen
		mListName = "";
		mPublicList = false;
		nextTitle = "Next";
		
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new NewContactListFragment()).commit();
		}
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
	        case R.id.action_photo_phone:
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
					removeSpinner();
				}
				else
					showParseException(e);
			}
		});
	}
	
	public void setContactListImage()
	{
		ImageView view = (ImageView) findViewById(R.id.image);
		if (view != null)
		{
			Picasso.with(this).load(mContactListImage.getUrl()).into(view);
			findViewById(R.id.addPhoto).setVisibility(View.GONE);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.new_contact_list, menu);
		return true;
	}
	
	public boolean onPrepareOptionsMenu(final Menu menu) {
	    MenuItem item = menu.findItem(R.id.action_next);
	    item.setTitle(nextTitle);
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
		else if (id == R.id.action_next)
		{
			if (item.getTitle().toString().compareTo("Next") == 0)
			{
				//go to the select members fragment
				FragmentTransaction transaction = getFragmentManager().beginTransaction();
				transaction.replace(R.id.container, new SelectMembersFromContactsFragment());
				transaction.addToBackStack(null);
				transaction.commit();
				return true;
			}
			else
			{
				//finish activity and save list
				saveContacts();
			}
		}
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	//manages the checkbox in the select members fragment
	public void selectAll(View view)
	{
		System.out.println(mListView);
		// get how many have been checked
		if (mListView != null)
		{
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
	}
	
	// http://stackoverflow.com/questions/12413159/android-contact-picker-with-checkbox/
	public void saveContacts()
	{
		//  i get the checked contact_id
		long[] id = ((ListView) findViewById(R.id.contactsList)).getCheckedItemIds();
        mContacts = new JSONArray();
        for (int i = 0; i < id.length; i++)
        {
        	mContacts.put(getPhoneNumber(id[i]));
        }
		if (mContacts.length() == 0)
			Toast.makeText(this, "No friends?", Toast.LENGTH_SHORT).show();
		else
			findOrCreateContacts();
	}
	
	/* References
	 * http://stackoverflow.com/questions/12413159/android-contact-picker-with-checkbox/
	 * http://stackoverflow.com/questions/7114573/get-contacts-mobile-number-only
	 * http://www.regular-expressions.info/shorthand.html
	 */
	private JSONObject getPhoneNumber(long id) {
		JSONObject contact = new JSONObject();
	    String phone = null;
	    String firstName = null;
	    String lastName = null;
	    Cursor phonesCursor = queryPhoneNumbers(id);
	    Cursor nameCursor = queryName(id);
	    
	    if (phonesCursor == null || phonesCursor.getCount() == 0) {
	        // No valid number
	    	System.out.println("No valid number");
	        return null;
	    }
	    else {
	        phonesCursor.moveToPosition(-1);
	        while (phonesCursor.moveToNext()) {
		        int phoneType = phonesCursor.getInt(phonesCursor.getColumnIndex(ContactsContract
		        		.CommonDataKinds.Phone.TYPE));
		        if (phoneType == Phone.TYPE_MOBILE)
		        {
		        	phone = phonesCursor.getString(phonesCursor.getColumnIndex(ContactsContract
			        		.CommonDataKinds.Phone.NUMBER));
		 	        firstName = nameCursor.getString(nameCursor.getColumnIndex(ContactsContract
			        		.CommonDataKinds.StructuredName.GIVEN_NAME));
			        lastName = nameCursor.getString(nameCursor.getColumnIndex(ContactsContract
			        		.CommonDataKinds.StructuredName.FAMILY_NAME));
		            break;
		        }
	        }
	    }
	    //if no mobile number, try the primary
	    if (phone == null)
	    {
	        phone = phonesCursor.getString(phonesCursor
	                .getColumnIndex(Phone.NUMBER));
	    }
        //phone = phone.replaceAll("[^[0-9]]", "");
        //TODO deal with missing area codes and missing country codes - use user's number
        //has area code but no country code - cloud code auto adds america
        
        //make the contact!! :D
        try {
			contact.put("mobileNumber", phone);
			contact.put("firstName", firstName);
			contact.put("lastName", lastName);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        System.out.println("mobileNumber: " + phone + "; firstName: " 
        		+ firstName + "; lastName :" + lastName);
        
        phonesCursor.close();
        nameCursor.close();
	    return contact;
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
	
	private Cursor queryName(long contactId) {
	    ContentResolver cr = getContentResolver();
	    Uri baseUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,
	            contactId);
	    Uri dataUri = Uri.withAppendedPath(baseUri,
	            ContactsContract.Contacts.Data.CONTENT_DIRECTORY);
	    Cursor c = cr.query(dataUri, new String[] { StructuredName.FAMILY_NAME, StructuredName.GIVEN_NAME },
	    		Data.MIMETYPE + "=?", new String[] { StructuredName.CONTENT_ITEM_TYPE }, null);
	            
	    if (c != null && c.moveToFirst()) {
	        return c;
	    }
	    return null;
	}
	
	public void findOrCreateContacts()
	{
		showSpinner();
		HashMap<String, Object> request = new HashMap<String, Object>();
		request.put("contacts", mContacts);
		System.out.println("starting to find or create users");
		ParseCloud.callFunctionInBackground("findOrCreateUsers", request,
				new FunctionCallback<Object>() {

					@SuppressWarnings("unchecked")
					@Override
					public void done(Object members, ParseException e) {
						if (e != null)
							showParseException(e);
						else
						{
							System.out.println("Find or create users complete.");
							makeContactList((ArrayList<ParseUser>) members);
						}
					}
			
		});
	}

	public void makeContactList(ArrayList<ParseUser> members)
	{
		ParseObject list = new ParseObject("ContactList");
		list.put("name", mListName);
		list.put("owner", ParseUser.getCurrentUser());
		list.put("isVisibleToMembers", mPublicList);
		if (mContactListImage != null)
			list.put("photo", mContactListImage);
		list.addAll("members", members);
		System.out.println("Saving...");
		list.saveInBackground(new SaveCallback() {
			public void done(ParseException e) {
				if (e != null)
					showParseException(e);
				else
					finishActivity();
				
			}
		});
	}
	
	public void finishActivity()
	{
		removeSpinner();
		//Show message
		Context context = getApplicationContext();
		CharSequence text = "New Contact List Made!";
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
		finish();
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

	public static class NewContactListFragment extends Fragment
	{
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_create_list,
					container, false);
			mListName = "";
			mPublicList = false;
			mContactListImage = null;
			return rootView;
		}
	
		public void onResume()
		{
			super.onResume();
			getActivity().setTitle(getResources().getString(R.string.title_create_list));
			nextTitle = "Next";
			getActivity().invalidateOptionsMenu();
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

	//because what the fuck am i doing
	public static class SelectMembersFromContactsFragment extends 
		NewInvitationActivity.SelectMembersFromContactsFragment implements LoaderCallbacks<Cursor> {
		
		public void onResume()
		{
			super.onResume();
			mListView = (ListView) getActivity().findViewById(R.id.contactsList);
			nextTitle = "Done";
			getActivity().invalidateOptionsMenu();
		}
	}
}
