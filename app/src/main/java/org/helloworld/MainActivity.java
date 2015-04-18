package org.helloworld;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MainActivity extends Activity implements  View.OnClickListener,ViewPager.OnPageChangeListener
{
	public static android.os.Handler handler;
	private ListView lvFriends;
	private ListView lvHistory;
	public static int updateCount=0;
	ContactAdapter adapter;
	myViewPagerAdapter pagerAdapter;
	HistoryAdapter hisAdapter;
	private int mViewCount;
	private int mCurSel;
	private LinearLayout[] mImageViews;
	private TextView liaotian;
	private TextView faxian;
	private TextView tongxunlu;
	ArrayList<UserInfo> list;

	ViewPager viewPager;
	List<View> pages;
	/**
	 * 处理导航标签的点击事件
	 * */
	@Override
	public void onClick(View v)
	{
		int pos = (Integer)(v.getTag());
		viewPager.setCurrentItem(pos,true);
		setCurPoint(pos);
	}
	/**
	 * 导航标签指示器
	 * */
	private void setCurPoint(int index)
	{
		if (index < 0 || index > mViewCount - 1 || mCurSel == index){
			return ;
		}
		mImageViews[mCurSel].setEnabled(true);
		mImageViews[index].setEnabled(false);
		mCurSel = index;

		if(index == 0){
			liaotian.setTextColor(0xff228B22);
			faxian.setTextColor(Color.BLACK);
			tongxunlu.setTextColor(Color.BLACK);
		}else if(index==1){
			liaotian.setTextColor(Color.BLACK);
			faxian.setTextColor(0xff228B22);
			tongxunlu.setTextColor(Color.BLACK);
		}else{
			liaotian.setTextColor(Color.BLACK);
			faxian.setTextColor(Color.BLACK);
			tongxunlu.setTextColor(0xff228B22);
		}
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels){}
	@Override
	public void onPageSelected(int position)
	{
		setCurPoint(position);
	}
	@Override
	public void onPageScrollStateChanged(int state){}

	public class parserWithExtraAsync extends AsyncTask<JSONObject, Void, Void>
	{
		private int index;
		public parserWithExtraAsync(int index)
		{
			this.index = index;
		}
		@Override
		protected Void doInBackground(JSONObject... jsons)
		{
			WebService getUser=new WebService("GetUser");
			try
			{
				SoapObject result = getUser.addProperty("name", jsons[0].getString("name")).call();
				list.set(index,UserInfo.parse(result));
			}
			catch (NullPointerException ignored){}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
			/*Todo 下载图片、补全额外信息*/
			try
			{
				list.get(index).Ex_remark=jsons[0].getString("remark");
			}
			catch (JSONException ignored){list.get(index).Ex_remark=null;}
			synchronized ((Object)updateCount)
			{
				updateCount++;
				if(updateCount>=list.size())
				{
					updateCount=0;
					android.os.Message message=new android.os.Message();
					message.what=Global.MSG_WHAT.W_REFRESH;
					MainActivity.handler.sendMessage(message);
				}
			}
			return null;
		}
	}

	@Override
	/**
	 * 这个activity再次回到前台时，历史消息很可能发生变化，要更新
	 * */
	protected void onResume()
	{
		super.onResume();
		for (History h : Global.map.values())
		{
			int i;
			for (i = 0; i < Global.list2.size(); i++)
			{
				if (Global.list2.get(i).fromName.equals(h.fromName))
					break;
			}
			if (i == Global.list2.size()) Global.list2.add(h);
		}
		if (hisAdapter != null)
			hisAdapter.notifyDataSetChanged();
		else
		{
			hisAdapter=new HistoryAdapter(Global.list2,MainActivity.this);
			lvHistory.setAdapter(hisAdapter);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();

		//Todo 异步加载表情图片放在载入界面里
		new Thread(new Runnable() {
			@Override
			public void run() {
				FaceConversionUtil.getInstace().getFileText(getApplication());
			}
		}).start();

		handler = new android.os.Handler()
		{
			@Override
			public void handleMessage(android.os.Message msg)
			{
				switch (msg.what)
				{
					case Global.MSG_WHAT.W_RECEIVED_NEW_MSG:
						ArrayList<Message> received= (ArrayList<Message>) msg.obj;
						for (Message m : received)
						{
							History h;
							m.msgType=Global.MSG_TYPE.T_RECEIVE_MSG & (~Global.MSG_TYPE.T_SEND_MSG);
							h=Global.map.get(m.FromId);
							if(h==null)
								h=new History();
							h.fromName=m.FromId;
							h.imgPath="xx";			//Todo 从userinfo里获取
							h.historyMsg.add(m);
							h.unreadCount++;
							if(ChatActivity.handler!=null)			//如果当前有活动的聊天界面则直接转发给ChatActivity
							{
								android.os.Message message=new android.os.Message();
								message.what=Global.MSG_WHAT.W_RECEIVED_NEW_MSG;
								message.obj=m;
								ChatActivity.handler.sendMessage(message);
							}
							else
							{
								Global.map.put(m.FromId, h);
								if (!Global.list2.contains(h))
									Global.list2.add(h);
							}
						}
						if(hisAdapter==null)
						{
							hisAdapter=new HistoryAdapter(Global.list2,MainActivity.this);
							lvHistory.setAdapter(hisAdapter);
						}
						else
							hisAdapter.notifyDataSetChanged();
						break;
					case Global.MSG_WHAT.W_GOT_FRIENDS_LIST:
						try
						{
							SoapObject obj = (SoapObject) (msg.obj);
							JSONObject json = new JSONObject(obj.getProperty(0).toString());
							JSONArray array = json.getJSONArray("friends");
							for (int i = 0; i < array.length(); i++)
							{
								JSONObject userInfo = array.getJSONObject(i);
								UserInfo friend = new UserInfo(userInfo.getString("name"));
								if(list.size()<array.length())
									list.add(friend);
								else
									list.set(i,friend);
								new parserWithExtraAsync(i).execute(userInfo);
							}
							BindAdapter(list);
						}
						catch (NullPointerException ignored){}
						catch (JSONException e)
						{
							e.printStackTrace();
						}
						break;
					case Global.MSG_WHAT.W_ERROR_NETWORK:
						Toast.makeText(MainActivity.this,Global.ERROR_HINT.HINT_ERROR_NETWORD,Toast.LENGTH_SHORT).show();
						break;
					case Global.MSG_WHAT.W_REFRESH:
						if(adapter!=null)
							adapter.notifyDataSetChanged();
						break;
				}
			}
		};
		FlushFriendsList();
		Intent I=new Intent(this,MsgPullService.class);
		startService(I);
	}

	private void init()
	{
		list=new ArrayList<UserInfo>();
		liaotian = (TextView)findViewById(R.id.tvLiaotian);
		faxian = (TextView)findViewById(R.id.tvFaxian);
		tongxunlu = (TextView)findViewById(R.id.tvTongxunlu);

		viewPager= (ViewPager) findViewById(R.id.viewpager);
		LayoutInflater mInflater = LayoutInflater.from(this);
		View v1=mInflater.inflate(R.layout.page1,null);
		View v2=mInflater.inflate(R.layout.faxian,null);
		View v3=mInflater.inflate(R.layout.page3,null);
		lvFriends= (ListView) v3.findViewById(R.id.listView);
		lvFriends.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
			{
				Intent chat=new Intent(MainActivity.this,ChatActivity.class);
				String to=list.get(i).username;
				chat.putExtra("chatTo", to);
				startActivity(chat);
			}
		});
		lvHistory= (ListView) v1.findViewById(R.id.listView);
		pages=new ArrayList<View>();
		pages.add(v1);pages.add(v2);pages.add(v3);
		pagerAdapter = new myViewPagerAdapter(this,pages);
		viewPager.setAdapter(pagerAdapter);
		viewPager.setCurrentItem(0);
		viewPager.setOnPageChangeListener(this);

		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.lllayout);
		mViewCount = pages.size();
		mImageViews = new LinearLayout[mViewCount];
		for(int i = 0; i < mViewCount; i++)    	{
			mImageViews[i] = (LinearLayout) linearLayout.getChildAt(i);
			mImageViews[i].setEnabled(true);
			mImageViews[i].setOnClickListener(this);
			mImageViews[i].setTag(i);
		}
		mCurSel = 0;
		mImageViews[mCurSel].setEnabled(false);
	}

	void BindAdapter(ArrayList<UserInfo> list)
	{
		if(adapter==null)
		{
			adapter = new ContactAdapter(this, list);
			lvFriends.setAdapter(adapter);
		}
		adapter.notifyDataSetChanged();
		Toast.makeText(this,"已更新",Toast.LENGTH_SHORT).show();
	}

	void FlushFriendsList()
	{
		updateCount=0;
		new Task(Global.MSG_WHAT.W_GOT_FRIENDS_LIST).execute("getFriends", 1, "name", Global.mySelf.username);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		switch (id)
		{
			case R.id.action_settings:
				return true;
			case R.id.meuFlush:
				FlushFriendsList();
				Toast.makeText(MainActivity.this,"正在刷新",Toast.LENGTH_SHORT).show();
				break;
		}

		return super.onOptionsItemSelected(item);
	}
}
