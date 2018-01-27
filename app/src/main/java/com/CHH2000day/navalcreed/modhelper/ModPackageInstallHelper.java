package com.CHH2000day.navalcreed.modhelper;
import java.io.*;
import java.util.zip.*;

public class ModPackageInstallHelper
{
	//常量声明
	private static final String FILE_MODINFO="mod.info";
	
	private File msrcFile;
	private ZipFile mpkgFile;
	
	
	
	
	public ModPackageInstallHelper(File pkgFile) throws IOException{
		msrcFile=pkgFile;
		init();
	}
	
	private void init() throws IOException{
		//创建mod文件实例
		fetch();
		//识别Mod文件并读取信息
		identify();
	}
	
	private void fetch() throws IOException{
		mpkgFile=new ZipFile(msrcFile);
		
	}
	private void identify() throws IOException{
		ZipEntry mInfoFile=mpkgFile.getEntry(FILE_MODINFO);
		InputStream zi=mpkgFile.getInputStream(mInfoFile);
		
	}
	public void install(){
		
	}
	public ModPackageInfo getModPackageInfo(){
		
		return null;
	}
}
