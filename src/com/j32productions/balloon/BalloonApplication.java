package com.j32productions.balloon;

import com.parse.Parse;
import com.parse.PushService;

import android.app.Application;

public class BalloonApplication extends Application {
	
	public static boolean mHasPoppedUp;
	
	public void onCreate()
	  {
		//TODO change roboto to helvetica neue
		FontsOverride.setDefaultFont(this, "SANS_SERIF", "fonts/Roboto-Light.ttf");
		
		//real backend
		Parse.initialize(this, "kEpJ1siPj27dM2spfX3TKDmGXsZEMgqFffVMtA9c",
				"J0q1R9NT55M37LU2ng3EEMGTz8WhTGa8LfzF0DKo");
		
		//controls location popup
		mHasPoppedUp = false;
		
		//developer backend
		/*
		Parse.initialize(this, "iXEPNEZfJXoEOIayxLgBBgpShMZBTj7ReVoi1eqn",
				"GHtE0svPk0epFG4olYnFTnnDtmARHtENXxXuHoXp");
		PushService.setDefaultPushCallback(this, MainActivity.class);
		*/
		//set the font
		super.onCreate();
	  }
}
