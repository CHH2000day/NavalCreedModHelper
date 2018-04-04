package com.CHH2000day.navalcreed.modhelper;
import android.view.*;
import android.widget.*;
import android.view.View.*;
import android.net.*;
import android.content.*;
import android.support.v7.app.*;
import android.support.design.widget.*;
import android.os.*;
import android.widget.AdapterView.*;
import java.io.*;
import java.util.*;
import android.content.res.*;

public class BGMReplacerFragmentSDK19B extends BGMReplacerFragment
{
	private static final int MUSICCOUNT_HARBOR=6;
	private static final int MUSICCOUNT_LOADING=3;
	private static final int MUSICCOUNT_BATTLESTART=4;
	private static final int MUSICCOUNT_BATTLEHEAT=7;
	private static final int MUSICCOUNT_BATTLEEND=3;
	private static final int MUSICCOUNT_BATTLEVICTORY=4;
	private static final int MUSICCOUNT_BATTLEFAIL=2;
	private static final String[] SCENE={"Harbor","Loading","BattleStart","BattleHeat","BattleEnd","Victory","Fial"/*因为你游程序员把Fail打成Fial了，所以将错就错了*/};
	//private static final String[] SCENE_TOSHOW={"港口","加载音乐","战斗开始","战斗激战","战斗即将结束","战斗胜利","战斗失败"};
	private static String[] scene_toshow;
	private static final String[] FILENAMES_UNIVERSAL={"1","2","3","4","5","6","7"};
	//private static final String[] FILENAMES_UNSELECTED={"请选择情景"};
	private static String[] filenames_unselected;
	private static final String[] FILENAMES_BATTLEFAIL={"Danger","Fail"};
	//private static final String[] FILENAMES_BATTLEFAIL_TOSHOW={"即将失败","失败"};
	private static String[] filenames_battlefail_toshow;
	private static final String[] FILENAMES_LOADING={"Loading","Login","Queuing"};
	//private static final String[] FILENAMES_LOADING_TOSHOW={"加载中","登录","匹配中"};
	private static String[] filenames_loading_toshow;
	private static final int TEXTVIEW_RES_ID=R.layout.support_simple_spinner_dropdown_item;
	public static final int TYPE_HARBOR=10;
	public static final int TYPE_LOADING=11;
	public static final int TYPE_BATTLESTART=12;
	public static final int TYPE_BATTLEHEAT=13;
	public static final int TYPE_BATTLEEND=14;
	public static final int TYPE_BATTLEVICTORY=15;
	public static final int TYPE_BATTLEFAIL=16;

	private static final int QUERY_CODE=2;

	private ModHelperApplication mapplication;
	private FileNameAdapter mfilenameadapter;
	private View v;
	private Spinner mSceneSpinner,mFileNameSpinner;
	private int curr_scene,curr_type,curr_music;
	private Button select,remove,update;
	private TextView pathTextView,infoTextView;
	//private String fileformat;
	private Uri srcfile;



	private void initValues(){
		Resources res=getResources();
		scene_toshow=res.getStringArray(R.array.bgm_scene_toshow);
		filenames_unselected=res.getStringArray(R.array.bgm_select_a_scene);
		filenames_loading_toshow=res.getStringArray(R.array.bgm_loading_toshow);
		filenames_battlefail_toshow=res.getStringArray(R.array.bgm_battlefail_toshow);
	}

	@Override
	public View onCreateView ( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{
		// TODO: Implement this method
		super.onCreateView(inflater,container,savedInstanceState);
		initValues();
		//更新操作说明
		String s=infoTextView.getText ( ).toString ( );
		infoTextView.setText ( s+getText(R.string.bgm_transcode_disabled));
		select.setOnClickListener ( new OnClickListener ( ){

				@Override
				public void onClick ( View p1 )
				{Intent intent=new Intent ( Intent.ACTION_GET_CONTENT );
					intent.setType ( "*/*" );
					intent.addCategory ( intent.CATEGORY_OPENABLE );
					startActivityForResult ( intent.createChooser ( intent, getText(R.string.select_a_file_selector) ), QUERY_CODE );

					// TODO: Implement this method
				}
			} );
		remove.setOnClickListener ( new OnClickListener ( ){

				@Override
				public void onClick ( View p1 )
				{
					AlertDialog.Builder adb=new AlertDialog.Builder ( getActivity ( ) );
					adb.setTitle ( R.string.notice )
						.setMessage ( new StringBuilder ( )
									 .append ( getString(R.string.confirm_to_remove_changes_to_parta) )
									 .append ( scene_toshow [ curr_scene ] )
									 .append ( getString(R.string.confirm_to_remove_changes_to_partb) )
									 .toString ( ) )
						.setNegativeButton ( R.string.cancel, null )
						.setPositiveButton ( R.string.ok, new DialogInterface.OnClickListener ( ){

							@Override
							public void onClick ( DialogInterface p1, int p2 )
							{
								String s=Utils.delDir ( getTargetFile ( curr_scene, curr_type, curr_music, Utils.FORMAT_WAV ).getParentFile ( ) ) ?"操作完成": "操作失败";
								Snackbar.make ( v, s, Snackbar.LENGTH_LONG ).show ( );
								// TODO: Implement this method
							}
						} )
						.create ( )
						.show ( );
					// TODO: Implement this method
				}
			} );
		remove.setOnLongClickListener ( new OnLongClickListener ( ){

				@Override
				public boolean onLongClick ( View p1 )
				{AlertDialog.Builder adb=new AlertDialog.Builder ( getActivity ( ) );
					adb.setTitle ( R.string.notice )
						.setMessage ( R.string.remove_all_bgm )
						.setNegativeButton ( R.string.cancel, null )
						.setPositiveButton ( R.string.ok, new DialogInterface.OnClickListener ( ){

							@Override
							public void onClick ( DialogInterface p1, int p2 )
							{
								String s=uninstallMod ( ) ?getString(R.string.success): getString(R.string.failed);
								Snackbar.make ( v, s, Snackbar.LENGTH_LONG ).show ( );
								// TODO: Implement this method
							}
						} )
						.create ( )
						.show ( );

					// TODO: Implement this method
					return true;
				}
			} );
		update.setOnClickListener ( new OnClickListener ( ){

				private void install ( )
				{
					AlertDialog.Builder adb=new AlertDialog.Builder ( getActivity ( ) );
					adb.setTitle ( R.string.please_wait )
						.setMessage ( R.string.transcode_writing )
						.setCancelable ( false );
					final AlertDialog ad=adb.create ( );
					ad.setCancelable ( false );
					final Handler h=new Handler ( ){
						public void handleMessage ( Message msg )
						{
							ad.dismiss ( );
							switch ( msg.what )
							{
								case 0:
									//无异常
									Snackbar.make ( v, R.string.success, Snackbar.LENGTH_LONG ).show ( );
									break;
								case 1:
									//操作出现异常
									Snackbar.make ( v, ( (Throwable)msg.obj ).getMessage ( ), Snackbar.LENGTH_LONG ).show ( );
							}
						}
					};
					ad.show ( );
					new Thread ( ){
						public void run ( )
						{
							try
							{
								Utils.copyFile ( getActivity ( ).getContentResolver ( ).openInputStream ( srcfile ), getTargetFile ( curr_scene, curr_type, curr_music, Utils.FORMAT_WAV ) );
								h.sendEmptyMessage ( 0 );
							}
							catch (IOException e)
							{
								h.sendMessage ( h.obtainMessage ( 1, e ) );
							}

						}
					}.start ( );
				}
				@Override
				public void onClick ( View p1 )
				{
					if ( null == srcfile /*|| null == fileformat || "".equals ( fileformat )*/)
					{
						Snackbar.make ( v, R.string.source_file_cannot_be_empty, Snackbar.LENGTH_LONG ).show ( );
						return;
					}
					if ( ModPackageManager.getInstance ( ).checkInstalled ( ModPackageInfo.MODTYPE_BGM, ModPackageInfo.SUBTYPE_EMPTY ) )
					{
						AlertDialog.Builder adb=new AlertDialog.Builder ( getActivity ( ) );
						adb.setTitle ( R.string.notice )
							.setMessage ( R.string.modpkg_install_ovwtmsg )
							.setNegativeButton ( R.string.cancel, null )
							.setPositiveButton ( R.string.uninstall_and_continue, new DialogInterface.OnClickListener ( ){

								@Override
								public void onClick ( DialogInterface p1, int p2 )
								{
									uninstallMod ( );
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


					// TODO: Implement this method
				}
			} );
		//配置场景选择的适配器
		mSceneSpinner.setAdapter ( new ArrayAdapter<String> ( getActivity ( ), TEXTVIEW_RES_ID, scene_toshow ) );
		//初始化文件名的适配器
		mfilenameadapter = FileNameAdapter.getInstance ( getActivity ( ), TEXTVIEW_RES_ID, TYPE_HARBOR );
		mFileNameSpinner.setAdapter ( mfilenameadapter );
		mSceneSpinner.setOnItemSelectedListener ( new OnItemSelectedListener ( ){

				@Override
				public void onItemSelected ( AdapterView<?> p1, View p2, int p3, long p4 )
				{
					curr_scene = p3;
					curr_type = p3 + 10;
					mFileNameSpinner.setAdapter ( FileNameAdapter.getInstance ( getActivity ( ), TEXTVIEW_RES_ID, curr_type ) );
					// TODO: Implement this method
				}

				@Override
				public void onNothingSelected ( AdapterView<?> p1 )
				{
					// TODO: Implement this method
				}
			} );
		mFileNameSpinner.setOnItemSelectedListener ( new OnItemSelectedListener ( ){

				@Override
				public void onItemSelected ( AdapterView<?> p1, View p2, int p3, long p4 )
				{
					curr_music = p3;
					// TODO: Implement this method
				}

				@Override
				public void onNothingSelected ( AdapterView<?> p1 )
				{
					// TODO: Implement this method
				}
			} );
		mapplication = (ModHelperApplication)getActivity ( ).getApplication ( );
		return v;
	}

	@Override
	public void onActivityCreated ( Bundle savedInstanceState )
	{
		// TODO: Implement this method
		//留空;
		super.onActivityCreated ( savedInstanceState );
	}

	/*
	private File getTargetFile ( int scene, int type, int num, String format )
	{
		File f=new File (
			new StringBuilder ( )
			.append ( mapplication.getResFilesDirPath ( ) )
			.append ( File.separatorChar )
			.append ( "sound" )
			.append ( File.separatorChar )
			.append ( "Music" )
			.append ( File.separatorChar )
			.append ( SCENE [ scene ] )
			.append ( File.separatorChar )
			.append ( getFileName ( type, num ) )
			.append ( format )
			.toString ( )
		);
		return f;
	}
	*/
	String getFileName ( int type, int num )
	{
		return getFileNameStrings ( type ) [ num ];
	}
	protected String identifyFormat ( InputStream in, boolean closeStream ) throws IOException
	{
		return Utils.identifyFormat ( in, closeStream );
	}
	private static String[] getFileNameStringsToShow ( int type )
	{
		if ( type == TYPE_BATTLEFAIL )
		{
			return filenames_battlefail_toshow;
		}
		if ( type == TYPE_LOADING )
		{
			return filenames_battlefail_toshow;
		}
		return getFileNameStrings ( type );
	}
	private static String[] getFileNameStrings ( int type )
	{
		if ( type == TYPE_BATTLEFAIL )
		{
			return FILENAMES_BATTLEFAIL;
		}
		if ( type == TYPE_LOADING )
		{
			return FILENAMES_LOADING;
		}
		int count=0;
		switch ( type )
		{
			case TYPE_HARBOR:
				count = MUSICCOUNT_HARBOR;
				break;
			case TYPE_BATTLESTART:
				count = MUSICCOUNT_BATTLESTART;
				break;
			case TYPE_BATTLEHEAT:
				count = MUSICCOUNT_BATTLEHEAT;
				break;
			case TYPE_BATTLEEND:
				count = MUSICCOUNT_BATTLEEND;
				break;
			case TYPE_BATTLEVICTORY:
				count = MUSICCOUNT_BATTLEVICTORY;
				break;
		}
		return Arrays.copyOf ( FILENAMES_UNIVERSAL, count );
	}

	@Override
	public void onActivityResult ( int requestCode, int resultCode, Intent data )
	{
		// TODO: Implement this method
		super.onActivityResult ( requestCode, resultCode, data );
		if ( resultCode == AppCompatActivity.RESULT_OK && requestCode == QUERY_CODE )
		{
			if ( data != null )
			{
				String s=Utils.FORMAT_UNKNOWN;
				if ( data.getData ( ) == null )
				{
					Snackbar.make ( v, R.string.source_file_cannot_be_empty, Snackbar.LENGTH_LONG ).show ( );
					return;
				}
				try
				{
					s = identifyFormat ( getActivity ( ).getContentResolver ( ).openInputStream ( data.getData ( ) ), true ) ;
				}
				catch (IOException e)
				{Snackbar.make ( v, e.getMessage ( ), Snackbar.LENGTH_LONG ).show ( );}
				if ( !Utils.FORMAT_WAV.equals ( s ) )
				{
					Snackbar.make ( v, R.string.not_a_wavfile, Snackbar.LENGTH_LONG ).show ( );
					return;
				}
				else
				{
					srcfile = data.getData ( );
					//fileformat=s;
					pathTextView.setText ( new StringBuilder ( ).append ( srcfile.getPath ( ) ).toString ( ) );
				}

			}
			else
			{
				Snackbar.make ( v, R.string.source_file_cannot_be_empty, Snackbar.LENGTH_LONG ).show ( );
				return;
			}
		}
	}


	private static class FileNameAdapter extends ArrayAdapter<String>
	{

		private static String[] data;
		private static String[] act_data;
		private static FileNameAdapter self;
		public static FileNameAdapter getInstance ( Context context, int textViewResId, int type )
		{
			data = getFileNameStringsToShow ( type );
			act_data = getFileNameStrings ( type );
			self = new FileNameAdapter ( context, textViewResId, data );
			return self;
		}
		private FileNameAdapter ( Context context, int textViewResourceId, String[] data )
		{
			super ( context, textViewResourceId, data );
		}
		/*
		 public void reconfigure (int type)
		 {
		 this.clear ( );
		 this.data = getFileNameStringsToShow ( type );
		 this.act_data = getFileNameStrings ( type );
		 this.addAll(data);
		 }*/
		public String[] getCurrentData ( )
		{
			return this.act_data;
		}


	}
}
