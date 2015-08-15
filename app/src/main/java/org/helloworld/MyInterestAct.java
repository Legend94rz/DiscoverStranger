package org.helloworld;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.helloworld.tools.CustomToast;
import org.helloworld.tools.Global;
import org.helloworld.tools.WebTask;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class MyInterestAct extends BaseActivity
{
	ListView listView;
	InterestAdapter adapter;
	LinearLayout llSel;
	RelativeLayout llInput;
	EditText editText;
	Button btnOK;
	Button btnCustom;
	Button btnHotkey;
	Button button;
	private static final int HOTKEY_REQUEST = 3;
	private static final int MAX_INTEREST_COUNT = 10;
	View footview;
	Handler handler;
	SweetAlertDialog dialog;
	ArrayList<String> list;

	class InterestAdapter extends BaseAdapter
	{
		LayoutInflater inflater;
		ArrayList<String> list;

		public InterestAdapter(ArrayList<String> list)
		{
			this.list = list;
			inflater = MyInterestAct.this.getLayoutInflater();
		}
		@Override
		public int getCount()
		{
			return list.size();
		}

		@Override
		public Object getItem(int i)
		{
			return list.get(i);
		}

		@Override
		public long getItemId(int i)
		{
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup)
		{
			final String s = (String) getItem(i);
			H h;
			if (view == null)
			{
				h = new H();
				view = inflater.inflate(R.layout.item_hotkey, null);
				h.textView = (TextView) view.findViewById(R.id.tvItem);
				h.button = (Button) view.findViewById(R.id.btnDel);
				view.setTag(h);
			}
			else
				h = (H) view.getTag();
			h.button.setVisibility(View.VISIBLE);
			h.button.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					list.remove(s);
					if (getCount() < MAX_INTEREST_COUNT) footview.setVisibility(View.VISIBLE);
					notifyDataSetChanged();
				}
			});
			h.textView.setText(s);
			return view;
		}
		@Override
		public boolean isEnabled(int position)
		{
			return false;
		}

		@Override
		public boolean areAllItemsEnabled()
		{
			return false;
		}
		class H
		{
			TextView textView;
			Button button;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_interest);
		listView = (ListView) findViewById(R.id.listview);
		footview = View.inflate(this, R.layout.interest_foot_view, null);
		listView.addFooterView(footview, null, false);
		list=new ArrayList<>();
		list.addAll(Global.settings.interests);
		adapter = new InterestAdapter(list);
		listView.setAdapter(adapter);
		llInput = (RelativeLayout) footview.findViewById(R.id.llInput);
		llSel = (LinearLayout) footview.findViewById(R.id.llSel);

		editText = (EditText) footview.findViewById(R.id.editText);
		btnOK = (Button) footview.findViewById(R.id.btnOK);
		btnCustom = (Button) footview.findViewById(R.id.btnCustom);
		btnHotkey = (Button) footview.findViewById(R.id.btnHotkey);
		button = (Button) findViewById(R.id.btnOK);
		SetListener();
		handler = new Handler(new Handler.Callback()
		{
			@Override
			public boolean handleMessage(Message message)
			{
				if (message.what == Global.MSG_WHAT.W_SENDED_REQUEST)
				{
					SoapObject so = (SoapObject) message.obj;
					if (so != null && Boolean.parseBoolean(so.getPropertyAsString(0)))
					{
						dialog.setTitleText("成功").setConfirmText("确定")
							.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener()
							{
								@Override
								public void onClick(SweetAlertDialog sweetAlertDialog)
								{
									sweetAlertDialog.dismiss();
									finish();
								}
							})
							.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
					}
					else
					{
						dialog.setTitleText("网络连接失败").setConfirmText("确定").changeAlertType(SweetAlertDialog.ERROR_TYPE);
					}
				}
				return false;
			}
		});
	}

	private void SetListener()
	{
		editText.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
			{
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
			{
			}

			@Override
			public void afterTextChanged(Editable editable)
			{
				if (editable.length() == 0) btnOK.setText("取消");
				else btnOK.setText("确定");
				if (editable.length() > 4)
					editText.setBackgroundResource(R.drawable.bg_editbox_err);
				else
					editText.setBackgroundResource(R.drawable.edit_bg);
			}
		});
		btnOK.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if (btnOK.getText() == "确定")
				{
					Editable s = editText.getText();
					if (s.length() <= 4)
						if(!list.contains(s.toString()) )
						{
							editText.setText("");
							list.add(s.toString());
							if (list.size() == MAX_INTEREST_COUNT)
								footview.setVisibility(View.GONE);
							adapter.notifyDataSetChanged();
							llInput.setVisibility(View.GONE);
							llSel.setVisibility(View.VISIBLE);
							button.setEnabled(true);
						}
						else
						{
							CustomToast.show(MyInterestAct.this, String.format("标签 %s 已存在",s.toString()), Toast.LENGTH_SHORT);
						}
					else
						CustomToast.show(MyInterestAct.this,"标签不多于4个字符",Toast.LENGTH_SHORT);
				}
				else
				{
					llInput.setVisibility(View.GONE);
					llSel.setVisibility(View.VISIBLE);
					button.setEnabled(true);
				}
			}
		});
		btnCustom.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				llInput.setVisibility(View.VISIBLE);
				llSel.setVisibility(View.GONE);
				button.setEnabled(false);
			}
		});
		btnHotkey.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Intent I = new Intent(MyInterestAct.this, HotKeyActivity.class);
				startActivityForResult(I, HOTKEY_REQUEST);
			}
		});
		button.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Global.settings.interests.clear();
				Global.settings.interests.addAll(list);
				dialog = new SweetAlertDialog(MyInterestAct.this, SweetAlertDialog.PROGRESS_TYPE);
				dialog.setTitleText("请稍候...").setCancelable(false);
				dialog.show();
				new WebTask(handler, Global.MSG_WHAT.W_SENDED_REQUEST).execute("updateSettings", 2, "username", Global.mySelf.username, "setString", Global.settings.toJson());
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == HOTKEY_REQUEST)
		{
			if (resultCode == HotKeyActivity.GET_HOT_KEY)
			{
				String s = data.getStringExtra("result");
				if (!list.contains(s))
				{
					list.add(s);
					if (list.size() == MAX_INTEREST_COUNT)
						footview.setVisibility(View.GONE);
					adapter.notifyDataSetChanged();
				}
			}
		}
	}
}
