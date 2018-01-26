package com.CHH2000day.navalcreed.modhelper;
import android.support.v7.app.*;
import android.os.*;
import android.view.*;
import android.support.v7.widget.*;
import android.content.*;
import android.widget.RelativeLayout;
import android.support.v4.app.*;

public class ModPackageInstallerActvity extends AppCompatActivity
{
	private Toolbar mtoolbar;
	private RelativeLayout mrl;
	private String mcurrentFragmentTAG;
	private static final int ID_MAINVIEW=R.id.modpackageinstaller_main_mainview;
;
	@Override
	protected void onCreate ( Bundle savedInstanceState )
	{
		// TODO: Implement this method
		super.onCreate ( savedInstanceState );
		setContentView(R.layout.modpackageinstaller_main);
		mtoolbar=(Toolbar)findViewById(R.id.modinstallertoolbar);
		setSupportActionBar(mtoolbar);
		mrl=(RelativeLayout)findViewById(ID_MAINVIEW);
		
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
