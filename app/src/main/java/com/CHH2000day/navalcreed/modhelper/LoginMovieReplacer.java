package com.CHH2000day.navalcreed.modhelper;

import android.annotation.SuppressLint;
import android.content.*;
import android.os.*;
import android.support.design.widget.*;
import android.support.v4.app.*;
import android.view.*;
import android.widget.*;
import android.net.*;
import java.io.*;
import android.view.View.*;
import android.support.v7.app.*;

public class LoginMovieReplacer extends Fragment
{

	private ModHelperApplication mapplication;
	private View v;
	private TextView file;

    private Uri srcfile;
	private File target;

	private static int QUERY_CODE=2;
	@Override
	public View onCreateView ( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{

		mapplication = (ModHelperApplication)getActivity ( ).getApplication ( );
		v = inflater.inflate ( R.layout.loginmoviereplacer_fragment, null );
		file = v.findViewById(R.id.loginmoviereplacerfragmentTextView);
		Button select = v.findViewById(R.id.loginmoviereplacerfragmentButtonSelect);
		Button update = v.findViewById(R.id.loginmoviereplacerfragmentButtonUpdate);
		Button remove = v.findViewById(R.id.loginmoviereplacerfragmentButtonRemove);
		// TODO: Implement this method

		select.setOnClickListener(p1 -> {
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("*/*");
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			startActivityForResult(Intent.createChooser(intent, getText(R.string.select_a_file_selector)), QUERY_CODE);

			// TODO: Implement this method
		});
		update.setOnClickListener(p1 -> {
			if (srcfile == null) {
				Snackbar.make(v, R.string.source_file_cannot_be_empty, Snackbar.LENGTH_LONG).show();
				return;
			}
			AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
			adb.setTitle(R.string.please_wait)
					.setMessage(R.string.transcode_writing)
					.setCancelable(false);
			final AlertDialog ad = adb.create();
			ad.setCancelable(false);
			@SuppressLint("HandlerLeak") final Handler h = new Handler() {
				public void handleMessage(Message msg) {
					ad.dismiss();
					switch (msg.what) {
						case 0:
							//无异常
							Snackbar.make(v, R.string.success, Snackbar.LENGTH_LONG).show();
							break;
						case 1:
							//操作出现异常
							Snackbar.make(v, ((Throwable) msg.obj).getMessage(), Snackbar.LENGTH_LONG).show();
					}
				}
			};
			ad.show();
			new Thread() {
				public void run() {
					try {
						Utils.copyFile(getActivity().getContentResolver().openInputStream(srcfile), gettargetfile());
						h.sendEmptyMessage(0);
					} catch (IOException e) {
						h.sendMessage(h.obtainMessage(1, e));
					}

				}
			}.start();

			// TODO: Implement this method
		});

        remove.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick ( View p1 )
				{
					String result =(String)( gettargetfile().delete()?getText(R.string.success):getText(R.string.failed));
					Snackbar.make(v,result,Snackbar.LENGTH_LONG).show();
					// TODO: Implement this method
				}
			} );

		return v;
	}

	private File gettargetfile(){
		if(target==null){
			target=new File(mapplication.getResFilesDir(),"loginmovie.ogv");
		}
		return target;
	}
	@Override
	public void onActivityResult ( int requestCode, int resultCode, Intent data )
	{
		// TODO: Implement this method
		super.onActivityResult ( requestCode, resultCode, data );
		if ( requestCode != QUERY_CODE )
		{return;}
		if( resultCode != AppCompatActivity.RESULT_OK){
			return;
		}
		if ( data == null || data.getData ( ) == null )
		{Snackbar.make ( v, R.string.source_file_cannot_be_empty, Snackbar.LENGTH_LONG ).show ( );
			return;}
		try
		{
			//OGG与OGV拥有相同的magic number
			srcfile = data.getData ( );

			if ( !Utils.FORMAT_OGG.equals ( Utils.identifyFormat ( getActivity ( ).getContentResolver ( ).openInputStream ( srcfile ), true ) ) )
			{
				srcfile = null;
				Snackbar.make ( v, R.string.not_a_ogv_file, Snackbar.LENGTH_LONG ).show ( );
			}

		}
		catch (IOException e)
		{
			srcfile = null;
			Snackbar.make ( v, R.string.failed, Snackbar.LENGTH_LONG ).show ( );
		}
		if ( srcfile != null )
		{
			file.setText ( srcfile.getPath ( ) );
		}

	}


}
