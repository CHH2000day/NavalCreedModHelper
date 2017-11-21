package com.CHH2000day.navalcreed.modhelper;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.support.v4.app.*;
import android.support.v7.app.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.widget.AdapterView.*;
import java.io.*;

import android.view.View.OnClickListener;
import android.support.design.widget.*;

public class BGReplacerFragment extends FunctionFragment
{

	
	private static final String[] filename={"loadingbg1.jpg","loadingbg2.jpg","loadingbg3.jpg"};
	private static final String[] cateoty={"loading","loadingmap","matching"};
	private String abs_path/*="/sdcard/Android/data/com.tencent.navalcreed/files/pic"*/;
	private Spinner cateory,file;
	private TextView picname;
	private Button btnrm,update,selpic;
	private Bitmap ba;
	private int cat=0,filepos=0;
	private View v;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		abs_path = new StringBuilder()
					.append(((ModHelperApplication)getActivity().getApplication()).getResFilesDirPath())
					.append(File.separatorChar)
					.append("pic")
					.toString();
		v=inflater.inflate(R.layout.bgreplacer_fragment, null);
		cateory = (Spinner)v.findViewById(R.id.bgreplacerSpinner1);
		cateory.setOnItemSelectedListener(new OnItemSelectedListener(){

				@Override
				public void onItemSelected(AdapterView<?> p1, View p2, int p3, long p4)
				{
					cat = p3;
					// TODO: Implement this method
				}

				@Override
				public void onNothingSelected(AdapterView<?> p1)
				{
					// TODO: Implement this method
				}
			});
		file = (Spinner)v.findViewById(R.id.bgreplacerSpinner2);

		file.setOnItemSelectedListener(new OnItemSelectedListener(){

				@Override
				public void onItemSelected(AdapterView<?> p1, View p2, int p3, long p4)
				{
					filepos = p3;
					// TODO: Implement this method
				}

				@Override
				public void onNothingSelected(AdapterView<?> p1)
				{
					// TODO: Implement this method
				}
			});
		picname = (TextView)v.findViewById(R.id.bgreplacerPic);
		btnrm = (Button)v.findViewById(R.id.bgreplacer_remove);
		update = (Button)v.findViewById(R.id.bgreplacerbtn_update);
		selpic = (Button)v.findViewById(R.id.bgreplacerbtn_select);
		selpic.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
					intent.setType("image/*");
					startActivityForResult(intent, 1);
					// TODO: Implement this method
				}
			});
		btnrm.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{AlertDialog.Builder adb=new AlertDialog.Builder(getActivity());
					adb.setTitle("确认要移除更改么？");
					adb.setNegativeButton("取消", null);
					adb.setPositiveButton("确认", new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface p1, int p2)
							{removechanges();
								// TODO: Implement this method
							}
						});

					adb.create().show();
					// TODO: Implement this method
				}
			});
		update.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{if (ba == null)
					{
						Snackbar.make(v, "源文件不能为空！", Snackbar.LENGTH_LONG).show();
					}
					else
					{
						File parent=new File(abs_path, cateoty[cat]);
						File target=new File(parent, filename[filepos]);
						if (!parent.exists())
						{
							parent.mkdirs();
						}
						try
						{
							FileOutputStream fos =new FileOutputStream(target);
							ba.compress(Bitmap.CompressFormat.JPEG, 100, fos);
							fos.flush();
							fos.close();
							Snackbar.make(v, "操作完成", Snackbar.LENGTH_LONG).show();
						}
						catch (Exception e)
						{Snackbar.make(v, e.getMessage(), Snackbar.LENGTH_LONG).show();}
					}
					// TODO: Implement this method
				}
			});

		// TODO: Implement this method
		return v;
	}
	private void removechanges()
	{
		Utils.delDir(new File(abs_path));
		Snackbar.make(v, "更改已移除", Snackbar.LENGTH_LONG).show();
	}

	@Override
	public void onDestroy ()
	{
		// TODO: Implement this method
		if(ba!=null){
			ba.recycle();
			//手动释放以防止Bitmap未被释放
			
		}
		super.onDestroy ( );
	}
	
	@Override
	public boolean installMod (int typenum, int num, byte[] deceyptedFileData) throws IOException
	{
		// TODO: Implement this method
		return false;
		//Not finished
	}
	
	

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// TODO: Implement this methodsuper.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1)
		{
			if(data==null){
				Snackbar.make(v,"文件为空！",Snackbar.LENGTH_LONG).show();
				return;}
			try
			{
				if(ba!=null){
					ba.recycle();
					//手动释放以防止Bitmap未被释放
				}
				ba = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(data.getData()));
				picname.setText(data.getData().toString());
			}
			catch (FileNotFoundException e)
			{e.printStackTrace();}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}


}
