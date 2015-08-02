package org.helloworld;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import org.helloworld.tools.FaceConversionUtil;
import org.helloworld.tools.Global;

/**
 * 启动界面
 */
public class SplashAct extends BaseActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		Global.DPI = (int) getApplicationContext().getResources().getDisplayMetrics().density;
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				FaceConversionUtil.getInstace().getFileText(getApplication());
			}
		}).start();
		new Handler().postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				redirectTo();
			}
		}, 1500);
	}

	private void redirectTo()
	{
		//Todo 为实现自动登录功能，在这里判断进入哪个Activity
		//if auto-login then enter MainAct; else:
		Intent i = new Intent(SplashAct.this, LogInAct.class);
		startActivity(i);
		//Intent I = new Intent(this, MsgPullService.class);
		//startService(I);
		finish();
	}

}
