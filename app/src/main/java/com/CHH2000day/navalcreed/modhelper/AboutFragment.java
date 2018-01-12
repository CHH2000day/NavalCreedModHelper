package com.CHH2000day.navalcreed.modhelper;
import android.support.v4.app.*;
import android.view.*;
import android.os.*;
import android.widget.*;
import android.view.View.*;
import android.content.res.*;
import okio.*;
import java.io.*;
import android.support.v7.app.*;

public class AboutFragment extends Fragment
{
	private View v;
	private Button license;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{	v=inflater.inflate(R.layout.about_fragment,null);
		license=(Button)v.findViewById(R.id.aboutfragmentLicense);
		license.setOnClickListener ( new OnClickListener ( ){

				@Override
				public void onClick ( View p1 )
				{
					
					try
					{
						Source s=Okio.source ( getResources ( ).getAssets ( ).open ( "LICENSE" ) );
						BufferedSource bs=Okio.buffer(s);
						AlertDialog.Builder adb=new AlertDialog.Builder(getActivity());
						adb.setTitle("关于开源许可")
							.setMessage(bs.readUtf8())
							.setPositiveButton("确定",null);
						bs.close();
						adb.create().show();
					}
					catch (IOException e)
					{}
					// TODO: Implement this method
				}
			} );
		// TODO: Implement this method
		return v;
	}
}
