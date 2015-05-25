package org.helloworld;

import android.app.Activity;
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

import org.helloworld.tools.FileUtils;
import org.helloworld.tools.Global;
import org.helloworld.tools.History;
import org.helloworld.tools.UserInfo;
import org.helloworld.tools.WebTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * 好友详细信息界面
 * */

public class FriendInfoAct extends Activity implements View.OnClickListener
{
	private TextView tvAge;
	private TextView tvSex;
	private ImageView ivHeadImg;
	private String friendName;
	private TextView tvNickName;
	private TextView tvRemark;
	public static Handler handler;
	public static final int MODIFY_RETURENED = 0;
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
							FileUtils.mkDir(new File(Global.PATH.HeadImg));
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
/*		else
			tvRemark.setVisibility(View.GONE);*/
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_friend_info, menu);
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
		new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
			.setTitleText("确定吗?")
			.setContentText(getString(R.string.delete_message))
			.setConfirmText("确定")
			.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener()
			{
				@Override
				public void onClick(final SweetAlertDialog sDialog)
				{
					//通知对方将自己从他的列表中删除
					JSONObject j = new JSONObject();
					try
					{
						j.put("cmdName", "delFriend");
						JSONArray params = new JSONArray();
						params.put(Global.mySelf.username);
						j.put("param", params);
						new WebTask(null, -1).execute("pushMsg", 5, "from", "cmd", "to", friendName, "msg", j.toString(), "time", Global.formatData(Global.getDate(), "yyyy-MM-dd HH:mm:ss"), "msgType", String.valueOf(Global.MSG_TYPE.T_TEXT_MSG));
					}
					catch (JSONException e)
					{
						e.printStackTrace();
					}
					DelFriend(friendName);
					sDialog
						.setTitleText("已删除")
						.setContentText("已将TA从你的好友列表中删除")
						.setConfirmText("知道了")
						.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener()
						{
							@Override
							public void onClick(SweetAlertDialog sweetAlertDialog)
							{
								sDialog.dismiss();
								finish();
							}
						})
						.showCancelButton(false)
						.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
				}
			})
			.setCancelText("取消").setCancelClickListener(null)
			.show();
	}

	public static void DelFriend(String friendName)
	{
		UserInfo u = Global.map2Friend.get(friendName);
		Global.map2Friend.remove(friendName);
		Global.friendList.remove(u);

		History h = Global.map.get(friendName);
		Global.map.remove(friendName);
		Global.historyList.remove(h);

		MainActivity.handler.sendEmptyMessage(Global.MSG_WHAT.W_REFRESH);

		String jobjStr = constructFriends2JSON().toString();
		new WebTask(null, -1).execute("updateFriendList", 2, "name", Global.mySelf.username, "friendList", jobjStr);
	}

	public void modifyRemark(View view)
	{
/*		final EditText etRemark=new EditText(this);
		etRemark.setHint("输入备注");
		final UserInfo u= Global.map2Friend.get(friendName);
		if(u.Ex_remark!=null)
			etRemark.setText(u.Ex_remark);
		AlertDialog.Builder builder=new AlertDialog.Builder(this)
									.setTitle("修改备注")
									.setPositiveButton("确定", new DialogInterface.OnClickListener()
									{
										@Override
										public void onClick(DialogInterface dialogInterface, int i)
										{
											u.Ex_remark = etRemark.getText().toString();
											String jsonStr = constructFriends2JSON().toString();
											new WebTask(null, -1).execute("updateFriendList", 2, "name", Global.mySelf.username, "friendList", jsonStr);
											finish();
										}
									})
									.setView(etRemark)
									.setNegativeButton("取消", null);
		builder.create().show();*/
		Intent intent=new Intent(this,ModifyFriendInfo.class);
		final UserInfo u= Global.map2Friend.get(friendName);
		if(u.Ex_remark==null)
			intent.putExtra("oldRemark","");
		else
			intent.putExtra("oldRemark",u.Ex_remark);
		startActivityForResult(intent,MODIFY_RETURENED);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(requestCode==MODIFY_RETURENED)
		{
			if(resultCode==ModifyFriendInfo.MODIFY_SUCCESS)
			{
				final UserInfo u= Global.map2Friend.get(friendName);
				u.Ex_remark = data.getStringExtra("newRemark");
				String jsonStr = constructFriends2JSON().toString();
				if(u.Ex_remark.length()>0)
					tvRemark.setText(u.Ex_remark);
				else
					tvRemark.setText("无备注");
				new WebTask(null, -1).execute("updateFriendList", 2, "name", Global.mySelf.username, "friendList", jsonStr);
			}
		}
	}

	public static JSONObject constructFriends2JSON()
	{
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
		return jobj;
	}

	public static void AddFriend(String strangerName)
	{
		JSONObject fL = FriendInfoAct.constructFriends2JSON();
		try
		{
			JSONArray fA = fL.getJSONArray("friends");
			for (int i = 0; i < fA.length(); i++)
			{
				if (fA.getJSONObject(i).getString("name").equals(strangerName))
					return;
			}
			JSONObject fnew = new JSONObject();
			fnew.put("name", strangerName);
			fA.put(fnew);
			new WebTask(MainActivity.handler, Global.MSG_WHAT.W_REFRESH_DEEP).execute("updateFriendList", 2, "name", Global.mySelf.username, "friendList", fL.toString());
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}
}
