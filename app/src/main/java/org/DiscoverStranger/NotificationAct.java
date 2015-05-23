package org.DiscoverStranger;

import android.app.Activity;
import android.os.*;
import android.os.Message;
import android.view.View;
import android.widget.ListView;

import org.DiscoverStranger.tools.DownloadTask;
import org.DiscoverStranger.tools.FileUtils;
import org.DiscoverStranger.tools.Global;
import org.DiscoverStranger.tools.History;
import org.DiscoverStranger.tools.NotificationAdapter;

/**
 * 查看通知界面
 * */

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
		history= Global.map.get("通知");
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
		for(org.DiscoverStranger.tools.Message m : history.historyMsg)
		{
			if(!FileUtils.Exist(Global.PATH.HeadImg + m.fromId + ".png"))
				new DownloadTask("HeadImg",Global.PATH.HeadImg,m.fromId +".png",Global.BLOCK_SIZE,handler,Global.MSG_WHAT.W_REFRESH,null);
		}
	}


	public void goback(View view)
	{
		finish();
	}
}
