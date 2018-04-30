package com.CHH2000day.navalcreed.modhelper;
import java.io.*;
import java.util.zip.*;
import com.CHH2000day.navalcreed.modhelper.ModPackageInfo.*;
import org.json.*;
import android.content.*;
import java.util.*;
import android.support.v7.app.*;
import android.os.*;
import okio.*;
import android.view.*;
import android.widget.*;
import android.graphics.*;
import android.view.View.*;

public class ModPackageInstallHelper
{
	//常量声明
	private static final String FILE_MODINFO="mod.info";
	private static final String PRIMARYPATH_CV=File.separatorChar + "sound" + File.separatorChar + "Voice";
	private static final String PRIMARYPATH_BGM=File.separatorChar + "sound" + File.separatorChar + "Music";
	private static final String PRIMARYPATH_SOUNDEFFECT=File.separatorChar + "sound" + File.separatorChar + "soundeffect" + File.separatorChar + "ginsir";
	private static final String PRIMARYPATH_SOUNDEFFECT_PRIM=File.separatorChar + "sound" + File.separatorChar + "soundeffect";
	private static final String PRIMARYPATH_SOUNDEFFECT_SEC=File.separatorChar + "sound" + File.separatorChar + "newsound";
	private static final String PRIMARYPATH_BACKGROUND=File.separatorChar + "pic";
	private static final String PRIMARYPATH_CREWHEAD=File.separatorChar + "pic" + File.separatorChar + "crewhead";
	private static final String PRIMARYTYPE_OTHER="";
	private static final String SUBPATH_CV_EN=File.separatorChar + "EnglishUsual";
	private static final String SUBPATH_CV_CN=File.separatorChar + "ChineseUsual";
	private static final String SUBPATH_CV_JP=File.separatorChar + "Japanesemoe";
	private static final String SUBPATH_CV_JP_CV=SUBPATH_CV_JP + File.separatorChar + "AirCarrier";
	private static final String SUBPATH_CV_JP_BB=SUBPATH_CV_JP + File.separatorChar + "Battleship";
	private static final String SUBPATH_CV_JP_CA=SUBPATH_CV_JP + File.separatorChar + "Crusier";
	private static final String SUBPATH_CV_JP_DD=SUBPATH_CV_JP + File.separatorChar + "Destroyer";

	public static final int SUBTYPE_NULL=0;
	public static final int SUBTYPE_CV_EN=1200;
	public static final int SUBTYPE_CV_CN=1201;
	public static final int SUBTYPE_CV_JP_CV=1202;
	public static final int SUBTYPE_CV_JP_BB=1203;
	public static final int SUBTYPE_CV_JP_CA=1204;
	public static final int SUBTYPE_CV_JP_DD=1205;
	private static final int SUBTYPE_CV_OFFSET=SUBTYPE_CV_EN;

	//private static String[] CV_COUNTRY={};


	private int msubtype=SUBTYPE_NULL;
	private ModHelperApplication mmha;
	private AppCompatActivity mactivty;
	private File msrcFile;
	private ZipFile mpkgFile;
	private ModPackageInfo mmpi;

	/*public static void init ( Context ctx )
	 {
	 //CV_COUNTRY = ctx.getResources ( ).getStringArray ( R.array.cv_types );
	 }*/
	public ModPackageInstallHelper ( File pkgFile, AppCompatActivity activity ) throws IOException, ModPackageInfo.IllegalModInfoException
	{
		msrcFile = pkgFile;
		mactivty = activity;
		mmha = (ModHelperApplication)mactivty.getApplication ( );
		load ( );
	}

	private void load ( ) throws IOException, ModPackageInfo.IllegalModInfoException
	{
		//创建mod文件实例
		fetch ( );
		//识别Mod文件并读取信息
		try
		{
			identify ( );
		}
		catch (JSONException e)
		{
			throw new IllegalModInfoException ( "Failed to resolve mod manifest\n" + e.getLocalizedMessage ( ) );
		}
	}

	public void recycle ( )
	{
		try
		{
			mpkgFile.close ( );

		}
		catch (IOException e)
		{}
	}
	private void fetch ( ) throws IOException
	{
		mpkgFile = new ZipFile ( msrcFile );
	}
	private void identify ( ) throws IOException, ModPackageInfo.IllegalModInfoException, JSONException
	{
		ZipEntry mInfoFile=mpkgFile.getEntry ( FILE_MODINFO );
		if ( mInfoFile == null )
		{
			throw new IllegalModInfoException ( "Could not found mod.info from package" );
		}
		InputStream zi=mpkgFile.getInputStream ( mInfoFile );
		mmpi = ModPackageInfo.Factory.createFromInputStream ( zi );
	}
	public void beginInstall ( )
	{
		checkVersion ( );


	}
	/*文件有效性改为由作者验证，此处不再验证
	 public boolean checkCVpackageValidity ()
	 {
	 return false;
	 }*/
	private void checkVersion ( )
	{
		//检查是否能实现mod包的所有功能
		if ( !mmpi.hasAllFeature ( ) )
		{
			AlertDialog.Builder adb=new AlertDialog.Builder ( mactivty );
			adb.setTitle ( R.string.notice )
				.setMessage ( R.string.modpkg_ver_warning )
				.setNegativeButton ( R.string.cancel, null )
				.setPositiveButton ( R.string.cont, new DialogInterface.OnClickListener ( ){

					@Override
					public void onClick ( DialogInterface p1, int p2 )
					{
						checkModType ( );
						// TODO: Implement this method
					}
				} );
			adb.create ( ).show ( );
		}
		else
		{
			checkModType ( );
		}
	}
	private void checkModType ( )
	{
		//检查mod包类型
		//如果mod包类型为语音包，确认安装位置
		if ( mmpi.getModType ( ).equals ( mmpi.MODTYPE_CV ) )
		{

			msubtype = SUBTYPE_CV_OFFSET;
			AlertDialog.Builder adb=new AlertDialog.Builder ( mactivty );
			adb.setTitle ( R.string.modpkg_cv_to_replace )
				.setSingleChoiceItems ( R.array.cv_types, 0, new DialogInterface.OnClickListener ( ){

					@Override
					public void onClick ( DialogInterface p1, int p2 )
					{
						msubtype = p2 + SUBTYPE_CV_OFFSET;
						// TODO: Implement this method
					}
				} )
				.setNegativeButton ( R.string.cancel, null )
				.setPositiveButton ( R.string.ok, new DialogInterface.OnClickListener ( ){

					@Override
					public void onClick ( DialogInterface p1, int p2 ) 
					{
						checkInstall ( );
						// TODO: Implement this method
					}
				} );
			adb.create ( ).show ( );

		}
		else
		{
			checkInstall ( );
		}


	}

	private void checkInstall ( )
	{
		ModPackageManager mpm=ModPackageManager.getInstance ( );
		if ( mpm.checkInstalled ( mmpi.getModType ( ), getSubType ( ) ) )
		{
			AlertDialog.Builder adb=new AlertDialog.Builder ( mactivty );
			adb.setTitle ( R.string.error )
				.setMessage ( R.string.modpkg_already_installed_warning );
			adb.create ( ).show ( );

		}
		else if ( mmpi.isAbandoned ( ) )
		{
			if ( ModPackageManager.getInstance ( ).isOverride ( ) )
			{
				install ( );
			}
			else
			{
				AlertDialog.Builder adb=new AlertDialog.Builder ( mactivty );
				adb.setTitle ( R.string.error )
					.setMessage ( R.string.modpkg_interface_warning );
				adb.create ( ).show ( );
			}
		}
		else
		{
			install ( );
		}

	}

	private void install ( )
	{

		InstallTask it=new InstallTask ( mmpi.getModType ( ), msubtype );
		it.execute ( );


	}
	public ModPackageInfo getModPackageInfo ( )
	{

		return mmpi;
	}
	private String getSubType ( )
	{
		String s=ModPackageInfo.SUBTYPE_EMPTY;
		if ( SUBTYPE_CV_EN == msubtype )
		{
			s = ModPackageInfo.SUB_MODTYPE_CV_EN;
		}
		else if ( SUBTYPE_CV_CN == msubtype )
		{
			s = ModPackageInfo.SUB_MODTYPE_CV_CN;
		}
		else if ( SUBTYPE_CV_JP_CV == msubtype )
		{
			s = ModPackageInfo.SUB_MODTYPE_CV_JP_CV;
		}
		else if ( SUBTYPE_CV_JP_BB == msubtype )
		{
			s = ModPackageInfo.SUB_MODTYPE_CV_JP_BB;
		}
		else if ( SUBTYPE_CV_JP_CA == msubtype )
		{
			s = ModPackageInfo.SUB_MODTYPE_CV_JP_CA;
		}
		else if ( SUBTYPE_CV_JP_DD == msubtype )
		{
			s = ModPackageInfo.SUB_MODTYPE_CV_JP_DD;
		}
		return s;
	}

	public static String getPath ( String modType, int subType, ModHelperApplication app )
	{
		String pth=app.getResFilesDirPath ( );

		if ( ModPackageInfo.MODTYPE_CV.equals ( modType ) )
		{
			pth = pth + PRIMARYPATH_CV;
			if ( subType == SUBTYPE_CV_CN )
			{
				pth = pth + SUBPATH_CV_CN;
			}
			else if ( subType == SUBTYPE_CV_EN )
			{
				pth = pth + SUBPATH_CV_EN;
			}
			else if ( subType == SUBTYPE_CV_JP_CV )
			{
				pth = pth + SUBPATH_CV_JP_CV;
			}
			else if ( subType == SUBTYPE_CV_JP_BB )
			{
				pth = pth + SUBPATH_CV_JP_BB;
			}
			else if ( subType == SUBTYPE_CV_JP_CA )
			{
				pth = pth + SUBPATH_CV_JP_CA;
			}
			else if ( subType == SUBTYPE_CV_JP_DD )
			{
				pth = pth + SUBPATH_CV_JP_DD;
			}
		}
		if ( ModPackageInfo.MODTYPE_BACKGROUND.equals ( modType ) )
		{
			pth = pth + PRIMARYPATH_BACKGROUND;
		}
		if ( ModPackageInfo.MODTYPE_BGM.equals ( modType ) )
		{
			pth = pth + PRIMARYPATH_BGM;
		}
		if ( ModPackageInfo.MODTYPE_CREWPIC.equals ( modType ) )
		{
			pth = pth + PRIMARYPATH_CREWHEAD;
		}
		if ( ModPackageInfo.MODTYPE_SOUNDEFFECT.equals ( modType ) )
		{
			pth = pth + PRIMARYPATH_SOUNDEFFECT;
		}
		if ( ModPackageInfo.MODTYPE_SOUNDEFFECT_SEC.equals ( modType ) )
		{
			pth = pth + PRIMARYPATH_SOUNDEFFECT_SEC;
		}
		if ( ModPackageInfo.MODTYPE_SOUNDEFFECT_PRIM.equals ( modType ) )
		{
			pth = pth + PRIMARYPATH_SOUNDEFFECT_PRIM;
		}
		if ( ModPackageInfo.MODTYPE_OTHER.equals ( modType ) )
		{
			pth = pth + PRIMARYTYPE_OTHER;
		}
		return pth;
	}
	private class InstallTask extends AsyncTask<Void,Integer,Boolean>
	{

		private Exception e;
		private String mainPath;
		private AlertDialog ad;
		private int count;
		private int totalcount;
		private View dialogView;
		private TextView stat;
		private ProgressBar progressbar;
		private DialogMonitor dm;
		protected InstallTask ( String modType, int subType )
		{
			mainPath = getPath ( modType, subType, (ModHelperApplication)mactivty.getApplication ( ) );
		}
		@Override
		protected Boolean doInBackground ( Void[] p1 )
		{
			if ( ModPackageInfo.Versions.VER_0 == mmpi.getModTargetVer ( ) || ModPackageInfo.Versions.VER_1 == mmpi.getModTargetVer ( ) )
			{
				return installModVer0 ( );}
			else 
			{
				return installModVer0 ( );
			}


		}
		private boolean installModVer0 ( )
		{
			try
			{
				ZipEntry ze;
				byte[]cache=new byte[2048];
				int len;
				File targetFile;
				ZipInputStream zis=new ZipInputStream ( new FileInputStream ( msrcFile ) );

				while ( ( ze = zis.getNextEntry ( ) ) != null )
				{
					//不解压mod描述文件
					if ( ze.getName ( ).equals ( FILE_MODINFO ) )
					{
						continue;
					}
					//判断获取到的Entry是否为目录
					if ( ze.isDirectory ( ) )
					{
						//若是，创建目录结构
						targetFile = new File ( mainPath, ze.getName ( ) );
						if ( !targetFile.getParentFile ( ).exists ( ) )
						{
							targetFile.getParentFile ( ).mkdirs ( );
						}
						if ( targetFile.isFile ( ) )
						{
							targetFile.delete ( );
						}
						targetFile.mkdirs ( );
						count++;
						publishProgress ( count );
					}
					//非目录则为文件
					else
					{
						//写出文件
						targetFile = new File ( mainPath, ze.getName ( ) );
						if ( !targetFile.getParentFile ( ).exists ( ) )
						{
							targetFile.getParentFile ( ).mkdirs ( );
						}
						//若写出的目标文件已为目录，删除
						if ( targetFile.isDirectory ( ) )
						{
							Utils.delDir ( targetFile );
						}
						//输出文件，使用Okio
						Sink s=Okio.sink ( targetFile );
						BufferedSink bs=Okio.buffer ( s );
						while ( ( len = zis.read ( cache ) ) != -1 )
						{
							bs.write ( cache, 0, len );
						}
						bs.flush ( );
						bs.close ( );
						count++;
						publishProgress ( count );
					}
					zis.closeEntry ( );

				}
				zis.close ( );
			}
			catch (Exception e)
			{
				e.printStackTrace ( );
				this.e = e;
				return false;
			}
			// TODO: Implement this method
			return true;
		}


		@Override
		protected void onPreExecute ( )
		{
			dialogView = mactivty.getLayoutInflater ( ).inflate ( R.layout.dialog_installmodpkg, null );
			stat = (TextView)dialogView.findViewById ( R.id.dialoginstallmodpkgStatus );
			progressbar = (ProgressBar)dialogView.findViewById ( R.id.dialoginstallmodpkgProgress );
			// TODO: Implement this method
			AlertDialog.Builder adb=new AlertDialog.Builder ( mactivty );
			adb.setTitle ( R.string.please_wait )
				.setView ( dialogView )
				.setPositiveButton ( R.string.close, null )
				.setCancelable ( false );

			ad = adb.create ( );
			ad.setCanceledOnTouchOutside ( false );
			dm = new DialogMonitor ( ad );
			ad.setOnShowListener ( dm );
			ad.show ( );

		}

		@Override
		protected void onPostExecute ( Boolean result )
		{
			progressbar.setProgress ( progressbar.getMax ( ) );
			dm.ondone ( );
			if ( result )
			{
				stat.setText ( R.string.success );

				ModPackageManager.getInstance ( ).postInstall ( getModPackageInfo ( ).getModType ( ), getSubType ( ), mmpi.getModName ( ) );
			}
			else
			{
				ad.setTitle(R.string.error);
				String s=new StringBuilder ( ).append ( mactivty.getText(R.string.failed))
					.append(":")
					.append ( "\n" )
					.append ( e.getMessage ( ) ).toString ( );
				stat.setText ( s );
			}

			// TODO: Implement this method
			super.onPostExecute ( result );
		}

		@Override
		protected void onProgressUpdate ( Integer[] values )
		{
			super.onProgressUpdate ( values );
			if ( totalcount == 0 )
			{
				totalcount = mpkgFile.size ( );
				progressbar.setMax ( totalcount );
				progressbar.setIndeterminate ( false );
				progressbar.setProgress ( 0 );
				stat.setText ( R.string.installing );
			}
			progressbar.setProgress ( values [ 0 ] );

			// TODO: Implement this method

		}
		private class DialogMonitor implements DialogInterface.OnShowListener
		{
			private AlertDialog alertdialog;
			private Button button;
			private int color;
			public DialogMonitor ( AlertDialog ad )
			{
				alertdialog = ad;
			}
			public void ondone ( )
			{
				button.setTextColor ( color );
				button.setClickable ( true );
			}
			@Override
			public void onShow ( DialogInterface p1 )
			{	button = alertdialog.getButton ( ad.BUTTON_POSITIVE );
				button.setOnClickListener ( new OnClickListener ( ){

						@Override
						public void onClick ( View p1 )
						{
							ad.dismiss ( );
							// TODO: Implement this method
						}
					} );
				color = button.getCurrentTextColor ( );
				button.setClickable ( false );
				button.setTextColor ( Color.GRAY );

				// TODO: Implement this method

			}


		}




	}
}
