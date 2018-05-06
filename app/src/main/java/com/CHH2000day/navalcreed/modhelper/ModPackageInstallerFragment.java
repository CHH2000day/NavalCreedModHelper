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
import android.text.method.*;
import android.provider.*;
import okio.*;

public class ModPackageInstallerFragment extends Fragment
{

	private UriLoader loader;
	private View v;
	private ImageView preview;
	private TextView info;
	private Button update,select,cancel;
	private ModPackageInstallHelper mpih;
	private boolean isCache;

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
		info.setMovementMethod ( new ScrollingMovementMethod ( ) );
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
		super.onActivityCreated ( savedInstanceState );
		// TODO: Implement this method
		select.setOnClickListener ( new OnClickListener ( ){

				@Override
				public void onClick ( View p1 )
				{Intent intent=new Intent ( Intent.ACTION_GET_CONTENT );
					intent.setType ( "*/*" );
					intent.addCategory ( intent.CATEGORY_OPENABLE );
					startActivityForResult ( intent.createChooser ( intent, getString ( R.string.select_file ) ), QUERY_CODE );

					// TODO: Implement this method
				}
			} );
		select.setOnLongClickListener ( new OnLongClickListener ( ){

				@Override
				public boolean onLongClick ( View p1 )
				{
					// TODO: Implement this method
					final String pkg="com.android.documentsui";
					Uri packageURI=Uri.parse ( "package:" + pkg );
					Intent intent =  new Intent ( Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI );  
					startActivity ( intent );
					return true;
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
						return;
					}
					mpih.beginInstall ( (Main)getActivity ( ) );
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
		if ( isCache )
		{
			Utils.delDir ( mpih.getSourceFile ( ) );
			isCache=false;
		}

		preview.setImageResource ( R.drawable.no_preview );
		info.setText ( R.string.modpkg_info_empty );
		mpih = null;
	}
	public void selectFile ( final Uri uri )
	{
		clear ( );
		String filepath=Utils.resolveFilePath ( uri, getActivity ( ) );
		if ( filepath == null )
		{
			AlertDialog.Builder adb=new AlertDialog.Builder ( getActivity ( ) );
			adb.setTitle ( R.string.please_wait )
				.setCancelable ( false )
				.setMessage ( new StringBuilder ( ).append ( getString ( R.string.failed_to_resolve_pth ) )
							 .append ( "\n" )
							 /*.append ( "请将此界面截屏并发给开发者" )
							  .append ( "\n" )*/
							 .append ( "authority:" )
							 .append ( uri.getAuthority ( ) )
							 .append ( "\n" )
							 .append ( "path:" )
							 .append ( uri.getPath ( ) )
							 .toString ( ) )
				.setNegativeButton ( R.string.cancel, null )
				.setPositiveButton ( R.string.altn_install, new DialogInterface.OnClickListener ( ){

					@Override
					public void onClick ( DialogInterface p1, int p2 )
					{
						final Handler h=new Handler ( ){
							public void handleMessage ( Message msg )
							{
								if ( msg.what == 0 )
								{
									isCache=true;
									load ( (File)msg.obj  );
								}
								else
								{
									AlertDialog.Builder adb=new AlertDialog.Builder ( getActivity ( ) );
									adb.setTitle ( R.string.error )
										.setMessage ( Utils.getErrMsg ( (Throwable)msg.obj ) )
										.setPositiveButton ( R.string.ok, null )
										.create ( ).show ( );
								}
							}
						};
						new Thread ( ){

							@Override
							public void run ( )
							{
								File f=new File ( getActivity ( ).getExternalCacheDir ( ), "cachedmodfile.ncmod" );
								InputStream is=null;
								Sink s=null;
								BufferedSink bs=null;
								try
								{
									if ( !f.getParentFile ( ).exists ( ) )
									{
										f.getParentFile ( ).mkdirs ( );
									}
									if ( f.isDirectory ( ) )
									{
										Utils.delDir ( f );
									}
									is = getActivity ( ).getContentResolver ( ).openInputStream ( uri );
									s = Okio.sink ( f );
									bs = Okio.buffer ( s );

									byte[]cache=new byte[1024 * 16];
									int len;
									while ( ( len = is.read ( cache ) ) != -1 )
									{
										bs.write ( cache, 0, len );
									}
									bs.flush ( );
									h.sendMessage ( h.obtainMessage ( 0, f ) );

								}
								catch (Throwable t)
								{
									h.sendMessage ( h.obtainMessage ( -1, t ) );
								}
								finally
								{
									if ( bs != null )
									{
										try
										{
											bs.close ( );
										}
										catch (IOException e)
										{}
									}
									if ( s != null )
									{
										try
										{
											s.close ( );
										}
										catch (IOException e)
										{}
									}
									if ( is != null )
									{
										try
										{
											is.close ( );
										}
										catch (IOException e)
										{}
									}
								}

								// TODO: Implement this method
							}
						}.start ( );
						// TODO: Implement this method
					}
				} );
			adb.create ( ).show ( );
			return;
		}
		else
		{
			isCache=false;
			load ( new File ( filepath ) );
		}

	}



	private void load ( final File source)
	{
		AlertDialog.Builder adb=new AlertDialog.Builder ( getActivity ( ) );
		adb.setTitle ( R.string.please_wait )
			.setMessage ( R.string.please_wait )
			.setCancelable ( false );
		final AlertDialog ad=adb.create ( );
		ad.setCanceledOnTouchOutside ( false );
		ad.show ( );


		mpih = new ModPackageInstallHelper ( source );
		final AppCompatActivity act=(Main)getActivity ( );
		mpih.load ( new ModPackageInstallHelper.onModPackageLoadDoneListener ( ){

				@Override
				public void onSuccess ( )
				{	ModPackageInfo mpi=mpih.getModPackageInfo ( );
					long modsize=mpih.getTotalSize ( );
					StringBuilder sb=new StringBuilder ( );
					sb.append ( getString ( R.string.modname ) )
						.append ( mpi.getModName ( ) )
						.append ( "\n" )
						.append ( getString ( R.string.modsize ) )
						.append ( Utils.convertFileSize ( modsize ) )
						.append ( "\n" )
						.append ( getString ( R.string.modtype ) )
						.append ( ModPackageManager.getInstance ( ). resolveModType ( mpi.getModType ( ) ) )
						.append ( "\n" )
						.append ( getText ( R.string.modauthor ) )
						.append ( mpi.getModAuthor ( ) )
						.append ( "\n" )
						.append ( getString ( R.string.modinfo ) )
						.append ( mpi.getModInfo ( ) );
					if ( mpi.getModType ( ).equals ( ModPackageInfo.MODTYPE_OTHER ) )
					{
						sb.append ( "\n" )
							.append ( getString ( R.string.ununinstallable_modpkg_warning ) );
					}
					info.setText ( sb.toString ( ) );
					if ( mpi.hasPreview ( ) )
					{
						preview.setImageBitmap ( mpi.getModPreview ( ) );
					}
					ad.dismiss ( );
					// TODO: Implement this method
				}

				@Override
				public void onFail ( Throwable t )
				{
					ad.setCancelable ( true );
					ad.setCanceledOnTouchOutside ( true );
					if ( t instanceof ModPackageInfo.IllegalModInfoException )
					{
						ad.setMessage ( getString ( -R.string.invalid_mod_info ) + "\n" + t.getLocalizedMessage ( ) );
					}
					else
					{
						ad.setMessage ( Utils.getErrMsg ( t ) );
					}
					if ( isCache )
					{
						Utils.delDir ( source );
					}
					// TODO: Implement this method
				}

				@Override
				public AppCompatActivity getActivity ( )
				{
					// TODO: Implement this method
					return act;
				}
			} );

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
