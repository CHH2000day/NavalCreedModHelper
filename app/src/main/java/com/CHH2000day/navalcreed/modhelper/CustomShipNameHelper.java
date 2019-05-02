package com.CHH2000day.navalcreed.modhelper;
import java.io.*;
import java.util.*;
import okio.*;
import com.orhanobut.logger.*;
import android.os.*;

public class CustomShipNameHelper
{
	public static CustomShipNameHelper mcsh;
	public synchronized static CustomShipNameHelper getInstance()
	{
		if (mcsh == null)
		{mcsh = new CustomShipNameHelper();}
		return mcsh;
	}
	private HashMap<Integer,String> shipnames;
	private ArrayList idList;
	//Contains all ships' id

	private static final String HOF="return\n{\n";
	private static final String EOC="}";
	private static final String EOF ="--[[ Generated by NavalCreedModHelper ]]";
	private static final String STATEMENT=" [%d] = \"%s\";";

	public void init(File src)
	{
		try
		{
			doInit(src);
		}
		catch (IOException e)
		{
			Logger.e(e, "Failed to load shipnames");
		}
	}
	private void doInit(File src) throws IOException
	{
		if (shipnames == null || idList == null)
		{
			shipnames = new HashMap<Integer,String>();
			idList = new ArrayList();
		}
		shipnames.clear();
		idList.clear();
		Source s=Okio.source(src);
		BufferedSource bs=Okio.buffer(s);
		String orig=bs.readUtf8();
		bs.close();
		s.close();
		String[] raw=orig.split("\n");
		char[] line=null;
		String str=null;
		ArrayList ids=new ArrayList();
		String id=null;
		String name=null;;
		boolean isInId=false;
		boolean isInName=false;
		for (int i=0;i < raw.length;i++)
		{
			str = raw[i].trim();
			if (str.startsWith("--") || str.startsWith("]]") || str.startsWith("return") || str.startsWith("{") || str.startsWith("}"))
			{
				continue;
			}
			id = str.substring(str.indexOf('[') + 1, str.indexOf(']') - 1);
			name = str.substring(str.indexOf('\"') + 1, str.lastIndexOf('"') - 1);
			//end of each line's resolve
			int shipId=Integer.valueOf(id);
			ids.add(shipId);
			shipnames.put(shipId, name);

			id = null;
			name = null;
			//reset StringBuilders after each loop
		}
		//end of all loops
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
		{
			Collections.sort(ids);
		}
		else
		{
			ids.sort(new Comparator<Integer>() {
					@Override
					public int compare(Integer o1, Integer o2)
					{
						// TODO Auto-generated method stub
						if ((int)o1 < (int)o2)return -1;
						else return 1;
					}
				});	

		}

	}
	private void writeToFile(File dest) throws IOException
	{
		Sink s=Okio.sink(dest);
		BufferedSink bs=Okio.buffer(s);
		ListIterator li=idList.listIterator();
		String name="";
		String empty="";
		bs.writeUtf8(HOF);
		while (li.hasNext())
		{
			int i=(Integer)li.next();
			name = shipnames.getOrDefault(i, name);
			if (name.equals(empty))
			{continue;}
			bs.writeUtf8(String.format(STATEMENT, i, shipnames.get(i)));
			bs.writeUtf8("\n");

		}
		bs.writeUtf8(EOC);
		bs.writeUtf8(EOF);
		bs.close();
		s.close();
	}

}