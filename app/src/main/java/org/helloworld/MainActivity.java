package org.helloworld;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends Activity implements View.OnClickListener, ViewPager.OnPageChangeListener
{
	public static android.os.Handler handler;
	private DrawerLayout drawerLayout;
	View drawerContent;
	private ListView lvFriends;
	private ListView lvHistory;
	public static int updateCount = 0;
	private ContactAdapter contactAdapter;
	myViewPagerAdapter pagerAdapter;
	HistoryAdapter hisAdapter;
	private int mViewCount;
	private int mCurSel;
	private LinearLayout[] mImageViews;
	private TextView liaotian;
	private TextView faxian;
	private TextView tongxunlu;
	TextView emptyNotice;

	ViewPager viewPager;
	List<View> pages;
	SwipeRefreshLayout refreshLayout1, refreshLayout2;

	/**
	 * 处理导航标签的点击事件
	 */
	@Override
	public void onClick(View v)
	{
		int pos = (Integer) (v.getTag());
		viewPager.setCurrentItem(pos, true);
		setCurPoint(pos);
	}

	/**
	 * 导航标签指示器
	 */
	private void setCurPoint(int index)
	{
		if (index < 0 || index > mViewCount - 1 || mCurSel == index)
		{
			return;
		}
		mImageViews[mCurSel].setEnabled(true);
		mImageViews[index].setEnabled(false);
		mCurSel = index;

		if (index == 0)
		{
			liaotian.setTextColor(0xff228B22);
			faxian.setTextColor(Color.BLACK);
			tongxunlu.setTextColor(Color.BLACK);
		}
		else if (index == 1)
		{
			liaotian.setTextColor(Color.BLACK);
			faxian.setTextColor(0xff228B22);
			tongxunlu.setTextColor(Color.BLACK);
		}
		else
		{
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
		setCurPoint(position);
	}

	@Override
	public void onPageScrollStateChanged(int state)
	{
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
			WebService getUser = new WebService("GetUser");
			String userName = null;
			try
			{
				userName = jsons[0].getString("name");
				SoapObject result = getUser.addProperty("name", jsons[0].getString("name")).call();
				Global.friendList.set(index, UserInfo.parse(result));
			}
			catch (NullPointerException ignored)
			{
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
			new DownloadTask("HeadImg", Global.PATH.HeadImg, userName + ".png", handler, Global.MSG_WHAT.W_REFRESH, null).execute();
			try
			{
				Global.friendList.get(index).Ex_remark = jsons[0].getString("remark");
			}
			catch (JSONException ignored)
			{
				Global.friendList.get(index).Ex_remark = null;
			}
			synchronized ((Object) updateCount)
			{
				updateCount++;
				if (updateCount >= Global.friendList.size())
				{
					updateCount = 0;
					MainActivity.handler.sendEmptyMessage(Global.MSG_WHAT.W_REFRESH);
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
			for (i = 0; i < Global.historyList.size(); i++)
			{
				if (Global.historyList.get(i).fromName.equals(h.fromName))
					break;
			}
			if (i == Global.historyList.size()) Global.historyList.add(h);
		}
		updateHistoryView();
	}

	private void updateHistoryView()
	{
		if (hisAdapter == null)
		{
			hisAdapter = new HistoryAdapter(Global.historyList, MainActivity.this);
			lvHistory.setAdapter(hisAdapter);
		}
		if (hisAdapter.getCount() == 0)
		{
			emptyNotice.setVisibility(View.VISIBLE);
			lvHistory.setVisibility(View.GONE);
		}
		else
		{
			emptyNotice.setVisibility(View.GONE);
			lvHistory.setVisibility(View.VISIBLE);
		}
		hisAdapter.notifyDataSetChanged();
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
					case Global.MSG_WHAT.W_RECEIVED_NEW_MSG:
						ArrayList<Message> received = (ArrayList<Message>) msg.obj;
						for (Message m : received)
						{
							if (!m.fromId.equals("cmd"))
							{
								History h;
								m.msgType |= Global.MSG_TYPE.T_RECEIVE_MSG;
								h = Global.map.get(m.fromId);
								if (h == null)
									h = new History(m.fromId);
								new MSGPreprocessor(m).Preprocess();
								h.historyMsg.add(m);
								h.unreadCount++;
								if ((m.msgType & Global.MSG_TYPE.T_PIC_MSG) > 0)
								{
									DownloadTask task = new DownloadTask("ChatPic", Global.PATH.ChatPic, m.text, ChatActivity.handler, Global.MSG_WHAT.W_RECEIVED_NEW_MSG, null);
									task.execute();
								}
								if (ChatActivity.handler != null && !h.fromName.equals("通知"))            //如果当前有活动的聊天界面则直接转发给ChatActivity
								{
									android.os.Message message = new android.os.Message();
									message.what = Global.MSG_WHAT.W_RECEIVED_NEW_MSG;
									message.obj = m;
									ChatActivity.handler.sendMessage(message);
								}
								else
								{
									Global.map.put(h.fromName, h);
									if (!Global.historyList.contains(h))
										Global.historyList.add(h);
								}
							}
							else            //如果这条消息是一个远程命令
							{
								CMDParser.Add(m);
							}
						}
						updateHistoryView();
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
								if (Global.friendList.size() < array.length())
									Global.friendList.add(friend);
								else
									Global.friendList.set(i, friend);
								new parserWithExtraAsync(i).execute(userInfo);
							}
						}
						catch (NullPointerException ignored)
						{
						}
						catch (ArrayIndexOutOfBoundsException ignored)
						{
						}
						catch (JSONException e)
						{
							e.printStackTrace();
						}
						break;
					case Global.MSG_WHAT.W_ERROR_NETWORK:
						Toast.makeText(MainActivity.this, Global.ERROR_HINT.HINT_ERROR_NETWORD, Toast.LENGTH_SHORT).show();
						break;
					case Global.MSG_WHAT.W_REFRESH_DEEP:
						FlushFriendsList();
					case Global.MSG_WHAT.W_REFRESH:
						if (contactAdapter != null)
							contactAdapter.notifyDataSetChanged();
						BindAdapter(Global.friendList);
						break;
				}
			}
		};
		FlushFriendsList();
		Intent I = new Intent(this, MsgPullService.class);
		startService(I);
	}

	private void init()
	{
		liaotian = (TextView) findViewById(R.id.tvLiaotian);
		faxian = (TextView) findViewById(R.id.tvFaxian);
		tongxunlu = (TextView) findViewById(R.id.tvTongxunlu);

		viewPager = (ViewPager) findViewById(R.id.viewpager);
		LayoutInflater mInflater = LayoutInflater.from(this);
		View v1 = mInflater.inflate(R.layout.page1, null);
		View v2 = mInflater.inflate(R.layout.faxian, null);
		View v3 = mInflater.inflate(R.layout.page3, null);
		lvFriends = (ListView) v3.findViewById(R.id.listView);
		lvFriends.setEmptyView(v3.findViewById(R.id.tvEmptyHint));
		lvFriends.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
			{
				Intent chat = new Intent(MainActivity.this, FriendInfoAct.class);
				String to = Global.friendList.get(i).username;
				chat.putExtra("friendName", to);
				startActivity(chat);
			}
		});
		refreshLayout1 = (SwipeRefreshLayout) v1.findViewById(R.id.swipe_container);
		refreshLayout1.setColorSchemeColors(R.color.blue, R.color.red, R.color.green, R.color.black);
		refreshLayout1.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
		{
			//Todo 刷新消息
			@Override
			public void onRefresh()
			{
				refreshLayout1.postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						refreshLayout1.setRefreshing(false);
					}
				}, 2000);
			}
		});
		refreshLayout2 = (SwipeRefreshLayout) v3.findViewById(R.id.swipe_container);
		refreshLayout2.setColorSchemeColors(R.color.blue, R.color.red, R.color.green, R.color.black);
		refreshLayout2.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
		{
			@Override
			public void onRefresh()
			{
				FlushFriendsList();
				refreshLayout2.postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						refreshLayout2.setRefreshing(false);
					}
				}, 2000);
			}
		});
		lvHistory = (ListView) v1.findViewById(R.id.listView);
		emptyNotice = (TextView) v1.findViewById(R.id.empty);
		lvHistory.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
			{
				String to = Global.historyList.get(i).fromName;
				if (!to.equals("通知"))
				{
					Intent chat = new Intent(MainActivity.this, ChatActivity.class);
					chat.putExtra("chatTo", to);
					startActivity(chat);
				}
				else
				{
					Intent notice = new Intent(MainActivity.this, NotificationAct.class);
					startActivity(notice);
				}
			}
		});
		pages = new ArrayList<View>();
		pages.add(v1);
		pages.add(v2);
		pages.add(v3);
		pagerAdapter = new myViewPagerAdapter(this, pages);
		viewPager.setAdapter(pagerAdapter);
		viewPager.setCurrentItem(0);
		viewPager.setOnPageChangeListener(this);

		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.lllayout);
		mViewCount = pages.size();
		mImageViews = new LinearLayout[mViewCount];
		for (int i = 0; i < mViewCount; i++)
		{
			mImageViews[i] = (LinearLayout) linearLayout.getChildAt(i);
			mImageViews[i].setEnabled(true);
			mImageViews[i].setOnClickListener(this);
			mImageViews[i].setTag(i);
		}
		mCurSel = 0;
		mImageViews[mCurSel].setEnabled(false);

		// Drawer:
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
		drawerContent = drawerLayout.getChildAt(1);

		SimpleAdapter simpleAdapter = new SimpleAdapter(this, getData(), R.layout.drawer_item, new String[]{"icon", "desc"}, new int[]{R.id.ivIcon, R.id.tvDesc});
		ListView l = (ListView) drawerContent.findViewById(R.id.lvDrawerMenu);
		l.setAdapter(simpleAdapter);
		l.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
			{
				Intent I;
				switch (i)
				{
					case 0:
						I = new Intent(MainActivity.this, ShakeActivity.class);
						startActivity(I);
						break;
					case 1:
						I = new Intent(MainActivity.this, NearbyStrangerAct.class);
						startActivity(I);
						break;
					case 2:
						exit();
						break;
				}
				drawerLayout.closeDrawer(drawerContent);
			}
		});
		RefreshMyInfo();
	}

	public void openDrawer(View view)
	{
		if (drawerLayout.isDrawerOpen(drawerContent))
			drawerLayout.closeDrawer(drawerContent);
		else
			drawerLayout.openDrawer(drawerContent);
	}

	private void RefreshMyInfo()
	{
		TextView tvNickname= (TextView) drawerContent.findViewById(R.id.tvNickname);
		tvNickname.setText(Global.mySelf.nickName);
		ImageView ivHeadImg= (ImageView) drawerContent.findViewById(R.id.ivHeadImg);
		if(FileUtils.Exist(Global.PATH.HeadImg+Global.mySelf.username+".png"))
			ivHeadImg.setImageBitmap(BitmapFactory.decodeFile(Global.PATH.HeadImg+Global.mySelf.username+".png"));
	}

	private List<HashMap<String, Object>> getData()
	{
		List<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("icon", R.drawable.find_more_friend_shake);
		map.put("desc", "摇一摇");
		data.add(map);
		map = new HashMap<String, Object>();
		map.put("icon", R.drawable.find_more_friend_near_icon);
		map.put("desc", "附近的人");
		data.add(map);
		map = new HashMap<String, Object>();
		map.put("icon", R.drawable.more_game);
		map.put("desc", "退出");
		data.add(map);
		return data;
	}

	void BindAdapter(ArrayList<UserInfo> list)
	{
		if (contactAdapter == null)
		{
			contactAdapter = new ContactAdapter(this, list);
			lvFriends.setAdapter(contactAdapter);
		}
		contactAdapter.notifyDataSetChanged();
		Global.map2Friend.clear();
		for (UserInfo u : list)
		{
			Global.map2Friend.put(u.username, u);
		}
		Toast.makeText(this, "已更新", Toast.LENGTH_SHORT).show();
	}

	void FlushFriendsList()
	{
		updateCount = 0;
		new WebTask(MainActivity.handler, Global.MSG_WHAT.W_GOT_FRIENDS_LIST).execute("getFriends", 1, "name", Global.mySelf.username);
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
			case R.id.meuExit:
				exit();
				break;
		}

		return super.onOptionsItemSelected(item);
	}

	private void exit()
	{
		finish();
	}

	///以下是 发现 页的几个监听，触发方式写在xml里
	public void llShack_Click(View view)
	{
		Intent I = new Intent(this, ShakeActivity.class);
		startActivity(I);
	}

	public void llNearStranger_Click(View view)
	{
		Intent I = new Intent(this, NearbyStrangerAct.class);
		startActivity(I);
	}
    //按两次退出主界面
    private long exitTime=0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
        {


            if((System.currentTimeMillis()-exitTime) > 2000)  //System.currentTimeMillis()无论何时调用，肯定大于2000
            {
                Toast.makeText(getApplicationContext(), "再按一次退出程序",Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            }
            else
            {

                finish();
                System.exit(0);
            }

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
