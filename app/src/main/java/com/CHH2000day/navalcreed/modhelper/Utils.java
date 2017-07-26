package com.CHH2000day.navalcreed.modhelper;
import java.io.*;
import java.nio.channels.*;

public class Utils
{
	public static byte[] readAllbytes (InputStream in) throws IOException
	{
		byte[] cache=new byte[8];
		ByteArrayOutputStream baos=new ByteArrayOutputStream ( );
		int i;
		while ((i = in.read ( cache )) != -1)
		{
			baos.write ( cache, 0, i );
		}
		cache = baos.toByteArray ( );
		baos.close ( );
		return cache;
	}
	public static boolean delDir (File f)
	{
        if (f == null) return false;
        if (!f.exists ( )) return true;
        if (f.isDirectory ( ))
		{
            File[] fs=f.listFiles ( );
            if (fs != null)
			{
                for (File e:fs)
				{
                    if (!delDir ( e )) return false;
                }
            }
        }
        return f.delete ( );
    }
	public static void copyFile (File infile, File outfile)throws IOException
	{
		if (!outfile.getParentFile ( ).exists ( ))
		{
			outfile.getParentFile ( ).mkdirs ( );
		}
		FileChannel inchannel=null;
		FileChannel outchannel=null;
		try
		{
			inchannel = new FileInputStream ( infile ).getChannel ( );
			outchannel = new FileOutputStream ( outfile ).getChannel ( );
			outchannel.transferFrom ( inchannel, 0, inchannel.size ( ) );
		}
		finally
		{
			inchannel.close ( );
			outchannel.close ( );

		}

	}

	public static void copyFile (InputStream in, File outfile)throws IOException
	{
		if (!outfile.getParentFile ( ).exists ( ))
		{
			outfile.getParentFile ( ).mkdirs ( );
		}
		FileOutputStream fos=new FileOutputStream(outfile);
		int i;
		byte[] cache=new byte[8];
		while((i=in.read(cache))!=-1){
			fos.write(cache,0,i);
		}
		fos.flush();
		in.close();
		fos.close();
		
	}
}
