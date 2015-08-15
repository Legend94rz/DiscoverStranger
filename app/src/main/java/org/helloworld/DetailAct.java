package org.helloworld;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.helloworld.tools.CustomToast;
import org.helloworld.tools.Global;
import org.helloworld.tools.Invitation;
import org.helloworld.tools.WebService;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.util.UUID;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class DetailAct extends BaseActivity
{
	String id,username;//UUID

	TextView tvTitle,tvText;
	Button btnResponse;
	EditText etResponse;
	Invitation invitation;
	class GetInvitation extends AsyncTask<Void,Void,Invitation>
	{

		@Override
		protected Invitation doInBackground(Void... voids)
		{
			WebService service=new WebService("getInvitation");
			service.addProperty("id",id);
			SoapObject soapObject = service.call();
			if(soapObject!=null)
			{
				SoapObject result= (SoapObject) soapObject.getProperty(0);
				return Invitation.parse(result);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Invitation model)
		{
			invitation=model;
			if(model!=null)
			{
				tvTitle.setText(model.title);
				tvText.setText(model.text);
			}
			else
			{
				tvText.setText("加载失败");
				tvTitle.setText("加载失败");
			}
		}
	}
	class Response extends AsyncTask<Void,Void,Boolean>
	{
		SweetAlertDialog dialog;
		String text;

		public Response(String text)
		{
			this.text = text;
		}

		@Override
		protected void onPreExecute()
		{
			btnResponse.setEnabled(false);
			dialog=new SweetAlertDialog(DetailAct.this,SweetAlertDialog.PROGRESS_TYPE);
			dialog.setTitleText("请稍候...");
			dialog.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				@Override
				public void onCancel(DialogInterface dialogInterface)
				{
					dialogInterface.dismiss();
					cancel(true);
				}
			});
			dialog.show();
		}

		@Override
		protected Boolean doInBackground(Void... voids)
		{
			WebService service = new WebService("pushMsg");
			service.addProperty("from", "通知").addProperty("to", username).addProperty("msg",text).addProperty("time", Global.formatDate(Global.getDate(), "yyyy-MM-dd HH:mm:ss")).addProperty("msgType",String.valueOf(Global.MSG_TYPE.T_TEXT_MSG));
			SoapObject soapObject = service.call();
			return soapObject != null && Boolean.parseBoolean(soapObject.getPropertyAsString(0));
		}

		@Override
		protected void onPostExecute(Boolean aBoolean)
		{
			btnResponse.setEnabled(true);
			dialog.dismiss();
			if(aBoolean)
			{
				CustomToast.show(DetailAct.this,"成功", Toast.LENGTH_SHORT);
				finish();
			}
			else
				CustomToast.show(DetailAct.this,"失败", Toast.LENGTH_SHORT);
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);
		id=getIntent().getStringExtra("id");
		username=getIntent().getStringExtra("username");
		tvTitle= (TextView) findViewById(R.id.tvTitle);
		tvText= (TextView) findViewById(R.id.tvText);
		btnResponse= (Button) findViewById(R.id.btnResponse);
		etResponse= (EditText) findViewById(R.id.etResponse);
		new GetInvitation().execute();
		invitation=null;
		btnResponse.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if(invitation==null)return;
				JSONObject jobj=new JSONObject();
				try
				{
					jobj.put("userName",Global.mySelf.username);
					if(etResponse.getText().length()>0)
						jobj.put("Text",String.format("%s(ID:%s) 回应了你标题为\"%s\"的邀请，并说：%s。",Global.mySelf.nickName,Global.mySelf.username,invitation.title,etResponse.getText().toString()));
					else
						jobj.put("Text",String.format("%s(ID:%s) 回应了你标题为\"%s\"的邀请。",Global.mySelf.nickName,Global.mySelf.username,invitation.title));
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}
				new Response(jobj.toString()).execute();
			}
		});
	}

}
