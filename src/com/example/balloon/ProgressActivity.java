package com.example.balloon;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.widget.Toast;

import com.parse.ParseException;

public class ProgressActivity extends Activity {

	public void showParseException(ParseException e)
	{
		removeSpinner();
		Toast.makeText(this, e.getStackTrace().toString(), Toast.LENGTH_SHORT).show();
		e.printStackTrace();
	}
	
	public void showSpinner()
	{
		if (getFragmentManager().findFragmentByTag("Progress") == null)
			getFragmentManager().beginTransaction().add(R.id.progress, new ProgressCircleFragment(),
					"Progress").setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
	}
	
	public void removeSpinner()
	{
		FragmentManager manager = getFragmentManager();
		Fragment fragment = manager.findFragmentByTag("Progress");
		if (fragment != null)
			manager.beginTransaction().remove(fragment).setTransition(FragmentTransaction
					.TRANSIT_FRAGMENT_CLOSE).commitAllowingStateLoss();
	}
	
	public void onStop()
	{
		removeSpinner();
		super.onStop();
	}
	
}