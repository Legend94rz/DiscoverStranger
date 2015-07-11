package org.helloworld;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.helloworld.tools.Global;
import org.helloworld.tools.Message;
import org.helloworld.tools.WebService;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;

/**
 * 消息拉取服务
 * */
public class MsgPullService extends Service
{
	private boolean isError;
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
				ArrayList<Message> listOfMsg = new ArrayList<Message>();
				try
				{
					SoapObject messages = pullMsg.call();
					SoapObject result= (SoapObject) messages.getProperty(0);
					int T=result.getPropertyCount();
					if(T>0)
					{
						android.os.Message newMessageHint = new android.os.Message();
						newMessageHint.what = Global.MSG_WHAT.W_RECEIVED_NEW_MSG;
						for (int i = 0; i < T; i++)
							listOfMsg.add(Message.parse((SoapObject) result.getProperty(i)));
						newMessageHint.obj = listOfMsg;
						MainActivity.handler.sendMessage(newMessageHint);
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
						M.what= Global.MSG_WHAT.W_ERROR_NETWORK;
						if(MainActivity.handler!=null)
							MainActivity.handler.sendMessage(M);
						isError=true;
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
