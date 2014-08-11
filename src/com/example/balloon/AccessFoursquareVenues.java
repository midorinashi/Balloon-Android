package com.example.balloon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;

public class AccessFoursquareVenues extends AccessFoursquare implements LocationListener {

	private ProgressFragment fragment;
	private LocationManager lm;
	private Location location;
	private String provider;
	
	public AccessFoursquareVenues(ProgressFragment f)
	{
		super();
		fragment = f;
		lm = (LocationManager) fragment.getActivity().getSystemService(Context.LOCATION_SERVICE);
		provider = lm.getBestProvider(new Criteria(), false);
		lm.requestLocationUpdates(provider, 20000, 1, this);

		f.showSpinner();
	}
	
	//string 0 is query
    protected JSONArray doInBackground(String... strings) {
    	String str = "";
    	String urlString = URL + SEARCH + CLIENT_ID + CLIENT_SECRET +
    			DEFAULT_VERSION ;

        JSONArray array = new JSONArray();
		//get location 
    	location = lm.getLastKnownLocation(provider);
    	
    	if (location != null)
	    	urlString += "%20&ll=" + location.getLatitude() + "," + location.getLongitude();
    	
    	//make sure we have a query
    	if (strings.length != 0 && strings[0].trim().compareTo("") != 0)
    		urlString += "%20&query=" + strings[0].replaceAll(" ", "%20") + "%20&limit=100" + "%20&radius=50000";
    	//we want to make query-less queries fast
    	else
    		urlString+= "%20&limit=10" + "%20&radius=500";
    	System.out.println(urlString);
    	// for explore, we can add "%20&time=any%20&day=any"
    	
    	BufferedReader rd  = null;
        StringBuilder sb = null;
        String line = null;
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
				System.out.println(array.length());
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
    	lm.removeUpdates(this);
        NewInvitationActivity.ChooseLocationFragment.makeList(array);
    }

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		provider = lm.getBestProvider(new Criteria(), false);
	}

	@Override
	public void onProviderEnabled(String provider) {
		provider = lm.getBestProvider(new Criteria(), false);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
}
