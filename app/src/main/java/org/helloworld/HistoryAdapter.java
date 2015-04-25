package org.helloworld;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/4/9.
 */
public class HistoryAdapter extends BaseAdapter
{
	private ArrayList<History> list;
	private Context context;

	public HistoryAdapter(ArrayList<History> list, Context context)
	{
		this.list = list;
		this.context = context;
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
			h.pic = (ImageView) view.findViewById(R.id.imgHead);
			h.Time = (TextView) view.findViewById(R.id.tvTime);
			h.name = (TextView) view.findViewById(R.id.tvName);
			h.MsgCount = (TextView) view.findViewById(R.id.tvCount);
			h.newMsg = (TextView) view.findViewById(R.id.tvLastmsg);
			view.setTag(h);
		} else
			h = (H) view.getTag();
		h.pic.setImageResource(R.drawable.icon);
		UserInfo temp = Global.friendList.get(history.fromName);
		String showName = temp.Ex_remark;
		if (showName == null || showName.equals("")) showName = temp.nickName;
		h.name.setText(showName);
		h.newMsg.setText(history.historyMsg.get(history.historyMsg.size() - 1).Text);
		if (history.unreadCount == 0)
		{
			h.MsgCount.setVisibility(View.INVISIBLE);
		} else
		{
			h.MsgCount.setText(String.valueOf(history.unreadCount));
			h.MsgCount.setVisibility(View.VISIBLE);
		}
		h.Time.setText(history.getLastDateWithFormat("HH:mm"));
		return view;
	}

	class H
	{
		ImageView pic;
		TextView name;
		TextView newMsg;
		TextView Time;
		TextView MsgCount;
	}
}
