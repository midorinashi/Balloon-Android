package com.example.balloon;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.squareup.picasso.Picasso;

//I'm extending settings so that i can do all the pictures
public class FirstPageActivity extends ProgressActivity {

	protected static ParseFile image;
	private File lastSavedFile;
	private ParseUser user;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_first_page);
		setTitle(getResources().getString(R.string.title_first_page));
		image = null;
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
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
	        	showSpinner();
	        	Bitmap bm = null;
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
	        	            bm = BitmapFactory.decodeFile(imageLocation);
	        		    }
	        		} 
	        	saveImage(bm);
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
			Bitmap bm = null;
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
            saveImage(bm);

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
        removeSpinner();
	}
	
	public void saveImage(Bitmap original)
	{
		if (original != null)
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
			image = new ParseFile("profile.png", byteArray);
			image.saveInBackground(new SaveCallback(){
				public void done(ParseException e) {
					if (e == null)
					{
						removeSpinner();
						findViewById(R.id.photo).setVisibility(View.VISIBLE);
						Picasso.with(getApplicationContext()).load(image.getUrl()).resize(160, 160)
							.into(((ImageView) findViewById(R.id.photo)));
					}
					else
						showParseException(e);
				}
			});
		}
	}
	
	//opens up the signup fragment
	public void openSignup(View view)
	{
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.replace(R.id.container, new SignupFragment());
		transaction.addToBackStack(null);
		transaction.commit();
	}
	
	//opens up the login fragment
	public void openLogin(View view)
	{
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.replace(R.id.container, new LoginFragment());
		transaction.addToBackStack(null);
		transaction.commit();
	}
	
	//signs a user up
	public void onClick(View view)
	{
		System.out.println(((EditText) findViewById(R.id.mobile)).getText().toString().replaceAll("[^0-9]", ""));
		String textForToast = "";
		if (((EditText) findViewById(R.id.mobile)).length() == 0)
			textForToast = "Please type your phone number";
		else if (((EditText) findViewById(R.id.mobile)).getText().toString().replaceAll("[^0-9]", "").length() <= 10)
			textForToast = "Please type your full phone number";
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
	
	//finds user with same phone number first
	public void saveUser()
	{
		showSpinner();
		final String mobile = "+" + (((EditText) findViewById(R.id.mobile)).getText().toString()
				.replaceAll("[^0-9]", ""));
		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.whereEqualTo("username", mobile);
		query.findInBackground(new FindCallback<ParseUser>() {
			@Override
			public void done(List<ParseUser> list, ParseException e) {
				if (e == null)
				{
					if (list.size() == 0)
					{
						user = new ParseUser();
						user.setUsername(mobile);
						user.setPassword("lol");
						user.put("isProxy", true);
						if (image != null)
							user.put("profilePhoto", image);
						user.signUpInBackground(new SignUpCallback() {
							@Override
							public void done(ParseException e) {
								if (e == null)
									saveUser(user);
								else
									signupFailure(e);
							}
						});
					}
					else if (list.get(0).getBoolean("isProxy"))
						saveUser(list.get(0));
					else
						signupFailure(e);
				}
				else
					signupFailure(e);
			}
		});
	}
	
	public void saveUser(final ParseUser u)
	{
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("mobileNumber", u.getUsername());
		
		user = u;
		ParseCloud.callFunctionInBackground("sendVerificationCode", params,
				new FunctionCallback<Object>() {
			public void done(Object o, ParseException e) {
				if (e == null)
					alertToVerify();
				else
					signupFailure(e);
			}
		});
	}
	
	public void alertToVerify()
	{
		removeSpinner();
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final FirstPageActivity context = this;
        builder.setMessage(R.string.verification_code_sent)
               .setPositiveButton(R.string.enter_code, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   FragmentTransaction transaction = context.getFragmentManager()
                       		.beginTransaction();
                	   transaction.replace(R.id.container, new VerificationFragment());
                	   transaction.addToBackStack(null);
                	   transaction.commit();
                   }
               })
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                   }
               });
        // Create the AlertDialog object and return it
        builder.create().show();
	}
	
	public void verify(View view)
	{
		final String verifyCode = (((EditText) findViewById(R.id.password)).getText().toString());
		//first, close the page
		getFragmentManager().popBackStackImmediate();
		showSpinner();
		if (user.containsKey("verificationCode"))
			verifyUser(verifyCode);
		else
			user.fetchInBackground(new GetCallback<ParseUser>() {
				@Override
				public void done(ParseUser u, ParseException e) {
					if (u != null)
					{
						user = u;
						verifyUser(verifyCode);
					}
					else
						e.printStackTrace();
				}
			});
	}
	
	public void verifyUser(String verifyCode)
	{
		if (user.getString("verificationCode").equals(verifyCode))
		{
			//time to set the user!
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("proxyUserId", user.getObjectId());
			params.put("firstName", (((EditText) findViewById(R.id.firstName)).getText().toString()));
			params.put("lastName", (((EditText) findViewById(R.id.lastName)).getText().toString()));
			final String password = (((EditText) findViewById(R.id.setPassword)).getText().toString());
			params.put("newPassword", password);
			
			ParseCloud.callFunctionInBackground("convertProxyToRegularUser", params,
					new FunctionCallback<Object>() {
				@Override
				public void done(Object o, ParseException e) {
					if (e == null)
					{
						ParseUser.logInInBackground(user.getUsername(), password, new LogInCallback() {
							@Override
							public void done(ParseUser user, ParseException e) {
								if (user != null)
									//saves the profile picture now if it exists
									startMainActivity();
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
		else
		{
			removeSpinner();
			Toast.makeText(this, "You fucked up", Toast.LENGTH_SHORT).show();
		}
	}
	
	public void login(View view)
	{
		String mobile = "+" + (((EditText) findViewById(R.id.mobile)).getText().toString())
				.replaceAll("[^0-9]", "");
		final String password = (((EditText) findViewById(R.id.password)).getText().toString());
		
		ParseUser.logInInBackground(mobile, password, new LogInCallback() {
			@Override
			public void done(ParseUser user, ParseException e) {
				if (user != null)
					startMainActivity();
				else
					loginFailure(e);
			}
			
		});
	}

	public void signupFailure(ParseException e)
	{
		showParseException(e);
		Toast.makeText(this, "What a terrible failure.", Toast.LENGTH_SHORT).show();
	}
	
	public void loginFailure(ParseException e)
	{
		showParseException(e);
		Toast.makeText(this, "Invalid username or password.", Toast.LENGTH_SHORT).show();
	}
	
	public void startMainActivity()
	{
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		removeSpinner();
		finish();
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
			if (image != null)
			{
				Picasso.with(getActivity()).load(image.getUrl()).resize(160, 160).into((ImageView) 
						getActivity().findViewById(R.id.photo));
			}
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
	
	public static class VerificationFragment extends Fragment {

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_verify, container,
					false);
			return rootView;
		}
		
		public void onResume()
		{
			super.onResume();
			InputMethodManager imm = (InputMethodManager)getActivity()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput((EditText) getActivity().findViewById(R.id.password),
					InputMethodManager.SHOW_IMPLICIT);
		}
	}
}
