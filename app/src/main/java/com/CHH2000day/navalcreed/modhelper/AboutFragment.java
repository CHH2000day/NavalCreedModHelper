package com.CHH2000day.navalcreed.modhelper;
import android.support.v4.app.*;
import android.view.*;
import android.os.*;
import android.widget.*;
import android.view.View.*;
import android.content.res.*;
import okio.*;
import java.io.*;
import android.support.v7.app.*;
import android.content.*;

public class AboutFragment extends Fragment
{
	private View v;
	private Button license,pkgname;
	private int selectedItem=0;
	private ModHelperApplication app;
	@Override
	public View onCreateView ( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{	app = (ModHelperApplication)getActivity ( ).getApplication ( );
		app.getPkgNameNum ( app.getMainSharedPrederences ( ).getString ( app.KEY_PKGNAME, app.CN ) );
		v = inflater.inflate ( R.layout.about_fragment, null );
		license = (Button)v.findViewById ( R.id.aboutfragmentLicense );
		pkgname = (Button)v.findViewById ( R.id.aboutfragmentButtonselectpkg );
		license.setOnClickListener ( new OnClickListener ( ){

				@Override
				public void onClick ( View p1 )
				{

					try
					{
						Source s=Okio.source ( getResources ( ).getAssets ( ).open ( "LICENSE" ) );
						BufferedSource bs=Okio.buffer ( s );
						AlertDialog.Builder adb=new AlertDialog.Builder ( getActivity ( ) );
						adb.setTitle ( "关于开源许可" )
							.setMessage ( bs.readUtf8 ( ) )
							.setPositiveButton ( "确定", null );
						bs.close ( );
						adb.create ( ).show ( );
					}
					catch (IOException e)
					{}
					// TODO: Implement this method
				}
			} );
		pkgname.setOnClickListener ( new OnClickListener ( ){

				@Override
				public void onClick ( View p1 )
				{

					AlertDialog.Builder adb=new AlertDialog.Builder ( getActivity ( ) );
					adb.setTitle ( "选择目标程序包名" );
					adb.setSingleChoiceItems ( app.pkgnames, app.getPkgNameNum ( app.getMainSharedPrederences ( ).getString ( app.KEY_PKGNAME, app.CN ) ), new DialogInterface.OnClickListener ( ){

							@Override
							public void onClick ( DialogInterface p1, int p2 )
							{
								selectedItem = p2;
								// TODO: Implement this method
							}
						} );
					adb.setPositiveButton ( "确定", new DialogInterface.OnClickListener ( ){  

							@Override
							public void onClick ( DialogInterface p1, int p2 )
							{
								app.getMainSharedPrederences ( ).edit ( ).putString ( app.KEY_PKGNAME, app.getPkgNameByNum ( selectedItem ) ).apply ( );
								// TODO: Implement this method
							}
						} );
					adb.setNegativeButton ( "取消", null );
					adb.create ( ).show ( );

					// TODO: Implement this method
				}
			} );

		// TODO: Implement this method
		return v;
	}

	@Override
	public void onResume ( )
	{
		// TODO: Implement this method
		super.onResume ( );
		selectedItem = app.getPkgNameNum ( app.getMainSharedPrederences ( ).getString ( app.KEY_PKGNAME, app.CN ) );
	}

}
