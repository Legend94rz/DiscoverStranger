package org.helloworld.tools;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.helloworld.R;

import java.util.ArrayList;

/**
 * 用于历史记录列表的显示
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
			h.time = (TextView) view.findViewById(R.id.tvTime);
			h.name = (TextView) view.findViewById(R.id.tvName);
			h.msgCount = (TextView) view.findViewById(R.id.tvCount);
			h.lastMsg = (TextView) view.findViewById(R.id.tvLastmsg);
			view.setTag(h);
		} else
			h = (H) view.getTag();
		if(history.headId!=-1)
			h.pic.setImageResource(history.headId);
		else
		{
			if (FileUtils.Exist(Global.PATH.HeadImg + history.partner + ".png"))
			{
				h.pic.setImageBitmap(BitmapFactory.decodeFile(Global.PATH.HeadImg + history.partner + ".png"));
			}
			else
				h.pic.setImageResource(R.drawable.nohead);
		}

		UserInfo temp = Global.map2Friend.get(history.partner);
		if(temp!=null)
		{
			String showName = temp.Ex_remark;
			if (showName == null || showName.equals("")) showName = temp.nickName;
			h.name.setText(showName);
		}
		else		//发消息的人不在好友列表中
		{
			h.name.setText(history.partner);
		}

		Message lastMessage = history.lastHistoryMsg;
		if((lastMessage.msgType& Global.MSG_TYPE.T_TEXT_MSG)>0)
		{
			SpannableString spannableString = FaceConversionUtil.getInstace().getExpressionString(context, lastMessage.text);
			h.lastMsg.setText( spannableString );
		}
		else if((lastMessage.msgType & Global.MSG_TYPE.T_PIC_MSG)>0)
			h.lastMsg.setText("[图片]");
		else if((lastMessage.msgType & Global.MSG_TYPE.T_VOICE_MSG)>0)
			h.lastMsg.setText("[语音]");

		if (history.unreadMsg.size() == 0)
		{
			h.msgCount.setVisibility(View.INVISIBLE);
		} else
		{
			h.msgCount.setText(String.valueOf(history.unreadMsg.size()));
			h.msgCount.setVisibility(View.VISIBLE);
		}
		h.time.setText(Global.formatDate(history.lastHistoryMsg.sendTime, "HH:mm"));
		return view;
	}

	class H
	{
		ImageView pic;
		TextView name;
		TextView lastMsg;
		TextView time;
		TextView msgCount;
	}
}
