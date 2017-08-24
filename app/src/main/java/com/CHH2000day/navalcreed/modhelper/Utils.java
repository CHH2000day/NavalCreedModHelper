package com.CHH2000day.navalcreed.modhelper;
import java.io.*;
import java.nio.channels.*;
import java.util.zip.*;
import java.util.*;

public class Utils
{
	public static byte[] readAllbytes (InputStream in) throws IOException
	{
		byte[] cache=new byte[1024];
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
		FileOutputStream fos=new FileOutputStream ( outfile );
		int i;
		byte[] cache=new byte[1024];
		while ((i = in.read ( cache )) != -1)
		{
			fos.write ( cache, 0, i );
		}
		fos.flush ( );
		in.close ( );
		fos.close ( );

	}
	public static void decompresssZIPFile (ZipFile srcFile, String destFilePath) throws IOException
	{
		ZipEntry entry = null;
		String entryFilePath = null;
		File entryFile = null;
		int count = 0, bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		SecurityManager securityManager = new SecurityManager ( );
		Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>)srcFile.entries ( );
		//循环对压缩包里的每一个文件进行解压		
		while (entries.hasMoreElements ( ))
		{
			entry = entries.nextElement ( );
			System.out.println ( "Selecting file:" + entry.getName ( ) );
			//构建压缩包中一个文件解压后保存的文件全路径
			entryFilePath = new StringBuilder ( )
				.append ( destFilePath )
				.append ( File.separator )
				.append ( entry.getName ( ) )
				.toString ( );
			entryFile = new File ( entryFilePath );
			if (!entryFile.getParentFile ( ).exists ( ))
			{
				entryFile.getParentFile ( ).mkdirs ( );
			}
			//判断该目标文件是否应为目录
			if (entry.getSize ( ) == 0)
			{
				//判断目标目录是否以文件方式存在
				if (entryFile.isFile ( ))
				{
					//检测文件是否允许删除，如果不允许删除，将会抛出SecurityException
					securityManager.checkDelete ( entryFilePath );
					//删除已存在的目标文件
					entryFile.delete ( );	
				}
				entryFile.mkdirs ( );
				continue;
			}
			else if (entry.isDirectory ( ))
			{
				System.out.println ( "Trying to Delete Dir:" + entryFilePath );
				delDir ( entryFile );
			}


			if (entryFile.exists ( ))
			{
				//检测文件是否允许删除，如果不允许删除，将会抛出SecurityException
				securityManager.checkDelete ( entryFilePath );
				//删除已存在的目标文件
				entryFile.delete ( );	
			}

			//写入文件
			System.out.println ( "Trying to write to file:" + entryFilePath );
			bos = new BufferedOutputStream ( new FileOutputStream ( entryFile ) );
			bis = new BufferedInputStream ( srcFile.getInputStream ( entry ) );
			while ((count = bis.read ( buffer, 0, bufferSize )) != -1)
			{
				bos.write ( buffer, 0, count );
			}
			bos.flush ( );
			bos.close ( );			
		}
	}
}
