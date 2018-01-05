package com.CHH2000day.navalcreed.modhelper;
import java.io.*;
import java.util.*;
import android.media.*;
import android.os.*;
import android.net.*;
import android.content.*;
import android.media.MediaCodec.*;
import java.nio.*;
import okio.*;

public class AudioFormatHelper
{
	//源文件
	private File srcFile;
	private Uri srcFileUri;
	private Context mcontext;
	private ByteArrayOutputStream decodeddata;
	private boolean isdecoded=false;
	private boolean isUnneededtocompress=false;
	//需要用到的UI操作的部分
	private Handler mEmptyHandler=new Handler ( ){

		@Override
		public void handleMessage ( Message msg )
		{
			// TODO: Implement this method
			//DO:NOTHING
		}

	};
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
	private int mChannel = AudioFormat.CHANNEL_IN_STEREO; //立体声
	private int mEncoding = AudioFormat.ENCODING_PCM_16BIT;


	//文件格式magic number
	//.wav:RIFF
	public static final byte[] HEADER_WAV={ 82, 73, 70, 70 };
	//.ogg/.ogv:OGGV
	public static final byte[] HEADER_OGG={ 79, 103, 103, 83 };

	//返回的结果
	public static final String RESULT_OK="OK";

	public AudioFormatHelper ( File audioFile ) throws FileNotFoundException
	{
		//检查文件是否存在及可读
		if ( !audioFile.exists ( ) || !audioFile.canRead ( ) )
		{
			throw new FileNotFoundException ( "Audio file:" + audioFile.getPath ( ) + " could not be found" );
		}
		this.srcFile = audioFile;
		mBufferSize = AudioRecord.getMinBufferSize ( mSampleRate, mChannel, mEncoding );
	}
	public AudioFormatHelper ( Uri audioFilePath, Context context )
	{
		srcFileUri = audioFilePath;
		mcontext = context;
		mBufferSize = AudioRecord.getMinBufferSize ( mSampleRate, mChannel, mEncoding );
	}
	//转码至wav
	public String compressToWav ( File targetFile )
	{
		return conpressToWav ( targetFile, mEmptyHandler );
	}
	public String conpressToWav ( File targetFile, Handler UIHandler )
	{
		String errorcode="";
		UIHandler.sendEmptyMessage ( STATUS_START );
		InputStream in;

		try
		{
			boolean bl=decodeAudio ( null, UIHandler );
		}
		catch (Exception e)
		{
			UIHandler.sendMessage ( UIHandler.obtainMessage ( STATUS_ERROR, e ) );
			return e.getMessage ( );
		}
		try
		{
			UIHandler.sendEmptyMessage ( STATUS_WRITING );
			Sink s=Okio.sink ( targetFile );
			BufferedSink f=Okio.buffer ( s );
			if ( !isUnneededtocompress )
			{
				f.write ( getWavHeader ( decodeddata.size ( ) ) );
			}
			f.write ( decodeddata.toByteArray ( ) );
			f.flush ( );
			f.close ( );
		}
		catch (IOException e)
		{
			UIHandler.sendMessage ( UIHandler.obtainMessage ( STATUS_ERROR, e ) );
			return e.getMessage ( );
		}
		errorcode = RESULT_OK;
		UIHandler.sendEmptyMessage ( STATUS_DONE );
		return errorcode;

	}
	private boolean decodeAudio ( String[] exceptions , Handler UIHandler ) throws Exception
	{

		UIHandler.sendEmptyMessage ( STATUS_LOADINGFILE );
		//配置音轨分离器
		decodeddata = new ByteArrayOutputStream ( );
		final MediaExtractor me=new MediaExtractor ( );
		//配置数据源，默认优先使用File
		if ( srcFile != null )
		{
			me.setDataSource ( srcFile.getAbsolutePath ( ) );
		}
		else
		{
			me.setDataSource ( mcontext, srcFileUri, null );
		}
		int soundtrackIndex=-1;
		String mime="";
		MediaFormat md=null;
		//获取音频轨
		for ( int i=0;i < me.getTrackCount ( );i++ )
		{
			md = me.getTrackFormat ( i );
			//通过检验mime判断是否为音频轨
			if ( ( mime = md.getString ( MediaFormat.KEY_MIME ) ).startsWith ( "audio" ) )
			{//若为音频轨，判断是否在需要跳过解码的例外中
				if ( exceptions != null )
				{
					for ( int ii=0;ii < exceptions.length;ii++ )
					{
						if ( mime.equals ( exceptions [ ii ] ) )
						{//若不需要解码，返回
							isUnneededtocompress = true;
							decodeddata.reset ( );
							if ( srcFile != null )
							{
								decodeddata.write ( Utils.readAllbytes ( new FileInputStream ( srcFile ) ) );
							}
							else
							{
								decodeddata.write ( Utils.readAllbytes ( mcontext.openFileInput ( srcFileUri.toString ( ) ) ) );
							}
							isdecoded = true;
							me.release ( );
							return true;
						}
					}
				}
				//更新选定的音轨
				soundtrackIndex = i;
				//从文件获取采样率与声道数
				mSampleRate = Integer.getInteger ( md.getString ( MediaFormat.KEY_SAMPLE_RATE ) );
				mChannel = Integer.getInteger ( md.getString ( MediaFormat.KEY_CHANNEL_COUNT ) );
				break;
			}
		}
		if ( soundtrackIndex == -1 )
		{
			throw new Exception ( "No audio track counld be found from file." );
		}
		//创建解码器
		MediaCodec mc=MediaCodec.createDecoderByType ( mime );
		mc.configure ( md, null, null, 0 );
		mc.setCallback ( new MediaCodec.Callback ( ){

				@Override
				public void onInputBufferAvailable ( MediaCodec p1, int p2 )
				{
					// TODO: Implement this method
					int len=me.readSampleData ( p1.getInputBuffer ( p2 ), 0 );
					if ( len < 0 )
					{
						p1.queueInputBuffer ( p2, 0, 0, 0, p1.BUFFER_FLAG_END_OF_STREAM );
					}
					else
					{
						p1.queueInputBuffer ( p2, 0, len, 0, 0 );
					}

				}

				@Override
				public void onOutputBufferAvailable ( MediaCodec p1, int p2, MediaCodec.BufferInfo p3 )
				{
					if ( p3.flags == p1.BUFFER_FLAG_END_OF_STREAM )
					{
						isdecoded = true;
						return;
					}
					else
					{
						try
						{
							decodeddata.write ( p1.getInputBuffer ( p2 ).array ( ) );
						}
						catch (IOException e)
						{

						}
					}

					// TODO: Implement this method

				}

				@Override
				public void onError ( MediaCodec p1, MediaCodec.CodecException p2 )
				{
					// TODO: Implement this method
				}

				@Override
				public void onOutputFormatChanged ( MediaCodec p1, MediaFormat p2 )
				{
					// TODO: Implement this method
				}
			} );
		//启动解码器，直至解码完成
		mc.start ( );
		do{
			try
			{Thread.sleep ( 50 );}
			finally
			{}
		}while(!isdecoded);
		//解码结束，释放资源
		mc.stop ( );
		mc.release ( );
		me.release ( );
		return false;
	}
	//由pcm格式数据获取wav的文件头
	private byte[] getWavHeader ( long audioLength )
	{
		long totalDataLength=audioLength + 8;
		long byteRate = 16 * mSampleRate * mChannel / 8;
		//文件头长度:44
		byte[] header=new byte[44];
		//将wav的magic number载入文件头(0-3)
		/*不用arraycopy防止阅读错误
		 System.arraycopy(HEADER_WAV,0,header,0,4);
		 */
		header [ 0 ] = HEADER_WAV [ 0 ];
		header [ 1 ] = HEADER_WAV [ 1 ];
		header [ 2 ] = HEADER_WAV [ 2 ];
		header [ 3 ] = HEADER_WAV [ 3 ];
		//载入文件总长度数据
		header [ 4 ] = (byte)( totalDataLength & 0xFF );
		header [ 5 ] = (byte)( ( totalDataLength >> 8 ) & 0xFF );
		header [ 6 ] = (byte)( ( totalDataLength >> 16 ) & 0xFF );
		header [ 7 ] = (byte)( ( totalDataLength >> 24 ) & 0xFF );
		header [ 8 ] = 'W';
		header [ 9 ] = 'A';
		header [ 10 ] = 'V';
		header [ 11 ] = 'E';
		//文件格式format chunk
		header [ 12 ] = 'f';
		header [ 13 ] = 'm';
		header [ 14 ] = 't';
		header [ 15 ] = ' ';
		header [ 16 ] = 16;//(16-19)size:16 无附加信息
		header [ 17 ] = 0;
		header [ 18 ] = 0;
		header [ 19 ] = 0;
		header [ 20 ] = 1;//(20-21)编码方式0x0001
		header [ 21 ] = 0;
		header [ 22 ] = (byte)mChannel;//(22-23)声道数 2:立体声 1:单声道
		header [ 23 ] = 0;
		header [ 24 ] = (byte)( mSampleRate & 0xFF );
		header [ 25 ] = (byte)( ( mSampleRate >> 8 ) & 0xFF );
		header [ 26 ] = (byte)( ( mSampleRate >> 16 ) & 0xFF );
		header [ 27 ] = (byte)( ( mSampleRate >> 24 ) & 0xFF );//(24-27)采样率
		header [ 28 ] = (byte)( byteRate & 0xFF );
		header [ 29 ] = (byte)( ( byteRate >> 8 ) & 0xFF );
		header [ 30 ] = (byte)( ( byteRate >> 16 ) & 0xFF );
		header [ 31 ] = (byte)( ( byteRate >> 24 ) & 0xFF );//(28-31)每秒所需字节数
		header [ 32 ] = 2 * 16 / 8;
		header [ 33 ] = 0;//(32-33) 每个采样所需字节数
		header [ 34 ] = 16;
		header [ 35 ] = 0;//(34-35) 每采样需要的bit
		//data chunk
		header [ 36 ] = 'd';
		header [ 37 ] = 'a';
		header [ 38 ] = 't';
		header [ 39 ] = 'a';
		header [ 40 ] = (byte)( audioLength & 0xFF );
		header [ 41 ] = (byte)( ( audioLength >> 8 ) & 0xFF );
		header [ 42 ] = (byte)( ( audioLength >> 16 ) & 0xFF );
		header [ 43 ] = (byte)( ( audioLength >> 24 ) & 0xFF );//(40-43)数据长度
		//文件头生成完成
		return header;
	}

}
