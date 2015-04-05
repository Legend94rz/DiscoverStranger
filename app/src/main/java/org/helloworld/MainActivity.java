package org.helloworld;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.ksoap2.serialization.SoapObject;


public class MainActivity extends Activity
{
	android.os.Handler handler;
	EditText etMsg;
	EditText etTo;
	public class MsgPuller implements Runnable
	{
		@Override
		public void run()
		{
			while (true)
			{
				WebService pullMsg = new WebService("pullMsg");
				pullMsg.addProperty("name", Global.mySelf.username);
				SoapObject messages = pullMsg.call();
				SoapObject result= (SoapObject) messages.getProperty(0);
				int T=result.getPropertyCount();
				for (int i = 0; i < T; i++)
				{
					org.helloworld.Message msg = org.helloworld.Message.parse((SoapObject)result.getProperty(i));
					Message newMessageHint=new Message();
					Bundle data=new Bundle();
					data.putString("content",msg.Text);
					newMessageHint.what=2;
					newMessageHint.setData(data);
					handler.sendMessage(newMessageHint);
				}
				try
				{
					Thread.sleep(5000);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	public class SendTask extends AsyncTask<Void,Void,Boolean>
	{
		public String ToId;
		public String MSG;

		public SendTask(String toId, String MSG)
		{
			ToId = toId;
			this.MSG = MSG;
		}

		@Override
		protected Boolean doInBackground(Void... voids)
		{
			WebService sendMsg=new WebService("pushMsg");
			sendMsg.addProperty("from",Global.mySelf.username).addProperty("to",ToId).addProperty("msg",MSG);
			SoapObject result=sendMsg.call();
			return Boolean.parseBoolean(result.getProperty(0).toString());
		}

		@Override
		protected void onPostExecute(Boolean aBoolean)
		{
			Toast.makeText(MainActivity.this,aBoolean.toString(),Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		handler = new android.os.Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				if(msg.what==2)
				{
					Toast.makeText(MainActivity.this,msg.getData().getString("content"),Toast.LENGTH_SHORT).show();
				}
			}
		};
		new Thread(new MsgPuller()).start();
		Button btnSend=(Button)findViewById(R.id.btnSend);
		etMsg=(EditText)findViewById(R.id.etMsg);
		etTo= (EditText) findViewById(R.id.etTo);
		btnSend.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				SendTask sendTask=new SendTask(etTo.getText().toString(),etMsg.getText().toString());
				sendTask.execute();
			}
		});
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
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
