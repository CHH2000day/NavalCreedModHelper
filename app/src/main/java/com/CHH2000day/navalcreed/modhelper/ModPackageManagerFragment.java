package com.CHH2000day.navalcreed.modhelper;
import android.support.v4.app.*;
import android.view.*;
import android.support.v7.widget.*;
import android.widget.*;
import android.os.*;

public class ModPackageManagerFragment extends Fragment
{
	private View v;
	private RecyclerView recyclerview;
	private ToggleButton ovrd_switch;

	@Override
	public View onCreateView ( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{
		// TODO: Implement this method
		super.onCreateView ( inflater, container, savedInstanceState );
		v=inflater.inflate(R.layout.modmanagerfragmemt,null);
		recyclerview=(RecyclerView)v.findViewById(R.id.modmanagerfragmemtRecyclerView);
		ovrd_switch=(ToggleButton)v.findViewById(R.id.modmanagerswitcherToggleButton1);
		return v;
	}
	
}
