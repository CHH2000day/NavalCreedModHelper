package com.CHH2000day.navalcreed.modhelper;

import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.orhanobut.logger.Logger;
import com.qy.sdk.Interfaces.RDInterface;
import com.qy.sdk.rds.BannerView;

public abstract class ModFragment extends Fragment
{
	/*
	public abstract boolean installMod(int typenum,int num,byte[] deceyptedFileData) throws IOException
	//typenum:在Installation.manifest中的类型
	//num:在Installation.manifest中的文件名
	//返回值:true为成功
	*/
	public abstract boolean uninstallMod();

    private Main getMainActivity() {
        return (Main) getActivity();
    }

    protected boolean shouldShowAd() {
        //return getMainActivity().isShowAd();
        return true;
    }

    protected void showAd(View v) {
        if (!shouldShowAd()) {
            return;
        }
        RelativeLayout l = v.findViewById(R.id.adlayout);
        if (l == null) {
            Logger.d("failed to get ad layout ");
            return;
        }
        BannerView ad = new BannerView();
        ad.setInterface(getMainActivity(), new RDInterface() {
            @Override
            public void rdView(ViewGroup benner) {
                super.rdView(benner);
                l.addView(benner); //layout是你自己定义的布局
            }
        });
        ad.load();
        ad.show();

    }

}
