package org.DiscoverStranger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * 启动界面
 * */
public class SplashAct extends Activity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

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
		Intent i=new Intent(SplashAct.this,LogInAct.class);
		startActivity(i);
		finish();
	}

}
