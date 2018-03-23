package com.CHH2000day.navalcreed.modhelper;
import android.app.*;
import android.content.pm.*;
import android.util.*;
import cn.bmob.v3.*;
import java.io.*;
import android.os.storage.*;
import android.os.*;
import com.CHH2000day.*;
import android.content.pm.PackageManager.*;
import android.content.*;
public class ModHelperApplication extends Application
{
	//never used
	//private android.os.Handler merrmsghdl;
	private File resDir;
	private File sdcard;
	private File resfilesdir;
	private String resfilePath="";
	private SharedPreferences mainpref;
	private static final String STOREDFILE_NAME="mod.install";
//public static final String GAME_PKGNAME="com.loong.warship.zl";
	protected static final String KEY_PKGNAME="pkgName";
	private static final String GAME_PKGNAME_CN_SERVER="IARJisxjM8tihdkvzU52XrgfhNLAY1FK";
	private static final String GAME_PKGNAME_EU_SERVER="";
	private static final String GAME_PKGNAME_TW_SERVER="";
	private static final String EU="EU";
	private static final String CN="CN";
	private static final String TW="TW";
	private static String pkgnameinuse=GAME_PKGNAME_CN_SERVER;//CN EU TW


	private String pkg_name;
	private boolean isMainPage=true;

	public void setIsMainPage ( boolean isMainPage )
	{
		this.isMainPage = isMainPage;
	}

	public boolean isMainPage ( )
	{
		return isMainPage;
	}
	@Override
	public void onCreate ( )
	{
		Bmob.initialize ( ModHelperApplication.this, StaticData.API_KEY );
		BmobInstallation.getCurrentInstallation ( ).save ( );
		Log.i ( "Bmob initalized", "Bmob initalized" );
		try
		{
			UncaughtExceptionHandler.getInstance ( ).init ( ModHelperApplication.this );
		}
		catch (PackageManager.NameNotFoundException e)
		{}

		try
		{
			Class c=Class.forName ( "cc.binmt.signature.PmsHookApplication" );
			if ( c != null )
			{
				throw new RuntimeException ( "Hook detected.Environment is not safe." );
			}
		}
		catch (ClassNotFoundException e)
		{
			//应当抛出异常
		}
		mainpref = getSharedPreferences ( "main", 0 );
		mainpref.registerOnSharedPreferenceChangeListener ( new MainSharedPreferencesChangeListener ( ) );
		try
		{
			Signature s=getPackageManager ( ).getPackageInfo ( getPackageName ( ), getPackageManager ( ).GET_SIGNATURES ).signatures [ 0 ];
			IceKeyHelper mhelper=new IceKeyHelper ( s.toByteArray ( ), 0 );
			//pkg_name=Base64.encodeToString(mhelper.encrypt(GAME_PKGNAME.getBytes()),Base64.DEFAULT);
			pkg_name = new String ( mhelper.decrypt ( Base64.decode ( GAME_PKGNAME_CN_SERVER, Base64.DEFAULT ) ) ).trim ( );
			ModPackageManager.getInstance ( ).init ( new File ( getResFilesDir ( ), STOREDFILE_NAME ) );
		}
		catch (Exception e)
		{}


		// TODO: Implement this method
		super.onCreate ( );
	}
	/*public android.os.Handler getErrMsgHdl ( )
	 {
	 return merrmsghdl;
	 }*/
	public File getResDir ( )
	{
		if ( resDir == null )
		{
			sdcard = Environment.getExternalStorageDirectory ( );
			//resfilePath: /sdcard/Android/data/$pkgname
			resfilePath = new StringBuilder ( )
				.append ( sdcard.getAbsolutePath ( ) )
				.append ( File.separatorChar )
				.append ( "Android" )
				.append ( File.separatorChar )
				.append ( "data" )
				.append ( File.separatorChar )
				.append ( pkg_name )
				.toString ( );
			resDir = new File ( resfilePath );

		}
		return resDir;
	}
	public String getResPath ( )
	{
		return getResDir ( ).getAbsolutePath ( );
	}
	public File getResFilesDir ( )
	{
		if ( resfilesdir == null )
		{
			resfilesdir = new File ( getResDir ( ), "files" );
		}
		return resfilesdir;
	}
	public String getResFilesDirPath ( )
	{
		return getResFilesDir ( ).getAbsolutePath ( );
	}
	private void cleanPathCache ( )
	{
		resfilesdir = null;
		resDir = null;
	}
	public SharedPreferences getMainSharedPrederences(){
		return mainpref;
	}
	private void updateTargetPackageName ( String type )
	{
		if ( EU.equals ( type ) )
		{
			pkgnameinuse = GAME_PKGNAME_EU_SERVER;
		}
		else
		{
			if ( CN.equals ( type ) )
			{
				pkgnameinuse = GAME_PKGNAME_CN_SERVER;
			}
			else
			{
				if ( TW.equals ( GAME_PKGNAME_TW_SERVER ) )
				{
					pkgnameinuse = GAME_PKGNAME_TW_SERVER;
				}
			}
		}
	}
	private class MainSharedPreferencesChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener
	{

		@Override
		public void onSharedPreferenceChanged ( SharedPreferences p1, String key )
		{
			if ( KEY_PKGNAME.equals ( key ) )
			{
				cleanPathCache ( );
				updateTargetPackageName ( p1.getString ( key, CN ) );
			}
			// TODO: Implement this method
		}


	}

}
