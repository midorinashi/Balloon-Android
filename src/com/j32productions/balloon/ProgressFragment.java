package com.j32productions.balloon;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.widget.Toast;

import com.parse.ParseException;

public abstract class ProgressFragment extends Fragment {
	
	public void showParseException(ParseException e)
	{
		removeSpinner();
		if (getActivity() != null)
			Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
		e.printStackTrace();
	}
	
	public void showSpinner()
	{
		if (getFragmentManager().findFragmentByTag("Progress") == null)
			getActivity().getFragmentManager().beginTransaction().add(R.id.progress, 
				new ProgressCircleFragment(), "Progress").setTransition(FragmentTransaction
				.TRANSIT_FRAGMENT_OPEN).commit();
	}
	
	public void removeSpinner()
	{
		if (getActivity() != null)
		{
			FragmentManager manager = getActivity().getFragmentManager();
			Fragment fragment = manager.findFragmentByTag("Progress");
			if (fragment != null)
				manager.beginTransaction().remove(fragment).setTransition(FragmentTransaction
						.TRANSIT_FRAGMENT_CLOSE).commitAllowingStateLoss();
		}
	}
}
