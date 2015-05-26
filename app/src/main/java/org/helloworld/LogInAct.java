package org.helloworld;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.helloworld.tools.Global;
import org.helloworld.tools.UserInfo;
import org.helloworld.tools.WebService;
import org.ksoap2.serialization.SoapObject;

import java.io.File;

/**
 * 登录界面
 * */

public class LogInAct extends Activity
{
	private ProgressBar pbLogInBar;
	private Button btnLogIn;
	private EditText etName;
	private ImageView ivHeadImg;

	public class SignInTask extends AsyncTask<Void, Void, Boolean>
	{
		public String Username;
		public String Password;

		public SignInTask(String username, String password)
		{
			Username = username;
			Password = password;
		}

		@Override
		protected Boolean doInBackground(Void... voids)
		{
			WebService login = new WebService("SignIn");
			login.addProperty("name", Username).addProperty("pass", Password);
			try
			{
				SoapObject result = login.call();
				if(Boolean.parseBoolean(result.getProperty(0).toString()))
				{
					WebService getUser = new WebService("GetUser");
					result = getUser.addProperty("name", Username).call();
					Global.mySelf = UserInfo.parse(result);
					return true;
				}
				else
					return false;
			}
			catch (NullPointerException e)
			{
				e.printStackTrace();
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean aBoolean)
		{
			pbLogInBar.setVisibility(View.INVISIBLE);
			btnLogIn.setEnabled(true);
			if(aBoolean)
			{
				Toast.makeText(LogInAct.this, "登录成功", Toast.LENGTH_SHORT).show();
				Intent i = new Intent(LogInAct.this, MainActivity.class);
				startActivity(i);
				finish();
			}
			else
			{
				Toast.makeText(LogInAct.this,"登录失败",Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log_in);
		initView();
		btnLogIn.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				String username = (etName).getText().toString();
				String password = ((TextView) findViewById(R.id.etPassword)).getText().toString();
				SignInTask task = new SignInTask(username, password);
				pbLogInBar.setVisibility(View.VISIBLE);
				btnLogIn.setEnabled(false);
				task.execute();
			}
		});

        //Jump to Register Activity
        TextView toRegister=(TextView)findViewById(R.id.tvToRegister);
        toRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(LogInAct.this,RegisterAct.class);
                startActivity(intent);
            }
        });

		etName.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)	{}
			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2){}
			@Override
			public void afterTextChanged(Editable editable)
			{
				File f = new File(Global.PATH.HeadImg, editable + ".png");
				if (f.exists())
				{
					ivHeadImg.setImageBitmap(BitmapFactory.decodeFile(Global.PATH.HeadImg + editable + ".png"));
				}
				else
				{
					ivHeadImg.setImageResource(R.drawable.nohead);
				}
			}
		});
	}

	private void initView()
	{
		btnLogIn = (Button) findViewById(R.id.btnLogin);
		pbLogInBar = (ProgressBar) findViewById(R.id.pbLoginProgress);
		etName = (EditText) findViewById(R.id.etName);
		ivHeadImg = (ImageView) findViewById(R.id.ivHeadImg);
	}

}