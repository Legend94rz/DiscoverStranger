package org.helloworld;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.*;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


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
						Intent intent = new Intent();
						intent.putExtra("strangerName", r.username);

						onActivityResult(0, SUCCESS_FINISH_GAME, intent);
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
						final Bundle data = message.getData();
						if (data.getBoolean("result"))
						{
							MainActivity.handler.sendEmptyMessage(Global.MSG_WHAT.W_REFRESH_DEEP);
							AlertDialog.Builder builder = new AlertDialog.Builder(ShakeResultAct.this);
							builder.setTitle(getString(R.string.HintTitle))
								.setMessage(getString(R.string.FinishAndAddFriendSuc))
								.setPositiveButton(getString(R.string.GotoChat), new DialogInterface.OnClickListener()
								{
									@Override
									public void onClick(DialogInterface dialogInterface, int i)
									{
										Intent intent = new Intent(ShakeResultAct.this, ChatActivity.class);
										intent.putExtra("chatTo", data.getString("strangerName"));
										startActivity(intent);
										finish();
									}
								})
								.setNegativeButton(getString(R.string.Later), null);
							builder.create().show();
						}
						else
						{
							AlertDialog.Builder builder =new AlertDialog.Builder(ShakeResultAct.this);
							builder.setTitle("错误")
								.setMessage("请求发送失败，是否重试?")
								.setPositiveButton("好", new DialogInterface.OnClickListener()
								{
									@Override
									public void onClick(DialogInterface dialogInterface, int i)
									{
										NearbyStrangerAct.SuccessFinishGame(ShakeResultAct.this,handler,data.getString("strangerName"));
									}
								})
								.setNegativeButton("算了",null);
							builder.create().show();

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
	}


	public void goback(View view)
	{
		finish();
	}
}
