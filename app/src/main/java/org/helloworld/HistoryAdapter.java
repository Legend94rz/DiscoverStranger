package org.helloworld;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;

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
		History history=list.get(i);
		H h=null;
		if(view==null)
		{
			h=new H();
			view= LayoutInflater.from(context).inflate(R.layout.liaotian,viewGroup,false);
			h.pic= (ImageView) view.findViewById(R.id.imgHead);
			h.Time= (TextView) view.findViewById(R.id.tvTime);
			h.name= (TextView) view.findViewById(R.id.tvName);
			h.MsgCount= (TextView) view.findViewById(R.id.tvCount);
			h.newMsg= (TextView) view.findViewById(R.id.tvLastmsg);
			view.setTag(h);
		}
		else
		h= (H) view.getTag();
		h.pic.setImageResource(R.drawable.icon);
		h.name.setText(history.fromName);
		h.newMsg.setText(history.lastMsg);
		if(history.count.equals("0"))
		{
			h.MsgCount.setVisibility(View.INVISIBLE);
		}
		else
		{
			h.MsgCount.setText(history.count);
			h.MsgCount.setVisibility(View.VISIBLE);
		}
		SimpleDateFormat format=new SimpleDateFormat("HH:mm");
		h.Time.setText(format.format(history.SendTime));
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
