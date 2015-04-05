package org.helloworld;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.style.BulletSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;


public class MainActivity extends Activity
{
	android.os.Handler handler;
	ExpandableListView elvFriends;
	public class Task extends AsyncTask<Object,Void,SoapObject>
	{
		int when_complete;
		public Task(int when_complete)
		{
			this.when_complete = when_complete;
		}
		/**
		 * @param objects [0]为调用函数名;[1]为参数个数;[2]及以后表示参数
		 * @return true表示请求发送成功;false表示发送失败
		 * */
		@Override
		protected SoapObject doInBackground(Object... objects)
		{
			WebService service=new WebService((String)objects[0]);
			for (int i=2;i<2+(Integer)(objects[1])*2;i+=2)
			{
				service.addProperty((String)objects[i],objects[i+1]);
			}
			return service.call();
		}

		@Override
		protected void onPostExecute(SoapObject soapObject)
		{
			android.os.Message soapMsg=new android.os.Message();
			soapMsg.what=when_complete;
			soapMsg.obj=soapObject;
			handler.sendMessage(soapMsg);
		}
	}
	/**
	 * 不断从服务器拉取新消息
	 * */
	 public class MsgPuller implements Runnable
	{
		@Override
		public void run()
		{
			WebService pullMsg = new WebService("pullMsg");
			pullMsg.addProperty("name", Global.mySelf.username);
			while (true)
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
				try
				{
					Thread.sleep(5000);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
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
		elvFriends= (ExpandableListView) findViewById(R.id.elvFriends);
		handler = new android.os.Handler()
		{
			@Override
			public void handleMessage(android.os.Message msg)
			{
				if(msg.what==Global.MSG_WHAT.RECEIVED_A_NEW_MSG)
				{
					Toast.makeText(MainActivity.this, msg.getData().getString("content"), Toast.LENGTH_SHORT).show();
				}
				else if(msg.what==Global.MSG_WHAT.GOT_FRIENDS_LIST)
				{
					SoapObject obj= (SoapObject) (msg.obj);
					try
					{
						final JSONObject json = new JSONObject(obj.getProperty(0).toString());
						final JSONArray array=json.getJSONArray("List");
						ExpandableListAdapter adp = new BaseExpandableListAdapter()
						{
							@Override
							public int getGroupCount()
							{
								return array.length();
							}
							@Override
							public int getChildrenCount(int i)
							{
								try
								{
									JSONObject temp = array.getJSONObject(i);
									String groupname = temp.keys().next().toString();
									JSONObject group=temp.getJSONObject(groupname);
									return group.getJSONArray("friends").length();
								}
								catch (JSONException e)
								{
									e.printStackTrace();
								}
								return 0;
							}
							@Override
							public Object getGroup(int i)
							{
								try
								{
									return array.getJSONObject(i).keys().next().toString();
								}
								catch (JSONException e)
								{
									e.printStackTrace();
								}
								return null;
							}
							@Override
							public Object getChild(int i, int i2)
							{
								try
								{
									JSONObject temp = array.getJSONObject(i);
									String groupname = temp.keys().next().toString();
									JSONObject group=temp.getJSONObject(groupname);
									JSONObject friend=group.getJSONArray("friends").getJSONObject(i2);
									return friend.getString("name");
								}
								catch (JSONException e)
								{
									e.printStackTrace();
								}
								return "";
							}
							@Override
							public long getGroupId(int i)
							{
								return i;
							}
							@Override
							public long getChildId(int i, int i2)
							{
								return i2;
							}
							@Override
							public boolean hasStableIds()
							{
								return true;
							}
							@Override
							public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup)
							{
								LinearLayout ll=new LinearLayout(MainActivity.this);
								TextView tv=new TextView(MainActivity.this);
								tv.setText((String)getGroup(i));
								ll.addView(tv);
								return ll;
							}
							@Override
							public View getChildView(int i, int i2, boolean b, View view, ViewGroup viewGroup)
							{
								LinearLayout ll=new LinearLayout(MainActivity.this);
								TextView tv=new TextView(MainActivity.this);
								tv.setText((String)getChild(i,i2));
								ll.addView(tv);
								return ll;
							}
							@Override
							public boolean isChildSelectable(int i, int i2)
							{
								return true;
							}
						};
						elvFriends.setAdapter(adp);
					}
					catch (JSONException e)
					{
						e.printStackTrace();
					}
				}
			}
		};
		new Task(Global.MSG_WHAT.GOT_FRIENDS_LIST).execute("getFriends",1,"name",Global.mySelf.username);
		new Thread(new MsgPuller()).start();
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
		if (id == R.id.action_settings)
		{
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
