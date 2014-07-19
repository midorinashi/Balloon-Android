package com.example.balloon;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
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
import android.widget.Toast;

import com.parse.ParseUser;


public class SettingsActivity extends ActionBarActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_lists);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
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
			if (((EditText) findViewById(R.id.mobileNumber)).length() == 0)
				textForToast = "Please type your phone number.";
			else if (((EditText) findViewById(R.id.firstName)).length() == 0)
				textForToast = "Please type your first name.";
			else if (((EditText) findViewById(R.id.lastName)).length() == 0)
				textForToast = "Please type your last name.";
			else if (((EditText) findViewById(R.id.setPassword)).length() == 0)
				textForToast = "Please type a password.";
			else if (((EditText) findViewById(R.id.setPassword)).getText().toString()
					.equals(((EditText) findViewById(R.id.confirmPassword)).getText().toString()))
				textForToast = "Passwords do not match.";
			else
			{
				//saveProfile();
				return true;
			}
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
	        	            ImageView view = (ImageView) findViewById(R.id.photo);
	        	            Bitmap bm = BitmapFactory.decodeFile(imageLocation);
	        	            view.setImageBitmap(bm);
	        	            view.setVisibility(ImageView.VISIBLE);
	        		    }
	        		} 
	            return true;
	        case R.id.action_photo_take:
	        	Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	        	intent.putExtra(MediaStore.EXTRA_OUTPUT,
	        			android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
	        	startActivityForResult(intent, 0);
	        	return true;
	        case R.id.action_photo_library:
	        	intent = new Intent(Intent.ACTION_PICK, 
	        			android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
	        	startActivityForResult(intent, 0);
	        	return true;
	        default:
	            return super.onContextItemSelected(item);
	    }
	}
	
	@Override
	//this is how we get the picture
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            // Image captured and saved to fileUri specified in the Intent
            ImageView view = (ImageView) findViewById(R.id.photo);
            view.setImageURI(data.getData());
            view.setVisibility(ImageView.VISIBLE);
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
	
	//need to change image uri to a byte[] so i can save to parse
	private void saveImage(Uri image)
	{
		InputStream iStream = null;
		try {
			iStream = getContentResolver().openInputStream(image);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("fuck");
			e.printStackTrace();
		}
		ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
	    int bufferSize = 1024;
	    byte[] buffer = new byte[bufferSize];

	    int len = 0;
	    try {
			while ((len = iStream.read(buffer)) != -1)
			    byteBuffer.write(buffer, 0, len);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("another fuck");
			e.printStackTrace();
		}
	    byte[] toSave = byteBuffer.toByteArray();
	    
	    //updating text view just doesn't work
	    /*
	    TextView tv = ((TextView) findViewById(R.id.finalEditPhotoText));
	    tv.setText(str);
	    tv.invalidate();
	    tv.requestLayout();
	    */
	}
	
	public static class SettingsFragment extends Fragment
	{
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_settings,
					container, false);
			return rootView;
		}
		
		public void onResume()
		{
			super.onResume();

			getActivity().findViewById(R.id.profilePhoto).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					getActivity().registerForContextMenu(v); 
				    getActivity().openContextMenu(v);
				    getActivity().unregisterForContextMenu(v);
				}
			});
		}
	}
}
