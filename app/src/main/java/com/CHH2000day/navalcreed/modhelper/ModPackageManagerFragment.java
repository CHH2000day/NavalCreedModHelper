package com.CHH2000day.navalcreed.modhelper;
import android.support.v4.app.*;
import android.view.*;
import android.support.v7.widget.*;
import android.widget.*;
import android.os.*;
import android.content.res.*;
import android.widget.CompoundButton.*;
import android.support.v7.app.*;
import android.content.*;

public class ModPackageManagerFragment extends Fragment
{
	private View v;
	private RecyclerView recyclerview;
	private ToggleButton ovrd_switch;


	public static final int COLOR_ECAM_AMBER=0xD97900;
	public static final int COLOR_ECAM_GREEN=0x00E300;
	@Override
	public View onCreateView ( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{
		// TODO: Implement this method
		super.onCreateView ( inflater, container, savedInstanceState );
		v = inflater.inflate ( R.layout.modmanagerfragmemt, null );
		recyclerview = (RecyclerView)v.findViewById ( R.id.modmanagerfragmemtRecyclerView );
		ovrd_switch = (ToggleButton)v.findViewById ( R.id.modmanagerswitcherToggleButton1 );
		return v;
	}

	@Override
	public void onActivityCreated ( Bundle savedInstanceState )
	{
		// TODO: Implement this method
		super.onActivityCreated ( savedInstanceState );
		if ( ModPackageManager.getInstance ( ).isOverride ( ) )
		{
			ovrd_switch.setChecked ( true );
		}
		ovrd_switch.setOnCheckedChangeListener ( new OnCheckedChangeListener ( ){

				@Override
				public void onCheckedChanged ( CompoundButton p1, boolean isChecked )
				{
					if ( !isChecked )
					{
						if ( ModPackageManager.getInstance ( ).isOverride ( ) )
						{
							ModPackageManager.getInstance ( ).setIsOverride ( false );
						}
						return;
					}
					AlertDialog.Builder adb= new AlertDialog.Builder ( getActivity ( ) );
					adb.setTitle ( "注意" )
						.setMessage ( "超控mod管理机制将使mod管理器失效，是否继续？" )
						.setNegativeButton ( "取消", new DialogInterface.OnClickListener ( ){

							@Override
							public void onClick ( DialogInterface p1, int p2 )
							{
								// TODO: Implement this method
								cancel ( );
							}
						} )
						.setPositiveButton ( "继续", new DialogInterface.OnClickListener ( ){

							@Override
							public void onClick ( DialogInterface p1, int p2 )
							{
								override ( );
								// TODO: Implement this method
							}
						} )
						.setCancelable ( false );
					AlertDialog ad=adb.create ( );
					ad.setCanceledOnTouchOutside ( false );
					ad.show ( );
					// TODO: Implement this method
				}
				private void override ( )
				{
					ModPackageManager.getInstance ( ).setIsOverride ( true );

				}
				private void cancel ( )
				{
					ovrd_switch.setChecked ( false );
					if ( ModPackageManager.getInstance ( ).isOverride ( ) )
					{
						ModPackageManager.getInstance ( ).setIsOverride ( false );
					}
				}
			} );
	}


}
