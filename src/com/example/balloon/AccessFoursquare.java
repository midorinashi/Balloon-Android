package com.example.balloon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;

public class AccessFoursquare extends AsyncTask<String, Void, JSONArray> {

    private Exception exception;

    private final static String SEARCH_URL = "https://api.foursquare.com/v2/venues/search";
	private final static String CLIENT_ID = "QIVN42TMR5KLGEA15W1VK0ISG4V3DOT0J4XAZVZ033HQK2MH";
	private final static String CLIENT_SECRET = "YDYDI1JXQJPCVAWM1ZDMHRCCCAEJY5DT3TUTLUXU2JZ5G2AJ";
	private final static String CALLBACK_URL = "fb527538684032224://foursquare";
	private final String DEFAULT_VERSION = "20140131"; 

    protected JSONArray doInBackground(String... queries) {
    	String str = "";
    	String urlString = SEARCH_URL + "?client_id=" + CLIENT_ID + "%20&client_secret=" + CLIENT_SECRET +
    			"&redirect_uri=" + CALLBACK_URL + "%20&v=" + DEFAULT_VERSION + "%20&ll=" +"40.7,-74" + 
    			"%20&limit=100" + "%20&radius=50000";
    	String query = "";
    	for (int i = 0; i < queries.length - 1; i++)
    		query += queries[i] + "%20";
    	// for explore, we can add "%20&time=any%20&day=any"
    	//make sure we have a query
    	if (queries.length > 0)
    	{
    		urlString += "%20&query=" + query + queries[queries.length - 1];
    	}
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
			try {
				array = (new JSONObject(str)).getJSONObject("response").getJSONArray("venues");
				System.out.println(array.getJSONObject(0).getString("name"));
				System.out.println("Got array!");
			} catch (JSONException e) {
				e.printStackTrace();
			}
         } catch (MalformedURLException e) {
             e.printStackTrace();
         } catch (ProtocolException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
		return array;
    }
    
    protected void onPostExecute(JSONArray array) {
    	System.out.println("giving back array to fragment");
        NewInvitationActivity.ChooseLocationFragment.makeList(array);
    }
}
