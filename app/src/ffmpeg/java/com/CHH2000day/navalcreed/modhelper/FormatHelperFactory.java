package com.CHH2000day.navalcreed.modhelper;
import android.net.*;
import android.content.*;
import java.util.*;
import java.io.*;
import com.github.hiteshsondhi88.libffmpeg.*;
import com.github.hiteshsondhi88.libffmpeg.exceptions.*;

public class FormatHelperFactory
{
	private static HashMap<Uri,AudioFormatHelper> audiohelpers;
	private static boolean loadedFFmpeg=false,loadingFFmpeg=false;
	public static AudioFormatHelper getAudioFormatHelper ( Uri file, Context ctx )
	{
		AudioFormatHelper afh=null;
		if(!loadedFFmpeg&&!loadingFFmpeg){
			loadFFmpeg(ctx);
		}
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
	public static void loadFFmpeg(Context ctx){
		try
		{
			FFmpeg.getInstance(ctx).loadBinary(new FFmpegLoadBinaryResponseHandler(){

					@Override
					public void onFailure()
					{
						loadedFFmpeg=false;
						throw new RuntimeException("Filed to load ffmpeg lib");
						// TODO: Implement this method
					}

					@Override
					public void onSuccess()
					{
						loadedFFmpeg=true;
						// TODO: Implement this method
					}

					@Override
					public void onStart()
					{
						loadingFFmpeg=true;
						// TODO: Implement this method
					}

					@Override
					public void onFinish()
					{
						loadingFFmpeg=false;
						// TODO: Implement this method
					}
				});
		}
		catch (FFmpegNotSupportedException e)
		{
			loadedFFmpeg=false;
		}
	}
}
