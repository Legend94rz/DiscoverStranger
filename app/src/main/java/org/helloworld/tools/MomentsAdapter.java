package org.helloworld.tools;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.helloworld.BigPicAct;
import org.helloworld.CircleImageView;
import org.helloworld.NearbyStrangerAct;
import org.helloworld.R;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/7/24.
 */
public class MomentsAdapter extends BaseAdapter implements AbsListView.OnScrollListener
{
	private ArrayList<Fresh> freshs;
	private Context context;
	private LayoutInflater inflater;
	private ListView listView;
	private Handler handler;
	private AsyImageLoader loader;
	private boolean isScroll;

	public MomentsAdapter(Context context, ArrayList<Fresh> freshs, ListView listView, Handler handler)
	{
		this.context = context;
		this.freshs = freshs;
		inflater = LayoutInflater.from(context);
		this.listView = listView;
		this.listView.setOnScrollListener(this);
		this.handler = handler;
		loader = new AsyImageLoader(context);
	}

	@Override
	public int getCount()
	{
		return freshs.size();
	}

	@Override
	public Object getItem(int i)
	{
		return freshs.get(i);
	}

	@Override
	public long getItemId(int i)
	{
		return i;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup)
	{
		final Fresh fresh = freshs.get(i);
		final H h;
		if (view == null)
		{
			h = new H();
			view = inflater.inflate(R.layout.moments_item, viewGroup, false);
			h.ivHead = (CircleImageView) view.findViewById(R.id.ivHead);
			h.btnHello = (Button) view.findViewById(R.id.btnHello);
			h.llImages = (LinearLayout) view.findViewById(R.id.llcontet);
			h.tvNickName = (TextView) view.findViewById(R.id.tvNickname);
			h.tvText = (TextView) view.findViewById(R.id.tvText);
			h.tvTime = (TextView) view.findViewById(R.id.tvTime);
			h.tvTag = (TextView) view.findViewById(R.id.tvTag);
			view.setTag(h);
		}
		else
			h = (H) view.getTag();
		h.tvTime.setText(Global.getShowDate(fresh.time));
		h.tvTag.setText(fresh.tag);
		if(fresh.tag.equals(""))
			h.tvTag.setVisibility(View.GONE);
		if (fresh.tag.length() == 0)
			h.tvTag.setVisibility(View.GONE);
		else
			h.tvText.setVisibility(View.VISIBLE);
		SpannableString spannableString = FaceConversionUtil.getInstace().getExpressionString(context, fresh.text);
		h.tvText.setText(spannableString);
		h.ivHead.setImageResource(R.drawable.nohead);
		h.ivHead.setTag(Global.PATH.HeadImg + fresh.username);
		if (Global.map2Friend.containsKey(fresh.username) || TextUtils.equals(Global.mySelf.username, fresh.username))
		{
			if (!TextUtils.equals(Global.mySelf.username, fresh.username))
			{
				h.btnHello.setVisibility(View.INVISIBLE);
				h.tvNickName.setText(Global.map2Friend.get(fresh.username).getShowName());
			}
			else
			{
				h.btnHello.setVisibility(View.GONE);
				h.tvNickName.setText(Global.mySelf.nickName);
			}
			Bitmap bitmap = loader.loadDrawable(Global.PATH.HeadImg, "HeadImg", fresh.username + ".png", isScroll, 48 * Global.DPI, new AsyImageLoader.ImageCallback()
			{
				@Override
				public void imageLoaded(Bitmap bitmap, String url)
				{
					ImageView imageView = (ImageView) listView.findViewWithTag(Global.PATH.HeadImg + fresh.username);
					if (imageView != null && bitmap != null)
					{
						imageView.setImageBitmap(bitmap);
						notifyDataSetChanged();
					}
				}
			});
			if (bitmap != null)
			{
				h.ivHead.setImageBitmap(bitmap);
			}
		}
		else
		{
			h.btnHello.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					NearbyStrangerAct.SayHello(context, fresh.username, handler);
				}
			});
			h.tvNickName.setText(fresh.username);
		}
		h.llImages.removeAllViews();
		if (fresh.picNames != null && fresh.picNames.size() > 0)
		{
			for (final String fileName : fresh.picNames)
			{
				final ImageView imageView = new ImageView(context);
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(64 * Global.DPI, 64 * Global.DPI);
				layoutParams.setMargins(0, 0, 5 * Global.DPI, 0);
				imageView.setLayoutParams(layoutParams);
				imageView.setScaleType(ImageView.ScaleType.FIT_XY);
				imageView.setTag(Global.PATH.ChatPic + fileName);
				h.llImages.addView(imageView);
				imageView.setImageResource(R.drawable.nopic);
				Bitmap bitmap = loader.loadDrawable(Global.PATH.ChatPic, "ChatPic", fileName, isScroll, 128 * Global.DPI, new AsyImageLoader.ImageCallback()
				{
					@Override
					public void imageLoaded(Bitmap bitmap, String url)
					{
						ImageView view = (ImageView) h.llImages.findViewWithTag(Global.PATH.ChatPic + fileName);
						if (view != null)
							if (bitmap != null)
							{
								view.setImageBitmap(bitmap);
								notifyDataSetChanged();
							}
							else
								view.setImageResource(R.drawable.broken);
					}
				});
				if (bitmap != null)
					imageView.setImageBitmap(bitmap);
				imageView.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View view)
					{
						Intent I = new Intent(context, BigPicAct.class);
						I.putExtra("imgsrc", ((String) imageView.getTag()));
						context.startActivity(I);
					}
				});
			}
			h.llImages.setVisibility(View.VISIBLE);
		}
		else
			h.llImages.setVisibility(View.GONE);

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
	@Override
	public boolean areAllItemsEnabled()
	{
		return false;
	}
	@Override
	public boolean isEnabled(int position)
	{
		return false;
	}
	class H
	{
		CircleImageView ivHead;
		TextView tvNickName, tvText, tvTime, tvTag;
		Button btnHello;
		LinearLayout llImages;
	}
}
