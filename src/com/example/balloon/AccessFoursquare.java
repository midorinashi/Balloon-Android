package com.example.balloon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.AsyncTask;

public class AccessFoursquare extends AsyncTask<String, Void, JSONArray> {

    private final static String SEARCH_URL = "https://api.foursquare.com/v2/venues/search";
	private final static String CLIENT_ID = "QIVN42TMR5KLGEA15W1VK0ISG4V3DOT0J4XAZVZ033HQK2MH";
	private final static String CLIENT_SECRET = "YDYDI1JXQJPCVAWM1ZDMHRCCCAEJY5DT3TUTLUXU2JZ5G2AJ";
	private final static String CALLBACK_URL = "fb527538684032224://foursquare";
	private final String DEFAULT_VERSION = "20140131";

	//string 0 is query
    protected JSONArray doInBackground(String... strings) {
    	String str = "";
    	String urlString = SEARCH_URL + "?client_id=" + CLIENT_ID + "%20&client_secret=" + CLIENT_SECRET +
    			"%20&v=" + DEFAULT_VERSION + "%20&limit=100" + "%20&radius=50000";
    	// "&redirect_uri=" + CALLBACK_URL + 
    	
    	// get the location
    	// Acquire a reference to the system Location Manager
    	
    	urlString += "%20&ll=";
    	if (MainActivity.getLatitude() != -1)
    		urlString += MainActivity.getLatitude() + "," + MainActivity.getLongitude();
    	else
    		urlString += "40.7,-74.0";
    	
    	//make sure we have a query
    	if (strings[0].compareTo("") != 0)
    		urlString += "%20&query=" + strings[0].replaceAll(" ", "%20");
    	System.out.println(urlString);
    	// for explore, we can add "%20&time=any%20&day=any"
    	
    	BufferedReader rd  = null;
        StringBuilder sb = null;
        String line = null;
        JSONArray array = new JSONArray();
        try {
        	URL url = new URL(urlString);
        	HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        	connection.setRequestMethod("GET");
        	connection.connect();
        	rd  = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        	sb = new StringBuilder();
        	while ((line = rd.readLine()) != null)
        		sb.append(line + '\n');
        	str =  sb.toString();
				array = (new JSONObject(str)).getJSONObject("response").getJSONArray("venues");
				System.out.println(array.getJSONObject(0).getString("name"));
				System.out.println("Got array!");
			
         } catch (IOException e) {
             e.printStackTrace();
             try {
            	    Thread.sleep(1000);
            	    doInBackground(strings);
            	} catch(InterruptedException ex) {
            	    Thread.currentThread().interrupt();
            	}
         } catch (Exception e) {
			e.printStackTrace();
		}
		return array;
    }
    
    protected void onPostExecute(JSONArray array) {
    	System.out.println("giving back array to fragment");
        NewInvitationActivity.ChooseLocationFragment.makeList(array);
    }
}
