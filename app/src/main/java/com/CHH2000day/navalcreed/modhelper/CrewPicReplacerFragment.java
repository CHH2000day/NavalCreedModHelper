package com.chh2000day.navalcreed.modhelper;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Objects;

public class CrewPicReplacerFragment extends ModFragment {

    private static final String PREFIX = "CUSTOM_CREWPIC";
    private View v;
    private Bitmap ba;
    private int selectedcountry = 0, selectedcrew = 0;
    private TextView selectedpic;
    private String[] countrys = {"usa", "japan", "uk", "china", "italy", "france", "ussr", "german"};
    private String parent_path;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.crew_pic_replacer, null);
        Spinner country = v.findViewById(R.id.crewpicreplacerSpinnerCountry);
        country.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> p1, View p2, int p3, long p4) {
                selectedcountry = p3;
					/*if(p3==3){
					 Snackbar.make(v,"该选项可能存在bug",Snackbar.LENGTH_LONG).show();
					 }*/
            }

            @Override
            public void onNothingSelected(AdapterView<?> p1) {
            }
        });

        Spinner num = v.findViewById(R.id.crewpicreplacerSpinnerCrew);
        num.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> p1, View p2, int p3, long p4) {
                selectedcrew = p3;
            }

            @Override
            public void onNothingSelected(AdapterView<?> p1) {
            }
        });
        Button selpic = v.findViewById(R.id.crewpicreplacerButtonSelectPic);
        selpic.setOnClickListener(p1 -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, 2);

        });
        Button removepic = v.findViewById(R.id.crewpicreplacerButtonRemove);
        removepic.setOnClickListener(p1 -> {
            AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
            adb.setTitle(R.string.notice)
                    .setMessage(R.string.confirm_to_remove_changes)
                    .setPositiveButton(R.string.remove_changes, (dialogInterface, p2) -> {
                        if (ModPackageManagerV2.INSTANCE.uninstall(getName(selectedcountry, selectedcrew))) {
                            Snackbar.make(v, R.string.success, Snackbar.LENGTH_LONG).show();
                        } else {
                            Snackbar.make(v, R.string.failed, Snackbar.LENGTH_LONG).show();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .create()
                    .show();
        });
        removepic.setOnLongClickListener(p1 -> {
            AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
            adb.setTitle(R.string.notice)
                    .setMessage(R.string.confirm_to_remove_all_changes)
                    .setPositiveButton(R.string.remove_changes, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface p1, int p2) {
                            removeChanges();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .create()
                    .show();
            return true;
        });
        Button updatepic = v.findViewById(R.id.crewpicreplacerButtonReplace);
        updatepic.setOnClickListener(new View.OnClickListener() {
            private void install(String name, String path) {
                try {
                    if (ModPackageManagerV2.INSTANCE.requestInstall(name, ModPackageInfo.MODTYPE_CREWPIC, ModPackageInfo.SUBTYPE_EMPTY)) {
                        File outFile = new File(parent_path, path);
                        if (_FileUtilsKt.existsCompatible(outFile)) {
                            ModPackageManagerV2.INSTANCE.renameConflict(path);
                        }
                        OutputStream fileOutputStream = getContext().getContentResolver().openOutputStream(_FileUtilsKt.toDocumentFile(outFile).getUri());
                        ba.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                        fileOutputStream.flush();
                        fileOutputStream.close();
                        ModPackageManagerV2.INSTANCE.onFileInstalled(path);
                        ModPackageManagerV2.INSTANCE.postInstall(-10);
                        Snackbar.make(v, R.string.success, Snackbar.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Snackbar.make(v, e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onClick(View p1) {
                if (null == ba) {
                    Snackbar.make(v, R.string.source_file_cannot_be_empty, Snackbar.LENGTH_LONG).show();
                    return;
                }
                HashSet<String> files = new HashSet<>();
                String path = getFilePath(selectedcountry, selectedcrew);
                String name = getName(selectedcountry, selectedcrew);
                files.add(path);
                if (ModPackageManagerV2.INSTANCE.checkInstall(name, ModPackageInfo.MODTYPE_CREWPIC, ModPackageInfo.SUBTYPE_EMPTY, -10, files).getResult() == ModPackageManagerV2.QueryResult.RESULT_CONFLICT) {
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
        });
        selectedpic = v.findViewById(R.id.crewpicreplacerSelectedFile);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        parent_path = ModPackageInstallHelper.getPath(ModPackageInfo.MODTYPE_CREWPIC, ModPackageInstallHelper.SUBTYPE_NULL, getMainActivity().getModHelperApplication());
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


    private String getName(int country, int num) {
        return PREFIX + '-' + countrys[country].toUpperCase() + '-' + (num + 1);
    }

    private String getFilePath(int country, int num) {
        return countrys[country] +
                File.separatorChar +
                (num + 1) +
                ".png";
    }

    private void removeChanges() {
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
//        ModPackageManager.getInstance().postUninstall(ModPackageInfo.MODTYPE_CREWPIC, ModPackageInfo.SUBTYPE_EMPTY);
//        return Utils.delDir(getFile(selectedcountry, selectedcrew).getParentFile());
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == AppCompatActivity.RESULT_OK) {
            try {
                if (data == null) {
                    Snackbar.make(v, R.string.source_file_cannot_be_empty, Snackbar.LENGTH_LONG).show();
                    return;
                }
                if (ba != null) {
                    //手动释放以防止Bitmap未被释放
                    ba.recycle();
                    System.gc();
                }
                ba = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(Objects.requireNonNull(data.getData())));
                selectedpic.setText(data.getData().toString());
            } catch (Throwable t) {
                Snackbar.make(v, t.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}
