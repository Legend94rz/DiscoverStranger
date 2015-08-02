package org.helloworld;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.helloworld.tools.Global;
import org.helloworld.tools.WebTask;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;


public class HotKeyActivity extends BaseActivity implements View.OnClickListener
{
	ListView listView;
	int curPage;
	ArrayAdapter<String> adapter;
	ArrayList<String> data;
	TextView tvMore;
	LinearLayout llLoading;
	Handler handler;
	private static final int PAGE_SIZE = 10;
	public static final int GET_HOT_KEY = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hot_key);
		curPage = 1;
		data = new ArrayList<>();
		listView = (ListView) findViewById(R.id.listView2);
		View view = View.inflate(this, R.layout.foot_view, null);
		tvMore = (TextView) view.findViewById(R.id.tvMore);
		llLoading = (LinearLayout) view.findViewById(R.id.llLoading);
		tvMore.setOnClickListener(this);
		listView.addFooterView(view, null, false);
		adapter = new ArrayAdapter<>(this, R.layout.item_hotkey, R.id.tvItem, data);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
			{
				Intent result = new Intent();
				result.putExtra("result", data.get(i));
				setResult(GET_HOT_KEY, result);
				finish();
			}
		});
		handler = new Handler(new Handler.Callback()
		{
			@Override
			public boolean handleMessage(Message message)
			{
				switch (message.what)
				{
					case Global.MSG_WHAT.W_GOT_HOTKEYLIST:
						tvMore.setVisibility(View.VISIBLE);
						llLoading.setVisibility(View.GONE);
						SoapObject so = (SoapObject) message.obj;
						if (so != null)
						{
							SoapObject r = (SoapObject) so.getProperty(0);
							if (r.getPropertyCount() > 0)
							{
								ArrayList<String> tmp = new ArrayList<>();
								for (int i = 0; i < r.getPropertyCount(); i++)
								{
									tmp.add(r.getPropertyAsString(i));
								}
								data.addAll(tmp);
								adapter.notifyDataSetChanged();
								curPage++;
							}
							else
							{
								tvMore.setEnabled(false);
								tvMore.setText("没有更多了");
							}
						}
						break;
				}
				return false;
			}
		});
		onClick(tvMore);
	}

	@Override
	public void onClick(View view)
	{
		switch (view.getId())
		{
			case R.id.tvMore:
				llLoading.setVisibility(View.VISIBLE);
				tvMore.setVisibility(View.GONE);
				new WebTask(handler, Global.MSG_WHAT.W_GOT_HOTKEYLIST).execute("getHotKeyByPage", 2, "pageSize", PAGE_SIZE, "pageIndex", curPage);
				break;
		}
	}
}
