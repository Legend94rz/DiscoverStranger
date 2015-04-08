package org.helloworld;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Objects;
import java.util.jar.Manifest;


public class MainActivity extends Activity
{
	public static android.os.Handler handler;
	private ListView elvFriends;
	public static int updateCount=0;
	static BaseAdapter adapter = null;

	public class parserWithExtraAsync extends AsyncTask<JSONObject, Void, Void>
	{
		private ArrayList<UserInfo> list;
		private int index;

		public parserWithExtraAsync(ArrayList<UserInfo> list, int index)
		{
			this.list = list;
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
					handler.sendMessage(message);
				}
			}
			return null;
		}
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

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		TabHost tabHost=(TabHost)findViewById(R.id.tabHost);
		tabHost.setup();

		TabHost.TabSpec tabSpec=tabHost.newTabSpec("tab1").setIndicator("最近").setContent(R.id.tab1);
		TabHost.TabSpec tabSpec1=tabHost.newTabSpec("tab2").setIndicator("所有").setContent(R.id.tab2);
		tabHost.addTab(tabSpec);
		tabHost.addTab(tabSpec1);
		elvFriends= (ListView) findViewById(R.id.elvFriends);
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
						final ArrayList<UserInfo> list = new ArrayList<UserInfo>();
						try
						{
							SoapObject obj = (SoapObject) (msg.obj);
							JSONObject json = new JSONObject(obj.getProperty(0).toString());
							JSONArray array = json.getJSONArray("friends");
							for (int i = 0; i < array.length(); i++)
							{
								JSONObject userInfo = array.getJSONObject(i);
								UserInfo friend = new UserInfo(userInfo.getString("name"));
								list.add(friend);
								new parserWithExtraAsync(list, i).execute(userInfo);
							}
							adapter = new BaseAdapter()
							{
								@Override
								public int getCount()
								{
									return list.size();
								}

								@Override
								public Object getItem(int i)
								{
									return list.get(i);
								}

								@Override
								public long getItemId(int i)
								{
									return i;
								}

								@Override
								public View getView(int i, View view, ViewGroup viewGroup)
								{
									LinearLayout l1 = new LinearLayout(MainActivity.this);
									LinearLayout l2=new LinearLayout(MainActivity.this);
									l1.setOrientation(LinearLayout.HORIZONTAL);
									l2.setOrientation(LinearLayout.VERTICAL);
									ImageView img = new ImageView(MainActivity.this);
									LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(64, 64);
									lp.setMargins(25, 15, 15, 15);
									img.setLayoutParams(lp);
									img.setScaleType(ImageView.ScaleType.FIT_XY);
									img.setImageResource(R.drawable.nohead);
									l1.addView(img);
									TextView tv = new TextView(MainActivity.this);
									tv.setText(list.get(i).username);
									tv.setTextSize(20);
									l2.addView(tv);
									TextView tv2=new TextView(MainActivity.this);
									tv2.setText("备注："+list.get(i).Ex_remark);
									l2.addView(tv2);
									l1.addView(l2);
									return l1;
								}
							};
							adapter.notifyDataSetChanged();
							elvFriends.setAdapter(adapter);
						}
						catch (JSONException e)
						{
							e.printStackTrace();
						}
						catch (NullPointerException ignored){}
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
	void FlushFriendsList()
	{
		updateCount=0;
		new Task(Global.MSG_WHAT.W_GOT_FRIENDS_LIST).execute("getFriends",1,"name",Global.mySelf.username);
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
