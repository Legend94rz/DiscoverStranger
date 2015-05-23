package org.DiscoverStranger.tools;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.DiscoverStranger.BigPicAct;
import org.DiscoverStranger.ChatActivity;
import org.DiscoverStranger.FaceConversionUtil;
import org.DiscoverStranger.R;

import java.util.List;

/**
 * 聊天消息数据
 */
public class ChatMsgAdapter extends BaseAdapter
{

	public interface IMsgViewType
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
		}
		else
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

		ViewHolder viewHolder;
		if (convertView == null)
		{
			if (isComMsg)
			{
				convertView = mInflater.inflate(R.layout.chatting_item_msg_text_left, null);
			}
			else
			{
				convertView = mInflater.inflate(R.layout.chatting_item_msg_text_right, null);
			}

			viewHolder = new ViewHolder();
			viewHolder.tvSendTime = (TextView) convertView.findViewById(R.id.tv_sendtime);
			viewHolder.tvContent = (TextView) convertView.findViewById(R.id.tvChatcontent);
			viewHolder.isComMsg = isComMsg;
			viewHolder.ivPic = (ImageView) convertView.findViewById(R.id.ivPic);
			viewHolder.ibResendbtn = (ImageButton) convertView.findViewById(R.id.ibResendbtn);
			viewHolder.pbSendbar = (ProgressBar) convertView.findViewById(R.id.pbSendbar);
			viewHolder.ivHead = (ImageView) convertView.findViewById(R.id.iv_userhead);
			viewHolder.tvContent.setOnClickListener(viewHolder);
			viewHolder.ibResendbtn.setOnClickListener(viewHolder);
			viewHolder.ivPic.setOnClickListener(viewHolder);
			convertView.setTag(viewHolder);
		}
		else
		{
			viewHolder = (ViewHolder) convertView.getTag();
		}

		viewHolder.msg = entity;

		SpannableString spannableString = null;
		if ((entity.msgType & Global.MSG_TYPE.T_TEXT_MSG) > 0)
		{
			viewHolder.tvContent.setVisibility(View.VISIBLE);
			viewHolder.ivPic.setVisibility(View.GONE);
			spannableString = FaceConversionUtil.getInstace().getExpressionString(context, entity.text);
		}
		else if ((entity.msgType & Global.MSG_TYPE.T_PIC_MSG) > 0)
		{
			viewHolder.tvContent.setVisibility(View.GONE);
			viewHolder.ivPic.setVisibility(View.VISIBLE);
			if (!isComMsg)        //如果是发送的消息，则显示本地照片
			{
				if (FileUtils.Exist(entity.extra.getString("localPath")))
				{
					viewHolder.ivPic.setImageBitmap(FileUtils.getOptimalBitmap(entity.extra.getString("localPath"),true));
				}
			}
			else                //接收到的消息，则显示下载的照片
			{
				if (FileUtils.Exist(Global.PATH.ChatPic + entity.text))
				{
					viewHolder.ivPic.setImageBitmap(FileUtils.getOptimalBitmap(Global.PATH.ChatPic + entity.text,true));
				}
			}
		}
		else if ((entity.msgType & Global.MSG_TYPE.T_VOICE_MSG) > 0)
		{
			viewHolder.tvContent.setVisibility(View.VISIBLE);
			viewHolder.ivPic.setVisibility(View.GONE);
			String[] parts = entity.text.split("~");
			spannableString = new SpannableString(parts[1]);

			Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.play_alt);
			bitmap = Bitmap.createScaledBitmap(bitmap, 35, 35, true);

			ImageSpan imageSpan = new ImageSpan(context, bitmap);
			spannableString.setSpan(imageSpan, 0, 7, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
		}
		viewHolder.tvContent.setText(spannableString);

		if(FileUtils.Exist(Global.PATH.HeadImg + entity.fromId +".png"))
			viewHolder.ivHead.setImageBitmap(BitmapFactory.decodeFile(Global.PATH.HeadImg + entity.fromId +".png"));
		else
			viewHolder.ivHead.setImageResource(R.drawable.nohead);

		viewHolder.tvSendTime.setText(entity.getDateWithFormat("yyyy-MM-dd HH:mm"));

		switch (entity.sendState)
		{
			case 0:    // 消息发送成功
				viewHolder.ibResendbtn.setVisibility(View.GONE);
				viewHolder.pbSendbar.setVisibility(View.GONE);
				break;
			case 1:    //正在发送
				viewHolder.pbSendbar.setVisibility(View.VISIBLE);
				viewHolder.ibResendbtn.setVisibility(View.GONE);
				break;
			case 2:    //发送失败
				viewHolder.pbSendbar.setVisibility(View.GONE);
				viewHolder.ibResendbtn.setVisibility(View.VISIBLE);
				break;
		}

		return convertView;
	}

	class ViewHolder implements View.OnClickListener
	{
		public TextView tvSendTime;
		public TextView tvContent;
		public boolean isComMsg = true;
		public ImageButton ibResendbtn;
		public ProgressBar pbSendbar;
		public ImageView ivPic;
		public ImageView ivHead;
		public Message msg;

		@Override
		public void onClick(View view)
		{
			//Toast.makeText(context, view.toString(), Toast.LENGTH_SHORT).show();

			switch (view.getId())
			{
				case R.id.ivPic:
					if(FileUtils.Exist(Global.PATH.ChatPic + tvContent.getText()))
					{
						Intent bigPic = new Intent(context, BigPicAct.class);
						if(isComMsg)
							bigPic.putExtra("imgsrc", Global.PATH.ChatPic + msg.text);
						else
							bigPic.putExtra("imgsrc",msg.extra.getString("localPath"));
						context.startActivity(bigPic);
					}
					break;
				case R.id.tvChatcontent:
					if((msg.msgType & Global.MSG_TYPE.T_VOICE_MSG)>0)
					{
						android.os.Message m=new android.os.Message();
						m.what= Global.MSG_WHAT.W_PLAY_SOUND;
						m.obj=msg;
						ChatActivity.handler.sendMessage(m);
					}
					break;
				case R.id.ibResendbtn:
					android.os.Message m=new android.os.Message();
					m.obj=msg;
					m.what= Global.MSG_WHAT.W_RESEND_MSG;
					ChatActivity.handler.sendMessage(m);
					break;
			}
		}
	}

}
