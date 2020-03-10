package com.CHH2000day.navalcreed.modhelper;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.InputStream;

public class ModPackageInstallerFragment extends Fragment {

    private static final int QUERY_CODE = 6;
    private UriLoader loader;
    private View v;
    private ImageView preview;
    private TextView info;
    private Button update, select, cancel;
    private ModPackageInstallHelper mpih;
    private boolean isCache;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO: Implement this method
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // TODO: Implement this method
        v = inflater.inflate(R.layout.modinfopage, null);
        preview = v.findViewById(R.id.modinfopageImageView);
        info = v.findViewById(R.id.modinfopageTextView);
        select = v.findViewById(R.id.modinfopageButtonSelect);
        update = v.findViewById(R.id.modinfopageButtonUpdate);
        cancel = v.findViewById(R.id.modinfopageButtonCancel);
        info.setMovementMethod(new ScrollingMovementMethod());
        return v;
    }

    @Override
    public void onResume() {
        // TODO: Implement this method
        super.onResume();
        if (loader != null && loader.getUri() != null) {
            selectFile(loader.getUri());
            //注销接口防止被重复使用
            loader = null;
        }

    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // TODO: Implement this method
        select.setOnClickListener(p1 -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(intent.CATEGORY_OPENABLE);
            startActivityForResult(intent.createChooser(intent, getString(R.string.select_file)), QUERY_CODE);

            // TODO: Implement this method
        });
        select.setOnLongClickListener(p1 -> {
            // TODO: Implement this method
            final String pkg = "com.android.documentsui";
            Uri packageURI = Uri.parse("package:" + pkg);
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
            startActivity(intent);
            return true;
        });
        cancel.setOnClickListener(p1 -> {
            clear();
            // TODO: Implement this method
        });
        update.setOnClickListener(p1 -> {
            if (mpih == null) {
                Snackbar.make(v, R.string.modpkg_info_empty, Snackbar.LENGTH_LONG).show();
                return;
            }
            mpih.beginInstall(getActivity());
            // TODO: Implement this method
        });
    }

    @Override
    public void onDestroyView() {
        // TODO: Implement this method
        super.onDestroyView();
        if (mpih != null) {
            mpih.recycle();
            mpih = null;
        }
    }


    @Override
    public void onAttach(Activity activity) {

        // TODO: Implement this method
        super.onAttach(activity);
        if (activity != null) {
            loader = (UriLoader) activity;
        }
    }


    private void clear() {
        if (mpih != null) {
            synchronized (mpih) {
                if (isCache) {
                    Utils.delDir(mpih.getSourceFile());
                    isCache = false;
                }
                mpih.recycle();
            }

        }

        preview.setImageResource(R.drawable.no_preview);
        info.setText(R.string.modpkg_info_empty);
        mpih = null;
    }

    public void selectFile(final Uri uri) {
        clear();
        String filepath = Utils.resolveFilePath(uri, getActivity());
        if (filepath == null) {
            AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
            adb.setTitle(R.string.please_wait)
                    .setCancelable(false)
                    .setMessage(new StringBuilder().append(getString(R.string.failed_to_resolve_pth))
                            .append("\n")
                            /*.append ( "请将此界面截屏并发给开发者" )
                             .append ( "\n" )*/
                            .append("authority:")
                            .append(uri.getAuthority())
                            .append("\n")
                            .append("path:")
                            .append(uri.getPath())
                            .toString())
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.altn_install, (p1, p2) -> {
                        final Handler h = new Handler() {
                            public void handleMessage(Message msg) {
                                if (msg.what == 0) {
                                    isCache = true;
                                    load((File) msg.obj);
                                } else {
                                    AlertDialog.Builder adb1 = new AlertDialog.Builder(getActivity());
                                    adb1.setTitle(R.string.error)
                                            .setMessage(Utils.getErrMsg((Throwable) msg.obj))
                                            .setPositiveButton(R.string.ok, null)
                                            .create().show();
                                }
                            }
                        };
                        new Thread() {

                            @Override
                            public void run() {
                                File f = new File(getActivity().getExternalCacheDir(), "cachedmodfile.ncmod");
                                try {
                                    InputStream in = getActivity().getContentResolver().openInputStream(uri);
                                    Utils.writeToFile(in, f);
                                    h.sendMessage(h.obtainMessage(0, f));

                                } catch (Throwable t) {
                                    h.sendMessage(h.obtainMessage(-1, t));
                                }

                                // TODO: Implement this method
                            }
                        }.start();
                        // TODO: Implement this method
                    });
            adb.create().show();
            return;
        } else {
            isCache = false;
            load(new File(filepath));
        }

    }


    private void load(final File source) {
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        adb.setTitle(R.string.please_wait)
                .setMessage(R.string.please_wait)
                .setCancelable(false);
        final AlertDialog ad = adb.create();
        ad.setCanceledOnTouchOutside(false);
        ad.show();


        mpih = new ModPackageInstallHelper(source);
        final AppCompatActivity act = (Main) getActivity();
        mpih.load(new ModPackageInstallHelper.onModPackageLoadDoneListener() {

            @Override
            public void onSuccess() {
                if (!isAdded()) {
                    return;
                }

                ModPackageInfo mpi = mpih.getModPackageInfo();
                long modsize = mpih.getTotalSize();
                StringBuilder sb = new StringBuilder();
                sb.append(getString(R.string.modname))
                        .append(mpi.getModName())
                        .append("\n")
                        .append(getString(R.string.modsize))
                        .append(Utils.convertFileSize(modsize))
                        .append("\n")
                        .append(getString(R.string.modtype))
                        .append(ModPackageManagerV2.INSTANCE.getModTypeName(mpi.getModType()))
                        .append("\n")
                        .append(getText(R.string.modauthor))
                        .append(mpi.getModAuthor())
                        .append("\n")
                        .append(getString(R.string.modinfo))
                        .append(mpi.getModInfo());
                if (mpi.getModType().equals(ModPackageInfo.MODTYPE_OTHER)) {
                    sb.append("\n")
                            .append(getString(R.string.ununinstallable_modpkg_warning));
                }
                info.setText(sb.toString());
                if (mpi.hasPreview()) {
                    preview.setImageBitmap(mpi.getModPreview());
                }
                ad.dismiss();
                // TODO: Implement this method
            }

            @Override
            public void onFail(Throwable t) {
                ad.setCancelable(true);
                ad.setCanceledOnTouchOutside(true);
                ad.setTitle(getString(R.string.error));
                if (t instanceof ModPackageInfo.IllegalModInfoException) {
                    ad.setMessage(getString(R.string.invalid_mod_info) + "\n" + Utils.getErrMsg(t));
                } else {
                    ad.setMessage(Utils.getErrMsg(t));
                }
                if (isCache) {
                    Utils.delDir(source);
                }
                mpih = null;
                // TODO: Implement this method
            }

            @Override
            public AppCompatActivity getActivity() {
                // TODO: Implement this method
                return act;
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO: Implement this method
        super.onActivityResult(requestCode, resultCode, data);
        if (QUERY_CODE == requestCode && AppCompatActivity.RESULT_OK == resultCode && data != null) {
            if (data.getData() == null) {
                Snackbar.make(v, R.string.source_file_cannot_be_empty, Snackbar.LENGTH_LONG).show();
            }
            selectFile(data.getData());
        }
    }

    public interface UriLoader {
        Uri getUri();
    }

}
