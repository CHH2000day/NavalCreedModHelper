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

public class AntiHexieFragment extends Fragment
{
	private View v;
	private Button exec;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		v = inflater.inflate(R.layout.antihexie_fragment, null);
		exec = (Button)v.findViewById(R.id.antihexiefragmentButtonExec);

		exec.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					Snackbar.make(v,"操作开始，在出现提示前请勿关闭程序",Snackbar.LENGTH_LONG).show();
					String path=new StringBuilder()
						.append(((ModHelperApplication)getActivity().getApplication()).getResFilePath())
						.append("/files/datas")
						.append("customnames.lua").toString();
					final File f=new File(path);
					if (f.exists() && f.isFile())
					{
						f.delete();
					}
					if (!f.getParentFile().exists())
					{
						f.getParentFile().mkdirs();
					}
					BmobQuery<UniversalObject> query=new BmobQuery<UniversalObject>();
					query.getObject(StaticData.DATA_ID_ANTIHEXIE, new QueryListener<UniversalObject>(){

							@Override
							public void done(UniversalObject p1, BmobException p2)
							{
								final AlertDialog.Builder adb=new AlertDialog.Builder(getActivity());

								if (p2 != null)
								{
									adb.setMessage(p2.getMessage())
										.setTitle("失败")
										.setPositiveButton("确定", null)
										.create().show();
									return;

								}
								BmobFile bf=p1.getPackagefile();
								if (bf != null)
								{
									bf.download(f, new DownloadFileListener(){

											@Override
											public void done(String p1, BmobException p2)
											{if (p2 != null)
												{
													adb.setTitle("错误")
														.setMessage(p2.getMessage())
														.setPositiveButton("确定", null)
														.create().show();
													return;
												}
												adb.setMessage("操作成功")
													.setTitle("成功")
													.setPositiveButton("确定", null)
													.create().show();

												// TODO: Implement this method
											}

											@Override
											public void onProgress(Integer p1, long p2)
											{
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
				}
			});
		// TODO: Implement this method
		return v;
	}
}
