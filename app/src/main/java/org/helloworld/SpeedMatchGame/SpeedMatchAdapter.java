package org.helloworld.SpeedMatchGame;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * 用于主页的分页显示
 */
public class SpeedMatchAdapter extends PagerAdapter
{
	private List<View> views;
	private Context context;

	public SpeedMatchAdapter(Context context, List<View> views)
	{
		this.views = views;
		this.context = context;
	}

	@Override
	public int getItemPosition(Object object)
	{
		return POSITION_NONE;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object)
	{
		if(position>= views.size())return;
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
