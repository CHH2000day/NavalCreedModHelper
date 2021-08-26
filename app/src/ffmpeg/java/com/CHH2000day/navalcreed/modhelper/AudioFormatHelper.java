package com.CHH2000day.navalcreed.modhelper;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioRecord;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okio.BufferedSink;
import okio.Okio;
import okio.Sink;
import okio.Source;

public class AudioFormatHelper {
    //源文件
    protected File srcFile;
    //private File mTargetFile;
    //转码时的缓存文件
    private File cacheFile;

    private HashMap<File, Boolean> validFiles;
    private ArrayList<File> cachedFiles;
    protected Uri srcFileUri;
    protected Context mContext;

    private boolean isdecoded = false;
    private boolean isProcessed = false;
    private boolean useCacheFile = false;
    //需要用到的UI操作的部分
    private Handler mEmptyHandler;
    public static final int STATUS_START = 1000;
    public static final int STATUS_LOADINGFILE = 1001;
    public static final int STATUS_TRANSCODING = 1002;
    public static final int STATUS_WRITING = 1003;
    public static final int STATUS_DONE = 1004;
    public static final int STATUS_ERROR = 1024;
    //转码配置
    private int mBufferSize = 256 * 1024; //缓存的音频大小
    //private int mSampleRate = 16000; //采样率，使用8000减少内存占用
    private int mSampleRate = 8000;
    private int mChannel = android.media.AudioFormat.CHANNEL_IN_STEREO; //立体声
    private int mEncoding = android.media.AudioFormat.ENCODING_PCM_16BIT;

    public static final int MODE_DENY_ALL_CACHE = -15;

    //.ogg/.ogv:OGGV
    public static final byte[] HEADER_OGG = {79, 103, 103, 83};

    //返回的结果
    public static final String RESULT_OK = "OK";
    private boolean isDone = false;
    private boolean hasError = false;

    public AudioFormatHelper(File audioFile, Context context) throws FileNotFoundException {

        //检查文件是否存在及可读
        if (!audioFile.exists() || !audioFile.canRead()) {
            throw new FileNotFoundException("Audio file:" + audioFile.getPath() + " could not be found");
        }
        init();
        this.srcFile = audioFile;
        mContext = context;
    }

    public AudioFormatHelper(Uri audioFilePath, Context context) {
        init();
        srcFileUri = audioFilePath;
        mContext = context;
    }

    @SuppressLint("HandlerLeak")
    private void init() {
        cachedFiles = new ArrayList<File>();
        validFiles = new HashMap<File, Boolean>();
        mBufferSize = AudioRecord.getMinBufferSize(mSampleRate, mChannel, mEncoding);
        mEmptyHandler = new Handler() {
            @Override
            public void handleMessage(@NotNull Message msg) {
                //DO:NOTHING
            }
        };
    }

    //转码至wav
    public String compressToWav(File targetFile) {
        return compressToWav(targetFile, mEmptyHandler);
    }

    public String compressToWav(final File targetFile, final Handler UIHandler) {
        String resultCode = "";
        UIHandler.sendEmptyMessage(STATUS_START);
        useCacheFile = (srcFileUri != null);
        hasError = false;

        try {
            File cachedFile = getValidCacheFile();
            FormatHelperFactory.refreshCache(targetFile);
            if (isProcessed && cachedFile != null && cachedFile.exists()) {
                UIHandler.sendEmptyMessage(STATUS_WRITING);
                Utils.copyFile(cachedFile, targetFile);
                UIHandler.sendEmptyMessage(STATUS_DONE);
                isDone = true;
            } else {
                if (useCacheFile) {
                    srcFile = new File(mContext.getExternalCacheDir(), "cache.audio");
                    Source source = Okio.source(mContext.getContentResolver().openInputStream(srcFileUri));
                    Sink sink = Okio.sink(srcFile);
                    BufferedSink bs = Okio.buffer(sink);
                    bs.writeAll(source);
                    bs.flush();
                    bs.close();
                    source.close();
                }
                UIHandler.sendEmptyMessage(STATUS_TRANSCODING);
                if (!targetFile.getParentFile().exists()) {
                    targetFile.getParentFile().mkdirs();
                }
                File destFile = _FileUtilsKt.getAndroid11Flag() ? new File(mContext.getExternalCacheDir(), "transCache.ogv") : targetFile;

                int result = FFmpeg.execute(new String[]{"-y", "-i", srcFile.getAbsolutePath(), destFile.getAbsolutePath()});
                if (RETURN_CODE_SUCCESS == result) {
//                    UIHandler.sendEmptyMessage(STATUS_DONE);
                    activeCache(targetFile);
                    isProcessed = true;
                } else {
                    hasError = true;
                    UIHandler.sendMessage(UIHandler.obtainMessage(STATUS_ERROR, new IOException("Failed to transcode")));
                    Config.printLastCommandOutput(Log.INFO);
                }
                if (_FileUtilsKt.getAndroid11Flag()) {
                    Utils.copyFile(destFile, targetFile);
                    _FileUtilsKt.toDocumentFile(destFile).delete();
                }
                isDone = true;
                if (useCacheFile && srcFile != null) {
                    _FileUtilsKt.toDocumentFile(srcFile).delete();
                }
            }
        } catch (Throwable e) {
            UIHandler.sendMessage(UIHandler.obtainMessage(STATUS_ERROR, e));
            hasError = true;
            isDone = true;
            resultCode = e.getMessage();
            if (useCacheFile && srcFile != null) {
                _FileUtilsKt.toDocumentFile(srcFile).delete();
            }
        }

        while (!isDone) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {

            }
        }
        if (TextUtils.isEmpty(resultCode)) {
            return RESULT_OK;
        } else {
            return resultCode;
        }
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
            validFiles.clear();
        } else {
            if (file != null) {
                if (cachedFiles.contains(file)) {
                    validFiles.put(file, Boolean.FALSE);
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
        validFiles.put(file, Boolean.TRUE);
    }

    private synchronized File getValidCacheFile() {
        if (cachedFiles.isEmpty() || validFiles.isEmpty()) {
            return null;
        }
        for (File f : cachedFiles) {
            if ((validFiles.get(f) != null) && validFiles.get(f)) {
                return f;
            }
        }
        return null;
    }
}
