package org.helloworld;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.navisdk.util.common.TaskQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class FriendInfoAct extends Activity implements View.OnClickListener
{
	private TextView tvAge;
	private TextView tvSex;
	private ImageView ivHeadImg;
	private String friendName;
	private TextView tvNickName;
	private TextView tvRemark;
	public static Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_friend_info);
		friendName = getIntent().getStringExtra("friendName");
		initView();

		handler = new Handler(new Handler.Callback()
		{
			@Override
			public boolean handleMessage(Message message)
			{
				if (message.what == Global.MSG_WHAT.W_DOWNLOADED_A_HAEDIMG)
				{
					try
					{
						SoapObject obj = (SoapObject) message.obj;
						byte[] bytes = Base64.decode(obj.getPropertyAsString(0), Base64.DEFAULT);
						Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
						if (bmp.getByteCount() > 0)
						{
							ivHeadImg.setImageBitmap(bmp);
							new File(Global.PATH.HeadImg).mkdir();
							File headFile = new File(Global.PATH.HeadImg, friendName + ".png");
							headFile.createNewFile();
							FileOutputStream fos = new FileOutputStream(headFile);
							bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
							fos.flush();
							fos.close();
						}
					}
					catch (NullPointerException ignored)
					{
					}
					catch (FileNotFoundException e)
					{
						e.printStackTrace();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
				return true;
			}
		});
		String path = Global.PATH.HeadImg + friendName + ".png";
		File file = new File(path);
		if (file.exists())
		{
			Bitmap bmp = BitmapFactory.decodeFile(path);
			ivHeadImg.setImageBitmap(bmp);
		}
		WebTask task = new WebTask(handler, Global.MSG_WHAT.W_DOWNLOADED_A_HAEDIMG);
		task.execute("downloadFile", 2, "path", "HeadImg", "fileName", friendName + ".png");

	}

	private void initView()
	{
		tvAge = (TextView) findViewById(R.id.tvAge);
		tvSex = (TextView) findViewById(R.id.tvSex);
		ivHeadImg = (ImageView) findViewById(R.id.ivHeadImg);
		tvNickName = (TextView) findViewById(R.id.tvNickName);
		tvRemark = (TextView) findViewById(R.id.tvRemark);
		UserInfo friend = Global.map2Friend.get(friendName);
		tvSex.setText(friend.sex ? "女" : "男");
		tvNickName.setText(friend.nickName);
		if (friend.Ex_remark != null && !friend.Ex_remark.equals(""))
			tvRemark.setText(friend.Ex_remark);
		else
			tvRemark.setVisibility(View.GONE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_friend_inof, menu);
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

	public void goback(View view)
	{
		finish();
	}

	@Override
	public void onClick(View view)
	{
		switch (view.getId())
		{
			case R.id.btnDel:
				deleteFriend(friendName);
				break;
			case R.id.btnChatWith:
				Intent intent = new Intent(this, ChatActivity.class);
				intent.putExtra("chatTo", friendName);
				startActivity(intent);
				finish();
				break;
		}
	}

	private void deleteFriend(final String friendName)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
										  .setTitle("警告")
										  .setPositiveButton("确定", new DialogInterface.OnClickListener()
										  {
											  @Override
											  public void onClick(DialogInterface dialogInterface, int i)
											  {
												  Global.friendList.remove(Global.map2Friend.get(friendName));
												  MainActivity.handler.sendEmptyMessage(Global.MSG_WHAT.W_REFRESH);
												  JSONObject jobj = new JSONObject();
												  JSONArray jarr = new JSONArray();
												  try
												  {

													  for (UserInfo u : Global.friendList)
													  {
														  JSONObject jUser = new JSONObject();
														  jUser.put("name", u.username);
														  if (u.Ex_remark != null && !u.Ex_remark.equals(""))
															  jUser.put("remark", u.Ex_remark);
														  jarr.put(jUser);
													  }
													  jobj.put("friends", jarr);
												  }
												  catch (JSONException e)
												  {
													  e.printStackTrace();
												  }
												  new WebTask(null,-1).execute("updateFriendList",2,"name",friendName,"friendList",jobj.toString());
												  finish();
											  }
										  })
										  .setMessage("删除将不可恢复，确定吗？")
										  .setNegativeButton("取消", null);
		builder.create().show();
	}
}
