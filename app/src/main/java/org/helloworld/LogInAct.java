package org.helloworld;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.*;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;


public class LogInAct extends Activity
{
	private ProgressBar pbLogInBar;
	private Button btnLogIn;
	private EditText etName;
	private ImageView ivHeadImg;
	private Handler handler;

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
			try
			{
				SoapObject result = login.call();
                result.toString();
				return Boolean.parseBoolean(result.getProperty(0).toString());
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
				Toast.makeText(LogInAct.this,"登录成功",Toast.LENGTH_SHORT).show();
				Global.mySelf.username=Username;
				Global.mySelf.password=Password;
				Intent i=new Intent(LogInAct.this,MainActivity.class);
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

		etName.setOnFocusChangeListener(new View.OnFocusChangeListener()
		{
			@Override
			public void onFocusChange(View view, boolean b)
			{
				if (!b)
				{
					Task downloadTask = new Task(handler, Global.MSG_WHAT.W_DOWNLOADED_A_HAEDIMG);
					String fileName = etName.getText()+".png";
					downloadTask.execute("downloadFile", 2, "path", "HeadImg", "fileName", fileName);
				}
			}
		});
		handler=new Handler(new Handler.Callback()
		{
			@Override
			public boolean handleMessage(Message message)
			{
				if(message.what==Global.MSG_WHAT.W_DOWNLOADED_A_HAEDIMG)
				{
					try
					{
						SoapObject obj = (SoapObject) message.obj;
						byte[] bytes = Base64.decode(obj.getPropertyAsString(0), Base64.DEFAULT);
						Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
						ivHeadImg.setImageBitmap(bmp);
					}
					catch (NullPointerException ignored){}
				}
				return true;
			}
		});
	}

	private void initView()
	{
		btnLogIn = (Button) findViewById(R.id.btnLogin);
		pbLogInBar = (ProgressBar) findViewById(R.id.pbLoginProgress);
		etName = (EditText) findViewById(R.id.etName);
		ivHeadImg= (ImageView) findViewById(R.id.ivHeadImg);
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
