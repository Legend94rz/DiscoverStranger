package org.helloworld;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.ksoap2.serialization.SoapObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * *****************************************
 *
 * @文件名称 : ChatActivity.java
 * @文件描述 : 聊天界面
 * *****************************************
 */
public class ChatActivity extends Activity implements OnClickListener, MediaRecorder.OnInfoListener
{
	private static final int PHOTO_REQUEST = 1;

	private String chatTo;
	private History history;
	private EditText mEditTextContent;
	private ListView mListView;
	private ChatMsgAdapter mAdapter;
	private InputMethodManager manager;

	//三个ToggleButton
	private ToggleButton swiFace;
	private ToggleButton swiMoreInput;
	private ToggleButton swiInput;

	//这5个是录音界面元素
	private RecordRelativeLayout recordRelativeLayout;
	private Button btnSend;
	private Button btnSendVoice;
	private Button btnCancel;
	private Button btnRec;

	private Chronograph timer;    //计时器
	int timeOfRec;        //录音时间长度
	private TextView tvTime;

	//录音相关
	File soundFile;
	MediaRecorder recorder;


	private View faceRelativeLayout;
	private TextView tvChatTitle;
	private GridView gvMoreInput;

	public static Handler handler;

	@Override
	protected void onPause()
	{
		history.unreadCount = 0;
		if (history.historyMsg.size() > 0) Global.map.put(chatTo, history);
		handler = null;
		super.onPause();
	}

	@Override
	public void onInfo(MediaRecorder mediaRecorder, int what, int extra)
	{
		switch (what)
		{
			case MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED:
				Toast.makeText(this, "达到最大文件大小限制,已停止", Toast.LENGTH_SHORT).show();
				stopRec();
				break;
		}
	}

	public class SendTask extends AsyncTask<Void, Void, Boolean>
	{
		private Message message;
		private String remotePath;

		public SendTask(Message message, @Nullable String remotePath)
		{
			this.message = message;
			this.remotePath = remotePath;
		}

		@Override
		protected Boolean doInBackground(Void... voids)
		{
			boolean r1 = true;
			if ((message.msgType & Global.MSG_TYPE.T_TEXT_MSG) == 0)
			{
				try
				{
					String base64 = FileUtils.toBase64(message.extra.getString("localPath"));

					WebService upload = new WebService("uploadFile");
					upload.addProperty("base64", base64).addProperty("path", remotePath).addProperty("fileName", message.extra.getString("remoteName"));
					SoapObject s1 = upload.call();
					r1 = Boolean.parseBoolean(s1.getPropertyAsString(0));
				}
				catch (NullPointerException ignored)
				{
					r1 = false;
				}
			}
			if (!r1) return false;
			WebService send = new WebService("pushMsg");
			send.addProperty("from", message.fromId)
				.addProperty("to", message.toId)
				.addProperty("msg", message.text)
				.addProperty("time", message.getDateWithFormat("yyyy-MM-dd HH:mm:ss"))
				.addProperty("msgType", message.msgType & (~Global.MSG_TYPE.T_RECEIVE_MSG) & (~Global.MSG_TYPE.T_SEND_MSG));
			try
			{
				SoapObject s2 = send.call();
				return Boolean.parseBoolean(s2.getPropertyAsString(0));
			}
			catch (NullPointerException e)
			{
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean bool)
		{
			if (bool)
				message.sendState = 0;
			else
				message.sendState = 2;
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		handler = new Handler(new Handler.Callback()
		{
			@Override
			public boolean handleMessage(android.os.Message message)
			{
				switch (message.what)
				{
					case Global.MSG_WHAT.W_RECEIVED_NEW_MSG:
						if (message.obj != null)
						{
							Message msg = (Message) message.obj;
							if (msg.fromId.equals(chatTo) && !history.historyMsg.contains(msg))
								history.historyMsg.add(msg);
						}
						mAdapter.notifyDataSetChanged();
						if (mListView.getLastVisiblePosition() == mListView.getCount() - 1)
							mListView.setSelection(mListView.getCount() - 1);
						mAdapter.notifyDataSetChanged();
						break;
					case Global.MSG_WHAT.W_SECOND_GO_BY:
						timeOfRec++;
						tvTime.setText(String.format("%02d:%02d", timeOfRec / 60, timeOfRec % 60));
						break;
					case Global.MSG_WHAT.W_RESEND_MSG:
						Message m = ((Message) message.obj);
						if ((m.msgType & Global.MSG_TYPE.T_TEXT_MSG) > 0)
							send((Message) message.obj);
						else if ((m.msgType & Global.MSG_TYPE.T_VOICE_MSG) > 0)
							sendVoiceMsg((Message) message.obj);
						else if ((m.msgType & Global.MSG_TYPE.T_PIC_MSG) > 0)
							sendPic(((Message) message.obj));
						break;
					case Global.MSG_WHAT.W_PLAY_SOUND:
						String P;
						if (message.obj instanceof Message)
						{
							Message m2 = ((Message) message.obj);
							String[] p = m2.text.split("~");
							P = p[0];
						}
						else
						{
							P = message.obj.toString();
						}
						playSound(P);
						break;
				}
				return false;
			}
		});
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		initView();
		initData();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev)
	{
		if (ev.getAction() == MotionEvent.ACTION_DOWN)
		{
			View view = getCurrentFocus();
			if (isHideInput(view, ev))
			{
				HideSoftInput(view.getWindowToken());
			}
		}
		return super.dispatchTouchEvent(ev);
	}

	boolean isPointInView(float x, float y, View v)
	{
		int[] l = {0, 0};
		v.getLocationInWindow(l);
		int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left + v.getWidth();
		return (x > left && x < right && y > top && y < bottom);
	}

	// 判定是否需要隐藏
	private boolean isHideInput(View v, MotionEvent ev)
	{
		if (v != null && (v instanceof EditText))
		{
			boolean b1 = isPointInView(ev.getX(), ev.getY(), v);
			boolean b2 = btnSend.getVisibility() == View.VISIBLE && isPointInView(ev.getX(), ev.getY(), btnSend);
			return !(b1 || b2);
		}
		return false;
	}

	// 隐藏软键盘
	private void HideSoftInput(IBinder token)
	{
		if (token != null)
		{
			manager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==PHOTO_REQUEST && data!=null && resultCode== RESULT_OK)
		{
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };

			Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
			cursor.moveToFirst();

			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String picturePath = cursor.getString(columnIndex);
			cursor.close();
			File tmp=new File(picturePath);
			if(tmp.length()>=64000)
			{
				Toast.makeText(this,"图片太大",Toast.LENGTH_SHORT).show();
				return;
			}
			Message entity = new Message();
			String localPath = tmp.getParent()+"/";
			String localName = tmp.getName();
			entity.msgType = Global.MSG_TYPE.T_SEND_MSG | Global.MSG_TYPE.T_PIC_MSG;
			entity.fromId = Global.mySelf.username;
			entity.toId = chatTo;
			entity.sendTime = Global.getDate();
			entity.text = String.format("%s-%s-%tQ.png", Global.mySelf.username, chatTo, entity.sendTime);
			entity.extra = new Bundle();
			entity.extra.putString("localPath", localPath + localName);
			entity.extra.putString("localName", localName);
			entity.extra.putString("remoteName", entity.text);
			entity.sendState = 1;
			//发送图片
			sendPic(entity);
		}
	}

	public void initView()
	{
		mListView = (ListView) findViewById(R.id.lvChatMsg);
		tvTime = (TextView) findViewById(R.id.tvTime);
		btnSend = (Button) findViewById(R.id.btnSend);
		btnSend.setOnClickListener(this);
		swiFace = (ToggleButton) findViewById(R.id.btnFace);
		swiInput = (ToggleButton) findViewById(R.id.btnSwitchInput);
		swiMoreInput = (ToggleButton) findViewById(R.id.ibMoreInput);
		mEditTextContent = (EditText) findViewById(R.id.etSendmessage);
		recordRelativeLayout = (RecordRelativeLayout) findViewById(R.id.recordView);
		faceRelativeLayout = findViewById(R.id.ll_facechoose);
		tvChatTitle = (TextView) findViewById(R.id.tvToId);

		mEditTextContent.setOnClickListener(this);

		//----------以下是初始化gvMoreInput--------------------
		gvMoreInput = (GridView) findViewById(R.id.gvMoreInput);
		ArrayList<HashMap<String, Object>> lstItem = new ArrayList<HashMap<String, Object>>();
		String[] des = new String[]{"图片"};
		for (int i = 0; i < 1; i++)
		{
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("ItemPic", R.drawable.nohead);
			map.put("ItemDes", des[i]);
			lstItem.add(map);
		}
		SimpleAdapter simpleAdapter = new SimpleAdapter(this, lstItem, R.layout.input_item, new String[]{"ItemPic", "ItemDes"}, new int[]{R.id.ivInputImg, R.id.tvInputName});
		gvMoreInput.setAdapter(simpleAdapter);
		gvMoreInput.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
			{
				HashMap<String, Object> p = (HashMap<String, Object>) adapterView.getItemAtPosition(i);
				//Toast.makeText(ChatActivity.this, p.get("ItemDes").toString(), Toast.LENGTH_SHORT).show();
				if (p.get("ItemDes").equals("图片"))
				{
					//Todo 选择图片
					Intent choostPhoto = new Intent(Intent.ACTION_PICK, null);
					choostPhoto.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
					startActivityForResult(choostPhoto, PHOTO_REQUEST);
				}
			}
		});
		//--------------以下是初始化录音界面--------------------
		btnCancel = (Button) findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(this);
		btnRec = (Button) findViewById(R.id.btnRec);
		btnRec.setOnClickListener(this);
		btnSendVoice = (Button) findViewById(R.id.btnSendVoice);
		btnSendVoice.setOnClickListener(this);
	}

	private void HideAndReset()
	{
		faceRelativeLayout.setVisibility(View.GONE);
		swiFace.setChecked(false);
		recordRelativeLayout.setVisibility(View.GONE);
		swiInput.setChecked(false);
		gvMoreInput.setVisibility(View.GONE);
		swiMoreInput.setChecked(false);
	}

	public void initData()
	{
		chatTo = getIntent().getStringExtra("chatTo");
		history = Global.map.get(chatTo);
		if (history == null)
		{
			history = new History(chatTo);
		}
		else history.unreadCount = 0;
		mAdapter = new ChatMsgAdapter(this, history.historyMsg);
		mListView.setAdapter(mAdapter);
		tvChatTitle.setText(chatTo);
	}

	@Override
	public void onClick(View v)
	{
		Message entity;
		switch (v.getId())
		{
			case R.id.btnSend:
				String contString = mEditTextContent.getText().toString();
				entity = new Message();
				entity.fromId = Global.mySelf.username;
				entity.toId = chatTo;
				entity.sendTime = Global.getDate();
				entity.msgType = Global.MSG_TYPE.T_SEND_MSG | Global.MSG_TYPE.T_TEXT_MSG;
				entity.text = contString;
				entity.sendState = 1;
				send(entity);
				break;
			case R.id.btnSendVoice:
				entity = new Message();
				entity.msgType = Global.MSG_TYPE.T_SEND_MSG | Global.MSG_TYPE.T_VOICE_MSG;
				entity.fromId = Global.mySelf.username;
				entity.toId = chatTo;
				entity.sendTime = Global.getDate();
				entity.extra = new Bundle();
				entity.extra.putString("localPath", soundFile.getAbsolutePath());
				entity.extra.putString("localName", soundFile.getName());
				entity.extra.putString("remoteName", soundFile.getName());
				entity.text = String.format("%s~[Voice] %d'", soundFile.getName(), timeOfRec);
				entity.sendState = 1;
				sendVoiceMsg(entity);
				btnSendVoice.setVisibility(View.INVISIBLE);
				btnCancel.setVisibility(View.INVISIBLE);
				btnRec.setText("点击录音");
				tvTime.setText("00:00");
				break;
			case R.id.btnCancel:
				btnRec.setText("点击录音");
				btnCancel.setVisibility(View.INVISIBLE);
				btnSendVoice.setVisibility(View.INVISIBLE);
				if (soundFile.exists())
					soundFile.delete();
				break;
			case R.id.btnRec:
				if (btnRec.getText().equals("点击录音"))
				{
					btnSendVoice.setVisibility(View.INVISIBLE);
					btnCancel.setVisibility(View.INVISIBLE);
					btnRec.setText("停止");
					timeOfRec = 0;
					timer = new Chronograph(handler);
					timer.start();
					String path = Global.PATH.SoundMsg;
					FileUtils.mkDir(new File(path));
					soundFile = new File(path + String.format("%s-%s-%tQ.amr", Global.mySelf.username, chatTo, Global.getDate()));
					recorder = new MediaRecorder();
					recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
					recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
					recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
					recorder.setMaxFileSize(64000);        //最大64k
					recorder.setOutputFile(soundFile.getAbsolutePath());
					try
					{
						recorder.prepare();
						recorder.start();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
				else if (btnRec.getText().equals("停止"))
				{
					stopRec();
				}
				else
				{
					playSound(soundFile.getName());
				}
				break;
			case R.id.etSendmessage:
				HideAndReset();
				break;
		}
	}

	private void playSound(String fileName)
	{
		if (FileUtils.Exist(Global.PATH.SoundMsg + fileName))
		{
			MediaPlayer player = new MediaPlayer();
			try
			{
				File tempFile = new File(Global.PATH.SoundMsg + fileName);
				FileInputStream fis = new FileInputStream(tempFile);
				player.reset();
				player.setDataSource(fis.getFD());
				player.prepare();
				player.start();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			/*异步任务下载语音*/
			DownloadTask t = new DownloadTask("soundMsg", Global.PATH.SoundMsg, fileName, handler, Global.MSG_WHAT.W_PLAY_SOUND, fileName);
			t.execute();
		}
	}

	private void stopRec()
	{
		btnSendVoice.setVisibility(View.VISIBLE);
		btnCancel.setVisibility(View.VISIBLE);
		btnRec.setText("试听");
		if (recorder != null)
		{
			recorder.stop();
			recorder = null;
		}
		if (timer != null)
		{
			timer.Break = true;
			timer = null;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		return keyCode == KeyEvent.KEYCODE_BACK && ((FaceRelativeLayout) findViewById(R.id.FaceRelativeLayout)).hideFaceView() || super.onKeyDown(keyCode, event);
	}

	private void sendVoiceMsg(Message entity)
	{

		if (!history.historyMsg.contains(entity))
			history.historyMsg.add(entity);
		mAdapter.notifyDataSetChanged();
		new SendTask(entity, "soundMsg").execute();
		mListView.setSelection(mListView.getCount() - 1);
	}

	private void sendPic(Message entity)
	{
		if (!history.historyMsg.contains(entity))
			history.historyMsg.add(entity);
		mAdapter.notifyDataSetChanged();
		new SendTask(entity, "ChatPic").execute();
		mListView.setSelection(mListView.getCount() - 1);
	}

	private void send(Message entity)
	{
		new SendTask(entity, null).execute();
		if (!history.historyMsg.contains(entity))
			history.historyMsg.add(entity);
		mAdapter.notifyDataSetChanged();
		mEditTextContent.setText("");
		mListView.setSelection(mListView.getCount() - 1);
	}

	public void goback(View v)
	{     //标题栏 返回按钮
		this.finish();
	}
}