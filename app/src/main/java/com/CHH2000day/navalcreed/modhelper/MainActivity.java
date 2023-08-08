package com.chh2000day.navalcreed.modhelper;

import android.app.Activity;

public class MainActivity extends Activity 
{/*
//Disabled since it's not needed.

	private static final String[] filename={"loadingbg1.jpg","loadingbg2.jpg","loadingbg3.jpg"};
	private static final String[] cateoty={"loading","loadingmap","matching"};
	private static final String abs_path="/sdcard/Android/data/com.tencent.navalcreed/files/pic";
	private Spinner cateory,file;
	private TextView picname;
	private Button btnrm,update,selpic;
	private Bitmap ba;
	private int cat=0,filepos=0;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		cateory=(Spinner)findViewById(R.id.mainSpinner1);
		cateory.setOnItemSelectedListener(new OnItemSelectedListener(){

				@Override
				public void onItemSelected(AdapterView<?> p1, View p2, int p3, long p4)
				{
					cat=p3;
					// TODO: Implement this method
				}

				@Override
				public void onNothingSelected(AdapterView<?> p1)
				{
					// TODO: Implement this method
				}
			});
		file=(Spinner)findViewById(R.id.mainSpinner2);

		file.setOnItemSelectedListener(new OnItemSelectedListener(){

				@Override
				public void onItemSelected(AdapterView<?> p1, View p2, int p3, long p4)
				{
					filepos=p3;
					// TODO: Implement this method
				}

				@Override
				public void onNothingSelected(AdapterView<?> p1)
				{
					// TODO: Implement this method
				}
			});
		picname=(TextView)findViewById(R.id.mainPic);
		btnrm=(Button)findViewById(R.id.remove);
		update=(Button)findViewById(R.id.btn_update);
		selpic=(Button)findViewById(R.id.btn_select);
		selpic.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("image/*");
				startActivityForResult(intent,1);
					// TODO: Implement this method
				}
			});
		btnrm.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{AlertDialog.Builder adb=new AlertDialog.Builder(MainActivity.this);
				adb.setTitle("确认要移除更改么？");
				adb.setNegativeButton("取消",null);
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
				{if(ba==null){
					Toast.makeText(MainActivity.this,"源文件不能为空！",Toast.LENGTH_LONG).show();
				}else{
					File parent=new File(abs_path,cateoty[cat]);
					File target=new File(parent,filename[filepos]);
					if(!parent.exists()){
						parent.mkdirs();
					}
					try
					{
						FileOutputStream fos =new FileOutputStream(target);
						ba.compress(Bitmap.CompressFormat.JPEG,100,fos);
						fos.flush();
						fos.close();
						Toast.makeText(MainActivity.this,"操作完成",Toast.LENGTH_LONG).show();
					}
					catch (Exception e)
					{Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();}
				}
					// TODO: Implement this method
				}
			});
    }
	private void removechanges(){
		delDir(new File(abs_path));
		Toast.makeText(this,"更改已移除",Toast.LENGTH_LONG).show();
	}
	public static boolean delDir(File f){
        if (f==null) return false;
        if (!f.exists()) return true;
        if (f.isDirectory()){
            File[] fs=f.listFiles();
            if (fs!=null){
                for(File e:fs) {
                    if (!delDir(e)) return false;
                }
            }
        }
        return f.delete();
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// TODO: Implement this method
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==1){
			try
			{
				ba = BitmapFactory.decodeStream(getContentResolver().openInputStream(data.getData()));
				picname.setText(data.getData().toString());
			}
			catch (FileNotFoundException e)
			{e.printStackTrace();}
		}
	}*/
	
}

