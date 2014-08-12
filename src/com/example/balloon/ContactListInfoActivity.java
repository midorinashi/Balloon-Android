package com.example.balloon;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

public class ContactListInfoActivity extends ProgressActivity {

	public static String mListName;
	public static String mListId;
	protected static ParseObject list;
	private File lastSavedFile;
	protected ParseFile mContactListImage;
	private static ContextMenu mMenu;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_list_info);
		
		mListName = getIntent().getExtras().getString("listName");
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
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_add)
		{
			//TODO
		}
		else if (id == R.id.action_send)
		{
			Intent intent = new Intent(this, NewInvitationFromGroupActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString("listId", mListId);
			intent.putExtras(bundle);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
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
			//TODO ?????
			Picasso.with(this).load(mContactListImage.getUrl()).resize(140, 140).into(view);
			System.out.println("What");
		}
		list.put("photo", mContactListImage);
		list.saveInBackground();
	}
	
	public static class ShowListFragment extends ProgressFragment {
		protected String[] names;
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
						ids = new ArrayList<String>();
						photoURLs = new String[length + 1];
						//first add the owner
						names[0] = contactList.getParseUser("owner").getString("firstName") + 
								" " + contactList.getParseUser("owner").getString("lastName");
						if (contactList.getParseUser("owner").containsKey("profilePhoto"))
							photoURLs[0] = contactList.getParseUser("owner")
									.getParseFile("profilePhoto").getUrl();
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
			if (isAdded())
			{
				if (mIsOwner)
				{
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
					//TODO
					((TextView) view.findViewById(R.id.listName)).setText(mListName);
				}
				LinearLayout ll = (LinearLayout) getActivity().findViewById(R.id.linearLayout);
				ll.removeAllViews();
				LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, 1);
				for (int i = 0; i < names.length; i++)
				{
					View member = View.inflate(getActivity(), R.layout.list_members, null);
					((TextView) member.findViewById(R.id.name)).setText(names[i]);
					Picasso.with(getActivity()).load(photoURLs[i]).resize(140, 140)
		        		.into((ImageView) member.findViewById(R.id.imageView1));
					ll.addView(member, 2*i);
					View line = new View(getActivity());
					line.setBackgroundColor(getActivity().getResources().getColor(R.color.lightGray));
					line.setLayoutParams(lp);
					ll.addView(line, 2*i + 1);
				}
				ll.invalidate();
				ll.requestLayout();
			}
		}
	}
}
