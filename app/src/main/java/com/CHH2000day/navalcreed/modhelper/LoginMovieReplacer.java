package com.CHH2000day.navalcreed.modhelper;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class LoginMovieReplacer extends ModFragment {

    private static final String MOD_NAME = "CUSTOM_LOGINMOVIE";
    private static final String FILENAME = "loginmovie.ogv";

    private Uri srcfile;
    private static int QUERY_CODE = 2;
    private View v;
    private TextView file;
    private File target;
    private String parent_path;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.loginmoviereplacer_fragment, null);
        file = v.findViewById(R.id.loginmoviereplacerfragmentTextView);
        Button select = v.findViewById(R.id.loginmoviereplacerfragmentButtonSelect);
        Button update = v.findViewById(R.id.loginmoviereplacerfragmentButtonUpdate);
        Button remove = v.findViewById(R.id.loginmoviereplacerfragmentButtonRemove);
        // TODO: Implement this method

        select.setOnClickListener(p1 -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, getText(R.string.select_a_file_selector)), QUERY_CODE);
        });
        update.setOnClickListener(view -> {
            if (srcfile == null) {
                Snackbar.make(v, R.string.source_file_cannot_be_empty, Snackbar.LENGTH_LONG).show();
                return;
            }
            AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
            adb.setTitle(R.string.please_wait)
                    .setMessage(R.string.transcode_writing)
                    .setCancelable(false);
            final AlertDialog ad = adb.create();
            ad.setCancelable(false);
            @SuppressLint("HandlerLeak") final Handler h = new Handler() {
                public void handleMessage(Message msg) {
                    ad.dismiss();
                    switch (msg.what) {
                        case 0:
                            //无异常
                            Snackbar.make(v, R.string.success, Snackbar.LENGTH_LONG).show();
                            break;
                        case 1:
                            //操作出现异常
                            Snackbar.make(v, ((Throwable) msg.obj).getMessage(), Snackbar.LENGTH_LONG).show();
                            break;
                    }
                }
            };
            ad.show();
            new Thread() {
                public void run() {
                    try {
                        if (ModPackageManagerV2.INSTANCE.requestInstall(MOD_NAME, ModPackageInfo.MODTYPE_OTHER, ModPackageInfo.SUBTYPE_EMPTY)) {
                            Utils.ensureFileParent(getTargetFile());
                            if (getTargetFile().exists()) {
                                ModPackageManagerV2.INSTANCE.renameConflict(FILENAME);
                            }
                            Utils.copyFile(getInStream(srcfile), getTargetFile());
                            ModPackageManagerV2.INSTANCE.onFileInstalled(FILENAME);
                            ModPackageManagerV2.INSTANCE.postInstall(-10);
                            h.sendEmptyMessage(0);
                        }

                    } catch (IOException e) {
                        h.sendMessage(h.obtainMessage(1, e));
                    }

                }
            }.start();
        });

        remove.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View p1) {
                Snackbar.make(v, (ModPackageManagerV2.INSTANCE.uninstall(MOD_NAME) == 0 ? R.string.success : R.string.failed), Snackbar.LENGTH_LONG).show();
            }
        });
        showAd(v);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        //showAd(v);
    }

    private File getTargetFile() {
        if (target == null) {
            target = new File(getMainActivity().getModHelperApplication().getResFilesDir(), FILENAME);
        }
        return target;
    }

    private void doLoad(Uri uri) {
        try {
            //OGG与OGV拥有相同的magic number
            srcfile = uri;
            Logger.d("Get uri: authority:%s path:%s", srcfile.getEncodedAuthority(), srcfile.getEncodedPath());

            if (!Utils.FORMAT_OGG.equals(Utils.identifyFormat(getInStream(uri), true))) {
                srcfile = null;
                Snackbar.make(v, R.string.not_a_ogv_file, Snackbar.LENGTH_LONG).show();
            }

        } catch (IOException e) {
            srcfile = null;
            Snackbar.make(v, R.string.failed, Snackbar.LENGTH_LONG).show();
        }
    }

    private InputStream getInStream(Uri uri) throws FileNotFoundException {
        InputStream in;
        String path = Utils.resolveFilePath(uri, getContext());
        if (path != null) {
            in = new FileInputStream(path);
        } else {
            in = getContext().getContentResolver().openInputStream(uri);
        }
        return in;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != QUERY_CODE) {
            return;
        }
        if (resultCode != AppCompatActivity.RESULT_OK) {
            return;
        }
        if (data == null || data.getData() == null) {
            Snackbar.make(v, R.string.source_file_cannot_be_empty, Snackbar.LENGTH_LONG).show();
            return;
        }
        doLoad(data.getData());
        if (srcfile != null) {
            file.setText(srcfile.getPath());
        }

    }

    @Override
    public boolean uninstallMod() {
        return false;
    }
}
