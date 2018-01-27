package com.CHH2000day.navalcreed.modhelper;
import java.io.*;
import android.graphics.*;
import org.json.*;
import com.CHH2000day.navalcreed.modhelper.ModPackageInfo.*;
import okio.*;

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

	//mod信息相关
	private static final String KEY_MINSUPPORTVER="minSupportVer";
	private static final String KEY_TARGETVER="targetVer";
	private static final String KEY_MODNANE="name";
	private static final String KEY_MODAUTHOR="author";
	private static final String KEY_MODINFO="modInfo";
	private static final String KEY_MODTYPE="modType";
	private static final String KEY_HASPREVIEW="hasPreview";
	private static final String KEY_PREVIEW="preView";

	private String modName;
	private String modType;
	private String modAuthor;
	private String modInfo;
	private Bitmap modPreview;
	private int modTargetVer;


	private ModPackageInfo ()
	{

	}

	private void setModName (String modName)
	{
		this.modName = modName;
	}

	public String getModName ()
	{
		return modName;
	}

	private void setModType (String modType)
	{
		this.modType = modType;
	}

	public String getModType ()
	{
		return modType;
	}

	private void setModAuthor (String modAuthor)
	{
		this.modAuthor = modAuthor;
	}

	public String getModAuthor ()
	{
		return modAuthor;
	}

	private void setModInfo (String modInfo)
	{
		this.modInfo = modInfo;
	}

	public String getModInfo ()
	{
		return modInfo;
	}

	private void setModPreview (Bitmap modPreview)
	{
		this.modPreview = modPreview;
	}

	public boolean hasPreview(){
		return (modPreview==null);
	}
	public Bitmap getModPreview ()
	{
		return modPreview;
	}

	private void setModTargetVer (int modTargetVer)
	{
		this.modTargetVer = modTargetVer;
	}

	public int getModTargetVer ()
	{
		return modTargetVer;
	}
	public boolean hasAllFeature(){
		return (PKGVER>=modTargetVer);
	}










	//使用Factory模式构造该实例
	public static class Factory
	{
		public static ModPackageInfo createFromInputStream (InputStream in) throws IOException, JSONException, ModPackageInfo.IllegalModInfoException
		{
			if (in == null)
			{
				throw new NullPointerException ( "InputStream could not be null!" );
			}
			//描述文件大小不会太大，因而直接read
			byte[] cache=new byte[in.available ( )];
			in.read ( cache );
			in.close ( );
			return createFromByteArray ( cache );
		}
		public static ModPackageInfo createFromByteArray (byte[]data) throws JSONException, ModPackageInfo.IllegalModInfoException
		{
			if (data == null)
			{
				throw new NullPointerException ( "Data could not be null" );
			}
			JSONObject jo=new JSONObject ( new String ( data ) );
			ModPackageInfo mpi=new ModPackageInfo ( );
			//检查最低兼容版本
			if (mpi.PKGVER < jo.getInt ( mpi.KEY_MINSUPPORTVER ))
			{
				throw new IllegalModInfoException ( new StringBuilder ( ).append ( "Installer version too low.This mod package requires a minimum version of" )
												   .append ( " " )
												   .append ( jo.getInt ( mpi.KEY_MINSUPPORTVER ) )
												   .append ( "." )
												   .append ( "But mod installer is version" )
												   .append ( " " )
												   .append ( mpi.PKGVER )
												   .append ( "." )
												   .toString ( )
												   );
			}
			mpi.setModName(jo.getString(mpi.KEY_MODNANE));
			mpi.setModType(jo.getString(mpi.KEY_MODTYPE));
			mpi.setModAuthor(jo.getString(mpi.KEY_MODAUTHOR));
			mpi.setModInfo(jo.getString(mpi.KEY_MODINFO));
			mpi.setModTargetVer(jo.getInt(mpi.KEY_TARGETVER));
			//检查是否有预览图并解码
			if(jo.getBoolean(mpi.KEY_HASPREVIEW)){
				byte[] piccache=Base64.decode(jo.getString(mpi.KEY_PREVIEW));
				mpi.setModPreview(BitmapFactory.decodeByteArray(piccache,0,piccache.length));
			}

			return mpi;
		}
	}





	public static class IllegalModInfoException extends Exception
	{
		public IllegalModInfoException (String info)
		{
			super ( info );
		}
	}
}
