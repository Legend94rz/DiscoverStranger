package org.helloworld;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import org.helloworld.tools.Global;
import org.helloworld.tools.Message;
import org.helloworld.tools.WebService;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;

/**
 * 消息拉取服务
 */
public class MsgPullService extends Service
{
	public static ArrayList<Handler> handlers = new ArrayList<>();

	public class MsgPuller implements Runnable
	{
		boolean isError;

		@Override
		public void run()
		{
			WebService pullMsg = new WebService("pullMsg");
			if (Global.mySelf == null) return;
			pullMsg.addProperty("name", Global.mySelf.username);
			while (true)
			{
				ArrayList<Message> listOfMsg = new ArrayList<>();
				ArrayList<Message> listOfCmd = new ArrayList<>();
				try
				{
					SoapObject messages = pullMsg.call();
					SoapObject result = (SoapObject) messages.getProperty(0);
					int T = result.getPropertyCount();
					for (int i = 0; i < T; i++)
					{
						Message m = Message.parse((SoapObject) result.getProperty(i));
						if (m.fromId.equals("$cmd"))
						{
							listOfCmd.add(m);
						}
						else
						{
							m.msgType |= Global.MSG_TYPE.T_RECEIVE_MSG;
							listOfMsg.add(m);
						}
					}
					if (listOfMsg.size() > 0)
					{
						for (Handler handler : handlers)
						{
							android.os.Message newMessageHint = new android.os.Message();
							newMessageHint.what = Global.MSG_WHAT.W_RECEIVED_NEW_MSG;
							newMessageHint.obj = listOfMsg;
							handler.sendMessage(newMessageHint);
						}
					}
					if (listOfCmd.size() > 0)
					{
						for (Handler handler : handlers)
						{
							android.os.Message newMessageHint = new android.os.Message();
							newMessageHint.what = Global.MSG_WHAT.W_RECEIVED_NEW_CMD;
							newMessageHint.obj = listOfCmd;
							handler.sendMessage(newMessageHint);
						}
					}
					Thread.sleep(5000);
					isError = false;
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				catch (NullPointerException e)
				{
					e.printStackTrace();
					if (!isError)
					{
						if (MainActivity.handler != null)
							MainActivity.handler.sendEmptyMessage(Global.MSG_WHAT.W_ERROR_NETWORK);
						isError = true;
					}
				}

			}
		}
	}

	public MsgPullService()
	{
	}

	@Override
	public boolean onUnbind(Intent intent)
	{
		return super.onUnbind(intent);
	}

	@Override
	public void onRebind(Intent intent)
	{
		super.onRebind(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		new Thread(new MsgPuller()).start();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
