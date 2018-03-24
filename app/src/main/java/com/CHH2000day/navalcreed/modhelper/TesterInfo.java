package com.CHH2000day.navalcreed.modhelper;
import cn.bmob.v3.*;

public class TesterInfo extends BmobObject
{
	private String key;
	private String model;
	private String deviceId;


	/*
	//不在客户端中编译
	public void setKey ( String key )
	{
		this.key = key;
	}
	
	public String getKey ( )
	{
		return key;
	}

	*/
	public void setModel ( String model )
	{
		this.model = model;
	}

	public String getModel ( )
	{
		return model;
	}

	public void setdeviceId ( String deviceId )
	{
		this.deviceId = deviceId;
	}

	public String getdeviceId ( )
	{
		return deviceId;
	}
}
