package com.CHH2000day.navalcreed.modhelper;

import android.support.v4.app.*;
import android.os.*;
import android.view.*;
public class BGMReplacerFragment extends FunctionFragment
{

	
	private View v;
	private static final int MUSICCOUNT_HARBOR=6;
	private static final int MUSICCOUNT_LOADING=3;
	private static final int MUSICCOUNT_BATTLESTART=4;
	private static final int MUSICCOUNT_BATTLEHEAT=7;
	private static final int MUSICCOUNT_BATTLEEND=3;
	private static final int MUSICCOUNT_BATTLEVICTORY=4;
	private static final int MUSICCOUNT_BATTLEFAIL=2;
	
	
	
	
	

	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// TODO: Implement this method
		v=inflater.inflate(R.layout.bgmreplacer,null);
		return v;
	}
	@Override
	public boolean installMod (int typenum, int num, byte[] deceyptedFileData)
	{
		// TODO: Implement this method
		return false;
		//Not finished
	}
	
}
