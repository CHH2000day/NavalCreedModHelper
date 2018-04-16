package com.CHH2000day.navalcreed.modhelper;
import java.io.*;
import java.nio.channels.*;
import java.util.zip.*;
import java.util.*;
import android.net.*;
import android.os.*;
import android.database.*;
import android.content.*;
import android.provider.*;

public class Utils
{
	public static final String FORMAT_OGG=".ogg";
	public static final String FORMAT_WAV=".wav";
	public static final String FORMAT_UNKNOWN="ERROR";
	public static final byte[] HEADER_WAV={ 82, 73, 70, 70 };
	public static final byte[] HEADER_OGG={ 79, 103, 103, 83 };

	public static String identifyFormat ( InputStream in, boolean closeStream ) throws IOException
	{
		byte[] b=new byte[4];
		in.read ( b );
		if ( closeStream )
		{
			in.close ( );
		}
		if ( Arrays.equals ( b, HEADER_WAV ) )
		{
			return FORMAT_WAV;
		}
		if ( Arrays.equals ( b, HEADER_OGG ) )
		{
			return FORMAT_OGG;
		}
		return FORMAT_UNKNOWN;
	}

	public static byte[] readAllbytes ( InputStream in ) throws IOException
	{
		byte[] cache=new byte[1024];
		ByteArrayOutputStream baos=new ByteArrayOutputStream ( );
		int i;
		while ( ( i = in.read ( cache ) ) != -1 )
		{
			baos.write ( cache, 0, i );
		}
		cache = baos.toByteArray ( );
		baos.close ( );
		return cache;
	}
	public static boolean delDir ( File f )
	{
        if ( f == null ) return false;
        if ( !f.exists ( ) ) return true;
        if ( f.isDirectory ( ) )
		{
            File[] fs=f.listFiles ( );
            if ( fs != null )
			{
                for ( File e:fs )
				{
                    if ( !delDir ( e ) ) return false;
                }
            }
        }
        return f.delete ( );
    }
	public static void copyFile ( File infile, File outfile )throws IOException
	{
		if ( !outfile.getParentFile ( ).exists ( ) )
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

	public static void copyFile ( InputStream in, File outfile )throws IOException
	{
		if ( !outfile.getParentFile ( ).exists ( ) )
		{
			outfile.getParentFile ( ).mkdirs ( );
		}
		FileOutputStream fos=new FileOutputStream ( outfile );
		int i;
		byte[] cache=new byte[1024];
		while ( ( i = in.read ( cache ) ) != -1 )
		{
			fos.write ( cache, 0, i );
		}
		fos.flush ( );
		in.close ( );
		fos.close ( );

	}
	public static void decompresssZIPFile ( ZipFile srcFile, String destFilePath ) throws IOException
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
		while ( entries.hasMoreElements ( ) )
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
			if ( !entryFile.getParentFile ( ).exists ( ) )
			{
				entryFile.getParentFile ( ).mkdirs ( );
			}
			//判断该目标文件是否应为目录
			if ( entry.isDirectory ( ) )
			{
				//判断目标目录是否以文件方式存在
				if ( entryFile.isFile ( ) )
				{
					//检测文件是否允许删除，如果不允许删除，将会抛出SecurityException
					securityManager.checkDelete ( entryFilePath );
					//删除已存在的目标文件
					entryFile.delete ( );	
				}
				if ( !entryFile.exists ( ) )
				{
					entryFile.mkdirs ( );
				}
				continue;
			}
			else if ( !entry.isDirectory ( ) )
			{
				if ( entryFile.isDirectory ( ) )
				{
					System.out.println ( "Trying to Delete Dir:" + entryFilePath );
					delDir ( entryFile );
				}
			}


			if ( entryFile.exists ( ) )
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
			while ( ( count = bis.read ( buffer, 0, bufferSize ) ) != -1 )
			{
				bos.write ( buffer, 0, count );
			}
			bos.flush ( );
			bos.close ( );			
		}
	}
	public static String resolveFilePath ( Uri uri , Context ctx )
	{
		//如果path已为绝对路径，直接返回
		if ( uri.getEncodedPath ( ).startsWith ( "/storage" ) )
		{
			return uri.getPath ( );
		}
		//如果为SAF返回的数据，解码
		if ( uri.getAuthority ( ).equals ( "com.android.externalstorage.documents" ) )
		{
			String docId=DocumentsContract.getDocumentId ( uri );
			String [] split = docId.split ( ":" , 2 );
            if ( split.length >= 2 )
			{
                String type = split [ 0 ];
                if ( "primary".equalsIgnoreCase ( type ) )
				{
                    return Environment.getExternalStorageDirectory ( ).getAbsolutePath ( ) + File.separator + split [ 1 ];
                }
				else if ( "secondary".equalsIgnoreCase ( type ) )
				{
					return System.getenv ( "SECONDARY_STORAGE" ) + File.separator + split [ 1 ];
				}
				else
				{
					String[] vol_id=split [ 0 ].split ( String.valueOf ( File.separatorChar ) );
					String vol=vol_id[vol_id.length-1];
					if(vol.contains("-")){
						return new StringBuilder().append(File.separatorChar).append("storage").append(File.separatorChar).append(vol).append(File.separatorChar).append(split[1]).toString();
					}
				}
			}
		}
		//对特殊机型的Uri进行解析
		//解析华为机型
		if ( uri.getAuthority ( ).equalsIgnoreCase ( "com.huawei.hidisk.fileprovider" ) )
		{
			String[] val=uri.getPath ( ).split ( "/root", 2 );
			return val [ 1 ];
		}
		//解析金立机型
		if ( uri.getAuthority ( ).equalsIgnoreCase ( "com.gionee.filemanager.fileprovider" ) )
		{
			String[] val=uri.getPath ( ).split ( "/external_path", 2 );
			return Environment.getExternalStorageDirectory ( ).getAbsolutePath ( ) + val [ 1 ];
		}
		//遍历查询

		Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try
		{
            cursor = ctx.getContentResolver ( ).query ( uri, projection, null, null, null );
            if ( cursor != null && cursor.moveToFirst ( ) )
			{
                final int column_index = cursor.getColumnIndex ( column );
				if ( column_index >= 0 )
				{
					String s=cursor.getString ( column_index );
					if ( s != null )
					{
						return s;
					}
				}
            }
        }
		finally
		{
            if ( cursor != null )
			{
                cursor.close ( );
            }
        }



		String string =uri.toString ( );
		String path[]=new String[2];
		//判断文件是否在sd卡中
		if ( string.indexOf ( String.valueOf ( Environment.getExternalStorageDirectory ( ) ) ) != -1 )
		{
			//对Uri进行切割
			path = string.split ( String.valueOf ( Environment.getExternalStorageDirectory ( ) ) );
			return Environment.getExternalStorageDirectory ( ).getAbsolutePath ( ) + path [ 1 ];
		}
		else if ( string.indexOf ( String.valueOf ( Environment.getDataDirectory ( ) ) ) != -1 )
		{ //判断文件是否在手机内存中
			//对Uri进行切割
			path = string.split ( String.valueOf ( Environment.getDataDirectory ( ) ) );
			return Environment.getDataDirectory ( ).getAbsolutePath ( ) + path [ 1 ];
		}

		return null;

	}
	public static String getErrMsg ( Throwable t )
	{
		Class err=t.getClass ( );
		return err.getName ( ) + "\n" + t.getMessage ( );
	}
}
