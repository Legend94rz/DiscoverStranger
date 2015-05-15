package org.helloworld;

import android.os.Handler;

/**
 * Created by Administrator on 2015/5/6.
 * 秒计时器。一个用途是记录录音时间
 */
public class Chronograph extends Thread
{
	private android.os.Handler handler;
	public boolean Break;
	public Chronograph(Handler handler)
	{
		this.handler = handler;
		Break=false;
	}

	@Override
	public void run()
	{
		while(!Break)
		{
			try
			{
				Thread.sleep(1000);
				handler.sendEmptyMessage(Global.MSG_WHAT.W_SECOND_GO_BY);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			catch (NullPointerException e1)
			{
				e1.printStackTrace();
			}
		}
	}
}

