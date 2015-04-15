package org.helloworld;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

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
	private boolean isNew;

	private EditText mEditTextContent;

	private ListView mListView;

	private ChatMsgAdapter mAdapter;

	private List<Message> mDataArrays = new ArrayList<Message>();

	@Override
	protected void onPause()
	{
		if(isNew)Global.map.put(chatTo,history);
		super.onPause();
	}

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_chat);
		getWindow().setSoftInputMode(
										WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		chatTo = getIntent().getStringExtra("chatTo");
		history = Global.map.get(chatTo);
		isNew=(history==null);
		if(isNew){history=new History();history.fromName=chatTo;}else history.unreadCount=0;
		initView();
		initData();
	}

	public void initView()
	{
		mListView = (ListView) findViewById(R.id.listview);
		mBtnSend = (Button) findViewById(R.id.btnSend);
		mBtnSend.setOnClickListener(this);
		mEditTextContent = (EditText) findViewById(R.id.etSendmessage);
		mEditTextContent.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3)
			{
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i2, int i3)
			{
			}

			@Override
			public void afterTextChanged(Editable editable)
			{
				if (editable.length() > 0)
					mBtnSend.setText("发送");
				else
					mBtnSend.setText("语音");
			}
		});
	}

	/*
	private String[] msgArray = new String[] { "[媚眼]测试啦[媚眼]", "测试啦", "测试啦",
			"测试啦", "测试啦", "你妹[苦逼]", "测[惊讶]你妹", "测你妹[胜利]",
			"测试啦" };

	private String[] dataArray = new String[] { "2012-12-12 12:00",
			"2012-12-12 12:10", "2012-12-12 12:11", "2012-12-12 12:20",
			"2012-12-12 12:30", "2012-12-12 12:35", "2012-12-12 12:40",
			"2012-12-12 12:50", "2012-12-12 12:50" };

	private final static int COUNT = 8;
	*/
	public void initData()
	{
		History h = Global.map.get(chatTo);
		if (h != null)
			for (int i = 0; i < h.historyMsg.size(); i++)
			{
				mDataArrays.add(h.historyMsg.get(i));
			}

		mAdapter = new ChatMsgAdapter(this, mDataArrays);
		mListView.setAdapter(mAdapter);

	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.btnSend:
				if (mBtnSend.getText().equals("发送"))
					send();
				else
					sendVoiceMsg();
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
		//Todo

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

			mDataArrays.add(entity);
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