package com.CHH2000day.navalcreed.modhelper;
import android.os.*;
import android.support.v4.app.*;
import android.support.v7.app.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import cn.bmob.v3.*;
import java.io.*;
import cn.bmob.v3.listener.*;
import cn.bmob.v3.exception.*;
import cn.bmob.v3.datatype.*;
import android.support.design.widget.*;
import android.content.*;

public class CustomShipNameFragment extends ModFragment
{

	@Override
	public boolean uninstallMod ()
	{
		// TODO: Implement this method
		return false;
	}


	
	private View v;
    private String path;
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

		Button exec = v.findViewById(R.id.antihexiefragmentButtonExec);

		exec.setOnClickListener(p1 -> {
			Snackbar.make(v, "操作开始，在出现提示前请勿关闭程序", Snackbar.LENGTH_LONG).show();
			final File f = new File(path);
			if (f.exists() && f.isFile()) {
				f.delete();
			}
			if (!f.getParentFile().exists()) {
				f.getParentFile().mkdirs();
			}
			BmobQuery<UniversalObject> query = new BmobQuery<UniversalObject>();
			query.getObject(StaticData.DATA_ID_ANTIHEXIE, new QueryListener<UniversalObject>() {

				@Override
				public void done(UniversalObject p1, BmobException p2) {
					final AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());

					if (p2 != null) {
						adb.setMessage(p2.getMessage())
								.setTitle(R.string.failed)
								.setPositiveButton(R.string.ok, null)
								.create().show();
						return;

					}
					BmobFile bf = p1.getPackagefile();
					if (bf != null) {
						bf.download(f, new DownloadFileListener() {

							@Override
							public void done(String p1, BmobException p2) {
								if (p2 != null) {
									adb.setTitle(R.string.failed)
											.setMessage(p2.getMessage())
											.setPositiveButton(R.string.ok, null)
											.create().show();
									return;
								}
								adb.setMessage(R.string.success)
										.setTitle(R.string.success)
										.setPositiveButton(R.string.ok, null)
										.create().show();

								// TODO: Implement this method
							}

							@Override
							public void onProgress(Integer p1, long p2) {
								// TODO: Implement this method
							}
						});
					}
					// TODO: Implement this method
				}
			});
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
		// TODO: Implement this method
		return v;
	}
	
	
}
