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

import org.helloworld.tools.Global;
import org.helloworld.tools.ShakeRecord;

import java.util.ArrayList;

/**
 * 摇一摇结果界面
 */

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
		lvResult = (ListView) findViewById(R.id.lvResult);
		result = ShakeActivity.records;
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
				final ShakeRecord r = result.get(i);
				if (view == null)
				{
					h = new H();
					view = LayoutInflater.from(ShakeResultAct.this).inflate(R.layout.shake_result_item, viewGroup, false);
					h.btnSayHello = (Button) view.findViewById(R.id.btnSayHello);
					h.tvName = (TextView) view.findViewById(R.id.tvName);
					view.setTag(h);
				}
				else
					h = (H) view.getTag();
				if(Global.map2Friend.containsKey(r.username))
				{
					h.tvName.setText(Global.map2Friend.get(r.username).getShowName());
					h.btnSayHello.setVisibility(View.INVISIBLE);
				}
				else
				{
					h.tvName.setText(r.username);
					h.btnSayHello.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View view)
						{
							NearbyStrangerAct.SayHello(ShakeResultAct.this, r.username, handler);
						}
					});
				}
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
						NearbyStrangerAct.DealSendRequestResult(ShakeResultAct.this,handler, message);
						break;
					case Global.MSG_WHAT.W_GOT_USER_SETTING:
						NearbyStrangerAct.DealGetSettingResult(ShakeResultAct.this,handler, message);
						break;
					case Global.MSG_WHAT.W_DOWNLOADED_A_FILE:
						NearbyStrangerAct.DealDownloadGame(ShakeResultAct.this, message);
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
		((BaseAdapter) lvResult.getAdapter()).notifyDataSetChanged();
	}
}
