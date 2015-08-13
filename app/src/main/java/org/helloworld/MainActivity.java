package org.helloworld;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.helloworld.interfaces.OnUserInfoModifyListener;
import org.helloworld.tools.CMDParser;
import org.helloworld.tools.ContactAdapter;
import org.helloworld.tools.CustomToast;
import org.helloworld.tools.DownloadTask;
import org.helloworld.tools.FileUtils;
import org.helloworld.tools.Global;
import org.helloworld.tools.History;
import org.helloworld.tools.HistoryAdapter;
import org.helloworld.tools.Message;
import org.helloworld.tools.Settings;
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
	SoundPool soundPool;
	boolean isLoaded = false;
	int soundId;

	LayoutInflater inflater;
	View popView;
	PopupWindow popupWindow;
	int longClickIndex;


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
		private UserInfo incompleteUser;
		private JSONObject jsonUser;

		public parserWithExtraAsync(UserInfo incompleteUser, JSONObject userInfo)
		{
			this.incompleteUser = incompleteUser;
			jsonUser = userInfo;
		}

		@Override
		protected Void doInBackground(Void... voids)
		{
			WebService getUser = new WebService("GetUser");
			try
			{
				SoapObject result = getUser.addProperty("name", jsonUser.getString("name")).call();
				UserInfo.parse(result,incompleteUser);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return null;
			}
			try
			{
				incompleteUser.Ex_remark = jsonUser.getString("remark");
			}
			catch (JSONException ignored)
			{
				incompleteUser.Ex_remark = null;
			}
			MainActivity.handler.sendEmptyMessage(Global.MSG_WHAT.W_REFRESH);
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid)
		{

		}
	}

	@Override
	/**
	 * 这个activity再次回到前台时，历史消息很可能发生变化，要更新
	 * */
	protected void onResume()
	{
		super.onResume();
		hisAdapter.notifyDataSetChanged();
	}


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
		Global.mySelf.SetOnModifyListener(this);

		handler = new android.os.Handler()
		{
			@Override
			public void handleMessage(android.os.Message msg)
			{
				switch (msg.what)
				{
					case Global.MSG_WHAT.W_RECEIVED_NEW_CMD:
					{
						ArrayList<Message> cmds = ((ArrayList<Message>) msg.obj);
						for (Message m : cmds)
							CMDParser.getInstant().AddCMD(m);
					}
					break;
					case Global.MSG_WHAT.W_RECEIVED_NEW_MSG:
						ArrayList<Message> received = (ArrayList<Message>) msg.obj;
						if (Global.settings.vibrate) RemindUser();
						if (Global.settings.sound) Beep();
						for (Message m : received)
						{
							History h;
							h = Global.map.get(m.fromId);
							if (h == null)
								h = new History(m.fromId);
							if(h.partner.equals("通知"))
							{
								try
								{
									JSONObject j = new JSONObject(m.text);
									m.fromId = j.getString("userName");
									m.text = j.getString("Text");
								}
								catch (JSONException e)
								{
									e.printStackTrace();
								}
							}
							h.unreadMsg.add(m);
							h.lastHistoryMsg = m;
							Global.map.put(h.partner, h);
							if (!Global.historyList.contains(h))
								Global.historyList.add(h);
						}
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
								Global.map2Friend.put(friend.username, friend);
								Global.friendList.add(friend);
								new parserWithExtraAsync(friend, userInfo).execute();
							}
						}
						catch (NullPointerException | ArrayIndexOutOfBoundsException ignored)
						{
						}
						catch (JSONException e)
						{
							e.printStackTrace();
						}
						synchronized (Global.refreshing)
						{
							Global.refreshing[0] = false;
							Global.refreshing.notify();
						}
						ArrayList<History> toRemove=new ArrayList<>();
						for(History h:Global.historyList)
						{
							if(!Global.map2Friend.containsKey(h.partner) && !h.isSystem())
							{
								toRemove.add(h);
								Global.map.remove(h.partner);
							}
						}
						Global.historyList.removeAll(toRemove);
						hisAdapter.notifyDataSetChanged();
						break;
					case Global.MSG_WHAT.W_ERROR_NETWORK:
						CustomToast.show(MainActivity.this, Global.ERROR_HINT.HINT_ERROR_NETWORD, Toast.LENGTH_SHORT);
						break;
					case Global.MSG_WHAT.W_REFRESH_DEEP:
						FlushState();
					case Global.MSG_WHAT.W_REFRESH:
						contactAdapter.notifyDataSetChanged();
						hisAdapter.notifyDataSetChanged();
						break;
					case Global.MSG_WHAT.W_GOT_USER_SETTING:
						SoapObject result = (SoapObject) msg.obj;
						if(result!=null)
							if (result.getPropertyCount() > 0)
								Global.settings = Settings.parse((SoapObject) result.getProperty(0));
						break;
					case Global.MSG_WHAT.W_DATA_CHANGED:
						hisAdapter.notifyDataSetChanged();
						contactAdapter.notifyDataSetChanged();
						RefreshMyInfo();
						break;
				}
			}
		};
		FlushState();
		Intent I = new Intent(this, MsgPullService.class);
		MsgPullService.handlers.add(handler);
		startService(I);
		//更新用户配置
		new WebTask(handler, Global.MSG_WHAT.W_GOT_USER_SETTING).execute("getUserSetting", 1, "username", Global.mySelf.username);
		soundPool = new SoundPool(3, AudioManager.STREAM_ALARM, 0);
		soundId = soundPool.load(this, R.raw.msg, 1);
		soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener()
		{
			@Override
			public void onLoadComplete(SoundPool soundPool, int i, int i1)
			{
				isLoaded = true;
			}
		});
	}

	/**
	 * 声音提醒
	 */
	private void Beep()
	{
		if (isLoaded)
			soundPool.play(soundId, 1, 1, 0, 0, 1);
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
			@Override
			public void onRefresh()
			{
				hisAdapter.notifyDataSetChanged();
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
		lvHistory.setEmptyView(v1.findViewById(R.id.empty));
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

		contactAdapter = new ContactAdapter(this, Global.friendList,lvFriends);
		lvFriends.setAdapter(contactAdapter);
		hisAdapter = new HistoryAdapter(Global.historyList, MainActivity.this,lvHistory);
		lvHistory.setAdapter(hisAdapter);

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
					case 0:	//编辑资料
						I = new Intent(MainActivity.this, Alter.class);
						startActivity(I);
						break;
					case 1:
						I = new Intent(MainActivity.this, SettingAct.class);
						startActivity(I);
						break;
					case 2:
						exitWithConfirm();
						break;
				}
				drawerLayout.closeDrawer(drawerContent);
			}
		});
		//Popup window
		inflater=LayoutInflater.from(this);
		popView = inflater.inflate(R.layout.popup_window, null);
		popupWindow = new PopupWindow(popView, ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
		popupWindow.setFocusable(true);
		popupWindow.setOutsideTouchable(true);
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		lvHistory.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
		{
			@Override
			public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l)
			{
				longClickIndex=i;
				int[] a = new int[2];
				view.getLocationOnScreen(a);
				popupWindow.showAtLocation(lvHistory, Gravity.BOTTOM|Gravity.CENTER,0,Global.screenHeight-a[1]);
				return false;
			}
		});
		popView.findViewById(R.id.btnDel).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Global.historyList.remove(longClickIndex);
				hisAdapter.notifyDataSetChanged();
				popupWindow.dismiss();
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
		TextView tvNickname = (TextView) drawerLayout.findViewById(R.id.tvNickname);
		tvNickname.setText(Global.mySelf.nickName);
		CircleImageView ivHeadImg = (CircleImageView) drawerLayout.findViewById(R.id.ivHeadImg);
		if (FileUtils.Exist(Global.PATH.HeadImg + Global.mySelf.username + ".png"))
			ivHeadImg.setImageBitmap(BitmapFactory.decodeFile(Global.PATH.HeadImg + Global.mySelf.username + ".png"));
		else
			new DownloadTask("HeadImg", Global.PATH.HeadImg, Global.mySelf.username + ".png", Global.BLOCK_SIZE, handler, Global.MSG_WHAT.W_REFRESH_DEEP, null).execute();
	}

	private List<HashMap<String, Object>> getData()
	{
		List<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("icon", R.drawable.icon_userinfo);
		map.put("desc", "编辑个人资料");
		data.add(map);
		map = new HashMap<String, Object>();
		map.put("icon", R.drawable.settings);
		map.put("desc", "设置");
		data.add(map);
		map = new HashMap<String, Object>();
		map.put("icon", R.drawable.exit);
		map.put("desc", "退出");
		data.add(map);
		return data;
	}



	void FlushState()
	{
		synchronized (Global.refreshing)
		{
			Global.refreshing[0] = true;
		}
		RefreshMyInfo();
		Global.friendList.clear();
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
		MsgPullService.handlers.remove(handler);
		Gson g = new Gson();
		File path = new File(Global.PATH.Cache);
		FileUtils.mkDir(path);
		//String contactfile = Global.mySelf.username + "contact.txt";
		String historyfile = Global.mySelf.username + "history.txt";
		//把Global.friendList挨个保存
		//try
		//{
		//	BufferedWriter writer1 = new BufferedWriter(new FileWriter(new File(path, contactfile)));
		//	for (UserInfo u : Global.friendList)
		//	{
		//		writer1.write(g.toJson(u));
		//		writer1.newLine();
		//	}
		//	writer1.close();
		//}
		//catch (IOException e)
		//{
		//	e.printStackTrace();
		//}
		//把Global.historyList中不是系统Id的保存
		try
		{
			BufferedWriter writer2 = new BufferedWriter(new FileWriter(new File(path, historyfile)));
			ExclusionStrategy strategy = new ExclusionStrategy()
			{
				@Override
				public boolean shouldSkipField(FieldAttributes fieldAttributes)
				{
					return fieldAttributes.getName().equals("extra");
				}

				@Override
				public boolean shouldSkipClass(Class<?> aClass)
				{
					return false;
				}
			};
			Gson gson= new GsonBuilder().setExclusionStrategies(strategy).create();
			for (History h : Global.historyList)
				if (h.headId == -1)
				{
					writer2.write(gson.toJson(h));
					writer2.newLine();
				}
			writer2.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		//保存配置信息
		File settingFolder = new File(Global.PATH.Setting);
		FileUtils.mkDir(settingFolder);
		try
		{
			BufferedWriter writer3 = new BufferedWriter(new FileWriter(new File(settingFolder, Global.mySelf.username + "_settings.txt")));
			writer3.write(g.toJson(Global.settings));
			writer3.newLine();
			writer3.close();
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

	public void llMoments_Click(View view)
	{
		Intent I = new Intent(this, MomentsActivity.class);
		startActivity(I);
	}

	public void llNearStranger_Click(View view)
	{
		Intent I = new Intent(this, NearbyStrangerAct.class);
		startActivity(I);
	}

	//public void llSettings_Click(View view)
	//{
	//	Intent I = new Intent(this, SettingAct.class);
	//	startActivity(I);
	//}

	//按两次退出主界面
	private long exitTime = 0;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
		{
			if ((System.currentTimeMillis() - exitTime) > 2000)  //System.currentTimeMillis()无论何时调用，肯定大于2000
			{
				CustomToast.show(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT);
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
