package org.helloworld.tools;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.navisdk.model.GeoLocateModel;

import org.helloworld.CircleImageView;
import org.helloworld.DetailAct;
import org.helloworld.R;

import java.util.ArrayList;

/**
 * 用于通知界面的显示
 */
public class NotificationAdapter extends BaseAdapter
{
	private ArrayList<Message> msglist;
	private Context context;

	public NotificationAdapter(Context context, ArrayList<Message> msglist)
	{
		this.context = context;
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
		final Message m = msglist.get(i);
		H h;
		if (view == null)
		{
			h = new H();
			view = LayoutInflater.from(context).inflate(R.layout.item_notification, viewGroup, false);
			h.ivHeadImg = (CircleImageView) view.findViewById(R.id.ivHeadImg);
			h.tvHint = (TextView) view.findViewById(R.id.tvHint);
			h.tvTime = (TextView) view.findViewById(R.id.tvTime);
			h.tvName = (TextView) view.findViewById(R.id.tvName);
			h.btnDetail= (Button) view.findViewById(R.id.btnDetail);
			view.setTag(h);
		}
		else
			h = (H) view.getTag();
		final String[] s=m.text.split(" ");
		h.tvName.setText(m.fromId);
		if (FileUtils.Exist(Global.PATH.HeadImg + m.fromId + ".png"))
			h.ivHeadImg.setImageBitmap(BitmapFactory.decodeFile(Global.PATH.HeadImg + m.fromId + ".png"));
		else
			h.ivHeadImg.setImageResource(R.drawable.nohead);
		if((m.msgType&Global.MSG_TYPE.T_INVITE_NOTIFICATION)==0)
		{
			h.tvHint.setText(m.text);
			h.btnDetail.setVisibility(View.GONE);
			h.btnDetail.setOnClickListener(null);
		}
		else
		{
			h.tvHint.setText(s[0]+s[1]);
			h.btnDetail.setVisibility(View.VISIBLE);
			h.btnDetail.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					Intent I = new Intent(context, DetailAct.class);
					I.putExtra("id", s[2]);
					I.putExtra("username", m.fromId);
					context.startActivity(I);
				}
			});
		}
		h.tvTime.setText(m.getDateWithFormat("yyyy-MM-dd HH:mm:ss"));
		return view;
	}
	public boolean areAllItemsEnabled()
	{
		return false;
	}
	public boolean isEnabled(int position)
	{
		return false;
	}
	class H
	{
		CircleImageView ivHeadImg;
		TextView tvName;
		TextView tvHint;
		TextView tvTime;
		Button btnDetail;
	}

}
