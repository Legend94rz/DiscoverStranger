package org.helloworld;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.*;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.helloworld.tools.Global;
import org.helloworld.tools.ShakeRecord;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class ShakeResultAct extends Activity
{
	private ListView lvResult;
	private ArrayList<ShakeRecord> result;
	private final int SUCCESS_FINISH_GAME = 1;
	private final int FAIL_FINISH_GAME = 2;
	public static Handler handler;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shake_result);
		lvResult= (ListView) findViewById(R.id.lvResult);
		result=ShakeActivity.records;
		lvResult.setAdapter(new BaseAdapter()
		{
			@Override
			public int getCount()
			{
				return result.size();
			}
			@Override
			public Object getItem(int i)
			{
				return result.get(i);
			}
			@Override
			public long getItemId(int i)
			{
				return i;
			}
			@Override
			public View getView(int i, View view, ViewGroup viewGroup)
			{
				H h = null;
				final ShakeRecord r=result.get(i);
				if(view==null)
				{
					h=new H();
					view = LayoutInflater.from(ShakeResultAct.this).inflate(R.layout.shake_result_item,viewGroup,false);
					h.btnSayHello= (Button) view.findViewById(R.id.btnSayHello);
					h.tvName= (TextView) view.findViewById(R.id.tvName);
					view.setTag(h);
				}
				else
					h= (H) view.getTag();
				h.tvName.setText(r.username);
				h.btnSayHello.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View view)
					{
						NearbyStrangerAct.SayHello(r.username, ShakeResultAct.this);
					}
				});

				return view;
			}
			class H
			{
				TextView tvName;
				Button btnSayHello;
			}
		});
		handler = new Handler(new Handler.Callback()
		{
			@Override
			public boolean handleMessage(Message message)
			{
				switch (message.what)
				{
					case Global.MSG_WHAT.W_SENDED_REQUEST:
						final Bundle data=message.getData();
						if(data.getBoolean("result"))
						{
							MainActivity.handler.sendEmptyMessage(Global.MSG_WHAT.W_REFRESH_DEEP);
							final SweetAlertDialog n = new SweetAlertDialog(ShakeResultAct.this, SweetAlertDialog.SUCCESS_TYPE);
							n.setContentText("你们已经成为好友啦");
							n.setConfirmText("好");
							n.setConfirmClickListener(null);
							n.setContentText(getString(R.string.FinishAndAddFriendSuc));
							n.show();
						}
						else
						{
							final SweetAlertDialog n = new SweetAlertDialog(ShakeResultAct.this, SweetAlertDialog.ERROR_TYPE);
							n.setContentText("请求发送失败，是否重试?");
							n.setConfirmText("好");
							n.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener()
							{
								@Override
								public void onClick(SweetAlertDialog sweetAlertDialog)
								{
									NearbyStrangerAct.SuccessFinishGame(ShakeResultAct.this, handler, data.getString("strangerName"));
									n.dismiss();
								}
							});
							n.setCancelText("算了");
							n.setCancelClickListener(null);
							n.show();
						}
						break;
				}
				return true;
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
//		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode==SUCCESS_FINISH_GAME)
		{
			NearbyStrangerAct.SuccessFinishGame(ShakeResultAct.this,handler,data.getStringExtra("strangerName"));
		}
		else
		{
			new SweetAlertDialog(this)
				.setContentText("很遗憾，你没有通过对方的游戏，不能加他为好友")
				.setConfirmText("知道了").setConfirmClickListener(null).show();
		}
	}
	
	public void goback(View view)
	{
		finish();
	}
}
