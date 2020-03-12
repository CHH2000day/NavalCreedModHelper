package com.CHH2000day.navalcreed.modhelper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.CHH2000day.navalcreed.modhelper.ModPackageInfo.IllegalModInfoException;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import okio.Sink;
import okio.Source;

public class ModPackageInstallHelper {
    public static final int SUBTYPE_NULL = 0;
    public static final int SUBTYPE_CV_EN = 1200;
    public static final int SUBTYPE_CV_CN = 1201;
    public static final int SUBTYPE_CV_JP_CV = 1202;
    public static final int SUBTYPE_CV_JP_BB = 1203;
    public static final int SUBTYPE_CV_JP_CA = 1204;
    public static final int SUBTYPE_CV_JP_DD = 1205;
    public static final int SUBTYPE_CV_DE = 1206;
    public static final int SUBTYPE_CV_RU_BEARD = 1207;
    public static final int SUBTYPE_CV_RU_VLAD = 1208;
    /*
    Mark original russian voice for migration.
    */
    public static final int SUBTYPE_CV_RU = 9300;
    static final int SUBTYPE_CV_OFFSET = SUBTYPE_CV_EN;
    //常量声明
    private static final String FILE_MODINFO = "mod.info";
    private static final String FILE_MODPREVIEW = "mod.preview";
    private static final String FILE_CUSTOMSHIPNAME_PATCH = "shipnames.patch";
    private static final String PRIMARYPATH_CV = File.separatorChar + "sound" + File.separatorChar + "Voice";
    private static final String PRIMARYPATH_BGM = File.separatorChar + "sound" + File.separatorChar + "Music";
    private static final String PRIMARYPATH_SOUNDEFFECT = File.separatorChar + "sound" + File.separatorChar + "soundeffect" + File.separatorChar + "ginsir";
    private static final String PRIMARYPATH_SOUNDEFFECT_PRIM = File.separatorChar + "sound" + File.separatorChar + "soundeffect";
    private static final String PRIMARYPATH_SOUNDEFFECT_SEC = File.separatorChar + "sound" + File.separatorChar + "newsound";
    private static final String PRIMARYPATH_BACKGROUND = File.separatorChar + "pic";
    private static final String PRIMARYPATH_CREWHEAD = File.separatorChar + "pic" + File.separatorChar + "crewhead";
    private static final String PRIMARYTYPE_OTHER = "";
    private static final String SUBPATH_CV_EN = File.separatorChar + "EnglishUsual";
    private static final String SUBPATH_CV_CN = File.separatorChar + "ChineseUsual";
    private static final String SUBPATH_CV_JP = File.separatorChar + "Japanesemoe";
    private static final String SUBPATH_CV_JP_CV = SUBPATH_CV_JP + File.separatorChar + "AirCarrier";
    private static final String SUBPATH_CV_JP_BB = SUBPATH_CV_JP + File.separatorChar + "Battleship";
    private static final String SUBPATH_CV_JP_CA = SUBPATH_CV_JP + File.separatorChar + "Cruiser";
    private static final String SUBPATH_CV_JP_DD = SUBPATH_CV_JP + File.separatorChar + "Destroyer";
    private static final String SUBPATH_CV_DE = File.separatorChar + "GermanUsual";
    private static final String SUBPATH_CV_RU = File.separatorChar + "RussianUsual";
    private static final String SUBPATH_CV_RU_VLAD = File.separatorChar + "RussianUsual_Vlad";
    private static final String SUBPATH_CV_RU_BEARD = File.separatorChar + "RussianUsual_Beard";
    private onModPackageLoadDoneListener mlistener;
    private long totalFileSize;
    private Handler mHandler;

    private int msubtype = SUBTYPE_NULL;
    private ModHelperApplication mmha;
    private File msrcFile;
    private ZipFile mpkgFile;
    private ModPackageInfo mmpi;
    private ModPackageChecker modPackageChecker;
    private HashSet<String> filesSet;

    public ModPackageInstallHelper(File pkgFile) {
        msrcFile = pkgFile;
    }

    public static int getSubTypeId(String subtype) {
        int subt = 0;
        switch (subtype) {
            case ModPackageInfo.SUB_MODTYPE_CV_CN:
                subt = ModPackageInstallHelper.SUBTYPE_CV_CN;
                break;
            case ModPackageInfo.SUB_MODTYPE_CV_EN:
                subt = ModPackageInstallHelper.SUBTYPE_CV_EN;
                break;
            case ModPackageInfo.SUB_MODTYPE_CV_JP_BB:
                subt = ModPackageInstallHelper.SUBTYPE_CV_JP_BB;
                break;
            case ModPackageInfo.SUB_MODTYPE_CV_JP_CA:
                subt = ModPackageInstallHelper.SUBTYPE_CV_JP_CA;
                break;
            case ModPackageInfo.SUB_MODTYPE_CV_JP_CV:
                subt = ModPackageInstallHelper.SUBTYPE_CV_JP_CV;
                break;
            case ModPackageInfo.SUB_MODTYPE_CV_JP_DD:
                subt = ModPackageInstallHelper.SUBTYPE_CV_JP_DD;
                break;
            case ModPackageInfo.SUB_MODTYPE_CV_DE:
                subt = ModPackageInstallHelper.SUBTYPE_CV_DE;
                break;
            case ModPackageInfo.SUB_MODTYPE_CV_RU:
                subt = ModPackageInstallHelper.SUBTYPE_CV_RU;
                break;
            case ModPackageInfo.SUB_MODTYPE_CV_RU_VLAD:
                subt = ModPackageInstallHelper.SUBTYPE_CV_RU_VLAD;
                break;
            case ModPackageInfo.SUB_MODTYPE_CV_RU_BEARD:
                subt = ModPackageInstallHelper.SUBTYPE_CV_RU_BEARD;
                break;
        }
        return subt;
    }

    private static String getSubType(int msubtype) {
        String s = "";
        switch (msubtype) {
            case SUBTYPE_CV_CN:
                s = ModPackageInfo.SUB_MODTYPE_CV_CN;
                break;
            case SUBTYPE_CV_EN:
                s = ModPackageInfo.SUB_MODTYPE_CV_EN;
                break;
            case SUBTYPE_CV_JP_CV:
                s = ModPackageInfo.SUB_MODTYPE_CV_JP_CV;
                break;
            case SUBTYPE_CV_JP_BB:
                s = ModPackageInfo.SUB_MODTYPE_CV_JP_BB;
                break;
            case SUBTYPE_CV_JP_CA:
                s = ModPackageInfo.SUB_MODTYPE_CV_JP_CA;
                break;
            case SUBTYPE_CV_JP_DD:
                s = ModPackageInfo.SUB_MODTYPE_CV_JP_DD;
                break;
            case SUBTYPE_CV_DE:
                s = ModPackageInfo.SUB_MODTYPE_CV_DE;
                break;
            case SUBTYPE_CV_RU:
                s = ModPackageInfo.SUB_MODTYPE_CV_RU;
                break;
            case SUBTYPE_CV_RU_VLAD:
                s = ModPackageInfo.SUB_MODTYPE_CV_RU_VLAD;
                break;
            case SUBTYPE_CV_RU_BEARD:
                s = ModPackageInfo.SUB_MODTYPE_CV_RU_BEARD;
                break;
            default:
                s = ModPackageInfo.SUBTYPE_EMPTY;
                break;
        }
        return s;
    }

    public static String getPath(String modType, int subType, ModHelperApplication app) {
        String pth = app.getResFilesDirPath();

        switch (modType) {
            case ModPackageInfo.MODTYPE_CV:
                pth += PRIMARYPATH_CV;
                switch (subType) {
                    case SUBTYPE_CV_CN:
                        pth += SUBPATH_CV_CN;
                        break;
                    case SUBTYPE_CV_EN:
                        pth += SUBPATH_CV_EN;
                        break;
                    case SUBTYPE_CV_JP_CV:
                        pth += SUBPATH_CV_JP_CV;
                        break;
                    case SUBTYPE_CV_JP_BB:
                        pth += SUBPATH_CV_JP_BB;
                        break;
                    case SUBTYPE_CV_JP_CA:
                        pth += SUBPATH_CV_JP_CA;
                        break;
                    case SUBTYPE_CV_JP_DD:
                        pth += SUBPATH_CV_JP_DD;
                        break;
                    case SUBTYPE_CV_DE:
                        pth += SUBPATH_CV_DE;
                        break;
                    case SUBTYPE_CV_RU:
                        pth += SUBPATH_CV_RU;
                        break;
                    case SUBTYPE_CV_RU_VLAD:
                        pth += SUBPATH_CV_RU_VLAD;
                        break;
                    case SUBTYPE_CV_RU_BEARD:
                        pth += SUBPATH_CV_RU_BEARD;
                        break;
                }
                break;
            case ModPackageInfo.MODTYPE_BACKGROUND:
                pth += PRIMARYPATH_BACKGROUND;
                break;
            case ModPackageInfo.MODTYPE_BGM:
                pth += PRIMARYPATH_BGM;
                break;
            case ModPackageInfo.MODTYPE_CREWPIC:
                pth += PRIMARYPATH_CREWHEAD;
                break;
            case ModPackageInfo.MODTYPE_SOUNDEFFECT:
                pth += PRIMARYPATH_SOUNDEFFECT;
                break;
            case ModPackageInfo.MODTYPE_SOUNDEFFECT_PRIM:
                pth += PRIMARYPATH_SOUNDEFFECT_PRIM;
                break;
            case ModPackageInfo.MODTYPE_SOUNDEFFECT_SEC:
                pth += PRIMARYPATH_SOUNDEFFECT_SEC;
                break;
            case ModPackageInfo.MODTYPE_OTHER:
                pth += PRIMARYTYPE_OTHER;
                break;
        }
        return pth;
    }

    public void load(@NonNull onModPackageLoadDoneListener listener) {
        mlistener = listener;
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (mlistener == null) {
                    return;
                }
                switch (msg.what) {
                    case 0:
                        mlistener.onSuccess();
                        break;
                    case -1:
                        mlistener.onFail((Throwable) msg.obj);
                        break;
                }
            }
        };
        mmha = (ModHelperApplication) listener.getActivity().getApplication();
        new Thread() {
            public void run() {
                try {
                    load();
                    totalFileSize = calculateTotalSize();
                    mHandler.sendEmptyMessage(0);
                } catch (Exception e) {
                    mHandler.sendMessage(mHandler.obtainMessage(-1, e));
                    Logger.d(e);
                }
            }
        }.start();
    }

    private void load() throws IOException, ModPackageInfo.IllegalModInfoException {
        //创建mod文件实例
        fetch();
        //识别Mod文件并读取信息
        identify();
    }

    public void recycle() {
        try {
            if (mpkgFile != null) {
                mpkgFile.close();
                mpkgFile = null;
            }
            mlistener = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fetch() throws IOException {
        if (!msrcFile.exists() || !msrcFile.isFile()) {
            throw new IOException("File :" + msrcFile.getAbsolutePath() + " does not exists");
        }
        mpkgFile = new ZipFile(msrcFile);
    }

    private void identify() throws IOException, ModPackageInfo.IllegalModInfoException {
        ZipEntry mInfoFile = mpkgFile.getEntry(FILE_MODINFO);
        if (mInfoFile == null) {
            throw new IllegalModInfoException("Could not load mod.info from package");
        }
        InputStream zi = mpkgFile.getInputStream(mInfoFile);
        ZipEntry mpicEntry;
        if ((mpicEntry = mpkgFile.getEntry(FILE_MODPREVIEW)) != null) {
            mmpi = ModPackageInfo.Factory.createFromInputStreamWithExternalPic(zi, mpkgFile.getInputStream(mpicEntry));
        } else {
            mmpi = ModPackageInfo.Factory.createFromInputStream(zi);
        }
    }

    public void beginInstall(Activity activity) {
        if (modPackageChecker == null) {
            modPackageChecker = new ModPackageChecker(activity, this);
        }
        modPackageChecker.start();

    }

    //Also adds file to set
    private long calculateTotalSize() {
        filesSet = new HashSet<>();
        Enumeration<? extends ZipEntry> en = mpkgFile.entries();
        long totalsize = 0;
        while (en.hasMoreElements()) {
            ZipEntry entry = en.nextElement();
            if (entry.getName().equals(FILE_MODINFO) || entry.getName().equals(FILE_MODPREVIEW)) {
                continue;
            }
            totalsize += entry.getSize();
            filesSet.add(entry.getName());
        }
        en = null;
        Logger.i("mod size:%d bytes", totalsize);
        return totalsize;
    }

    public long getTotalSize() {
        return totalFileSize;
    }

    /**
     * Check whether app satisfies all function
     *
     * @return null if everything goes normal
     */
    private ErrorMsg checkVersion() {
        if (mmpi.hasAllFeature()) {
            return null;
        }
        return new ErrorMsg(mmha.getResources().getString(R.string.modpkg_ver_warning), true);
    }

    private ErrorMsg checkAvailSpace() {
        StatFs fs = new StatFs(mmha.getResFilesDirPath());
        long avail = fs.getAvailableBytes();
        if (getTotalSize() > avail) {
            return new ErrorMsg(mmha.getResources().getString(R.string.modpkg_space_warning, getTotalSize(), avail), false);
        } else {
            return null;
        }
    }

    private void install(Activity activity) {

        InstallTask it = new InstallTask(msubtype, activity, getModPackageInfo(), msrcFile, mpkgFile);
        it.execute();


    }

    public ModPackageInfo getModPackageInfo() {
        return mmpi;
    }

    public File getSourceFile() {
        return msrcFile;
    }

    public interface onModPackageLoadDoneListener {
        void onSuccess();

        void onFail(Throwable t);

        AppCompatActivity getActivity();
    }

    private static class InstallTask extends AsyncTask<Void, Integer, Boolean> {

        private Exception e;
        private String mainPath;
        private AlertDialog ad;
        private int count;
        private int totalcount;
        private View dialogView;
        private TextView stat;
        private ProgressBar progressbar;
        private DialogMonitor dm;
        private Activity mactivity;
        private ModPackageInfo mmpi;
        private File msrcFile;
        private ZipFile mpkgFile;
        private int mSubType;
        private HashSet<String> fileSet = new HashSet<String>();

        protected InstallTask(int subType, Activity activity, ModPackageInfo mpi, File srcFile, ZipFile pkgFile) {
            mSubType = subType;
            mactivity = activity;
            mmpi = mpi;
            msrcFile = srcFile;
            mpkgFile = pkgFile;
            mainPath = getPath(mmpi.getModType(), subType, (ModHelperApplication) mactivity.getApplication());
        }

        @Override
        protected Boolean doInBackground(Void[] p1) {
            if (ModPackageInfo.Versions.VER_0 == mmpi.getModTargetVer() || ModPackageInfo.Versions.VER_1 == mmpi.getModTargetVer()) {
                if (mmpi.getModType().equals(ModPackageInfo.MOSTYPE_CUSTOMSHIPNAME)) {
                    return patchShipName();
                }
                return installModVer0();
            } else {
                if (mmpi.getModType().equals(ModPackageInfo.MOSTYPE_CUSTOMSHIPNAME)) {
                    return patchShipName();
                }
                return installModVer0();
            }


        }

        private boolean patchShipName() {
            publishProgress(0);
            ZipFile zipFile = null;
            try {
                zipFile = new ZipFile(msrcFile);
                ZipEntry ze = zipFile.getEntry(FILE_CUSTOMSHIPNAME_PATCH);
                if (ze == null) {
                    throw new IOException("Could not found patch:" + FILE_CUSTOMSHIPNAME_PATCH);
                }
                Source source = Okio.source(zipFile.getInputStream(ze));
                if (source == null) {
                    throw new IOException("Failed to read patch from file");
                }
                BufferedSource bufferedSource = Okio.buffer(source);
                return CustomShipNameHelper.getInstance().patch(bufferedSource);
            } catch (Exception ex) {
                Logger.e(ex, null);
                ex.printStackTrace();
                e = ex;
            } finally {
                if (zipFile != null) {
                    try {
                        zipFile.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            return false;
        }

        private boolean installModVer0() {
            try {
                ZipEntry ze;
                File targetFile;
                ZipInputStream zis = new ZipInputStream(new FileInputStream(msrcFile));
                Source source;
                Logger.i("Starting to install Mod package");
                while ((ze = zis.getNextEntry()) != null) {
                    //不解压mod描述文件
                    if (ze.getName().equals(FILE_MODINFO) || ze.getName().equals(FILE_MODPREVIEW)) {
                        continue;
                    }
                    //判断获取到的Entry是否为目录
                    if (ze.isDirectory()) {
                        //若是，创建目录结构
                        targetFile = new File(mainPath, ze.getName());
                        Logger.i("Creating file path:%s", targetFile.getPath());
                        Utils.ensureFileParent(targetFile);
                        if (targetFile.isFile()) {
                            targetFile.delete();
                        }
                        targetFile.mkdirs();
                        count++;
                        publishProgress(count);
                    }
                    //非目录则为文件
                    else {
                        //写出文件
                        targetFile = new File(mainPath, ze.getName());
                        if (targetFile.exists()) {
                            ModPackageManagerV2.INSTANCE.renameConflict(ze.getName());
                        }
                        Logger.i("Writing file:%s", targetFile.getPath());
                        Utils.ensureFileParent(targetFile);
                        //若写出的目标文件已为目录，删除
                        if (targetFile.isDirectory()) {
                            Utils.delDir(targetFile);
                        }
                        //输出文件，使用Okio
                        Sink s = Okio.sink(targetFile);
                        BufferedSink bs = Okio.buffer(s);
                        source = Okio.source(mpkgFile.getInputStream(ze));
                        bs.writeAll(source);
                        ModPackageManagerV2.INSTANCE.onFileInstalled(ze.getName());
                        bs.flush();
                        bs.close();
                        source.close();
                        fileSet.add(ze.getName());
                        count++;
                        publishProgress(count);
                    }
                    zis.closeEntry();

                }
                zis.close();
            } catch (Exception e) {
                e.printStackTrace();
                Logger.d(e);
                this.e = e;
                ModPackageManagerV2.INSTANCE.rollback();
                return false;
            }
            return true;
        }


        @Override
        protected void onPreExecute() {
            dialogView = mactivity.getLayoutInflater().inflate(R.layout.dialog_installmodpkg, null);
            stat = dialogView.findViewById(R.id.dialoginstallmodpkgStatus);
            progressbar = dialogView.findViewById(R.id.dialoginstallmodpkgProgress);
            AlertDialog.Builder adb = new AlertDialog.Builder(mactivity);
            adb.setTitle(R.string.please_wait)
                    .setView(dialogView)
                    .setPositiveButton(R.string.close, null)
                    .setCancelable(false);

            ad = adb.create();
            ad.setCanceledOnTouchOutside(false);
            dm = new DialogMonitor(ad);
            ad.setOnShowListener(dm);
            ad.show();

        }

        @Override
        protected void onPostExecute(Boolean result) {
            progressbar.setProgress(progressbar.getMax());
            dm.ondone();
            //Prevent illegal state
            if (result) {
                ad.setTitle(R.string.success);
                stat.setText(R.string.success);
                ModPackageManagerV2.INSTANCE.postInstall(mmpi.getVersion());
            } else {
                ad.setTitle(R.string.error);
                String s = mactivity.getText(R.string.failed) +
                        ":" +
                        "\n" +
                        e.getMessage();
                stat.setText(s);
                ModPackageManagerV2.INSTANCE.onInstallFail();
            }
            super.onPostExecute(result);
        }

        @Override
        protected void onProgressUpdate(Integer[] values) {
            super.onProgressUpdate(values);
            if (totalcount == 0) {
                totalcount = mpkgFile.size();
                progressbar.setMax(totalcount);
                progressbar.setIndeterminate(false);
                progressbar.setProgress(0);
                stat.setText(R.string.installing);
            }
            progressbar.setProgress(values[0]);


        }

        private class DialogMonitor implements DialogInterface.OnShowListener {
            private AlertDialog alertdialog;
            private Button button;
            private int color;

            public DialogMonitor(AlertDialog ad) {
                alertdialog = ad;
            }

            public void ondone() {
                button.setTextColor(color);
                button.setClickable(true);
            }

            @Override
            public void onShow(DialogInterface p1) {
                button = alertdialog.getButton(ad.BUTTON_POSITIVE);
                button.setOnClickListener(view -> ad.dismiss()
                );
                color = button.getCurrentTextColor();
                button.setClickable(false);
                button.setTextColor(Color.GRAY);
            }
        }

    }

    private static class ErrorMsg {
        private String message;
        private boolean couldOverride;

        public ErrorMsg(String message, boolean couldOverride) {
            this.message = message;
            this.couldOverride = couldOverride;
        }

        public String getMessage() {
            return message;
        }

        public boolean isCouldOverride() {
            return couldOverride;
        }

    }

    private static class ModPackageChecker {
        private static Handler UIHandler;
        private int pos = 0;
        private Activity activity;
        private AlertDialog dialog;
        private ModPackageInstallHelper parent;
        private boolean isCVpack = false;
        private int subtype = 0;

        @SuppressLint("HandlerLeak")
        public ModPackageChecker(Activity activity, ModPackageInstallHelper parent) {
            this.activity = activity;
            this.parent = parent;
            UIHandler = new Handler() {
                @SuppressLint("HandlerLeak")
                @Override
                public void handleMessage(@NonNull Message msg) {
                    super.handleMessage(msg);
                    AlertDialog.Builder adb = new AlertDialog.Builder(activity);
                    AlertDialog ad = null;
                    switch (msg.what) {
                        case Action.SHOW_CV_SELECTION:
                            //args:type
                            subtype = SUBTYPE_CV_OFFSET;
                            adb.setTitle(R.string.modpkg_cv_to_replace)
                                    .setSingleChoiceItems(R.array.cv_types, 0, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            subtype += which;
                                        }
                                    })
                                    .setNegativeButton(R.string.cancel, (dialog, which) -> {
                                        close();
                                    })
                                    .setPositiveButton(R.string.ok, (dialog, which) -> {
                                        currentAction(subtype);
                                        // TODO: Implement this method
                                    });
                            adb.create().show();
                            break;
                        case Action.SHOW_WARNING:
                            //args:null
                            adb.setMessage((int) msg.obj);
                            adb.setCancelable(false);
                            adb.setPositiveButton(R.string.ok, (dialog, which) -> {
                                currentAction((Object) null);
                            });
                            adb.setNegativeButton(R.string.cancel, (dialog, which) -> {
                                close();
                            });
                            ad = adb.create();
                            ad.setCanceledOnTouchOutside(false);
                            ad.show();
                            break;
                        case Action.SHOW_ERROR:
                            //Exit worker
                            adb.setTitle(R.string.error);
                            adb.setMessage(((ErrorMsg) msg.obj).getMessage());
                            adb.setCancelable(false);
                            adb.setNegativeButton(R.string.close, (dialog, which) -> {
                                close();
                            });
                            ad = adb.create();
                            ad.setCanceledOnTouchOutside(false);
                            ad.show();
                            break;
                    }
                }
            };
        }

        void next() {
            pos++;
            currentAction();
        }

        private void currentAction() {
            ErrorMsg msg = null;
            switch (pos) {
                case 0:
                    msg = parent.checkAvailSpace();
                    if (msg == null) {
                        next();
                    } else {
                        UIHandler.sendMessage(UIHandler.obtainMessage(Action.SHOW_ERROR, msg));
                    }
                    break;
                case 1:
                    if (!parent.mmpi.hasAllFeature()) {
                        UIHandler.sendMessage(UIHandler.obtainMessage(Action.SHOW_WARNING, R.string.modpkg_ver_warning));
                    } else {
                        next();
                    }
                    break;
                case 2:
                    isCVpack = parent.mmpi.getModType().equals(ModPackageInfo.MODTYPE_CV);
                    if (isCVpack) {
                        UIHandler.sendEmptyMessage(Action.SHOW_CV_SELECTION);
                    } else {
                        next();
                    }
                    break;
                case 3:
                    ModPackageManagerV2.QueryResult result = ModPackageManagerV2.INSTANCE.checkInstall(parent.getModPackageInfo().getModName(), parent.getModPackageInfo().getModType(), getSubType(isCVpack ? subtype : SUBTYPE_NULL), parent.getModPackageInfo().getVersion(), parent.filesSet);
                    if (result.getResult() == ModPackageManagerV2.QueryResult.RESULT_CONFLICT) {
                        UIHandler.sendMessage(UIHandler.obtainMessage(Action.SHOW_WARNING, R.string.modpkg_conflict));
                    } else {
                        next();
                    }
                    break;
                case 4:
                    if (ModPackageManagerV2.INSTANCE.requestInstall(parent.mmpi.getModName(), parent.mmpi.getModType(), getSubType(subtype))) {
                        parent.install(activity);
                        pos = 0;
                    } else {
                        UIHandler.sendMessage(UIHandler.obtainMessage(Action.SHOW_ERROR, new ErrorMsg("Installation in progress!", false)));
                    }
                    break;
            }
        }

        private void currentAction(Object... args) {
            switch (pos) {
                //Assume to satisfy version or types requirement
                case 1:
                case 2:
                case 3:
                    next();
                    break;
            }
        }

        protected void close() {
            activity = null;
            UIHandler = null;
        }

        /**
         * Starts this worker;
         */
        protected void start() {
            currentAction();
        }

        private static final class Action {
            private static final int SHOW_WARNING = 100;
            private static final int SHOW_ERROR = 101;
            private static final int SHOW_CV_SELECTION = 102;
        }
    }
}
