package com.CHH2000day.navalcreed.modhelper;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.SoftReference;

public class BGReplacerFragment extends ModFragment
{


	private static final String[] filename={"loadingbg1.jpg","loadingbg2.jpg","loadingbg3.jpg"};
	private static final String[] cateoty={"loading","loadingmap","matching"};
	private String abs_path/*="/sdcard/Android/data/com.tencent.navalcreed/files/pic"*/;
	private TextView picname;
	private Bitmap ba;
	private int cat=0,filepos=0;
	private View v;
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		abs_path = new StringBuilder ( )
			.append ( ((ModHelperApplication)getActivity ( ).getApplication ( )).getResFilesDirPath ( ) )
			.append ( File.separatorChar )
			.append ( "pic" )
			.toString ( );
		v = inflater.inflate ( R.layout.bgreplacer_fragment, null );
		Spinner cateory = v.findViewById(R.id.bgreplacerSpinner1);
        cateory.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected (AdapterView<?> p1, View p2, int p3, long p4)
				{
					cat = p3;
					// TODO: Implement this method
				}

				@Override
				public void onNothingSelected (AdapterView<?> p1)
				{
					// TODO: Implement this method
				}
			} );
		Spinner file = v.findViewById(R.id.bgreplacerSpinner2);

        file.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected (AdapterView<?> p1, View p2, int p3, long p4)
				{
					filepos = p3;
					// TODO: Implement this method
				}

				@Override
				public void onNothingSelected (AdapterView<?> p1)
				{
					// TODO: Implement this method
				}
			} );
		picname = v.findViewById(R.id.bgreplacerPic);
		Button btnrm = v.findViewById(R.id.bgreplacer_remove);
		Button update = v.findViewById(R.id.bgreplacerbtn_update);
		Button selpic = v.findViewById(R.id.bgreplacerbtn_select);
		selpic.setOnClickListener(p1 -> {
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");
			startActivityForResult(intent, 1);

			// TODO: Implement this method
		});
		btnrm.setOnClickListener(p1 -> {
			AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
			adb.setTitle(R.string.notice)
					.setMessage(R.string.confirm_to_remove_all_changes)
					.setNegativeButton(R.string.cancel, null);
			adb.setPositiveButton(R.string.ok, (dialogInterface, p2) -> {
				removechanges();
				// TODO: Implement this method
			});

			adb.create().show();
			// TODO: Implement this method
		});
        update.setOnClickListener(new OnClickListener() {

				private void install ()
				{File parent=new File ( abs_path, cateoty[ cat ] );
					File target=new File ( parent, filename[ filepos ] );
					if (!parent.exists ( ))
					{
						parent.mkdirs ( );
					}
					try
					{
						FileOutputStream fos =new FileOutputStream ( target );
						ba.compress ( Bitmap.CompressFormat.JPEG, 100, fos );
						fos.flush ( );
						fos.close ( );
						Snackbar.make ( v, R.string.success, Snackbar.LENGTH_LONG ).show ( );
					}
					catch (Exception e)
					{Snackbar.make ( v, e.getMessage ( ), Snackbar.LENGTH_LONG ).show ( );}

				}
				@Override
				public void onClick (View p1)
				{if (ba == null)
					{
						Snackbar.make ( v, R.string.source_file_cannot_be_empty, Snackbar.LENGTH_LONG ).show ( );
					}
					else
					{if (ModPackageManager.getInstance ( ).checkInstalled ( ModPackageInfo.MODTYPE_BACKGROUND, ModPackageInfo.SUBTYPE_EMPTY ))
						{
							AlertDialog.Builder adb=new AlertDialog.Builder ( getActivity ( ) );
							adb.setTitle ( R.string.notice )
								.setMessage ( R.string.modpkg_install_ovwtmsg )
								.setNegativeButton ( R.string.cancel, null )
									.setPositiveButton(R.string.cancel_and_exit, (p11, p2) -> {
										uninstallMod();
										install();
										// TODO: Implement this method
									});
							adb.create().show();
						}
						else
						{
							install ( );
						}
					}
					// TODO: Implement this method
				}
			} );

		// TODO: Implement this method
        showAd(v);
		return v;
	}
	private void removechanges ()
	{
		//防止误删船员头像
		//Utils.delDir(new File(abs_path));
		uninstallMod ( );
		Snackbar.make ( v, R.string.success, Snackbar.LENGTH_LONG ).show ( );
	}
	@Override
	public boolean uninstallMod ()
	{
		ModPackageManager.getInstance ( ).postUninstall ( ModPackageInfo.MODTYPE_BACKGROUND, ModPackageInfo.SUBTYPE_EMPTY );
		for (String s:cateoty)
		{
			Utils.delDir ( new File ( abs_path, s ) );
		}
		// TODO: Implement this method
		return true;
	}

	@Override
	public void onResume() {
		super.onResume();
        //showAd(v);
	}

	@Override
	public void onDestroy () {
		// TODO: Implement this method
		if (ba != null) {
			ba.recycle ( );
			//手动释放以防止Bitmap未被释放

		}
		super.onDestroy ( );
	}



	@Override
	public void onActivityResult (int requestCode, int resultCode, @NonNull Intent data)
	{
		// TODO: Implement this method
		super.onActivityResult ( requestCode, resultCode, data );
		if (data != null && requestCode == 1 && resultCode == AppCompatActivity.RESULT_OK)
		{
			try
			{
				if (ba != null)
				{
					ba.recycle ( );
					System.gc ( );
					//手动释放以防止Bitmap未被释放
				}
				Uri u=data.getData ( );
				InputStream in=getActivity ( ).getContentResolver ( ).openInputStream ( u );
				//ba = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(u));
				SoftReference sr=new SoftReference ( BitmapFactory.decodeStream ( in ) );
				ba = (Bitmap)sr.get ( );
				in.close ( );
				picname.setText ( data.getData ( ).toString ( ) );

			}
			catch (OutOfMemoryError r)
			{
				Snackbar.make ( v, "文件过大，内存溢出", Snackbar.LENGTH_LONG ).show ( );
			}
			catch (final Throwable t)
			{
				t.printStackTrace ( );
				String clazzn =t.getClass ( ).getName ( );
				int p=clazzn.lastIndexOf ( '$' );

				String clazz=(p >= 0) ?clazzn.substring ( 0, p ): "错误";

				Snackbar.make ( v, clazz, Snackbar.LENGTH_LONG ).show ( );
			}
		}
		super.onActivityResult ( requestCode, resultCode, data );
	}


}
