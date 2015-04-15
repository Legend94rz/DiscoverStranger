package org.helloworld;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

/**
 * Created by Administrator on 2015/4/10.
 * 此文件用于主页的分页显示
 */
public class myViewPagerAdapter extends PagerAdapter
{
	private List<View> views;
	private Context context;

	public myViewPagerAdapter(Context context,List<View> views)
	{
		this.views = views;
		this.context = context;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object)
	{
		container.removeView(views.get(position));
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position)
	{
		container.addView(views.get(position));
		return views.get(position);
	}

	@Override
	public int getCount()
	{
		return views.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object object)
	{
		return view==object;
	}
}