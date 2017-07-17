package com.CHH2000day.navalcreed.modhelper;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.support.v4.app.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import android.widget.AdapterView.*;
import android.support.v7.app.*;
import android.support.design.widget.*;

public class CrewPicReplacerFragment extends Fragment
{
	private View v;
	private Bitmap ba;
	private int selectedcountry=0,selectedcrew=0;
	private Spinner country,num;
	private Button selpic,updatepic,removepic;
	private TextView selectedpic;
	private String[] countrys={"usa","japan","uk","china","italy","france","ussr","german"};
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{v = inflater.inflate(R.layout.crew_pic_replacer, null);
		country = (Spinner)v.findViewById(R.id.crewpicreplacerSpinnerCountry);
		country.setOnItemSelectedListener(new OnItemSelectedListener(){

				@Override
				public void onItemSelected(AdapterView<?> p1, View p2, int p3, long p4)
				{
					selectedcountry = p3;
					/*if(p3==3){
						Snackbar.make(v,"该选项可能存在bug",Snackbar.LENGTH_LONG).show();
					}*/
					// TODO: Implement this method
				}

				@Override
				public void onNothingSelected(AdapterView<?> p1)
				{
					// TODO: Implement this method
				}
			});

		num = (Spinner)v.findViewById(R.id.crewpicreplacerSpinnerCrew);
		num.setOnItemSelectedListener(new OnItemSelectedListener(){

				@Override
				public void onItemSelected(AdapterView<?> p1, View p2, int p3, long p4)
				{selectedcrew = p3;
					// TODO: Implement this method
				}

				@Override
				public void onNothingSelected(AdapterView<?> p1)
				{
					// TODO: Implement this method
				}
			});
		selpic = (Button)v.findViewById(R.id.crewpicreplacerButtonSelectPic);
		selpic.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
					intent.setType("image/*");
					startActivityForResult(intent, 2);

					// TODO: Implement this method
				}
			});
		removepic = (Button)v.findViewById(R.id.crewpicreplacerButtonRemove);
		removepic.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{AlertDialog.Builder adb=new AlertDialog.Builder(getActivity());
					adb.setTitle("确认")
						.setMessage("确认要移除更改吗？")
						.setPositiveButton("移除", new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface p1, int p2)
							{if (getFile(selectedcountry, selectedcrew).delete())
								{
									Snackbar.make(v, "更改已移除", Snackbar.LENGTH_LONG).show();
								}
								else
								{Snackbar.make(v, "更改移除失败", Snackbar.LENGTH_LONG).show();}
								
								// TODO: Implement this method
							}
						})
						.setNegativeButton("取消", null)
						.create()
						.show();
					// TODO: Implement this method
				}
			});
		removepic.setOnLongClickListener(new OnLongClickListener(){

				@Override
				public boolean onLongClick(View p1)
				{AlertDialog.Builder adb=new AlertDialog.Builder(getActivity());
					adb.setTitle("确认")
						.setMessage("确认要所有移除更改吗？")
						.setPositiveButton("移除", new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface p1, int p2)
							{if (Utils.delDir(getFile(selectedcountry, selectedcrew).getParentFile()))
								{
									Snackbar.make(v, "所有更改已移除", Snackbar.LENGTH_LONG).show();
								}
								else
								{Snackbar.make(v, "更改移除失败", Snackbar.LENGTH_LONG).show();}
								// TODO: Implement this method
							}
						})
						.setNegativeButton("取消", null)
						.create()
						.show();

					// TODO: Implement this method
					return true;
				}
			});
		updatepic = (Button)v.findViewById(R.id.crewpicreplacerButtonReplace);
		updatepic.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{if(null==ba){
					Snackbar.make(v,"源文件不能为空",Snackbar.LENGTH_LONG).show();
					return;
					}try
					{File out=getFile(selectedcountry, selectedcrew);
					if(!out.getParentFile().exists()){out.getParentFile().mkdirs();}
						FileOutputStream fos=new FileOutputStream(out);
						ba.compress(Bitmap.CompressFormat.PNG,100,fos);
						fos.flush();
						fos.close();
						Snackbar.make(v,"操作完成",Snackbar.LENGTH_LONG).show();
					}
					catch (Exception e)
					{
						Snackbar.make(v,e.getMessage(),Snackbar.LENGTH_LONG).show();
					}
					// TODO: Implement this method
				}
			});
		selectedpic = (TextView)v.findViewById(R.id.crewpicreplacerSelectedFile);
		
		// TODO: Implement this method
		return v;
	}

	private File getFile(int country, int num)
	{
		return new File(getFilePath(country, num));
	}
	private String getFilePath(int country, int num)
	{
		return new StringBuilder()
			.append(((ModHelperApplication)getActivity().getApplication()).getResFilePath())
			.append("/files/pic/crewhead/")
			.append(countrys[country])
			.append("/")
			.append((num+1))
			.append(".png")
			.toString();
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// TODO: Implement this method
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 2)
		{
			try
			{
				if(data==null){
					Snackbar.make(v,"文件为空！",Snackbar.LENGTH_LONG).show();
					return;
				}
				ba = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(data.getData()));
				selectedpic.setText(data.getData().toString());
			}
			catch (FileNotFoundException e)
			{e.printStackTrace();}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}


}
