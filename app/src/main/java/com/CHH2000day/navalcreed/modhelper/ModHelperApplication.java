package com.CHH2000day.navalcreed.modhelper;
import android.app.*;
import android.content.pm.*;
import android.util.*;
import cn.bmob.v3.*;
import java.io.*;
public class ModHelperApplication extends Application
{private android.os.Handler merrmsghdl;
private File parfile;
private String parfilePath="";
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
		merrmsghdl = new android.os.Handler(){
			public void handleMessage ( android.os.Message msg )
			{
				AlertDialog.Builder adb=new AlertDialog.Builder ( ModHelperApplication.this );
				adb.setTitle ( "Oops..." )
					.setMessage ( msg.obj.toString ( ) )
					.setPositiveButton ( "OK", null )
					.create ( )
					.show ( );
			}
		};
		Bmob.initialize(ModHelperApplication.this,StaticData.API_KEY);
		
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
	public android.os.Handler getErrMsgHdl ( )
	{
		return merrmsghdl;
	}
	public File getResDir(){
		if(parfile==null){
			
		}
		return null;
	}
	public String getResFilePath(){
		if("".equals(parfilePath)){
			parfilePath="sdcard/Android/data/com.tencent.navalcreed";
		}
		return parfilePath;
	}
	
}
