package com.chh2000day.navalcreed.modhelper;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.CHH2000day.navalcreed.modhelper.R;
import com.google.android.material.snackbar.Snackbar;

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
					adb.setTitle ( R.string.notice )
						.setMessage ( R.string.ovrd_warning )
							.setNegativeButton(R.string.cancel, (p112, p2) -> {
								// TODO: Implement this method
								cancel();
							})
							.setPositiveButton(R.string.cont, (p11, p2) -> {
								override();
								// TODO: Implement this method
							})
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
            recyclerview.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
            recyclerview.addItemDecoration(new VerticalSpaceItemDecoration());
			recyclerview.setAdapter ( adapter );
			ModPackageManager.getInstance ( ).setonDataChangedListener ( this );
		}
	}

	@Override
	public void onResume ( )
	{
		// TODO: Implement this method
		super.onResume ( );

		onChange ( );
	}

	@Override
	public void onPause ( )
	{
		// TODO: Implement this method
		super.onPause ( );

	}

	@Override
	public void onDestroyView ( )
	{
		// TODO: Implement this method
		super.onDestroyView ( );
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

    private static class ViewHolder extends RecyclerView.ViewHolder {

        private final View v;

        public ViewHolder(View v) {
            super(v);
            this.v = v;
        }

        public View getView() {
            return v;
        }
    }

    private class UninstallListener implements View.OnLongClickListener {

        @Override
        public boolean onLongClick(View p1) {
            if (ModPackageManager.getInstance().isOverride()) {
                //OVRD时禁用管理器
                return false;
            }
            int num = (int) p1.getTag();
            String modtype = ModPackageManager.PUBLIC_KEYS[num];
            if (modtype.equals(ModPackageInfo.MODTYPE_OTHER)) {
                Snackbar.make(v, "This type of mod package can't be uninstalled", Snackbar.LENGTH_LONG).show();
            }
            if (modtype.startsWith("CV")) {
                if (ModPackageManager.getInstance().checkInstalled(ModPackageInfo.MODTYPE_CV, modtype)) {
                    uninstall(modtype);
                }
            } else {
                if (ModPackageManager.getInstance().checkInstalled(modtype, ModPackageInfo.SUBTYPE_EMPTY)) {
                    uninstall(modtype);
                }
            }


            // TODO: Implement this method
            return true;
        }

        private void uninstall(final String key) {
            AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
            adb.setTitle(R.string.notice)
                    .setMessage(getString(R.string.confirm_to_remove_changes_to_parta) + ModPackageManager.getInstance().resolveModType(key) + ":" + ModPackageManager.getInstance().getModList().get(key) + getString(R.string.confirm_to_remove_changes_to_partb))
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.cont, (p1, p2) -> {
                        String type = key.startsWith("CV") ? ModPackageInfo.MODTYPE_CV : key;
                        String subType = type.equals(ModPackageInfo.MODTYPE_CV) ? key : ModPackageInfo.SUBTYPE_EMPTY;
                        boolean b = ModPackageManager.getInstance().requestUninstall(type, subType, (ModHelperApplication) getActivity().getApplication());
                        String str = b ? getString(R.string.success) : getString(R.string.failed);
                        Snackbar.make(v, str, Snackbar.LENGTH_LONG).show();
                        // TODO: Implement this method
                    });
            adb.create().show();

        }

    }

	private class MyAdapter extends RecyclerView.Adapter
	{

		private final Context context;
		private final LayoutInflater li;
		private final String[] keys;
		private final View.OnLongClickListener listener;
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
            return new ViewHolder(vi);
		}

		@Override
		public void onBindViewHolder ( RecyclerView.ViewHolder p1, int p2 )
		{
			View v=( (ViewHolder)p1 ).getView ( );
			RelativeLayout rl=(RelativeLayout)v.findViewById ( R.id.modmanageritemRelativeLayout );
			TextView info=(TextView)v.findViewById ( R.id.modmanageritemTextView );
			TextView memo=(TextView)v.findViewById ( R.id.modmanageritemMemo );
			rl.setTag ( p2 );
			String key=keys [ p2 ];
			String type="";
			String subtype="";
			if ( key.equals ( ModPackageInfo.SUB_MODTYPE_CV_CN ) || key.equals ( ModPackageInfo.SUB_MODTYPE_CV_EN ) || key.equals ( ModPackageInfo.SUB_MODTYPE_CV_JP_BB ) || key.equals ( ModPackageInfo.SUB_MODTYPE_CV_JP_CV ) || key.equals ( ModPackageInfo.SUB_MODTYPE_CV_JP_CA ) || key.equals ( ModPackageInfo.SUB_MODTYPE_CV_JP_DD ) )
			{
				type = ModPackageInfo.MODTYPE_CV;
				subtype = key;
			}
			else
			{
				type = key;
				subtype = ModPackageInfo.SUBTYPE_EMPTY;
			}
			//如果对应mod包已安装
			if ( ModPackageManager.getInstance ( ).checkInstalled ( type, subtype ) )
			{
				info.setText ( new StringBuilder ( ).append ( getString ( R.string.modtype ) )
							  .append ( ModPackageManager.getInstance ( ).resolveModType ( keys [ p2 ] ) )
							  .append ( "\n" )
							  .append ( ModPackageManager.getInstance ( ).getModName ( keys [ p2 ] ) ) );
				rl.setOnLongClickListener ( listener );
				memo.setText ( R.string.long_click_to_uninstall );

			}
			else
			{
                memo.setText(R.string.mod_not_installed);
				info.setText ( new StringBuilder ( ).append ( getString ( R.string.modtype ) )
							  .append ( ModPackageManager.getInstance ( ).resolveModType ( keys [ p2 ] ) )
                        .toString());
			}


			// TODO: Implement this method
		}

		@Override
		public int getItemCount ( )
		{
			if ( ModPackageManager.getInstance ( ).isOverride ( ) )
			{
				return 0;
			}
			// TODO: Implement this method
			return keys.length;
		}




	}
}
