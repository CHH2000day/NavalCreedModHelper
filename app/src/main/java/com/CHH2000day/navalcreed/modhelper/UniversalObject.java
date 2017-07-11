package com.CHH2000day.navalcreed.modhelper;
import cn.bmob.v3.*;
import cn.bmob.v3.datatype.*;

public class UniversalObject extends BmobObject
{
	private Integer version;
	private String type;
	private BmobFile packagefile;
	private String changelog;
	private boolean avail;


	public Integer getVersion()
	{
		return version;
	}


	public BmobFile getPackagefile()
	{
		return packagefile;
	}

	public String getChangelog()
	{
		return changelog;
	}}
