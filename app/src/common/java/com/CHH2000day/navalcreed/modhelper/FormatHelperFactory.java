package com.CHH2000day.navalcreed.modhelper;
import android.net.*;
import android.content.*;
import java.util.*;
import java.io.*;

public class FormatHelperFactory
{
	private static HashMap<Uri,AudioFormatHelper> audiohelpers;
	public static AudioFormatHelper getAudioFormatHelper ( Uri file, Context ctx )
	{
		AudioFormatHelper afh=null;
		if ( audiohelpers == null )
		{
			audiohelpers = new HashMap<Uri,AudioFormatHelper> ( );
		}
		afh = audiohelpers.get ( file );
		if ( afh == null )
		{
			afh = new AudioFormatHelper ( file, ctx );
			audiohelpers.put ( file, afh );
		}
		return afh;
	}
	public synchronized static void refreshCache ( File file )
	{
		if ( audiohelpers == null )
		{
			return;
		}
		Collection c=audiohelpers.values ( );
		Iterator i=c.iterator ( );
		while ( i.hasNext ( ) )
		{
			AudioFormatHelper afh=(AudioFormatHelper)i.next ( );
			afh.denyCache ( file );
		}


	}
	public synchronized static void denyAllCaches ( )
	{
		if ( audiohelpers == null )
		{
			return;
		}
		Collection c=audiohelpers.values ( );
		Iterator i=c.iterator ( );
		while ( i.hasNext ( ) )
		{
			AudioFormatHelper afh=(AudioFormatHelper)i.next ( );
			afh.denyCache ( null, afh.MODE_DENY_ALL_CACHE );
		}

	}
	
}
