package com.CHH2000day.navalcreed.modhelper;

public class StaticData
{
	public static final String API_KEY="c6e9ff72dba67952e2da6ba65474cb34";
	public static final String DATAID_ALPHA="NpDx111F";
	public static final String DATAID_RELEASE="vae3AAAP";
	public static final String DATAID=BuildConfig.DEBUG?DATAID_ALPHA:DATAID_RELEASE;
	public static final String DATA_ID_ANTIHEXIE="NkYO888W";
	//public static final String DATA_ID_ANNOUNCEMENT="ZeHyBBBP";
	private static final String DATA_ID_ANNOUNCEMENT_ALPHA="aujE666H";
	private static final String DATA_ID_ANNOUNCEMENT_RELEASE="ZeHyBBBP";
	public static final String DATA_ID_ANNOUNCEMENT=BuildConfig.DEBUG ? DATA_ID_ANNOUNCEMENT_ALPHA: DATA_ID_ANNOUNCEMENT_RELEASE;
	public static final String KEY_TESTER_ID="Testet_id";
	/*public static final String getDataid ( )
	{
		if ( BuildConfig.DEBUG )
		{
			return DATAID_ALPHA;
		}
		return DATAID_RELEASE;
	}*/
}
