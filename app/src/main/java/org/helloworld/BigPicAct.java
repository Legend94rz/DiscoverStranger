package org.helloworld;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import org.helloworld.tools.FileUtils;
/**
 * 查看大图 界面
 * */

public class BigPicAct extends Activity
{
	ImageView ivImg;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_big_pic);
		ivImg= (ImageView) findViewById(R.id.ivImg);
		String imgsrc=getIntent().getStringExtra("imgsrc");

		if(FileUtils.Exist(imgsrc))
			ivImg.setImageBitmap(FileUtils.getOptimalBitmap(imgsrc,false));
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_big_pic, menu);
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
}
