package com.CHH2000day.navalcreed.modhelper;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

public class AudioFormatHelper {
    public static final int STATUS_START = 1000;
    public static final int STATUS_LOADINGFILE = 1001;
    public static final int STATUS_TRANSCODING = 1002;
    public static final int STATUS_WRITING = 1003;
    public static final int STATUS_DONE = 1004;
    public static final int STATUS_ERROR = 1024;
    public static final int MODE_DENY_ALL_CACHE = -15;
    //文件格式magic number
    //.wav:RIFF
    public static final byte[] HEADER_WAV = {82, 73, 70, 70};
    //.ogg/.ogv:OGGV
    public static final byte[] HEADER_OGG = {79, 103, 103, 83};
    //返回的结果
    public static final String RESULT_OK = "OK";
    //源文件
    protected File srcFile;
    protected Uri srcFileUri;
    protected Context mcontext;
    //private File mTargetFile;
    //转码时的缓存文件
    private File cacheFile;
    private HashMap<File, Boolean> valids;
    private ArrayList<File> cachedFiles;
    /*
     //使用本地缓存，弃用该stream
     private ByteArrayOutputStream decodeddata;
     */
    private boolean isdecoded = false;
    private boolean isNotNeedToCompress = false;
    private boolean isProcessed = false;
    //需要用到的UI操作的部分
    private Handler mEmptyHandler;
    //private int mSampleRate = 16000; //采样率，使用8000减少内存占用
    private int mSampleRate = 8000;
    private int mChannel = AudioFormat.CHANNEL_IN_STEREO; //立体声
    private long byteCount = 0;

    public AudioFormatHelper(File audioFile, Context context) throws FileNotFoundException {

        //检查文件是否存在及可读
        if (!audioFile.exists() || !audioFile.canRead()) {
            throw new FileNotFoundException("Audio file:" + audioFile.getPath() + " could not be found");
        }
        init();
        this.srcFile = audioFile;
        mcontext = context;
    }

    public AudioFormatHelper(Uri audioFilePath, Context context) {
        init();
        srcFileUri = audioFilePath;
        mcontext = context;
    }

    @SuppressLint("HandlerLeak")
    private void init() {
        cachedFiles = new ArrayList<File>();
        valids = new HashMap<File, Boolean>();
        int mEncoding = AudioFormat.ENCODING_PCM_16BIT;//转码配置
        int mBufferSize = AudioRecord.getMinBufferSize(mSampleRate, mChannel, mEncoding);
        mEmptyHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                //DO:NOTHING
            }
        };
    }

    //转码至wav
    public String compressToWav(File targetFile) {
        return compressToWav(targetFile, mEmptyHandler);
    }

    public String compressToWav(final File targetFile, final Handler UIHandler) {
        _FileUtilsKt.mkdirCompatible(targetFile);
        String errorcode = "";
        UIHandler.sendEmptyMessage(STATUS_START);
        BufferedSource bsource = null;
        File cachedFile = getValidCacheFile();
        if (!isProcessed || cachedFile == null) {
            try {
                //验证源文件是否已为wav格式
                if (srcFile != null) {
                    bsource = _FileUtilsKt.toBufferedSource(cacheFile);
                } else {
                    bsource = Okio.buffer(Okio.source(mcontext.getContentResolver().openInputStream(srcFileUri)));
                }
                byte[] b = new byte[4];
                bsource.read(b);
                if (Arrays.equals(b, HEADER_WAV)) {

                    //若是，直接读取为已解码数据，并跳过添加文件头
                    isNotNeedToCompress = true;
                } else {
                    //解码数据
                    decodeAudio(UIHandler);
                }
            } catch (Exception e) {
                UIHandler.sendMessage(UIHandler.obtainMessage(STATUS_ERROR, e));
                return e.getMessage();
            }
        }
        try {
            UIHandler.sendEmptyMessage(STATUS_WRITING);
            FormatHelperFactory.refreshCache(targetFile);
            //如果存在已处理好的缓存文件，直接复制
            if (isProcessed && cachedFile != null && cachedFile.exists()) {
                Utils.copyFile(cachedFile, targetFile);

            } else {
                BufferedSink bufferedSink = _FileUtilsKt.toBufferedSink(targetFile);
                //添加文件头
                //若源文件已为wav格式，直接读取源文件并写入
                if (isNotNeedToCompress) {
                    bufferedSink.write(HEADER_WAV);
                    bufferedSink.writeAll(bsource);
                } else {
                    //从缓存文件读取数据并处理
                    if (cacheFile == null || !cacheFile.exists()) {
                        throw new IOException("Unable to raed transcode cache,it may have been deleted by system");
                    }
                    BufferedSource bs = _FileUtilsKt.toBufferedSource(cacheFile);
                    bufferedSink.write(getWavHeader(byteCount));
                    bufferedSink.writeAll(bs);
                    bs.close();
                }
                bufferedSink.flush();
                bufferedSink.close();
                if (bsource != null) {
                    bsource.close();
                }
                activeCache(targetFile);
            }

        } catch (IOException e) {
            UIHandler.sendMessage(UIHandler.obtainMessage(STATUS_ERROR, e));
            return e.getMessage();
        }
        errorcode = RESULT_OK;
        UIHandler.sendEmptyMessage(STATUS_DONE);
        isProcessed = true;
        return errorcode;

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean decodeAudio(final Handler UIHandler) throws Exception {

        UIHandler.sendEmptyMessage(STATUS_LOADINGFILE);
        //配置音轨分离器
        final MediaExtractor me = new MediaExtractor();
        cacheFile = new File(mcontext.getCacheDir(), "cache.pcm");
        //配置数据源，默认优先使用File
        if (srcFile != null) {
            me.setDataSource(mcontext.getContentResolver().openFileDescriptor(_FileUtilsKt.toDocumentFile(srcFile).getUri(), "r").getFileDescriptor());
        } else {
            me.setDataSource(mcontext, srcFileUri, null);
        }
        int soundtrackIndex = -1;
        String mime = "";
        MediaFormat md = null;
        //获取音频轨
        for (int i = 0; i < me.getTrackCount(); i++) {
            md = me.getTrackFormat(i);
            //通过检验mime判断是否为音频轨
            if ((mime = md.getString(MediaFormat.KEY_MIME)).startsWith("audio")) {//若为音频轨，判断是否在需要跳过解码的例外中
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
        MediaCodec mc = MediaCodec.createDecoderByType(mime);
        mc.configure(md, null, null, 0);
        UIHandler.sendEmptyMessage(STATUS_TRANSCODING);

        //创建缓存文件的输出流
        final BufferedSink bs = _FileUtilsKt.toBufferedSink(cacheFile);
        mc.setCallback(new MediaCodec.Callback() {

            int role = 0;

            @Override
            public void onInputBufferAvailable(MediaCodec p1, int p2) {
                //如果解码完成，直接停止输入数据
                if (isdecoded) {
                    return;
                }
                //role:输入次数
                role++;
                int len = me.readSampleData(p1.getInputBuffer(p2), 0);
                if (len < 0) {//如果数据读完，通知解码器
                    p1.queueInputBuffer(p2, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                } else {
                    p1.queueInputBuffer(p2, 0, len, 0, 0);
                    byteCount += len;
                }
                //读取完数据后，移入下一帧
                me.advance();
            }

            @Override
            public void onOutputBufferAvailable(MediaCodec p1, int p2, MediaCodec.BufferInfo p3) {
                if (p3.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {//如果解码器提示数据读完，停止输入数据，关闭输出流，通知主线程
                    try {
                        bs.close();
                    } catch (IOException e) {//忽略
                    }
                    isdecoded = true;
                } else {
                    //从解码器读取数据
                    //将解码后数据写入本地缓存
                    try {
                        ByteBuffer buffer=p1.getOutputBuffer(p2);
                        bs.write(buffer);
                        buffer.clear();
                        p1.releaseOutputBuffer(p2, false);
                    } catch (IOException e) {
                        UIHandler.sendMessage(UIHandler.obtainMessage(STATUS_ERROR, e));
                    }
                }
            }

            @Override
            public void onError(MediaCodec p1, MediaCodec.CodecException p2) {
                UIHandler.handleMessage(UIHandler.obtainMessage(STATUS_ERROR, p2));
            }

            @Override
            public void onOutputFormatChanged(MediaCodec p1, MediaFormat p2) {
            }
        });
        //启动解码器，直至解码完成
        mc.start();
        do {
            Thread.sleep(50);
        } while (!isdecoded);
        //解码结束，释放资源
        mc.stop();
        mc.release();
        me.release();
        return false;
    }

    //由pcm格式数据获取wav的文件头
    private byte[] getWavHeader(long audioLength) {
        long totalDataLength = audioLength + 8;
        long byteRate = 16 * mSampleRate * mChannel / 8;
        //文件头长度:44
        byte[] header = new byte[44];
        //将wav的magic number载入文件头(0-3)
        header[0] = HEADER_WAV[0];
        header[1] = HEADER_WAV[1];
        header[2] = HEADER_WAV[2];
        header[3] = HEADER_WAV[3];
        //载入文件总长度数据
        header[4] = (byte) (totalDataLength & 0xFF);
        header[5] = (byte) ((totalDataLength >> 8) & 0xFF);
        header[6] = (byte) ((totalDataLength >> 16) & 0xFF);
        header[7] = (byte) ((totalDataLength >> 24) & 0xFF);
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
        header[22] = (byte) mChannel;//(22-23)声道数 2:立体声 1:单声道
        header[23] = 0;
        header[24] = (byte) (mSampleRate & 0xFF);
        header[25] = (byte) ((mSampleRate >> 8) & 0xFF);
        header[26] = (byte) ((mSampleRate >> 16) & 0xFF);
        header[27] = (byte) ((mSampleRate >> 24) & 0xFF);//(24-27)采样率
        header[28] = (byte) (byteRate & 0xFF);
        header[29] = (byte) ((byteRate >> 8) & 0xFF);
        header[30] = (byte) ((byteRate >> 16) & 0xFF);
        header[31] = (byte) ((byteRate >> 24) & 0xFF);//(28-31)每秒所需字节数
        header[32] = 2 * 16 / 8;
        header[33] = 0;//(32-33) 每个采样所需字节数
        header[34] = 16;
        header[35] = 0;//(34-35) 每采样需要的bit
        //data chunk
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (audioLength & 0xFF);
        header[41] = (byte) ((audioLength >> 8) & 0xFF);
        header[42] = (byte) ((audioLength >> 16) & 0xFF);
        header[43] = (byte) ((audioLength >> 24) & 0xFF);//(40-43)数据长度
        //文件头生成完成
        return header;
    }


    //回收使用后的临时资源
    public void recycle() {
        if (cacheFile != null) {
            cacheFile.delete();
        }
        isdecoded = false;
    }

    //使缓存失效
    public synchronized void invalidCache(File file, int mode) {
        if (MODE_DENY_ALL_CACHE == mode) {
            valids.clear();
        } else {
            if (file != null) {
                if (cachedFiles.contains(file)) {
                    valids.put(file, Boolean.FALSE);
                }
            }
        }
    }

    public void invalidCache(File file) {
        invalidCache(file, 0);
    }

    //激活已转码为wav的缓存文件
    protected synchronized void activeCache(File file) {
        if (!cachedFiles.contains(file)) {
            cachedFiles.add(file);
        }
        valids.put(file, Boolean.TRUE);
    }

    private synchronized File getValidCacheFile() {
        if (cachedFiles.isEmpty() || valids.isEmpty()) {
            return null;
        }
        for (File f : cachedFiles) {
            if ((valids.get(f) != null) && valids.get(f)) {
                return f;
            }
        }
        return null;
    }


}
