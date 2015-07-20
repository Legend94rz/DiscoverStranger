package org.helloworld.tools;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Administrator on 2015/7/19.
 */
public class noTouchPager extends ViewPager
{
	public noTouchPager(Context context)
	{
		super(context);
	}

	public noTouchPager(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev)
	{
		return false;
	}
}
