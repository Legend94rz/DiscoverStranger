package org.helloworld;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import org.helloworld.tools.CustomToast;
import org.helloworld.tools.Global;
import org.helloworld.tools.UserInfo;
import org.helloworld.tools.WebService;
import org.ksoap2.serialization.SoapObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class AlterBasicInfo extends BaseActivity
{

	private Button btEnsure;
	private EditText etNickname;
	private DatePicker dpBirthday;
	private RadioButton rbFemale;
	private RadioButton rbMale;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alter_basic_info);

		btEnsure = (Button) findViewById(R.id.confirm);
		etNickname = (EditText) findViewById(R.id.nickname);
		dpBirthday = (DatePicker) findViewById(R.id.datePicker);
		dpBirthday.updateDate(Global.mySelf.birthday.getYear()+1900,Global.mySelf.birthday.getMonth(),Global.mySelf.birthday.getDate());
		rbFemale = (RadioButton) findViewById(R.id.femaleButton);
		rbMale = (RadioButton) findViewById(R.id.maleButton);
		etNickname.setText(Global.mySelf.nickName);
		etNickname.setSelection(etNickname.length());
		rbFemale.setChecked(Global.mySelf.sex);rbMale.setChecked(!Global.mySelf.sex);
		btEnsure.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				String nickname = etNickname.getText().toString();
				String username = Global.mySelf.username;
				String password = Global.mySelf.password;
				if(nickname.trim().equals(""))nickname=Global.mySelf.username;
				String birthday = String.format("%4d-%02d-%02d",dpBirthday.getYear()>=1900?dpBirthday.getYear():dpBirthday.getYear()+1900,dpBirthday.getMonth()+1,dpBirthday.getDayOfMonth());
				boolean usergender = rbFemale.isChecked();
				AlterBasicInfo_online task = new AlterBasicInfo_online(username, password, usergender, nickname, birthday);
				task.execute();

			}
		});
	}
	public class AlterBasicInfo_online extends AsyncTask<Void, Void, Byte>
	{
		public String Username;
		public String Password;
		public boolean Gender;
		public String Nickname;
		public String Birthday;
		private SweetAlertDialog dialog;

		public AlterBasicInfo_online(String username, String password, boolean gender, String nickname, String birthday)
		{
			Username = username;
			Password = password;
			Gender = gender;
			Nickname = nickname;
			Birthday = birthday;
		}

		@Override
		protected void onPreExecute()
		{
			btEnsure.setEnabled(false);
			dialog=new SweetAlertDialog(AlterBasicInfo.this,SweetAlertDialog.PROGRESS_TYPE);
			dialog.setCancelable(false);
			dialog.setTitleText("请稍候...");
			dialog.show();
		}

		@Override
		protected Byte doInBackground(Void... voids)
		{
			WebService updateUserInfo = new WebService("updateUserInfo");
			updateUserInfo.addProperty("username", Username).addProperty("password", Password).addProperty("sex", Gender).addProperty("nickName", Nickname).addProperty("birthday", Birthday);
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
			btEnsure.setEnabled(true);
			dialog.dismiss();
			switch (aByte)
			{
				case 1:
				{
					CustomToast.show(AlterBasicInfo.this, "修改成功", Toast.LENGTH_SHORT);
					UserInfo userInfo = new UserInfo();
					userInfo.username = Username;
					userInfo.nickName = Nickname;
					userInfo.password = Password;
					userInfo.sex = Gender;
					try
					{
						userInfo.birthday = new SimpleDateFormat("yyyy-MM-dd").parse(Birthday);
					}
					catch (ParseException e)
					{
						e.printStackTrace();
					}
					Global.mySelf.UpdateInfo(userInfo);
					finish();
					break;
				}
				case 4:
				{
					CustomToast.show(AlterBasicInfo.this, "修改失败", Toast.LENGTH_SHORT);
					break;
				}
			}
		}
	}
}
