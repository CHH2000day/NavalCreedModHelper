package com.CHH2000day.navalcreed.modhelper;
import android.support.v4.app.*;
import java.io.*;

public abstract class FunctionFragment extends Fragment
{
	public abstract boolean installMod(int typenum,int num,byte[] deceyptedFileData) throws IOException
	//typenum:在Installation.manifest中的类型
	//num:在Installation.manifest中的文件名
	//返回值:true为成功
}
