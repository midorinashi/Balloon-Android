package com.example.balloon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class MainActivity extends ActionBarActivity
{
	private boolean firstTimeUser;
	private int user;
	private final String CLIENT_ID = "QIVN42TMR5KLGEA15W1VK0ISG4V3DOT0J4XAZVZ033HQK2MH";
	private final String CLIENT_SECRET = "YDYDI1JXQJPCVAWM1ZDMHRCCCAEJY5DT3TUTLUXU2JZ5G2AJ";
	private final String callback_URL = "fb527538684032224://foursquare";
	private final String DEFAULT_VERSION = "20140131";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//initialize parse
		Parse.initialize(this, "iXEPNEZfJXoEOIayxLgBBgpShMZBTj7ReVoi1eqn",
				"GHtE0svPk0epFG4olYnFTnnDtmARHtENXxXuHoXp");
		ParseUser.logInInBackground("+19739002782", "cat", new LogInCallback() {
			  public void done(ParseUser user, ParseException e) { 
			    if (user != null) {
			      // Hooray! The user is logged in.
			    	onCreateInvitationsFragment();
			    } else {
			      // Signup failed. Look at the ParseException to see what happened.
			    	System.out.println("fuck");
			    }
			  }
			});
		
		setContentView(R.layout.activity_main);
		/*
		 * Here, we have three options at start up. First, this is the first time the app has been used.
		 * In this case, we load up the WelcomeVC. Second, the app has been run before, but there is no
		 * user logged in. So this goes to the login fragment. Last, we have an actual user logged in,
		 * so we go straight to the Invitation fragment.
		 * Obviously, if the app crashed before, try to get to the page that it crashed. No guarentees.
		 * 
		 * TO DO:
		 * Find out how to find whether this is the first time app has run, and how to find current user
		 * These are currently hard coded.
		 */ 
		firstTimeUser = false;
		user = 1;
		//Loads WelcomeVC. I need to find out how to determine if the app was just installed
		if (firstTimeUser)
		{
			
		}
		
		//I also need to figure out how to make a login screen! With phone numbers, ocourse
		else if (user == 0)
		{
			
		}
		
		//Loads the Invitations Screen
		/*
		else if (savedInstanceState == null)
		{
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new InvitationsFragment()).commit();
		}
		*/
	}
	
	public void onCreateInvitationsFragment()
	{
		getSupportFragmentManager().beginTransaction()
		.add(R.id.container, new InvitationsFragment()).commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings)
		{
			return true;
		}
		if (id == R.id.action_new_invitation)
		{
			newInvitation();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void newInvitation()
	{
		Intent intent = new Intent(this, NewInvitationActivity.class);
		startActivity(intent);
	}
	
	/**
	 * This is the invitations screen. I think it makes sense for this to be the main activity.
	 */
	public static class InvitationsFragment extends Fragment
	{	
		public InvitationsFragment()
		{
			
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_invitations, container,
					false);
			
			//CRAPPY HARDCODED EVENTS 8D
			/*
			for (int i = 0; i < 7; i++)
			{
				LinearLayout lin = (LinearLayout) rootView.findViewById(R.id.invitations);
				Event invite = new Event(getActivity());
				invite.setCreator("Kevin");
				invite.setAgenda("Test 1, test 2");
				invite.setVenueInfo(""+i);
				invite.setVenuePhoto("http://i.stack.imgur.com/c5SvX.png");
				invite.setExpiresAt((long) 2000000000);
				if (i%2==1)
					invite.setBackgroundColor(getResources().getColor(R.color.lightBlue));
				invite.setId(i);
				invite.setMoreInfoObserver(new MoreInfoObserver(){
					public void moreInfo(Event event) {
						
					}
				});
				lin.addView(invite);
			}
			*/
			return rootView;
		}
		
		public void onResume()
		{
			super.onResume();
			refreshScreen();
		}
		
		public void onStop()
		{
			super.onStop();
		}
		
		/*
		 * This refreshes the invitations. We need to get the invitations that have been updated since the last
		 * refresh, that the user has not RSVPed to, and that have not expired.
		 * We need to order them by expiration date. So, we just have to keep track of the expiration time of the
		 * invitation at the top. Binary Search and insert 
		 * We also need a string list of IDs. Probably should keep a list of 100. Who even gets 100 invites 
		 * in a day...
		 * 
		 * Right now it just gets all the invites cause EFFICIENCY
		 */
		public void refreshScreen()
		{
			ParseQuery<ParseObject> query = ParseQuery.getQuery("Meetup"); 
			query.orderByAscending("expiresAt");
			
			//get only ones that haven't expired
			query.whereGreaterThan("expiresAt", new Date());
			
			//get only ones that i am invited to
			ArrayList<ParseUser> currentUser = new ArrayList<ParseUser>();
			currentUser.add(ParseUser.getCurrentUser());
			query.whereContainsAll("invitedUsers", currentUser);
			
			query.findInBackground(new FindCallback<ParseObject>(){
				public void done(List<ParseObject> eventList, ParseException e) {
					if (e != null)
						System.out.println(e);
					else
					{
						List<ParseObject> list = new ArrayList<ParseObject>();
						System.out.println("eventList size is "+eventList.size());
						for (ParseObject event : eventList)
						{
							list.add(event);
							System.out.println("event added");
						}
						makeList(list);
					}
				}
			});
		}
		
		//Takes the list of meetups from parse query and, well, lists them.
		// TODO I need to make a list of events to avoid memory leaks, also stop all timers
		public void makeList(List<ParseObject> list)
		{
			LinearLayout lin = (LinearLayout) getActivity().findViewById(R.id.invitations);
			//removes all the views for now because EFFICIENCY WHAT
			lin.removeAllViews();
			ParseUser creator = new ParseUser();
			
			for (int i = 0; i < list.size(); i++)
			{
				ParseObject meetup = list.get(i);
				Event event = new Event(getActivity());
				event.setObjectId(meetup.getObjectId());
				try {
					creator = meetup.getParseUser("creator").fetchIfNeeded();
					event.setCreator(creator.getString("firstName")+" "+creator.getString("lastName"));
				} catch (ParseException e) {
					// Auto-generated catch block
					e.printStackTrace();
				}
				event.setAgenda(meetup.getString("agenda"));
				try {
					event.setVenueInfo(meetup.getJSONObject("venueInfo").getString("name"));
				} catch (JSONException e) {
					// Auto-generated catch block
					e.printStackTrace();
				}
				//invite.setVenuePhoto(event.getParseFile(key));
				event.setExpiresAt(meetup.getDate("expiresAt"));
				
				if (i % 2 == 0)
					event.setBackgroundColor(getResources().getColor(R.color.lightBlue));
				lin.addView(event);
			}
		}
	}
}
