package org.helloworld;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import org.helloworld.tools.FileUtils;
import org.helloworld.tools.Global;
import org.helloworld.tools.WebTask;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class SettingAct extends BaseActivity
{

	LinearLayout llMyGame;
	LinearLayout llChatBackground;
	LinearLayout llDeleteCache;
	ToggleButton tbVib,tbVoi;
	Handler handler;
	@Override
	public void finish()
	{
		super.finish();
		new WebTask(null,-1).execute("updateSettings",2,"username",Global.mySelf.username,"setString",Global.settings.toJson());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		llChatBackground = (LinearLayout) findViewById(R.id.linearLayout_ChatBackground);
		llMyGame = (LinearLayout) findViewById(R.id.linearLayout_MyGame);
		tbVib = (ToggleButton) findViewById(R.id.tbVib);
		tbVoi = (ToggleButton) findViewById(R.id.tbVoi);
		handler = new Handler(new Handler.Callback()
		{
			@Override
			public boolean handleMessage(Message message)
			{
				if(message.what==Global.MSG_WHAT.W_DELETEED_FILE)
				{
					SweetAlertDialog dialog= (SweetAlertDialog) message.obj;
					dialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
					dialog.setContentText("").setTitleText("删除成功").setConfirmText("确定").setConfirmClickListener(null);
				}
				return false;
			}
		});
		llDeleteCache = (LinearLayout) findViewById(R.id.linearLayout_DeleteCache);

		llChatBackground.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(SettingAct.this, SetChatBackground.class);
				startActivity(intent);
			}
		});
		llMyGame.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(SettingAct.this, SetMyGame.class);
				startActivity(intent);
			}
		});
		llDeleteCache.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				SweetAlertDialog dialog = new SweetAlertDialog(SettingAct.this,SweetAlertDialog.WARNING_TYPE);
				dialog.setTitleText("警告").setConfirmText("确定").setContentText("删除缓存(本地图片、语音消息、游戏安装包)吗?");
				dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener()
				{
					@Override
					public void onClick(final SweetAlertDialog sweetAlertDialog)
					{
						sweetAlertDialog.changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
						sweetAlertDialog.setTitleText("正在删除...");
						sweetAlertDialog.setContentText("").showCancelButton(false);
						sweetAlertDialog.setCancelable(false);
						new Thread(new Runnable()
						{
							@Override
							public void run()
							{
								FileUtils.deleteDirectory(Global.PATH.HeadImg);
								FileUtils.deleteDirectory(Global.PATH.Cache);
								FileUtils.deleteDirectory(Global.PATH.ChatPic);
								FileUtils.deleteDirectory(Global.PATH.SoundMsg);
								FileUtils.deleteDirectory(Global.PATH.APK);
								Message message=handler.obtainMessage(Global.MSG_WHAT.W_DELETEED_FILE,sweetAlertDialog);
								handler.sendMessage(message);
							}
						}).start();
					}
				});
				dialog.setCancelText("取消");
				dialog.show();
			}
		});
		tbVoi.setChecked(Global.settings.sound);
		tbVib.setChecked(Global.settings.vibrate);
		tbVib.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				Global.settings.vibrate = isChecked;
			}
		});
		tbVoi.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b)
			{
				Global.settings.sound=b;
			}
		});


	}


}
