package com.CHH2000day.navalcreed.modhelper;
import android.support.v7.app.*;
import android.os.*;
import android.view.*;
import android.support.v7.widget.*;
import android.content.*;
import android.widget.RelativeLayout;
import android.support.v4.app.*;
import android.support.design.widget.*;

public class ModPackageInstallerActvity extends AppCompatActivity
{
    private String mPkgPath;
	private static final int ID_MAINVIEW=R.id.modpackageinstaller_main_mainview;

	@Override
	protected void onCreate ( Bundle savedInstanceState )
	{
		// TODO: Implement this method
		super.onCreate ( savedInstanceState );
		//初始化UI
		setContentView(R.layout.modpackageinstaller_main);
        Toolbar mtoolbar = (Toolbar) findViewById(R.id.modinstallertoolbar);
		setSupportActionBar(mtoolbar);
        RelativeLayout mrl = (RelativeLayout) findViewById(ID_MAINVIEW);
		if(getIntent().getData()!=null){
		    mPkgPath=getIntent().getData().getPath();
		}
		if(getIntent().getData()==null||mPkgPath==null||mPkgPath.equals("")){
		    Snackbar.make(mrl,"beta",Snackbar.LENGTH_LONG).show();
			//如果要安装的文件为空，报错
			//测试，暂不使用
			/*
			AlertDialog.Builder adb=new AlertDialog.Builder(this);
			adb.setTitle("错误")
				.setMessage("选择的文件不存在")
				.setPositiveButton ( "退出", new DialogInterface.OnClickListener ( ){

					@Override
					public void onClick (DialogInterface p1, int p2)
					{
						finish();
						// TODO: Implement this method
					}
				} )
				.setCancelable(false);
			AlertDialog ad=adb.create();
			ad.setCanceledOnTouchOutside(false);
			ad.show();
			*/
		}
		}
	@Override
	public boolean onCreateOptionsMenu ( Menu menu )
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater ( ).inflate ( R.menu.main, menu );
		return true;
	}

	@Override
	public boolean onOptionsItemSelected ( MenuItem item )
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId ( );

		//noinspection SimplifiableIfStatement
		if ( id == R.id.action_exit )
		{
			exit ( );
		}

		return true;
	}
	public void exit ( )
	{
		AlertDialog.Builder adb=new AlertDialog.Builder ( this );
		adb.setTitle ( "确定" )
			.setMessage ( "是否退出？" )
			.setPositiveButton ( "是", new DialogInterface.OnClickListener ( ){

				@Override
				public void onClick ( DialogInterface p1, int p2 )
				{
					finish();
					// TODO: Implement this method
				}
			} )
			.setNegativeButton ( "否", null )
			.create ( )
			.show ( );
	}
	
}
