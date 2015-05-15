package org.helloworld;

import android.app.Activity;
import android.os.*;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;


public class NotificationAct extends Activity
{
	private ListView lvNotification;
	private NotificationAdapter adapter;
	private History history;
	public static Handler handler;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notification);
		history=Global.map.get("通知");
		history.unreadCount=0;
		initView();
		handler=new Handler(new Handler.Callback()
		{
			@Override
			public boolean handleMessage(Message message)
			{
				switch (message.what)
				{
					case Global.MSG_WHAT.W_REFRESH:
						if(adapter!=null)
							adapter.notifyDataSetChanged();
						break;
				}
				return true;
			}
		});
	}

	private void initView()
	{
		lvNotification= (ListView) findViewById(R.id.lvNotification);
		adapter=new NotificationAdapter(this,history.historyMsg);
		lvNotification.setAdapter(adapter);
		for(org.helloworld.Message m : history.historyMsg)
		{
			if(!FileUtils.Exist(Global.PATH.HeadImg + m.fromId +".png"))
				new DownloadTask("HeadImg",Global.PATH.HeadImg,m.fromId +".png",handler,Global.MSG_WHAT.W_REFRESH,null);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_notification, menu);
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

	public void goback(View view)
	{
		finish();
	}
}
