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
import android.support.design.widget.*;

public class AboutFragment extends Fragment
{
	private View v;
	private Button license,pkgname;
	private int selectedItem=0;
	private ModHelperApplication app;
	private TextView mtextview;
	@Override
	public View onCreateView ( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{	app = (ModHelperApplication)getActivity ( ).getApplication ( );
		app.getPkgNameNum ( app.getMainSharedPrederences ( ).getString ( app.KEY_PKGNAME, app.CN ) );
		v = inflater.inflate ( R.layout.about_fragment, null );
		license = (Button)v.findViewById ( R.id.aboutfragmentLicense );
		pkgname = (Button)v.findViewById ( R.id.aboutfragmentButtonselectpkg );
		mtextview = (TextView)v.findViewById ( R.id.aboutfragmentTextView );
		if ( BuildConfig.DEBUG )
		{
			mtextview.setText ( new StringBuilder ( ).append ( "Device id:" )
							   .append ( Build.SERIAL )
							   .append ( "\n" )
							   .append ( mtextview.getText ( ) )
							   .toString ( ) );
			mtextview.setOnClickListener ( new OnClickListener ( ){

					@Override
					public void onClick ( View p1 )
					{
						ClipboardManager cmb = (ClipboardManager)getActivity().getSystemService ( Context.CLIPBOARD_SERVICE );  
						cmb.setText ( Build.SERIAL );  
						Snackbar.make(v,"Device id has been copied",Snackbar.LENGTH_LONG).show();
						// TODO: Implement this method
					}
				} );
		}
		license.setOnClickListener ( new OnClickListener ( ){

				@Override
				public void onClick ( View p1 )
				{

					try
					{
						Source s=Okio.source ( getResources ( ).getAssets ( ).open ( "LICENSE" ) );
						BufferedSource bs=Okio.buffer ( s );
						AlertDialog.Builder adb=new AlertDialog.Builder ( getActivity ( ) );
						adb.setTitle ( R.string.about_license )
							.setMessage ( bs.readUtf8 ( ) )
							.setPositiveButton ( R.string.ok, null );
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
					adb.setTitle ( R.string.select_target_package );
					adb.setSingleChoiceItems ( app.pkgnames, app.getPkgNameNum ( app.getMainSharedPrederences ( ).getString ( app.KEY_PKGNAME, app.CN ) ), new DialogInterface.OnClickListener ( ){

							@Override
							public void onClick ( DialogInterface p1, int p2 )
							{
								selectedItem = p2;
								// TODO: Implement this method
							}
						} );
					adb.setPositiveButton ( R.string.ok, new DialogInterface.OnClickListener ( ){  

							@Override
							public void onClick ( DialogInterface p1, int p2 )
							{
								app.getMainSharedPrederences ( ).edit ( ).putString ( app.KEY_PKGNAME, app.getPkgNameByNum ( selectedItem ) ).apply ( );
								// TODO: Implement this method
							}
						} );
					adb.setNegativeButton ( R.string.cancel, null );
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
