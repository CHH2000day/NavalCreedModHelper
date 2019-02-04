package com.CHH2000day.navalcreed.modhelper;
import android.app.*;
import android.content.pm.*;
import android.util.*;
import cn.bmob.v3.*;
import java.io.*;

import android.os.*;
import com.CHH2000day.*;
import android.content.pm.PackageManager.*;
import android.content.*;
import java.util.*;
import org.json.*;
public class ModHelperApplication extends Application
{
	//never used
	//private android.os.Handler merrmsghdl;
	private File resDir;
    private File resfilesdir;
	private String resfilePath="";
	private SharedPreferences mainpref;
	private static final String STOREDFILE_NAME="mod.install";
//public static final String GAME_PKGNAME="com.loong.warship.zl";
	protected static final String KEY_PKGNAME="pkgName";
	private static final String GAME_PKGNAME_CN_SERVER="com.loong.warship.zl";
	private static final String GAME_PKGNAME_EU_SERVER="com.zloong.eu.nc";
	private static final String GAME_PKGNAME_TW_SERVER="hk.com.szn.zj";
	private static final String EU="EU";//num1
	public static final String CN="CN";//num0
	private static final String TW="TW";//num2
	private String pkgnameinuse=GAME_PKGNAME_CN_SERVER;//CN EU TW
	private static MainSharedPreferencesChangeListener preflistener;
    public static final String[] pkgnames;
	private boolean isMainPage=true;
	private String versionName="unknown";

	public String getVersionName()
	{
		return versionName;
	}
	static{
		pkgnames = new String[3];
		pkgnames [ 0 ] = CN;
		pkgnames [ 1 ] = EU;
		pkgnames [ 2 ] = TW;
	}
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
		Log.i ( "Bmob initalized", "Bmob initalized" );
		try
		{
			UncaughtExceptionHandler.getInstance ( ).init ( ModHelperApplication.this );
		} catch (PackageManager.NameNotFoundException ignored)
		{}


		PackageManager packageManager = getPackageManager();
		try
		{
			PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
			versionName=packageInfo.versionName;
		}
		catch (PackageManager.NameNotFoundException e)
		{
			
		}

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
		preflistener=new MainSharedPreferencesChangeListener();
		mainpref.registerOnSharedPreferenceChangeListener ( preflistener);
		cleanPathCache();
		updateTargetPackageName ( getMainSharedPrederences ( ).getString ( KEY_PKGNAME, CN ) );
		/*try
		{
			ModPackageManager.getInstance ( ).init ( new File ( getResFilesDir ( ), STOREDFILE_NAME ) );
		}
		catch (IOException e)
		{e.printStackTrace();}
		catch (JSONException e)
		{e.printStackTrace();}
		*/
		//ModPackageInstallHelper.init(this);
		ModPackageManager.getInstance().init(this);
		reconfigModPackageManager();
		
		// TODO: Implement this method
		super.onCreate ( );
	}
	
	/*public android.os.Handler getErrMsgHdl ( )
	 {
	 return merrmsghdl;
	 }*/


	@Override
	public void onTerminate ( )
	{
		// TODO: Implement this method
		super.onTerminate ( );
		mainpref.unregisterOnSharedPreferenceChangeListener(preflistener);
	}
	public void reconfigModPackageManager(){
		try
		{
			ModPackageManager.getInstance ( ).config ( new File ( getResFilesDir ( ), STOREDFILE_NAME ) );
		}
		catch (IOException e)
		{e.printStackTrace();}
		catch (JSONException e)
		{e.printStackTrace();}
		
	}
	public File getResDir ( )
	{
		if ( resDir == null )
		{
            File sdcard = Environment.getExternalStorageDirectory();
			//resfilePath: /sdcard/Android/data/$pkgname
			resfilePath = new StringBuilder ( )
				.append ( sdcard.getAbsolutePath ( ) )
				.append ( File.separatorChar )
				.append ( "Android" )
				.append ( File.separatorChar )
				.append ( "data" )
				.append ( File.separatorChar )
				.append ( pkgnameinuse )
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
		resfilePath=null;
		resDir = null;
	}
	public String getPkgNameByNum ( int i )
	{
		return pkgnames [ i ];
	}
	public int getPkgNameNum ( String name )
	{
		//防止返回-1发生越界问题
		return Math.max ( 0, Arrays.binarySearch ( pkgnames, name ) );
	}
	public SharedPreferences getMainSharedPrederences ( )
	{
		return mainpref;
	}
	private void updateTargetPackageName ( String type )
	{
		if ( EU.equals ( type ) )
		{
			pkgnameinuse = decodepkgname(GAME_PKGNAME_EU_SERVER);
		}
		else
		{
			if ( CN.equals ( type ) )
			{
				pkgnameinuse = decodepkgname(GAME_PKGNAME_CN_SERVER);
			}
			else
			{
				if ( TW.equals ( type ) )
				{
					pkgnameinuse = decodepkgname(GAME_PKGNAME_TW_SERVER);
				}
			}
		}
	}
	private String decodepkgname(String data){
		return data;
	}
	private class MainSharedPreferencesChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener
	{

		@Override
		public void onSharedPreferenceChanged ( SharedPreferences p1, String key )
		{
			if ( KEY_PKGNAME.equals ( key ) )
			{
				cleanPathCache ( );
				updateTargetPackageName ( p1.getString ( KEY_PKGNAME, key ) );
				reconfigModPackageManager();
			}
			// TODO: Implement this method
		}


	}

}
