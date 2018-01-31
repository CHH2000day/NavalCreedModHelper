package com.CHH2000day.navalcreed.modhelper;
import android.support.v4.app.*;
import android.os.*;
import android.view.*;
import java.io.*;
import android.net.*;
import android.app.Activity;
import android.content.*;
import com.CHH2000day.navalcreed.modhelper.ModPackageInfo.*;
import org.json.*;
import android.widget.*;
import android.support.v7.app.*;
import android.view.View.*;
import android.support.design.widget.*;

public class ModPackageInstallerFragment extends Fragment
{

	private UriLoader loader;
	private View v;
	private ImageView preview;
	private TextView info;
	private Button update,select,cancel;
	private ModPackageInstallHelper mpih;

	private static final int QUERY_CODE=6;
	@Override
	public void onCreate ( Bundle savedInstanceState )
	{
		// TODO: Implement this method
		super.onCreate ( savedInstanceState );
	}

	@Override
	public View onCreateView ( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{
		
		// TODO: Implement this method
		v = inflater.inflate ( R.layout.modinfopage, null );
		preview = (ImageView)v.findViewById ( R.id.modinfopageImageView );
		info = (TextView)v.findViewById ( R.id.modinfopageTextView );
		select = (Button)v.findViewById ( R.id.modinfopageButtonSelect );
		update = (Button)v.findViewById ( R.id.modinfopageButtonUpdate );
		cancel = (Button)v.findViewById ( R.id.modinfopageButtonCancel );
		return v;
	}

	@Override
	public void onResume ( )
	{
		// TODO: Implement this method
		super.onResume ( );
		if ( loader != null && loader.getUri ( ) != null )
		{
			selectFile ( loader.getUri ( ) );
			//注销接口防止被重复使用
			loader = null;
		}
		
	}

	
	@Override
	public void onActivityCreated ( Bundle savedInstanceState )
	{
		super.onActivityCreated(savedInstanceState);
		// TODO: Implement this method
		select.setOnClickListener ( new OnClickListener ( ){

				@Override
				public void onClick ( View p1 )
				{Intent intent=new Intent ( Intent.ACTION_GET_CONTENT );
					intent.setType ( "*/*" );
					intent.addCategory ( intent.CATEGORY_OPENABLE );
					startActivityForResult ( intent.createChooser ( intent, "请选择mod包文件" ), QUERY_CODE );

					// TODO: Implement this method
				}
			} );
		cancel.setOnClickListener ( new OnClickListener ( ){

				@Override
				public void onClick ( View p1 )
				{
					clear ( );
					// TODO: Implement this method
				}
			} );
		update.setOnClickListener ( new OnClickListener ( ){

				@Override
				public void onClick ( View p1 )
				{
					if ( mpih == null )
					{
						Snackbar.make ( v, R.string.modpkg_info_empty, Snackbar.LENGTH_LONG ).show ( );
					}
					mpih.beginInstall ( );
					// TODO: Implement this method
				}
			} );
	}

	@Override
	public void onDestroyView ( )
	{
		// TODO: Implement this method
		super.onDestroyView ( );
		if ( mpih != null )
		{
			mpih.recycle ( );
			mpih = null;
		}
	}


	@Override
	public void onAttach ( Activity activity )
	{

		// TODO: Implement this method
		super.onAttach ( activity );
		if ( activity != null )
		{
			loader = (UriLoader)activity;
		}
	}


	private void clear ( )
	{
		if ( mpih != null )
		{
			mpih.recycle ( );
		}
		preview.setImageResource ( R.drawable.no_pereview_tiny );
		info.setText ( R.string.modpkg_info_empty );
		mpih = null;
	}
	public void selectFile ( Uri uri )
	{
		AlertDialog.Builder adb=new AlertDialog.Builder ( getActivity ( ) );
		adb.setTitle ( "请稍等" )
			.setMessage ( "正在读取文件...." )
			.setCancelable ( false );
		AlertDialog ad=adb.create ( );
		ad.setCanceledOnTouchOutside ( false );
		ad.show ( );
		String filepath=Utils.resolveFilePath ( uri,getActivity() );
		try
		{
			mpih = new ModPackageInstallHelper ( new File ( filepath ), (Main)getActivity ( )  );
			ModPackageInfo mpi=mpih.getModPackageInfo ( );
			info.setText ( new StringBuilder ( ).append ( "mod名:" )
						  .append ( mpi.getModName ( ) )
						  .append ( "\n" )
						  .append ( "mod类型:" )
						  .append ( resolveModType ( mpi.getModType ( ) ) )
						  .append ( "\n" )
						  .append ( "mod作者:" )
						  .append ( mpi.getModAuthor ( ) )
						  .append ( "\n" )
						  .append ( "简介:" )
						  .append ( mpi.getModInfo ( ) )
						  .toString ( ) );
			if ( mpi.hasPreview ( ) )
			{
				preview.setImageBitmap ( mpi.getModPreview ( ) );
			}
			ad.dismiss ( );
		}
		catch (IOException e)
		{
			ad.setCancelable ( true );
			ad.setCanceledOnTouchOutside ( false );
			ad.setMessage ( e.getLocalizedMessage ( ) );}
		catch (ModPackageInfo.IllegalModInfoException e)
		{
			ad.setCancelable ( true );
			ad.setCanceledOnTouchOutside ( false );
			ad.setMessage ( "Mod包描述文件出错\n" + e.getLocalizedMessage ( ) );
		}

	}
	private String resolveModType ( String modtype )
	{
		String s="";
		if ( ModPackageInfo.MODTYPE_BACKGROUND.equals ( modtype ) )
		{
			s = "背景图片";
		}
		else if ( ModPackageInfo.MODTYPE_BGM.equals ( modtype ) )
		{
			s = "背景音乐";
		}
		else if ( ModPackageInfo.MODTYPE_CREWPIC.equals ( modtype ) )
		{
			s = "船员头像";
		}
		else if ( ModPackageInfo.MODTYPE_CV.equals ( modtype ) )
		{
			s = "舰长语音";
		}
		else
		{
			s = "未知";
		}
		return s;
	}
	public static interface UriLoader
	{
		public Uri getUri ( );
	}

	@Override
	public void onActivityResult ( int requestCode, int resultCode, Intent data )
	{
		// TODO: Implement this method
		super.onActivityResult ( requestCode, resultCode, data );
		if ( QUERY_CODE == requestCode && AppCompatActivity.RESULT_OK == resultCode && data != null )
		{
			selectFile ( data.getData ( ) );
		}
	}

}
