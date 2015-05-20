package org.helloworld;

import android.os.Handler;

/**
 * Created by Administrator on 2015/5/6.
 * 秒计时器。一个用途是记录录音时间
 */
public class Chronograph extends Thread implements OnTickListener
{
	public boolean Break;
	private OnTickListener listener;

	public void setListener(OnTickListener listener)
	{
		this.listener = listener;
	}

	@Override
	public void run()
	{
		while(!Break)
		{
			try
			{
				Thread.sleep(1000);
				OnTick();
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

	@Override
	public void OnTick()
	{
		listener.OnTick();
	}
}

