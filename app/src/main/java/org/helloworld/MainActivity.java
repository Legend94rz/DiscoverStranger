package org.helloworld;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.helloworld.interfaces.OnUserInfoModifyListener;
import org.helloworld.tools.CMDParser;
import org.helloworld.tools.ContactAdapter;
import org.helloworld.tools.DownloadTask;
import org.helloworld.tools.FileUtils;
import org.helloworld.tools.Global;
import org.helloworld.tools.History;
import org.helloworld.tools.HistoryAdapter;
import org.helloworld.tools.MSGPreprocessor;
import org.helloworld.tools.Message;
import org.helloworld.tools.UserInfo;
import org.helloworld.tools.WebService;
import org.helloworld.tools.WebTask;
import org.helloworld.tools.myViewPagerAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * 主界面
 */

public class MainActivity extends BaseActivity implements View.OnClickListener, ViewPager.OnPageChangeListener, OnUserInfoModifyListener
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

	@Override
	public void OnModify(UserInfo newUser)
	{
		RefreshMyInfo();
	}

	public class parserWithExtraAsync extends AsyncTask<Void, Void, Void>
	{
		private String incompleteName;
		private JSONObject jsonUser;

		public parserWithExtraAsync(String incompleteName, JSONObject userInfo)
		{
			this.incompleteName = incompleteName;
			jsonUser = userInfo;
		}

		@Override
		protected Void doInBackground(Void... voids)
		{
			WebService getUser = new WebService("GetUser");
			String userName = null;
			UserInfo u = null;
			try
			{
				userName = jsonUser.getString("name");
				SoapObject result = getUser.addProperty("name", jsonUser.getString("name")).call();
				u = UserInfo.parse(result);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return null;
			}
			try
			{
				u.Ex_remark = jsonUser.getString("remark");
			}
			catch (JSONException ignored)
			{
				u.Ex_remark = null;
			}
			Global.map2Friend.put(incompleteName, u);
			new DownloadTask("HeadImg", Global.PATH.HeadImg, userName + ".png", Global.BLOCK_SIZE, handler, Global.MSG_WHAT.W_REFRESH, null).execute();
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
		for (History h : Global.map.values())    //ChatAct是维护的map,所以要手动保持historyList与Map的一致
		{
			int i;
			for (i = 0; i < Global.historyList.size(); i++)
			{
				if (Global.historyList.get(i).partner.equals(h.partner))
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
						if (!received.get(0).fromId.equals("cmd")) RemindUser();
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
								h.unreadMsg.add(m);
								h.lastHistoryMsg = m;
								if ((m.msgType & Global.MSG_TYPE.T_PIC_MSG) > 0)
								{
									m.sendState = 1;
									DownloadTask task = new DownloadTask("ChatPic", Global.PATH.ChatPic, m.text, Global.BLOCK_SIZE, handler, Global.MSG_WHAT.W_DOWNLOADED_A_FILE, m);
									task.execute();
								}
								if ((m.msgType & Global.MSG_TYPE.T_VOICE_MSG) > 0)
								{
									m.sendState = 1;
									String fileName = m.text.split("~")[0];
									DownloadTask t = new DownloadTask("soundMsg", Global.PATH.SoundMsg, fileName, Global.BLOCK_SIZE, handler, Global.MSG_WHAT.W_DOWNLOADED_A_FILE, m);
									t.execute();
								}
								if (ChatActivity.handler != null && !h.partner.equals("通知"))            //如果当前有活动的聊天界面则直接转发给ChatActivity
								{
									android.os.Message message = new android.os.Message();
									message.what = Global.MSG_WHAT.W_RECEIVED_NEW_MSG;
									message.obj = m;
									ChatActivity.handler.sendMessage(message);
								}
								else
								{
									Global.map.put(h.partner, h);
									if (!Global.historyList.contains(h))
										Global.historyList.add(h);
								}
							}
							else            //如果这条消息是一个远程命令
							{
								CMDParser.Execute(m);
							}
						}
						updateHistoryView();
						break;
					case Global.MSG_WHAT.W_DOWNLOADED_A_FILE:        //下载完成了一个文件(针对语音消息跟图片消息).这个实现方式很可能不好。
					{
						if (ChatActivity.handler != null)    //如果此时有活动的聊天界面
						{
							android.os.Message newMsg = new android.os.Message();
							newMsg.copyFrom(msg);            //否则会引起This message is already in use异常
							newMsg.what = Global.MSG_WHAT.W_REFRESH;
							ChatActivity.handler.sendMessage(newMsg);    //基本原样转发，提醒聊天界面刷新
						}
					}
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
								Global.map2Friend.put(userInfo.getString("name"), friend);
								new parserWithExtraAsync(friend.username, userInfo).execute();
							}
						}
						catch (NullPointerException | ArrayIndexOutOfBoundsException ignored)
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
						FlushState();
					case Global.MSG_WHAT.W_REFRESH:
						BindAdapter();
						break;
				}
			}
		};
		FlushState();
		Intent I = new Intent(this, MsgPullService.class);
		startService(I);
	}

	/**
	 * 震动提醒
	 */
	private void RemindUser()
	{
		Vibrator vibrator = (Vibrator) getApplication().getSystemService(VIBRATOR_SERVICE);
		vibrator.vibrate(500);
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
				FlushState();
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
				String to = Global.historyList.get(i).partner;
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
						exitWithConfirm();
						break;
				}
				drawerLayout.closeDrawer(drawerContent);
			}
		});
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
		TextView tvNickname = (TextView) drawerContent.findViewById(R.id.tvNickname);
		tvNickname.setText(Global.mySelf.nickName);
		ImageView ivHeadImg = (ImageView) drawerContent.findViewById(R.id.ivHeadImg);
		if (FileUtils.Exist(Global.PATH.HeadImg + Global.mySelf.username + ".png"))
			ivHeadImg.setImageBitmap(BitmapFactory.decodeFile(Global.PATH.HeadImg + Global.mySelf.username + ".png"));
		else
			new DownloadTask("HeadImg", Global.PATH.HeadImg, Global.mySelf.username + ".png", Global.BLOCK_SIZE, handler, Global.MSG_WHAT.W_REFRESH_DEEP, null).execute();
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
		map.put("icon", R.drawable.exit);
		map.put("desc", "退出");
		data.add(map);
		return data;
	}

	void BindAdapter()
	{
		Global.friendList.clear();
		for (UserInfo u : Global.map2Friend.values())
		{
			Global.friendList.add(u);
		}
		if (contactAdapter == null)
		{
			contactAdapter = new ContactAdapter(this, Global.friendList);
			lvFriends.setAdapter(contactAdapter);
		}
		contactAdapter.notifyDataSetChanged();
		//Toast.makeText(this, "已更新", Toast.LENGTH_SHORT).show();
	}

	void FlushState()
	{
		RefreshMyInfo();
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
			case R.id.meuOpenDrawer:
				openDrawer(null);
				break;
			case R.id.meuExit:
				exitWithConfirm();
				break;
		}

		return super.onOptionsItemSelected(item);
	}

	private void exit()
	{
		finish();
		Gson g = new Gson();
		File path = new File(Global.PATH.Cache);
		FileUtils.mkDir(path);
		String contactfile = Global.mySelf.username + "contact.txt";
		String historyfile = Global.mySelf.username + "history.txt";
		//把Global.friendList挨个保存
		try
		{
			BufferedWriter writer1 = new BufferedWriter(new FileWriter(new File(path, contactfile)));
			for (UserInfo u : Global.friendList)
			{
				writer1.write(g.toJson(u));
				writer1.newLine();
			}
			writer1.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		//把Global.historyList中不是系统Id的保存
		try
		{
			BufferedWriter writer2 = new BufferedWriter(new FileWriter(new File(path, historyfile)));
			for (History h : Global.historyList)
				if (h.headId == -1)
				{
					writer2.write(g.toJson(h));
					writer2.newLine();
				}
			writer2.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		android.os.Process.killProcess(android.os.Process.myPid());    //获取PID
		System.exit(0);   //常规java、c#的标准退出法，返回值为0代表正常退出
	}

	private void exitWithConfirm()
	{
		final SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
		dialog.setTitleText("真的要退出吗？");
		dialog.setCancelText("取消");
		dialog.setCancelClickListener(null);
		dialog.setConfirmText("确定");
		dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener()
		{
			@Override
			public void onClick(SweetAlertDialog sweetAlertDialog)
			{
				dialog.dismiss();
				exit();
			}
		});
		dialog.show();
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
	private long exitTime = 0;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
		{
			if ((System.currentTimeMillis() - exitTime) > 2000)  //System.currentTimeMillis()无论何时调用，肯定大于2000
			{
				Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
				exitTime = System.currentTimeMillis();
			}
			else
			{
				exit();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
