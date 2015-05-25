package org.helloworld;

import android.app.Activity;
import android.content.Intent;
import android.os.*;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.helloworld.tools.Global;
import org.helloworld.tools.ShakeRecord;
import org.helloworld.tools.WebTask;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;

/**
 * 摇一摇界面
 * */

public class ShakeActivity extends Activity
{
	private ShakeListener mShakeListener;
	private Vibrator mVibrator;
	private RelativeLayout mImgUp;
	private RelativeLayout mImgDn;
	public static ArrayList<ShakeRecord> records;
	public static Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shake);

		mImgUp = (RelativeLayout) findViewById(R.id.shakeImgUp);
		mImgDn = (RelativeLayout) findViewById(R.id.shakeImgDown);
		handler=new Handler(new Handler.Callback()
		{
			@Override
			public boolean handleMessage(Message message)
			{
				switch (message.what)
				{
					case Global.MSG_WHAT.W_GOT_SHAKE_RESULT:
						records = new ArrayList<ShakeRecord>();
						SoapObject T;
						try
						{
							T = ((SoapObject) ((SoapObject) message.obj).getProperty(0));
						}
						catch (NullPointerException e)
						{
							e.printStackTrace();
							return true;
						}
						catch (ClassCastException e)
						{
							e.printStackTrace();
							return true;
						}
						int count = T.getPropertyCount();
						for (int i = 0; i < count; i++)
						{
							ShakeRecord record = ShakeRecord.parse(((SoapObject) T.getProperty(i)));
							if( !Global.map2Friend.containsKey(record.username) )
								records.add(record);
						}
						if (records.size() == 0)
						{
							Toast mtoast;
							mtoast = Toast.makeText(getApplicationContext(), "抱歉，暂时没有找到在同一时刻摇一摇的人。\n再试一次吧", Toast.LENGTH_SHORT);
							mtoast.show();
						}
						else
						{
							Intent intent = new Intent(ShakeActivity.this, ShakeResultAct.class);
							startActivity(intent);
						}
						break;
				}
				return true;
			}
		});
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		mVibrator = (Vibrator)getApplication().getSystemService(VIBRATOR_SERVICE);
		mShakeListener = new ShakeListener(this);
		mShakeListener.setOnShakeListener(new ShakeListener.OnShakeListener()
		{
			public void onShake()
			{
				startAnim();  //开始 摇一摇手掌动画
				mShakeListener.stop();
				startVibrato(); //开始 震动
				new WebTask(null, -1).execute("addShake", 2, "name", Global.mySelf.username, "time", Global.formatData(Global.getDate(), "yyyy-MM-dd HH:mm:ss"));
				Toast.makeText(ShakeActivity.this, "正在搜索同一时刻摇动手机的人...", Toast.LENGTH_SHORT).show();
				new Handler().postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						mVibrator.cancel();
						mShakeListener.start();
						new WebTask(handler, Global.MSG_WHAT.W_GOT_SHAKE_RESULT).execute("getShakes", 2, "name", Global.mySelf.username, "time", Global.formatData(Global.getDate(), "yyyy-MM-dd HH:mm:ss"));
					}
				}, 3000);
			}
		});
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		mShakeListener.stop();
		mShakeListener=null;

		mVibrator.cancel();
		mVibrator=null;
	}

	public void startAnim () {   //定义摇一摇动画动画
		AnimationSet animup = new AnimationSet(true);
		TranslateAnimation mytranslateanimup0 = new TranslateAnimation(Animation.RELATIVE_TO_SELF,0f,Animation.RELATIVE_TO_SELF,0f,Animation.RELATIVE_TO_SELF,0f,Animation.RELATIVE_TO_SELF,-0.5f);
		mytranslateanimup0.setDuration(1000);
		TranslateAnimation mytranslateanimup1 = new TranslateAnimation(Animation.RELATIVE_TO_SELF,0f,Animation.RELATIVE_TO_SELF,0f,Animation.RELATIVE_TO_SELF,0f,Animation.RELATIVE_TO_SELF,+0.5f);
		mytranslateanimup1.setDuration(1000);
		mytranslateanimup1.setStartOffset(1000);
		animup.addAnimation(mytranslateanimup0);
		animup.addAnimation(mytranslateanimup1);
		mImgUp.startAnimation(animup);

		AnimationSet animdn = new AnimationSet(true);
		TranslateAnimation mytranslateanimdn0 = new TranslateAnimation(Animation.RELATIVE_TO_SELF,0f,Animation.RELATIVE_TO_SELF,0f,Animation.RELATIVE_TO_SELF,0f,Animation.RELATIVE_TO_SELF,+0.5f);
		mytranslateanimdn0.setDuration(1000);
		TranslateAnimation mytranslateanimdn1 = new TranslateAnimation(Animation.RELATIVE_TO_SELF,0f,Animation.RELATIVE_TO_SELF,0f,Animation.RELATIVE_TO_SELF,0f,Animation.RELATIVE_TO_SELF,-0.5f);
		mytranslateanimdn1.setDuration(1000);
		mytranslateanimdn1.setStartOffset(1000);
		animdn.addAnimation(mytranslateanimdn0);
		animdn.addAnimation(mytranslateanimdn1);
		mImgDn.startAnimation(animdn);
	}
	private void startVibrato(){		//定义震动
		mVibrator.vibrate( new long[]{500,200,500,200}, -1); //第一个｛｝里面是节奏数组， 第二个参数是重复次数，-1为不重复，非-1俄日从pattern的指定下标开始重复
	}

	public void goback(View v) {     //标题栏 返回按钮
		this.finish();
	}



}
