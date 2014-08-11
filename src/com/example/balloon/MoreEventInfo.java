package com.example.balloon;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MoreEventInfo extends ViewGroup
{
	//Expiration date in milliseconds
		private long mExpiresAt;
		
		//Views for easy access
		private TextView mCreatorView;
		private TextView mAgendaView;
		private TextView mVenueInfoView;
		private ImageView mVenuePhotoView;
		private TextView mTimeToRSVPView;
		private ImageView mArrowView;

		//All constructors because I don't know what's the difference between the second and third
		public MoreEventInfo(Context context)
		{
			super(context);
			initViews();
			mExpiresAt = 0;
		}
		
		public MoreEventInfo(Context context, AttributeSet attrs)
		{
			super(context, attrs);
			initViews();
			setAttributes(context, attrs);
		}
		
		public MoreEventInfo(Context context, AttributeSet attrs, int defStyleAttr)
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
			setBackgroundColor(getResources().getColor(R.color.yellow));
			setFocusable(true);
			setClickable(true);
			setFocusableInTouchMode(true);

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
			mTimeToRSVPView = new TextView(getContext());
			mTimeToRSVPView.setTextColor(getResources().getColor(R.color.red));
			mTimeToRSVPView.setPaintFlags(Paint.ANTI_ALIAS_FLAG);
			mTimeToRSVPView.setTextSize(12);
			mTimeToRSVPView.setTypeface(tf);
			mTimeToRSVPView.setId(5);
			mTimeToRSVPView.setLayoutParams(wrapContent);
			//CHANGE THIS i'm hardcoding time to rsvp
			mTimeToRSVPView.setText("10 hours");
			addView(mTimeToRSVPView);
			
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
				setExpiresAt(Long.parseLong(a.getString(R.styleable.Event_expiresAt)));
			}
			finally
			{
				a.recycle();
			}
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
		
		public long getExpiresAt()
		{
			return mExpiresAt;
		}
		
		public String getTimeToRSVP()
		{
			return mTimeToRSVPView.getText().toString();
		}

		//All the setters (? Is that what you call them?)
		//Double Check to see if invalidating a child view works vs. invalidating whole viewgroup
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
		
		public void setVenuePhoto(String url)
		{
			mVenuePhotoView.setImageURI(Uri.parse(url));
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
		public void setExpiresAt(Long time)
		{
			mExpiresAt = time;
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
}
