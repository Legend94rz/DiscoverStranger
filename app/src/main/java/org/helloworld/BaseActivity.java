package org.helloworld;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

/**
 * Created by Administrator on 2015/7/13.
 */
public class BaseActivity extends Activity
{
	public void goback(View v)
	{
		finish();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.slide_left_in,R.anim.slide_right_out);
	}

	@Override
	public void finish()
	{
		super.finish();
		overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
	}
}
