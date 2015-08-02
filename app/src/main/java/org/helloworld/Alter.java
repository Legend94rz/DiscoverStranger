package org.helloworld;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;


public class Alter extends BaseActivity
{

	private LinearLayout llAlterPassword;
	private LinearLayout llAlterBasicInfo;
	private LinearLayout llAlterAvatar;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alter);
		llAlterBasicInfo = (LinearLayout) findViewById(R.id.linearLayout_AlterBasicInfo);
		llAlterPassword = (LinearLayout) findViewById(R.id.linearLayout_AlterPassword);
		llAlterAvatar = (LinearLayout) findViewById(R.id.linearLayout_AlterAvatar);
		llAlterPassword.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(Alter.this, OldpasswordAct.class);
				startActivity(intent);
			}
		});
		llAlterBasicInfo.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(Alter.this, AlterBasicInfo.class);
				startActivity(intent);
			}
		});
		llAlterAvatar.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(Alter.this, AlterAvatarAct.class);
				startActivity(intent);
			}
		});


	}

}
