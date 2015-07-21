package org.helloworld;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.helloworld.game.GameSplash;
import org.helloworld.tools.Global;
import org.helloworld.tools.Settings;
import org.helloworld.tools.ShakeRecord;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * 摇一摇结果界面
 * */

public class ShakeResultAct extends BaseActivity
{
	private ListView lvResult;
	private ArrayList<ShakeRecord> result;
	public static final int PLAY_GAME = 3;

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
						NearbyStrangerAct.SayHello(ShakeResultAct.this, r.username, handler);
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
					case Global.MSG_WHAT.W_GOT_USER_SETTING:
					{
						Settings settings = (Settings) message.obj;
						if (message.getData().getBoolean("result"))
						{
							Intent intent;
							if (settings.game == 1)
								intent = new Intent(ShakeResultAct.this, GameSplash.class);
							else
							{
								//Todo 启动另外的游戏
								intent = new Intent();
							}
							intent.putExtra("strangerName", message.getData().getString("strangerName"));
							startActivityForResult(intent, PLAY_GAME);
						}
						else
						{
							SweetAlertDialog dialog2 = new SweetAlertDialog(ShakeResultAct.this, SweetAlertDialog.ERROR_TYPE);
							dialog2.setTitleText("错误").setContentText(Global.ERROR_HINT.HINT_ERROR_NETWORD);
							dialog2.setConfirmClickListener(null);
						}
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
		NearbyStrangerAct.DealGameResult(requestCode, resultCode, data, handler, ShakeResultAct.this);
	}
}
