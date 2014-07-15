package com.example.balloon;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Contacts.Data;
import android.provider.ContactsContract.RawContacts;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class NewContactListActivity extends ActionBarActivity {

	public static String mListName;
	public static boolean mPublicList;
	private static Menu mMenu;
	private static String nextTitle;
	private static ListView mListView;
	private static CheckBox mCheckbox;
	private static JSONArray mContacts;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_contact_list);
		
		//Set default values for first screen
		mListName = "";
		mPublicList = false;
		nextTitle = "Next";
		
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new NewContactListFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.new_contact_list, menu);
		mMenu = menu;
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
		if (id == R.id.action_next)
		{
			if (item.getTitle().toString().compareTo("Next") == 0)
			{
				//go to the select members fragment
				FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
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
		HashMap<String, Object> request = new HashMap<String, Object>();
		request.put("contacts", mContacts);
		System.out.println("starting to find or create users");
		ParseCloud.callFunctionInBackground("findOrCreateUsers", request,
				new FunctionCallback<Object>() {

					@SuppressWarnings("unchecked")
					@Override
					public void done(Object members, ParseException e) {
						if (e != null)
							e.printStackTrace();
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
		list.addAll("members", members);
		System.out.println("Saving...");
		list.saveInBackground(new SaveCallback() {
			public void done(ParseException e) {
				if (e != null)
					e.printStackTrace();
				else
					finishActivity();
				
			}
		});
	}
	
	public void finishActivity()
	{
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
			return rootView;
		}
	
		public void onResume()
		{
			super.onResume();
			getActivity().setTitle(getResources().getString(R.string.title_create_list));
			nextTitle = "Next";
			getActivity().invalidateOptionsMenu();
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
			mCheckbox = (CheckBox) getActivity().findViewById(R.id.contactsSelectAll);
			nextTitle = "Done";
			getActivity().invalidateOptionsMenu();
		}
	}
}
