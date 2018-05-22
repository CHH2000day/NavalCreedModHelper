package com.CHH2000day.navalcreed.modhelper;
import java.io.*;
import java.text.*;

public class Logger
{
	private static Logger logger;
	private SimpleDateFormat sdf;
	private boolean inited=false;
	private File logFile;
	private OutputStream os;
	
	private Logger(){
		sdf=new SimpleDateFormat("");
	}
	public synchronized static Logger getInstance(){
		if(logger==null){
			logger=new Logger();
		}
		return logger;
	}
	public void init(File logFile) throws FileNotFoundException{
		this.logFile=logFile;
		os=new FileOutputStream(this.logFile);
		inited=true;
	}
	
}
