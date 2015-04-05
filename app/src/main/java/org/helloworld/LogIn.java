package org.helloworld;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.ksoap2.serialization.SoapObject;


public class LogIn extends Activity
{
	private ProgressBar pbLogInBar;
	public class SignInTask extends AsyncTask<Void, Void, Boolean>
	{
		public String Username;
		public String Password;

		public SignInTask(String username, String password)
		{
			Username=username;
			Password=password;
		}
		@Override
		protected Boolean doInBackground(Void... voids)
		{
			WebService login=new WebService("SignIn");
			login.addProperty("name",Username).addProperty("pass",Password);
			SoapObject result=login.call();
			return Boolean.parseBoolean(result.getProperty(0).toString());
		}
		@Override
		protected void onPostExecute(Boolean aBoolean)
		{
			pbLogInBar.setVisibility(View.INVISIBLE);
			if(aBoolean)
			{
				Toast.makeText(LogIn.this,"登录成功",Toast.LENGTH_SHORT).show();
				Global.mySelf.username=Username;
				Global.mySelf.password=Password;
				Intent i=new Intent(LogIn.this,MainActivity.class);
				startActivity(i);
				finish();
			}
			else
			{
				Toast.makeText(LogIn.this,"登录失败",Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log_in);
		Button btnLogIn = (Button) findViewById(R.id.btnLogin);
		pbLogInBar = (ProgressBar) findViewById(R.id.pbLoginProgress);
		btnLogIn.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				String username = ((TextView) findViewById(R.id.etName)).getText().toString();
				String password = ((TextView) findViewById(R.id.etPassword)).getText().toString();
				SignInTask task = new SignInTask(username, password);
				pbLogInBar.setVisibility(View.VISIBLE);
				task.execute();
			}
		});

	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_log_in, menu);
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
