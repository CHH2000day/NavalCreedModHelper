package com.CHH2000day.navalcreed.modhelper;

public class StaticData
{
	public static final String API_KEY="c6e9ff72dba67952e2da6ba65474cb34";
	public static final String DATAID_BETA="NpDx111F";
	public static final String DATAID_RELEASE="vae3AAAP";
	public static final String DATA_ID_ANTIHEXIE="NkYO888W";
	public static final String DATA_ID_ANNOUNCEMENT="ZeHyBBBP";

	public static final String getDataid()
	{
		if (BuildConfig.DEBUG)
		{
			return DATAID_BETA;
		}
		return DATAID_RELEASE;
	}
}
