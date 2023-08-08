package com.chh2000day.navalcreed.modhelper;

public class StaticData
{
	public static final String API_KEY="1aea5425cdef9a5a7a17cafcd8df925b";
	public static final String DATAID_ALPHA="dc1311a323";
	public static final String DATAID_RELEASE="fb087fb234";
	public static final String DATAID=BuildConfig.DEBUG?DATAID_ALPHA:DATAID_RELEASE;
	public static final String DATA_ID_ANTIHEXIE="945119b17f";
	//public static final String DATA_ID_ANNOUNCEMENT="ZeHyBBBP";
	private static final String DATA_ID_ANNOUNCEMENT_ALPHA="fb6aec3eec";
	private static final String DATA_ID_ANNOUNCEMENT_RELEASE="6aff9eb4d9";
	public static final String DATA_ID_ANNOUNCEMENT= BuildConfig.DEBUG ? DATA_ID_ANNOUNCEMENT_ALPHA: DATA_ID_ANNOUNCEMENT_RELEASE;
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
