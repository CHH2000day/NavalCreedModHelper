package com.CHH2000day.navalcreed.modhelper;
import cn.bmob.v3.*;
import android.util.*;
import android.os.*;

public class Bugly extends BmobObject
{
	private String MODEL;
	private String ANDROID_SDK_VER;
	private String SYSTEM_VER;
	private Long TIME;
	private Integer APPVER_INT;
	private String Data;
	private String APP_VER;
	public Bugly postBug(String data,long time,String appver,int appver_int){
		TIME=new Long(time);
		MODEL=Build.MODEL;
		ANDROID_SDK_VER=Build.VERSION.SDK;
		SYSTEM_VER=Build.VERSION.RELEASE;
		APPVER_INT=appver_int;
		APP_VER=appver;
		Data=data;
		return this;
	}
	public Bugly(){
		setTableName("bugly");
	}

}
