package org.helloworld;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/5/9.
 */
public class NotificationAdapter extends BaseAdapter
{
	private ArrayList<Message> msglist;
	private Context context;
	public NotificationAdapter(Context context, ArrayList<Message> msglist)
	{
		this.context=context;
		this.msglist = msglist;
	}

	@Override
	public int getCount()
	{
		return msglist.size();
	}

	@Override
	public Object getItem(int i)
	{
		return msglist.get(i);
	}

	@Override
	public long getItemId(int i)
	{
		return i;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup)
	{
		Message m=msglist.get(i);
		H h;
		if(view==null)
		{
			h=new H();
			view= LayoutInflater.from(context).inflate(R.layout.notification_item,viewGroup,false);
			h.ivHeadImg = (ImageView) view.findViewById(R.id.ivHeadImg);
			h.tvHint= (TextView) view.findViewById(R.id.tvHint);
			h.tvTime= (TextView) view.findViewById(R.id.tvTime);
			h.tvName= (TextView) view.findViewById(R.id.tvName);
			view.setTag(h);
		}
		else
			h= (H) view.getTag();
		h.tvName.setText(m.fromId);
		if(FileUtils.Exist(Global.PATH.HeadImg+m.fromId+".png"))
			h.ivHeadImg.setImageBitmap(BitmapFactory.decodeFile(Global.PATH.HeadImg+m.fromId+".png"));
		h.tvHint.setText(m.text);
		h.tvTime.setText(m.getDateWithFormat("yyyy-MM-dd HH:mm:ss"));
		return view;
	}
	class H
	{
		ImageView ivHeadImg;
		TextView tvName;
		TextView tvHint;
		TextView tvTime;
	}

}
