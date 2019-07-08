package com.CHH2000day.navalcreed.modhelper;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.qy.sdk.Interfaces.RDInterface;
import com.qy.sdk.rds.VideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okio.BufferedSource;
import okio.Okio;
import okio.Source;

public class AboutFragment extends Fragment {

    private View v;
    private int selectedItem = 0;
    private ModHelperApplication app;
    private String deviceId;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        app = (ModHelperApplication) getActivity().getApplication();
        app.getPkgNameNum(app.getMainSharedPrederences().getString(app.KEY_PKGNAME, app.CN));
        v = inflater.inflate(R.layout.about_fragment, null);
        deviceId = ((Main) getActivity()).getDevId();
        Button license = v.findViewById(R.id.aboutfragmentLicense);
        Button pkgname = v.findViewById(R.id.aboutfragmentButtonselectpkg);
        TextView mtextview = v.findViewById(R.id.aboutfragmentTextView);
        Button donate = v.findViewById(R.id.aboutfragmentButtonDonate);
        if (!isChineseEnvironment()) {
            donate.setVisibility(View.GONE);
        } else {
            donate.setOnLongClickListener(listener -> {
                Snackbar.make(v, "Loading ad", Snackbar.LENGTH_LONG).show();
                VideoView ad = new VideoView();
                ad.setInterface(getActivity(), new RDInterface() {
                    @Override
                    public void onLoadSuccess() {
                        super.onLoadSuccess();
                        ad.show();
                    }
                });
                return true;
            });
            donate.setOnClickListener(p1 -> {
                AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
                View diaView = inflater.inflate(R.layout.dialog_donate, null);
                final ImageView ali = diaView.findViewById(R.id.dialogdonateImageAlipay);
                final ImageView wechat = diaView.findViewById(R.id.dialogdonateImageWeChat);
                OnLongClickListener listener = p11 -> {
                    Snackbar sb = Snackbar.make(v, "二维码已保存到本地文件夹", Snackbar.LENGTH_LONG);
                    switch (p11.getId()) {
                        case R.id.dialogdonateImageAlipay:
                            try {
                                BitmapDrawable bd = (BitmapDrawable) ali.getDrawable();
                                Bitmap b = bd.getBitmap();
                                FileOutputStream fos = new FileOutputStream(new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "donat_alipay.jpg"));
                                b.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                                fos.flush();
                                fos.close();
                                sb.show();
                            } catch (IOException e) {
                            }
                            break;
                        case R.id.dialogdonateImageWeChat:
                            try {
                                BitmapDrawable bd = (BitmapDrawable) wechat.getDrawable();
                                Bitmap b = bd.getBitmap();
                                FileOutputStream fos = new FileOutputStream(new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "donat_wechat.jpg"));
                                b.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                                fos.flush();
                                fos.close();
                                sb.show();
                            } catch (IOException e) {
                            }
                            break;
                        default:
                            break;
                    }
                    // TODO: Implement this method
                    return true;
                };
                ali.setOnLongClickListener(listener);
                wechat.setOnLongClickListener(listener);
                adb.setTitle("捐赠")
                        .setView(diaView)
                        .setPositiveButton(R.string.ok, null);
                adb.create().show();
                // TODO: Implement this method
            });

        }
        if (BuildConfig.DEBUG) {
            mtextview.setText(new StringBuilder().append("Device SSAID:")
                    .append(deviceId)
                    .append("\n")
                    .append(mtextview.getText())
                    .toString());
            mtextview.setOnClickListener(p1 -> {
                ClipboardManager cmb = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                cmb.setText(deviceId);
                Snackbar.make(v, "Device id has been copied", Snackbar.LENGTH_LONG).show();
                // TODO: Implement this method
            });
        }
        mtextview.setText(new StringBuilder().append(app.getVersionName())
                .append(" ")
                .append(BuildConfig.DEBUG ? String.valueOf(app.BUILDVER) : "")
                .append("\n")
                .append(mtextview.getText())
                .toString());
        license.setOnClickListener(p1 -> {

            try {
                Source s = Okio.source(getResources().getAssets().open("LICENSE"));
                BufferedSource bs = Okio.buffer(s);
                AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
                adb.setTitle(R.string.about_license)
                        .setMessage(bs.readUtf8())
                        .setPositiveButton(R.string.ok, null);
                bs.close();
                adb.create().show();
            } catch (IOException e) {
            }
            // TODO: Implement this method
        });
        license.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View p1) {
                // TODO: Implement this method
                SharedPreferences sp = app.getMainSharedPrederences();
                if (KeyUtil.checkKeyFormat(sp.getString(Main.KEY_AUTHKEY, ""))) {
                    //If local key is avail
                    boolean status = ((Main) getActivity()).isUseAlphaChannel();
                    Snackbar.make(v, "Check alpha version:" + String.valueOf(!status), Snackbar.LENGTH_LONG).show();
                    ((Main) getActivity()).setUseAlphaChannel(!status);
                } else {
                    ((Main) getActivity()).showKeyDialog();
                }

                return false;
            }
        });

        pkgname.setOnClickListener(p1 -> {

            AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
            adb.setTitle(R.string.select_target_package);
            adb.setSingleChoiceItems(app.pkgnames, app.getPkgNameNum(app.getMainSharedPrederences().getString(app.KEY_PKGNAME, app.CN)), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface p1, int p2) {
                    selectedItem = p2;
                    // TODO: Implement this method
                }
            });
            adb.setPositiveButton(R.string.ok, (dialogInterface, p2) -> {
                app.getMainSharedPrederences().edit().putString(app.KEY_PKGNAME, app.getPkgNameByNum(selectedItem)).apply();
                // TODO: Implement this method
            });
            adb.setNegativeButton(R.string.cancel, null);
            adb.create().show();

            // TODO: Implement this method
        });

        // TODO: Implement this method
        return v;
    }

    private boolean isChineseEnvironment() {
        return getResources().getConfiguration().locale.getLanguage().contains("zh");
    }

    @Override
    public void onResume() {
        // TODO: Implement this method
        super.onResume();
        selectedItem = app.getPkgNameNum(app.getMainSharedPrederences().getString(app.KEY_PKGNAME, app.CN));
    }

}
