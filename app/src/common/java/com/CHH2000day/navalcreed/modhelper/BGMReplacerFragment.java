package com.CHH2000day.navalcreed.modhelper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;

public class BGMReplacerFragment extends ModFragment {

    public static final int TYPE_HARBOR = 10;
    public static final int TYPE_LOADING = 11;
    public static final int TYPE_BATTLESTART = 12;
    public static final int TYPE_BATTLEHEAT = 13;
    public static final int TYPE_BATTLEEND = 14;
    public static final int TYPE_BATTLEVICTORY = 15;
    public static final int TYPE_BATTLEFAIL = 16;
    private static final int MUSICCOUNT_HARBOR = 8;
    private static final int MUSICCOUNT_LOADING = 3;
    private static final int MUSICCOUNT_BATTLESTART = 4;
    private static final int MUSICCOUNT_BATTLEHEAT = 8;
    private static final int MUSICCOUNT_BATTLEEND = 3;
    private static final int MUSICCOUNT_BATTLEVICTORY = 4;
    private static final int MUSICCOUNT_BATTLEFAIL = 2;
    private static final String[] SCENE = {"Harbor", "Loading", "BattleStart", "BattleHeat", "BattleEnd", "Victory", "Fial"/*因为你游程序员把Fail打成Fial了，所以将错就错了*/};
    private static final String[] FILENAMES_UNIVERSAL = {"1", "2", "3", "4", "5", "6", "7", "8"};
    private static final String[] FILENAMES_BATTLEFAIL = {"Danger", "Fail"};
    private static final String[] FILENAMES_LOADING = {"Loading", "Login", "Queuing"};
    private static final int TEXTVIEW_RES_ID = R.layout.support_simple_spinner_dropdown_item;
    private static final int QUERY_CODE = 2;
    private static final String PREFIX = "CUSTOMBGM";
    //private static final String[] SCENE_TOSHOW={"港口","加载音乐","战斗开始","战斗激战","战斗即将结束","战斗胜利","战斗失败"};
    private static String[] scene_toshow;
    //private static final String[] FILENAMES_BATTLEFAIL_TOSHOW={"即将失败","失败"};
    private static String[] filenames_battlefail_toshow;
    //private static final String[] FILENAMES_LOADING_TOSHOW={"加载中","登录","匹配中"};
    private static String[] filenames_loading_toshow;
    private View v;
    private Spinner mSceneSpinner, mFileNameSpinner;
    private int curr_scene, curr_type, curr_music;
    private Button select, remove, update;
    private TextView mtextview;
    //private String fileformat;
    private Uri srcfile;

    private static String[] getFileNameStringsToShow(int type) {
        if (type == TYPE_BATTLEFAIL) {
            return filenames_battlefail_toshow;
        }
        if (type == TYPE_LOADING) {
            return filenames_loading_toshow;
        }
        return getFileNameStrings(type);
    }

    private static String[] getFileNameStrings(int type) {
        if (type == TYPE_BATTLEFAIL) {
            return FILENAMES_BATTLEFAIL;
        }
        if (type == TYPE_LOADING) {
            return FILENAMES_LOADING;
        }
        int count = 0;
        switch (type) {
            case TYPE_HARBOR:
                count = MUSICCOUNT_HARBOR;
                break;
            case TYPE_BATTLESTART:
                count = MUSICCOUNT_BATTLESTART;
                break;
            case TYPE_BATTLEHEAT:
                count = MUSICCOUNT_BATTLEHEAT;
                break;
            case TYPE_BATTLEEND:
                count = MUSICCOUNT_BATTLEEND;
                break;
            case TYPE_BATTLEVICTORY:
                count = MUSICCOUNT_BATTLEVICTORY;
                break;
        }
        return Arrays.copyOf(FILENAMES_UNIVERSAL, count);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.bgmreplacer_fragment, null);
        mSceneSpinner = (Spinner) v.findViewById(R.id.bgmreplacerScene);
        mFileNameSpinner = (Spinner) v.findViewById(R.id.bgmreplacerMusic);
        mtextview = (TextView) v.findViewById(R.id.bgmreplacerText);
        select = (Button) v.findViewById(R.id.bgmreplacerSelect);
        remove = (Button) v.findViewById(R.id.bgmreplacerRemove);
        update = (Button) v.findViewById(R.id.bgmreplacerUpdate);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initValues();
        initview();
    }

    @Override
    public void onResume() {
        super.onResume();
        //showAd(v);
    }

    private void initValues() {
        Resources res = getResources();
        scene_toshow = res.getStringArray(R.array.bgm_scene_toshow);
        //private static final String[] FILENAMES_UNSELECTED={"请选择情景"};
        String[] filenames_unselected = res.getStringArray(R.array.bgm_select_a_scene);
        filenames_loading_toshow = res.getStringArray(R.array.bgm_loading_toshow);
        filenames_battlefail_toshow = res.getStringArray(R.array.bgm_battlefail_toshow);
    }

    private void initview() {
        select.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View p1) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(intent, getString(R.string.select_a_file_selector)), QUERY_CODE);
            }
        });
        remove.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View p1) {
                AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
                adb.setTitle(R.string.notice)
                        .setMessage(getString(R.string.confirm_to_remove_changes_to_parta) +
                                scene_toshow[curr_scene] +
                                getString(R.string.confirm_to_remove_changes_to_partb))
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface p1, int p2) {

                                //注销所有缓存文件
                                FormatHelperFactory.denyAllCaches();
                                HashSet<String> mods = new HashSet<>();
                                String prefix = getModScenePrefix(curr_scene);
                                for (ModPackageManagerV2.ModInstallationInfo mod : ModPackageManagerV2.INSTANCE.getMods()) {
                                    if (mod.getName().startsWith(prefix)) {
                                        mods.add(mod.getName());
                                    }
                                }
                                for (String modName : mods) {
                                    ModPackageManagerV2.INSTANCE.uninstall(modName);
                                }
                                Snackbar.make(v, R.string.success, Snackbar.LENGTH_LONG).show();
                            }
                        })
                        .create()
                        .show();
            }
        });
        remove.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View p1) {
                AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
                adb.setTitle(R.string.notice)
                        .setMessage(R.string.remove_all_bgm)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface p1, int p2) {
                                HashSet<String> mods = new HashSet<>();
                                for (ModPackageManagerV2.ModInstallationInfo mod : ModPackageManagerV2.INSTANCE.getMods()) {
                                    if (mod.getName().startsWith(PREFIX)) {
                                        mods.add(mod.getName());
                                    }
                                }
                                for (String modName : mods) {
                                    ModPackageManagerV2.INSTANCE.uninstall(modName);
                                }
                                Snackbar.make(v, R.string.success, Snackbar.LENGTH_LONG).show();
                            }
                        })
                        .create()
                        .show();
                return true;
            }
        });
        update.setOnClickListener(new OnClickListener() {

            View dialogView;
            TextView progress;
            ProgressBar pb;
            long starttime;

            @Override
            public void onClick(View p1) {
                if (null == srcfile) {
                    Snackbar.make(v, R.string.source_file_cannot_be_empty, Snackbar.LENGTH_LONG).show();
                    return;
                }
                String path = getPath(curr_scene, curr_type, curr_music, Utils.FORMAT_WAV);
                String name = getModName(curr_scene, curr_type, curr_music);
                HashSet<String> hashSet = new HashSet<>();
                hashSet.add(path);
                if (ModPackageManagerV2.INSTANCE.checkInstall(name, ModPackageInfo.MODTYPE_BGM, ModPackageInfo.SUBTYPE_EMPTY, -10, hashSet).getResult() == ModPackageManagerV2.QueryResult.RESULT_CONFLICT) {
                    AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
                    adb.setTitle(R.string.notice)
                            .setMessage(R.string.modpkg_install_ovwtmsg)
                            .setNegativeButton(R.string.cancel, null)
                            .setPositiveButton(R.string.uninstall_and_continue, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface p1, int p2) {
                                    install(name, path);
                                }
                            });
                    adb.create().show();
                } else {
                    install(name, path);
                }
            }

            private void install(String name, String path) {
                //开始创建进度对话框

                dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_transcode, null);
                progress = (TextView) dialogView.findViewById(R.id.dialogtranscodeTextView);
                pb = (ProgressBar) dialogView.findViewById(R.id.dialogtranscodeProgressBar);
                AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
                adb.setTitle(R.string.please_wait)
                        .setView(dialogView)
                        .setCancelable(false)
                        .setPositiveButton(R.string.close, null);
                final AlertDialog ad = adb.create();
                final Monitor mon = new Monitor(ad);
                ad.setOnShowListener(mon);
                ad.setCancelable(false);
                @android.annotation.SuppressLint("HandlerLeak") final Handler h = new Handler() {
                    public void handleMessage(Message msg) {
                        if (!isAdded()) return;
                        switch (msg.what) {
                            case AudioFormatHelper.STATUS_START:
                                //无异常
                                progress.setText(R.string.transcode_starting);
                                break;
                            case AudioFormatHelper.STATUS_LOADINGFILE:
                                //操作出现异常
                                progress.setText(R.string.transcode_getting_audio_track);
                                break;
                            case AudioFormatHelper.STATUS_TRANSCODING:
                                progress.setText(R.string.transcode_transcoding);
                                break;
                            case AudioFormatHelper.STATUS_WRITING:
                                progress.setText(R.string.transcode_writing);
                                break;
                            case AudioFormatHelper.STATUS_DONE:
                                long usedtime = System.currentTimeMillis() - starttime;
                                pb.setIndeterminate(false);
                                pb.setProgress(100);
                                ad.setTitle(R.string.success);
                                if (isAdded()) {
                                    progress.setText(getString(R.string.transcode_success, usedtime));
                                    mon.onDone();
                                } else {
                                    ad.dismiss();
                                }
                                break;
                            case AudioFormatHelper.STATUS_ERROR:
                                String s = progress.getText().toString();
                                Exception e = (Exception) msg.obj;
                                long l = System.currentTimeMillis() - starttime;
                                progress.setText(getString(R.string.transcode_failed, s, l, Utils.getErrMsg(e)));
                                pb.setIndeterminate(false);
                                pb.setProgress(100);
                                ad.setTitle(R.string.failed);
                                mon.onDone();
                                break;
                            case 1:
                                //停用该功能以避免IllegalArgumentException
                                //Snackbar.make ( v, (String)msg.obj, Snackbar.LENGTH_LONG ).show ( );
                                break;
                        }
                    }
                };
                ad.show();
                final AudioFormatHelper afh = FormatHelperFactory.getAudioFormatHelper(srcfile, getActivity());
                new Thread() {
                    public void run() {
                        //try
                        //{
							/*音频转码，移除原代码
							 Utils.copyFile ( getActivity ( ).getContentResolver ( ).openInputStream ( srcfile ), getTargetFile ( curr_scene, curr_type, curr_music, Utils.FORMAT_WAV ) );
							 */
                        starttime = System.currentTimeMillis();
                        if (ModPackageManagerV2.INSTANCE.requestInstall(name, ModPackageInfo.MODTYPE_BGM, ModPackageInfo.SUBTYPE_EMPTY)) {
                            File file = getTargetFile(curr_scene, curr_type, curr_music, Utils.FORMAT_WAV);
                            Utils.ensureFileParent(file);
                            if (file.exists()) {
                                ModPackageManagerV2.INSTANCE.renameConflict(path);
                            }
                            String result = afh.compressToWav(file, h);
                            afh.recycle();
                            boolean isSuccess = AudioFormatHelper.RESULT_OK.equals(result);
                            result = isSuccess ? getString(R.string.success) : result;
                            ModPackageManagerV2.INSTANCE.onFileInstalled(path);
                            if (isSuccess) {
                                ModPackageManagerV2.INSTANCE.postInstall(-10);
                            } else {
                                ModPackageManagerV2.INSTANCE.onInstallFail();
                            }
                            h.sendMessage(h.obtainMessage(1, result));
                        } else {
                            h.sendMessage(h.obtainMessage(AudioFormatHelper.STATUS_ERROR, "Start error"));
                        }

                        //}
							/*catch (IOException e)
							 {
							 h.sendMessage ( h.obtainMessage ( 1, e ) );
							 }*/

                    }
                }.start();

            }
        });
        //配置场景选择的适配器
        mSceneSpinner.setAdapter(new ArrayAdapter<String>(getActivity(), TEXTVIEW_RES_ID, scene_toshow));
        //初始化文件名的适配器
        FileNameAdapter mfilenameadapter = FileNameAdapter.getInstance(getActivity(), TEXTVIEW_RES_ID, TYPE_HARBOR);
        mFileNameSpinner.setAdapter(mfilenameadapter);
        mSceneSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> p1, View p2, int pos, long p4) {
                curr_scene = pos;
                curr_type = pos + 10;
                mFileNameSpinner.setAdapter(FileNameAdapter.getInstance(getActivity(), TEXTVIEW_RES_ID, curr_type));
            }

            @Override
            public void onNothingSelected(AdapterView<?> p1) {

            }
        });
        mFileNameSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> p1, View p2, int p3, long p4) {
                curr_music = p3;
            }

            @Override
            public void onNothingSelected(AdapterView<?> p1) {

            }
        });
    }

    private String getModName(int scene, int type, int num) {
        return PREFIX + '-' + SCENE[scene] + '-' + getFileName(type, num);
    }

    private String getModScenePrefix(int scene) {
        return PREFIX + '-' + SCENE[scene];
    }

    private String getPath(int scene, int type, int num, String format) {
        return SCENE[scene] +
                File.separatorChar +
                getFileName(type, num) +
                format;
    }

    private File getTargetFile(int scene, int type, int num, String format) {
        return new File(
                getMainActivity().getModHelperApplication().getResFilesDirPath() +
                        File.separatorChar +
                        "sound" +
                        File.separatorChar +
                        "Music" +
                        File.separatorChar +
                        getPath(scene, type, num, format)

        );
    }

    private String getFileName(int type, int num) {
        return getFileNameStrings(type)[num];
    }

    protected String identifyFormat(InputStream in, boolean closeStream) throws IOException {
        return Utils.identifyFormat(in, closeStream);
    }

    @Override
    public boolean uninstallMod() {
        //注销所有缓存
        FormatHelperFactory.denyAllCaches();
        ModPackageManager.getInstance().postUninstall(ModPackageInfo.MODTYPE_BGM, ModPackageInfo.SUBTYPE_EMPTY);
        return Utils.delDir(getTargetFile(curr_scene, curr_type, curr_music, Utils.FORMAT_WAV).getParentFile().getParentFile());
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) return;
        if (requestCode == QUERY_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            if (data != null) {
                if (data.getData() == null) {
                    Snackbar.make(v, R.string.source_file_cannot_be_empty, Snackbar.LENGTH_LONG).show();
                    return;
                }
				/*测试音频转码,跳过音频格式验证
				 try
				 {
				 s = identifyFormat ( getActivity ( ).getContentResolver ( ).openInputStream ( data.getData ( ) ), true ) ;
				 }
				 catch (IOException e)
				 {Snackbar.make ( v, e.getMessage ( ), Snackbar.LENGTH_LONG ).show ( );}
				 if ( !Utils.FORMAT_WAV.equals ( s ) )
				 {
				 Snackbar.make ( v, "文件格式错误！文件不为wav编码", Snackbar.LENGTH_LONG ).show ( );
				 return;
				 }
				 else
				 {
				 srcfile = data.getData ( );
				 //fileformat=s;
				 mtextview.setText ( new StringBuilder ( ).append ( srcfile.getPath ( ) ).toString ( ) );
				 }*/
                srcfile = data.getData();
                mtextview.setText(new StringBuilder().append(srcfile.getPath()).toString());


            } else {
                Snackbar.make(v, R.string.source_file_cannot_be_empty, Snackbar.LENGTH_LONG).show();
                return;
            }
        }
    }


    private static class FileNameAdapter extends ArrayAdapter<String> {

        private static String[] data;
        private static String[] act_data;
        private static FileNameAdapter self;

        private FileNameAdapter(Context context, int textViewResourceId, String[] data) {
            super(context, textViewResourceId, data);
        }

        public static FileNameAdapter getInstance(Context context, int textViewResId, int type) {
            data = getFileNameStringsToShow(type);
            act_data = getFileNameStrings(type);
            self = new FileNameAdapter(context, textViewResId, data);
            return self;
        }

        /*
         public void reconfigure (int type)
         {
         this.clear ( );
         this.data = getFileNameStringsToShow ( type );
         this.act_data = getFileNameStrings ( type );
         this.addAll(data);
         }*/
        public String[] getCurrentData() {
            return this.act_data;
        }


    }

    public class Monitor implements DialogInterface.OnShowListener {
        private Button button;
        private AlertDialog ad;
        private int color;

        public Monitor(AlertDialog dialog) {
            ad = dialog;
        }

        public void onDone() {
            if (!ad.isShowing()) return;
            button.setTextColor(color);
            button.setClickable(true);
        }

        @Override
        public void onShow(DialogInterface p1) {
            button = ad.getButton(ad.BUTTON_POSITIVE);
            button.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View p1) {
                    ad.dismiss();
                }
            });
            color = button.getCurrentTextColor();
            button.setClickable(false);
            button.setTextColor(Color.GRAY);
        }


    }

}
