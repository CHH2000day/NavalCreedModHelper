package com.CHH2000day.navalcreed.modhelper;
import java.util.*;
import java.io.*;
import okio.*;
import org.json.*;

public class ModPackageManager
{
	//<ModType,ModName>
	private HashMap<String,String> installedMod;
	private File configFile;
	private boolean isOverride=false;
	private static final String OVRD="override";
	private static ModPackageManager mmm;

	public static ModPackageManager getInstance ()
	{
		if (mmm == null)
		{
			mmm = new ModPackageManager ( );
		}
		return mmm;
	}
	private ModPackageManager ()
	{}
	public void init (File storedFile) throws JSONException, IOException
	{
		configFile = storedFile;
		installedMod = new HashMap<String,String> ( );
		reflesh ( );

	}
	private void reflesh () throws JSONException, IOException
	{
		try
		{
			Source s=Okio.source ( configFile );
			updateConfig ( false );
			BufferedSource bs=Okio.buffer ( s );
			JSONObject jo=new JSONObject ( bs.readUtf8 ( ) );
			bs.close ( );
			installedMod.put ( ModPackageInfo.MODTYPE_BACKGROUND, jo.getString ( ModPackageInfo.MODTYPE_BACKGROUND ) );
			installedMod.put ( ModPackageInfo.MODTYPE_BGM, jo.getString ( ModPackageInfo.MODTYPE_BGM ) );
			installedMod.put ( ModPackageInfo.MODTYPE_CREWPIC, jo.getString ( ModPackageInfo.MODTYPE_CREWPIC ) );
			installedMod.put ( ModPackageInfo.MODTYPE_SOUNDEFFECT, jo.getString ( ModPackageInfo.MODTYPE_SOUNDEFFECT ) );
			JSONObject j=jo.getJSONObject ( ModPackageInfo.MODTYPE_CV );
			installedMod.put ( ModPackageInfo.SUB_MODTYPE_CV_CN, j.getString ( ModPackageInfo.SUB_MODTYPE_CV_CN ) );
			installedMod.put ( ModPackageInfo.SUB_MODTYPE_CV_EN, j.getString ( ModPackageInfo.SUB_MODTYPE_CV_EN ) );
			if(jo.has(OVRD)){
				isOverride=jo.getBoolean(OVRD);
			}
		}
		catch (FileNotFoundException t)
		{try
			{
				updateConfig ( true );
				reflesh ( );
			}
			catch (FileNotFoundException e)
			{}
			catch (JSONException e)
			{}
		}

	}
	private void commit () throws IOException, JSONException
	{
		updateConfig ( false );
		reflesh ( );
	}

	public void postUninstall (String modtype, String subtype)
	{
		if(isOverride){
			return;
		}
		if (modtype.equals ( ModPackageInfo.MODTYPE_CV ))
		{
			installedMod.put ( subtype, "" );
		}
		else
		{
			installedMod.put ( modtype, "" );
		}
		try
		{
			commit ( );
		}
		catch (IOException e)
		{e.printStackTrace ( );}
		catch (JSONException e)
		{e.printStackTrace ( );}
		return;

	}
	public void postInstall (String modtype, String subtype, String modname)
	{
		if(isOverride){
			return;
		}
		if (modtype.equals ( ModPackageInfo.MODTYPE_CV ))
		{
			installedMod.put ( subtype, modname );
		}
		else
		{
			installedMod.put ( modtype, modname );
		}
		try
		{
			commit ( );
		}
		catch (IOException e)
		{e.printStackTrace ( );}
		catch (JSONException e)
		{e.printStackTrace ( );}

	}

	private void updateConfig (boolean isNew) throws JSONException, IOException
	{
		JSONObject jo=new JSONObject ( );
		if (isNew || installedMod.isEmpty ( ))
		{
			jo.put ( ModPackageInfo.MODTYPE_BGM, "" );
			jo.put ( ModPackageInfo.MODTYPE_BACKGROUND, "" );
			jo.put ( ModPackageInfo.MODTYPE_CREWPIC, "" );
			jo.put ( ModPackageInfo.MODTYPE_SOUNDEFFECT, "" );
			JSONObject jcv=new JSONObject ( );
			jcv.put ( ModPackageInfo.SUB_MODTYPE_CV_CN, "" );
			jcv.put ( ModPackageInfo.SUB_MODTYPE_CV_EN, "" );
			jo.put ( ModPackageInfo.MODTYPE_CV, jcv );
			writeConfigFile ( jo );
			return;			
		}
		else
		{
			jo.put ( ModPackageInfo.MODTYPE_BGM, installedMod.get ( ModPackageInfo.MODTYPE_BGM ) );
			jo.put ( ModPackageInfo.MODTYPE_BACKGROUND, installedMod.get ( ModPackageInfo.MODTYPE_BACKGROUND ) );
			jo.put ( ModPackageInfo.MODTYPE_CREWPIC, installedMod.get ( ModPackageInfo.MODTYPE_CREWPIC ) );
			jo.put ( ModPackageInfo.MODTYPE_SOUNDEFFECT, installedMod.get ( ModPackageInfo.MODTYPE_SOUNDEFFECT ) );
			JSONObject jcv=new JSONObject ( );
			jcv.put ( ModPackageInfo.SUB_MODTYPE_CV_CN, installedMod.get ( ModPackageInfo.SUB_MODTYPE_CV_CN ) );
			jcv.put ( ModPackageInfo.SUB_MODTYPE_CV_EN, installedMod.get ( ModPackageInfo.SUB_MODTYPE_CV_EN ) );
			jo.put ( ModPackageInfo.MODTYPE_CV, jcv );
			jo.put(OVRD,isOverride);
			writeConfigFile ( jo );
		}


	}
	private void writeConfigFile (JSONObject jo) throws  IOException
	{
		if (!configFile.getParentFile ( ).exists ( ))
		{
			configFile.getParentFile ( ).mkdirs ( );
		}
		if (configFile.isDirectory ( ))
		{
			Utils.delDir ( configFile );
		}
		Sink s=Okio.sink ( configFile );
		BufferedSink bs=Okio.buffer ( s );
		bs.writeUtf8 ( jo.toString ( ) );
		bs.flush();
		bs.close ( );
	}
	public void setIsOverride ( boolean isOverride )
	{
		this.isOverride = isOverride;
		try
		{
			commit ( );
		}
		catch (IOException e)
		{e.printStackTrace();}
		catch (JSONException e)
		{e.printStackTrace();}
	}

	public boolean isOverride ( )
	{
		return isOverride;
	}
	
	public HashMap<String,String> getModList ()
	{
		return installedMod;
	}

	public boolean checkInstalled (String type, String subtype)
	{
		if(isOverride){
			return false;
		}
		if (type.equals ( ModPackageInfo.MODTYPE_CV ))
		{
			return (!"".equals ( installedMod.get ( subtype ) ));
		}
		return (!"".equals ( installedMod.get ( type ) ));
	}

}
