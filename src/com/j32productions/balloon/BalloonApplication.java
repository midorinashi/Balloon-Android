package com.j32productions.balloon;

import com.parse.Parse;
import com.parse.PushService;

import android.app.Application;

public class BalloonApplication extends Application {
	public void onCreate()
	  {
		//TODO change roboto to helvetica neue
		FontsOverride.setDefaultFont(this, "MONOSPACE", "fonts/Roboto-Light.ttf");
		Parse.initialize(this, "iXEPNEZfJXoEOIayxLgBBgpShMZBTj7ReVoi1eqn",
				"GHtE0svPk0epFG4olYnFTnnDtmARHtENXxXuHoXp");
		PushService.setDefaultPushCallback(this, MainActivity.class);
		//set the font
		super.onCreate();
	  }
}
