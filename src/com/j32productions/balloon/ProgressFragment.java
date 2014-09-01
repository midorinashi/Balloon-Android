package com.j32productions.balloon;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.view.View;
import android.widget.Toast;

import com.parse.ParseException;

public abstract class ProgressFragment extends Fragment {
	
	public void showParseException(ParseException e)
	{
		removeSpinner();
		if (getActivity() != null)
		{
			if (e != null)
			{
				Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
			else
				Toast.makeText(getActivity(), "Oops! An error has occured", Toast.LENGTH_LONG).show();
		}
	}
	
	public void showSpinner()
	{
		View container = getActivity().findViewById(R.id.progress);
		if (container != null)
			container.setVisibility(View.VISIBLE);
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
			else
			{
				View container = getActivity().findViewById(R.id.progress);
				if (container != null)
					container.setVisibility(View.GONE);
			}
		}
	}
}
