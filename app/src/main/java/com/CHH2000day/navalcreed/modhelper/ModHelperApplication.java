package com.CHH2000day.navalcreed.modhelper;
import android.app.*;
import android.content.pm.*;
import android.util.*;
import cn.bmob.v3.*;
import java.io.*;
import android.os.storage.*;
import android.os.*;
public class ModHelperApplication extends Application
{
	//never used
	//private android.os.Handler merrmsghdl;
private File resDir;
private File sdcard;
private File resfilesdir;
private String resfilePath="";
	public static final String GAME_PKGNAME="com.loong.warship.zl";
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
		Bmob.initialize(ModHelperApplication.this,StaticData.API_KEY);
		BmobInstallation.getCurrentInstallation().save();
		Log.i("Bmob initalized","Bmob initalized");
		try
		{
			UncaughtExceptionHandler.getInstance().init(ModHelperApplication.this);
		}
		catch (PackageManager.NameNotFoundException e)
		{}
		
		// TODO: Implement this method
		super.onCreate ( );
	}
	/*public android.os.Handler getErrMsgHdl ( )
	{
		return merrmsghdl;
	}*/
	public File getResDir(){
		if(resDir==null){
			sdcard=Environment.getExternalStorageDirectory();
			//resfilePath: /sdcard/Android/data/$pkgname
			resfilePath=new StringBuilder()
						.append(sdcard.getAbsolutePath())
						.append(File.separatorChar)
						.append("Android")
						.append(File.separatorChar)
						.append("data")
						.append(File.separatorChar)
						.append(GAME_PKGNAME)
						.toString();
		resDir=new File(resfilePath);
			
		}
		return resDir;
	}
	public String getResPath(){
		return getResDir().getAbsolutePath();
	}
	public File getResFilesDir(){
		if(resfilesdir==null){
			resfilesdir=new File(getResDir(),"files");
		}
		return resfilesdir;
	}
	public String getResFilesDirPath(){
		return getResFilesDir().getAbsolutePath();
	}
	
}
