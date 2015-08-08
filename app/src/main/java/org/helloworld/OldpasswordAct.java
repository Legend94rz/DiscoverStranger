package org.helloworld;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.helloworld.tools.CustomToast;
import org.helloworld.tools.Global;


public class OldpasswordAct extends BaseActivity
{
	Button btConfirm;
	EditText etPassword;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_oldpassword);
		btConfirm = (Button) findViewById(R.id.confirm);
		etPassword = (EditText) findViewById(R.id.password);
		btConfirm.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (etPassword.getText().length()==0)
				{
					Global.Shake(OldpasswordAct.this,etPassword);
				}
				else if (!TextUtils.equals(etPassword.getText(),Global.mySelf.password))
				{
					CustomToast.show(OldpasswordAct.this, "密码错误", Toast.LENGTH_SHORT);
					etPassword.setText("");
				}
				else
				{
					Intent intent = new Intent(OldpasswordAct.this, ConfirmNewPassword.class);
					startActivity(intent);
					finish();
				}
			}
		});

	}

}
