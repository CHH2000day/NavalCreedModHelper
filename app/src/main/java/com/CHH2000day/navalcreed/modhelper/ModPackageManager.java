package com.CHH2000day.navalcreed.modhelper;
import java.util.*;
import java.io.*;

public class ModPackageManager
{
	//<ModType,ModName>
	private Map<String,String> installedMod;
	private File storedFile;
	private static ModPackageManager mmm;
	public static ModPackageManager getInstance(){
		if(mmm==null){
			mmm=new ModPackageManager();
		}
		return mmm;
	}
	private ModPackageManager(){
	}
	public void init(File storFile){
		storedFile=storFile;
	}
	
}
