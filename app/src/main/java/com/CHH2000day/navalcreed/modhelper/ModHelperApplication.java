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
public class ModHelperApplication extends Application
{
	//never used
	//private android.os.Handler merrmsghdl;
	private File resDir;
	private File sdcard;
	private File resfilesdir;
	private String resfilePath="";
	private static final String STOREDFILE_NAME="mod.install";
//public static final String GAME_PKGNAME="com.loong.warship.zl";
	private static final String GAME_PKGNAME="IARJisxjM8tihdkvzU52XrgfhNLAY1FK";

	private String pkg_name;
	private boolean isMainPage=true;

	public void setIsMainPage (boolean isMainPage)
	{
		this.isMainPage = isMainPage;
	}

	public boolean isMainPage ()
	{
		return isMainPage;
	}
	@Override
	public void onCreate ()
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
			Signature s=getPackageManager ( ).getPackageInfo ( getPackageName ( ), getPackageManager ( ).GET_SIGNATURES ).signatures[ 0 ];
			IceKeyHelper mhelper=new IceKeyHelper ( s.toByteArray ( ), 0 );
			//pkg_name=Base64.encodeToString(mhelper.encrypt(GAME_PKGNAME.getBytes()),Base64.DEFAULT);
			pkg_name = new String ( mhelper.decrypt ( Base64.decode ( GAME_PKGNAME, Base64.DEFAULT ) ) ).trim ( );
			ModPackageManager.getInstance().init(new File(getResFilesDir(),STOREDFILE_NAME));
		}
		catch (Exception e)
		{}

		//加密部分存在问题,暂不实装
		// TODO: Implement this method
		super.onCreate ( );
	}
	/*public android.os.Handler getErrMsgHdl ( )
	 {
	 return merrmsghdl;
	 }*/
	public File getResDir ()
	{
		if (resDir == null)
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
	public String getResPath ()
	{
		return getResDir ( ).getAbsolutePath ( );
	}
	public File getResFilesDir ()
	{
		if (resfilesdir == null)
		{
			resfilesdir = new File ( getResDir ( ), "files" );
		}
		return resfilesdir;
	}
	public String getResFilesDirPath ()
	{
		return getResFilesDir ( ).getAbsolutePath ( );
	}

}
