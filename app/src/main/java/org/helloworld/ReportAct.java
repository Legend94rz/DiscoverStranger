package org.helloworld;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.helloworld.tools.CustomToast;
import org.helloworld.tools.Global;
import org.helloworld.tools.WebService;
import org.ksoap2.serialization.SoapObject;

import javax.crypto.BadPaddingException;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ReportAct extends BaseActivity
{
	Button btnOK;
	EditText etTarget;
	EditText etReason;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_report);
		btnOK= (Button) findViewById(R.id.btnOK);
		btnOK.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				new ReportTask(etReason.getText().toString(),etTarget.getText().toString()).execute();
			}
		});
		etTarget = (EditText) findViewById(R.id.etTarget);
		etReason= (EditText) findViewById(R.id.etReason);
	}
	class ReportTask extends AsyncTask<Void,Void,Boolean>
	{
		SweetAlertDialog dialog;
		String target,reason;

		public ReportTask(String reason, String target)
		{
			this.reason = reason;
			this.target = target;
		}

		@Override
		protected void onPreExecute()
		{
			btnOK.setEnabled(false);
			dialog=new SweetAlertDialog(ReportAct.this,SweetAlertDialog.PROGRESS_TYPE);
			dialog.setTitleText("请稍候...");
			dialog.show();
		}
		@Override
		protected Boolean doInBackground(Void... voids)
		{
			WebService report=new WebService("report");
			report.addProperty("sourceName", Global.mySelf.username)
				.addProperty("targetName",target)
				.addProperty("reason", reason)
				.addProperty("time",Global.formatDate(Global.getDate(),"yyyy-MM-dd HH:mm:ss"));
			SoapObject soapObject = report.call();
			if(soapObject!=null)
			{
				return Boolean.parseBoolean(soapObject.getPropertyAsString(0));
			}
			else
				return false;
		}
		@Override
		protected void onPostExecute(Boolean aBoolean)
		{
			btnOK.setEnabled(true);
			dialog.dismiss();
			if(aBoolean)
			{
				CustomToast.show(ReportAct.this,"举报成功", Toast.LENGTH_SHORT);
				finish();
			}
			else
				CustomToast.show(ReportAct.this,"发送信息失败",Toast.LENGTH_SHORT);
		}

	}
}
