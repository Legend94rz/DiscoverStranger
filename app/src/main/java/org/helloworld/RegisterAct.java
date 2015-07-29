package org.helloworld;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.helloworld.tools.FileUtils;
import org.helloworld.tools.Global;
import org.helloworld.tools.UploadTask;
import org.helloworld.tools.UserInfo;
import org.helloworld.tools.WebService;
import org.ksoap2.serialization.SoapObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * 注册界面
 */
public class RegisterAct extends BaseActivity
{

	private static final int PHOTO_REQUEST = 1;
	private static final int CAMERA_REQUEST = 2;
	private static final int PHOTO_CLIP = 3;
	private ImageView iv2;
	private ImageView ivAvatarimg;
	private ProgressBar pbcheckname;
	private EditText etUser_name;
	private EditText etpasswords;
	private EditText etconfirmpasswords;
	private Button btnNext;
	private RadioButton rbmale;
	private RadioButton rbfemale;
	private Bitmap photo;
	private ImageView ivUsernameError;
	private ImageView ivPassError;
	private TextView tvError_info_username;
	private TextView tvError_info_password;

	public static Handler handler;
	private Boolean canRegister = true;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		ivAvatarimg = (ImageView) findViewById(R.id.Avatar);
		etUser_name = (EditText) findViewById(R.id.username);
		etpasswords = (EditText) findViewById(R.id.password);
		etconfirmpasswords = (EditText) findViewById(R.id.confirmPassword);
		pbcheckname = (ProgressBar) findViewById(R.id.check_name);
		iv2 = (ImageView) findViewById(R.id.imageView2);
		final EditText etnick_name = (EditText) findViewById(R.id.nickname);
		rbmale = (RadioButton) findViewById(R.id.maleButton);
		rbfemale = (RadioButton) findViewById(R.id.femaleButton);
		btnNext = (Button) findViewById(R.id.next);
		ivPassError = (ImageView) findViewById(R.id.password_error);
		ivUsernameError = (ImageView) findViewById(R.id.username_error);
		tvError_info_password = (TextView) findViewById(R.id.error_info_password);
		tvError_info_username = (TextView) findViewById(R.id.error_info_username);
		/** To set the user's avatar by choosing image fromm gallery or taking photo
		 * **/
		ivAvatarimg.setOnClickListener(new setAvatar());
		/**To check if the user name has bend used.
		 * */
		etUser_name.setOnFocusChangeListener(new View.OnFocusChangeListener()
		{
			@Override
			public void onFocusChange(View v, boolean hasfocus)
			{
				if (!hasfocus && etUser_name.getText().length() > 0)
				{
					checkUserName();
				}
			}
		});
		/** To check if the password is the same as the confirmed_password.*/
		final View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener()
		{
			@Override
			public void onFocusChange(View v, boolean hasfocus)
			{
				if (!hasfocus)
				{
					checkPassword();
				}

			}

		};
		etconfirmpasswords.setOnFocusChangeListener(onFocusChangeListener);
		/**To send to info to webservice and to register.
		 * */
		btnNext.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				canRegister = true;
				checkUserName();
				checkPassword();
				if (!canRegister) return;
				btnNext.setEnabled(false);
				String Username = etUser_name.getText().toString();
				String psw = etpasswords.getText().toString();
				boolean usergender = rbfemale.isChecked();
				String nick = etnick_name.getText().toString();
				Register_online task = new Register_online(Username, psw, usergender, nick);
				task.execute();

			}
		});
		handler = new Handler(new Handler.Callback()
		{
			@Override
			public boolean handleMessage(Message message)
			{
				switch (message.what)
				{
					case Global.MSG_WHAT.W_CHECKED_USERNAME:
						pbcheckname.setVisibility(View.INVISIBLE);
						if (!((Boolean) message.obj))
						{
							ivUsernameError.setVisibility(View.VISIBLE);
							tvError_info_username.setVisibility(View.VISIBLE);
							tvError_info_username.setText("用户名已存在");
						}
						else
						{
							ivUsernameError.setVisibility(View.GONE);
							tvError_info_username.setVisibility(View.GONE);
							tvError_info_username.setText("");
						}
						break;
				}
				return true;
			}
		});

	}

	public void checkPassword()
	{
		String ps = etpasswords.getText().toString();
		String cmps = etconfirmpasswords.getText().toString();
		String error_info_password = "";
		if (!ps.equals(cmps))
		{
			error_info_password += " 两次输入的密码不一致 ";
		}
		if (!((ps.length() >= 8) && (ps.length() <= 32)))
			error_info_password += " 密码长度为8~32位";
		if (error_info_password.length() > 0)
		{
			ivPassError.setVisibility(View.VISIBLE);
			tvError_info_password.setVisibility(View.VISIBLE);
			tvError_info_password.setText(error_info_password);
			canRegister &= false;
		}
		else
		{
			ivPassError.setVisibility(View.GONE);
			tvError_info_password.setVisibility(View.GONE);
			tvError_info_password.setText("");
			canRegister &= true;
		}
	}

	public void checkUserName()
	{
		String urname = etUser_name.getText().toString();
		boolean isLegal = true;
		String error_info_username = "";
		for (int i = 0; i < urname.length(); i++)
		{
			int s = (int) urname.charAt(i);
			if (!((s > 47) && (s < 58) || ((s > 64) && (s < 91)) || ((s > 96) && (s < 122)) || (s == 95)))
				isLegal = false;
		}
		if (!isLegal) error_info_username += " 用户名只能包含字母数字和下划线 ";
		if (urname.length() > 16 || urname.length() < 1) error_info_username += "用户名在1-16位之间";
		if (error_info_username.length() > 0)
		{
			ivUsernameError.setVisibility(View.VISIBLE);
			tvError_info_username.setVisibility(View.VISIBLE);
			tvError_info_username.setText(error_info_username);
			canRegister &= false;
			return;
		}
		else
		{
			ivUsernameError.setVisibility(View.GONE);
			tvError_info_username.setVisibility(View.GONE);
			tvError_info_username.setText("");
		}
		pbcheckname.setVisibility(View.VISIBLE);
		Check_online task = new Check_online(urname);
		task.execute();
	}

	/**
	 * To set the user's avatar by choosing image fromm gallery or taking photo
	 * *
	 */
	class setAvatar implements View.OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			SweetAlertDialog dialog = new SweetAlertDialog(RegisterAct.this);
			dialog.setTitleText("请选择头像图片的来源")
				.setConfirmText("相册")
				.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener()
				{
					@Override
					public void onClick(SweetAlertDialog sweetAlertDialog)
					{
						sweetAlertDialog.dismiss();
						getPicFromPhoto();
					}
				})
				.setCancelText("拍照")
				.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener()
				{
					@Override
					public void onClick(SweetAlertDialog sweetAlertDialog)
					{
						sweetAlertDialog.dismiss();
						getPicFromCamera();
					}
				}).show();
		}
	}

	/**
	 * To check if the password is the same as the confirmed_password.
	 */
	public class Check_online extends AsyncTask<Void, Void, Boolean>
	{
		public String Username;

		public Check_online(String username)
		{
			Username = username;
		}

		@Override
		protected Boolean doInBackground(Void... voids)
		{
			WebService check = new WebService("GetUser");
			check.addProperty("name", Username);
			try
			{
				SoapObject result = check.call();
				return result.getPropertyCount() == 0;
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
			Message m = new Message();
			m.what = Global.MSG_WHAT.W_CHECKED_USERNAME;
			m.obj = aBoolean;
			canRegister &= aBoolean;
			handler.sendMessage(m);
			pbcheckname.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * To register.
	 */
	public class Register_online extends AsyncTask<Void, Void, Byte>
	{
		public String Username;
		public String Password;
		public boolean Gender;
		public String Nickname;

		public Register_online(String username, String password, boolean gender, String nickname)
		{
			Username = username;
			Password = password;
			Gender = gender;
			Nickname = nickname;
		}

		@Override
		protected Byte doInBackground(Void... voids)
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			if (photo == null)
				return 2;        //No photo
			else
				photo.compress(Bitmap.CompressFormat.PNG, 100, baos);
			byte[] bytes = baos.toByteArray();
			UploadTask uploadTask = new UploadTask("HeadImg", Username + ".png", Global.BLOCK_SIZE, bytes);
			if (!uploadTask.call()) return 3;
			WebService register = new WebService("SignUp");
			register.addProperty("name", Username).addProperty("pass", Password).addProperty("sex", Gender).addProperty("nickName", Nickname);
			try
			{
				SoapObject result = register.call();
				if (!result.getPropertyAsString(0).equals(Global.OPT_SUCCEED)) return 4;
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
			btnNext.setEnabled(true);
			switch (aByte)
			{
				case 1:
				{
					Toast.makeText(RegisterAct.this, "注册成功", Toast.LENGTH_SHORT).show();
					String path = Global.PATH.HeadImg;
					String filename = Username + ".png";
					File file = new File(path, filename);
					FileUtils.mkDir(file.getParentFile());
					try
					{
						file.createNewFile();
						FileOutputStream fileOutputStream = new FileOutputStream(file);
						photo.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
						fileOutputStream.flush();
						fileOutputStream.close();
					}
					catch (FileNotFoundException e)
					{
						e.printStackTrace();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
					Global.mySelf = new UserInfo();
					Global.mySelf.username = Username;
					Global.mySelf.password = Password;
					Global.mySelf.nickName = Nickname;
					Global.mySelf.sex = Gender;
					Global.InitData();
					Intent i = new Intent(RegisterAct.this, MainActivity.class);
					startActivity(i);
					finish();
					break;
				}
				case 2:
				{
					Toast.makeText(RegisterAct.this, "请先选择一张照片作为头像", Toast.LENGTH_SHORT).show();
					break;
				}
				case 3:
				case 4:
				{
					Toast.makeText(RegisterAct.this, String.format("注册失败，错误%d", aByte), Toast.LENGTH_SHORT).show();
					break;
				}
			}
		}
	}

	private void getPicFromPhoto()
	{
		Intent intent = new Intent(Intent.ACTION_PICK, null);
		intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
		startActivityForResult(intent, PHOTO_REQUEST);
	}

	private void getPicFromCamera()
	{
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// 下面这句指定调用相机拍照后的照片存储的路径
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(Global.PATH.HeadImg, "temp.png")));
		startActivityForResult(intent, CAMERA_REQUEST);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode)
		{
			case CAMERA_REQUEST:
				if (resultCode == -1)// -1表示拍照成功
				{
					File file = new File(Global.PATH.HeadImg, "temp.png");
					if (file.exists())
					{
						photoClip(Uri.fromFile(file));
					}
				}
				break;
			case PHOTO_REQUEST:
				if (data != null)
				{
					photoClip(data.getData());
				}
				break;
			case PHOTO_CLIP:
				if (data != null)
				{
					Bundle extras = data.getExtras();
					if (extras != null)
					{
						photo = extras.getParcelable("data");
						ivAvatarimg.setImageBitmap(photo);
					}
				}
				break;
			default:
				break;
		}

	}

	private void photoClip(Uri uri)
	{
		// 调用系统中自带的图片剪裁
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		// 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
		intent.putExtra("crop", "true");
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY 是裁剪图片宽高
		intent.putExtra("outputX", 150);
		intent.putExtra("outputY", 150);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, PHOTO_CLIP);
	}

}
