package org.helloworld.JigsawGame;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.helloworld.CircleImageView;
import org.helloworld.R;
import org.helloworld.tools.FileUtils;
import org.helloworld.tools.Global;
import org.helloworld.tools.Rank;
import org.helloworld.tools.WebTask;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;

/**
 * 拼图游戏跟快速匹配共用排行榜
 * */
public class RankAct extends ActionBarActivity
{
	String pakageName;
	boolean order;
	ListView listView;
	PopupWindow popupWindow;
	String GameName;
	ArrayList<Rank> data;
	Handler handler;
	listViewAdapter adapter;
	class listViewAdapter extends BaseAdapter
	{
		private LayoutInflater inflater;
		public listViewAdapter()
		{
			inflater=LayoutInflater.from(RankAct.this);
		}

		@Override
		public int getCount()
		{
			return data.size();
		}

		@Override
		public Object getItem(int i)
		{
			return data.get(i);
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
			Rank rank=data.get(i);
			if(view==null)
			{
				h=new H();
				view=inflater.inflate(R.layout.item_rank,null);
				h.ivHead= (CircleImageView) view.findViewById(R.id.ivHead);
				h.tvName= (TextView) view.findViewById(R.id.tvName);
				h.tvScore= (TextView) view.findViewById(R.id.tvScore);
				view.setTag(h);
			}
			else
			{
				h= (H) view.getTag();
			}
			if(Global.map2Friend.containsKey(rank.username))
			{
				String filePath=Global.PATH.HeadImg+rank.username+".png";
				if(FileUtils.Exist(filePath))
					h.ivHead.setImageBitmap(FileUtils.getOptimalBitmap(filePath,128*Global.DPI));
				else
					h.ivHead.setImageResource(R.drawable.nohead);
				h.tvName.setText(Global.map2Friend.get(rank.username).getShowName());
			}
			else
			{
				h.ivHead.setImageResource(R.drawable.nohead);
				h.tvName.setText(rank.username);
			}
			h.tvScore.setText(String.valueOf(rank.score));
			return view;
		}
		class H
		{
			CircleImageView ivHead;
			TextView tvName,tvScore;
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rank);
		pakageName=getIntent().getStringExtra("pakageName");
		order=getIntent().getBooleanExtra("order", true);
		GameName=getIntent().getStringExtra("GameName");
		data=new ArrayList<>();
		adapter=new listViewAdapter();
		listView.setAdapter(adapter);
		handler=new Handler(new Handler.Callback()
		{
			@Override
			public boolean handleMessage(Message message)
			{
				if(message.what==0)
				{
					if(message.obj!=null)
					{
						data.clear();
						SoapObject result= (SoapObject) ((SoapObject) message.obj).getProperty(0);
						int count=result.getPropertyCount();
						for (int i=0;i<count;i++)
						{
							data.add(Rank.parse((SoapObject) result.getProperty(i)));
						}
						adapter.notifyDataSetChanged();
					}
				}
				return true;
			}
		});
		new WebTask(handler,0).execute("getRank",2,"pakageName",pakageName,"order",order);
	}

}
