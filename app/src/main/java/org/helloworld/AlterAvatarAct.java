package org.helloworld;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.helloworld.tools.CustomToast;
import org.helloworld.tools.FileUtils;
import org.helloworld.tools.Global;
import org.helloworld.tools.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class AlterAvatarAct extends BaseActivity
{
	private static final int PHOTO_REQUEST = 1;
	private static final int CAMERA_REQUEST = 2;
	private static final int PHOTO_CLIP = 3;

	private ImageView ivAvatarimg;
	private Button btnNext;
	private Bitmap photo;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alter_avatar);
		ivAvatarimg = (ImageView) findViewById(R.id.Avatar);
		btnNext = (Button) findViewById(R.id.confirm);
		ivAvatarimg.setOnClickListener(new setAvatar());
		btnNext.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				AlterAvatar_online task = new AlterAvatar_online(Global.mySelf.username);
				task.execute();
			}
		});
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
			final SweetAlertDialog dialog = new SweetAlertDialog(AlterAvatarAct.this);
			dialog.setTitleText("请选择头像图片的来源");
			dialog.setConfirmText("相册");
			dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener()
			{
				@Override
				public void onClick(SweetAlertDialog sweetAlertDialog)
				{
					dialog.dismiss();
					getPicFromPhoto();
				}
			});
			dialog.setCancelText("拍照");
			dialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener()
			{
				@Override
				public void onClick(SweetAlertDialog sweetAlertDialog)
				{
					dialog.dismiss();
					getPicFromCamera();
				}
			});
			dialog.show();
		}
	}

	/**
	 * To register.
	 */
	public class AlterAvatar_online extends AsyncTask<Void, Void, Byte>
	{
		public String Username;

		public AlterAvatar_online(String username)
		{
			Username = username;
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
					CustomToast.show(AlterAvatarAct.this, "修改成功", Toast.LENGTH_SHORT);
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
					finish();
					break;
				}
				case 2:
				{
					CustomToast.show(AlterAvatarAct.this, "请先选择一张照片作为头像", Toast.LENGTH_SHORT);
					break;
				}
				case 3:
				case 4:
				{
					CustomToast.show(AlterAvatarAct.this, String.format("修改失败，错误%d", aByte), Toast.LENGTH_SHORT);
					break;
				}
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode)
		{
			case CAMERA_REQUEST:
				switch (resultCode)
				{
					case -1:// -1表示拍照成功
						File file = new File(Global.PATH.HeadImg, "temp.png");
						if (file.exists())
						{
							photoClip(Uri.fromFile(file));
						}
						break;
					default:
						break;
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

}
