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

import org.helloworld.CircleImageView;
import org.helloworld.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

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
	Comparator<UserInfo> comparator;

	public ContactAdapter(Context context, ArrayList<UserInfo> list,ListView listView)
	{
		this.context = context;
		this.list = list;
		this.listView=listView;
		loader=new AsyImageLoader(context);
		this.listView.setOnScrollListener(this);
		comparator=new Comparator<UserInfo>()
		{
			@Override
			public int compare(UserInfo userInfo, UserInfo t1)
			{
				if(HanziToPinyin.getInstance().isEnable())
				{
					HanziToPinyin.Token token1 = HanziToPinyin.getInstance().get(String.valueOf(userInfo.getShowName().charAt(0))).get(0);
					HanziToPinyin.Token token2 = HanziToPinyin.getInstance().get(String.valueOf(t1.getShowName().charAt(0))).get(0);
					return token1.target.toUpperCase().compareTo(token2.target.toUpperCase());
				}
				return String.valueOf(userInfo.getShowName().charAt(0)).compareTo(String.valueOf(t1.getShowName().charAt(0)));
			}
		};
	}

	@Override
	public void notifyDataSetChanged()
	{
		Collections.sort(list,comparator);
		super.notifyDataSetChanged();
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
		UserInfo userInfo = list.get(position);
		H h;
		if (view == null)
		{
			h = new H();
			view = LayoutInflater.from(context).inflate(R.layout.tongxunlu, parent, false);
			h.pic = (CircleImageView) view.findViewById(R.id.tx1);
			h.name = (TextView) view.findViewById(R.id.tx2);
			h.seperator= (TextView) view.findViewById(R.id.seprator);
			h.tvGroupName = (TextView) view.findViewById(R.id.tvGroupName);
			view.setTag(h);
		}
		else
		{
			h = (H) view.getTag();
		}
		h.pic.setImageResource(R.drawable.nohead);
		h.pic.setTag(Global.PATH.HeadImg + userInfo.username + ".png");
		Bitmap bitmap=loader.loadDrawable(Global.PATH.HeadImg, "HeadImg", userInfo.username + ".png", isScroll, 128 * Global.DPI, new AsyImageLoader.ImageCallback()
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

		h.name.setText(userInfo.getShowName());

		if(position==0 || comparator.compare(list.get(position-1),list.get(position))!=0)
		{
			h.tvGroupName.setVisibility(View.VISIBLE);
			if(position!=0)
				h.seperator.setVisibility(View.VISIBLE);
			else
				h.seperator.setVisibility(View.INVISIBLE);
			if(HanziToPinyin.getInstance().isEnable())
			{
				ArrayList<HanziToPinyin.Token> tokens = HanziToPinyin.getInstance().get(list.get(position).getShowName());
				HanziToPinyin.Token t = tokens.get(0);
				h.tvGroupName.setText(String.valueOf(t.target.toUpperCase().charAt(0)));
			}
			else
			{
				h.tvGroupName.setText(String.valueOf(list.get(position).getShowName().toUpperCase().charAt(0)));
			}
		}
		else
		{
			h.tvGroupName.setVisibility(View.INVISIBLE);
			h.seperator.setVisibility(View.INVISIBLE);
		}
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
		CircleImageView pic;
		TextView name;
		TextView seperator;
		TextView tvGroupName;
	}
}
