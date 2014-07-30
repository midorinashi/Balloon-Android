package com.example.balloon;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

public class ProgressCircleFragment extends Fragment {
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_progress, container,
				false);
	
		return rootView;
	}
	
	public void onResume()
	{
		super.onResume();
		((ProgressBar) getActivity().findViewById(R.id.progressCircle)).getIndeterminateDrawable()
			.setColorFilter(getResources().getColor(R.color.white),
			android.graphics.PorterDuff.Mode.SRC_ATOP);
	}
	
}
