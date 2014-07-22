package com.example.balloon;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.squareup.picasso.Picasso;

//I'm extending settings so that i can do all the pictures
public class FirstPageActivity extends ActionBarActivity {

	protected static Bitmap bm;
	private File lastSavedFile;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_first_page);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new FirstPageFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.first_page, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
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
	        	            ParseImageView view = (ParseImageView) findViewById(R.id.photo);
	        	            bm = BitmapFactory.decodeFile(imageLocation);
	        	            view.setImageBitmap(bm);
	        	            view.setVisibility(ImageView.VISIBLE);
	        		    }
	        		} 
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
	
	//opens up the signup fragment
	public void openSignup(View view)
	{
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.container, new SignupFragment());
		transaction.addToBackStack(null);
		transaction.commit();
	}
	
	//opens up the login fragment
	public void openLogin(View view)
	{
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.container, new LoginFragment());
		transaction.addToBackStack(null);
		transaction.commit();
	}
	
	//signs a user up
	public void onClick(View view)
	{
		String textForToast = "";
		if (((EditText) findViewById(R.id.mobile)).length() == 0)
			textForToast = "Please type your phone number";
		else if (((EditText) findViewById(R.id.firstName)).length() == 0)
			textForToast = "Please type your first name.";
		else if (((EditText) findViewById(R.id.lastName)).length() == 0)
			textForToast = "Please type your last name.";
		else if (((EditText) findViewById(R.id.setPassword)).length() == 0)
			textForToast = "Please set your password.";
		else if (((EditText) findViewById(R.id.confirmPassword)).length() == 0)
			textForToast = "Please confirm your password.";
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
	}
	
	public void saveUser()
	{
		//TODO Need to add pluses and country/area code EVERYWHEREEEEE
		final ParseUser user = new ParseUser();
		user.setUsername(((EditText) findViewById(R.id.mobile)).getText().toString());
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
						saveUser(user, image);
					else
						e.printStackTrace();
				}
			});
		}
		else
		{
			System.out.println("bitmap does not exist");
			saveUser(user, null);
		}
	}
	
	public void saveUser(ParseUser user, ParseFile image)
	{
		if (image != null)
		{
			user.put("profilePhoto", image);
		}
		final FirstPageActivity context = this;
		user.signUpInBackground(new SignUpCallback() {

			@Override
			public void done(ParseException e) {
				if (e == null)
				{
					Intent intent = new Intent(context, MainActivity.class);
					context.startActivity(intent);
					context.finish();
				}
				else
					e.printStackTrace();
			}
			
		});
	}
	
	public void login(View view)
	{
		final ActionBarActivity context = this;
		System.out.println(((EditText) findViewById(R.id.mobile)).getText().toString());
		System.out.println(((EditText) findViewById(R.id.password)).getText().toString());
		ParseUser.logInInBackground(((EditText) findViewById(R.id.mobile)).getText().toString(),
				((EditText) findViewById(R.id.password)).getText().toString(), new LogInCallback() {
			@Override
			public void done(ParseUser user, ParseException e) {
				if (user != null)
				{
					Intent intent = new Intent(context, MainActivity.class);
					context.startActivity(intent);
					context.finish();
				}
				else
					Toast.makeText(context, "Invalid username or password",
							Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	public static class FirstPageFragment extends Fragment {

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_first_page, container,
					false);
			return rootView;
		}
	}

	public static class SignupFragment extends Fragment {

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_signup, container,
					false);
			bm = null;
			return rootView;
		}
	}
	
	public static class LoginFragment extends Fragment {

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_login, container,
					false);
			return rootView;
		}
	}
}
