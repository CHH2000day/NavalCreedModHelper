package com.CHH2000day.navalcreed.modhelper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import com.orhanobut.logger.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;

public class ModPackageInfo
{
	//常量声明

	//软件版本
    public static final int PKGVER = Versions.VER_5;

	public static final String MODTYPE_CV="CaptainVoice";
	public static final String MODTYPE_SOUNDEFFECT_PRIM="SoundEffect_PRIM";
	public static final String MODTYPE_SOUNDEFFECT_SEC="SoundEffect_SEC";
	public static final String MODTYPE_SOUNDEFFECT="SoundEffect";
	public static final String MODTYPE_BGM="BackgroundMusic";
	public static final String MODTYPE_BACKGROUND="Background";
	public static final String MODTYPE_CREWPIC="CrewPic";
	public static final String MOSTYPE_CUSTOMSHIPNAME = "CustomShipName";
	public static final String MODTYPE_OTHER="Other";
	public static final String SUBTYPE_EMPTY="";
	public static final String SUB_MODTYPE_CV_CN="CV_CN";
	public static final String SUB_MODTYPE_CV_EN="CV_EN";
	public static final String SUB_MODTYPE_CV_JP_CV="CV_JP_CV";
	public static final String SUB_MODTYPE_CV_JP_BB="CV_JP_BB";
	public static final String SUB_MODTYPE_CV_JP_CA="CV_JP_CA";
	public static final String SUB_MODTYPE_CV_JP_DD="CV_JP_DD";
	public static final String SUB_MODTYPE_CV_DE="CV_DE";
	public static final String SUB_MODTYPE_CV_RU="CV_RU";
	public static final String SUB_MODTYPE_CV_RU_VLAD="CV_RU_VLAD";
	public static final String SUB_MODTYPE_CV_RU_BEARD="CV_RU_BEARD";
	private static ArrayList<String> abandoned_types;
	//mod信息相关
	private static final String KEY_MINSUPPORTVER="minSupportVer";
	private static final String KEY_TARGETVER="targetVer";
	private static final String KEY_MODNANE="name";
	private static final String KEY_MODAUTHOR="author";
	private static final String KEY_MODINFO="modInfo";
	private static final String KEY_MODTYPE="modType";
	private static final String KEY_HASPREVIEW="hasPreview";
	private static final String KEY_PREVIEW="preview";

	private String modName;
	private String modType;
	private String modAuthor;
	private String modInfo;
	private SoftReference<Bitmap> modPreview;
	private int modTargetVer;
	private byte[] byteizedPic;


	static{
		abandoned_types = new ArrayList<String> ( );
		abandoned_types.add ( MODTYPE_SOUNDEFFECT );
	}
	private ModPackageInfo ( )
	{

	}

	public static boolean checkIsAbandoned ( String modtype )
	{
		return abandoned_types.contains ( modtype );
	}

	private void setModName ( String modName )
	{
		this.modName = modName;
	}

	public String getModName ( )
	{
		return modName;
	}

	private void setModType ( String modType )
	{
		this.modType = modType;
	}

	public String getModType ( )
	{
		return modType;
	}

	private void setModAuthor ( String modAuthor )
	{
		this.modAuthor = modAuthor;
	}

	public String getModAuthor ( )
	{
		return modAuthor;
	}

	private void setModInfo ( String modInfo )
	{
		this.modInfo = modInfo;
	}

	public String getModInfo ( )
	{
		return modInfo;
	}

	private void setModPreview ( byte[] data )
	{
		byteizedPic = data;
		data = null;
		performPreviewLoad ( );
	}


	public boolean hasPreview ( )
	{
		return ( getModPreview ( ) != null );
	}
	public Bitmap getModPreview ( )
	{
		return doGetModPreview ( );
	}

	private void setModTargetVer ( int modTargetVer )
	{
		this.modTargetVer = modTargetVer;
	}

	public int getModTargetVer ( )
	{
		return modTargetVer;
	}
	public boolean hasAllFeature ( )
	{
		return ( PKGVER >= modTargetVer );
	}
	public boolean isAbandoned ( )
	{
		return checkIsAbandoned ( getModType ( ) );
	}

    public int getVersion() {
        return 0;
        //TODO:Add this in mod.info
    }
	private Bitmap doGetModPreview ( )
	{
		if ( modPreview == null )
		{
			return null;
		}
		if ( modPreview.get ( ) != null )
		{
			return modPreview.get ( );
		}
		if ( performPreviewLoad ( ) )
		{
			return modPreview.get ( );
		}
		else
		{
			return null;
		}
	}
	private boolean performPreviewLoad ( )
	{
		if ( byteizedPic == null || byteizedPic.length == 0 )
		{
			return false;
		}
		modPreview = new SoftReference<Bitmap> ( BitmapFactory.decodeByteArray ( byteizedPic, 0, byteizedPic.length ) );
		return ( modPreview.get ( ) != null );
	}
	private void setModPreview ( Bitmap bitmap )
	{
		modPreview = new SoftReference<Bitmap> ( bitmap );
	}

	@Override
	public void finalize ( ) throws Throwable
	{
		// TODO: Implement this method
		super.finalize ( );
		if ( modPreview != null && modPreview.get ( ) != null )
		{
			modPreview.get ( ).recycle ( );
		}
	}





	//使用Factory模式构造该实例
	public static class Factory
	{
		public static ModPackageInfo createFromInputStream ( InputStream in ) throws IOException, ModPackageInfo.IllegalModInfoException
		{
			if ( in == null )
			{
				throw new NullPointerException ( "InputStream could not be null!" );
			}

			byte[] cache=Utils.readAllbytes ( in );
			in.close ( );
			return createFromByteArray ( cache );
		}
		public static ModPackageInfo createFromByteArray ( byte[]data ) throws ModPackageInfo.IllegalModInfoException
		{
			if ( data == null )
			{
				throw new NullPointerException ( "Data could not be null" );
			}
			try
			{
				JSONObject jo=new JSONObject ( new String ( data ) );
				ModPackageInfo mpi=new ModPackageInfo ( );
				//检查最低兼容版本
				if ( mpi.PKGVER < jo.getInt ( mpi.KEY_MINSUPPORTVER ) )
				{
					throw new IllegalModInfoException ( new StringBuilder ( ).append ( "Installer version is too low.This mod package requires a minimum version of " )
													   .append ( " " )
													   .append ( jo.getInt ( mpi.KEY_MINSUPPORTVER ) )
													   .append ( "." )
													   .append ( "But this mod requires a installer version of " )
													   .append ( " " )
													   .append ( mpi.PKGVER )
													   .append ( "." )
													   .toString ( )
													   );
				}
				mpi.setModName ( jo.getString ( mpi.KEY_MODNANE ) );
				mpi.setModType ( jo.getString ( mpi.KEY_MODTYPE ) );
				mpi.setModAuthor ( jo.getString ( mpi.KEY_MODAUTHOR ) );
				mpi.setModInfo ( jo.getString ( mpi.KEY_MODINFO ) );
				mpi.setModTargetVer ( jo.getInt ( mpi.KEY_TARGETVER ) );
				//检查是否有预览图并解码
				String b64pic=jo.getString ( mpi.KEY_PREVIEW );
				//Not to load pic in mod.info if it's smaller than 40 bytes
				//If external preview is used keep the value empty(recommended) or less than 60 bytes long
				//NO preview pic can be shortrer than 40 bytes(60 bytes after encoded into base64)
				if ( jo.getBoolean ( mpi.KEY_HASPREVIEW ) && b64pic != null && b64pic.length ( ) >= 60 )
				{
					byte[] piccache=android.util.Base64.decode ( jo.getString ( mpi.KEY_PREVIEW ), android.util.Base64.DEFAULT );
					mpi.setModPreview ( piccache  );
				}
				Logger.i("ModPackageInfo created");
				Logger.i("Mod name:%s",mpi.getModName());
				Logger.i("Mod type:%s",mpi.getModType());
				Logger.i("Target verion:%s",mpi.getModTargetVer());
				return mpi;

			}
			catch (JSONException e)
			{
				throw new IllegalModInfoException ( "Invalid mod.info" );
			}
		}
		public static ModPackageInfo createFromInputStreamWithExternalPic ( InputStream in, @NonNull InputStream picStream ) throws IOException, ModPackageInfo.IllegalModInfoException
		{
			ModPackageInfo info=createFromInputStream ( in );
			info.setModPreview ( Utils.readAllbytes ( picStream ) );
			return info;
		}

		public static ModPackageInfo createFromInputstreamWIthExternalPic ( InputStream in, @NonNull Bitmap pic ) throws IOException, ModPackageInfo.IllegalModInfoException
		{
			ModPackageInfo info=createFromInputStream ( in );
			info.setModPreview ( pic );
			return info;
		}

	}





	public static class IllegalModInfoException extends Exception
	{
		public IllegalModInfoException ( String info )
		{
			super ( info );
		}
	}
	public static final class Versions
	{
		public static final int VER_0=0;
		//ver0 -> ver1 enable soundeffect support
		public static final int VER_1=1;
		//ver1 -> ver2 abandon old soudeffect interface and use a new one.
		public static final int VER_2=2;
		//ver2 -> ver3 add support for replacing Japanese Captain Voice.
		public static final int VER_3=3;
		//ver3 -> ver4 support for external mod preview
		public static final int VER_4=4;
        //ver5: support for custom ship name
        public static final int VER_5 = 5;
	}
}
