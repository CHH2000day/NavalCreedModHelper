package com.CHH2000day.navalcreed.modhelper;
import java.util.*;

public class KeyUtil
{
	private static final int BLOCKSIZE=5;
	private static final char SEPARATERCHAR='-';
	private static char[] chars;
	static{
		chars = new char[36];
		chars [ 0 ] = 'A';
		chars [ 1 ] = 'B';
		chars [ 2 ] = 'C';
		chars [ 3 ] = 'D';
		chars [ 4 ] = 'E';
		chars [ 5 ] = 'F';
		chars [ 6 ] = 'G';
		chars [ 7 ] = 'H';
		chars [ 8 ] = 'I';
		chars [ 9 ] = 'J';
		chars [ 10 ] = 'K';
		chars [ 11 ] = 'L';
		chars [ 12 ] = 'M';
		chars [ 13 ] = 'N';
		chars [ 14 ] = 'O';
		chars [ 15 ] = 'P';
		chars [ 16 ] = 'Q';
		chars [ 17 ] = 'R';
		chars [ 18 ] = 'S';
		chars [ 19 ] = 'T';
		chars [ 20 ] = 'U';
		chars [ 21 ] = 'V';
		chars [ 22 ] = 'W';
		chars [ 23 ] = 'X';
		chars [ 24 ] = 'Y';
		chars [ 25 ] = 'Z';
		chars [ 26 ] = '0';
		chars [ 27 ] = '1';
		chars [ 28 ] = '2';
		chars [ 29 ] = '3';
		chars [ 30 ] = '4';
		chars [ 31 ] = '5';
		chars [ 32 ] = '6';
		chars [ 33 ] = '7';
		chars [ 34 ] = '8';
		chars [ 35 ] = '9';
	}
	public static boolean checkKeyFormat ( String key )
	{
		final int len= BLOCKSIZE * 4 + 3 * 1;
		if ( key.length ( ) != len )
		{
			return false;
		}
		String[] subs=key.split ( String.valueOf ( SEPARATERCHAR ) );
		if ( subs.length != 4 )
		{
			return false;
		}
		byte[]block1=subs [ 0 ].getBytes ( );
		byte[] block2 =subs [ 1 ].getBytes ( );
		byte[] block3 =subs [ 2 ].getBytes ( );
		byte[] block4=subs [ 3 ].getBytes ( );
		if ( block1.length != BLOCKSIZE || block2.length != BLOCKSIZE || block3.length != BLOCKSIZE || block4.length != BLOCKSIZE )
		{
			return false;
		}
		StringBuilder sb=new StringBuilder ( );
		for ( int i=0;i < BLOCKSIZE;i++ )
		{
			int s=( block2 [ i ] ^ block3 [ i ] );
			if ( i < 2 )
			{
				if ( i == 0 )
				{
					s = s ^ block1 [ i ];
				}
				if ( i == 1 )
				{
					s = s << block1 [ i ];
				}
			}
			sb.append ( chars [ s % chars.length ] );
		}
		return Arrays.equals ( sb.toString ( ).getBytes ( ), block4 );

	}
}
