package com.j32productions.balloon;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
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
	private File lastSavedFile;
	private static ContextMenu mMenu;
	private static ArrayList<ParseUser> members;
	private static boolean hasChangedName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_contact_list);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		//Set default values for first screen
		mListName = "New Group";
		if (getIntent().hasExtra("name"))
			mListName = getIntent().getStringExtra("name");
		mPublicList = false;
		nextTitle = "Next";
		
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new SelectMembersFromContactsFragment()).commit();
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
		ImageView view = (ImageView) findViewById(R.id.editGroup).findViewById(R.id.imageView1);
		if (view != null)
		{
			Picasso.with(this).load(mContactListImage.getUrl()).resize(140, 140).into(view);
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
			else if (item.getTitle().toString().compareTo("Done") == 0)
			{
				//finish activity and save list
				saveContacts();
			}
			else
			{
				makeContactList();
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
		SelectMembersFromContactsFragment.saveContacts();
		System.out.println("Count is " + SelectMembersFromContactsFragment.getCheckedCount());
		if (SelectMembersFromContactsFragment.getCheckedCount() == 0)
			Toast.makeText(this, "No friends?", Toast.LENGTH_SHORT).show();
		else
			findOrCreateContacts();
	}
	
	public void findOrCreateContacts()
	{
		showSpinner();
		HashMap<String, Object> params = new HashMap<String, Object>();
		JSONArray contacts = new JSONArray();
		for (int i = 0; i < SelectMembersFromContactsFragment.getCheckedCount(); i++)
		{
			JSONObject contact = new JSONObject();
			try {
				contact.put("firstName", SelectMembersFromContactsFragment.getFirstName(i));
				contact.put("lastName", SelectMembersFromContactsFragment.getLastName(i));
				contact.put("mobileNumber", SelectMembersFromContactsFragment.getPhoneNumber(i));
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			contacts.put(contact);
		}
		params.put("contacts", contacts);
		System.out.println("starting to find or create users");
		ParseCloud.callFunctionInBackground("findOrCreateUsers", params,
				new FunctionCallback<ArrayList<ParseUser>>() {

					@Override
					public void done(ArrayList<ParseUser> m, ParseException e) {
						if (e != null)
							showParseException(e);
						else
						{
							members = m;
							System.out.println("Find or create users complete.");
							FragmentTransaction transaction = getFragmentManager().beginTransaction();
							transaction.replace(R.id.container, new GroupSummaryFragment());
							transaction.addToBackStack(null);
							transaction.commit();
						}
					}
			
		});
	}
	
	public void changeName(View view)
	{
	    DialogFragment newFragment = new ChangeNameFragment();
	    newFragment.show(getFragmentManager(), null);
	}

	public void makeContactList()
	{
		ParseObject list = new ParseObject("ContactList");
		list.put("name", mListName);
		list.put("owner", ParseUser.getCurrentUser());
		list.put("isVisibleToMembers", ((Checkable) findViewById(R.id.checkBox1)).isChecked());
		if (mContactListImage != null)
			list.put("photo", mContactListImage);
		JSONArray mMembers = new JSONArray();
		for (int i = 0; i < members.size(); i++)
		{
			try {
				JSONObject person = new JSONObject();
				person.put("__type", "Pointer");
				person.put("className", "_User");
				person.put("objectId", members.get(i).getObjectId());
				mMembers.put(person);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		list.put("members", mMembers);
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
		CharSequence text = "New Group Made!";
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
		NewInvitationActivity.SelectMembersFromContactsFragment {
		
		public void onResume()
		{
			super.onResume();
			getActivity().setTitle(getResources().getString(R.string.title_create_list));
			mListView = (ListView) getActivity().findViewById(R.id.contactsList);
			nextTitle = "Done";
			getActivity().invalidateOptionsMenu();
		}
	}
	
	public static class GroupSummaryFragment extends Fragment
	{
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_contact_list_info,
					container, false);

			nextTitle = "Save";
			getActivity().invalidateOptionsMenu();
			getActivity().setTitle(getResources().getString(R.string.title_create_list));
			
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
			
			for (int i = 0; i < members.size(); i++)
			{
				member = View.inflate(getActivity(), R.layout.list_members, null);
				user = members.get(i);
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
				else
				{
					Uri uri = Uri.parse("android.resource://com.j32productions.balloon"
							+ "/drawable/user280");
		        	try {
						InputStream stream = getActivity().getContentResolver().openInputStream(uri);
				        Picasso.with(getActivity()).load(uri).resize(140, 140)
			        		.into((ImageView) member.findViewById(R.id.imageView1));
				        stream.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
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
	                	   if (!str.replaceAll("[\\W]", "").equals(""))
	                	   {
	                		   mListName = str;
		                	   ((TextView) getActivity().findViewById(R.id.listName)).setText(mListName);
		                	   hasChangedName = true;
		                	   System.out.println(mListName);
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
}
