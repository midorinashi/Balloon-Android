package com.example.balloon;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Fragment;
import android.content.Intent;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseUser;
import com.parse.PushService;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;


public class SettingsActivity extends ProgressActivity {
	protected static Bitmap bm;
	private File lastSavedFile;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_lists);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new SettingsFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_save)
		{
			String textForToast = "";
			
			if (((EditText) findViewById(R.id.firstName)).length() == 0)
				textForToast = "Please type your first name.";
			else if (((EditText) findViewById(R.id.lastName)).length() == 0)
				textForToast = "Please type your last name.";
			else if (!((EditText) findViewById(R.id.setPassword)).getText().toString()
					.equals(((EditText) findViewById(R.id.confirmPassword)).getText().toString()))
				textForToast = "Passwords do not match.";
			
			//if there were no problems, continue
			if (textForToast.equals(""))
			{
				saveUser();
				//first deal with getting the image
            	System.out.println("clicked the save button");
			}
			else
				Toast.makeText(this, textForToast, Toast.LENGTH_SHORT).show();
			return true;
			
		}
		if (id == R.id.action_invites)
		{
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
			return true;
		}
		if (id == R.id.action_lists)
		{
			Intent intent = new Intent(this, ContactListsActivity.class);
			startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			return true;
		}
		if (id == R.id.action_create)
		{
			Intent intent = new Intent(this, NewInvitationActivity.class);
			startActivity(intent);
			return true;
		}
		if (id == R.id.action_rsvp)
		{
			Intent intent = new Intent(this, RSVPEventsActivity.class);
			startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void saveUser()
	{
		showSpinner();
		ParseUser user = ParseUser.getCurrentUser();
		user.put("firstName", ((EditText) findViewById(R.id.firstName)).getText().toString());
		user.put("lastName", ((EditText) findViewById(R.id.lastName)).getText().toString());
		String password = ((EditText) findViewById(R.id.setPassword)).getText().toString();
		if (!password.equals(""))
			user.setPassword(password);
		
		if (bm != null)
		{
			System.out.println("bitmap exists");
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
			byte[] byteArray = stream.toByteArray();
			final ParseFile image = new ParseFile("profile.png", byteArray);
			image.saveInBackground(new SaveCallback(){
				public void done(ParseException e) {
					if (e == null)
						saveUser(image);
					else
						showParseException(e);
				}
			});
		}
		else
		{
			System.out.println("bitmap does not exist");
			saveUser(null);
		}
	}
	
	public void saveUser(ParseFile image)
	{
		if (image != null)
		{
			ParseUser.getCurrentUser().put("profilePhoto", image);
		}
		final SettingsActivity context = this;
		ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
			public void done(ParseException e) {
				if (e == null)
				{
					removeSpinner();
					Toast.makeText(context, "Profile saved!", Toast.LENGTH_SHORT).show();
				}
				else
					showParseException(e);
			}
		}); 
	}
	
	public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.phone_photo_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
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
	        	            ParseImageView view = (ParseImageView) findViewById(R.id.photo);
	        	            bm = BitmapFactory.decodeFile(imageLocation);
	        	            view.setImageBitmap(bm);
	        	            view.setVisibility(ImageView.VISIBLE);
	        		    }
	        		} 
        		removeSpinner();
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
				bm = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
				System.out.println(bm.toString());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
            Picasso.with(this).load(uri).into((ImageView) findViewById(R.id.photo));
            removeSpinner();
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
	
	//this will log out the user
	public void onClick(View view)
	{
		PushService.unsubscribe(this, "");
		PushService.unsubscribe(this, "u" + ParseUser.getCurrentUser().getObjectId());
		ParseUser.logOut();
		Intent intent = new Intent(this, FirstPageActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
	}
	
	public static class SettingsFragment extends Fragment
	{
		private boolean toRefresh;
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_settings,
					container, false);
			bm = null;
			toRefresh = true;
			return rootView;
		}
		
		public void onResume()
		{
			super.onResume();
			if (toRefresh)
				refresh();
			getActivity().findViewById(R.id.profilePhoto).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					getActivity().registerForContextMenu(v); 
				    getActivity().openContextMenu(v);
				    getActivity().unregisterForContextMenu(v);
				}
			});
		}
		
		//make sure all info is equal to parse's version
		public void refresh()
		{
			ParseUser user = ParseUser.getCurrentUser();
			((TextView) getActivity().findViewById(R.id.mobileNumber)).setText(user.getUsername());
			EditText et = (EditText) getActivity().findViewById(R.id.firstName);
			et.setText(user.getString("firstName"));
			et = (EditText) getActivity().findViewById(R.id.lastName);
			et.setText(user.getString("lastName"));
			if (user.containsKey("profilePhoto"))
			{
				Picasso.with(getActivity()).load(user.getParseFile("profilePhoto").getUrl())
					.resize(160, 160).into((ImageView) getActivity().findViewById(R.id.photo));
			}
			toRefresh = false;
		}
	}
}
