package com.CHH2000day.navalcreed.modhelper;
import android.support.v4.app.*;
import android.os.*;
import android.view.*;
import java.io.*;
import android.net.*;
import android.app.Activity;
import android.content.*;
import com.CHH2000day.navalcreed.modhelper.ModPackageInfo.*;
import org.json.*;

public class ModPackageInstallerFragment extends Fragment
{

	private UriLoader loader;
	private ModPackageInstallHelper mpih;
	@Override
	public void onCreate ( Bundle savedInstanceState )
	{
		// TODO: Implement this method
		super.onCreate ( savedInstanceState );
	}

	@Override
	public View onCreateView ( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{
		// TODO: Implement this method
		return inflater.inflate ( R.layout.modinfopage, null );
	}

	@Override
	public void onActivityCreated ( Bundle savedInstanceState )
	{
		// TODO: Implement this method
		if ( loader != null )
		{
			selectFile ( loader.getUri ( ) );
			//注销接口防止被重复使用
			loader = null;
		}
	}


	@Override
	public void onAttach ( Activity activity )
	{

		// TODO: Implement this method
		super.onAttach ( activity );
		if ( activity != null )
		{
			loader = (UriLoader)activity;
		}
	}


	public void selectFile ( Uri uri )
	{
		String filepath=Utils.resolveFilePath ( uri );
		try
		{
			mpih = new ModPackageInstallHelper ( new File ( filepath ), (Main)getActivity ( )  );
		}
		catch (IOException e)
		{}
		catch (ModPackageInfo.IllegalModInfoException e)
		{}

	}
	public static interface UriLoader
	{
		public Uri getUri ( );
	}

	@Override
	public void onActivityResult ( int requestCode, int resultCode, Intent data )
	{
		// TODO: Implement this method
		super.onActivityResult ( requestCode, resultCode, data );
	}

}
