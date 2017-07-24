package com.CHH2000day.navalcreed.modhelper;
import android.support.v4.app.*;

public abstract class FunctionFragment extends Fragment
{
	public abstract String getTargetPath()
	public abstract boolean isHasType()
	public abstract String getTypeName(int typenum)
	//typenum:在Installation.manifest中定义
	public abstract String getFileName(int num)
	//num:在Installation.manifest中定义的文件名
}
