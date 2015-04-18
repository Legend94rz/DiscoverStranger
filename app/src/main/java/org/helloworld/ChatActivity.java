package org.helloworld;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * *****************************************
 *
 * @author 廖乃波
 * @文件名称    : ChatActivity.java
 * @创建时间    : 2013-1-27 下午02:33:53
 * @文件描述    : 聊天界面
 * *****************************************
 */
public class ChatActivity extends Activity implements OnClickListener
{
	private String chatTo;
	private History history;
	private Button mBtnSend;
	private Button btnSendVoice;
	private Button btnCancel;
	private Button btnRec;
	private EditText mEditTextContent;
	private ListView mListView;
	private ChatMsgAdapter mAdapter;
	private RecordRelativeLayout recordRelativeLayout;
	private View faceRelativeLayout;

	public static Handler handler;
	@Override
	protected void onPause()
	{
		history.unreadCount=0;
		if(history.historyMsg.size()>0)Global.map.put(chatTo,history);
		handler=null;
		super.onPause();
	}
	public class SendTask extends AsyncTask<Void,Void,Boolean>
	{
		private Message message;
		public SendTask(Message message)
		{
			this.message = message;
		}
		@Override
		protected Boolean doInBackground(Void... voids)
		{
			WebService send=new WebService("pushMsg");
			send.addProperty("from",message.FromId).addProperty("to",message.ToId).addProperty("msg",message.Text);
			try
			{
				SoapObject s = send.call();
				return Boolean.parseBoolean( s.getProperty(0).toString() );
			}
			catch (NullPointerException e)
			{
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean bool)
		{
			if(bool)
				message.sendState=0;
			else
				message.sendState=2;
			mAdapter.notifyDataSetChanged();
		}
	}
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_chat);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		initView();
		initData();
		handler=new Handler(new Handler.Callback()
		{
			@Override
			public boolean handleMessage(android.os.Message message)
			{
				if(message.what==Global.MSG_WHAT.W_RECEIVED_NEW_MSG)
				{
					Message msg = (Message) message.obj;
					if (msg.FromId.equals(chatTo))
					{
						if(!history.historyMsg.contains(msg))
							history.historyMsg.add(msg);
						mAdapter.notifyDataSetChanged();
					}
				}
				return false;
			}
		});
	}

	public void initView()
	{
		mListView = (ListView) findViewById(R.id.listview);
		mBtnSend = (Button) findViewById(R.id.btnSend);
		mBtnSend.setOnClickListener(this);
		mEditTextContent = (EditText) findViewById(R.id.etSendmessage);
		recordRelativeLayout= (RecordRelativeLayout) findViewById(R.id.recordView);
		faceRelativeLayout= (View) findViewById(R.id.ll_facechoose);
		mEditTextContent.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3){}
			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i2, int i3){}
			@Override
			public void afterTextChanged(Editable editable)
			{
				if (editable.length() > 0)
					mBtnSend.setText("发送");
				else
					mBtnSend.setText("语音");
			}
		});
		btnCancel= (Button) findViewById(R.id.btnCancel);btnCancel.setOnClickListener(this);
		btnRec= (Button) findViewById(R.id.btnRec);		 btnRec.setOnClickListener(this);
		btnSendVoice=(Button)findViewById(R.id.btnSendVoice);btnSendVoice.setOnClickListener(this);
	}

	public void initData()
	{
		chatTo = getIntent().getStringExtra("chatTo");
		history = Global.map.get(chatTo);
		if(history==null){history=new History();history.fromName=chatTo;}else history.unreadCount=0;
		mAdapter = new ChatMsgAdapter(this, history.historyMsg);
		mListView.setAdapter(mAdapter);
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.btnSend:
				faceRelativeLayout.setVisibility(View.GONE);
				if (mBtnSend.getText().equals("发送"))
				{
					send();
				}
				else
				{
					if (recordRelativeLayout.getVisibility() == View.GONE)
						recordRelativeLayout.setVisibility(View.VISIBLE);
					else
						recordRelativeLayout.setVisibility(View.GONE);
					if(mEditTextContent.getVisibility()==View.VISIBLE)
						mEditTextContent.setVisibility(View.INVISIBLE);
					else
						mEditTextContent.setVisibility(View.VISIBLE);
				}
				break;
			case R.id.btnSendVoice:
				sendVoiceMsg();
				break;
			case R.id.btnCancel:
				btnRec.setText("点击录音");
				btnCancel.setVisibility(View.INVISIBLE);
				btnSendVoice.setVisibility(View.INVISIBLE);
				//Todo 删除临时声音文件

				break;
			case R.id.btnRec:
				if(btnRec.getText().equals("点击录音"))
				{
					btnSendVoice.setVisibility(View.INVISIBLE);
					btnCancel.setVisibility(View.INVISIBLE);
					btnRec.setText("停止");
					//Todo 开始录音

				}
				else if(btnRec.getText().equals("停止"))
				{
					btnSendVoice.setVisibility(View.VISIBLE);
					btnCancel.setVisibility(View.VISIBLE);
					btnRec.setText("试听");
					//Todo 停止录音

				}
				else
				{
					//Todo 播放声音

				}
				break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		return keyCode == KeyEvent.KEYCODE_BACK && ((FaceRelativeLayout) findViewById(R.id.FaceRelativeLayout)).hideFaceView() || super.onKeyDown(keyCode, event);
	}

	private void sendVoiceMsg()
	{
		Message entity = new Message();
		entity.msgType = Global.MSG_TYPE.T_SEND_MSG | Global.MSG_TYPE.T_VOICE_MSG;
		//Todo 上传声音文件

	}

	private void send()
	{
		String contString = mEditTextContent.getText().toString();
		if (contString.length() > 0)
		{
			Message entity = new Message();
			entity.FromId=Global.mySelf.username;
			entity.ToId=chatTo;
			entity.SendTime = getDate();
			entity.msgType = Global.MSG_TYPE.T_SEND_MSG | Global.MSG_TYPE.T_TEXT_MSG;
			entity.Text = contString;
			entity.sendState=1;
			new SendTask(entity).execute();
			mAdapter.notifyDataSetChanged();
			mEditTextContent.setText("");
			mListView.setSelection(mListView.getCount() - 1);
			history.historyMsg.add(entity);
		}
	}

	private Date getDate()
	{
		return new Date(System.currentTimeMillis());
	}
}