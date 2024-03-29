package com.chh2000day.navalcreed.modhelper;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okio.BufferedSource;
import okio.Okio;
import okio.Source;

public class AboutFragment extends ModFragment {

    private View v;
    private int selectedItem = 0;
    private String deviceId;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getMainActivity().getModHelperApplication().getPkgNameNum(getMainActivity().getModHelperApplication().getMainSharedPreferences().getString(getMainActivity().getModHelperApplication().KEY_PKGNAME, getMainActivity().getModHelperApplication().CN));
        v = inflater.inflate(R.layout.about_fragment, null);
        deviceId = ((Main) getActivity()).getDevId();
        Button license = v.findViewById(R.id.aboutfragmentLicense);
        Button pkgname = v.findViewById(R.id.aboutfragmentButtonselectpkg);
        TextView mtextview = v.findViewById(R.id.aboutfragmentTextView);
        Button donate = v.findViewById(R.id.aboutfragmentButtonDonate);
        if (!isChineseEnvironment()) {
            donate.setVisibility(View.GONE);
        } else {
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
                    return true;
                };
                ali.setOnLongClickListener(listener);
                wechat.setOnLongClickListener(listener);
                adb.setTitle("捐赠")
                        .setView(diaView)
                        .setPositiveButton(R.string.ok, null);
                adb.create().show();
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
            });
        }
        mtextview.setText(new StringBuilder().append(getMainActivity().getModHelperApplication().getVersionName())
                .append(" ")
                .append(String.valueOf(BuildConfig.BuildVersion))
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
            } catch (IOException ignored) {
            }
        });
        license.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View p1) {
                SharedPreferences sp = getMainActivity().getModHelperApplication().getMainSharedPreferences();
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
            adb.setSingleChoiceItems(getMainActivity().getModHelperApplication().pkgnames, getMainActivity().getModHelperApplication().getPkgNameNum(getMainActivity().getModHelperApplication().getMainSharedPreferences().getString(getMainActivity().getModHelperApplication().KEY_PKGNAME, getMainActivity().getModHelperApplication().CN)), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface p1, int p2) {
                    selectedItem = p2;
                }
            });
            adb.setPositiveButton(R.string.ok, (dialogInterface, p2) -> {
                getMainActivity().getModHelperApplication().getMainSharedPreferences().edit().putString(getMainActivity().getModHelperApplication().KEY_PKGNAME, getMainActivity().getModHelperApplication().getPkgNameByNum(selectedItem)).apply();
            });
            adb.setNegativeButton(R.string.cancel, null);
            adb.create().show();
        });
        return v;
    }

    private boolean isChineseEnvironment() {
        return getResources().getConfiguration().locale.getLanguage().contains("zh");
    }

    @Override
    public void onResume() {
        super.onResume();
        selectedItem = getMainActivity().getModHelperApplication().getPkgNameNum(getMainActivity().getModHelperApplication().getMainSharedPreferences().getString(getMainActivity().getModHelperApplication().KEY_PKGNAME, getMainActivity().getModHelperApplication().CN));
    }

    @Override
    public boolean uninstallMod() {
        //Nothing to do
        return false;
    }
}
