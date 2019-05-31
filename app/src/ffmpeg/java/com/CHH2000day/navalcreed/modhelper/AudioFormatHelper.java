package com.CHH2000day.navalcreed.modhelper;
import java.io.*;
import java.util.*;

import android.annotation.TargetApi;
import android.media.*;
import android.os.*;
import android.net.*;
import android.content.*;
import android.media.MediaCodec.*;
import java.nio.*;
import okio.*;
import com.github.hiteshsondhi88.libffmpeg.*;
import com.github.hiteshsondhi88.libffmpeg.exceptions.*;
import android.text.*;

public class AudioFormatHelper
{
	//源文件
	protected File srcFile;
	//private File mTargetFile;
	//转码时的缓存文件
	private File cacheFile;

	private HashMap<File,Boolean> valids;
	private ArrayList<File> cachedFiles;
	protected Uri srcFileUri;
	protected Context mcontext;
	/*
	 //使用本地缓存，弃用该stream
	 private ByteArrayOutputStream decodeddata;
	 */
	private boolean isdecoded=false;
	private boolean isProcessed=false;
	private boolean useCacheFile=false;
	//需要用到的UI操作的部分
	private Handler mEmptyHandler;
	public static final int STATUS_START=1000;
	public static final int STATUS_LOADINGFILE=1001;
	public static final int STATUS_TRANSCODING=1002;
	public static final int STATUS_WRITING=1003;
	public static final int STATUS_DONE=1004;
	public static final int STATUS_ERROR=1024;
	//转码配置
	private int mBufferSize=256 * 1024; //缓存的音频大小
	//private int mSampleRate = 16000; //采样率，使用8000减少内存占用
	private int mSampleRate = 8000;
	private int mChannel = android.media.AudioFormat.CHANNEL_IN_STEREO; //立体声
	private int mEncoding = android.media.AudioFormat.ENCODING_PCM_16BIT;

	public static final int MODE_DENY_ALL_CACHE=-15;

	//文件格式magic number
	//.wav:RIFF
	public static final byte[] HEADER_WAV={ 82, 73, 70, 70 };
	//.ogg/.ogv:OGGV
	public static final byte[] HEADER_OGG={ 79, 103, 103, 83 };

	//返回的结果
	public static final String RESULT_OK="OK";
	private boolean isDone=false;
	private boolean hasError=false;
	public AudioFormatHelper(File audioFile, Context context) throws FileNotFoundException
	{

		//检查文件是否存在及可读
		if (!audioFile.exists() || !audioFile.canRead())
		{
			throw new FileNotFoundException("Audio file:" + audioFile.getPath() + " could not be found");
		}
		init();
		this.srcFile = audioFile;
		mcontext = context;
	}
	public AudioFormatHelper(Uri audioFilePath, Context context)
	{
		init();
		srcFileUri = audioFilePath;
		mcontext = context;
	}
	private void init()
	{
		cachedFiles = new ArrayList<File>();
		valids = new HashMap<File,Boolean>();
		mBufferSize = AudioRecord.getMinBufferSize(mSampleRate, mChannel, mEncoding);
		mEmptyHandler = new Handler(){

			@Override
			public void handleMessage(Message msg)
			{
				// TODO: Implement this method
				//DO:NOTHING
			}

		};
	}
	//转码至wav
	public String compressToWav(File targetFile)
	{
		return compressToWav(targetFile, mEmptyHandler);
	}
	public String compressToWav(final File targetFile, final Handler UIHandler)
	{
//		if (!targetFile.getParentFile().exists())
//		{
//			targetFile.getParentFile().mkdirs();
//		}
		String resultCode="";
		UIHandler.sendEmptyMessage(STATUS_START);
		useCacheFile = (srcFileUri != null);
		hasError = false;

		try
		{
			File cachedFile=getValidCacheFile();
			FormatHelperFactory.refreshCache(targetFile);
			if (isProcessed && cachedFile != null && cachedFile.exists())
			{
				UIHandler.sendEmptyMessage(STATUS_WRITING);
				Utils.copyFile(cachedFile, targetFile);
				UIHandler.sendEmptyMessage(STATUS_DONE);
				isDone = true;
			}
			else
			{
				if (useCacheFile)
				{
					srcFile = new File(mcontext.getCacheDir(), "cache.audio");
					Source source=Okio.source(mcontext.getContentResolver().openInputStream(srcFileUri));
					Sink sink=Okio.sink(srcFile);
					BufferedSink bs=Okio.buffer(sink);
					bs.writeAll(source);
					bs.flush();
					bs.close();
					source.close();

				}

				final FFmpeg ffmpeg=FFmpeg.getInstance(mcontext);
				try
				{
					UIHandler.sendEmptyMessage(STATUS_TRANSCODING);
					if (!targetFile.getParentFile().exists())
					{
						targetFile.getParentFile().mkdirs();
					}
					ffmpeg.execute(new String[]{"-y","-i",srcFile.getAbsolutePath(),targetFile.getAbsolutePath()}, new FFmpegExecuteResponseHandler(){

							@Override
							public void onSuccess(String p1)
							{
								UIHandler.sendEmptyMessage(STATUS_DONE);
								activeCache(targetFile);
								isProcessed = true;
								// TODO: Implement this method
							}

							@Override
							public void onProgress(String p1)
							{
								// TODO: Implement this method
							}

							@Override
							public void onFailure(String p1)
							{
								hasError = true;
								UIHandler.sendMessage(UIHandler.obtainMessage(STATUS_ERROR, new IOException(p1)));
								// TODO: Implement this method
							}

							@Override
							public void onStart()
							{
								// TODO: Implement this method
							}

							@Override
							public void onFinish()
							{
								isDone = true;
								if (useCacheFile && srcFile != null)
								{
									Utils.delDir(srcFile);
								}

								// TODO: Implement this method
							}
						});
				}
				catch (FFmpegCommandAlreadyRunningException e)
				{
					hasError = true;
					throw new RuntimeException(e);
				}
				/*ffmpeg.loadBinary(new FFmpegLoadBinaryResponseHandler(){

						@Override
						public void onFailure()
						{
							throw new RuntimeException("Failed to load ffmpeg lib");

							// TODO: Implement this method
						}

						@Override
						public void onSuccess()
						{
							
							// TODO: Implement this method
						}

						@Override
						public void onStart()
						{
							// TODO: Implement this method
						}

						@Override
						public void onFinish()
						{
							isDone = true;
							// TODO: Implement this method
						}
					});*/
			}
		}
		catch (Throwable e)
		{
			UIHandler.sendMessage(UIHandler.obtainMessage(STATUS_ERROR, e));
			hasError = true;
			isDone = true;
			resultCode = e.getMessage();
			if (useCacheFile && srcFile != null)
			{
				Utils.delDir(srcFile);
			}
		}

		while (!isDone)
		{
			try
			{
				Thread.sleep(10);
			}
			catch (InterruptedException e)
			{

			}
		}
		if (TextUtils.isEmpty(resultCode))
		{
			return RESULT_OK;
		}
		else
		{
			return resultCode;
		}

//		UIHandler.sendEmptyMessage(STATUS_START);
//		InputStream in=null;
//		File cachedFile=getValidCacheFile();
//		if (!isProcessed || cachedFile == null)
//		{	try
//			{
//				//验证源文件是否已为wav格式
//				if (srcFile != null)
//				{
//					in = new FileInputStream(srcFile);
//				}
//				else
//				{
//					in = mcontext.getContentResolver().openInputStream(srcFileUri);
//				}
//				byte[] b=new byte[4];
//				in.read(b);
//				if (Arrays.equals(b, HEADER_WAV))
//				{
//
//					//若是，直接读取为已解码数据，并跳过添加文件头
//					/*
//					 //使用本地缓存，弃用原方法
//					 //
//					 isUnneededtocompress = true;
//					 if(decodeddata==null){
//					 decodeddata=new ByteArrayOutputStream();
//					 }
//					 decodeddata.reset ( );
//					 decodeddata.write ( HEADER_WAV, 0, HEADER_WAV.length );
//					 byte[] cache=new byte[1024];
//					 int len;
//					 while ( ( len = in.read ( cache ) ) > 0 )
//					 {
//					 decodeddata.write ( cache, 0, len );
//					 }*/
//					isUnneededtocompress = true;
//				}
//				else
//				{
//					//解码数据
//					//boolean bl=decodeAudio ( null, UIHandler );
//					decodeAudio(UIHandler);
//				}
//			}
//			catch (Exception e)
//			{
//				UIHandler.sendMessage(UIHandler.obtainMessage(STATUS_ERROR, e));
//				return e.getMessage();
//			}
//		}
//		try
//		{
//			UIHandler.sendEmptyMessage(STATUS_WRITING);
//			FormatHelperFactory.refreshCache(targetFile);
//			//如果存在已处理好的缓存文件，直接复制
//			if (isProcessed && cachedFile != null && cachedFile.exists())
//			{
//				if (!targetFile.getParentFile().exists())
//				{
//					targetFile.getParentFile().mkdirs();
//				}
//				Utils.copyFile(cachedFile, targetFile);
//
//			}
//			else
//			{
//				Sink s=Okio.sink(targetFile);
//				BufferedSink f=Okio.buffer(s);
//				//添加文件头
//				//若源文件已为wav格式，直接读取源文件并写入
//				if (isUnneededtocompress)
//				{
//					f.write(HEADER_WAV);
//					byte[] cache=new byte[2048];
//					int len;
//					while ((len = in.read(cache)) != -1)
//					{
//						f.write(cache, 0, len);
//					}
//				}
//				else
//				{
//					//从缓存文件读取数据并处理
//					if (cacheFile == null || !cacheFile.exists())
//					{
//						throw new IOException("Unable to raed transcode cache,it may have been deleted by system");
//					}
//					FileInputStream fis=new FileInputStream(cacheFile);
//					f.write(getWavHeader(fis.available()));	
//					int len;
//					byte[] cache=new byte[2048];
//					while ((len = fis.read(cache)) != -1)
//					{
//						f.write(cache, 0, len);
//					}
//					fis.close();
//				}
//				f.flush();
//				f.close();
//				if (in != null)
//				{
//					in.close();}
//				activeCache(targetFile);
//			}
//
//		}
//		catch (IOException e)
//		{
//			UIHandler.sendMessage(UIHandler.obtainMessage(STATUS_ERROR, e));
//			return e.getMessage();
//		}
//		errorcode = RESULT_OK;
//		UIHandler.sendEmptyMessage(STATUS_DONE);
//		isProcessed = true;
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private boolean decodeAudio(final Handler UIHandler) throws Exception {

		UIHandler.sendEmptyMessage(STATUS_LOADINGFILE);
		//配置音轨分离器
		//decodeddata = new ByteArrayOutputStream ( );
		final MediaExtractor me=new MediaExtractor();
		cacheFile = new File(mcontext.getCacheDir(), "cache.pcm");
		//配置数据源，默认优先使用File
		if (srcFile != null) {
			me.setDataSource(srcFile.getAbsolutePath());
		} else {
			me.setDataSource(mcontext, srcFileUri, null);
		}
		int soundtrackIndex=-1;
		String mime="";
		MediaFormat md=null;
		//获取音频轨
		for (int i=0; i < me.getTrackCount(); i++) {
			md = me.getTrackFormat(i);
			//通过检验mime判断是否为音频轨
			if ((mime = md.getString(MediaFormat.KEY_MIME)).startsWith("audio")) {//若为音频轨，判断是否在需要跳过解码的例外中
				//
				//取消例外在解码过程中的处理，转为在处理时处理
				/*
				 if ( exceptions != null )
				 {
				 for ( int ii=0;ii < exceptions.length;ii++ )
				 {
				 if ( mime.equals ( exceptions [ ii ] ) )
				 {//若不需要解码，返回，且缓存文件指定为原文件
				 isUnneededtocompress = true;
				 decodeddata.reset ( );
				 if ( srcFile != null )
				 {
				 decodeddata.write ( Utils.readAllbytes ( new FileInputStream ( srcFile ) ) );
				 }
				 else
				 {
				 decodeddata.write ( Utils.readAllbytes ( mcontext.getContentResolver ( ).openInputStream ( srcFileUri ) ) );
				 }
				 isdecoded = true;
				 me.release ( );
				 return true;
				 }
				 }
				 }*/
				//更新选定的音轨
				soundtrackIndex = i;
				//从文件获取采样率与声道数
				mSampleRate = md.getInteger(MediaFormat.KEY_SAMPLE_RATE);
				mChannel = md.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
				break;
			}
		}
		if (soundtrackIndex == -1) {
			throw new Exception("No audio track counld be found from file.");
		}
		me.selectTrack(soundtrackIndex);
		//创建解码器
		MediaCodec mc=MediaCodec.createDecoderByType(mime);
		mc.configure(md, null, null, 0);
		UIHandler.sendEmptyMessage(STATUS_TRANSCODING);

		//创建缓存文件的输出流
		if (!cacheFile.getParentFile().exists()) {
			cacheFile.getParentFile().mkdirs();
		}
		final FileOutputStream fos=new FileOutputStream(cacheFile);
		mc.setCallback(new MediaCodec.Callback(){

			int role=0;

			@Override
			public void onInputBufferAvailable(MediaCodec p1, int p2) {
				//如果解码完成，直接停止输入数据
				if (isdecoded) {return;}
				//role:输入次数
				role++;
				int len=me.readSampleData(p1.getInputBuffer(p2), 0);
				if (len < 0) {//如果数据读完，通知解码器
					p1.queueInputBuffer(p2, 0, 0, 0, p1.BUFFER_FLAG_END_OF_STREAM);
				} else {
					p1.queueInputBuffer(p2, 0, len, 0, 0);
				}
				//读取完数据后，移入下一帧
				me.advance();


			}

			@Override
			public void onOutputBufferAvailable(MediaCodec p1, int p2, MediaCodec.BufferInfo p3) {
				if (p3.flags == p1.BUFFER_FLAG_END_OF_STREAM) {//如果解码器提示数据读完，停止输入数据，关闭输出流，通知主线程
					try {
						fos.close();
					} catch (IOException e) {//忽略
					}
					isdecoded = true;
					return;
				} else {
					//从解码器读取数据
					byte[] b=new byte[p1.getOutputBuffer(p2).remaining()];
					p1.getOutputBuffer(p2).get(b, 0, b.length);
					p1.getOutputBuffer(p2).clear();
					p1.releaseOutputBuffer(p2, false);
						/*
						 //将数据写入ByteStream
						 //decodeddata.write ( b, 0, b.length );
						 */
					//将解码后数据写入本地缓存
					try {
						fos.write(b, 0, b.length);
					} catch (IOException e) {
						UIHandler.sendMessage(UIHandler.obtainMessage(STATUS_ERROR, e));
					}

				}

				// TODO: Implement this method

			}

			@Override
			public void onError(MediaCodec p1, MediaCodec.CodecException p2) {
				UIHandler.handleMessage(UIHandler.obtainMessage(STATUS_ERROR, p2));
				// TODO: Implement this method
			}

			@Override
			public void onOutputFormatChanged(MediaCodec p1, MediaFormat p2) {

				// TODO: Implement this method
			}
		});
		//启动解码器，直至解码完成
		mc.start();
		do{
			try {Thread.sleep(50);} finally {}
		}while(!isdecoded);
		//解码结束，释放资源
		mc.stop();
		mc.release();
		me.release();
		return false;
	}
	//由pcm格式数据获取wav的文件头
	private byte[] getWavHeader(long audioLength)
	{
		long totalDataLength=audioLength + 8;
		long byteRate = 16 * mSampleRate * mChannel / 8;
		//文件头长度:44
		byte[] header=new byte[44];
		//将wav的magic number载入文件头(0-3)
		/*不用arraycopy防止阅读错误
		 System.arraycopy(HEADER_WAV,0,header,0,4);
		 */
		header[0] = HEADER_WAV[0];
		header[1] = HEADER_WAV[1];
		header[2] = HEADER_WAV[2];
		header[3] = HEADER_WAV[3];
		//载入文件总长度数据
		header[4] = (byte)(totalDataLength & 0xFF);
		header[5] = (byte)((totalDataLength >> 8) & 0xFF);
		header[6] = (byte)((totalDataLength >> 16) & 0xFF);
		header[7] = (byte)((totalDataLength >> 24) & 0xFF);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		//文件格式format chunk
		header[12] = 'f';
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16;//(16-19)size:16 无附加信息
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1;//(20-21)编码方式0x0001
		header[21] = 0;
		header[22] = (byte)mChannel;//(22-23)声道数 2:立体声 1:单声道
		header[23] = 0;
		header[24] = (byte)(mSampleRate & 0xFF);
		header[25] = (byte)((mSampleRate >> 8) & 0xFF);
		header[26] = (byte)((mSampleRate >> 16) & 0xFF);
		header[27] = (byte)((mSampleRate >> 24) & 0xFF);//(24-27)采样率
		header[28] = (byte)(byteRate & 0xFF);
		header[29] = (byte)((byteRate >> 8) & 0xFF);
		header[30] = (byte)((byteRate >> 16) & 0xFF);
		header[31] = (byte)((byteRate >> 24) & 0xFF);//(28-31)每秒所需字节数
		header[32] = 2 * 16 / 8;
		header[33] = 0;//(32-33) 每个采样所需字节数
		header[34] = 16;
		header[35] = 0;//(34-35) 每采样需要的bit
		//data chunk
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte)(audioLength & 0xFF);
		header[41] = (byte)((audioLength >> 8) & 0xFF);
		header[42] = (byte)((audioLength >> 16) & 0xFF);
		header[43] = (byte)((audioLength >> 24) & 0xFF);//(40-43)数据长度
		//文件头生成完成
		return header;
	}


	//回收使用后的临时资源
	public void recycle()
	{
		/*if ( decodeddata != null )
		 {
		 decodeddata.reset ( );}*/
		if (cacheFile != null)
		{
			cacheFile.delete();
		}
		isdecoded = false;
	}

	//使缓存失效
	public synchronized void denyCache(File file, int mode)
	{
		if (MODE_DENY_ALL_CACHE == mode)
		{
			valids.clear();
		}
		else
		{
			if (file != null)
			{
				if (cachedFiles.contains(file))
				{
					valids.put(file, Boolean.valueOf(false));
				}
			}
		}
	}
	public void denyCache(File file)
	{
		denyCache(file, 0);
	}

	//激活已转码为wav的缓存文件
	protected synchronized void activeCache(File file)
	{
		if (!cachedFiles.contains(file))
		{
			cachedFiles.add(file);
		}
		valids.put(file, Boolean.valueOf(true));
	}
	private synchronized File getValidCacheFile()
	{
		if (cachedFiles.isEmpty() || valids.isEmpty())
		{
			return null;
		}
		for (File f:cachedFiles)
		{
			if ((valids.get(f) != null) && valids.get(f))
			{
				return f;
			}
		}
		return null;
	}



}
