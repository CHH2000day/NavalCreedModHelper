package com.CHH2000day.navalcreed.modhelper;

import android.content.*;
import android.content.pm.*;
import android.util.*;
import cn.bmob.v3.exception.*;
import cn.bmob.v3.listener.*;
import java.io.*;
import android.widget.*;
import android.os.*;
import okio.*;

public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler
{
	private static String app_ver;
	private static int app_ver_int;
	private File parent;
	private static final String PREF_NAME="bugly";
	private static final String ERRTIME="errtime";
	private String type="unknown";
	private static UncaughtExceptionHandler exceptionHandler;
	private UncaughtExceptionHandler()
	{}
	public static UncaughtExceptionHandler getInstance()
	{
		if (exceptionHandler == null)
			exceptionHandler = new UncaughtExceptionHandler();
		return exceptionHandler;
	}

	private Context ctx;

	public void init(Context ctx) throws PackageManager.NameNotFoundException {
		Context ce = ctx;
		boolean inited = false;
		if (inited) return;
		this.ctx = ctx;
		Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
		app_ver = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;

		app_ver_int = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionCode;
		parent = ctx.getExternalFilesDir("err-log");
		type = ctx.getString(R.string.buildtype);
		checkUnpostedBug();

	}

	private void checkUnpostedBug()
	{
		final long time=ctx.getSharedPreferences(PREF_NAME, 0).getLong(ERRTIME, -1);
		if (time == -1)
		{return;}
		else
		{
			Log.d("Bugly", "Posting unposted log");
			Toast.makeText(ctx, "提交之前错误日志中", Toast.LENGTH_LONG).show();
			final File tgt=new File(parent, new StringBuilder().append(time).append(".log").toString());
			try
			{
				FileInputStream fis =new FileInputStream(tgt);
				String testerid=ctx.getSharedPreferences(PREF_NAME, 0).getString(StaticData.KEY_TESTER_ID, "");
				new Bugly().postBug(testerid + "\n" + new String(Utils.readAllbytes(fis)), time, app_ver, app_ver_int).save(new SaveListener<String>(){

						@Override
						public void done(String p1, BmobException p2)
						{
							if (p2 == null)
							{
								ctx.getSharedPreferences(PREF_NAME, 0).edit().putLong(ERRTIME, -1).apply();
							}
							else
							{p2.printStackTrace();}
							// TODO: Implement this method
						}
					});}
			catch (Exception e)
			{
				e.printStackTrace();
			}

		}
	}
	@Override
	public void uncaughtException(Thread p1, Throwable p2)
	{

//p1:出错线程
//p2:错误

		final StringWriter stringWriter=new StringWriter();
		PrintWriter printWriter=new PrintWriter(stringWriter);
		p2.printStackTrace(printWriter);
//保存错误
		final long time=System.currentTimeMillis();

		String filename=String.valueOf(time) + ".log";
		final File file=new File(parent, filename);
		try
		{
			/*
			 FileOutputStream fos=new FileOutputStream ( file );
			 fos.write ( stringWriter.toString ( ).getBytes ( ) );
			 fos.close ( );
			 */
			Sink s=Okio.sink(file);
			BufferedSink bs=Okio.buffer(s);
			bs.writeUtf8(String.format("Device:%s (%s)%n", Build.MODEL, Build.DEVICE));
			bs.writeUtf8(String.format("App ver:%s %n", app_ver));
			bs.writeUtf8(String.format("App ver_int:%d %n", app_ver_int));
			bs.writeUtf8(String.format("Build type:%s %n", type));
			bs.writeUtf8(stringWriter.toString());
			bs.flush();
			bs.close();
			s.close();


		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		ctx.getSharedPreferences(PREF_NAME, 0).edit().putLong(ERRTIME, time).apply();
		android.os.Process.killProcess(android.os.Process.myPid());
		/*F
		 Disabled for satble reason.
		 new Thread(){
		 public void run(){
		 Looper.prepare();
		 AlertDialog.Builder adb=new AlertDialog.Builder(ctx);
		 adb.setTitle("Oops")
		 .setMessage("正在上传错误日志")
		 ;
		 final AlertDialog ad=adb.create();
		 ad.show();
		 new Bugly().postBug(new String(stringWriter.toString()), time, app_ver).save(new SaveListener<String>(){

		 @Override
		 public void done(String p1, BmobException p2)
		 {
		 Log.w("log", "log");
		 if (p2==null)
		 {
		 ctx.getSharedPreferences(PREF_NAME, 0).edit().putLong(ERRTIME, -1).commit();
		 ad.dismiss();
		 }
		 System.exit(0);
		 // TODO: Implement this method
		 }
		 });
		 Looper.loop();
		 }
		 }.start();
		 */
		/*
		 new Thread() {
		 @Override
		 public void run()
		 {
		 //该部分被取消因为还未适配
		 /*
		 Looper.prepare();
		 String s =ctx.getString(R.string.crash_msg_head);
		 String t=ctx.getString(R.string.crash_msg_tail);
		 new AlertDialog.Builder(ctx).setTitle(R.string.notice)
		 .setCancelable(false)
		 .setMessage(s+file.getAbsolutePath()+". "+t).setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {


		 @Override
		 public void onClick(DialogInterface dialog, int which) {
		 android.os.Process.killProcess(android.os.Process.myPid());
		 }
		 })
		 .create().show();
		 Looper.loop();*/
		//	}
		//	}.start();

	}
}


