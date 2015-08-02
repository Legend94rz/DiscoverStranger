package org.helloworld;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.helloworld.tools.Global;
import org.helloworld.tools.WebService;
import org.ksoap2.serialization.SoapObject;

import java.text.SimpleDateFormat;


public class ConfirmNewPassword extends BaseActivity
{
	private EditText etPassword;
	private EditText etConfirmPassword;
	private TextView tvErrorInfo;
	private Button btConfirm;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_confirm_new_password);
		etPassword = (EditText) findViewById(R.id.password);
		etConfirmPassword = (EditText) findViewById(R.id.confirmPassword);
		tvErrorInfo = (TextView) findViewById(R.id.error_info_password);
		btConfirm = (Button) findViewById(R.id.confirm);
		btConfirm.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(etPassword.getText().length()==0){Global.Shake(ConfirmNewPassword.this,etPassword);return;}

				if (checkPassword())
				{
					AlterBasicInfo_online task = new AlterBasicInfo_online(Global.mySelf.username, etPassword.getText().toString());
					task.execute();
				}
				else
				{
					etPassword.setText("");
					etConfirmPassword.setText("");
				}
			}
		});
	}

	public boolean checkPassword()
	{
		String ps = etPassword.getText().toString();
		String cmps = etConfirmPassword.getText().toString();
		String error_info_password = "";
		if (!ps.equals(cmps))
		{
			error_info_password += " 两次输入的密码不一致 ";
		}
		if (!((ps.length() >= 8) && (ps.length() <= 32)))
			error_info_password += " 密码长度为8~32位";
		if (error_info_password.length() > 0)
		{
			tvErrorInfo.setVisibility(View.VISIBLE);
			tvErrorInfo.setText(error_info_password);
			return false;
		}
		else
		{
			tvErrorInfo.setVisibility(View.GONE);
			tvErrorInfo.setText("");
			return  true;
		}
	}

	public class AlterBasicInfo_online extends AsyncTask<Void, Void, Byte>
	{
		public String Username;
		public String Password;


		public AlterBasicInfo_online(String username, String password)
		{
			Username = username;
			Password = password;
		}

		@Override
		protected void onPreExecute()
		{
			btConfirm.setEnabled(false);
		}

		@Override
		protected Byte doInBackground(Void... voids)
		{
			WebService updateUserInfo = new WebService("updateUserInfo");
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			String birthday2 = df.format(Global.mySelf.birthday);

			updateUserInfo.addProperty("username", Username).addProperty("password", Password).addProperty("sex", Global.mySelf.sex).addProperty("nickName", Global.mySelf.nickName).addProperty("birthday", birthday2);
			try
			{
				SoapObject result = updateUserInfo.call();
				if (result.getProperty(0).equals(false)) return 4;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return 4;
			}
			return 1;
		}

		@Override
		protected void onPostExecute(Byte aByte)
		{
			btConfirm.setEnabled(true);
			switch (aByte)
			{
				case 1:
				{
					Toast.makeText(ConfirmNewPassword.this, "修改成功", Toast.LENGTH_SHORT).show();
					Global.mySelf.password = Password;
					finish();
					break;
				}
				case 4:
				{
					Toast.makeText(ConfirmNewPassword.this, "修改失败", Toast.LENGTH_SHORT).show();
					break;
				}
			}
		}
	}
}
