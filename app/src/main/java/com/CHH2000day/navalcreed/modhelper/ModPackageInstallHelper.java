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

public class ModPackageInstallHelper
{
	//常量声明
	private static final String FILE_MODINFO="mod.info";
	private static final String PRIMARYPATH_CV=File.separatorChar + "sound" + File.separatorChar + "Voice";
	private static final String SUBPATH_CV_EN=File.separatorChar + "EnglishUsual";
	private static final String SUBPATH_CV_CN=File.separatorChar + "ChineseUsual";

	private static final int SUBTYPE_NULL=0;
	private static final int SUBTYPE_CV_EN=1200;
	private static final int SUBTYPE_CV_CN=1201;

	private static final String[] CV_COUNTRY={"英语","中文"};


	private int msubtype=SUBTYPE_NULL;
	private ModHelperApplication mmha;
	private AppCompatActivity mactivty;
	private File msrcFile;
	private ZipFile mpkgFile;
	private ModPackageInfo mmpi;

	public ModPackageInstallHelper (File pkgFile, AppCompatActivity activity) throws IOException, ModPackageInfo.IllegalModInfoException, JSONException
	{
		msrcFile = pkgFile;
		mactivty = activity;
		mmha = (ModHelperApplication)mactivty.getApplication ( );
		init ( );
	}

	private void init () throws IOException, ModPackageInfo.IllegalModInfoException, JSONException
	{
		//创建mod文件实例
		fetch ( );
		//识别Mod文件并读取信息
		identify ( );
	}

	private void fetch () throws IOException
	{
		mpkgFile = new ZipFile ( msrcFile );
	}
	private void identify () throws IOException, ModPackageInfo.IllegalModInfoException, JSONException
	{
		ZipEntry mInfoFile=mpkgFile.getEntry ( FILE_MODINFO );
		InputStream zi=mpkgFile.getInputStream ( mInfoFile );
		mmpi = ModPackageInfo.Factory.createFromInputStream ( zi );
	}
	public void beginInstall ()
	{
		//检查mod包类型
		//如果mod包类型为语音包，确认安装位置
		if (mmpi.getModType ( ).equals ( mmpi.MODTYPE_CV ))
		{

			AlertDialog.Builder adb=new AlertDialog.Builder ( mactivty );
			adb.setTitle ( "请选择要替换的舰长语音" )
				.setSingleChoiceItems ( CV_COUNTRY, 0, new DialogInterface.OnClickListener ( ){

					@Override
					public void onClick (DialogInterface p1, int p2)
					{
						msubtype = p2;
						// TODO: Implement this method
					}
				} )
				.setNegativeButton ( "取消", null )
				.setPositiveButton ( "确定", new DialogInterface.OnClickListener ( ){

					@Override
					public void onClick (DialogInterface p1, int p2) 
					{
						install ( );
						// TODO: Implement this method
					}
				} );
			adb.create ( ).show ( );

		}
		else
		{
			install ( );
		}



	}
	/*文件有效性改为由作者验证，此处不再验证
	 public boolean checkCVpackageValidity ()
	 {
	 return false;
	 }*/
	private void install ()
	{

		InstallTask it=new InstallTask ( mmpi.getModType ( ), msubtype );
		it.execute ( );


	}
	public ModPackageInfo getModPackageInfo ()
	{

		return mmpi;
	}

	private String getPath (String modeType, int subType)
	{
		String pth=mmha.getResFilesDirPath ( );

		if (ModPackageInfo.MODTYPE_CV.equals ( modeType ))
		{
			pth = pth + PRIMARYPATH_CV;
			if (subType == SUBTYPE_CV_CN)
			{
				pth = pth + SUBPATH_CV_CN;
			}
			else if (subType == SUBTYPE_CV_EN)
			{
				pth = pth + SUBPATH_CV_EN;
			}
		}
		return pth;
	}
	private class InstallTask extends AsyncTask<Void,Integer,Boolean>
	{

		private Exception e;
		private String mainPath;
		private AlertDialog ad;
		private int counts;
		protected InstallTask (String modType, int subType)
		{
			mainPath = getPath ( modType, subType );
		}
		@Override
		protected Boolean doInBackground (Void[] p1)
		{
			try
			{
				ZipEntry ze;
				byte[]cache=new byte[2048];
				int len;
				File targetFile;
				ZipInputStream zis=new ZipInputStream ( new FileInputStream ( msrcFile ) );

				while ((ze = zis.getNextEntry ( )) != null)
				{
					//不解压mod描述文件
					if (ze.getName ( ).equals ( FILE_MODINFO ))
					{
						continue;
					}
					//判断获取到的Entry是否为目录
					if (ze.isDirectory ( ))
					{
						//若是，创建目录结构
						targetFile = new File ( mainPath, ze.getName ( ) );
						if (!targetFile.getParentFile ( ).exists ( ))
						{
							targetFile.getParentFile ( ).mkdirs ( );
						}
						if (targetFile.isFile ( ))
						{
							targetFile.delete ( );
						}
						targetFile.mkdirs ( );
						counts++;
						publishProgress(counts);
					}
					//非目录则为文件
					else
					{
						//写出文件
						targetFile = new File ( mainPath, ze.getName ( ) );
						if (!targetFile.getParentFile ( ).exists ( ))
						{
							targetFile.getParentFile ( ).mkdirs ( );
						}
						//若写出的目标文件已为目录，删除
						if (targetFile.isDirectory ( ))
						{
							Utils.delDir ( targetFile );
						}
						//输出文件，使用Okio
						Sink s=Okio.sink ( targetFile );
						BufferedSink bs=Okio.buffer ( s );
						while ((len = zis.read ( cache )) != -1)
						{
							bs.write ( cache, 0, len );
						}
						bs.flush ( );
						bs.close ( );
						counts++;
						publishProgress(counts);
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
		protected void onPreExecute ()
		{
			// TODO: Implement this method
			super.onPreExecute ( );


		}

		@Override
		protected void onPostExecute (Boolean result)
		{
			// TODO: Implement this method
			super.onPostExecute ( result );
		}

		@Override
		protected void onProgressUpdate (Integer[] values)
		{
			// TODO: Implement this method
			super.onProgressUpdate ( values );
		}
		private class DialogMonitor implements DialogInterface.OnShowListener
		{

			public DialogMonitor ()
			{

			}
			public void ondone ()
			{

			}
			@Override
			public void onShow (DialogInterface p1)
			{
				// TODO: Implement this method
			}


		}




	}
}
