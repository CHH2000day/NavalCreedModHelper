package com.CHH2000day.navalcreed.modhelper;

import android.content.*;
import android.net.*;
import android.os.*;
import android.support.design.widget.*;
import android.support.v4.app.*;
import android.support.v4.view.*;
import android.support.v4.widget.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.util.*;
import android.view.*;
import cn.bmob.v3.*;
import cn.bmob.v3.datatype.*;
import cn.bmob.v3.exception.*;
import cn.bmob.v3.listener.*;
import java.io.*;
import java.util.*;

public class Main extends AppCompatActivity
{
	private ViewPager mViewPager;
	private TabLayout mTabLayout;
	private FragmentPagerAdapter mAdapter;
	private List<Fragment> fragments;
	private List<String> titles;
	private LayoutInflater li;
	private Handler mupdateHandler;
	private static final String GENERAL="general";
	private static final String ANNOU_VER="annover";
	private CrewPicReplacerFragment mCrewPicReplacerFragment;
	private BGReplacerFragment mBGReplacerFragment;
	private BGMReplacerFragment mBGMReplacerFragment;
	private CustomShipNameFragment mAntiHexieFragment;

	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		super.onCreate ( savedInstanceState );
		setContentView ( R.layout.main );
		Toolbar toolbar = (Toolbar) findViewById ( R.id.toolbar );
		setSupportActionBar ( toolbar );
		mupdateHandler = new Handler ( ){
			public void handleMessage (final Message msg)
			{
				AlertDialog.Builder adb=(AlertDialog.Builder)msg.obj;
				adb.create ( ).show ( );


			}
		};
		/*禁用FloatingActionButton
		 FloatingActionButton fab = (FloatingActionButton) findViewById ( R.id.fab );
		 fab.setOnClickListener ( new View.OnClickListener ( ) {
		 @Override
		 public void onClick ( View view )
		 {
		 Snackbar.make ( view, "Replace with your own action", Snackbar.LENGTH_LONG )
		 .setAction ( "Action", null ).show ( );
		 }
		 } );
		 */
		/*DrawerLayout drawer = (DrawerLayout) findViewById ( R.id.drawer_layout );
		 ActionBarDrawerToggle toggle = new ActionBarDrawerToggle (
		 this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close );
		 toggle.syncState ( );
		 drawer.setDrawerListener ( toggle );*/
		li = LayoutInflater.from ( this );
		/*禁用NavigationView
		 NavigationView navigationView = (NavigationView) findViewById ( R.id.nav_view );
		 navigationView.setNavigationItemSelectedListener ( this );*/
		//配置ViewPager与TabLayout
		mViewPager = (ViewPager)findViewById ( R.id.viewPager );
		mTabLayout = (TabLayout)findViewById ( R.id.tabLayout );
		//构造Fragment实例
		mBGReplacerFragment = new BGReplacerFragment ( );
		mCrewPicReplacerFragment = new CrewPicReplacerFragment ( );
		mAntiHexieFragment = new CustomShipNameFragment ( );
		mBGMReplacerFragment = new BGMReplacerFragment ( );
		//进行数据配置
		fragments = new ArrayList<Fragment> ( );
		fragments.add ( mBGReplacerFragment );
		fragments.add ( mCrewPicReplacerFragment );
		fragments.add ( mAntiHexieFragment );
		fragments.add ( mBGMReplacerFragment );
		fragments.add ( new AboutFragment ( ) );
		titles = new ArrayList<String> ( );
		titles.add ( "背景替换" );
		titles.add ( "船员头像修改" );
		titles.add ( "反和谐" );
		titles.add ( "BGM替换" );
		titles.add ( "关于" );
		mAdapter = new ViewPagerAdapter ( getSupportFragmentManager ( ), fragments, titles );
		mViewPager.setAdapter ( mAdapter );
		mTabLayout.setupWithViewPager ( mViewPager );

		new UpdateThread ( ).start ( );
		new AnnouncementThread ( ).start ( );
	}


	@Override
	public void onBackPressed ()
	{
		if (!((ModHelperApplication)getApplication ( )).isMainPage ( ))
		{
			super.onBackPressed ( );
		}
		else
		{
			exit ( );
		}
	}
	public BGReplacerFragment getBGReplacerFragment ()
	{
		return mBGReplacerFragment;
	}
	public BGMReplacerFragment getBGMReplacerFragment ()
	{
		return mBGMReplacerFragment;
	}
	public CrewPicReplacerFragment getCrewPicReplacerFragment ()
	{
		return mCrewPicReplacerFragment;
	}
	public CustomShipNameFragment getCustomShipNameFragment ()
	{
		return mAntiHexieFragment;
	}

	public void exit ()
	{
		AlertDialog.Builder adb=new AlertDialog.Builder ( this );
		adb.setTitle ( "确定" )
			.setMessage ( "是否退出？" )
			.setPositiveButton ( "是", new DialogInterface.OnClickListener ( ){

				@Override
				public void onClick (DialogInterface p1, int p2)
				{
					android.os.Process.killProcess ( android.os.Process.myPid ( ) );
					// TODO: Implement this method
				}
			} )
			.setNegativeButton ( "否", null )
			.create ( )
			.show ( );
	}
	@Override
	public boolean onCreateOptionsMenu (Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater ( ).inflate ( R.menu.main, menu );
		return true;
	}

	@Override
	public boolean onOptionsItemSelected (MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId ( );

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_exit)
		{
			exit ( );
		}

		return true;
	}
	/*
	 @SuppressWarnings("StatementWithEmptyBody")
	 @Override
	 public boolean onNavigationItemSelected(MenuItem item)
	 {
	 // Handle navigation view item clicks here.
	 int id = item.getItemId();

	 if (id == R.id.nav_camera)
	 {
	 // Handle the camera action
	 }
	 else if (id == R.id.nav_gallery)
	 {

	 }
	 else if (id == R.id.nav_slideshow)
	 {

	 }
	 else if (id == R.id.nav_manage)
	 {

	 }
	 else if (id == R.id.nav_share)
	 {

	 }
	 else if (id == R.id.nav_send)
	 {

	 }

	 DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
	 drawer.closeDrawer(GravityCompat.START);
	 return true;
	 }*/

	protected class UpdateThread extends Thread
	{

		@Override
		public void run ()
		{
			BmobQuery<UniversalObject> query=new BmobQuery<UniversalObject> ( );

			query.getObject ( StaticData.getDataid ( ), new QueryListener<UniversalObject> ( ){

					@Override
					public void done (final UniversalObject universalobj, BmobException p2)
					{
						if (p2 != null)
						{
							Log.w ( "Updater", "Failed to get update data" );
							return;
						}
						//如果为测试版，检测服务器端是否允许测试
						if (BuildConfig.DEBUG)
						{
							if (!universalobj.isAvail ( ))
							{
								AlertDialog.Builder adb=new AlertDialog.Builder ( Main.this );
								adb.setTitle ( "提示" )
									.setMessage ( "测试未开始" )
									.setCancelable ( false )
									.setPositiveButton ( "退出", new DialogInterface.OnClickListener ( ){

										@Override
										public void onClick (DialogInterface p1, int p2)
										{
											System.exit ( 0 );
											// TODO: Implement this method
										}
									} );
								mupdateHandler.sendMessage ( mupdateHandler.obtainMessage ( 0, adb ) );
								return;

							}
						}
						int serverver=universalobj.getVersion ( ).intValue ( );
						try
						{
							int currver=getPackageManager ( ).getPackageInfo ( getPackageName ( ), 0 ).versionCode;
							if (serverver <= currver)
							{
								return;
							}
							AlertDialog.Builder adb=new AlertDialog.Builder ( Main.this );
							adb.setTitle ( "发现更新" )
								.setMessage ( universalobj.getChangelog ( ) )
								.setNegativeButton ( "取消", null )
								.setPositiveButton ( "更新", new DialogInterface.OnClickListener ( ){

									@Override
									public void onClick (DialogInterface p1, int p2)
									{BmobFile tgtfile=universalobj.getPackagefile ( );
										if (tgtfile == null)
										{
											return;
										}
										Snackbar.make ( mViewPager, "开始下载", Snackbar.LENGTH_LONG ).show ( );
										final File distfile=new File ( getExternalCacheDir ( ), "update.apk" );
										tgtfile.download ( distfile, new DownloadFileListener ( ){

												@Override
												public void done (String p1, BmobException p2)
												{
													Snackbar.make ( mViewPager, "下载完成", Snackbar.LENGTH_LONG ).show ( );
													Intent i=new Intent ( Intent.ACTION_VIEW );
													i.setFlags ( Intent.FLAG_ACTIVITY_NEW_TASK );
													i.setDataAndType ( Uri.fromFile ( distfile ), "application/vnd.android.package-archive" );
													startActivity ( i );
													// TODO: Implement this method
												}

												@Override
												public void onProgress (Integer p1, long p2)
												{
													// TODO: Implement this method
												}
											} );
										// TODO: Implement this method
									}
								} );
							mupdateHandler.sendMessage ( mupdateHandler.obtainMessage ( 0, adb ) );
							// TODO: Implement this method
						}
						catch (Exception e)
						{e.printStackTrace ( );}}
				} );
			// TODO: Implement this method
			super.run ( );

		}

	}
	private class AnnouncementThread extends Thread
	{

		@Override
		public void run ()
		{
			BmobQuery<BmobMessage> query=new BmobQuery<BmobMessage> ( );
			query.getObject ( StaticData.DATA_ID_ANNOUNCEMENT, new QueryListener<BmobMessage> ( ){

					@Override
					public void done (final BmobMessage bmobmsg, BmobException p2)
					{
						if (p2 != null)
						{
							p2.printStackTrace ( );
							return;
						}
						int id=bmobmsg.getmsgid ( );
						int currid=getSharedPreferences ( GENERAL, 0 ).getInt ( ANNOU_VER, -1 );
						if (id > currid)
						{
							getSharedPreferences ( GENERAL, 0 ).edit ( ).putInt ( ANNOU_VER, id ).commit ( );AlertDialog.Builder adb0=new AlertDialog.Builder ( Main.this );
							adb0.setTitle ( "公告" )
								.setMessage ( bmobmsg.getMessage ( ) )
								.setPositiveButton ( "确定", null )
								.setNegativeButton ( "复制", new DialogInterface.OnClickListener ( ){

									@Override
									public void onClick (DialogInterface p1, int p2)
									{ClipboardManager cmb = (ClipboardManager)getSystemService ( Context.CLIPBOARD_SERVICE );  
										cmb.setText ( bmobmsg.tocopy ( ).trim ( ) );  
										// TODO: Implement this method
									}
								} );

							mupdateHandler.sendMessage ( mupdateHandler.obtainMessage ( 1, adb0 ) );
						}
						// TODO: Implement this method
					}
				} );
			// TODO: Implement this method
			super.run ( );

		}

	}
}
