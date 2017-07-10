package com.CHH2000day.navalcreed.modhelper;
import android.os.*;
import android.support.v4.app.*;
import android.view.*;
import android.widget.*;
import android.view.View.*;
import android.support.v7.app.*;
import android.net.rtp.*;
import android.support.design.widget.*;
import java.io.*;

public class AntiHexieFragment extends Fragment
{
private View v;
private Button exec;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		v=inflater.inflate(R.layout.antihexie_fragment,null);
		exec=(Button)v.findViewById(R.id.antihexiefragmentButtonExec);
		
		exec.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{AlertDialog.Builder adb=new AlertDialog.Builder(getActivity());
				adb.setTitle("提示")
					.setMessage("该功能尚未实现")
					.setPositiveButton("确定",null)
					.create()
					.show();
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
