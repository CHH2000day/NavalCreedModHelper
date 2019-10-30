package com.CHH2000day.navalcreed.modhelper;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Objects;

public class CrewPicReplacerFragment extends ModFragment
{

	private View v;
	private Bitmap ba;
	private int selectedcountry=0,selectedcrew=0;
	private TextView selectedpic;
	private String[] countrys={"usa","japan","uk","china","italy","france","ussr","german"};
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.crew_pic_replacer, null);
		Spinner country = v.findViewById(R.id.crewpicreplacerSpinnerCountry);
        country.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected (AdapterView<?> p1, View p2, int p3, long p4)
				{
					selectedcountry = p3;
					/*if(p3==3){
					 Snackbar.make(v,"该选项可能存在bug",Snackbar.LENGTH_LONG).show();
					 }*/
					// TODO: Implement this method
				}

				@Override
				public void onNothingSelected (AdapterView<?> p1)
				{
					// TODO: Implement this method
				}
			} );

		Spinner num = v.findViewById(R.id.crewpicreplacerSpinnerCrew);
        num.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected (AdapterView<?> p1, View p2, int p3, long p4)
				{selectedcrew = p3;
					// TODO: Implement this method
				}

				@Override
				public void onNothingSelected (AdapterView<?> p1)
				{
					// TODO: Implement this method
				}
			} );
		Button selpic = v.findViewById(R.id.crewpicreplacerButtonSelectPic);
		selpic.setOnClickListener(p1 -> {
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");
			startActivityForResult(intent, 2);

			// TODO: Implement this method
		});
		Button removepic = v.findViewById(R.id.crewpicreplacerButtonRemove);
		removepic.setOnClickListener(p1 -> {
			AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
			adb.setTitle(R.string.notice)
					.setMessage(R.string.confirm_to_remove_changes)
					.setPositiveButton(R.string.remove_changes, (dialogInterface, p2) -> {
						if (getFile(selectedcountry, selectedcrew).delete()) {
							Snackbar.make(v, R.string.success, Snackbar.LENGTH_LONG).show();
						} else {
							Snackbar.make(v, R.string.failed, Snackbar.LENGTH_LONG).show();
						}

						// TODO: Implement this method
					})
					.setNegativeButton(R.string.cancel, null)
					.create()
					.show();
			// TODO: Implement this method
		});
		removepic.setOnLongClickListener(p1 -> {
			AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
			adb.setTitle(R.string.notice)
					.setMessage(R.string.confirm_to_remove_all_changes)
					.setPositiveButton(R.string.remove_changes, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface p1, int p2) {
							if (uninstallMod()) {
								Snackbar.make(v, R.string.success, Snackbar.LENGTH_LONG).show();
							} else {
								Snackbar.make(v, R.string.failed, Snackbar.LENGTH_LONG).show();
							}
							// TODO: Implement this method
						}
					})
					.setNegativeButton(R.string.cancel, null)
					.create()
					.show();

			// TODO: Implement this method
			return true;
		});
		Button updatepic = v.findViewById(R.id.crewpicreplacerButtonReplace);
        updatepic.setOnClickListener(new View.OnClickListener() {
				private void install ()
				{
					try
					{	File out=getFile ( selectedcountry, selectedcrew );
						if (!out.getParentFile ( ).exists ( ))
						{out.getParentFile ( ).mkdirs ( );}
						FileOutputStream fos=new FileOutputStream ( out );
						ba.compress ( Bitmap.CompressFormat.PNG, 100, fos );
						fos.flush ( );
						fos.close ( );
						Snackbar.make ( v, R.string.success, Snackbar.LENGTH_LONG ).show ( );
					}
					catch (Exception e)
					{
						Snackbar.make ( v, e.getMessage ( ), Snackbar.LENGTH_LONG ).show ( );
					}
				}
				@Override
				public void onClick (View p1)
				{
					if (null == ba)
					{
						Snackbar.make ( v, R.string.source_file_cannot_be_empty, Snackbar.LENGTH_LONG ).show ( );
						return;
					}
					if (ModPackageManager.getInstance ( ).checkInstalled ( ModPackageInfo.MODTYPE_CREWPIC, ModPackageInfo.SUBTYPE_EMPTY ))
					{
						AlertDialog.Builder adb=new AlertDialog.Builder ( getActivity ( ) );
						adb.setTitle ( R.string.notice )
							.setMessage ( R.string.modpkg_install_ovwtmsg )
							.setNegativeButton ( R.string.cancel, null )
							.setPositiveButton ( R.string.uninstall_and_continue, new DialogInterface.OnClickListener ( ){

								@Override
								public void onClick (DialogInterface p1, int p2)
								{
									uninstallMod ( );
									install ( );
									// TODO: Implement this method
								}
							} );
						adb.create().show();
					}
					else
					{
						install ( );
					}

					// TODO: Implement this method
				}
			} );
		selectedpic = v.findViewById(R.id.crewpicreplacerSelectedFile);
		// TODO: Implement this method
		showAd(v);
		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		//showAd(v);
	}

	@Override
	public void onDestroy () {
		if (ba != null) {
			ba.recycle ( );
			//手动释放以防止Bitmap未被释放

		}
		// TODO: Implement this method
		super.onDestroy ( );
	}


	private File getFile (int country, int num)
	{
		return new File ( getFilePath ( country, num ) );
	}
	private String getFilePath (int country, int num)
	{
		return new StringBuilder ( )
				.append(getMainActivity().getModHelperApplication().getResFilesDirPath())
			.append ( File.separatorChar )
			.append ( "pic" )
			.append ( File.separatorChar )
			.append ( "crewhead" )
			.append ( File.separatorChar )
			.append ( countrys[ country ] )
			.append ( File.separatorChar )
			.append ( (num + 1) )
			.append ( ".png" )
			.toString ( );
	}
	@Override
	public boolean uninstallMod ()
	{
		// TODO: Implement this method
		ModPackageManager.getInstance ( ).postUninstall ( ModPackageInfo.MODTYPE_CREWPIC, ModPackageInfo.SUBTYPE_EMPTY );
		return Utils.delDir ( getFile ( selectedcountry, selectedcrew ).getParentFile ( ) );
	}

	@Override
	public void onActivityResult (int requestCode, int resultCode, Intent data)
	{
		// TODO: Implement this method
		super.onActivityResult ( requestCode, resultCode, data );
		if (requestCode == 2 && resultCode == AppCompatActivity.RESULT_OK)
		{
			try
			{
				if (data == null)
				{
					Snackbar.make ( v, R.string.source_file_cannot_be_empty, Snackbar.LENGTH_LONG ).show ( );
					return;
				}
				if (ba != null)
				{
					//手动释放以防止Bitmap未被释放
					ba.recycle ( );
					System.gc ( );
				}
				ba = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(Objects.requireNonNull(data.getData())));
				selectedpic.setText ( data.getData ( ).toString ( ) );
			}
			catch (Throwable t)
			{Snackbar.make ( v, t.getMessage ( ), Snackbar.LENGTH_LONG ).show ( );}
		}
		super.onActivityResult ( requestCode, resultCode, data );
	}


}
