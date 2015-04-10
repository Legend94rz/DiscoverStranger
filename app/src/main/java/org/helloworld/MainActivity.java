package org.helloworld;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseAdapter;
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
	@Override
	public void onClick(View v)
	{
		//Todo V.getTag(). tag的数据从哪来的？？
		int pos = (Integer)(v.getTag());
		viewPager.setCurrentItem(pos,true);
		setCurPoint(pos);
	}

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
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
	{
	}

	@Override
	public void onPageSelected(int position)
	{
		Toast.makeText(this,String.valueOf(position),Toast.LENGTH_SHORT).show();
		setCurPoint(position);
		//View v=viewPager.getChildAt(position);
	}
	@Override
	public void onPageScrollStateChanged(int state)
	{
	}

	/**
	 * 不断从服务器拉取新消息
	 * */
	 public class MsgPuller implements Runnable
	{
		boolean isError;
		@Override
		public void run()
		{
			WebService pullMsg = new WebService("pullMsg");
			pullMsg.addProperty("name", Global.mySelf.username);
			while (true)
			{
				try
				{
					SoapObject messages = pullMsg.call();
					SoapObject result= (SoapObject) messages.getProperty(0);
					int T=result.getPropertyCount();
					for (int i = 0; i < T; i++)
					{
						org.helloworld.Message msg = org.helloworld.Message.parse((SoapObject)result.getProperty(i));
						android.os.Message newMessageHint=new android.os.Message();
						Bundle data=new Bundle();
						data.putString("content",msg.Text);
						newMessageHint.what=2;
						newMessageHint.setData(data);
						handler.sendMessage(newMessageHint);
					}
					Thread.sleep(5000);
					isError=false;
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				catch (NullPointerException e)
				{
					e.printStackTrace();
					if(!isError)
					{
						android.os.Message M=new android.os.Message();
						M.what=Global.MSG_WHAT.W_ERROR_NETWORK;
						MainActivity.handler.sendMessage(M);
						isError=true;
					}
				}
			}
		}
	}
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
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
		handler = new android.os.Handler()
		{
			@Override
			public void handleMessage(android.os.Message msg)
			{
				switch (msg.what)
				{
					case Global.MSG_WHAT.W_RECEIVED_A_NEW_MSG:
						Toast.makeText(MainActivity.this, msg.getData().getString("content"), Toast.LENGTH_SHORT).show();
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
								//new parserWithExtraAsync(i).execute(userInfo);
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
						if(adapter!=null)adapter.notifyDataSetChanged();
						break;
				}
			}
		};
		FlushFriendsList();
		new Thread(new MsgPuller()).start();
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

	private ArrayList<UserInfo> getContact(){
		ArrayList<UserInfo> hcList = new ArrayList<UserInfo>();

		UserInfo c0 = new UserInfo();
		c0.username="23423";

		UserInfo c1 = new UserInfo();
		c1.username="23";

		UserInfo c2 = new UserInfo();
		c2.username="2";

		hcList.add(c0);
		hcList.add(c1);

		return hcList;
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
