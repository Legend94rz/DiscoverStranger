package org.helloworld;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;
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
				final SweetAlertDialog dialog = new SweetAlertDialog(SettingAct.this);
				dialog.setTitleText("删除缓存（包括您的本地聊天记录）");
				dialog.setConfirmText("确定");
				dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener()
				{
					@Override
					public void onClick(SweetAlertDialog sweetAlertDialog)
					{
						dialog.dismiss();
						FileUtils.deleteDirectory(Global.PATH.HeadImg);
						FileUtils.deleteDirectory(Global.PATH.Cache);
						FileUtils.deleteDirectory(Global.PATH.ChatPic);
						FileUtils.deleteDirectory(Global.PATH.SoundMsg);
						Toast.makeText(SettingAct.this, "缓存已删除", Toast.LENGTH_SHORT).show();
					}
				});
				dialog.setCancelText("取消");
				dialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener()
				{
					@Override
					public void onClick(SweetAlertDialog sweetAlertDialog)
					{
						dialog.dismiss();

					}
				});
				dialog.show();
			}
		});

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
