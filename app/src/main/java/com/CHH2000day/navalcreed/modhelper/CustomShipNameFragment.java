package com.CHH2000day.navalcreed.modhelper;

import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.snackbar.Snackbar;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;

public class CustomShipNameFragment extends ModFragment
{

	@Override
	public boolean uninstallMod ()
	{
		// TODO: Implement this method
		return false;
	}

    private static final String res_url = "https://static.CHH2000day.com/nc/customshipname_v22.patch";
	private View v;
    private String path;
    private File file;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		v = inflater.inflate(R.layout.antihexie_fragment, null);
		path = new StringBuilder()
			.append(((ModHelperApplication)getActivity().getApplication()).getResFilesDirPath())
			.append(File.separatorChar)
			.append("datas")
			.append(File.separatorChar)
			.append("customnames.lua").toString();
        file = new File(path);
        if (!file.exists()) {
            Utils.ensureFileParent(file);
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        CustomShipNameHelper.getInstance().init(file);
		Button exec = v.findViewById(R.id.antihexiefragmentButtonExec);

		exec.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					final AlertDialog.Builder adb=new AlertDialog.Builder(getActivity());
					adb.setTitle("请稍等");
					adb.setMessage("正在进行反和谐");
					adb.setCancelable(false);
					final AlertDialog ad=adb.create();
					ad.setCanceledOnTouchOutside(false);
					ad.show();
					final File f = new File(path);
					if (f.exists() && f.isFile()) {
						f.delete();
					}
					new Thread(){
						public void run(){
							Looper.prepare();
							try
							{
                                Request r = new Request.Builder().url(res_url).build();
                                Response response = OKHttpHelper.getClient().newCall(r).execute();
                                if (CustomShipNameHelper.getInstance().patch(response.body().source(), file)) {
                                    adb.setMessage(R.string.success)
                                            .setTitle(R.string.success).
                                            setPositiveButton(R.string.ok, null)
                                            .create().show();
                                } else {
                                    adb.setMessage(R.string.failed)
                                            .setTitle(R.string.failed)
                                            .setPositiveButton(R.string.ok, null)
                                            .create().show();
                                }

								
							}
							catch (IOException e)
							{
								Logger.e(e,e.getLocalizedMessage());
								adb.setMessage(R.string.failed)
									.setTitle(R.string.failed)
									.setPositiveButton(R.string.ok,null)
									.create().show();
							}
							finally{
								ad.dismiss();
								Looper.loop();
							}
						}
					}.start();
							
							
				// TODO: Implement this method
				}

			
			
			
			/*AlertDialog.Builder adb=new AlertDialog.Builder(getActivity());
			 adb.setTitle("提示")
			 .setMessage("该功能尚未实现")
			 .setPositiveButton("确定",null)
			 .create()
			 .show();*/
			//throw new RuntimeException("debug");

			/*final MediaHelper mh=MediaHelper.newInstance();
			 mh.setEncodeType(MediaFormat.MIMETYPE_AUDIO_OPUS);
			 mh.setIOPath("/sdcard/nonnon.mp3","/sdcard/nonon.ogg");
			 mh.setOnCompleteListener(new MediaHelper.OnCompleteListener(){

			 @Override
			 public void completed()
			 {Snackbar.make(v,"completed",Snackbar.LENGTH_LONG).show();
			 mh.release();
			 // TODO: Implement this method
			 }
			 });
			 mh.prepare();
			 mh.startAsync();*/
			// TODO: Implement this method
		});
		exec.setOnLongClickListener(p1 -> {
			final File f = new File(path);
			AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
			adb.setTitle(R.string.notice)
					.setMessage(R.string.confirm_to_remove_all_changes)
					.setNegativeButton(R.string.cancel, null)
					.setPositiveButton(R.string.remove_changes, (dialogInterface, p2) -> {
						if (f.isFile()) {
							f.delete();
						}
						Snackbar.make(v, R.string.success, Snackbar.LENGTH_LONG).show();
						// TODO: Implement this method
					})
					.create()
					.show();
			// TODO: Implement this method
			return true;
		});
        showAd(v);
		// TODO: Implement this method
		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
        //showAd(v);
	}
}
