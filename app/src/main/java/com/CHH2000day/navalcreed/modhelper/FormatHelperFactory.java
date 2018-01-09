package com.CHH2000day.navalcreed.modhelper;
import android.net.*;
import android.content.*;
import java.util.*;

public class FormatHelperFactory
{
	private static TreeMap<Uri,AudioFormatHelper> audiohelpers;
	public static AudioFormatHelper getAudioFormatHelper ( Uri file, Context ctx )
	{
		AudioFormatHelper afh=null;
		if ( audiohelpers == null )
		{
			audiohelpers = new TreeMap<Uri,AudioFormatHelper> ( );
		}
		afh = audiohelpers.get ( file );
		if ( afh == null )
		{
			afh = new AudioFormatHelper ( file, ctx );
			audiohelpers.put ( file, afh );
		}
		return afh;

	}
}
