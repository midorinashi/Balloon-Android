package com.example.balloon;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class InviteCard extends ViewGroup
{
	//Expiration date in milliseconds
	private Date mExpiresAt;
	
	private boolean mHasResponded;
	private boolean mWillAttend;
	private boolean mIsCreator;
	
	//Whether the view is visible - get rid of it when event expires
	private boolean mIsVisible;
	
	//Parse ID of the event so I can access later
	private String mObjectId;

	private MoreInfoObserver mObserver;

	private Timer mTimer;

	private Handler mHandler;

	//All constructors because I don't know what's the difference between the second and third
	public InviteCard(Context context)
	{
		super(context);
		initViews();
		mIsVisible = false;
	}
	
	public InviteCard(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initViews();
		setAttributes(context, attrs);
	}
	
	public InviteCard(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		initViews();
		setAttributes(context, attrs);
	}
	
	/*
	 * This initializes all the Views (image, the four text fields, and the arrow the Paint objects for each text)
	 *  and does basic stuff for the ViewGroup stuff
	 *  Maybe I can use this invitation for the more info page? I would just have to delete the arrow and let
	 *  all the text fields be longer....
	 *  There's a lot of hardcoding right now: padding, text size, image max size. Clearly, this needs fixing
	 */
	public void initViews()
	{
		System.out.println("Adding view");
		View view = inflate(getContext(), R.layout.invite_card, this);
		//view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		addView(view);
		
		//Time to RSVP
		mTimer = new Timer("RSVPTimer");
		final TextView mTimeToRSVPView = (TextView) findViewById(R.id.timer);

		//Handles changing the RSVP time every second with the timer
		mHandler = new Handler() {
			public void handleMessage(Message message)
			{
				Date now = new Date();
				long timeToRSVP = mExpiresAt.getTime() - now.getTime();
				String time = "" + (int)timeToRSVP/(60*60*1000) + ":";
				int minutes = (int)(timeToRSVP/(60*1000))%60;
				if (minutes < 10)
					time = time + "0";
				time = time + minutes + ":";
				int seconds = (int)(timeToRSVP/1000)%60;
				if (seconds < 10)
					time = time+ "0";
				time = time + seconds;
				mTimeToRSVPView.setText(time);
				mTimeToRSVPView.invalidate();
				mTimeToRSVPView.requestLayout();
				//invalidate();
				//requestLayout();
			}
		};
	}
	
	/*
	 * This is for hardcoded xml stuff (why would you do that for these invitations?
	 * But just incase.
	 * It parses through all the possible attributes and then assigns them.
	 */
	public void setAttributes(Context context, AttributeSet attrs)
	{
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Event, 0, 0);
		try
		{
			setCreator(a.getString(R.styleable.Event_creator));
			setAgenda(a.getString(R.styleable.Event_agenda));
			setVenueInfo(a.getString(R.styleable.Event_venueInfo));
			
			//figure out the photo stuff later plz
			//Code gotten from http://stackoverflow.com/questions/9662914/android-url-to-uri
			//I'm just going to take it out for now
			//setVenuePhoto(Uri.parse(a.getString(R.styleable.Event_venuePhotoURL)));
			
			//just gets the expiration - will calculate the text later
			//setExpiresAt(Long.parseLong(a.getString(R.styleable.Event_expiresAt)));
			//check to see if it's not expired
			mIsVisible = (new Date()).after(mExpiresAt);
		}
		finally
		{
			a.recycle();
		}
	}
	
	public String getObjectID()
	{
		return mObjectId;
	}
	
	//All the accessors
	public String getCreator()
	{
		return ((TextView) findViewById(R.id.creator)).getText().toString();
	}
	
	public String getAgenda()
	{
		return ((TextView) findViewById(R.id.agenda)).getText().toString();
	}
	
	public String getVenueInfo()
	{
		return ((TextView) findViewById(R.id.venueInfo)).getText().toString();
	}
	
	//Is it possible to get the image URI? Check later
	
	public Date getExpiresAt()
	{
		return mExpiresAt;
	}
	
	public String getTimeToRSVP()
	{
		return ((TextView) findViewById(R.id.timer)).getText().toString();
	}
	
	public boolean getIsVisible()
	{
		return mIsVisible;
	}
	
	public boolean getHasResponded()
	{
		return mHasResponded;
	}
	
	public boolean getWillAttend()
	{
		return mWillAttend;
	}
	
	public boolean getIsCreator()
	{
		return mIsCreator;
	}

	//All the setters (? Is that what you call them?)
	public void setObjectId(String id)
	{
		mObjectId = id;
	}
	
	//Also, I need to adjust all pieces of text if they get too long
	public void setCreator(String creator)
	{
		TextView view = ((TextView) findViewById(R.id.creator));
		view.setText(creator);
		view.invalidate();
		view.requestLayout();
		invalidate();
		requestLayout();
		//System.out.println(getCreator());
	}
	
	public void setAgenda(String agenda)
	{
		TextView view = ((TextView) findViewById(R.id.agenda));
		view.setText(agenda);
		view.invalidate();
		view.requestLayout();
		invalidate();
		requestLayout();
	}

	public void setVenueInfo(String venueInfo)
	{
		TextView view = ((TextView) findViewById(R.id.venueInfo));
		view.setText(venueInfo);
		view.invalidate();
		view.requestLayout();
		invalidate();
		requestLayout();
	}
	
	public void setVenuePhoto(Context context, String url)
	{
		ImageView view = ((ImageView) findViewById(R.id.image));
		Picasso.with(context).load(url).into(view);
		view.invalidate();
		view.requestLayout();
		invalidate();
		requestLayout();
	}
	
	//No need to invalidate this one, because this view will always change
	public void setExpiresAt(Date date)
	{
		System.out.println("expiration setting");
		if (mExpiresAt == null)
		{
			System.out.println("YO");
			mExpiresAt = date;
			mTimer.schedule(new RSVPTimerTask(), 10, 1000);
		}
		else
			mExpiresAt = date;
		System.out.println(mExpiresAt);
	}
	
	//Should toggle entire view group's visibility - so invalidates whole group
	public void setVisibility(boolean isVisible)
	{
		mIsVisible = isVisible;
		invalidate();
		requestLayout();
	}
	
	public void setHasResponded(boolean b)
	{
		mHasResponded = b;
	}
	
	public void setWillAttend(boolean b)
	{
		mWillAttend = b;
	}
	
	public void setIsCreator(boolean b)
	{
		mIsCreator = b;
	}
	
	//the padding is symmetric, i'm not really sure why i have it but just in case i guess so i'll comment it out
	/*
	private float getTextPaddingBottom()
	{
		return getTextPaddingTop();
	}
	*/
	
	//I still don't know why I need this
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom)
	{
		
	}
	
	/*
	 * I want to make this whole event clickable so that when I click on it, I go to the event info screen
	 * Obviously, I have to make the info screen. But right now I'm lazy.
	 * I also need to determine the difference between scrolling and clicking
	 */
	
	public void setMoreInfoObserver(MoreInfoObserver observer)
	{
		mObserver = observer;
	}
	
	/*This is the touch listener. It does... well, it should make clicks go to the more info page. I dont' know
	 * if i have to deal with scrolls
	 */
	public class mListener implements View.OnClickListener
	{
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getContext(), MoreInfoActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString("objectId", mObjectId);
			bundle.putBoolean("hasResponded", mHasResponded);
			bundle.putBoolean("willAttend", mWillAttend);
			bundle.putBoolean("isCreator", mIsCreator);
			intent.putExtras(bundle);
			System.out.println("The id is: " + mObjectId);
			((Activity) getContext()).startActivity(intent);
			
		}
	}
	
	public class RSVPTimerTask extends TimerTask
	{ 
		@Override
		public void run() {
			mHandler.obtainMessage(1).sendToTarget();
		}
	}
}
