package com.chh2000day.navalcreed.modhelper;

import androidx.fragment.app.Fragment;

import com.chh2000day.navalcreed.modhelper.Main;

public abstract class ModFragment extends Fragment {
    /*
    public abstract boolean installMod(int typenum,int num,byte[] deceyptedFileData) throws IOException
    //typenum:在Installation.manifest中的类型
    //num:在Installation.manifest中的文件名
    //返回值:true为成功
    */
    public abstract boolean uninstallMod();

    private boolean isAdLoaded = false;

    protected Main getMainActivity() {
        return (Main) getActivity();
    }

}
