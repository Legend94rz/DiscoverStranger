package org.helloworld.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.helloworld.R;

import java.util.ArrayList;

/**
 * 联系人列表
 */
public class ContactAdapter extends BaseAdapter implements AbsListView.OnScrollListener
{
	private Context context;
	private ArrayList<UserInfo> list;
	private boolean isScroll;
	private ListView listView;
	private AsyImageLoader loader;

	public ContactAdapter(Context context, ArrayList<UserInfo> list,ListView listView)
	{
		this.context = context;
		this.list = list;
		this.listView=listView;
		loader=new AsyImageLoader(context);
		this.listView.setOnScrollListener(this);
	}

	@Override
	public int getCount()
	{
		return list.size();
	}

	@Override
	public Object getItem(int position)
	{
		return list.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent)
	{
		UserInfo hh = list.get(position);
		H h;
		if (view == null)
		{
			h = new H();
			view = LayoutInflater.from(context).inflate(R.layout.tongxunlu, parent, false);
			h.pic = (ImageView) view.findViewById(R.id.tx1);
			h.name = (TextView) view.findViewById(R.id.tx2);

			view.setTag(h);
		}
		else
		{
			h = (H) view.getTag();
		}
		h.pic.setImageResource(R.drawable.nohead);
		h.pic.setTag(Global.PATH.HeadImg + hh.username + ".png");
		Bitmap bitmap=loader.loadDrawable(Global.PATH.HeadImg, "HeadImg", hh.username + ".png", isScroll, 128 * Global.DPI, new AsyImageLoader.ImageCallback()
		{
			@Override
			public void imageLoaded(Bitmap bitmap, String url)
			{
				ImageView iv= (ImageView) listView.findViewWithTag(url);
				if(iv!=null)
				{
					if(bitmap!=null)
					{
						iv.setImageBitmap(bitmap);
						notifyDataSetChanged();
					}
				}
			}
		});
		if(bitmap!=null)
			h.pic.setImageBitmap(bitmap);

		if (hh.Ex_remark == null || hh.Ex_remark.length() == 0)
			h.name.setText(hh.nickName);
		else
			h.name.setText(hh.Ex_remark);

		return view;
	}

	@Override
	public void onScrollStateChanged(AbsListView absListView, int i)
	{
		if (i == AbsListView.OnScrollListener.SCROLL_STATE_IDLE)
		{
			isScroll = false;
			notifyDataSetChanged();
		}
		else
			isScroll = true;
	}

	@Override
	public void onScroll(AbsListView absListView, int i, int i1, int i2)
	{

	}

	class H
	{
		ImageView pic;
		TextView name;
	}
}
