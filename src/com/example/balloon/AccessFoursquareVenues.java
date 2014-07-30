package com.example.balloon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.AsyncTask;

public class AccessFoursquareVenues extends AccessFoursquare {

	private ProgressFragment fragment;
	
	public AccessFoursquareVenues(ProgressFragment f)
	{
		super();
		fragment = f;
		f.showSpinner();
	}
	
	//string 0 is query
    protected JSONArray doInBackground(String... strings) {
    	String str = "";
    	String urlString = URL + SEARCH + CLIENT_ID + CLIENT_SECRET +
    			DEFAULT_VERSION ;
    	
    	// get the location
    	// Acquire a reference to the system Location Manager
    	
    	urlString += "%20&ll=";
    	if (MainActivity.getLatitude() != -1)
    		urlString += MainActivity.getLatitude() + "," + MainActivity.getLongitude();
    	else
    		urlString += "40.7,-74.0";
    	
    	//make sure we have a query
    	if (strings[0].compareTo("") != 0)
    		urlString += "%20&query=" + strings[0].replaceAll(" ", "%20") + "%20&limit=100" + "%20&radius=50000";
    	//we want to make query-less queries fast
    	else
    		urlString+= "%20&limit=10" + "%20&radius=500";
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
         } finally {
        	 if (rd != null)
        	 {
	        	 try {
					rd.close();
	        	 } catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
	        	 }
        	 }
         }
		return array;
    }
    
    protected void onPostExecute(JSONArray array) {
    	System.out.println("giving back array to fragment");
    	fragment.removeSpinner();
        NewInvitationActivity.ChooseLocationFragment.makeList(array);
    }
}
