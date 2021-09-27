package com.chh2000day.navalcreed.modhelper;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.util.HashSet;

public class BGReplacerFragment extends ModFragment {


    private static final String[] filename = {"loadingbg1.jpg", "loadingbg2.jpg", "loadingbg3.jpg"};
    private static final String[] cateoty = {"loading", "loadingmap", "matching"};
    private static final String PREFIX = "CUSTOM_BG";
    private String parent_path/*="/sdcard/Android/data/com.tencent.navalcreed/files/pic"*/;
    private TextView picname;
    private Bitmap ba;
    private int cat = 0, filepos = 0;
    private View v;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.bgreplacer_fragment, null);
        Spinner category = v.findViewById(R.id.bgreplacerSpinner1);
        category.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> p1, View p2, int p3, long p4) {
                cat = p3;
            }

            @Override
            public void onNothingSelected(AdapterView<?> p1) {
            }
        });
        Spinner file = v.findViewById(R.id.bgreplacerSpinner2);

        file.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> p1, View p2, int p3, long p4) {
                filepos = p3;
            }

            @Override
            public void onNothingSelected(AdapterView<?> p1) {
            }
        });
        picname = v.findViewById(R.id.bgreplacerPic);
        Button btnrm = v.findViewById(R.id.bgreplacer_remove);
        Button update = v.findViewById(R.id.bgreplacerbtn_update);
        Button selpic = v.findViewById(R.id.bgreplacerbtn_select);
        selpic.setOnClickListener(p1 -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, 1);
        });
        btnrm.setOnClickListener(p1 -> {
            AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
            adb.setTitle(R.string.notice)
                    .setMessage(R.string.confirm_to_remove_all_changes)
                    .setNegativeButton(R.string.cancel, null);
            adb.setPositiveButton(R.string.ok, (dialogInterface, p2) -> {
                removeChanges();
            });
            adb.create().show();
        });
        update.setOnClickListener(new OnClickListener() {

            private void install(String name, String path) {
                File target = new File(parent_path, path);
                _FileUtilsKt.mkdirCompatible(target);
                if (ModPackageManagerV2.INSTANCE.requestInstall(name, ModPackageInfo.MODTYPE_BACKGROUND, ModPackageInfo.SUBTYPE_EMPTY)) {
                    try {
                        if (_FileUtilsKt.existsCompatible(target)) {
                            ModPackageManagerV2.INSTANCE.renameConflict(path);
                        }
                        OutputStream fileOutputStream = getContext().getContentResolver().openOutputStream(_FileUtilsKt.toDocumentFile(target).getUri());
                        ba.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                        fileOutputStream.flush();
                        fileOutputStream.close();
                        ModPackageManagerV2.INSTANCE.onFileInstalled(path);
                        ModPackageManagerV2.INSTANCE.postInstall(-10);
                        Snackbar.make(v, R.string.success, Snackbar.LENGTH_LONG).show();
                    } catch (Exception e) {
                        String message = e.getMessage();
                        if (message == null) {
                            message = "Unknown reason";
                        }
                        Snackbar.make(v, message, Snackbar.LENGTH_LONG).show();
                    }
                }
            }

            @SuppressWarnings("unchecked")
            @Override
            public void onClick(View p1) {
                if (ba == null) {
                    Snackbar.make(v, R.string.source_file_cannot_be_empty, Snackbar.LENGTH_LONG).show();
                } else {
                    String name = PREFIX + '-' + cateoty[cat] + '-' + filename[filepos];
                    String path = cateoty[cat].toUpperCase() + File.separatorChar + filename[filepos].toUpperCase();
                    HashSet fSet = new HashSet<String>();
                    fSet.add(name);
                    if (ModPackageManagerV2.INSTANCE.checkInstall(name, ModPackageInfo.MODTYPE_BACKGROUND, ModPackageInfo.SUBTYPE_EMPTY, -10, fSet).getResult() == ModPackageManagerV2.QueryResult.RESULT_CONFLICT) {
                        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
                        adb.setTitle(R.string.notice)
                                .setMessage(R.string.modpkg_install_ovwtmsg)
                                .setNegativeButton(R.string.cancel, null)
                                .setPositiveButton(R.string.cont, (p11, p2) -> {
                                    install(name, path);
                                });
                        adb.create().show();
                    } else {
                        install(name, path);
                    }
                }
            }
        });
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        parent_path = ModPackageInstallHelper.getPath(ModPackageInfo.MODTYPE_BACKGROUND, ModPackageInstallHelper.SUBTYPE_NULL, getMainActivity().getModHelperApplication());
    }

    private void removeChanges() {
        //防止误删船员头像
        //Utils.delDir(new File(abs_path));
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

    @Override
    public boolean uninstallMod() {
//        ModPackageManager.getInstance().postUninstall(ModPackageInfo.MODTYPE_BACKGROUND, ModPackageInfo.SUBTYPE_EMPTY);
//        for (String s : cateoty) {
//            Utils.delDir(new File(parent_path, s));
//        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        //showAd(v);
    }

    @Override
    public void onDestroy() {
        if (ba != null) {
            ba.recycle();
            //手动释放以防止Bitmap未被释放

        }
        super.onDestroy();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && requestCode == 1 && resultCode == AppCompatActivity.RESULT_OK) {
            try {
                if (ba != null) {
                    ba.recycle();
                    System.gc();
                    //手动释放以防止Bitmap未被释放
                }
                Uri u = data.getData();
                InputStream in = getActivity().getContentResolver().openInputStream(u);
                //ba = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(u));
                SoftReference sr = new SoftReference(BitmapFactory.decodeStream(in));
                ba = (Bitmap) sr.get();
                in.close();
                picname.setText(data.getData().toString());

            } catch (OutOfMemoryError r) {
                Snackbar.make(v, "文件过大，内存溢出", Snackbar.LENGTH_LONG).show();
            } catch (final Throwable t) {
                t.printStackTrace();
                String clazzn = t.getClass().getName();
                int p = clazzn.lastIndexOf('$');

                String clazz = (p >= 0) ? clazzn.substring(0, p) : "错误";

                Snackbar.make(v, clazz, Snackbar.LENGTH_LONG).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}
