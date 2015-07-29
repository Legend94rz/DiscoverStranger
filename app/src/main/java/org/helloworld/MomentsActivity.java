package org.helloworld;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.helloworld.tools.FileUtils;
import org.helloworld.tools.Fresh;
import org.helloworld.tools.Global;
import org.helloworld.tools.MomentsAdapter;
import org.helloworld.tools.WebTask;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;


public class MomentsActivity extends BaseActivity implements View.OnClickListener
{
	ListView listView;
	SwipeRefreshLayout swipeRefreshLayout;
	MomentsAdapter adapter;
	ArrayList<Fresh> freshs;
	ImageView ivMyHead;
	Handler handler;
	TextView tvMore;
	LinearLayout llLoading;
	public static final int PLAY_GAME = 3;
	private static final int COUNT = 15;
	@Override
	public void onClick(View view)
	{
		if(view.getId()==R.id.tvMore)
		{
			tvMore.setVisibility(View.GONE);
			llLoading.setVisibility(View.VISIBLE);
			String where = "";
			if (Global.settings.interests.size() > 0)
				where = "tag in" + Global.settings.getInterestList();
			if (freshs.size() > 0)
			{
				if (where.length() > 0) where += " and ";
				where += String.format("id<%s", freshs.get(freshs.size() - 1).id);
			}
			new WebTask(handler, Global.MSG_WHAT.W_GOT_FRESHES_OLD).execute("getFreshBy", 2, "count", COUNT, "where", where);
		}
		else if(view.getId()==R.id.tvSetInterest)
		{
			Intent i=new Intent(this,MyInterestAct.class);
			startActivity(i);
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_moments);
		findViewById(R.id.btnShare).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Intent I=new Intent(MomentsActivity.this,WriteFreshAct.class);
				startActivity(I);
			}
		});

		listView= (ListView) findViewById(R.id.listview);
		View footView=View.inflate(this,R.layout.foot_view,null);
		listView.addFooterView(footView,null,false);
		tvMore= (TextView) footView.findViewById(R.id.tvMore);
		llLoading= (LinearLayout) footView.findViewById(R.id.llLoading);
		tvMore.setOnClickListener(this);
		View headView = View.inflate(this,R.layout.moments_head,null);
		((TextView) headView.findViewById(R.id.tvNickName)).setText(Global.mySelf.nickName);
		headView.findViewById(R.id.tvSetInterest).setOnClickListener(this);
		ivMyHead= (ImageView)headView.findViewById(R.id.ivHead);
		ivMyHead.setImageBitmap(FileUtils.getOptimalBitmap(this,Global.PATH.HeadImg+Global.mySelf.username+".png",48*Global.DPI));
		listView.addHeaderView(headView,null,false);
		freshs=new ArrayList<>();
		handler=new Handler(new Handler.Callback()
		{
			@Override
			public boolean handleMessage(Message message)
			{
				switch (message.what)
				{
					case Global.MSG_WHAT.W_SENDED_REQUEST:
						NearbyStrangerAct.DealSendRequestResult(MomentsActivity.this, message);
						break;
					case Global.MSG_WHAT.W_GOT_USER_SETTING:
						NearbyStrangerAct.DealGetSettingResult(MomentsActivity.this, message);
						break;
					case Global.MSG_WHAT.W_GOT_FRESHES_OLD:
						tvMore.setVisibility(View.VISIBLE);
						llLoading.setVisibility(View.GONE);
						if (message.obj != null)
						{
							SoapObject soapObject= (SoapObject) ((SoapObject) message.obj).getProperty(0);
							int count=soapObject.getPropertyCount();
							for(int i=0;i<count;i++)
							{
								freshs.add(Fresh.parse((SoapObject) soapObject.getProperty(i)));
							}
							adapter.notifyDataSetChanged();
							if(count<COUNT)
							{
								tvMore.setEnabled(false);
								tvMore.setText("没有更多了");
							}
						}
						break;
					case Global.MSG_WHAT.W_GOT_FRESHES_NEW:
						swipeRefreshLayout.setRefreshing(false);
						if(message.obj!=null)
						{
							SoapObject soapObject= (SoapObject) ((SoapObject) message.obj).getProperty(0);
							int count=soapObject.getPropertyCount();
							if(count>0)
							{
								ArrayList<Fresh> tmp = new ArrayList<>();
								for (int i = 0; i < count; i++)
								{
									tmp.add(Fresh.parse((SoapObject) soapObject.getProperty(i)));
								}
								freshs.addAll(0, tmp);
								adapter.notifyDataSetChanged();
							}
						}
						break;
				}
				return false;
			}
		});
		adapter=new MomentsAdapter(this,freshs,listView,handler);
		listView.setAdapter(adapter);
		swipeRefreshLayout= (SwipeRefreshLayout) findViewById(R.id.pullrefresh);
		swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
		{
			@Override
			public void onRefresh()
			{
				if(tvMore.getVisibility()!=View.GONE)
				{
					swipeRefreshLayout.setRefreshing(false);
					return;
				}
				String where="";
				if(Global.settings.interests.size()>0)
					where="tag in" + Global.settings.getInterestList();
				if(freshs.size()>0)
				{
					if(where.length()>0)where+=" and ";
					where += String.format("id>%s", freshs.get(0).id);
				}
				new WebTask(handler,Global.MSG_WHAT.W_GOT_FRESHES_OLD).execute("getFreshBy", 2, "count", COUNT, "where", where);
			}
		});
		onClick(tvMore);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		NearbyStrangerAct.DealGameResult(requestCode, resultCode, data, handler, MomentsActivity.this);
	}
}
