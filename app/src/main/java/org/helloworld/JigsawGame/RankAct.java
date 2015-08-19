package org.helloworld.JigsawGame;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.navisdk.model.GeoLocateModel;

import org.helloworld.BaseActivity;
import org.helloworld.CircleImageView;
import org.helloworld.R;
import org.helloworld.tools.CustomToast;
import org.helloworld.tools.FileUtils;
import org.helloworld.tools.Game;
import org.helloworld.tools.Global;
import org.helloworld.tools.Rank;
import org.helloworld.tools.WebTask;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;

/**
 * 拼图游戏跟快速匹配共用排行榜
 * */
public class RankAct extends Activity
{
	String pakageName;
	boolean order;
	ListView listView;
	PopupWindow popupWindow;
	String GameName;
	ArrayList<Rank> data;
	Handler handler;
	listViewAdapter adapter;
	LayoutInflater inflater;
	private View popView;
	private int longClickIndex;

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
		inflater=LayoutInflater.from(this);
		listView= (ListView) findViewById(R.id.listView);
		popView=inflater.inflate(R.layout.popup_ranklist_window, null);
		pakageName=getIntent().getStringExtra("pakageName");
		order=getIntent().getBooleanExtra("order", true);
		GameName=getIntent().getStringExtra("gameName");
		data=new ArrayList<>();
		adapter=new listViewAdapter();
		listView.setAdapter(adapter);
		handler=new Handler(new Handler.Callback()
		{
			@Override
			public boolean handleMessage(Message message)
			{
				switch (message.what)
				{
					case 0:
						if (message.obj != null)
						{
							data.clear();
							SoapObject result = (SoapObject) ((SoapObject) message.obj).getProperty(0);
							int count = result.getPropertyCount();
							for (int i = 0; i < count; i++)
							{
								data.add(Rank.parse((SoapObject) result.getProperty(i)));
							}
							adapter.notifyDataSetChanged();
						}
						else
						{
							CustomToast.show(RankAct.this,"获取数据失败",Toast.LENGTH_SHORT);
						}
						break;
					case Global.MSG_WHAT.W_SENDED_REQUEST:
						SoapObject soapObject= (SoapObject) message.obj;
						if(soapObject!=null)
						{
							if(Boolean.parseBoolean(soapObject.getPropertyAsString(0)))
								CustomToast.show(RankAct.this,"发送成功", Toast.LENGTH_SHORT);
							else
								CustomToast.show(RankAct.this,"发送失败",Toast.LENGTH_SHORT);
						}
						break;
				}
				return true;
			}
		});
		new WebTask(handler,0).execute("getRank",2,"pakageName",pakageName,"order",order);
		popupWindow = new PopupWindow(popView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		popupWindow.setFocusable(true);
		popupWindow.setOutsideTouchable(true);
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
		{
			@Override
			public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l)
			{
				longClickIndex = i;
				int[] a = new int[2];
				view.getLocationOnScreen(a);
				popupWindow.showAtLocation(listView, Gravity.BOTTOM | Gravity.CENTER, 0, Global.screenHeight - a[1]);
				return false;
			}
		});
		popView.findViewById(R.id.btnGood).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				popupWindow.dismiss();
				JSONObject jobj = new JSONObject();
				try
				{
					jobj.put("userName", Global.mySelf.username);
					jobj.put("Text", String.format("%s 在游戏 %s 中赞了你。", Global.mySelf.username, GameName));
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}
				new WebTask(handler, Global.MSG_WHAT.W_SENDED_REQUEST)
					.execute("pushMsg", 5, "from", "通知",
								"to", data.get(longClickIndex).username,
								"msg", jobj.toString(),
								"time", Global.formatDate(Global.getDate(), "yyyy-MM-dd HH:mm:ss"),
								"msgType", String.valueOf(Global.MSG_TYPE.T_TEXT_MSG));
			}
		});
		popView.findViewById(R.id.btnBad).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				popupWindow.dismiss();
				JSONObject jobj=new JSONObject();
				try
				{
					jobj.put("userName",Global.mySelf.username);
					jobj.put("Text",String.format("%s 在游戏 %s 中踩了你。",Global.mySelf.username, GameName));
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}
				new WebTask(handler, Global.MSG_WHAT.W_SENDED_REQUEST)
					.execute("pushMsg", 5, "from", "通知",
								"to", data.get(longClickIndex).username,
								"msg", jobj.toString() ,
								"time", Global.formatDate(Global.getDate(), "yyyy-MM-dd HH:mm:ss"),
								"msgType", String.valueOf(Global.MSG_TYPE.T_TEXT_MSG));

			}
		});
	}

}
