package com.j32productions.balloon;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
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
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
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

public class ContactListInfoActivity extends ProgressActivity implements OnMenuItemClickListener {

	public static String mListName;
	public static String mListId;
	protected static ParseObject list;
	protected static String[] phones;
	private File lastSavedFile;
	protected ParseFile mContactListImage;
	private static Menu mOptionMenu;
	private static ContextMenu mMenu;
	public static String memberToDelete;
	public static String phoneToDelete;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_list_info);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		if (getIntent().hasExtra("listName"))
			mListName = getIntent().getExtras().getString("listName");
		if (getIntent().hasExtra("listId"))
			mListId = getIntent().getExtras().getString("listId");

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new ShowListFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.contact_list_info, menu);
		mOptionMenu = menu;
		return true;
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
		else if (id == R.id.action_add_members)
		{
			FragmentTransaction transaction = getFragmentManager().beginTransaction();
			transaction.replace(R.id.container, new SelectMembersFromContactsFragment());
			transaction.addToBackStack(null);
			transaction.commit();
		}
		else if (id == R.id.action_send)
		{
			Intent intent = new Intent(this, NewInvitationFromGroupActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString("listId", mListId);
			intent.putExtras(bundle);
			startActivity(intent);
		}
		else if (id == R.id.action_delete_yourself)
		{
			deleteYourself();
		}
		else if (id == R.id.action_delete_group)
		{
			deleteGroup();
		}
		else if (id == R.id.action_delete_group)
		{
			deleteGroup();
		}
		else if (id == R.id.save)
		{
			saveNewMembers();
		}
		
		//TODO ALL THE REMOVESSSSS
		return super.onOptionsItemSelected(item);
	}
	
	public void deleteYourself()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.delete_yourself_warning)
	    	.setTitle(R.string.action_delete_yourself)
	    	.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {

					showSpinner();
					JSONArray members = list.getJSONArray("members");
					String id = ParseUser.getCurrentUser().getObjectId();
					JSONArray newMembers = new JSONArray();
					for (int i = 0; i < members.length(); i++)
					{
						try {
							//because i can't use remove D:
							if (members.getJSONObject(i).getString("objectId").compareTo(id) != 0)
							{
								newMembers.put(members.getJSONObject(i));
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					list.put("members", newMembers);
					//finally, close the list
					System.out.println("saving");
					list.saveInBackground(new SaveCallback() {
						@Override
						public void done(ParseException e) {
							if (e == null)
							{
								System.out.println("done!");
								removeSpinner();
								finish();
							}
							else
								showParseException(e);
						}
					});
				}
	    	})
	       	.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
				}
	       	})
	       	.create()
	       	.show();
	}
	
	public void deleteGroup()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.delete_group_warning)
	    	.setTitle(R.string.action_delete_group)
	    	.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					showSpinner();
					list.deleteInBackground(new DeleteCallback() {
						@Override
						public void done(ParseException e) {
							if (e == null)
							{
								System.out.println("done!");
								removeSpinner();
								finish();
							}
							else
								showParseException(e);
						}
					});
				}
	    	})
	       	.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
				}
	       	})
	       	.create()
	       	.show();
	}
	
	//so we can delete members
	public boolean onMenuItemClick(MenuItem item)
	{
		showSpinner();
		System.out.println(item.getItemId());
		System.out.println(R.id.action_delete_member);
		if (item.getItemId() == R.id.action_delete_member)
		{
			System.out.println("deleting");
			JSONArray members = list.getJSONArray("members");
			JSONArray newMembers = new JSONArray();
			for (int i = 0; i < members.length(); i++)
			{
				try {
					//because i can't use remove D:
					if (members.getJSONObject(i).getString("objectId").compareTo(memberToDelete) != 0)
					{
						newMembers.put(members.getJSONObject(i));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			list.put("members", newMembers);
			//finally, close the list
			System.out.println("saving");
			list.saveInBackground(new SaveCallback() {
				@Override
				public void done(ParseException e) {
					if (e == null)
					{
						System.out.println("done!");
						System.out.println("To delete: "+phoneToDelete);
						for (int i = 0; i < phones.length; i++)
						{
							if (phones[i].equals(phoneToDelete))
							{
								phones[i] = "HUEHUEHUEHUEHUE";
								System.out.println("found");
							}
						}
						//then we need to take out this member from the view
						LinearLayout ll = (LinearLayout) findViewById(R.id.linearLayout);
						//we don't want the lines
						if (ll != null)
						{
							for (int i = 0; i < ll.getChildCount(); i += 2)
							{
								View view = ll.getChildAt(i);
								if (((TextView) view.findViewById(R.id.id)).getText().toString()
										.compareTo(memberToDelete) == 0)
								{
									//remove both the members and the line under it
									ll.removeViews(i, 2);
									break;
								}
							}
						}
						removeSpinner();
					}
					else
						showParseException(e);
				}
			});
			return true;
		}
		return false;
	}
	
	public void saveNewMembers()
	{
		showSpinner();
		SelectMembersFromContactsFragment.saveContacts();
		final JSONArray members = list.getJSONArray("members");
		final ArrayList<String> memberIds = new ArrayList<String>();
		//get a list of member object ids who are in the group already
		for (int i = 0; i < members.length(); i++)
		{
			try {
				memberIds.add(members.getJSONObject(i).getString("objectId"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		//find le parse members
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
		ParseCloud.callFunctionInBackground("findOrCreateUsers", params, 
				new FunctionCallback<ArrayList<ParseUser>>() {
			@Override
			public void done(ArrayList<ParseUser> newMembers, ParseException e) {
				if (e == null)
				{
					for (int i = 0; i < newMembers.size(); i++)
					{
						//if this person isn't already in the list, add them
						if (!memberIds.contains(newMembers.get(i).getObjectId()))
						{
							JSONObject person = new JSONObject();
							try {
								person.put("__type", "Pointer");
								person.put("className", "_User");
								person.put("objectId", newMembers.get(i).getObjectId());
							} catch (JSONException e1) {
								e1.printStackTrace();
							}
							members.put(person);
						}
					}
					list.put("members", members);
					list.saveInBackground(new SaveCallback() {
						@Override
						public void done(ParseException e) {
							if (e == null)
							{
								Toast.makeText(getApplication(), "Members saved!", 
										Toast.LENGTH_SHORT).show();
								getFragmentManager().popBackStack();
								showSpinner();
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
			Picasso.with(this).load(mContactListImage.getUrl()).resize(140, 140).into(view);
		list.put("photo", mContactListImage);
		list.saveInBackground();
	}
	
	public void changeName(View view)
	{
	    DialogFragment newFragment = new ChangeNameFragment();
	    newFragment.show(getFragmentManager(), null);
	}

	public void toggleVisibleToGroup(View view)
	{
		Checkable checkbox = (Checkable) findViewById(R.id.checkBox1);
		if (checkbox != null)
		{
			checkbox.setChecked(!checkbox.isChecked());
			list.put("isVisibleToMembers", checkbox.isChecked());
			list.saveInBackground();
		}
	}
	
	public static class ShowListFragment extends ProgressFragment {
		protected String[] names;
		protected double[] responseRates;
		protected ArrayList<String> ids;
		protected String[] photoURLs;
		protected boolean mIsOwner;
		protected boolean mIsShared;

		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_contact_list_info,
					container, false);
			return rootView;
		}
		
		public void onResume()
		{
			super.onResume();
			getActivity().setTitle(mListName);
			
			showSpinner();
			ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("ContactList");
		    query.whereEqualTo("objectId", mListId);
		    query.include("owner");
		    query.getFirstInBackground(new GetCallback<ParseObject>() {
				public void done(ParseObject contactList, ParseException e) {
					if (e == null)
					{
						list = contactList;
						JSONArray members = contactList.getJSONArray("members");
						// don't forget to include the owner
						int length = members.length();
						names = new String[length + 1];
						phones = new String[length + 1];
						responseRates = new double[length + 1];
						ids = new ArrayList<String>();
						photoURLs = new String[length + 1];
						//first add the owner
						names[0] = contactList.getParseUser("owner").getString("firstName") + 
								" " + contactList.getParseUser("owner").getString("lastName");
						phones[0] = contactList.getParseUser("owner").getString("username");
						if (contactList.getParseUser("owner").containsKey("profilePhoto"))
							photoURLs[0] = contactList.getParseUser("owner")
									.getParseFile("profilePhoto").getUrl();
						if (contactList.getParseUser("owner").containsKey("responseRate"))
							responseRates[0] = contactList.getParseUser("owner").getDouble("responseRate");
						else
							responseRates[0] = 1;
						
						//then see if the owner is the user
						mIsOwner = contactList.getParseUser("owner").getObjectId().compareTo
								(ParseUser.getCurrentUser().getObjectId()) == 0;
						//put all member names and ids into respective arrays
						for (int i = 0; i < members.length(); i++)
						{
							try {
								ids.add(members.getJSONObject(i).getString("objectId"));
							} catch (JSONException e1) {
								e1.printStackTrace();
							}
								
						}
						fetchNames();
					}
					else
					{
						showParseException(e);
						Toast.makeText(getActivity(), "Oops! We can't find this group. Maybe it was deleted.",
								Toast.LENGTH_SHORT).show();
						getActivity().finish();
					}
				}
		    });
		}
		
		public void fetchNames()
		{
			if (ids.size() == 0)
			{
				Toast.makeText(getActivity(), "Oops! This group is empty. Deleting group...", Toast.LENGTH_LONG).show();
				list.deleteInBackground(new DeleteCallback() {
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
							phones[i+1] = userList.get(i).getString("username");
							ids.set(i, userList.get(i).getObjectId());
							if (userList.get(i).containsKey("profilePhoto"))
								photoURLs[i + 1] = userList.get(i).getParseFile("profilePhoto").getUrl();
							if (userList.get(i).containsKey("responseRate"))
								responseRates[i + 1] = userList.get(i).getDouble("responseRate");
							else
								responseRates[i + 1] = 1;
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
			if (isAdded())
			{
				if (mIsOwner)
				{
					//fix that menu
					mOptionMenu.getItem(0).setVisible(true);
					mOptionMenu.getItem(1).setVisible(true);
					mOptionMenu.getItem(2).setVisible(false);
					mOptionMenu.getItem(3).setVisible(true);
					mOptionMenu.getItem(4).setVisible(false);
					
					View view = getActivity().findViewById(R.id.editGroup);
					view.setVisibility(View.VISIBLE);
					CheckBox cb = (CheckBox) view.findViewById(R.id.checkBox1);
					cb.setChecked(list.getBoolean("isVisibleToMembers"));
					cb.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							list.put("isVisibleToMembers", !list.getBoolean("isVisibleToMembers"));
							list.saveInBackground();
						}
					});
					ImageView image = (ImageView) view.findViewById(R.id.imageView1);
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
					if (list.has("photo"))
						Picasso.with(getActivity()).load(list.getParseFile("photo").getUrl()).resize(140, 140)
							.into(image);
					else
					{
						TypedArray array = getResources().obtainTypedArray(R.array
								.contact_list_images);
						Picasso.with(getActivity()).load(array.getResourceId(Math.abs(mListName.hashCode())
								% 12,R.drawable.color_balloon_8)).resize(140, 140).into(image);
						array.recycle();
					}
					
					((TextView) view.findViewById(R.id.listName)).setText(mListName);
				}
				else
				{
					//fix that menu
					mOptionMenu.getItem(0).setVisible(true);
					mOptionMenu.getItem(1).setVisible(true);
					mOptionMenu.getItem(2).setVisible(true);
					mOptionMenu.getItem(3).setVisible(false);
					mOptionMenu.getItem(4).setVisible(false);
				}
				LinearLayout ll = (LinearLayout) getActivity().findViewById(R.id.linearLayout);
				ll.removeAllViews();
				LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, 1);
				for (int i = 0; i < names.length; i++)
				{
					View member = View.inflate(getActivity(), R.layout.list_members, null);
					((TextView) member.findViewById(R.id.name)).setText(names[i]);
					((TextView) member.findViewById(R.id.number)).setText(phones[i]);
					((TextView) member.findViewById(R.id.responseRate)).setText("" + 
							(Math.round(responseRates[i]*1000)/10.0) + "%");
					if (photoURLs[i] == null)
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
					else
						Picasso.with(getActivity()).load(photoURLs[i]).resize(140, 140)
		        			.into((ImageView) member.findViewById(R.id.imageView1));
					if (mIsOwner && i > 0)
					{
						//need to offset for the owner
						((TextView) member.findViewById(R.id.id)).setText(ids.get(i - 1));
						member.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								memberToDelete = ((TextView) v.findViewById(R.id.id)).getText().toString();
								phoneToDelete = ((TextView) v.findViewById(R.id.number)).getText().toString();
								PopupMenu popup = new PopupMenu(getActivity(), v);
							    MenuInflater inflater = popup.getMenuInflater();
							    popup.setOnMenuItemClickListener((OnMenuItemClickListener) getActivity());
							    inflater.inflate(R.menu.delete_member, popup.getMenu());
							    popup.show();
							}
						});
					}
					ll.addView(member, 2*i);
					View line = new View(getActivity());
					line.setBackgroundColor(getActivity().getResources().getColor(R.color.lineGray));
					line.setLayoutParams(lp);
					ll.addView(line, 2*i + 1);
				}
				ll.invalidate();
				ll.requestLayout();
				removeSpinner();
			}
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
	        	   .setTitle("Change group name")
	               .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface d, int id) {
	                	   String str = ((EditText) dialog.findViewById(R.id.changeName))
	                			   .getText().toString();
	                	   if (!str.replaceAll("\\s+", "").equals(""))
	                	   {
	                		   mListName = str;
		                	   getActivity().setTitle(mListName);
		                	   ((TextView) getActivity().findViewById(R.id.listName)).setText(mListName);
		                	   list.put("name", mListName);
		                	   list.saveInBackground();
	                	   }
	                   }
	               })
	               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	           	    	ChangeNameFragment.this.getDialog().cancel();
	                   }
	               });
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
	
	public static class SelectMembersFromContactsFragment extends 
		NewInvitationActivity.SelectMembersFromContactsFragment {
		
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			NewInvitationActivity.mPhoneNumbers = new String[0];
		}
		
		public void onResume()
		{
			super.onResume();
			getActivity().setTitle(getActivity().getString(R.string.add_members_to));
			//fix that menu
			for (int i = 0; i < mOptionMenu.size() - 1; i++)
				mOptionMenu.getItem(i).setVisible(false);
			mOptionMenu.findItem(R.id.save).setVisible(true);
			mOptionMenu.findItem(R.id.save).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			LinearLayout ll = (LinearLayout) getActivity().findViewById(R.id.linearLayout);
			for (int i = 0; i < ll.getChildCount(); i++)
			{
				for (int j = 0; j < phones.length; j++)
				{
					View child = ll.getChildAt(i);
					if (phones[j].endsWith(((TextView) child.findViewById(R.id.number)).getText()
							.toString().replaceAll("[^[0-9]]", "")))
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
