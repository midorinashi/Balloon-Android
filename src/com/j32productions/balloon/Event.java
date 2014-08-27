package com.j32productions.balloon;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Event extends ViewGroup
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
	
	//Views for easy access
	private TextView mCreatorView;
	private TextView mAgendaView;
	private TextView mVenueInfoView;
	private ImageView mVenuePhotoView;
	private TextView mTimeToRSVPView;
	private ImageView mArrowView;

	private MoreInfoObserver mObserver;

	private Timer mTimer;

	private Handler mHandler;

	//All constructors because I don't know what's the difference between the second and third
	public Event(Context context)
	{
		super(context);
		initViews();
		mIsVisible = false;
	}
	
	public Event(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initViews();
		setAttributes(context, attrs);
	}
	
	public Event(Context context, AttributeSet attrs, int defStyleAttr)
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
		//This is the entire box where the invitation goes in
		setPadding(5, 5, 10, 5);
		setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 110));
		setFocusable(true);
		setClickable(true);
		setFocusableInTouchMode(true);
		setOnClickListener(new mListener());

		RelativeLayout.LayoutParams wrapContent = new RelativeLayout.LayoutParams
				(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		//Initializes (?) the font - obviously
		//Find the font please
		Typeface tf = Typeface.create("Comic Sans", Typeface.NORMAL);
		
		//Image
		mVenuePhotoView = new ImageView(getContext());
		mVenuePhotoView.setAdjustViewBounds(true);
		mVenuePhotoView.setMaxHeight(50);
		mVenuePhotoView.setMaxWidth(50);
		mVenuePhotoView.setId(1);
		mVenuePhotoView.setLayoutParams(wrapContent);
		addView(mVenuePhotoView);
		
		//Inviter name
		mCreatorView = new TextView(getContext());
		mCreatorView.setTextColor(getResources().getColor(R.color.black));
		mCreatorView.setPaintFlags(Paint.ANTI_ALIAS_FLAG);
		mCreatorView.setTextSize(12);
		mCreatorView.setTypeface(tf);
		mCreatorView.setId(2);
		addView(mCreatorView);
		
		//Agenda
		mAgendaView = new TextView(getContext());
		mAgendaView.setTextColor(getResources().getColor(R.color.gray));
		mAgendaView.setPaintFlags(Paint.ANTI_ALIAS_FLAG);
		mAgendaView.setTextSize(10);
		mAgendaView.setTypeface(tf);
		mAgendaView.setId(3);
		mAgendaView.setLayoutParams(wrapContent);
		addView(mAgendaView);
		
		//Location
		mVenueInfoView = new TextView(getContext());
		mVenueInfoView.setTextColor(getResources().getColor(R.color.blue));
		mVenueInfoView.setPaintFlags(Paint.ANTI_ALIAS_FLAG);
		mVenueInfoView.setTextSize(12);
		mVenueInfoView.setTypeface(tf);
		mVenueInfoView.setId(4);
		mVenueInfoView.setLayoutParams(wrapContent);
		addView(mVenueInfoView);
		
		//Time to RSVP
		mTimer = new Timer("RSVPTimer");
		mTimeToRSVPView = new TextView(getContext());
		mTimeToRSVPView.setTextColor(getResources().getColor(R.color.red));
		mTimeToRSVPView.setPaintFlags(Paint.ANTI_ALIAS_FLAG);
		mTimeToRSVPView.setTextSize(12);
		mTimeToRSVPView.setTypeface(tf);
		mTimeToRSVPView.setId(5);
		mTimeToRSVPView.setLayoutParams(wrapContent);
		mTimeToRSVPView.setText("                           ");
		addView(mTimeToRSVPView);

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
		
		//that arrow
		mArrowView = new ImageView(getContext());
		mArrowView.setLayoutParams(wrapContent);
		addView(mArrowView);
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
		return mCreatorView.getText().toString();
	}
	
	public String getAgenda()
	{
		return mAgendaView.getText().toString();
	}
	
	public String getVenueInfo()
	{
		return mVenueInfoView.getText().toString();
	}
	
	//Is it possible to get the image URI? Check later
	
	public Date getExpiresAt()
	{
		return mExpiresAt;
	}
	
	public String getTimeToRSVP()
	{
		return mTimeToRSVPView.getText().toString();
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
		mCreatorView.setText(creator);
		mCreatorView.invalidate();
		mCreatorView.requestLayout();
		invalidate();
		requestLayout();
		//System.out.println(getCreator());
	}
	
	public void setAgenda(String agenda)
	{
		mAgendaView.setText(agenda);
		mAgendaView.invalidate();
		mAgendaView.requestLayout();
		invalidate();
		requestLayout();
	}

	public void setVenueInfo(String venueInfo)
	{
		mVenueInfoView.setText(venueInfo);
		mVenueInfoView.invalidate();
		mVenueInfoView.requestLayout();
		invalidate();
		requestLayout();
	}
	
	public void setVenuePhoto(Context context, String url)
	{
		Picasso.with(context).load(url).into(mVenuePhotoView);
		mVenuePhotoView.invalidate();
		mVenuePhotoView.requestLayout();
		invalidate();
		requestLayout();
	}
	
	public void setVenuePhoto(Uri uri)
	{
		mVenuePhotoView.setImageURI(uri);
		mVenuePhotoView.invalidate();
		mVenuePhotoView.requestLayout();
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
	
	//Right now, this just defaults to max photo height. This is because the photo square is larger than
	//everything else! It might be better to use the text though
	@Override
	protected int getSuggestedMinimumHeight()
	{
		return (int) (mVenuePhotoView.getHeight());
	}
	
	/*
	 * This gives the additional padding for the text. It's the white space above the text after the padding.
	 * This is because the top of the picture does not line up with the top of the text. Instead, their centers
	 * (for the heights) should line up. They don't right now.... Maybe.
	 */
	private float getTextPaddingTop()
	{
		return ((float) getSuggestedMinimumHeight() - mCreatorView.getTextSize() - mAgendaView.getTextSize()
				- mVenueInfoView.getTextSize() - mTimeToRSVPView.getTextSize())/2.0f;
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
	 * um....
	 * I'm just using this to get things to line up and all. It looks messy, but fear not.
	 */
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
        super.onSizeChanged(w, h, oldw, oldh);
        //first deal with the picture - always a box on the left side
        //not using setPosition because I want to use photoBounds to measure the other things
		//float length = getSuggestedMinimumHeight();
		float length = 100.0f;
        RectF photoBounds = new RectF(0.0f, 0.0f, length, length);
		photoBounds.offset(getPaddingLeft(), getPaddingTop());
		mVenuePhotoView.layout((int) photoBounds.left, (int) photoBounds.top,
				(int) photoBounds.right, (int) photoBounds.bottom);
		
		//next, deal with all the text. 
		//i'm using getPaddingLeft() because i want the space around the photo to be all the same on all sides
		float textLeftBounds = photoBounds.right + getPaddingLeft();
		//System.out.println("textLeftBounds: "+textLeftBounds);
		//using setTextPositionInLayout, I will keep finding where the next piece of text should go
		float textNextTopBounds = getPaddingTop() + getTextPaddingTop();
		//System.out.println("creator:");
		textNextTopBounds = setPositionInLayout(mCreatorView, textLeftBounds, textNextTopBounds);
		//System.out.println("agenda:");
		textNextTopBounds = setPositionInLayout(mAgendaView, textLeftBounds, textNextTopBounds);
		//System.out.println("venue info:");
		textNextTopBounds = setPositionInLayout(mVenueInfoView, textLeftBounds, textNextTopBounds);
		//System.out.println("time to rsvp:");
		textNextTopBounds = setPositionInLayout(mTimeToRSVPView, textLeftBounds, textNextTopBounds);
		
		//lastly, deal with that arrow, look at that math witchcraft
		//i mean, if you look at the image, it's kind of clear what i'm trying to go for... i think?
		setPositionInLayout(mArrowView, getWidth() - mArrowView.getWidth() - getPaddingRight(),
				photoBounds.centerX() - mArrowView.getHeight() / 2);
		//System.out.println("onSizeChanged completed");
	}
	
	/*
	 * This function sets the view to display at a certain location. We give the x-y coordinate of the top left
	 * corner. This does NOT work if you don't add the view to the viewgroup first. Trust me on this one.
	 * It returns the bottom bound of this view so that i know where the next piece of text goes
	 */
	private float setPositionInLayout(View view, float textLeftBounds, float textTopBounds)
	{
		view.measure(getMeasuredWidth(), getMeasuredHeight());
		RectF viewBounds = new RectF(0.0f, 0.0f, view.getMeasuredWidth(), view.getMeasuredHeight());
		viewBounds.offset(textLeftBounds, textTopBounds);
		view.layout((int) viewBounds.left, (int) viewBounds.top,
				(int) viewBounds.right, (int) viewBounds.bottom);
		//System.out.println("view's layoutparams: "+view.getLayoutParams().height + " " +
		//		view.getLayoutParams().width);
		//System.out.println("view's measured width: "+view.getMeasuredWidth());
		//System.out.println("view's layout set to " + (int)viewBounds.left+" "+(int)viewBounds.top +
		//		" " + (int)viewBounds.right+" "+(int)viewBounds.bottom);
		return viewBounds.bottom;
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
	
	public void onClick()
	{
		if (mObserver != null)
			mObserver.moreInfo(this);
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
