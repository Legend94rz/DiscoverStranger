package org.helloworld;

import android.content.Context;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

/**
 * *****************************************
 *
 * @author 廖乃波
 * @文件名称    : ChatMsgAdapter.java
 * @创建时间    : 2013-1-27 下午02:33:16
 * @文件描述    : 消息数据填充起
 * *****************************************
 */
public class ChatMsgAdapter extends BaseAdapter
{

	public static interface IMsgViewType
	{
		int IMVT_COM_MSG = 0;
		int IMVT_TO_MSG = 1;
	}

	private List<Message> coll;
	private LayoutInflater mInflater;
	private Context context;

	public ChatMsgAdapter(Context context, List<Message> coll)
	{
		this.coll = coll;
		mInflater = LayoutInflater.from(context);
		this.context = context;
	}

	public int getCount()
	{
		return coll.size();
	}

	public Object getItem(int position)
	{
		return coll.get(position);
	}

	public long getItemId(int position)
	{
		return position;
	}

	public int getItemViewType(int position)
	{
		Message entity = coll.get(position);

		if ((entity.msgType & Global.MSG_TYPE.T_RECEIVE_MSG) > 0)
		{
			return IMsgViewType.IMVT_COM_MSG;
		} else
		{
			return IMsgViewType.IMVT_TO_MSG;
		}

	}

	public int getViewTypeCount()
	{
		return 2;
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		Message entity = coll.get(position);
		boolean isComMsg = (entity.msgType & Global.MSG_TYPE.T_RECEIVE_MSG) > 0;

		ViewHolder viewHolder = null;
		if (convertView == null)
		{
			if (isComMsg)
			{
				convertView = mInflater.inflate(R.layout.chatting_item_msg_text_left, null);
			} else
			{
				convertView = mInflater.inflate(R.layout.chatting_item_msg_text_right, null);
			}

			viewHolder = new ViewHolder();
			viewHolder.tvSendTime = (TextView) convertView.findViewById(R.id.tv_sendtime);
			viewHolder.tvContent = (TextView) convertView.findViewById(R.id.tv_chatcontent);
			viewHolder.isComMsg = isComMsg;
			viewHolder.ibResendbtn= (ImageButton) convertView.findViewById(R.id.ibResendbtn);
			viewHolder.pbSendbar= (ProgressBar) convertView.findViewById(R.id.pbSendbar);
			convertView.setTag(viewHolder);
		} else
		{
			viewHolder = (ViewHolder) convertView.getTag();
		}

		viewHolder.tvSendTime.setText(entity.getDateWithFormat("yyyy-MM-dd HH:mm"));
		SpannableString spannableString = FaceConversionUtil.getInstace().getExpressionString(context, entity.Text);
		viewHolder.tvContent.setText(spannableString);
		switch (entity.sendState)
		{
			case 0:	// 消息发送成功
				viewHolder.ibResendbtn.setVisibility(View.GONE);
				viewHolder.pbSendbar.setVisibility(View.GONE);
				break;
			case 1:	//正在发送
				viewHolder.pbSendbar.setVisibility(View.VISIBLE);
				viewHolder.ibResendbtn.setVisibility(View.GONE);
				break;
			case 2:	//发送失败
				viewHolder.pbSendbar.setVisibility(View.GONE);
				viewHolder.ibResendbtn.setVisibility(View.VISIBLE);
				break;
		}
		//Todo 设置重新发送点击事件 以及 播放语音消息事件
		return convertView;
	}

	class ViewHolder
	{
		public TextView tvSendTime;
		public TextView tvContent;
		public boolean isComMsg = true;
		public ImageButton ibResendbtn;
		public ProgressBar pbSendbar;
	}

}
