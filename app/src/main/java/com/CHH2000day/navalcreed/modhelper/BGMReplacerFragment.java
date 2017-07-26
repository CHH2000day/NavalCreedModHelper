package com.CHH2000day.navalcreed.modhelper;

import android.content.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.*;
public class BGMReplacerFragment extends FunctionFragment
{


	private static final int MUSICCOUNT_HARBOR=6;
	private static final int MUSICCOUNT_LOADING=3;
	private static final int MUSICCOUNT_BATTLESTART=4;
	private static final int MUSICCOUNT_BATTLEHEAT=7;
	private static final int MUSICCOUNT_BATTLEEND=3;
	private static final int MUSICCOUNT_BATTLEVICTORY=4;
	private static final int MUSICCOUNT_BATTLEFAIL=2;
	private static final String[] SCENE={"Harbor","BattleStart","BattleHeat","BattleEnd","Victory","Fial"/*因为你游程序员把Fail打成Fial了，所以将错就错了*/,"Loading"};
	private static final String[] SCENE_TOSHOW={"港口","战斗开始","战斗激战","战斗即将结束","战斗胜利","战斗失败","加载音乐"};
	private static final String[] FILENAMES_UNIVERSAL={"1","2","3","4","5","6","7"};
	private static final String[] FILENAMES_UNSELECTED={"请选择情景"};
	private static final String[] FILENAMES_BATTLEFAIL={"Danger","Fail"};
	private static final String[] FILENAMES_BATTLEFAIL_TOSHOW={"即将失败","失败"};
	private static final String[] FILENAMES_LOADING={"Loading","Login","Queuing"};
	private static final String[] FILENAMES_LOADING_TOSHOW={"加载中","登录","匹配中"};
	public static final int TYPE_HARBOR=10;
	public static final int TYPE_LOADING=11;
	public static final int TYPE_BATTLESTART=12;
	public static final int TYPE_BATTLEHEAT=13;
	public static final int TYPE_BATTLEEND=14;
	public static final int TYPE_BATTLEVICTORY=15;
	public static final int TYPE_BATTLEFAIL=16;
	public static final String FORMAT_OGG=".ogg";
	public static final String FORMAT_WAV=".wav";


	private ModHelperApplication mapplication;
	private FileNameAdapter mfilenameadapter;
	private View v;
	private Spinner mSceneSpinner,mFileNameSpinner;





	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// TODO: Implement this method
		v = inflater.inflate ( R.layout.bgmreplacer, null );
		//初始化文件名的适配器
		mfilenameadapter = new FileNameAdapter ( getActivity ( ), android.R.layout.simple_spinner_dropdown_item, FILENAMES_UNSELECTED );
		mapplication = (ModHelperApplication)getActivity ( ).getApplication ( );
		return v;
	}
	@Override
	public boolean installMod (int typenum, int num, byte[] deceyptedFileData)
	{
		// TODO: Implement this method
		return false;
		//Not finished
	}
	private File getTargetFile (int type, int num, String format)
	{
		File f=new File (
			new StringBuilder ( )
			.append ( mapplication.getResFilesDirPath ( ) )
			.append ( File.separatorChar )
			.append ( "sound" )
			.append ( File.separatorChar )
			.append ( "Music" )
			.append ( File.separatorChar )
			.append ( SCENE[ type ] )
			.append ( File.separatorChar )
			.append ( getFileName ( type, num ) )
			.append ( format )
			.toString ( )
		);
		return f;
	}
	String getFileName (int type, int num)
	{
		return getFileNameStrings ( type )[ num ];
	}
	private static String[] getFileNameStringsToShow (int type)
	{
		if (type == TYPE_BATTLEFAIL)
		{
			return FILENAMES_BATTLEFAIL_TOSHOW;
		}
		if (type == TYPE_LOADING)
		{
			return FILENAMES_LOADING_TOSHOW;
		}
		return getFileNameStrings ( type );
	}
	private static String[] getFileNameStrings (int type)
	{
		if (type == TYPE_BATTLEFAIL)
		{
			return FILENAMES_BATTLEFAIL;
		}
		if (type == TYPE_LOADING)
		{
			return FILENAMES_LOADING;
		}
		int count=0;
		switch (type)
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

	private static class FileNameAdapter extends ArrayAdapter<String>
	{

		private static String[] data;
		private static String[] act_data;
		private static FileNameAdapter self;
		public static FileNameAdapter getInstance (Context context, int textViewResId, int type)
		{
			data = getFileNameStringsToShow ( type );
			act_data = getFileNameStrings ( type );
			self = new FileNameAdapter ( context, textViewResId, data );
			return self;
		}
		private FileNameAdapter (Context context, int textViewResourceId, String[] data)
		{
			super ( context, textViewResourceId, data );
		}
		public void reconfigure (int type)
		{
			this.clear ( );
			this.data = getFileNameStringsToShow ( type );
			this.act_data = getFileNameStrings ( type );
		}
		public String[] getCurrentData ()
		{
			return this.act_data;
		}


	}

}
