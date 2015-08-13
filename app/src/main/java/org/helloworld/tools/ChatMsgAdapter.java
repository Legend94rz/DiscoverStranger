package org.helloworld.tools;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.helloworld.BigPicAct;
import org.helloworld.ChatActivity;
import org.helloworld.CircleImageView;
import org.helloworld.R;

import java.util.List;

/**
 * 聊天消息数据
 */
public class ChatMsgAdapter extends BaseAdapter implements AbsListView.OnScrollListener
{
	@Override
	public void onScrollStateChanged(AbsListView absListView, int scrollState)
	{
		if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE)
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

	public interface IMsgViewType
	{
		int IMVT_COM_MSG = 0;
		int IMVT_TO_MSG = 1;
	}

	private List<Message> coll;
	private LayoutInflater mInflater;
	private Context context;
	private AsyImageLoader loader;
	private ListView listView;
	private boolean isScroll;

	public ChatMsgAdapter(Context context, List<Message> coll, ListView listView)
	{
		this.coll = coll;
		mInflater = LayoutInflater.from(context);
		this.context = context;
		loader = new AsyImageLoader(context);
		this.listView = listView;
		listView.setOnScrollListener(this);
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
		final Message entity = coll.get(position);
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
			viewHolder.pbPlayVoice = (ProgressBar) convertView.findViewById(R.id.pbPlayVoice);
			viewHolder.tvContent = (TextView) convertView.findViewById(R.id.tvChatcontent);
			viewHolder.isComMsg = isComMsg;
			viewHolder.ivPic = (ImageView) convertView.findViewById(R.id.ivPic);
			viewHolder.ibResendbtn = (ImageButton) convertView.findViewById(R.id.ibResendbtn);
			viewHolder.pbSendbar = (ProgressBar) convertView.findViewById(R.id.pbSendbar);
			viewHolder.ivHead = (CircleImageView) convertView.findViewById(R.id.iv_userhead);
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
		else if ((entity.msgType & Global.MSG_TYPE.T_PIC_MSG) > 0 && entity.sendState != 2)
		{
			viewHolder.tvContent.setVisibility(View.GONE);
			viewHolder.ivPic.setVisibility(View.VISIBLE);
			String url = Global.PATH.ChatPic + entity.text;
			viewHolder.ivPic.setTag(url);
			viewHolder.ivPic.setImageResource(R.drawable.nopic);
			Bitmap cacheBitmap = loader.loadDrawable(Global.PATH.ChatPic, "ChatPic", entity.text, isScroll, 200 * Global.DPI, new AsyImageLoader.ImageCallback()
			{
				@Override
				public void imageLoaded(Bitmap bitmap, String url)
				{
					ImageView imageView = (ImageView) listView.findViewWithTag(url);
					if (imageView != null)
					{
						if (bitmap != null)
						{
							imageView.setImageBitmap(bitmap);
							entity.sendState = 0;
						}
						else
							entity.sendState = 2;
						notifyDataSetChanged();
					}
				}
			});
			if (cacheBitmap != null)
			{
				entity.sendState = 0;
				viewHolder.ivPic.setImageBitmap(cacheBitmap);
			}
			else
				entity.sendState = 1;
		}
		else if ((entity.msgType & Global.MSG_TYPE.T_VOICE_MSG) > 0)
		{
			viewHolder.tvContent.setVisibility(View.VISIBLE);
			viewHolder.ivPic.setVisibility(View.GONE);
			String[] parts = entity.text.split("~");
			spannableString = new SpannableString(parts[1]);

			Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.play_alt);
			bitmap = Bitmap.createScaledBitmap(bitmap, 28 * Global.DPI, 28 * Global.DPI, true);

			ImageSpan imageSpan = new ImageSpan(context, bitmap);
			spannableString.setSpan(imageSpan, 0, 7, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
		}
		viewHolder.tvContent.setText(spannableString);

		viewHolder.ivHead.setImageResource(R.drawable.nohead);
		String url=Global.PATH.HeadImg+entity.fromId+".png";
		viewHolder.ivHead.setTag(url);
		Bitmap cacheHead=loader.loadDrawable(Global.PATH.HeadImg, "HeadImg", entity.fromId + ".png", isScroll, 128 * Global.DPI, new AsyImageLoader.ImageCallback()
		{
			@Override
			public void imageLoaded(Bitmap bitmap, String url)
			{
				ImageView iv = (ImageView) listView.findViewWithTag(url);
				if (iv != null)
				{
					if(bitmap!=null)
					{
						iv.setImageBitmap(bitmap);
						notifyDataSetChanged();
					}
				}
			}
		});
		if(cacheHead!=null)
			viewHolder.ivHead.setImageBitmap(cacheHead);

		viewHolder.tvSendTime.setText(Global.getShowDate(entity.sendTime));
		if (position == 0 || entity.sendTime.getTime() - coll.get(position - 1).sendTime.getTime() > 1000 * 60)    //两条消息间隔大于1分钟才显示时间
			viewHolder.tvSendTime.setVisibility(View.VISIBLE);
		else
			viewHolder.tvSendTime.setVisibility(View.GONE);

		if ((entity.msgType & Global.MSG_TYPE.T_VOICE_MSG) == 0)
			viewHolder.pbPlayVoice.setVisibility(View.GONE);
		else
			viewHolder.pbPlayVoice.setVisibility(View.INVISIBLE);
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

	@Override
	public boolean isEnabled(int position)
	{
		return false;
	}

	@Override
	public boolean areAllItemsEnabled()
	{
		return false;
	}

	class ViewHolder implements View.OnClickListener
	{
		public TextView tvSendTime;
		public TextView tvContent;
		public boolean isComMsg = true;
		public ImageButton ibResendbtn;
		public ProgressBar pbSendbar;
		public ImageView ivPic;
		public CircleImageView ivHead;
		public Message msg;
		public ProgressBar pbPlayVoice;

		@Override
		public void onClick(View view)
		{
			//CustomToast.show(context, view.toString(), Toast.LENGTH_SHORT);

			switch (view.getId())
			{
				case R.id.ivPic:
					Intent bigPic = new Intent(context, BigPicAct.class);
					if (FileUtils.Exist(Global.PATH.ChatPic + msg.text))
						bigPic.putExtra("imgsrc", Global.PATH.ChatPic + msg.text);
					context.startActivity(bigPic);
					break;
				case R.id.tvChatcontent:
					if ((msg.msgType & Global.MSG_TYPE.T_VOICE_MSG) > 0)
					{
						android.os.Message m = new android.os.Message();
						m.what = Global.MSG_WHAT.W_PLAY_SOUND;
						Bundle data = new Bundle();
						data.putString("content", msg.text.split("~")[0]);
						String tmp = msg.text.split("    ")[1];
						data.putString("length", tmp.substring(0, tmp.length() - 1));
						m.setData(data);
						m.obj = pbPlayVoice;
						ChatActivity.handler.sendMessage(m);
					}
					break;
				case R.id.ibResendbtn:
					if (msg.extra != null)    //历史消息不能重发
					{
						android.os.Message m = new android.os.Message();
						m.obj = msg;
						m.what = Global.MSG_WHAT.W_RESEND_MSG;
						ChatActivity.handler.sendMessage(m);
					}
					break;
			}
		}
	}

}
