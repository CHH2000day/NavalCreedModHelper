package com.CHH2000day.navalcreed.modhelper;

public class StaticData
{
	public static final String API_KEY="/*This is the api key,not public*/";
	public static final String DATAID_BETA="/*Not public*/";
	public static final String DATAID_RELEASE="/*Not public*/";
	public static final String DATA_ID_ANTIHEXIE="/*Not public*/";
	public static final String DATA_ID_ANNOUNCEMENT="/*Not public*/";
	public static final String KEY_TESTER_ID="Testet_id";
	public static final String getDataid()
	{
		if (BuildConfig.DEBUG)
		{
			return DATAID_BETA;
		}
		return DATAID_RELEASE;
	}
}
