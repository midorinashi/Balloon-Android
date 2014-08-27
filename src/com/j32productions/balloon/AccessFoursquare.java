package com.j32productions.balloon;

import org.json.JSONArray;

import android.os.AsyncTask;

public abstract class AccessFoursquare extends AsyncTask<String, Void, JSONArray> {
	
	//contains all the keys and shit
	protected final static String URL = "https://api.foursquare.com/v2/venues/";
	protected final static String PHOTO = "/photos";
    protected final static String SEARCH = "search";
    protected final static String CLIENT_ID = "?client_id=QIVN42TMR5KLGEA15W1VK0ISG4V3DOT0J4XAZVZ033HQK2MH";
    protected final static String CLIENT_SECRET = "%20&client_secret=YDYDI1JXQJPCVAWM1ZDMHRCCCAEJY5DT3TUTLUXU2JZ5G2AJ";
    protected final static String CALLBACK_URL = "&redirect_uri=fb527538684032224://foursquare";
    protected final String DEFAULT_VERSION = "%20&v=20140131";
}
