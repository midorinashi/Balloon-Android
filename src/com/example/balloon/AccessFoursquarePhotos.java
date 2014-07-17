package com.example.balloon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AccessFoursquarePhotos extends AccessFoursquare {

	@Override
	//string 0 is the location id
	protected JSONArray doInBackground(String... id) {
		String str = "";
		String urlString = URL + id[0] + PHOTO + CLIENT_ID + CLIENT_SECRET + DEFAULT_VERSION;

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
				array = (new JSONObject(str)).getJSONObject("response").getJSONObject("photos")
						.getJSONArray("items");
				System.out.println("Got array!");
			
		} catch (IOException e) {
		    e.printStackTrace();
		    try {
		   	    Thread.sleep(1000);
		   	    doInBackground(id);
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
	
	//oh my god time to get all the images
    protected void onPostExecute(JSONArray items)
    {
    	String[] urls = new String[items.length()];
    	JSONObject item;
    	for (int i = 0; i < items.length(); i++)
    	{
    		try {
				item = (JSONObject) items.getJSONObject(i);
	    		urls[i] = (item.getString("prefix") + "300x100" + item.getString("suffix"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	AbstractImageListFragment.setURLs(urls);
    }

}
