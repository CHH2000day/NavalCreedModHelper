package com.CHH2000day.navalcreed.modhelper;
import android.support.v4.app.*;
import android.view.*;
import android.support.v7.widget.*;
import android.widget.*;
import android.os.*;
import android.content.res.*;
import android.widget.CompoundButton.*;
import android.support.v7.app.*;
import android.content.*;
import java.util.*;
import android.support.design.widget.*;

public class ModPackageManagerFragment extends Fragment implements ModPackageManager.OnDataChangedListener
{

	private View v;
	private RecyclerView recyclerview;
	private MyAdapter adapter;
	private ToggleButton ovrd_switch;


	public static final int COLOR_ECAM_AMBER=0xD97900;
	public static final int COLOR_ECAM_GREEN=0x00E300;
	@Override
	public View onCreateView ( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{
		// TODO: Implement this method
		super.onCreateView ( inflater, container, savedInstanceState );
		v = inflater.inflate ( R.layout.modmanagerfragmemt, null );
		recyclerview = (RecyclerView)v.findViewById ( R.id.modmanagerfragmemtRecyclerView );
		ovrd_switch = (ToggleButton)v.findViewById ( R.id.modmanagerswitcherToggleButton1 );
		return v;
	}

	@Override
	public void onActivityCreated ( Bundle savedInstanceState )
	{
		// TODO: Implement this method
		super.onActivityCreated ( savedInstanceState );
		if ( ModPackageManager.getInstance ( ).isOverride ( ) )
		{
			ovrd_switch.setChecked ( true );
		}
		adapter = new MyAdapter ( getActivity ( ) );
		ovrd_switch.setOnCheckedChangeListener ( new OnCheckedChangeListener ( ){

				@Override
				public void onCheckedChanged ( CompoundButton p1, boolean isChecked )
				{
					if ( !isChecked )
					{
						if ( ModPackageManager.getInstance ( ).isOverride ( ) )
						{
							ModPackageManager.getInstance ( ).setIsOverride ( false );
						}
						return;
					}
					AlertDialog.Builder adb= new AlertDialog.Builder ( getActivity ( ) );
					adb.setTitle ( "注意" )
						.setMessage ( "超控mod管理机制将使mod管理器失效，是否继续？" )
						.setNegativeButton ( "取消", new DialogInterface.OnClickListener ( ){

							@Override
							public void onClick ( DialogInterface p1, int p2 )
							{
								// TODO: Implement this method
								cancel ( );
							}
						} )
						.setPositiveButton ( "继续", new DialogInterface.OnClickListener ( ){

							@Override
							public void onClick ( DialogInterface p1, int p2 )
							{
								override ( );
								// TODO: Implement this method
							}
						} )
						.setCancelable ( false );
					AlertDialog ad=adb.create ( );
					ad.setCanceledOnTouchOutside ( false );
					ad.show ( );
					// TODO: Implement this method
				}
				private void override ( )
				{
					ModPackageManager.getInstance ( ).setIsOverride ( true );

				}
				private void cancel ( )
				{
					ovrd_switch.setChecked ( false );
					if ( ModPackageManager.getInstance ( ).isOverride ( ) )
					{
						ModPackageManager.getInstance ( ).setIsOverride ( false );
					}
				}
			} );
		if ( !ModPackageManager.getInstance ( ).isOverride ( ) )
		{
			recyclerview.setLayoutManager ( new LinearLayoutManager ( getActivity ( ), LinearLayoutManager.VERTICAL, false ) );
			recyclerview.setAdapter ( adapter );
		}
	}

	@Override
	public void onResume ( )
	{
		// TODO: Implement this method
		super.onResume ( );
		ModPackageManager.getInstance ( ).setonDataChangedListener ( this );
		onChange ( );
	}

	@Override
	public void onPause ( )
	{
		// TODO: Implement this method
		super.onPause ( );
		ModPackageManager.getInstance ( ).unregistDataChangeListener ( );
	}

	@Override
	public void onChange ( )
	{	if ( adapter != null && recyclerview != null )
		{
			adapter = null;
			adapter = new MyAdapter ( getActivity ( ) );	
			recyclerview.setAdapter ( adapter );
		}
		// TODO: Implement this method
	}


	private class MyAdapter extends RecyclerView.Adapter
	{

		private Context context;
		private LayoutInflater li;
		private String[] keys;
		private OnLongClickListener listener;
		public MyAdapter ( Context ctx )
		{
			context = ctx;
			li = LayoutInflater.from ( context );
			keys = ModPackageManager.PUBLIC_KEYS;
			listener = new UninstallListener ( );
		}

		@Override
		public RecyclerView.ViewHolder onCreateViewHolder ( ViewGroup p1, int p2 )
		{
			View vi=li.inflate ( R.layout.modmanager_item, null );
			// TODO: Implement this method
			return new ViewHolder ( vi );
		}

		@Override
		public void onBindViewHolder ( RecyclerView.ViewHolder p1, int p2 )
		{View v=( (ViewHolder)p1 ).getView ( );
			RelativeLayout rl=(RelativeLayout)v.findViewById ( R.id.modmanageritemRelativeLayout );
			TextView info=(TextView)v.findViewById ( R.id.modmanageritemTextView );
			TextView memo=(TextView)v.findViewById ( R.id.modmanageritemMemo );
			rl.setTag ( p2 );
			//如果对应mod包已安装
			if ( !ModPackageManager.getInstance ( ).getModList ( ).get ( keys [ p2 ] ).equals ( "" ) )
			{
				info.setText ( new StringBuilder ( ).append ( "mod类型:" )
							  .append ( ModPackageManager.resolveModType ( keys [ p2 ] ) )
							  .append ( "\n" )
							  .append ( ModPackageManager.getInstance ( ).getModList ( ).get ( keys [ p2 ] ).toString ( ) ) );
				rl.setOnLongClickListener ( listener );


			}
			else
			{
				memo.setText ( "" );
				info.setText ( new StringBuilder ( ).append ( "mod类型:" )
							  .append ( ModPackageManager.resolveModType ( keys [ p2 ] ) )
							  .append ( "\n" )
							  .append ( "mod未安装" ).toString ( ) );
			}


			// TODO: Implement this method
		}

		@Override
		public int getItemCount ( )
		{
			// TODO: Implement this method
			return keys.length;
		}




	}
	private class UninstallListener implements OnLongClickListener
	{

		@Override
		public boolean onLongClick ( View p1 )
		{
			int num=p1.getTag ( );
			String key=ModPackageManager.PUBLIC_KEYS [ num ];
			if ( key.equals ( ModPackageInfo.SUB_MODTYPE_CV_CN ) || key.equals ( ModPackageInfo.SUB_MODTYPE_CV_EN ) )
			{
				if ( ModPackageManager.getInstance ( ).checkInstalled ( ModPackageInfo.MODTYPE_CV, key ) )
				{
					uninstall ( key );
				}
			}
			else
			{
				if ( ModPackageManager.getInstance ( ).checkInstalled ( key, ModPackageInfo.SUBTYPE_EMPTY ) )
				{
					uninstall ( key );
				}
			}


			// TODO: Implement this method
			return true;
		}
		private void uninstall ( final String key )
		{
			AlertDialog.Builder adb=new AlertDialog.Builder ( getActivity ( ) );
			adb.setTitle ( "注意" )
				.setMessage ( "确定要卸载" + ModPackageManager.resolveModType ( key ) + ":" + ModPackageManager.getInstance ( ).getModList ( ).get ( key ) + "么?" )
				.setNegativeButton ( "取消", null )
				.setPositiveButton ( "确定", new DialogInterface.OnClickListener ( ){

					@Override
					public void onClick ( DialogInterface p1, int p2 )
					{
						String type=	key.equals ( ModPackageInfo.SUB_MODTYPE_CV_CN ) || key.equals ( ModPackageInfo.SUB_MODTYPE_CV_EN ) ?ModPackageInfo.MODTYPE_CV: key;
						String subType=type.equals ( ModPackageInfo.MODTYPE_CV ) ?key: ModPackageInfo.SUBTYPE_EMPTY;
						ModPackageManager.getInstance ( ).requestUninstall ( type, subType , (ModHelperApplication)getActivity ( ).getApplication ( ) );
						Snackbar.make ( v, "操作完成", Snackbar.LENGTH_LONG ).show ( );
						// TODO: Implement this method
					}
				} );
			adb.create ( ).show ( );

		}

	}
	private class ViewHolder extends RecyclerView.ViewHolder
	{

		private View v;
		public ViewHolder ( View v )
		{
			super ( v );
			this.v = v;
		}
		public View getView ( )
		{
			return v;
		}
	}
}
