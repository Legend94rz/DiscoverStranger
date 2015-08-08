package org.helloworld.tools;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.helloworld.R;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/7/19.
 */
public class StrangerAdapter extends BaseAdapter
{
	private ArrayList<PositionInfo> positionInfos;
	private Context context;

	public StrangerAdapter(Context context, ArrayList<PositionInfo> positionInfos)
	{
		this.context = context;
		this.positionInfos = positionInfos;
	}

	@Override
	public int getCount()
	{
		return positionInfos.size();
	}

	@Override
	public Object getItem(int i)
	{
		return positionInfos.get(i);
	}

	@Override
	public long getItemId(int i)
	{
		return i;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup)
	{
		H h;
		if (view == null)
		{
			h = new H();
			LayoutInflater inflater = LayoutInflater.from(context);
			view = inflater.inflate(R.layout.tongxunlu, viewGroup, false);
			h.ivHead = (ImageView) view.findViewById(R.id.tx1);
			h.tvName = (TextView) view.findViewById(R.id.tx2);
			view.setTag(h);
		}
		else
		{
			h = (H) view.getTag();
		}
		PositionInfo pi = positionInfos.get(i);
		if (Global.map2Friend.containsKey(pi.strangerName))
		{
			h.ivHead.setImageBitmap(BitmapFactory.decodeFile(Global.PATH.HeadImg + pi.strangerName + ".png"));
			h.tvName.setText(Global.map2Friend.get(pi.strangerName).getShowName());
		}
		else
			h.tvName.setText(pi.strangerName);
		return view;
	}

	class H
	{
		ImageView ivHead;
		TextView tvName;
	}
}
