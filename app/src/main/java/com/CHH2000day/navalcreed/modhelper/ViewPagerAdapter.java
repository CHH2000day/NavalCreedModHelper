package com.CHH2000day.navalcreed.modhelper;
import android.support.v4.app.*;
import java.util.*;

public class ViewPagerAdapter extends FragmentPagerAdapter {
		private List<Fragment>  fragments;
		private List<String> titles;
		/**
		 * 构造方法
		 * @param manager
		 * @param fragments
		 */
		public ViewPagerAdapter(FragmentManager manager, List<Fragment> fragments,List<String> titles){
				super(manager);
				this.fragments=fragments;
				this.titles=titles;
			}

		@Override
		public int getCount() {
				if (fragments!=null){
						return fragments.size();
					}
				return 0;
			}

		@Override
		public Fragment getItem(int position) {
				if (fragments!=null){
						return fragments.get(position);
					}
				return null;
			}


		@Override
		public CharSequence getPageTitle(int position) {
				if (titles!=null){
						return titles.get(position);
					}
				return "";
			}

}

