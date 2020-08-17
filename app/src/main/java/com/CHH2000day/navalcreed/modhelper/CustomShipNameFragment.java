package com.CHH2000day.navalcreed.modhelper;

import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.snackbar.Snackbar;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;

public class CustomShipNameFragment extends ModFragment {

    private static final String res_url = "https://static.CHH2000day.com/nc/customshipname_latest.patch";
    private View v;

    @Override
    public boolean uninstallMod() {
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.antihexie_fragment, null);
        ;
        Button exec = v.findViewById(R.id.antihexiefragmentButtonExec);

        exec.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View p1) {
                final AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
                adb.setTitle("请稍等");
                adb.setMessage("正在进行反和谐");
                adb.setCancelable(false);
                final AlertDialog ad = adb.create();
                ad.setCanceledOnTouchOutside(false);
                ad.show();

                new Thread() {
                    public void run() {
                        Looper.prepare();
                        try {
                            Request r = new Request.Builder().url(res_url).build();
                            Response response = OKHttpHelper.getClient().newCall(r).execute();
                            if (CustomShipNameHelper.INSTANCE.patch(response.body().source())) {
                                adb.setMessage(R.string.success)
                                        .setTitle(R.string.success).
                                        setPositiveButton(R.string.ok, null)
                                        .create().show();
                            } else {
                                adb.setMessage(R.string.failed)
                                        .setTitle(R.string.failed)
                                        .setPositiveButton(R.string.ok, null)
                                        .create().show();
                            }


                        } catch (IOException e) {
                            Logger.e(e, e.getLocalizedMessage());
                            adb.setMessage(R.string.failed)
                                    .setTitle(R.string.failed)
                                    .setPositiveButton(R.string.ok, null)
                                    .create().show();
                        } finally {
                            ad.dismiss();
                            Looper.loop();
                        }
                    }
                }.start();
            }

        });
        exec.setOnLongClickListener(p1 -> {
            final File f = getMainActivity().getModHelperApplication().getCustomShipNameFile();
            AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
            adb.setTitle(R.string.notice)
                    .setMessage(R.string.confirm_to_remove_all_changes)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.remove_changes, (dialogInterface, p2) -> {
                        if (f != null && f.isFile()) {
                            //noinspection ResultOfMethodCallIgnored
                            f.delete();
                        }
                        Snackbar.make(v, R.string.success, Snackbar.LENGTH_LONG).show();
                    })
                    .create()
                    .show();
            return true;
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
