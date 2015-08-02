package org.helloworld;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ListView;

import org.helloworld.tools.DownloadTask;
import org.helloworld.tools.FileUtils;
import org.helloworld.tools.Global;
import org.helloworld.tools.History;
import org.helloworld.tools.NotificationAdapter;

import java.util.ArrayList;

/**
 * 查看通知界面
 */

public class NotificationAct extends BaseActivity
{
	private ListView lvNotification;
	private NotificationAdapter adapter;
	private ArrayList<org.helloworld.tools.Message> messages;
	private History history;
	public static Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notification);
		history = Global.map.get("通知");
		messages = new ArrayList<>(history.unreadMsg);
		history.unreadMsg.clear();
		initView();
		handler = new Handler(new Handler.Callback()
		{
			@Override
			public boolean handleMessage(Message message)
			{
				switch (message.what)
				{
					case Global.MSG_WHAT.W_REFRESH:
						if (adapter != null)
							adapter.notifyDataSetChanged();
						break;
				}
				return true;
			}
		});
	}

	private void initView()
	{
		lvNotification = (ListView) findViewById(R.id.lvNotification);
		adapter = new NotificationAdapter(this, messages);
		lvNotification.setAdapter(adapter);
		for (org.helloworld.tools.Message m : messages)
		{
			if (!FileUtils.Exist(Global.PATH.HeadImg + m.fromId + ".png"))
				new DownloadTask("HeadImg", Global.PATH.HeadImg, m.fromId + ".png", Global.BLOCK_SIZE, handler, Global.MSG_WHAT.W_REFRESH, null);
		}
	}

}
