package com.CHH2000day.navalcreed.modhelper;
import java.io.*;

public class Utils
{public static byte[] readAllbytes(InputStream in) throws IOException{
	byte[] cache=new byte[8];
	ByteArrayOutputStream baos=new ByteArrayOutputStream();
	int i;
	while((i=in.read(cache))!=-1){
		baos.write(cache,0,i);
	}
	cache=baos.toByteArray();
	baos.close();
	return cache;
}
	public static boolean delDir(File f){
        if (f==null) return false;
        if (!f.exists()) return true;
        if (f.isDirectory()){
            File[] fs=f.listFiles();
            if (fs!=null){
                for(File e:fs) {
                    if (!delDir(e)) return false;
                }
            }
        }
        return f.delete();
    }
	
}
