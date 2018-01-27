package com.CHH2000day.navalcreed.modhelper;
import java.io.*;
import android.graphics.*;

public class ModPackageInfo
{
	//常量声明
	
	//软件版本
	public static final int PKGVER=0;
	
	public static final String MODTYPE_CV="CaptainVoice";
	public static final String MODTYPE_SOUNDEFFECT="SoundEffect";
	public static final String MODTYPE_BGM="BackgroundMusic";
	public static final String MODTYPE_BACKGROUND="Background";
	public static final String MODTYPE_CREWPIC="CrewPic";
	public static final String MODTYPE_OTHER="Other";
	
	
	private String modName;
	private String modType;
	private String modAuthor;
	private String modInfo;
	private Bitmap[] modPreview;
	private int modTargetVer;
	
	
	private ModPackageInfo(){
		
	}
	public static class Factory{
		public static ModPackageInfo createFromInputStream(InputStream in) throws IOException{
			if(in==null){
				throw new NullPointerException("InputStream could not be null!");
			}
			//描述文件大小不会太大，因而直接read
			byte[] cache=new byte[in.available()];
			in.read(cache);
			return createFromByteArray(cache);
		}
		public static ModPackageInfo createFromByteArray(byte[]data){
			if(data==null){
				throw new NullPointerException("Data could not be null");
			}
			ModPackageInfo mpi=new ModPackageInfo();
			
			return null;
			}
	}
}
