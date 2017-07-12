package com.CHH2000day.navalcreed.modhelper;
import cn.bmob.v3.*;

public class BmobMessage extends BmobObject
{
	private Integer msgid;
	private String message;
	private String tocopy;

	public int getmsgid()
	{
		return msgid.intValue();
	}

	public String tocopy(){
		return tocopy;
	}
	public String getMessage()
	{
		return message;
	}
}
