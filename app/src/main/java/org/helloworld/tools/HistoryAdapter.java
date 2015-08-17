package org.helloworld.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.SpannableString;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**
 * 用于历史记录列表的显示
 */
public class HistoryAdapter extends BaseAdapter implements AbsListView.OnScrollListener
{
	private ArrayList<History> list;
	private Context context;
	private boolean isScroll;
	private ListView listView;
	private AsyImageLoader loader;

	public HistoryAdapter(ArrayList<History> list, Context context, ListView listView)
	{
		this.list = list;
		this.context = context;
		this.listView = listView;
		this.listView.setOnScrollListener(this);
		loader = new AsyImageLoader(context);
	}

	@Override
	public void notifyDataSetChanged()
	{
		Collections.sort(list, new Comparator<History>()
		{
			@Override
			public int compare(History history, History t1)
			{
				return -history.lastHistoryMsg.sendTime.compareTo(t1.lastHistoryMsg.sendTime);
			}
		});
		super.notifyDataSetChanged();
	}

	@Override
	public int getCount()
	{
		return list.size();
	}

	@Override
	public Object getItem(int i)
	{
		return list.get(i);
	}

	@Override
	public long getItemId(int i)
	{
		return i;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup)
	{
		History history = list.get(i);
		H h = null;
		if (view == null)
		{
			h = new H();
			view = LayoutInflater.from(context).inflate(R.layout.liaotian, viewGroup, false);
			h.pic = (CircleImageView) view.findViewById(R.id.imgHead);
			h.time = (TextView) view.findViewById(R.id.tvTime);
			h.name = (TextView) view.findViewById(R.id.tvName);
			h.msgCount = (TextView) view.findViewById(R.id.tvCount);
			h.lastMsg = (TextView) view.findViewById(R.id.tvLastmsg);
			h.tvDay = (TextView) view.findViewById(R.id.tvDay);
			view.setTag(h);
		}
		else
			h = (H) view.getTag();
		if (history.isSystem())
			h.pic.setImageResource(history.headId);
		else
		{
			h.pic.setImageResource(R.drawable.nohead);
			h.pic.setTag(Global.PATH.HeadImg + history.partner + ".png");
			Bitmap bitmap = loader.loadDrawable(Global.PATH.HeadImg, "HeadImg", history.partner + ".png", isScroll, 128 * Global.DPI, new AsyImageLoader.ImageCallback()
			{
				@Override
				public void imageLoaded(Bitmap bitmap, String url)
				{
					ImageView iv = (ImageView) listView.findViewWithTag(url);
					if (iv != null)
					{
						if (bitmap != null)
						{
							iv.setImageBitmap(bitmap);
							notifyDataSetChanged();
						}
					}
				}
			});
			if (bitmap != null)
				h.pic.setImageBitmap(bitmap);
		}

		UserInfo temp = Global.map2Friend.get(history.partner);
		if (temp != null)
		{
			h.name.setText(temp.getShowName());
		}
		else        //发消息的人不在好友列表中
		{
			h.name.setText(history.partner);
		}

		Message lastMessage = history.lastHistoryMsg;
		if ((lastMessage.msgType & Global.MSG_TYPE.T_TEXT_MSG) > 0)
		{
			SpannableString spannableString = FaceConversionUtil.getInstace().getExpressionString(context, lastMessage.text);
			h.lastMsg.setText(spannableString);
		}
		else if ((lastMessage.msgType & Global.MSG_TYPE.T_PIC_MSG) > 0)
			h.lastMsg.setText("[图片]");
		else if ((lastMessage.msgType & Global.MSG_TYPE.T_VOICE_MSG) > 0)
			h.lastMsg.setText("[语音]");

		if (history.unreadMsg.size() == 0)
		{
			h.msgCount.setVisibility(View.INVISIBLE);
		}
		else
		{
			h.msgCount.setText(String.valueOf(history.unreadMsg.size()));
			h.msgCount.setVisibility(View.VISIBLE);
		}
		h.time.setText(Global.formatDate(history.lastHistoryMsg.sendTime, "HH:mm"));
		h.tvDay.setText(day(list.get(i).lastHistoryMsg.sendTime));
		if (i == 0 || !day(list.get(i).lastHistoryMsg.sendTime).equals(day(list.get(i - 1).lastHistoryMsg.sendTime)))
			h.tvDay.setVisibility(View.VISIBLE);
		else
			h.tvDay.setVisibility(View.GONE);

		return view;
	}

	private String day(Date date)
	{
		Date now = Global.getDate();
		if (date.getYear() == now.getYear())
			if (date.getMonth() == now.getMonth())
			{
				if (date.getDate() == now.getDate())
					return "今天";
				else if (date.getDate() + 1 == now.getDate())
					return "昨天";
				else
					return Global.formatDate(date,"dd日");
			}
			else
			{
				return Global.formatDate(date,"MM月dd日");
			}
		else
			return Global.formatDate(date,"yyyy年MM月dd日");
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
		TextView lastMsg;
		TextView time;
		TextView msgCount;
		TextView tvDay;
	}
}
