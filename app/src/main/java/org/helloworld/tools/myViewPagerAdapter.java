package org.helloworld.tools;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * 用于主页的分页显示
 */
public class myViewPagerAdapter extends PagerAdapter
{
	private List<View> views;
	private Context context;

	public myViewPagerAdapter(Context context, List<View> views)
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
		return view == object;
	}
}
