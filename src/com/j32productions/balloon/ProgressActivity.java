package com.j32productions.balloon;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.view.View;
import android.widget.Toast;

import com.parse.ParseException;

public class ProgressActivity extends Activity {

	public void showParseException(ParseException e)
	{
		removeSpinner();
		if (e != null)
		{
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
		else
			Toast.makeText(this, "Oops! An error has occured", Toast.LENGTH_LONG).show();
	}
	
	public void showSpinner()
	{
		View container = findViewById(R.id.progress);
		if (container != null)
			container.setVisibility(View.VISIBLE);
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
		//double check
		else
		{
			View container = findViewById(R.id.progress);
			if (container != null)
				container.setVisibility(View.GONE);
		}
	}
	
	public void onStop()
	{
		removeSpinner();
		super.onStop();
	}
	
}
