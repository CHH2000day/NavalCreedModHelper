package com.CHH2000day.navalcreed.modhelper;
import cn.bmob.v3.*;
import android.util.*;
import android.os.*;

public class Bugly extends BmobObject
{
    public Bugly postBug(String data, long time, String appver, int appver_int) {
        Long TIME = Long.valueOf(time);
        String MODEL = Build.MODEL;
        String ANDROID_SDK_VER = Build.VERSION.SDK;
        String SYSTEM_VER = Build.VERSION.RELEASE;
        Integer APPVER_INT = appver_int;
        String APP_VER = appver;
        String data1 = data;
		return this;
	}
	public Bugly(){
		setTableName("bugly");
	}

}
