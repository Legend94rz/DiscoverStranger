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


public class MainActivity_old extends Activity
{

	EditText etMsg;
	EditText etTo;
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
	}
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_old);
		etMsg=(EditText)findViewById(R.id.etMsg);
		etTo= (EditText) findViewById(R.id.etTo);
		Button btnSend=(Button)findViewById(R.id.btnSend);
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
